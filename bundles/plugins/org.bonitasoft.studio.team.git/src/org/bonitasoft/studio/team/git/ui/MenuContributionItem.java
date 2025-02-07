/*******************************************************************************
 * Copyright (C) 2018 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.studio.team.git.ui;

import java.io.IOException;
import java.util.HashMap;
import java.util.function.BooleanSupplier;

import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.common.repository.core.BonitaProject;
import org.bonitasoft.studio.common.repository.model.IRepository;
import org.bonitasoft.studio.common.ui.PlatformUtil;
import org.bonitasoft.studio.pics.Pics;
import org.bonitasoft.studio.team.git.TeamGitPlugin;
import org.bonitasoft.studio.team.git.core.CloneGitProject;
import org.bonitasoft.studio.team.git.core.CustomEvaluationContext;
import org.bonitasoft.studio.team.git.core.CustomHandlerService;
import org.bonitasoft.studio.team.git.core.CustomPushBranchActionHandler;
import org.bonitasoft.studio.team.git.core.ShareGitProject;
import org.bonitasoft.studio.team.git.i18n.Messages;
import org.bonitasoft.studio.ui.dialog.ExceptionDialogHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.egit.core.GitProvider;
import org.eclipse.egit.ui.Activator;
import org.eclipse.egit.ui.UIPreferences;
import org.eclipse.egit.ui.internal.UIIcons;
import org.eclipse.egit.ui.internal.actions.CommitActionHandler;
import org.eclipse.egit.ui.internal.actions.MergeActionHandler;
import org.eclipse.egit.ui.internal.actions.PullFromUpstreamConfigAction;
import org.eclipse.egit.ui.internal.actions.RebaseActionHandler;
import org.eclipse.egit.ui.internal.actions.ResetActionHandler;
import org.eclipse.egit.ui.internal.actions.SimpleFetchActionHandler;
import org.eclipse.egit.ui.internal.actions.SimplePushActionHandler;
import org.eclipse.egit.ui.internal.actions.SwitchToMenu;
import org.eclipse.egit.ui.internal.selection.SelectionUtils;
import org.eclipse.egit.ui.internal.staging.StagingView;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class MenuContributionItem extends ContributionItem {

    private ResourceManager pluginResources = Activator.getDefault().getResourceManager();

    @Override
    public void fill(Menu menu, int index) {
        super.fill(menu, index);
        IEvaluationContext customEvaluationContext = createEvaluationContext();

        createShareGitProjectMenu(menu);
        createCloneGitProjectMenu(menu);
        createCommitMenu(menu, customEvaluationContext);
        createSimplePushMenu(menu, customEvaluationContext);
        createSimpleFetchMenu(menu, customEvaluationContext);
        var project = getProject();
        org.eclipse.jgit.lib.Repository repository = null;
        if (project != null) {
            repository = SelectionUtils.getRepository(new StructuredSelection(project));
        }
        if (repository != null) {
            createPushBranchMenu(menu, repository);
        }
        createPullFromUpstreamMenu(menu);

        if (repository != null) {
            createSwitchToMenu(menu, customEvaluationContext);
        }
        createMergeMenu(menu, customEvaluationContext);
        createResetMenu(menu, customEvaluationContext);
        createRebaseMenu(menu, customEvaluationContext);
        createShowGitStagingMenu(menu);
        createShowHistoryMenu(menu);
        createOpenPreferenceMenu(menu);
    }

    private void createOpenPreferenceMenu(Menu menu) {
        createMenu(menu, Messages.openGitPreferences, openGitPreferenceListener(), () -> true,
                TeamGitPlugin.getImage("icons/editconfig.png"));
    }

    private void createShowHistoryMenu(Menu menu) {
        createMenu(menu,
                Messages.showHistory,
                openView("org.eclipse.team.ui.GenericHistoryView"),
                () -> RepositoryManager.getInstance().getCurrentRepository()
                        .filter(repo -> repo.isShared(GitProvider.ID)).isPresent(),
                TeamGitPlugin.getImage("icons/history.png"));
        if (RepositoryManager.getInstance().getCurrentRepository()
                .filter(repo -> repo.isShared(GitProvider.ID)).isPresent()) {
            new MenuItem(menu, SWT.SEPARATOR);
        }
    }

    private void createShowGitStagingMenu(Menu menu) {
        createMenu(menu,
                Messages.showGitStaging,
                openView(StagingView.VIEW_ID),
                () -> RepositoryManager.getInstance().getCurrentRepository()
                        .filter(repo -> repo.isShared(GitProvider.ID)).isPresent(),
                TeamGitPlugin.getImage("icons/staging.png"));
    }

    private void createRebaseMenu(Menu menu, IEvaluationContext customEvaluationContext) {
        RebaseActionHandler rebaseActionHandler = new RebaseActionHandler();
        rebaseActionHandler.setEnabled(customEvaluationContext);
        createMenu(menu, Messages.rebase,
                e -> {
                    try {
                        rebaseActionHandler
                                .execute(new ExecutionEvent(null, new HashMap<>(), null, customEvaluationContext));
                    } catch (ExecutionException e1) {
                        BonitaStudioLog.error(e1);
                    }
                },
                rebaseActionHandler::isEnabled,
                UIIcons.getImage(pluginResources, UIIcons.REBASE));
        if (rebaseActionHandler.isEnabled()) {
            new MenuItem(menu, SWT.SEPARATOR);
        }
    }

    private void createResetMenu(Menu menu, IEvaluationContext customEvaluationContext) {
        ResetActionHandler resetActionHandler = new ResetActionHandler();
        resetActionHandler.setEnabled(customEvaluationContext);
        createMenu(menu, Messages.reset,
                e -> {
                    try {
                        resetActionHandler
                                .execute(new ExecutionEvent(null, new HashMap<>(), null, customEvaluationContext));
                    } catch (ExecutionException e1) {
                        BonitaStudioLog.error(e1);
                    }
                },
                () -> resetActionHandler.isEnabled(),
                UIIcons.getImage(pluginResources, UIIcons.RESET));
    }

    private void createMergeMenu(Menu menu, IEvaluationContext customEvaluationContext) {
        MergeActionHandler mergeActionHandler = new MergeActionHandler();
        mergeActionHandler.setEnabled(customEvaluationContext);
        createMenu(menu, Messages.merge,
                e -> {
                    try {
                        mergeActionHandler
                                .execute(new ExecutionEvent(null, new HashMap<>(), null, customEvaluationContext));
                    } catch (ExecutionException e1) {
                        BonitaStudioLog.error(e1);
                    }
                },
                mergeActionHandler::isEnabled,
                UIIcons.getImage(pluginResources, UIIcons.MERGE));

        if (mergeActionHandler.isEnabled()) {
            new MenuItem(menu, SWT.SEPARATOR);
        }
    }

    private void createSwitchToMenu(Menu menu, IEvaluationContext customEvaluationContext) {
        Menu switchToMenu = createSwitchToMenu(menu);
        SwitchToMenu switchToMenuContribution = new CustomSwitchToMenu(
                new CustomHandlerService(customEvaluationContext));
        switchToMenuContribution.initialize(PlatformUI.getWorkbench());
        switchToMenuContribution.fill(switchToMenu, 0);
        new MenuItem(menu, SWT.SEPARATOR);
    }

    private void createPullFromUpstreamMenu(Menu menu) {
        PullFromUpstreamConfigAction pullAction = new PullFromUpstreamConfigAction();
        var project = getProject();
        if (project != null) {
            pullAction.selectionChanged(null, new StructuredSelection(project));
        }
        createMenu(menu,
                Messages.pull,
                e -> pullAction.run(null),
                pullAction::isEnabled,
                UIIcons.getImage(pluginResources, UIIcons.PULL));
        if (pullAction.isEnabled()) {
            new MenuItem(menu, SWT.SEPARATOR);
        }
    }

    private void createPushBranchMenu(Menu menu, org.eclipse.jgit.lib.Repository repository) {
        CustomPushBranchActionHandler pushHandler = new CustomPushBranchActionHandler(getProject());
        String branch = "";
        try {
            branch = repository.getBranch();
        } catch (IOException e2createMenu) {
            BonitaStudioLog.error(e2createMenu);
        }
        createMenu(menu,
                String.format(Messages.push, branch),
                e -> {
                    try {
                        pushHandler.execute(null);
                    } catch (ExecutionException e1) {
                        BonitaStudioLog.error(e1);
                    }
                },
                pushHandler::isEnabled,
                UIIcons.getImage(pluginResources, UIIcons.PUSH));
    }

    private void createSimpleFetchMenu(Menu menu, IEvaluationContext customEvaluationContext) {
        SimpleFetchActionHandler fetchActionHandler = new SimpleFetchActionHandler();
        fetchActionHandler.setEnabled(customEvaluationContext);
        createMenu(menu,
                Messages.fetch,
                e -> {
                    try {
                        fetchActionHandler
                                .execute(new ExecutionEvent(null, new HashMap<>(), null, customEvaluationContext));
                    } catch (ExecutionException e1) {
                        BonitaStudioLog.error(e1);
                    }
                },
                fetchActionHandler::isEnabled,
                UIIcons.getImage(pluginResources, UIIcons.FETCH));
    }

    private void createSimplePushMenu(Menu menu, IEvaluationContext customEvaluationContext) {
        SimplePushActionHandler simplePushActionHandler = new SimplePushActionHandler();
        simplePushActionHandler.setEnabled(customEvaluationContext);
        createMenu(menu,
                Messages.pushToUpstream,
                e -> {
                    try {
                        simplePushActionHandler
                                .execute(new ExecutionEvent(null, new HashMap<>(), null, customEvaluationContext));
                    } catch (ExecutionException e1) {
                        BonitaStudioLog.error(e1);
                    }
                },
                () -> simplePushActionHandler.isEnabled(),
                UIIcons.getImage(pluginResources, UIIcons.PUSH));
    }

    private void createCommitMenu(Menu menu, IEvaluationContext customEvaluationContext) {
        CommitActionHandler commitActionHandler = new CommitActionHandler();
        commitActionHandler.setEnabled(customEvaluationContext);
        createMenu(menu,
                Messages.commit,
                e -> {
                    boolean useStagingView = Activator.getDefault().getPreferenceStore()
                            .getBoolean(UIPreferences.ALWAYS_USE_STAGING_VIEW);
                    try {
                        Activator.getDefault()
                                .getPreferenceStore().setValue(UIPreferences.ALWAYS_USE_STAGING_VIEW, false);
                        commitActionHandler
                                .execute(new ExecutionEvent(null, new HashMap<>(), null, customEvaluationContext));
                    } catch (ExecutionException e1) {
                        BonitaStudioLog.error(e1);
                    } finally {
                        Activator.getDefault().getPreferenceStore().setValue(UIPreferences.ALWAYS_USE_STAGING_VIEW,
                                useStagingView);
                    }
                },
                commitActionHandler::isEnabled,
                UIIcons.getImage(pluginResources, UIIcons.COMMIT));
    }

    private void createCloneGitProjectMenu(Menu menu) {
        CloneGitProject cloneGitProject = new CloneGitProject();
        createMenu(menu,
                Messages.clone,
                e -> cloneGitProject.execute(),
                () -> true,
                UIIcons.getImage(pluginResources, UIIcons.CLONEGIT));
        new MenuItem(menu, SWT.SEPARATOR);
    }

    private void createShareGitProjectMenu(Menu menu) {
        ShareGitProject shareGitProject = new ShareGitProject();
        createMenu(menu,
                Messages.shareWithGit,
                e -> shareGitProject.execute(getBonitaProject()),
                () -> shareGitProject.canExecute(getProject()),
                Pics.getImage("git.png", TeamGitPlugin.getDefault()));
    }

    private Menu createSwitchToMenu(Menu menu) {
        MenuItem switchToMenuItem = new MenuItem(menu, SWT.CASCADE);
        switchToMenuItem.setText(Messages.switchTo);
        switchToMenuItem.setImage(UIIcons.getImage(pluginResources, UIIcons.BRANCHES));
        Menu switchToMenu = new Menu(menu);
        switchToMenuItem.setMenu(switchToMenu);
        return switchToMenu;
    }

    protected Listener openView(String viewId) {
        return e -> {
            try {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewId);
                if (PlatformUtil.isIntroOpen()) {
                    PlatformUtil.closeIntro();
                }
                IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                        .getActiveEditor();
                if (activeEditor != null) {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(activeEditor);
                }
            } catch (PartInitException e1) {
                new ExceptionDialogHandler().openErrorDialog(Display.getDefault().getActiveShell(),
                        "Failed to open view",
                        e1);
            }
        };
    }

    protected IEvaluationContext createEvaluationContext() {
        IEvaluationContext context = new CustomEvaluationContext();
        IProject project = getProject();
        if (project != null) {
            context.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, new StructuredSelection(getProject()));
        }
        context.addVariable(ISources.ACTIVE_SHELL_NAME, Display.getDefault().getActiveShell());
        context.addVariable(ISources.ACTIVE_PART_NAME,
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart());
        return context;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.ContributionItem#isDynamic()
     */
    @Override
    public boolean isDynamic() {
        return true;
    }

    private void createMenu(Menu parent, String label, Listener selectionListener, BooleanSupplier enablement,
            Image icon) {
        if (enablement.getAsBoolean()) {
            MenuItem item = new MenuItem(parent, SWT.NONE);
            item.setText(label);
            item.addListener(SWT.Selection, selectionListener);
            item.setEnabled(enablement.getAsBoolean());
            if (icon != null) {
                item.setImage(icon);
            }
        }
    }

    private Listener openGitPreferenceListener() {
        return e -> {
            PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(Display.getDefault().getActiveShell(),
                    "org.eclipse.egit.ui.GitPreferences",
                    new String[] { "org.eclipse.egit.ui.GitPreferences",
                            "org.eclipse.egit.ui.internal.preferences.ProjectsPreferencePage",
                            "org.eclipse.egit.ui.internal.preferences.WindowCachePreferencePage",
                            "org.eclipse.egit.ui.internal.preferences.DateFormatPreferencePage",
                            "org.eclipse.egit.ui.internal.preferences.GitDecoratorPreferencePage",
                            "org.eclipse.egit.ui.internal.preferences.GlobalConfigurationPreferencePage",
                            "org.eclipse.egit.ui.internal.preferences.CommittingPreferencePage",
                            "org.eclipse.egit.ui.internal.preferences.StagingViewPreferencePage",
                            "org.eclipse.egit.ui.internal.preferences.DialogsPreferencePage",
                            "org.eclipse.egit.ui.internal.preferences.SynchronizePreferencePage",
                            "org.eclipse.egit.ui.internal.preferences.HistoryPreferencePage" },
                    null);
            dialog.open();
        };
    }

    private IProject getProject() {
        var bonitaProject = getBonitaProject();
        if (bonitaProject != null) {
            return bonitaProject.getAppProject();
        }
        return null;
    }

    private BonitaProject getBonitaProject() {
        return RepositoryManager.getInstance().getCurrentProject().orElse(null);
    }

}
