/**
 * Copyright (C) 2009-2011 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.studio.application;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.bonitasoft.studio.application.contribution.IPreShutdownContribution;
import org.bonitasoft.studio.application.contribution.RecoverWorkspaceContribution;
import org.bonitasoft.studio.application.dialog.ExitDialog;
import org.bonitasoft.studio.application.handler.OpenReleaseNoteHandler;
import org.bonitasoft.studio.application.i18n.Messages;
import org.bonitasoft.studio.common.DateUtil;
import org.bonitasoft.studio.common.FileUtil;
import org.bonitasoft.studio.common.extension.BonitaStudioExtensionRegistryManager;
import org.bonitasoft.studio.common.extension.IPostStartupContribution;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.net.PortSelector;
import org.bonitasoft.studio.common.repository.AbstractRepository;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.common.repository.core.ActiveOrganizationProvider;
import org.bonitasoft.studio.common.ui.PlatformUtil;
import org.bonitasoft.studio.designer.core.UIDesignerServerManager;
import org.bonitasoft.studio.engine.BOSEngineManager;
import org.bonitasoft.studio.engine.BOSWebServerManager;
import org.bonitasoft.studio.engine.EnginePlugin;
import org.bonitasoft.studio.engine.preferences.EnginePreferenceConstants;
import org.bonitasoft.studio.model.process.diagram.part.ProcessDiagramEditorPlugin;
import org.bonitasoft.studio.preferences.BonitaPreferenceConstants;
import org.bonitasoft.studio.preferences.BonitaStudioPreferencesPlugin;
import org.bonitasoft.studio.preferences.BonitaThemeConstants;
import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogBlockedHandler;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.browser.WebBrowserUtil;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;
import org.osgi.framework.Bundle;

public class BonitaStudioWorkbenchAdvisor extends WorkbenchAdvisor implements IStartup {

    private static final String BONITA_STUDIO_SKIP_RELEASE_NOTE_SYSTEM_PROPERTY = "bonita.studio.skipReleaseNote";
    private static final String AWT_DRAW_STRING_AS_IMAGE = "drawStringAsImage";
    private static final Set<String> EDITOR_TYPE_TO_CLOSE_ON_EXIT = new HashSet<>();

    static {
        EDITOR_TYPE_TO_CLOSE_ON_EXIT.add("org.bonitasoft.studio.customProfile.editor");
        EDITOR_TYPE_TO_CLOSE_ON_EXIT.add("org.bonitasoft.studio.la.editor");
    }

    private final class PreShutdownStudio implements IRunnableWithProgress {

        @Override
        public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            monitor.beginTask(Messages.shuttingDown, IProgressMonitor.UNKNOWN);
            UIDesignerServerManager.getInstance().stop();
            Job.getJobManager().cancel(RepositoryManager.class);
            RepositoryManager.getInstance().getCurrentRepository()
                    .ifPresent(org.bonitasoft.studio.common.repository.model.IRepository::disableOpenIntroListener);
            executePreShutdownContribution();
            new ActiveOrganizationProvider().flush();
            if (BOSWebServerManager.getInstance().serverIsStarted() && BOSEngineManager.getInstance().isRunning()) {
                BOSEngineManager.getInstance().stop();
            }
            deleteH2DatabasesFiles();
            deleteTomcatTempDir();
            monitor.done();
        }

        private void deleteH2DatabasesFiles() {
            RepositoryManager.getInstance().getCurrentRepository().ifPresent(currentRepository -> {
                if (BonitaStudioPreferencesPlugin.getDefault().getPreferenceStore()
                        .getBoolean(BonitaPreferenceConstants.DELETE_TENANT_ON_EXIT)) {
                    currentRepository.getDatabaseHandler().removeEngineDatabase();
                }
                if (dropBusinessDataDBOnExit()) {
                    currentRepository.getDatabaseHandler().removeBusinessDataDatabase();
                }
            });
        }

        private boolean dropBusinessDataDBOnExit() {
            final IPreferenceStore preferenceStore = EnginePlugin.getDefault().getPreferenceStore();
            return preferenceStore.getBoolean(EnginePreferenceConstants.DROP_BUSINESS_DATA_DB_ON_EXIT_PREF);
        }

        private void deleteTomcatTempDir() {
            File tempDir = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() + File.separator
                    + "tomcat" + File.separator + "server" + File.separator + "temp");
            if (tempDir.exists()) {
                FileUtil.deleteDir(tempDir);
            }
        }

        private void executePreShutdownContribution() {
            final IConfigurationElement[] elements = BonitaStudioExtensionRegistryManager.getInstance()
                    .getConfigurationElements(
                            "org.bonitasoft.studio.application.preshutdown"); //$NON-NLS-1$
            IPreShutdownContribution contrib = null;
            for (final IConfigurationElement elem : elements) {
                try {
                    contrib = (IPreShutdownContribution) elem.createExecutableExtension("class"); //$NON-NLS-1$
                    contrib.execute();
                } catch (final CoreException e) {
                    BonitaStudioLog.error(e);
                }
            }
        }
    }

    private static final String FIRST_STARTUP = "firstStartup";

    @Override
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(final IWorkbenchWindowConfigurer configurer) {
        return new BonitaStudioWorkbenchWindowAdvisor(configurer);
    }

    @Override
    public void initialize(final IWorkbenchConfigurer configurer) {
        super.initialize(configurer);
        configurer.setSaveAndRestore(true);
        final IContextService contextService = PlatformUI.getWorkbench().getService(IContextService.class);
        contextService.activateContext("org.bonitasoft.studio.context.id");
        initializeIDEImages(configurer);
    }

    /**
     * Workaround to load icons for Common Navigator component in a RCP
     */
    private void initializeIDEImages(final IWorkbenchConfigurer configurer) {
        IDE.registerAdapters();
        declareWorkbenchImages();
    }

    /********************************************************************
     * /!\ Copy-pasted from IDEWorkbenchAvisor.declareWorkbenchImages() *
     * ******************************************************************
     * Declares all IDE-specific workbench images. This includes both "shared"
     * images (named in {@link org.eclipse.ui.ide.IDE.SharedImages}) and internal images (named in
     * {@link org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages}).
     *
     * @see IWorkbenchConfigurer#declareImage
     */
    private void declareWorkbenchImages() {

        final String ICONS_PATH = "$nl$/icons/full/";//$NON-NLS-1$
        final String PATH_ELOCALTOOL = ICONS_PATH + "elcl16/"; // Enabled //$NON-NLS-1$

        // toolbar
        // icons.
        final String PATH_DLOCALTOOL = ICONS_PATH + "dlcl16/"; // Disabled //$NON-NLS-1$
        // //$NON-NLS-1$
        // toolbar
        // icons.
        final String PATH_ETOOL = ICONS_PATH + "etool16/"; // Enabled toolbar //$NON-NLS-1$
        // //$NON-NLS-1$
        // icons.
        final String PATH_DTOOL = ICONS_PATH + "dtool16/"; // Disabled toolbar //$NON-NLS-1$
        // //$NON-NLS-1$
        // icons.
        final String PATH_OBJECT = ICONS_PATH + "obj16/"; // Model object //$NON-NLS-1$
        // //$NON-NLS-1$
        // icons
        final String PATH_WIZBAN = ICONS_PATH + "wizban/"; // Wizard //$NON-NLS-1$
        // //$NON-NLS-1$
        // icons

        // View icons
        final String PATH_EVIEW = ICONS_PATH + "eview16/"; //$NON-NLS-1$

        final Bundle ideBundle = Platform.getBundle(IDEWorkbenchPlugin.IDE_WORKBENCH);

        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_ETOOL_BUILD_EXEC, PATH_ETOOL
                        + "build_exec.png", //$NON-NLS-1$
                false);
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_ETOOL_BUILD_EXEC_HOVER,
                PATH_ETOOL + "build_exec.png", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_ETOOL_BUILD_EXEC_DISABLED,
                PATH_DTOOL + "build_exec.png", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_ETOOL_SEARCH_SRC, PATH_ETOOL
                        + "search_src.png", //$NON-NLS-1$
                false);
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_ETOOL_SEARCH_SRC_HOVER,
                PATH_ETOOL + "search_src.png", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_ETOOL_SEARCH_SRC_DISABLED,
                PATH_DTOOL + "search_src.png", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_ETOOL_NEXT_NAV, PATH_ETOOL
                        + "next_nav.png", //$NON-NLS-1$
                false);

        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_ETOOL_PREVIOUS_NAV, PATH_ETOOL
                        + "prev_nav.png", //$NON-NLS-1$
                false);

        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_WIZBAN_NEWPRJ_WIZ, PATH_WIZBAN
                        + "newprj_wiz.png", //$NON-NLS-1$
                false);
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_WIZBAN_NEWFOLDER_WIZ,
                PATH_WIZBAN + "newfolder_wiz.png", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_WIZBAN_NEWFILE_WIZ, PATH_WIZBAN
                        + "newfile_wiz.png", //$NON-NLS-1$
                false);

        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_WIZBAN_IMPORTDIR_WIZ,
                PATH_WIZBAN + "importdir_wiz.png", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_WIZBAN_IMPORTZIP_WIZ,
                PATH_WIZBAN + "importzip_wiz.png", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_WIZBAN_EXPORTDIR_WIZ,
                PATH_WIZBAN + "exportdir_wiz.png", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_WIZBAN_EXPORTZIP_WIZ,
                PATH_WIZBAN + "exportzip_wiz.png", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_WIZBAN_RESOURCEWORKINGSET_WIZ,
                PATH_WIZBAN + "workset_wiz.png", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_DLGBAN_SAVEAS_DLG, PATH_WIZBAN
                        + "saveas_wiz.png", //$NON-NLS-1$
                false);

        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_DLGBAN_QUICKFIX_DLG, PATH_WIZBAN
                        + "quick_fix.png", //$NON-NLS-1$
                false);

        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJ_PROJECT,
                PATH_OBJECT + "prj_obj.png", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED, PATH_OBJECT
                        + "cprj_obj.png", //$NON-NLS-1$
                true);
        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OPEN_MARKER,
                PATH_ELOCALTOOL + "gotoobj_tsk.png", true); //$NON-NLS-1$

        // Quick fix icons
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_ELCL_QUICK_FIX_ENABLED,
                PATH_ELOCALTOOL + "smartmode_co.png", true); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_DLCL_QUICK_FIX_DISABLED,
                PATH_DLOCALTOOL + "smartmode_co.png", true); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_OBJS_FIXABLE_WARNING,
                PATH_OBJECT + "quickfix_warning_obj.png", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_OBJS_FIXABLE_ERROR,
                PATH_OBJECT + "quickfix_error_obj.png", true); //$NON-NLS-1$

        // task objects
        // declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_HPRIO_TSK,
        // PATH_OBJECT+"hprio_tsk.png");
        // declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_MPRIO_TSK,
        // PATH_OBJECT+"mprio_tsk.png");
        // declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_LPRIO_TSK,
        // PATH_OBJECT+"lprio_tsk.png");

        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJS_TASK_TSK,
                PATH_OBJECT + "taskmrk_tsk.png", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJS_BKMRK_TSK,
                PATH_OBJECT + "bkmrk_tsk.png", true); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_OBJS_COMPLETE_TSK, PATH_OBJECT
                        + "complete_tsk.png", //$NON-NLS-1$
                true);
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_OBJS_INCOMPLETE_TSK, PATH_OBJECT
                        + "incomplete_tsk.png", //$NON-NLS-1$
                true);
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_OBJS_WELCOME_ITEM, PATH_OBJECT
                        + "welcome_item.png", //$NON-NLS-1$
                true);
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_OBJS_WELCOME_BANNER, PATH_OBJECT
                        + "welcome_banner.png", //$NON-NLS-1$
                true);
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_OBJS_ERROR_PATH, PATH_OBJECT
                        + "error_tsk.png", //$NON-NLS-1$
                true);
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_OBJS_WARNING_PATH, PATH_OBJECT
                        + "warn_tsk.png", //$NON-NLS-1$
                true);
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_OBJS_INFO_PATH, PATH_OBJECT
                        + "info_tsk.png", //$NON-NLS-1$
                true);

        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_LCL_FLAT_LAYOUT, PATH_ELOCALTOOL
                        + "flatLayout.png", //$NON-NLS-1$
                true);
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_LCL_HIERARCHICAL_LAYOUT,
                PATH_ELOCALTOOL + "hierarchicalLayout.png", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEM_CATEGORY,
                PATH_ETOOL + "problem_category.png", true); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEMS_VIEW,
                PATH_EVIEW + "problems_view.png", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEMS_VIEW_ERROR,
                PATH_EVIEW + "problems_view_error.png", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEMS_VIEW_WARNING,
                PATH_EVIEW + "problems_view_warning.png", true); //$NON-NLS-1$
    }

    /**
     * Declares an IDE-specific workbench image.
     *
     * @param symbolicName
     *        the symbolic name of the image
     * @param path
     *        the path of the image file; this path is relative to the base
     *        of the IDE plug-in
     * @param shared
     *        <code>true</code> if this is a shared image, and
     *        <code>false</code> if this is not a shared image
     * @see IWorkbenchConfigurer#declareImage
     */
    private void declareWorkbenchImage(final Bundle ideBundle, final String symbolicName,
            final String path, final boolean shared) {
        final URL url = FileLocator.find(ideBundle, new Path(path), null);
        final ImageDescriptor desc = ImageDescriptor.createFromURL(url);
        getWorkbenchConfigurer().declareImage(symbolicName, desc, shared);
    }

    @Override
    public String getInitialWindowPerspectiveId() {
        return null;
    }

    @Override
    public String getMainPreferencePageId() {
        return "org.bonitasoft.studio.preferences.preferences.UIPreferencePage";
    }

    @Override
    public void preStartup() {
        new RecoverWorkspaceContribution().execute();

        // Initialize adapter factories and avoid deadlock at startup
        ProcessDiagramEditorPlugin.getInstance().getItemProvidersAdapterFactory();

        disableInternalWebBrowser();
        setSystemProperties();
        configureGradleScriptContentType();
     }

    private void configureGradleScriptContentType() {
        // Avoid error logs to be written by the groovy eclipse editor plugin
        try {
            IContentType contentType = Platform.getContentTypeManager()
                    .getContentType("org.eclipse.buildship.core.files.gradlebuildscript");
            if (contentType == null) {
                Platform.getContentTypeManager().addContentType("org.eclipse.buildship.core.files.gradlebuildscript",
                        "Gradle Build Script", null);
            }
        } catch (CoreException e) {
            BonitaStudioLog.error(e);
        }
    }

    protected void setSystemProperties() {
        var instanceLocation = Platform.getInstanceLocation();
        if (instanceLocation != null) {
            String workspaceLocation = new File(instanceLocation.getURL().getFile()).getPath();
            System.setProperty("bonita.tomcat.lib.dir", String.format("%s%stomcat%sserver%slib", workspaceLocation,
                    File.separator, File.separator, File.separator));
            BonitaStudioLog.info("bonita.tomcat.lib.dir=" + System.getProperty("bonita.tomcat.lib.dir"),
                    ApplicationPlugin.PLUGIN_ID);
        } else {
            BonitaStudioLog.warning("Property 'bonita.tomcat.lib.dir' has not been set.", ApplicationPlugin.PLUGIN_ID);
        }
        // Workaround for STUDIO-3651
        System.setProperty(AWT_DRAW_STRING_AS_IMAGE, System.getProperty(AWT_DRAW_STRING_AS_IMAGE, "true"));
    }

    @Override
    public void postStartup() {
        try {
            HealthCheckServerManager.getInstance().start(PortSelector.findFreePort());
        } catch (IOException e) {
            BonitaStudioLog.error(e);
        }
        var initializeProjectJob = new WorkspaceJob("Initialize project") {

            @Override
            public boolean belongsTo(Object family) {
                return RepositoryManager.class.equals(family);
            }

            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                RepositoryManager.getInstance().installRequiredMavenDependencies(monitor);
                RepositoryManager.getInstance().getAccessor().start(monitor);
                return Status.OK_STATUS;
            }
        };
        initializeProjectJob.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                if (initializeProjectJob.equals(event.getJob())) {
                    executePostStartupContributions();
                }
                super.done(event);
            }
        });
        initializeProjectJob.setPriority(Job.INTERACTIVE);
        if (RepositoryManager.getInstance().hasActiveRepository()) {
            initializeProjectJob.schedule();
        }

        super.postStartup();
        IThemeEngine engine = PlatformUI.getWorkbench().getService(IThemeEngine.class);
        synchronizeTheme(engine);
        applyTheme(engine);
    }

    /**
     * Synchronize active eclipse theme with the Bonita preference,
     * to ensure that specifics adjustments for Dark theme are applied.
     * The preference value can be out-dated if the user update the theme from the eclipse preference panel.
     */
    private void synchronizeTheme(IThemeEngine engine) {
        String currentValue = BonitaStudioPreferencesPlugin.getDefault().getPreferenceStore()
                .getString(BonitaThemeConstants.STUDIO_THEME_PREFERENCE);
        String activeTheme = engine.getActiveTheme() == null
                ? null
                : engine.getActiveTheme().getId();
        if (!themeIsValid(activeTheme)) {
            BonitaStudioPreferencesPlugin.getDefault().getPreferenceStore()
                    .setValue(BonitaThemeConstants.STUDIO_THEME_PREFERENCE, BonitaThemeConstants.LIGHT_THEME);
        } else if (!Objects.equals(currentValue, engine.getActiveTheme().getId())) {
            BonitaStudioPreferencesPlugin.getDefault().getPreferenceStore()
                    .setValue(BonitaThemeConstants.STUDIO_THEME_PREFERENCE, engine.getActiveTheme().getId());
        }
    }

    private boolean themeIsValid(String themeId) {
        if (themeId != null && !themeId.isBlank()) {
            return Objects.equals(themeId, BonitaThemeConstants.LIGHT_THEME)
                    || Objects.equals(themeId, BonitaThemeConstants.DARK_THEME);
        }
        return false;
    }

    /**
     * Apply the theme if required (usually on first start).
     */
    private void applyTheme(IThemeEngine engine) {
        String expectedTheme = BonitaStudioPreferencesPlugin.getDefault().getPreferenceStore()
                .getString(BonitaThemeConstants.STUDIO_THEME_PREFERENCE);
        if (engine.getActiveTheme() == null
                || !Objects.equals(expectedTheme, engine.getActiveTheme().getId())) {
            BonitaStudioLog.info(String.format("Applying theme %s", expectedTheme), ApplicationPlugin.PLUGIN_ID);
            engine.setTheme(expectedTheme, true);
        }
    }

    /**
     * Disconnect from the core workspace.
     */
    private void disconnectFromWorkspace(final IProgressMonitor monitor) {
        // save the workspace
        final MultiStatus status = new MultiStatus(
                IDEWorkbenchPlugin.IDE_WORKBENCH, 1,
                IDEWorkbenchMessages.ProblemSavingWorkbench, null);
        try {
            final ProgressMonitorJobsDialog p = new ProgressMonitorJobsDialog(
                    null);

            final boolean applyPolicy = ResourcesPlugin.getWorkspace()
                    .getDescription().isApplyFileStatePolicy();

            final IRunnableWithProgress runnable = new IRunnableWithProgress() {

                @Override
                public void run(final IProgressMonitor monitor) {
                    try {
                        if (applyPolicy) {
                            status.merge(((Workspace) ResourcesPlugin
                                    .getWorkspace()).save(true, true, monitor));
                        }
                    } catch (final CoreException e) {
                        status.merge(e.getStatus());
                    }
                }
            };
            p.run(true, false, runnable);
        } catch (final InvocationTargetException e) {
            status
                    .merge(new Status(IStatus.ERROR,
                            IDEWorkbenchPlugin.IDE_WORKBENCH, 1,
                            IDEWorkbenchMessages.InternalError, e
                                    .getTargetException()));
        } catch (final InterruptedException e) {
            status.merge(new Status(IStatus.ERROR,
                    IDEWorkbenchPlugin.IDE_WORKBENCH, 1,
                    IDEWorkbenchMessages.InternalError, e));
        }
        ErrorDialog.openError(null,
                IDEWorkbenchMessages.ProblemsSavingWorkspace, null, status,
                IStatus.ERROR | IStatus.WARNING);
        if (!status.isOK()) {
            IDEWorkbenchPlugin.log(
                    IDEWorkbenchMessages.ProblemsSavingWorkspace, status);
        }
    }

    protected void disableInternalWebBrowser() {
        final String noRegister = System.getProperty("bonita.noregister"); //$NON-NLS-1$
        if (noRegister == null || !noRegister.equals("1")) {
            WebBrowserUtil.isInternalBrowserOperational = false;
        }
    }

    @Override
    public void postShutdown() {
        super.postShutdown();
        disconnectFromWorkspace(AbstractRepository.NULL_PROGRESS_MONITOR);
    }

    private boolean promptOnExit(Shell parentShell) {
        final IPreferenceStore store = IDEWorkbenchPlugin.getDefault()
                .getPreferenceStore();
        final boolean promptOnExit = store
                .getBoolean(IDEInternalPreferences.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW);

        if (promptOnExit) {
            if (parentShell == null) {
                final IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                if (workbenchWindow != null) {
                    parentShell = workbenchWindow.getShell();
                }
            }
            if (parentShell != null) {
                parentShell.setMinimized(false);
                parentShell.forceActive();
            }
            final MessageDialogWithToggle dlg = ExitDialog.openExitDialog(parentShell);
            if (dlg.getReturnCode() != IDialogConstants.OK_ID) {
                return false;
            }
            if (dlg.getToggleState()) {
                store
                        .setValue(
                                IDEInternalPreferences.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW,
                                false);
                IDEWorkbenchPlugin.getDefault().savePluginPreferences();
            }
        }

        IWorkbenchPage activePage = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow()
                .getActivePage();
        IEditorReference[] editorReferences = activePage
                .getEditorReferences();
        return activePage.closeEditors(Stream.of(editorReferences)
                .filter(ref -> EDITOR_TYPE_TO_CLOSE_ON_EXIT.contains(ref.getId()))
                .toArray(IEditorReference[]::new), true);
    }

    @Override
    public boolean preShutdown() {
        var shell = Display.getCurrent() != null ? Display.getCurrent().getActiveShell()
                : Display.getDefault().getActiveShell();
        if (promptOnExit(shell)) {
            IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage();
            Stream.of(activePage.getViewReferences())
                    .filter(vr -> Objects.equals("org.eclipse.ui.browser.view", vr.getId()))
                    .forEach(activePage::hideView);
            Job.getJobManager().cancel(RepositoryManager.class);
            final boolean returnValue = super.preShutdown();
            if (returnValue) {
                try {
                    if (PlatformUI.isWorkbenchRunning() && PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null
                            && PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null) {
                        PlatformUI.getWorkbench().getProgressService().run(true, false, new PreShutdownStudio());
                        return true;
                    }
                } catch (final Exception e) {
                    BonitaStudioLog.error(e);
                }
            }
            return returnValue;
        }
        return false;
    }

    @Override
    public void earlyStartup() {
        if (PlatformUtil.isHeadless()) {
            return;//Do not execute earlyStartup in headless mode
        }
        // Force groovy plugin startup
        GroovyPlugin.getDefault();

        final long startupDuration = System.currentTimeMillis() - BonitaStudioApplication.START_TIME;
        BonitaStudioLog.info("Startup duration : " + DateUtil.getDisplayDuration(startupDuration),
                ApplicationPlugin.PLUGIN_ID);
        ApplicationPlugin.getDefault().getPreferenceStore().setDefault(FIRST_STARTUP, true);
        if (isFirstStartup()) {
            String skipReleaseNote = System.getProperty(BONITA_STUDIO_SKIP_RELEASE_NOTE_SYSTEM_PROPERTY);
            if (skipReleaseNote == null || !(Boolean.valueOf(skipReleaseNote) || skipReleaseNote.isEmpty())) {
                new OpenReleaseNoteHandler().openBrowser();
            }
        }

        if (RepositoryManager.getInstance().hasActiveRepository()) {
            PlatformUtil.openDashboardIfNoOtherEditorOpen();
        } else {
            PlatformUtil.openIntroIfNoOtherEditorOpen();
        }

        ApplicationPlugin.getDefault().getPreferenceStore().setValue(FIRST_STARTUP, false);
        // Use a dummy block handler to avoid blocked job dialog to open
        Dialog.setBlockedHandler(new IDialogBlockedHandler() {

            @Override
            public void clearBlocked() {
                // No default behavior
            }

            @Override
            public void showBlocked(IProgressMonitor blocking,
                    IStatus blockingStatus, String blockedName) {
                // No default behavior
            }

            @Override
            public void showBlocked(Shell parentShell, IProgressMonitor blocking,
                    IStatus blockingStatus, String blockedName) {
                // No default behavior
            }
        });
    }

    private void executePostStartupContributions() {
        final IConfigurationElement[] elements = BonitaStudioExtensionRegistryManager.getInstance()
                .getConfigurationElements(
                        "org.bonitasoft.studio.common.ui.poststartup"); //$NON-NLS-1$
        for (final IConfigurationElement elem : elements) {
            final Workbench workbench = (Workbench) PlatformUI.getWorkbench();
            try {
                IPostStartupContribution contrib = (IPostStartupContribution) ContextInjectionFactory
                        .make(Platform.getBundle(elem.getDeclaringExtension().getNamespaceIdentifier())
                                .loadClass(elem.getAttribute("class")), workbench.getContext());
                Display.getDefault().asyncExec(contrib::execute);
            } catch (InjectionException | ClassNotFoundException | InvalidRegistryObjectException e) {
                BonitaStudioLog.error(e);
            }
        }
    }

    private boolean isFirstStartup() {
        return ApplicationPlugin.getDefault().getPreferenceStore().getBoolean(FIRST_STARTUP);
    }

}
