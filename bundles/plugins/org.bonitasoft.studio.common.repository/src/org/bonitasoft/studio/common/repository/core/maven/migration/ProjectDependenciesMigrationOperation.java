/**
 * Copyright (C) 2021 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.common.repository.core.maven.migration;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.bonitasoft.studio.common.repository.AbstractRepository;
import org.bonitasoft.studio.common.repository.Messages;
import org.bonitasoft.studio.common.repository.core.InputStreamSupplier;
import org.bonitasoft.studio.common.repository.core.maven.DependencyGetOperation;
import org.bonitasoft.studio.common.repository.core.maven.JarLookupOperation;
import org.bonitasoft.studio.common.repository.core.maven.migration.model.DependencyLookup;
import org.bonitasoft.studio.common.repository.core.maven.migration.model.DependencyLookup.Status;
import org.bonitasoft.studio.common.repository.core.maven.migration.model.GAV;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class ProjectDependenciesMigrationOperation implements IRunnableWithProgress {

    public static final String MAVEN_CENTRAL_REPOSITORY_URL = "https://repo.maven.apache.org/maven2/";
    private static final String BDM_CLIENT_POJO_JAR = "bdm-client-pojo.jar";

    private List<InputStreamSupplier> jars;
    private Set<DependencyLookup> result = new HashSet<>();
    private Set<String> repositories = new HashSet<>();
    private Set<String> usedDependencies = new HashSet<>();
    private Set<String> usedDefinitions = new HashSet<>();

    public ProjectDependenciesMigrationOperation(List<InputStreamSupplier> jars) {
        this.jars = jars;
    }

    public ProjectDependenciesMigrationOperation addRemoteRespository(String repositoryUrl) {
        this.repositories.add(repositoryUrl);
        return this;
    }

    public ProjectDependenciesMigrationOperation addUsedDependencies(Set<String> usedDependencies) {
        this.usedDependencies = usedDependencies;
        return this;
    }

    public ProjectDependenciesMigrationOperation addUsedDefinitions(Set<String> usedDefinitions) {
        this.usedDefinitions = usedDefinitions;
        return this;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask(Messages.convertingDependencies, jars.size());
        List<BonitaJarDependencyReplacement> replacements = BonitaJarDependencyReplacement
                .getBonitaJarDependencyReplacements();

        List<String> jarNames = jars.stream()
                .map(InputStreamSupplier::getName)
                .collect(Collectors.toList());

        Set<String> dependenciesToRemove = jarNames.stream()
                .filter(jarName -> replacements.stream().anyMatch(r -> r.matches(jarName)))
                .collect(Collectors.toSet());

        // Search Bonita connectors/actor filters replacements
        List<DependencyLookup> providedDependencies = jarNames.stream()
                .flatMap(jarName -> replacements.stream()
                        .filter(r -> r.matches(jarName))
                        .map(r -> newDependencyLookup(jarName, r, usedDependencies.contains(jarName))))
                .peek(jar -> monitor.worked(1))
                .collect(Collectors.toList());

        replacements.stream()
                .filter(r -> usedDefinitions.stream().anyMatch(r::matchesDefinition))
                .map(r -> newDependencyLookup(null, r, true))
                .forEach(providedDependencies::add);

        for (DependencyLookup dl : providedDependencies) {
            Optional<DependencyLookup> search = result.stream()
                    .filter(d -> Objects.equals(dl.getArtifactId(), d.getArtifactId())
                            && Objects.equals(dl.getGroupId(), d.getGroupId()))
                    .findAny();
            if (search.isPresent()) {
                DependencyLookup existingDep = search.get();
                if (!existingDep.isUsed()) {
                    result.remove(existingDep);
                    result.add(dl);
                }
            } else {
                result.add(dl);
            }
        }

        // Remove all transitive jar from bonita artifacts
        jars.stream()
                .filter(jarInputStreamProvider -> dependenciesToRemove.contains(jarInputStreamProvider.getName()))
                .flatMap(jarInputStreamProvider -> BonitaJarDependencyReplacement
                        .getTransitiveDependencies(jarInputStreamProvider.toTempFile()).stream())
                .peek(jar -> monitor.worked(1))
                .forEach(dependenciesToRemove::add);

        dependenciesToRemove.add(BDM_CLIENT_POJO_JAR);

        Set<InputStreamSupplier> jarsToLookup = jars.stream()
                .filter(f -> !dependenciesToRemove.contains(f.getName()))
                .collect(Collectors.toSet());
        for (InputStreamSupplier jarToLookup : jarsToLookup) {
            DependencyLookup dependencyLookup = lookupDependency(monitor, jarToLookup);
            if (dependencyLookup != null) {
                if (usedDependencies.contains(jarToLookup.getName())) {
                    dependencyLookup.setUsed(true);
                }
                if (isLatestVersion(dependencyLookup, result)) {
                    result.add(dependencyLookup);
                }
                monitor.worked(1);
            }
        }

    }

    private DependencyLookup lookupDependency(IProgressMonitor monitor, InputStreamSupplier jarToLookup)
            throws InvocationTargetException, InterruptedException {
        DependencyLookup dep = null;
        JarLookupOperation jarLookupOperation = new JarLookupOperation(jarToLookup);
        repositories.stream()
                .forEach(jarLookupOperation::addRemoteRespository);
        monitor.setTaskName(String.format(Messages.lookupDependencyFor, jarToLookup.getName()));
        jarLookupOperation.run(AbstractRepository.NULL_PROGRESS_MONITOR);
        var status = jarLookupOperation.getStatus();
        if (status.isOK()) {
            dep = jarLookupOperation.getResult();
            if (dep.getStatus() == Status.FOUND
                    && DependencyLookup.guessClassifier(jarToLookup.getName(), dep.getGAV()) != null) {
                GAV gavWithClassifier = dep.getGAV();
                gavWithClassifier.setClassifier(DependencyLookup.guessClassifier(jarToLookup.getName(), dep.getGAV()));
                var dependencyGetOperation = new DependencyGetOperation(gavWithClassifier);
                repositories.stream()
                        .forEach(dependencyGetOperation::addRemoteRespository);
                monitor.setTaskName(String.format(Messages.lookupDependencyFor, dep.getGAV()));
                dependencyGetOperation.run(monitor);
                status = dependencyGetOperation.getStatus();
                if (status.isOK() && dependencyGetOperation.getResult() != null) {
                    dep = dependencyGetOperation.getResult();
                }
            } else if (dep.getStatus() == Status.NOT_FOUND) {
                GAV gav = DependencyLookup.readPomProperties(jarToLookup.toTempFile())
                        .map(properties -> new GAV(properties.getProperty("groupId"),
                                properties.getProperty("artifactId"),
                                properties.getProperty("version")))
                        .orElse(null);
                if (gav != null) {
                    var dependencyGetOperation = new DependencyGetOperation(gav);
                    repositories.stream()
                            .forEach(dependencyGetOperation::addRemoteRespository);
                    monitor.setTaskName(String.format(Messages.lookupDependencyFor, gav));
                    dependencyGetOperation.run(monitor);
                    status = dependencyGetOperation.getStatus();
                    if (status.isOK()) {
                        if (dependencyGetOperation.getResult() != null) {
                            dep = dependencyGetOperation.getResult();
                        } else if (DependencyLookup.guessClassifier(jarToLookup.getName(), gav) != null) {
                            gav.setClassifier(DependencyLookup.guessClassifier(jarToLookup.getName(), gav));
                            dependencyGetOperation = new DependencyGetOperation(gav);
                            repositories.stream()
                                    .forEach(dependencyGetOperation::addRemoteRespository);
                            dependencyGetOperation.run(monitor);
                            status = dependencyGetOperation.getStatus();
                            if (status.isOK() && dependencyGetOperation.getResult() != null) {
                                dep = dependencyGetOperation.getResult();
                            }
                        }
                    }
                }
                if(dep.getStatus() == Status.NOT_FOUND && dep.getFile().exists()) {
                    dep.setStatus(Status.LOCAL);
                }
            }
        }
        return dep;
    }

    private DependencyLookup newDependencyLookup(String jarName,
            BonitaJarDependencyReplacement dep,
            boolean isUsed) {
        DependencyLookup dependencyLookup = new DependencyLookup(jarName,
                null, // no sha1
                DependencyLookup.Status.FOUND,
                new GAV(dep.getMavenDependency()),
                MAVEN_CENTRAL_REPOSITORY_URL);
        dependencyLookup.setUsed(isUsed);
        return dependencyLookup;
    }

    private boolean isLatestVersion(DependencyLookup depToAdd, Collection<DependencyLookup> dependencies) {
        List<DependencyLookup> conflictingDependencies = dependencies.stream()
                .filter(existingDep -> Objects.equals(depToAdd.getGroupId(), existingDep.getGroupId())
                        && Objects.equals(depToAdd.getArtifactId(), existingDep.getArtifactId()))
                .sorted((d1, d2) -> new DefaultArtifactVersion(d1.getVersion())
                        .compareTo(new DefaultArtifactVersion(d2.getVersion())))
                .collect(Collectors.toList());
        if (conflictingDependencies.isEmpty()) { // No conflicting deps
            return true;
        } else if (conflictingDependencies.get(0).equals(depToAdd)) { // Conflicting deps found and depToAdd is the latest version
            dependencies.removeAll(conflictingDependencies);
            return true;
        }
        return false; // Conflicting deps found and depToAdd is not the latest version
    }

    public Set<DependencyLookup> getResult() {
        return result;
    }

}
