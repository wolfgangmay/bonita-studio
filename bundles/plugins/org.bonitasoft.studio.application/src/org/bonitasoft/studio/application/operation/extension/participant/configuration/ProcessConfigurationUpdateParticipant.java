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
package org.bonitasoft.studio.application.operation.extension.participant.configuration;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.bonitasoft.bpm.model.configuration.Configuration;
import org.bonitasoft.studio.application.i18n.Messages;
import org.bonitasoft.studio.application.operation.extension.participant.configuration.preview.JarAddedChange;
import org.bonitasoft.studio.application.operation.extension.participant.configuration.preview.JarRemovedChange;
import org.bonitasoft.studio.application.operation.extension.participant.configuration.preview.JarUpdatedChange;
import org.bonitasoft.studio.application.operation.extension.participant.preview.PreviewResultImpl;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.AbstractRepository;
import org.bonitasoft.studio.common.repository.core.maven.ProjectDependenciesResolver;
import org.bonitasoft.studio.common.repository.core.migration.dependencies.configuration.ProcessConfigurationChange;
import org.bonitasoft.studio.common.repository.core.migration.dependencies.configuration.ProcessConfigurationUpdater;
import org.bonitasoft.studio.common.repository.extension.update.DependencyUpdate;
import org.bonitasoft.studio.common.repository.extension.update.participant.ExtensionUpdateParticipant;
import org.bonitasoft.studio.common.repository.extension.update.preview.ChangePreview;
import org.bonitasoft.studio.common.repository.extension.update.preview.PreviewMessageProvider;
import org.bonitasoft.studio.common.repository.extension.update.preview.PreviewResult;
import org.bonitasoft.studio.common.repository.model.IRepository;
import org.bonitasoft.studio.connectors.repository.DatabaseConnectorPropertiesRepositoryStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

public class ProcessConfigurationUpdateParticipant implements ExtensionUpdateParticipant {

    private ProcessConfigurationCollector configurationCollector;
    private List<DependencyUpdate> dependenciesUpdates;
    private IRepository repository;
    private ProjectDependenciesResolver projectDependenciesResolver;
    private PreviewResult previewResult;
    private Map<Artifact, Set<Artifact>> currentArtifacts;
    private ProcessConfigurationUpdater processConfigurationUpdater;
    private Set<Resource> modifiedResources;

    public ProcessConfigurationUpdateParticipant(List<DependencyUpdate> dependenciesUpdates,
            ProcessConfigurationCollector configurationCollector,
            ProjectDependenciesResolver projectDependenciesResolver,
            ProcessConfigurationUpdater processConfigurationUpdater,
            IRepository repository) {
        this.dependenciesUpdates = dependenciesUpdates;
        this.configurationCollector = configurationCollector;
        this.projectDependenciesResolver = projectDependenciesResolver;
        this.processConfigurationUpdater = processConfigurationUpdater;
        this.repository = repository;
    }

    @Override
    public void preUpdate(IProgressMonitor monitor) {
        monitor.beginTask(Messages.preparingProcessConfigurationUpdate, IProgressMonitor.UNKNOWN);
        try {
            var mavenProject = getMavenProject();
            currentArtifacts = dependenciesUpdates.stream()
                    .filter(update -> isJarDependency(update.getCurrentDependency()))
                    .map(update -> toArtifact(mavenProject, update.getCurrentDependency()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(a -> a, this::transitiveDependencies));
        } catch (CoreException e) {
            BonitaStudioLog.error(e);
        }

    }

    @Override
    public PreviewResult runPreview(IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(Messages.updatePreview, IProgressMonitor.UNKNOWN);

        var mavenProject = getMavenProject();
        previewResult = new PreviewResultImpl();

        var updatedArtifacts = new HashSet<Artifact>();
        var updateRename = new HashSet<Artifact>();

        dependenciesUpdates.stream()
                .filter(update -> update.getUpdatedDependency() != null)
                .filter(update -> isJarDependency(update.getUpdatedDependency()))
                .forEach(update -> {
                    var artifact = toArtifact(mavenProject, update.getUpdatedDependency());
                    if (artifact != null) {
                        updatedArtifacts.add(artifact);
                        if (update.isRename()) {
                            updateRename.add(artifact);
                        }
                    }
                });

        for (var artifact : currentArtifacts.keySet()) {
            var key = new ArtifactKey(artifact);
            updatedArtifacts.stream()
                    .filter(a -> updateRename.contains(a) || existsInAnotherVersion(new ArtifactKey(a), key))
                    .findFirst()
                    .ifPresent(
                            updatedArtifact -> {
                                var configurations = configurationCollector
                                        .findArtifactDependingOn(artifact.getFile().getName());
                                previewResult
                                        .addChange(createUpdateChanges(artifact, updatedArtifact, configurations));
                            });
            if (updatedArtifacts.stream().noneMatch(a -> updateRename.contains(a)
                    || existsInSameVersion(new ArtifactKey(a), key)
                    || existsInAnotherVersion(new ArtifactKey(a), key))) {
                // Artifact has been removed
                var configurations = configurationCollector
                        .findArtifactDependingOn(artifact.getFile().getName());
                previewResult.addChange(
                        createRemovedChange(artifact, configurations));
            }
        }
        return previewResult;
    }

    private MavenProject getMavenProject() throws CoreException {
        IMavenProjectFacade projectFacade = MavenPlugin.getMavenProjectRegistry().getProject(repository.getProject());
        var mavenProject = projectFacade.getMavenProject();
        if (mavenProject == null) {
            mavenProject = projectFacade.getMavenProject(new NullProgressMonitor());
        }
        // We wait for project artifacts to be resolved as it is done asynchronously.
        int nbTry = 0;
        while (mavenProject.getArtifacts().isEmpty() && nbTry < 50) {
            try {
                Thread.sleep(20);
                nbTry++;
            } catch (InterruptedException e) {
                BonitaStudioLog.error(e);
            }
        }
        return mavenProject;
    }

    private boolean isJarDependency(Dependency dependency) {
        return Objects.equals(dependency.getType(), "jar");
    }

    private JarRemovedChange createRemovedChange(Artifact artifact, Collection<Configuration> configurations) {
        var change = new JarRemovedChange(artifact.getFile().getName(), configurations);
        var artifactDependencyTree = currentArtifacts.get(artifact);
        artifactDependencyTree.stream()
                .map(Artifact::getFile)
                .map(File::getName)
                .map(jarName -> new JarRemovedChange(jarName, change))
                .forEach(change::addChangeDetail);
        return change;
    }

    private ChangePreview createUpdateChanges(Artifact artifact, Artifact updatedArtifact,
            Collection<Configuration> configurations) {
        JarUpdatedChange change = new JarUpdatedChange(updatedArtifact, artifact, configurations);

        var artifactDependencyTree = currentArtifacts.get(artifact);
        var updatedArtifactDependencyTree = transitiveDependencies(updatedArtifact);

        for (var previousArtifactDependency : artifactDependencyTree) {
            var key = new ArtifactKey(previousArtifactDependency);
            if (updatedArtifactDependencyTree.stream().noneMatch(a -> existsInSameVersion(new ArtifactKey(a), key)
                    || existsInAnotherVersion(new ArtifactKey(a), key))) {
                // Artifact has been removed with the updated
                change.addChangeDetail(
                        new JarRemovedChange(previousArtifactDependency.getFile().getName(), change));
            } else {
                updatedArtifactDependencyTree.stream()
                        .filter(a -> existsInAnotherVersion(new ArtifactKey(a), key))
                        .findFirst()
                        .ifPresent(a ->
                        // Artifact has been updated with the updated
                        change.addChangeDetail(new JarUpdatedChange(a, previousArtifactDependency, null, change)));
            }
        }
        for (var newArtifactDependency : updatedArtifactDependencyTree) {
            var key = new ArtifactKey(newArtifactDependency);
            if (artifactDependencyTree.stream().noneMatch(a -> existsInSameVersion(new ArtifactKey(a), key)
                    || existsInAnotherVersion(new ArtifactKey(a), key))) {
                // New artifact has been added
                change.addChangeDetail(new JarAddedChange(newArtifactDependency, change));
            }
        }

        return change;
    }

    private Artifact toArtifact(MavenProject mavenProject, Dependency currentDependency) {
        ArtifactKey artifactKey = new ArtifactKey(currentDependency.getGroupId(), currentDependency.getArtifactId(),
                currentDependency.getVersion(), currentDependency.getClassifier());
        Set<Artifact> artifacts = mavenProject.getArtifacts();
        return artifacts.stream()
                .filter(a -> Objects.equals(new ArtifactKey(a), artifactKey))
                .findFirst()
                .orElse(null);
    }

    private Set<Artifact> transitiveDependencies(Artifact artifact) {
        try {
            return projectDependenciesResolver
                    .getTransitiveDependencies(artifact,
                            AbstractRepository.NULL_PROGRESS_MONITOR)
                    .stream()
                    .collect(Collectors.toSet());
        } catch (CoreException e) {
            BonitaStudioLog.error(e);
        }
        return Set.of();
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask(Messages.updatingConfiguration, IProgressMonitor.UNKNOWN);

        if (previewResult == null) {
            try {
                previewResult = runPreview(new NullProgressMonitor());
            } catch (CoreException e) {
                throw new InvocationTargetException(e.getCause());
            }
        }

        modifiedResources = previewResult.getChanges().stream()
                .filter(ProcessConfigurationChange.class::isInstance)
                .map(ProcessConfigurationChange.class::cast)
                .flatMap(change -> processConfigurationUpdater.update(change).stream())
                .collect(Collectors.toSet());

        var dbConfStore = repository.getRepositoryStore(DatabaseConnectorPropertiesRepositoryStore.class);
        previewResult.getChanges().stream()
                .filter(DatabaseConnectorConfigurationChange.class::isInstance)
                .map(DatabaseConnectorConfigurationChange.class::cast)
                .forEach(change -> change.apply(dbConfStore));
    }

    @Override
    public Collection<Resource> getModifiedResources() {
        return modifiedResources;
    }

    private boolean sameGAC(ArtifactKey updatedArtifact, ArtifactKey currentArtifact) {
        return currentArtifact.groupId().equals(updatedArtifact.groupId())
                && currentArtifact.artifactId().equals(updatedArtifact.artifactId())
                && Objects.equals(currentArtifact.classifier(), updatedArtifact.classifier());
    }

    private boolean existsInAnotherVersion(ArtifactKey updatedArtifact, ArtifactKey currentArtifact) {
        return sameGAC(updatedArtifact, currentArtifact)
                && !currentArtifact.version().equals(updatedArtifact.version());
    }

    private boolean existsInSameVersion(ArtifactKey updatedArtifact, ArtifactKey currentArtifact) {
        return sameGAC(updatedArtifact, currentArtifact)
                && currentArtifact.version().equals(updatedArtifact.version());
    }

    @Override
    public PreviewResult getPreviewResult() {
        return previewResult;
    }

    @Override
    public PreviewMessageProvider getPreviewMessageProvider() {
        return new ProcessConfigurationPreviewMessageProvider();
    }

}
