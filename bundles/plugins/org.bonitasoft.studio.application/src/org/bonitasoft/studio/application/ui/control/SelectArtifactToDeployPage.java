/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.studio.application.ui.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bonitasoft.studio.actors.model.organization.Organization;
import org.bonitasoft.studio.actors.model.organization.User;
import org.bonitasoft.studio.application.i18n.Messages;
import org.bonitasoft.studio.application.ui.control.model.Artifact;
import org.bonitasoft.studio.application.ui.control.model.OrganizationArtifact;
import org.bonitasoft.studio.application.ui.control.model.ProcessArtifact;
import org.bonitasoft.studio.application.ui.control.model.ProcessVersion;
import org.bonitasoft.studio.application.ui.control.model.RepositoryModel;
import org.bonitasoft.studio.application.ui.control.model.RepositoryStore;
import org.bonitasoft.studio.businessobject.core.repository.BusinessObjectModelFileStore;
import org.bonitasoft.studio.common.jface.databinding.validator.EmptyInputValidator;
import org.bonitasoft.studio.common.repository.core.ActiveOrganizationProvider;
import org.bonitasoft.studio.common.repository.ui.viewer.CheckboxRepositoryTreeViewer;
import org.bonitasoft.studio.common.repository.ui.viewer.FileStoreObservableFactory;
import org.bonitasoft.studio.common.repository.ui.viewer.FileStoreTreeStructureAdvisor;
import org.bonitasoft.studio.configuration.ConfigurationPlugin;
import org.bonitasoft.studio.configuration.extension.IEnvironmentProvider;
import org.bonitasoft.studio.configuration.preferences.ConfigurationPreferenceConstants;
import org.bonitasoft.studio.pics.Pics;
import org.bonitasoft.studio.pics.PicsConstants;
import org.bonitasoft.studio.ui.validator.MultiValidator;
import org.bonitasoft.studio.ui.validator.MultiValidator.Builder;
import org.bonitasoft.studio.ui.widget.ButtonWidget;
import org.bonitasoft.studio.ui.widget.ComboWidget;
import org.bonitasoft.studio.ui.widget.SearchWidget;
import org.bonitasoft.studio.ui.widget.TextWidget;
import org.bonitasoft.studio.ui.wizard.ControlSupplier;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class SelectArtifactToDeployPage implements ControlSupplier {

    private static final String DEPLOY_DEFAULT_SELECTION = "deployDefaultSelection";
    private static final String CLEAN_BDM_DEFAULT_SELECTION = "cleanBDMDefaultSelection";

    private CheckboxRepositoryTreeViewer fileStoreViewer;
    protected IObservableSet<Object> checkedElementsObservable; // Bounded to the viewer -> doesn't contain filtered elements
    protected Set<Object> allCheckedElements = new HashSet<>(); // Contains all checked elements, even those not in the viewer anymore (filtered) 
    private IObservableValue<String> searchObservableValue = new WritableValue<>();
    protected List<Object> toFilter = new ArrayList<>();
    private Button cleanDeployOption;
    private String defaultUsername;
    private String environment;
    private boolean cleanBDM;
    private IObservableValue<String> usernameObservable;
    private SimpleContentProposalProvider usernameProposalProvider;
    private TextWidget defaultUserTextWidget;
    private IEnvironmentProvider environmentProvider;
    private Set<Object> defaultSelectedElements;
    private RepositoryModel repositoryModel;

    public SelectArtifactToDeployPage(RepositoryModel repositoryModel, IEnvironmentProvider environmentProvider) {
        this.repositoryModel = repositoryModel;
        this.environmentProvider = environmentProvider;
    }

    @Override
    public void loadSettings(IDialogSettings settings) {
        IDialogSettings section = settings.getSection(repositoryModel.getName());
        if (section != null && section.getArray(DEPLOY_DEFAULT_SELECTION) != null) {
            defaultSelectedElements = fromStrings(section.getArray(DEPLOY_DEFAULT_SELECTION));
            cleanBDM = section.getBoolean(CLEAN_BDM_DEFAULT_SELECTION);
        }
        environment = ConfigurationPlugin.getDefault().getPreferenceStore()
                .getString(ConfigurationPreferenceConstants.DEFAULT_CONFIGURATION);
    }

    @Override
    public void saveSettings(IDialogSettings settings) {
        mergeSets();
        IDialogSettings section = settings.addNewSection(repositoryModel.getName());
        section.put(DEPLOY_DEFAULT_SELECTION, stringify(allCheckedElements));
        section.put(CLEAN_BDM_DEFAULT_SELECTION, cleanBDM);
        ConfigurationPlugin.getDefault().getPreferenceStore()
                .setValue(ConfigurationPreferenceConstants.DEFAULT_CONFIGURATION, environment);
    }

    private String[] stringify(Set<Object> selectedElements) {
        return selectedElements.stream()
                .map(elem -> {
                    if (elem instanceof ProcessArtifact && ((ProcessArtifact) elem).getVersions().size() == 1) {
                        return ((ProcessArtifact) elem).getLatestVersion().toString();
                    } else if (elem instanceof Artifact && !(elem instanceof ProcessArtifact)) {
                        return elem.toString();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toArray(String[]::new);
    }

    private Set<Object> fromStrings(String[] selectedElements) {
        Collection<String> selection = Arrays.asList(selectedElements);
        return repositoryModel.getArtifacts().stream()
                .filter(artifact -> selection.contains(artifact.toString()))
                .map(singleVersionProcess())
                .collect(Collectors.toSet());
    }

    private Function<Object, Object> singleVersionProcess() {
        return object -> {
            if (object instanceof ProcessVersion && ((ProcessVersion) object).getParent().getVersions().size() == 1) {
                return ((ProcessVersion) object).getParent();
            }
            return object;
        };
    }

    @Override
    public Control createControl(Composite parent, IWizardContainer wizardContainer, DataBindingContext ctx) {
        Composite mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setLayout(GridLayoutFactory.swtDefaults()
                .create());
        mainComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

        //        Label warningDependencyLabel = new Label(mainComposite, SWT.WRAP);
        //        warningDependencyLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        //        warningDependencyLabel.setText(Messages.warningMissingDependency);

        Composite viewerAndButtonsComposite = new Composite(mainComposite, SWT.NONE);
        viewerAndButtonsComposite.setLayout(
                GridLayoutFactory.fillDefaults().numColumns(2).spacing(LayoutConstants.getSpacing().x, 1).create());
        viewerAndButtonsComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

        new Label(viewerAndButtonsComposite, SWT.NONE); // column filler
        createSearchAndCollapseComposite(ctx, viewerAndButtonsComposite);
        createSelectButtonComposite(viewerAndButtonsComposite);
        createViewer(ctx, viewerAndButtonsComposite);
        createDeployOptions(ctx, viewerAndButtonsComposite);

        checkedElementsObservable = fileStoreViewer.checkedElementsObservable();
        defaultSelection();
        searchObservableValue.addValueChangeListener(e -> applySearch(e.diff.getNewValue()));
        checkedElementsObservable.addSetChangeListener(event -> {
            updateCleanDeployEnablement();
            updateUserProposals();
        });

        updateUserProposals();
        updateCleanDeployEnablement();

        return mainComposite;
    }

    private void updateCleanDeployEnablement() {
        cleanDeployOption.setEnabled(
                checkedElementsObservable.stream().anyMatch(BusinessObjectModelFileStore.class::isInstance));
    }

    protected void mergeSets() {
        checkedElementsObservable.stream()
                .filter(object -> object instanceof Artifact)
                .forEach(allCheckedElements::add);
        List<Object> toRemove = allCheckedElements.stream()
                .filter(fileStore -> !checkedElementsObservable.contains(fileStore)
                        && !toFilter.contains(fileStore))
                .collect(Collectors.toList());
        allCheckedElements.removeAll(toRemove);
    }

    private void createSelectButtonComposite(Composite parent) {
        Composite buttonComposite = new Composite(parent, SWT.NONE);
        buttonComposite.setLayout(GridLayoutFactory.fillDefaults().spacing(LayoutConstants.getSpacing().x, 1).create());
        buttonComposite.setLayoutData(GridDataFactory.fillDefaults().create());

        new ButtonWidget.Builder()
                .withLabel(Messages.selectAll)
                .fill()
                .onClick(e -> checkAllElements())
                .createIn(buttonComposite);

        new ButtonWidget.Builder()
                .withLabel(Messages.selectNone)
                .fill()
                .onClick(e -> checkedElementsObservable.clear())
                .createIn(buttonComposite);
    }

    private void createViewer(DataBindingContext ctx, Composite parent) {
        fileStoreViewer = new CheckboxRepositoryTreeViewer(parent);
        fileStoreViewer.getTree().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        fileStoreViewer.setContentProvider(new ObservableListTreeContentProvider(observableFactory(),
                treeStructureAdvisor()));
        fileStoreViewer.setLabelProvider(new DeployTreeLabelProvider());
        fileStoreViewer.bindTree(ctx, new WritableList(repositoryModel.getStores(), RepositoryStore.class));
        fileStoreViewer.setComparator(new ViewerComparator() {

            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                if (e1 instanceof ProcessVersion && e2 instanceof ProcessVersion) {
                    return ((ProcessVersion) e1).compareTo((ProcessVersion) e2);
                }
                if (e1 instanceof RepositoryStore && e2 instanceof RepositoryStore) {
                    return ((RepositoryStore) e1).compareTo((RepositoryStore) e2);
                }
                return super.compare(viewer, e1, e2);
            }
        });
        fileStoreViewer.addDoubleClickListener(
                e -> {
                    if (e.getSelection() instanceof TreeSelection) {
                        TreeSelection selection = (TreeSelection) e.getSelection();
                        fileStoreViewer.setExpandedState(selection.getFirstElement(),
                                !fileStoreViewer.getExpandedState(selection.getFirstElement()));
                    }
                });
        fileStoreViewer.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof RepositoryStore) {
                    return ((RepositoryStore) element).getArtifacts().stream()
                            .anyMatch(fileStore -> !toFilter.contains(fileStore));
                }
                return !toFilter.contains(element);
            }
        });
        fileStoreViewer.addCheckStateListener(event -> {
            if (isOrganizationRepositoryStore(event.getElement()) && event.getChecked()) {
                uniqueOrganizationSelection(getDefaultOrganization());
            }
            if (event.getElement() instanceof OrganizationArtifact && countSelectedOrganizations() > 1) {
                uniqueOrganizationSelection((OrganizationArtifact) event.getElement());
            }
        });
    }

    private boolean isOrganizationRepositoryStore(Object element) {
        if(element instanceof RepositoryStore) {
            return ((RepositoryStore) element).getArtifacts().get(0) instanceof OrganizationArtifact;
        }
        return false;
    }

    private FileStoreTreeStructureAdvisor treeStructureAdvisor() {
        return new FileStoreTreeStructureAdvisor() {

            @Override
            public Boolean hasChildren(Object element) {
                if (element instanceof ProcessArtifact) {
                    return ((ProcessArtifact) element).getVersions().size() > 1;
                }
                return element instanceof RepositoryStore || super.hasChildren(element);
            }

            @Override
            public Object getParent(Object element) {
                if (element instanceof Artifact) {
                    return ((Artifact) element).getParent();
                }
                return super.getParent(element);
            }

        };
    }

    private FileStoreObservableFactory observableFactory() {
        return new FileStoreObservableFactory() {

            @Override
            public IObservable createObservable(Object target) {
                if (target instanceof RepositoryStore) {
                    return new WritableList(((RepositoryStore) target).getArtifacts(), Artifact.class);
                }
                if (target instanceof ProcessArtifact) {
                    return new WritableList(((ProcessArtifact) target).getVersions(), ProcessVersion.class);
                }
                return super.createObservable(target);
            }
        };
    }

    private Artifact getDefaultOrganization() {
        return repositoryModel.getArtifacts().stream()
                .filter(OrganizationArtifact.class::isInstance)
                .filter(artifact -> Objects.equals(new ActiveOrganizationProvider().getActiveOrganizationFileName(),
                        artifact.getName()))
                .findFirst()
                .orElse(repositoryModel.getArtifacts().stream()
                        .filter(OrganizationArtifact.class::isInstance)
                        .findFirst()
                        .orElse(null));
    }

    private void uniqueOrganizationSelection(Artifact selectedOrganizationArtifact) {
        checkedElementsObservable.add(selectedOrganizationArtifact);
        checkedElementsObservable
                .removeIf(element -> (element instanceof OrganizationArtifact)
                        && !Objects.equals(element, selectedOrganizationArtifact));
        updateUserProposals();
    }

    private long countSelectedOrganizations() {
        return checkedElementsObservable.stream()
                .filter(OrganizationArtifact.class::isInstance)
                .count();
    }

    private void createSearchAndCollapseComposite(DataBindingContext ctx, Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
        composite.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

        createSearch(ctx, composite);
        createToolbarComposite(composite);
    }

    private void createToolbarComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 8, 0).create());
        composite.setLayoutData(
                GridDataFactory.fillDefaults().align(SWT.END, SWT.FILL).grab(true, false).create());

        ToolBar toolBar = new ToolBar(composite, SWT.HORIZONTAL | SWT.RIGHT | SWT.NO_FOCUS | SWT.FLAT);
        toolBar.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_TRANSPARENT));

        ToolItem expandAll = new ToolItem(toolBar, SWT.PUSH);
        expandAll.setImage(Pics.getImage(PicsConstants.expandAll));
        expandAll.setToolTipText(Messages.expandAll);
        expandAll.addListener(SWT.Selection, e -> fileStoreViewer.expandAll());

        ToolItem collapseAll = new ToolItem(toolBar, SWT.PUSH);
        collapseAll.setImage(Pics.getImage(PicsConstants.collapseAll));
        collapseAll.setToolTipText(Messages.collapseAll);
        collapseAll.addListener(SWT.Selection, e -> fileStoreViewer.collapseAll());
    }

    private void createSearch(DataBindingContext ctx, Composite parent) {
        Composite searchComposite = new Composite(parent, SWT.NONE);
        searchComposite.setLayout(GridLayoutFactory.fillDefaults().create());
        searchComposite.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BOTTOM).create());

        new SearchWidget.Builder()
                .useNativeRender()
                .labelAbove()
                .fill()
                .widthHint(400)
                .withPlaceholder(Messages.searchArtifact)
                .bindTo(searchObservableValue)
                .withDelay(500)
                .inContext(ctx)
                .createIn(searchComposite);
    }

    private void createDeployOptions(DataBindingContext ctx, Composite parent) {
        new Label(parent, SWT.NONE);

        Group deployOptionGroup = new Group(parent, SWT.NONE);
        deployOptionGroup
                .setLayout(GridLayoutFactory.swtDefaults().numColumns(1).extendedMargins(10, 0, 0, 0).create());
        deployOptionGroup.setLayoutData(GridDataFactory.fillDefaults().create());
        deployOptionGroup.setText(Messages.deployOptions);

        if (!environmentProvider.getEnvironment().isEmpty()) {
            new ComboWidget.Builder()
                    .withLabel(Messages.environment)
                    .labelAbove()
                    .widthHint(400)
                    .useNativeRender()
                    .readOnly()
                    .withItems(environmentProvider.getEnvironment().toArray(new String[] {}))
                    .bindTo(PojoProperties.value("environment").observe(this))
                    .inContext(ctx)
                    .createIn(deployOptionGroup);
        }

        createDefaultUserTextWidget(ctx, deployOptionGroup);

        cleanDeployOption = new Button(deployOptionGroup, SWT.CHECK);
        cleanDeployOption.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).create());
        cleanDeployOption.setText(Messages.cleanBDMDatabase);

        ctx.bindValue(WidgetProperties.selection().observe(cleanDeployOption),
                PojoProperties.value("cleanBDM").observe(this));

    }

    private Organization getSelectedOrganization() {
        return checkedElementsObservable != null ? checkedElementsObservable.stream()
                .filter(OrganizationArtifact.class::isInstance)
                .map(OrganizationArtifact.class::cast)
                .map(artifact -> artifact.getModel())
                .findFirst()
                .orElse(null) : null;
    }

    private void createDefaultUserTextWidget(DataBindingContext ctx, final Composite mainComposite) {
        usernameObservable = PojoProperties.value("defaultUsername").observe(this);
        usernameProposalProvider = new SimpleContentProposalProvider();
        usernameProposalProvider.setFiltering(true);
        defaultUserTextWidget = new TextWidget.Builder()
                .useNativeRender()
                .labelAbove()
                .widthHint(530)
                .withLabel(org.bonitasoft.studio.actors.i18n.Messages.defaultUser)
                .withTootltip(org.bonitasoft.studio.actors.i18n.Messages.defaultUserTooltip)
                .bindTo(usernameObservable)
                .withProposalProvider(usernameProposalProvider)
                .withValidator(defaultUserValidator().create())
                .inContext(ctx)
                .createIn(mainComposite);
    }

    private void updateUserProposals() {
        usernameObservable.setValue("");
        Organization selectedOrganization = getSelectedOrganization();
        if (selectedOrganization == null) {
            setWidgetEnabled(defaultUserTextWidget, false);
            return;
        }
        String[] proposals = usernames();
        boolean enableDefaultUser = proposals != null && proposals.length > 0;
        setWidgetEnabled(defaultUserTextWidget, enableDefaultUser);
        usernameProposalProvider.setProposals(proposals);
        if (usernameObservable.getValue() == null || usernameObservable.getValue().isEmpty()) {
            if (proposals != null && proposals.length > 0) {
                ActiveOrganizationProvider activeOrganizationProvider = new ActiveOrganizationProvider();
                if (Objects.equals(activeOrganizationProvider.getActiveOrganization(),
                        selectedOrganization.getName())) {
                    usernameObservable.setValue(activeOrganizationProvider.getDefaultUser());
                } else {
                    usernameObservable.setValue(proposals[0]);
                }
            }
        }
    }

    private void setWidgetEnabled(Control control, boolean enabled) {
        control.setEnabled(enabled);
        if (control instanceof CLabel) {
            control.setForeground(enabled ? Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND)
                    : Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
        }
        if (control instanceof Composite) {
            Stream.of(((Composite) control).getChildren()).forEach(c -> setWidgetEnabled(c, enabled));
        }
    }

    private IStatus userExistsInOrganization(Object user) {
        if (getSelectedOrganization() != null) {
            IStatus status = new EmptyInputValidator(org.bonitasoft.studio.actors.i18n.Messages.defaultUser)
                    .validate(user);
            if (!status.isOK()) {
                return status;
            }
            if (Stream.of(usernames()).noneMatch(user::equals)) {
                return ValidationStatus
                        .error(Messages.bind(org.bonitasoft.studio.actors.i18n.Messages.UserDoesntExistError, user));
            }
        }
        return ValidationStatus.ok();
    }

    private String[] usernames() {
        Organization selectedOrganization = getSelectedOrganization();
        return selectedOrganization == null
                ? new String[0]
                : selectedOrganization
                        .getUsers()
                        .getUser()
                        .stream()
                        .map(User::getUserName)
                        .toArray(String[]::new);
    }

    protected Builder defaultUserValidator() {
        return new MultiValidator.Builder()
                .havingValidators(this::userExistsInOrganization);
    }

    private void applySearch(String searchValue) {
        Display.getDefault().asyncExec(() -> {
            mergeSets();
            updateArtifactsToFilter(searchValue);
            fileStoreViewer.refresh();
            checkedElementsObservable.clear();
            allCheckedElements.stream()
                    .filter(fileStore -> !toFilter.contains(fileStore))
                    .forEach(checkedElementsObservable::add);
        });
    }

    protected void updateArtifactsToFilter(String searchValue) {
        toFilter.clear();
        repositoryModel.getArtifacts().stream()
                .filter(artifact -> !artifact.getDisplayName().toLowerCase().contains(searchValue.toLowerCase()))
                .filter(artifact -> !Objects.equals(searchValue, artifact.getParent().toString()))
                .forEach(toFilter::add);
    }

    private void defaultSelection() {
        if (defaultSelectedElements != null && !defaultSelectedElements.isEmpty()) {
            defaultSelectedElements.stream()
                    .forEach(checkedElementsObservable::add);
        } else {
            checkAllElements();
        }
    }

    private void checkAllElements() {
        repositoryModel.getArtifacts().stream()
                .filter(artifact -> !OrganizationArtifact.class.isInstance(artifact))
                .filter(artifact -> ProcessVersion.class.isInstance(artifact) ? ((ProcessVersion) artifact).getParent().getVersions().size() > 1 : true )
                .forEach(checkedElementsObservable::add);

        //Only one organization can be selected at a time
        uniqueOrganizationSelection(getDefaultOrganization());
    }

    public Collection<Artifact> getSelectedArtifacts() {
        return checkedElementsObservable.stream()
                .filter(Artifact.class::isInstance)
                .map(Artifact.class::cast)
                .map(artifact -> artifact instanceof ProcessArtifact && ((ProcessArtifact) artifact).getVersions().size() == 1
                        ? ((ProcessArtifact) artifact).getLatestVersion() : artifact)
                .collect(Collectors.toList());
    }

    public String getDefaultUsername() {
        return defaultUsername;
    }

    public void setDefaultUsername(String defaultUsername) {
        this.defaultUsername = defaultUsername;
    }

    public boolean isCleanBDM() {
        return cleanBDM;
    }

    public void setCleanBDM(boolean cleanBDM) {
        this.cleanBDM = cleanBDM;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

}
