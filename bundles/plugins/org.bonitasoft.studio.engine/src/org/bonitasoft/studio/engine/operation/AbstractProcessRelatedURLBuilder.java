/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.studio.engine.operation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bonitasoft.studio.common.CommandExecutor;
import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.common.repository.model.IRepositoryFileStore;
import org.bonitasoft.studio.common.repository.model.ReadFileStoreException;
import org.bonitasoft.studio.configuration.ConfigurationPlugin;
import org.bonitasoft.studio.configuration.preferences.ConfigurationPreferenceConstants;
import org.bonitasoft.studio.diagram.custom.repository.ProcessConfigurationRepositoryStore;
import org.bonitasoft.studio.model.configuration.Configuration;
import org.bonitasoft.studio.model.process.AbstractProcess;

public abstract class AbstractProcessRelatedURLBuilder extends AbstractBonitaURLBuilder {

    private static final String FIND_USER_PASSWORD_COMMAND = "org.bonitasoft.studio.actors.command.userPassword";
    protected final AbstractProcess process;
    protected String configurationId;

    private CommandExecutor commandExecutor = new CommandExecutor();

    protected AbstractProcessRelatedURLBuilder(final AbstractProcess process, final String configurationId) {
        this.process = process;
        this.configurationId = configurationId;
    }

    protected Configuration getConfiguration() {
        if (process != null) {
            initConfigurationId();
            if (ConfigurationPreferenceConstants.LOCAL_CONFIGURATION.equals(configurationId)) {
                return retrieveConfigurationForLocalConf();
            } else {
                return retrieveConfigurationInsideProcess();
            }
        }
        return null;
    }

    @Override
    protected String buildLoginUrl() {
        String userName = getDefaultUsername();
        String password = getDefaultPassword();

        final Configuration conf = getConfiguration();
        if (conf != null && conf.getUsername() != null) {
            userName = conf.getUsername();
            password = retrieveUserPasswordFromActiveOrga(userName)
                    .orElseThrow(() -> new RuntimeException(
                            String.format("Unable to retrieve the password of %s in the active organization.",
                                    conf.getUsername())));
        }

        return buildLoginUrl(userName, password);
    }

    protected Optional<String> retrieveUserPasswordFromActiveOrga(String user) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("userName", user);
        Object result = commandExecutor.executeCommand(FIND_USER_PASSWORD_COMMAND, parameters);
        return result instanceof Optional ? (Optional<String>) result : Optional.empty();
    }

    protected void initConfigurationId() {
        if (configurationId == null) {
            configurationId = ConfigurationPlugin
                    .getDefault()
                    .getPreferenceStore()
                    .getString(
                            ConfigurationPreferenceConstants.DEFAULT_CONFIGURATION);
        }
    }

    private Configuration retrieveConfigurationInsideProcess() {
        for (final Configuration conf : process.getConfigurations()) {
            if (configurationId.equals(conf.getName())) {
                return conf;
            }
        }
        return null;
    }

    private Configuration retrieveConfigurationForLocalConf() {
        final ProcessConfigurationRepositoryStore processConfStore = RepositoryManager.getInstance().getRepositoryStore(
                ProcessConfigurationRepositoryStore.class);
        final String id = ModelHelper.getEObjectID(process);
        final IRepositoryFileStore file = processConfStore.getChild(id
                + ".conf", true);
        if (file == null) {
            return null;
        }
        try {
            return (Configuration) file.getContent();
        } catch (final ReadFileStoreException e) {
            BonitaStudioLog.error("Failed to read process configuration", e);
        }
        return null;
    }
}
