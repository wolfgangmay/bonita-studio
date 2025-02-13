/**
 * Copyright (C) 2012-2014 Bonitasoft S.A.
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
package org.bonitasoft.studio.expression.editor.viewer;

import static org.bonitasoft.studio.common.ui.jface.databinding.UpdateStrategyFactory.updateValueStrategy;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.bonitasoft.bpm.model.expression.Expression;
import org.bonitasoft.bpm.model.expression.ExpressionFactory;
import org.bonitasoft.bpm.model.expression.ExpressionPackage;
import org.bonitasoft.bpm.model.expression.provider.ExpressionItemProvider;
import org.bonitasoft.bpm.model.expression.provider.ExpressionItemProviderAdapterFactory;
import org.bonitasoft.bpm.model.process.Element;
import org.bonitasoft.bpm.model.process.SearchIndex;
import org.bonitasoft.bpm.model.util.ExpressionConstants;
import org.bonitasoft.studio.common.IBonitaVariableContext;
import org.bonitasoft.studio.common.databinding.validator.EmptyInputValidator;
import org.bonitasoft.studio.common.emf.tools.ExpressionHelper;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.RepositoryAccessor;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.common.ui.jface.SWTBotConstants;
import org.bonitasoft.studio.common.ui.jface.databinding.CustomEMFEditObservables;
import org.bonitasoft.studio.expression.editor.ExpressionProviderService;
import org.bonitasoft.studio.expression.editor.autocompletion.AutoCompletionField;
import org.bonitasoft.studio.expression.editor.autocompletion.BonitaContentProposalAdapter;
import org.bonitasoft.studio.expression.editor.autocompletion.ExpressionProposal;
import org.bonitasoft.studio.expression.editor.autocompletion.IBonitaContentProposalListener2;
import org.bonitasoft.studio.expression.editor.autocompletion.IExpressionProposalLabelProvider;
import org.bonitasoft.studio.expression.editor.filter.ExpressionReturnTypeFilter;
import org.bonitasoft.studio.expression.editor.i18n.Messages;
import org.bonitasoft.studio.expression.editor.provider.ExpressionComparator;
import org.bonitasoft.studio.expression.editor.provider.ExpressionContentProvider;
import org.bonitasoft.studio.expression.editor.provider.ExpressionLabelProvider;
import org.bonitasoft.studio.expression.editor.provider.ExpressionTypeLabelProvider;
import org.bonitasoft.studio.expression.editor.provider.IExpressionNatureProvider;
import org.bonitasoft.studio.expression.editor.provider.IExpressionProvider;
import org.bonitasoft.studio.expression.editor.provider.IExpressionValidator;
import org.bonitasoft.studio.expression.editor.widget.ContentAssistText;
import org.bonitasoft.studio.refactoring.core.AbstractRefactorOperation;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.observable.UnmodifiableObservableValue;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

/**
 * @author Romain Bioteau
 */
public class ExpressionViewer extends ContentViewer implements SWTBotConstants,
        IContentProposalListener, IBonitaContentProposalListener2, IBonitaVariableContext,
        IExpressionValidationListener,
        IValueChangeListener {

    protected Composite control;
    private Control textControl;
    protected ToolItem editControl;
    private AutoCompletionField autoCompletion;
    protected EMFDataBindingContext internalDataBindingContext = new EMFDataBindingContext();
    protected final Set<ViewerFilter> filters;
    private String example;
    private ControlDecoration messageDecoration;
    protected String mandatoryFieldName;
    protected EObject context;
    private final List<ISelectionChangedListener> expressionEditorListener = new ArrayList<>();
    private boolean enableEditExpression = true;
    private final List<IExpressionValidationListener> validationListeners = new ArrayList<>();
    private boolean isPageFlowContext = false;
    private boolean isOverviewContext = false;
    private AbstractRefactorOperation operation;
    private AbstractRefactorOperation removeOperation;
    final ExpressionViewerValidator expressionViewerValidator = new ExpressionViewerValidator();
    private Optional<IValidator> mandatoryFieldValidator = Optional.empty();
    protected final DisposeListener disposeListener = e -> handleDispose(e);

    protected IExpressionNatureProvider expressionNatureProvider = ExpressionContentProvider.getInstance();
    protected DataBindingContext externalDataBindingContext;
    protected Binding expressionBinding;
    protected boolean isPassword;
    private DefaultToolTip textTooltip;
    private IExpressionProposalLabelProvider expressionProposalLableProvider;
    private ContentAssistText contentAssistText;
    private ISelection selection;
    private final ExpressionProviderService expressionEditorService;
    private final Set<String> filteredEditor = new HashSet<>();
    protected final ExpressionItemProvider expressionItemProvider = new ExpressionItemProvider(
            new ExpressionItemProviderAdapterFactory());
    private String defaultReturnType;
    protected ExpressionNameResolver expressionNameResolver = new DefaultExpressionNameResolver();
    private IStatus defaultStatus = ValidationStatus.info("");
    private IStatus status = defaultStatus;

    public ExpressionViewer(final Composite composite, final int style,
            final TabbedPropertySheetWidgetFactory widgetFactory) {
        this(composite, style, widgetFactory, null);
    }

    public ExpressionViewer(final Composite composite, final int style) {
        this(composite, style, null, null);
    }

    /**
     * @deprecated use ExpressionViewer(final Composite composite, final int style) instead
     * @param composite
     * @param style
     * @param expressionReference
     */
    @Deprecated
    public ExpressionViewer(final Composite composite, final int style, final EReference expressionReference) {
        this(composite, style, null, null, expressionReference);
    }

    /**
     * @deprecated use ExpressionViewer(final Composite composite, final int style,, final TabbedPropertySheetWidgetFactory
     *             widgetFactory) instead
     * @param composite
     * @param style
     * @param expressionReference
     */
    @Deprecated
    public ExpressionViewer(final Composite composite, final int style,
            final TabbedPropertySheetWidgetFactory widgetFactory,
            final EReference expressionReference) {
        this(composite, style, widgetFactory, null, expressionReference);
    }

    public ExpressionViewer(final Composite composite, final int style,
            final TabbedPropertySheetWidgetFactory widgetFactory,
            final EditingDomain editingDomain, final EReference expressionReference) {
        this(composite, style, widgetFactory, editingDomain, expressionReference, true);
    }

    public ExpressionViewer(final Composite composite, final int style,
            final TabbedPropertySheetWidgetFactory widgetFactory,
            final EditingDomain editingDomain, final EReference expressionReference,
            final boolean enableEditExpression) {
        Assert.isNotNull(composite, "composite");
        this.enableEditExpression = enableEditExpression;
        expressionEditorService = ExpressionProviderService.getInstance();
        filters = new HashSet<>();
        createControl(composite, style, widgetFactory);
        setContentProvider(new ArrayContentProvider());
        setLabelProvider(new ExpressionLabelProvider());
    }

    protected void createControl(final Composite composite, final int style,
            final TabbedPropertySheetWidgetFactory widgetFactory) {
        control = widgetFactory != null
                ? widgetFactory.createComposite(composite)
                : new Composite(composite, SWT.INHERIT_DEFAULT);
        WidgetProperties.enabled().observe(control).addValueChangeListener(e -> updateEnablement(e.diff.getNewValue()));

        control.addDisposeListener(disposeListener);
        control.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).spacing(0, 0).create());
        createTextControl(style, widgetFactory);
    }

    protected void updateEnablement(final boolean enabled) {
        contentAssistText.setEnabled(enabled);
    }

    protected boolean shouldAddEditToolItem() {
        return enableEditExpression;
    }

    protected void editControlSelected(EditingDomain editingDomain) {
        final EditExpressionDialog dialog = createEditDialog();
        openEditDialog(dialog);
    }

    protected void erase(final Expression selectedExpression) {
        final String type = selectedExpression.getType();
        if (ExpressionConstants.SCRIPT_TYPE.equals(type)) {
            if (!MessageDialog.openConfirm(Display.getDefault().getActiveShell(), Messages.cleanExpressionTitle,
                    Messages.cleanExpressionMsg)) {
                return;
            }
        }
        clearExpression(selectedExpression);
        if (expressionBinding != null) {
            expressionBinding.validateTargetToModel();
        }
        validate();
        refresh();
    }

    protected void validateExternalDatabindingContextTargets(final DataBindingContext dbc) {
        if (dbc != null) {
            final IObservableList validationStatusProviders = dbc.getValidationStatusProviders();
            final Iterator iterator = validationStatusProviders.iterator();
            while (iterator.hasNext()) {
                final ValidationStatusProvider validationStatusProvider = (ValidationStatusProvider) iterator.next();
                final IObservableValue validationStatus = validationStatusProvider.getValidationStatus();
                if (!(validationStatus instanceof UnmodifiableObservableValue)) {
                    final IStatus status = (IStatus) validationStatus.getValue();
                    if (status != null) {
                        if (status.getSeverity() == IStatus.OK) {
                            validationStatus.setValue(ValidationStatus.ok());
                        } else if (status.getSeverity() == IStatus.WARNING) {
                            validationStatus.setValue(ValidationStatus.warning(status.getMessage()));
                        } else if (status.getSeverity() == IStatus.INFO) {
                            validationStatus.setValue(ValidationStatus.info(status.getMessage()));
                        } else if (status.getSeverity() == IStatus.ERROR) {
                            validationStatus.setValue(ValidationStatus.error(status.getMessage()));
                        } else if (status.getSeverity() == IStatus.CANCEL) {
                            validationStatus.setValue(ValidationStatus.cancel(status.getMessage()));
                        }
                    }
                }
            }
        }
    }

    private void clearExpression(final Expression selectedExpression) {
        final EditingDomain editingDomain = getEditingDomain();
        if (editingDomain != null) {
            final CompoundCommand cc = clearExpression(selectedExpression, editingDomain);
            cc.append(SetCommand.create(editingDomain, selectedExpression,
                    ExpressionPackage.Literals.EXPRESSION__TYPE, defaultExpressionType()));
            final boolean hasBeenExecuted = executeRemoveOperation(cc);
            if (!hasBeenExecuted) {
                editingDomain.getCommandStack().execute(cc);
            }
        } else {
            ExpressionHelper.clearExpression(selectedExpression);
            selectedExpression.setType(defaultExpressionType());
        }
    }

    private static CompoundCommand clearExpression(final Expression expr, final EditingDomain editingDomain) {
        if (editingDomain != null) {
            String returnType = expr.getReturnType();
            if (!expr.isReturnTypeFixed() || expr.getReturnType() == null) {
                returnType = String.class.getName();
            }
            final CompoundCommand cc = new CompoundCommand("Clear Expression");
            if (!ExpressionConstants.CONDITION_TYPE.equals(expr.getType())) {
                cc.append(SetCommand.create(editingDomain, expr, ExpressionPackage.Literals.EXPRESSION__TYPE,
                        ExpressionConstants.CONSTANT_TYPE));
            }
            cc.append(SetCommand.create(editingDomain, expr, ExpressionPackage.Literals.EXPRESSION__NAME, ""));
            cc.append(SetCommand.create(editingDomain, expr, ExpressionPackage.Literals.EXPRESSION__CONTENT, ""));
            cc.append(
                    SetCommand.create(editingDomain, expr, ExpressionPackage.Literals.EXPRESSION__RETURN_TYPE,
                            returnType));
            cc.append(RemoveCommand.create(editingDomain, expr,
                    ExpressionPackage.Literals.EXPRESSION__REFERENCED_ELEMENTS,
                    expr.getReferencedElements()));
            cc.append(SetCommand.create(editingDomain, expr, ExpressionPackage.Literals.EXPRESSION__INTERPRETER, null));
            cc.append(RemoveCommand.create(editingDomain, expr, ExpressionPackage.Literals.EXPRESSION__CONNECTORS,
                    expr.getConnectors()));
            return cc;
        } else {
            ExpressionHelper.clearExpression(expr);
            return null;
        }
    }

    protected String defaultExpressionType() {
        return ExpressionConstants.CONSTANT_TYPE;
    }

    public void setDefaultReturnType(final String defaultReturnType) {
        this.defaultReturnType = defaultReturnType;
    }

    public String getDefaultReturnType() {
        return defaultReturnType;
    }

    public void setExpressionProposalLableProvider(
            final IExpressionProposalLabelProvider expressionProposalLableProvider) {
        this.expressionProposalLableProvider = expressionProposalLableProvider;
        if (autoCompletion != null) {
            autoCompletion.setExpressionProposalLabelProvider(expressionProposalLableProvider);
        }
    }

    public ContentAssistText getContentAssistText() {
        return contentAssistText;
    }

    protected void createTextControl(final int style, final TabbedPropertySheetWidgetFactory widgetFactory) {
        if (expressionProposalLableProvider == null) {
            expressionProposalLableProvider = new ExpressionLabelProvider();
        }
        contentAssistText = new ContentAssistText(control, expressionProposalLableProvider, shouldAddEditToolItem(),
                style);
        contentAssistText.addEraseSelectionListener(e -> erase(getSelectedExpression()));

        contentAssistText.addEditListener(event -> editControlSelected(getEditingDomain()));
        textControl = contentAssistText.getTextControl();
        if (widgetFactory != null) {
            widgetFactory.adapt(textControl, false, false);
        }
        textControl.addDisposeListener(disposeListener);
        textTooltip = new DefaultToolTip(textControl) {

            @Override
            protected boolean shouldCreateToolTip(final Event event) {
                return super.shouldCreateToolTip(event) && getText(event) != null;
            }
        };
        textTooltip.setShift(new Point(5, 5));
        textTooltip.setRespectMonitorBounds(true);
        textTooltip.setPopupDelay(100);

        messageDecoration = new ControlDecoration(contentAssistText, SWT.LEFT, control);
        messageDecoration.setShowHover(true);
        messageDecoration.setMarginWidth(1);
        messageDecoration.hide();

        contentAssistText.addContentAssistListener(this);
        autoCompletion = contentAssistText.getAutocompletion();
        autoCompletion.addExpressionProposalListener(this);

        int indent = 0;
        if ((style & SWT.PASSWORD) != 0) {
            isPassword = true;
        }
        if ((style & SWT.BORDER) != 0) {
            indent = 16;
        }
        contentAssistText.setLayoutData(GridDataFactory.fillDefaults()
                .indent(indent, 0)
                .grab(true, true)
                .create());
    }

    protected void openEditDialog(final EditExpressionDialog dialog) {
        if (dialog.open() == Window.OK) {
            final Expression newExpression = dialog.getExpression();
            executeOperation(newExpression.getName());
            final Expression selectedExpression = getSelectedExpression();
            refresh();
            fireExpressionEditorChanged(new SelectionChangedEvent(this, new StructuredSelection(selectedExpression)));
        }
    }

    protected EditingDomain getEditingDomain() {
        final Expression selectedExpression = getSelectedExpression();
        if (selectedExpression != null && selectedExpression.eResource() != null) {
            return TransactionUtil.getEditingDomain(selectedExpression);
        }
        if (context != null && context.eResource() != null) {
            return TransactionUtil.getEditingDomain(context);
        }
        return null;
    }

    protected EditExpressionDialog createEditDialog() {
        final Object input = getInput();
        final EObject editInput = getEditInput(input);
        return createEditDialog(editInput);
    }

    private EObject getEditInput(final Object input) {
        EObject editInput = context;
        if (input != null && editInput == null) {
            if (input instanceof EObject) {
                editInput = (EObject) input;
            } else {
                editInput = (EObject) ((IObservableValue) input).getValue();
            }
        }
        return editInput;
    }

    protected EditExpressionDialog createEditDialog(final EObject editInput) {
        final EditExpressionDialog dialog = new EditExpressionDialog(control.getShell(), isPassword,
                getSelectedExpression(), editInput,
                getEditingDomain(), filters.toArray(new ViewerFilter[filters.size()]), this, expressionNameResolver);
        dialog.setEditorFilters(filteredEditor);
        return dialog;
    }

    public void setExpressionNameResolver(ExpressionNameResolver expressionNameResolver) {
        this.expressionNameResolver = expressionNameResolver;
    }

    protected void fireExpressionEditorChanged(final SelectionChangedEvent selectionChangedEvent) {
        for (final ISelectionChangedListener listener : expressionEditorListener) {
            listener.selectionChanged(selectionChangedEvent);
        }
    }

    @Override
    public Control getControl() {
        return control;
    }

    @Override
    public ISelection getSelection() {
        return selection != null ? selection : new StructuredSelection();
    }

    public BonitaContentProposalAdapter getContentProposal() {
        return autoCompletion.getContentProposalAdapter();
    }

    @Override
    public void refresh() {
        if (!getSelection().isEmpty()) {
            internalRefresh();
        }
    }

    @Override
    public void setSelection(final ISelection selection, final boolean reveal) {
        Assert.isLegal(selection instanceof IStructuredSelection);
        if (!selection.equals(this.selection)) {
            this.selection = selection;
            final Expression selectedExpression = getSelectedExpression();
            if (selectedExpression != null) {
                bindExpression();
                fireSelectionChanged(new SelectionChangedEvent(this, selection));
                updateAutoCompletionContentProposalAdapter();
                validate();
                refresh();
            }
        }
    }

    private void updateAutoCompletionContentProposalAdapter() {
        autoCompletion.getContentProposalAdapter().setEnabled(true);
        autoCompletion.getContentProposalAdapter().setProposalAcceptanceStyle(
                ContentProposalAdapter.PROPOSAL_REPLACE);
    }

    protected void updateSelection(final CompoundCommand cc, final Expression expression) {
        new ExpressionSynchronizer(getEditingDomain(), expression, getSelectedExpression()).synchronize(cc);
        refresh();
    }

    @Override
    protected void inputChanged(Object input, final Object oldInput) {
        if (input instanceof IObservableValue) {
            input = ((IObservableValue) input).getValue();
        }
        if (input != null) {
            manageNatureProviderAndAutocompletionProposal(input);
        }

    }

    public void manageNatureProviderAndAutocompletionProposal(final Object input) {
        if (input instanceof EObject && context == null) {
            setContext((EObject) input);
        }
        final Expression selectedExpression = getSelectedExpression();
        if (selectedExpression != null && ExpressionConstants.CONDITION_TYPE.equals(selectedExpression.getType())) {
            setProposalsFiltering(false);
            autoCompletion.getContentProposalAdapter()
                    .setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_INSERT);
        } else {
            autoCompletion.getContentProposalAdapter()
                    .setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
        }
        autoCompletion.setContext(context);
        final Set<Expression> filteredExpressions = getFilteredExpressions();
        autoCompletion.setProposals(filteredExpressions.toArray(new Expression[filteredExpressions.size()]));

        final ArrayList<String> filteredExpressionType = getFilteredExpressionType();
        autoCompletion.setFilteredExpressionType(filteredExpressionType);
        if (filteredExpressionType.contains(ExpressionConstants.VARIABLE_TYPE)
                && filteredExpressionType.contains(ExpressionConstants.PARAMETER_TYPE)
                && filteredExpressionType.contains(ExpressionConstants.FORM_REFERENCE_TYPE)
                && filteredExpressions.isEmpty()) {
            contentAssistText.setProposalEnabled(false);
        } else {
            contentAssistText.setProposalEnabled(true);
        }
    }

    protected Expression getSelectedExpression() {
        return (Expression) ((IStructuredSelection) getSelection()).getFirstElement();
    }

    private ArrayList<String> getFilteredExpressionType() {
        final ArrayList<String> expressionType = new ArrayList<>();
        final Set<ViewerFilter> fitlers = getFilters();

        final Expression exp = ExpressionFactory.eINSTANCE.createExpression();
        exp.setName("");
        if (filters != null && expressionNatureProvider != null && context != null && expressionEditorService != null) {
            final Set<IExpressionProvider> expressionProviders = expressionEditorService.getExpressionProviders();
            for (final IExpressionProvider provider : expressionProviders) {
                for (final ViewerFilter viewerFilter : fitlers) {
                    exp.setType(provider.getExpressionType());
                    if (!viewerFilter.select(this, context, exp)) {
                        expressionType.add(provider.getExpressionType());
                    }
                }
            }
        }
        return expressionType;
    }

    public void setExpressionNatureProvider(final IExpressionNatureProvider expressionNatureProvider) {
        this.expressionNatureProvider = expressionNatureProvider;
    }

    protected Set<Expression> getFilteredExpressions() {
        final Set<Expression> filteredExpressions = new TreeSet<>(new ExpressionComparator());
        if (expressionNatureProvider != null) {
            if (!(expressionNatureProvider instanceof ExpressionContentProvider)) {
                final ExpressionContentProvider provider = ExpressionContentProvider.getInstance();
                final Expression[] expressions = provider.getExpressions(context);
                if (expressions != null) {
                    filteredExpressions.addAll(Arrays.asList(expressions));
                }
            }
            final Expression[] expressions = expressionNatureProvider.getExpressions(context);
            if (expressions != null) {
                filteredExpressions.addAll(Arrays.asList(expressions));
            }
            final Set<Expression> toRemove = new HashSet<>();
            if (context != null) {
                for (final Expression exp : filteredExpressions) {
                    for (final ViewerFilter filter : getFilters()) {
                        if (filter != null && !filter.select(this, context, exp)) {
                            toRemove.add(exp);
                        }
                    }
                    final Expression selectedExpression = getSelectedExpression();
                    if (selectedExpression != null
                            && !ExpressionConstants.CONDITION_TYPE.equals(selectedExpression.getType())) {
                        if (selectedExpression != null && selectedExpression.isReturnTypeFixed()
                                && selectedExpression.getReturnType() != null) {
                            if (!compatibleReturnTypes(selectedExpression, exp)) {
                                toRemove.add(exp);
                            }
                        }
                    }
                }
            }
            filteredExpressions.removeAll(toRemove);
        }
        return filteredExpressions;
    }

    protected boolean compatibleReturnTypes(final Expression currentExpression, final Expression targetExpression) {
        final String currentReturnType = currentExpression.getReturnType();
        final String targetReturnType = targetExpression.getReturnType();
        final RepositoryAccessor repositoryAccessor = RepositoryManager.getInstance().getAccessor();
        return new ExpressionReturnTypeFilter(repositoryAccessor).compatibleReturnTypes(
                currentReturnType, targetReturnType);
    }

    protected Set<ViewerFilter> getFilters() {
        return filters;
    }

    protected void bindExpression() {
        if (expressionBinding != null && externalDataBindingContext != null) {
            externalDataBindingContext.removeBinding(expressionBinding);
            expressionBinding.dispose();
        }

        final IObservableValue nameObservable = getExpressionNameObservable();
        final IObservableValue typeObservable = getExpressionTypeObservable();
        final IObservableValue returnTypeObservable = getExpressionReturnTypeObservable();
        final IObservableValue contentObservable = getExpressionContentObservable();

        nameObservable.addValueChangeListener(this);
        typeObservable.addValueChangeListener(this);
        contentObservable.addValueChangeListener(this);
        returnTypeObservable.addValueChangeListener(this);

        final UpdateValueStrategy targetToModelNameStrategy = new UpdateValueStrategy();
        if (mandatoryFieldName != null) {
            targetToModelNameStrategy
                    .setBeforeSetValidator(mandatoryFieldValidator.orElse(new EmptyInputValidator(mandatoryFieldName)));
        }
        targetToModelNameStrategy.setConverter(getNameConverter());

        if (textControl instanceof Text) {
            var textDelayedObservableValue = WidgetProperties.text(SWT.Modify).observeDelayed(200, textControl);
            expressionBinding = internalDataBindingContext.bindValue(textDelayedObservableValue, nameObservable,
                    targetToModelNameStrategy, updateValueStrategy().create());
        } else {
            expressionBinding = internalDataBindingContext.bindValue(WidgetProperties.text().observe(textControl),
                    nameObservable,
                    targetToModelNameStrategy, updateValueStrategy().create());
        }

        bindEditableText(typeObservable);
        internalDataBindingContext.bindValue(ViewerProperties.singleSelection().observe(contentAssistText),
                getSelectedExpressionObservable());
        if (externalDataBindingContext != null) {
            externalDataBindingContext.addBinding(expressionBinding);
            externalDataBindingContext.addValidationStatusProvider(expressionViewerValidator);
        }
    }

    protected IObservableValue getExpressionNameObservable() {
        IObservableValue nameObservable;
        final EditingDomain editingDomain = getEditingDomain();
        if (editingDomain != null) {
            nameObservable = CustomEMFEditObservables.observeDetailValue(Realm.getDefault(),
                    getSelectedExpressionObservable(),
                    ExpressionPackage.Literals.EXPRESSION__NAME);
        } else {
            nameObservable = EMFObservables.observeDetailValue(Realm.getDefault(), getSelectedExpressionObservable(),
                    ExpressionPackage.Literals.EXPRESSION__NAME);
        }
        return nameObservable;
    }

    protected IObservableValue getExpressionTypeObservable() {
        IObservableValue nameObservable;
        final EditingDomain editingDomain = getEditingDomain();
        if (editingDomain != null) {
            nameObservable = CustomEMFEditObservables.observeDetailValue(Realm.getDefault(),
                    getSelectedExpressionObservable(),
                    ExpressionPackage.Literals.EXPRESSION__TYPE);
        } else {
            nameObservable = EMFObservables.observeDetailValue(Realm.getDefault(), getSelectedExpressionObservable(),
                    ExpressionPackage.Literals.EXPRESSION__TYPE);
        }
        return nameObservable;
    }

    protected IObservableValue getExpressionReturnTypeObservable() {
        IObservableValue returnTypeObservable;
        final EditingDomain editingDomain = getEditingDomain();
        if (editingDomain != null) {
            returnTypeObservable = CustomEMFEditObservables.observeDetailValue(Realm.getDefault(),
                    getSelectedExpressionObservable(),
                    ExpressionPackage.Literals.EXPRESSION__RETURN_TYPE);
        } else {
            returnTypeObservable = EMFObservables.observeDetailValue(Realm.getDefault(),
                    getSelectedExpressionObservable(),
                    ExpressionPackage.Literals.EXPRESSION__RETURN_TYPE);
        }
        return returnTypeObservable;
    }

    protected IObservableValue getExpressionContentObservable() {
        IObservableValue returnContentObservable;
        final EditingDomain editingDomain = getEditingDomain();
        if (editingDomain != null) {
            returnContentObservable = CustomEMFEditObservables.observeDetailValue(Realm.getDefault(),
                    getSelectedExpressionObservable(),
                    ExpressionPackage.Literals.EXPRESSION__CONTENT);
        } else {
            returnContentObservable = EMFObservables.observeDetailValue(Realm.getDefault(),
                    getSelectedExpressionObservable(),
                    ExpressionPackage.Literals.EXPRESSION__CONTENT);
        }
        return returnContentObservable;
    }

    private IObservableValue getSelectedExpressionObservable() {
        return ViewerProperties.singleSelection().observe(this);
    }

    protected void bindEditableText(final IObservableValue typeObservable) {
        final UpdateValueStrategy modelToTargetTypeStrategy = new UpdateValueStrategy();
        modelToTargetTypeStrategy.setConverter(new Converter(String.class, Boolean.class) {

            @Override
            public Object convert(final Object from) {
                if (from == null) {
                    return true;
                }
                final boolean isScriptType = ExpressionConstants.SCRIPT_TYPE.equals(from.toString());
                final boolean isConnectorType = from.toString().equals(ExpressionConstants.CONNECTOR_TYPE);
                final boolean isXPathType = from.toString().equals(ExpressionConstants.XPATH_TYPE);
                final boolean isJavaType = from.toString().equals(ExpressionConstants.JAVA_TYPE);
                final boolean isQueryType = from.toString().equals(ExpressionConstants.QUERY_TYPE);
                final boolean isFormReference = from.toString().equals(ExpressionConstants.FORM_REFERENCE_TYPE);
                if (isScriptType) {
                    textTooltip.setText(Messages.editScriptExpressionTooltip);
                } else if (isXPathType) {
                    textTooltip.setText(Messages.editXpathExpressionTooltip);
                } else if (isJavaType) {
                    textTooltip.setText(Messages.editJavaExpressionTooltip);
                } else if (isConnectorType) {
                    textTooltip.setText(Messages.editConnectorExpressionTooltip);
                } else if (isQueryType) {
                    textTooltip.setText(Messages.editQueryExpressionTooltip);
                } else {
                    textTooltip.setText(null);
                }
                return !(isScriptType || isConnectorType || isJavaType || isXPathType || isQueryType
                        || isFormReference);
            }

        });

        internalDataBindingContext.bindValue(WidgetProperties.editable().observe(textControl), typeObservable,
                new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), modelToTargetTypeStrategy);
    }

    protected void updateContent(final String newContent) {
        final Expression selectedExpression = getSelectedExpression();
        if (newContent != null && !newContent.equals(selectedExpression.getContent())) {
            expressionItemProvider.setPropertyValue(selectedExpression,
                    ExpressionPackage.Literals.EXPRESSION__CONTENT.getName(), newContent);
        }
    }

    protected void updateContentType(final String newContentType) {
        final Expression selectedExpression = getSelectedExpression();
        if (!newContentType.equals(selectedExpression.getType())) {
            expressionItemProvider.setPropertyValue(getSelectedExpression(),
                    ExpressionPackage.Literals.EXPRESSION__TYPE.getName(), newContentType);
            if (!ExpressionConstants.SCRIPT_TYPE.equals(newContentType)) {
                expressionItemProvider.setPropertyValue(getSelectedExpression(),
                        ExpressionPackage.Literals.EXPRESSION__INTERPRETER.getName(), null);
            }
            if (ExpressionConstants.CONSTANT_TYPE.equals(newContentType)) {
                expressionItemProvider.setPropertyValue(getSelectedExpression(),
                        ExpressionPackage.Literals.EXPRESSION__REFERENCED_ELEMENTS.getName(), new ArrayList<>());
            }
        }
    }

    protected String getContentFromInput(final String input) {
        final Expression selectedExpression = getSelectedExpression();
        final String selectedExpressionType = selectedExpression.getType();
        if (ExpressionConstants.SCRIPT_TYPE.equals(selectedExpressionType)
                || ExpressionConstants.PATTERN_TYPE.equals(selectedExpressionType)
                || ExpressionConstants.XPATH_TYPE.equals(selectedExpressionType)
                || ExpressionConstants.JAVA_TYPE.equals(selectedExpressionType)
                || ExpressionConstants.QUERY_TYPE.equals(selectedExpressionType)
                || ExpressionConstants.FORM_REFERENCE_TYPE.equals(selectedExpressionType)) {
            return selectedExpression.getContent(); // NO CONTENT UPDATE WHEN
            // THOSES TYPES
        }

        final Set<String> cache = new HashSet<>();
        for (final Expression e : getFilteredExpressions()) {
            if (e.getName() != null && e.getName().equals(input)) {
                cache.add(e.getContent());
            }
        }
        if (cache.size() > 1) {
            for (final String content : cache) {
                if (content.equals(selectedExpression.getContent())) {
                    return content;
                }
            }
            return cache.iterator().next();
        } else if (cache.size() == 1) {
            return cache.iterator().next();
        }
        return input;
    }

    protected String getContentTypeFromInput(final String input) {
        final Expression selectedExpression = getSelectedExpression();
        Assert.isNotNull(selectedExpression);
        String expressionType = selectedExpression.getType();
        if (input.equals(selectedExpression.getName()) && ExpressionConstants.CONSTANT_TYPE.equals(expressionType)) {
            return expressionType;
        }
        if (expressionType == null) {
            return ExpressionConstants.CONSTANT_TYPE;
        }
        if (ExpressionConstants.SCRIPT_TYPE.equals(expressionType)) {
            return ExpressionConstants.SCRIPT_TYPE;
        } else if (ExpressionConstants.PATTERN_TYPE.equals(expressionType)) {
            return ExpressionConstants.PATTERN_TYPE;
        } else if (ExpressionConstants.JAVA_TYPE.equals(expressionType)) {
            return ExpressionConstants.JAVA_TYPE;
        } else if (ExpressionConstants.XPATH_TYPE.equals(expressionType)) {
            return ExpressionConstants.XPATH_TYPE;
        } else if (ExpressionConstants.SEARCH_INDEX_TYPE.equals(expressionType)) {
            return ExpressionConstants.SEARCH_INDEX_TYPE;
        } else if (ExpressionConstants.QUERY_TYPE.equals(expressionType)) {
            return ExpressionConstants.QUERY_TYPE;
        } else if (ExpressionConstants.MESSAGE_ID_TYPE.equals(expressionType)) {
            return ExpressionConstants.MESSAGE_ID_TYPE;
        }
        return matchingExpressionType(input, expressionType);
    }

    private String matchingExpressionType(final String input, String expressionType) {
        return getFilteredExpressions().stream()
                .filter(expr -> Objects.equals(expr.getName(), input))
                .filter(expr -> Objects.equals(expr.getType(), expressionType))
                .findFirst()
                .map(Expression::getType)
                .orElse(ExpressionConstants.CONSTANT_TYPE);
    }

    protected void internalRefresh() {
        final Control composite = getControl();
        if (!composite.isDisposed()) {
            refreshTypeDecoration();
            refreshMessageDecoration();
            refreshEraseEnablement();
        }
    }

    private void refreshEraseEnablement() {
        Expression selectedExpression = getSelectedExpression();
        contentAssistText.setEraseEnabled(
                selectedExpression != null && (selectedExpression.hasName() || selectedExpression.hasContent()));
    }

    private void refreshTypeDecoration() {
        updateTypeDecorationIcon();
        updateTypeDecorationDescriptionText();
    }

    private void updateTypeDecorationIcon() {
        final ILabelProvider labelProvider = (ILabelProvider) getLabelProvider();
        contentAssistText.setImage(labelProvider.getImage(getSelectedExpression()));
    }

    private void updateTypeDecorationDescriptionText() {
        final ExpressionTypeLabelProvider expTypeProvider = new ExpressionTypeLabelProvider();
        final String desc = expTypeProvider.getText(getSelectedExpression().getType());
        contentAssistText.setImageToolTipText(desc);
    }

    private void refreshMessageDecoration() {
        Display.getDefault().asyncExec(() -> {
            if (messageDecoration.getControl() != null
                    && !messageDecoration.getControl().isDisposed()) {
                final String message = status.getMessage();
                if (message != null && !message.isEmpty()) {
                    messageDecoration.setDescriptionText(message);
                    messageDecoration.setShowOnlyOnFocus(false);
                    final Image icon = getImageForMessageKind(status);
                    if (icon != null) {
                        messageDecoration.setImage(icon);
                    }
                    messageDecoration.show();
                } else {
                    messageDecoration.hide();
                }
            }
        });
    }

    private Image getImageForMessageKind(final IStatus status) {
        if (!PlatformUI.isWorkbenchRunning()) {
            return null;
        }
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench != null) {
            switch (status.getSeverity()) {
                case IStatus.WARNING:
                    return workbench.getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
                case IStatus.INFO:
                    return workbench.getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
                case IStatus.ERROR:
                    return workbench.getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
                default:
                    break;
            }
        }
        return null;
    }

    @Override
    public void setLabelProvider(final IBaseLabelProvider labelProvider) {
        Assert.isTrue(labelProvider instanceof ExpressionLabelProvider);
        super.setLabelProvider(labelProvider);
    }

    public Control getTextControl() {
        return textControl;
    }

    public ToolItem getButtonControl() {
        return editControl;
    }

    public void addFilter(final ViewerFilter viewerFilter) {
        filters.add(viewerFilter);
    }

    public void removeFilter(final ViewerFilter viewerFilter) {
        filters.remove(viewerFilter);
    }

    public String getExample() {
        return example;
    }

    public void setExample(final String example) {
        this.example = example;
        if (textControl instanceof Text) {
            ((Text) textControl).setMessage(example);
            textControl.redraw();
        }
    }

    public String getMessage() {
        return status.getMessage();
    }

    public void setMessage(String message) {
        this.defaultStatus = ValidationStatus.info(message);
        this.status = defaultStatus;
        refreshMessageDecoration();
    }

    @Override
    protected void handleDispose(final DisposeEvent event) {
        if (expressionBinding != null && externalDataBindingContext != null) {
            externalDataBindingContext.removeBinding(expressionBinding);
            expressionBinding.dispose();
        }
        if (internalDataBindingContext != null) {
            internalDataBindingContext.dispose();
        }
        super.handleDispose(event);
    }

    public void setMandatoryField(final String fieldName, final DataBindingContext dbc) {
        setMandatoryField(fieldName, dbc, null);
    }

    public void setMandatoryField(final String fieldName, final DataBindingContext dbc, IValidator<String> validator) {
        mandatoryFieldName = fieldName;
        externalDataBindingContext = dbc;
        this.mandatoryFieldValidator = Optional.ofNullable(validator);
    }

    public void addExpressionEditorChangedListener(final ISelectionChangedListener iSelectionChangedListener) {
        expressionEditorListener.add(iSelectionChangedListener);
    }

    public void setContext(final EObject context) {
        this.context = context;
    }

    public void updateAutocompletionProposals() {
        if (expressionNatureProvider != null) {
            final Set<Expression> filteredExpressions = getFilteredExpressions();
            autoCompletion.setProposals(filteredExpressions.toArray(new Expression[filteredExpressions.size()]));
        }
    }

    public void setProposalsFiltering(final boolean filterProposal) {
        autoCompletion.getContentProposalProvider().setFiltering(filterProposal);
    }

    public ToolItem getEraseControl() {
        return contentAssistText.getEraseControl();
    }

    protected Converter<String, String> getNameConverter() {
        return new Converter<>(String.class, String.class) {

            @Override
            public String convert(final String input) {
                if (textControl instanceof Text) {
                    final int caretPosition = ((Text) textControl).getCaretPosition();
                    final String contentTypeFromInput = getContentTypeFromInput(input);
                    if (!Objects.equals(getSelectedExpression().getType(), contentTypeFromInput)) {
                        updateContentType(contentTypeFromInput);
                        refresh();
                    }

                    final String contentFromInput = getContentFromInput(input);
                    if (!Objects.equals(getSelectedExpression().getContent(), contentFromInput)) {
                        updateContent(contentFromInput);
                    }

                    final boolean hasBeenExecuted = executeOperation(input);

                    if (hasBeenExecuted) {
                        ((Text) textControl).setSelection(caretPosition, caretPosition);
                    }
                }
                return input;
            }
        };
    }

    public void addExpressionValidator(final IExpressionValidator validator) {
        expressionViewerValidator.addValidator(validator);
    }

    public void addExpressionValidationListener(final IExpressionValidationListener listener) {
        if (!validationListeners.contains(listener)) {
            validationListeners.add(listener);
        }
    }

    public void validate() {
        expressionViewerValidator.setContext(context);
        expressionViewerValidator.setExpression(getSelectedExpression());
        expressionViewerValidator.addValidationsStatusChangedListener(this);
        for (final IExpressionValidationListener l : validationListeners) {
            expressionViewerValidator.addValidationsStatusChangedListener(l);
        }
        final Expression selectedExpression = getSelectedExpression();
        if (selectedExpression != null) {
            expressionViewerValidator.validate(selectedExpression.getName());
        }
        validateExternalDatabindingContextTargets(externalDataBindingContext);
    }

    public void setExternalDataBindingContext(final DataBindingContext ctx) {
        externalDataBindingContext = ctx;
    }

    @Override
    public void proposalAccepted(final IContentProposal proposal) {
        final int proposalAcceptanceStyle = autoCompletion.getContentProposalAdapter().getProposalAcceptanceStyle();
        if (proposalAcceptanceStyle == ContentProposalAdapter.PROPOSAL_REPLACE) {
            final Expression selectedExpression = getSelectedExpression();
            final CompoundCommand cc = new CompoundCommand("Update Expression (and potential side components)");
            final ExpressionProposal prop = (ExpressionProposal) proposal;
            final Expression copy = EcoreUtil.copy((Expression) prop.getExpression());
            copy.setReturnTypeFixed(selectedExpression.isReturnTypeFixed());
            sideModificationOnProposalAccepted(cc, copy);

            updateSelection(cc, copy);
            fireSelectionChanged(new SelectionChangedEvent(ExpressionViewer.this,
                    new StructuredSelection(selectedExpression)));
            validate();
        }
    }

    protected void sideModificationOnProposalAccepted(final CompoundCommand cc, final Expression copy) {

    }

    @Override
    public void proposalPopupOpened(final BonitaContentProposalAdapter adapter) {
        manageNatureProviderAndAutocompletionProposal(getInput());
    }

    @Override
    public void proposalPopupClosed(final BonitaContentProposalAdapter adapter) {

    }

    public IExpressionNatureProvider getExpressionNatureProvider() {
        return expressionNatureProvider;
    }

    @Override
    public boolean isPageFlowContext() {
        return isPageFlowContext;
    }

    @Override
    public void setIsPageFlowContext(final boolean isPageFlowContext) {
        this.isPageFlowContext = isPageFlowContext;

    }

    public void setRefactorOperationToExecuteWhenUpdatingContent(final AbstractRefactorOperation<?, ?, ?> operation) {
        this.operation = operation;
    }

    private boolean executeOperation(final String newValue) {
        boolean hasBeenExecuted = false;
        if (operation != null) {
            operation.clearItemToRefactor();
            final Object oldValue = getInput();
            if (oldValue instanceof Element && !newValue.equals(((Element) getInput()).getName())
                    || oldValue instanceof SearchIndex
                            && !newValue.equals(((SearchIndex) getInput()).getName().getName())) {
                final SearchIndex searchIndex = (SearchIndex) getInput();
                final SearchIndex newSearchIndex = EcoreUtil.copy(searchIndex);
                newSearchIndex.getName().setContent(newValue);
                newSearchIndex.getName().setName(newValue);
                operation.addItemToRefactor(newSearchIndex, searchIndex);
                final IProgressService service = PlatformUI.getWorkbench().getProgressService();
                try {
                    service.busyCursorWhile(operation);
                    hasBeenExecuted = true;
                } catch (final InvocationTargetException e) {
                    BonitaStudioLog.error(e);
                } catch (final InterruptedException e) {
                    BonitaStudioLog.error(e);
                }
            }
        }
        return hasBeenExecuted;
    }

    public void setRemoveOperation(final AbstractRefactorOperation<?, ?, ?> removeOperation) {
        this.removeOperation = removeOperation;
    }

    private boolean executeRemoveOperation(final CompoundCommand cc) {
        boolean isExecuted = false;
        if (removeOperation != null) {
            removeOperation.setCompoundCommand(cc);
            final IProgressService service = PlatformUI.getWorkbench().getProgressService();
            try {
                service.busyCursorWhile(removeOperation);
                isExecuted = true;
            } catch (final InvocationTargetException | InterruptedException e) {
                BonitaStudioLog.error(e);
            }

        }
        return isExecuted;
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.studio.common.IBonitaVariableContext#isOverViewContext()
     */
    @Override
    public boolean isOverViewContext() {
        return isOverviewContext;
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.studio.common.IBonitaVariableContext#setIsOverviewContext(boolean)
     */
    @Override
    public void setIsOverviewContext(final boolean isOverviewContext) {
        this.isOverviewContext = isOverviewContext;

    }

    public void setAutocomplitionLabelProvider(final LabelProvider labelProvider) {
        getContentAssistText().getAutocompletion().setLabelProvider(labelProvider);

    }

    @Override
    public void validationStatusChanged(final IStatus newStatus) {
        this.status = newStatus.isOK() ? defaultStatus : newStatus;
        refreshMessageDecoration();
    }

    @Override
    public void handleValueChange(final ValueChangeEvent event) {
        validate();
        refreshEraseEnablement();
    }

    @Override
    public void setInput(final Object input) {
        if (isOldContextAndInputSimilar(input)) {
            setContext((EObject) input);
        }
        super.setInput(input);
    }

    protected boolean isOldContextAndInputSimilar(final Object input) {
        return getInput() != null && input instanceof EObject && getInput().equals(context);
    }

    public void addEditorFilter(final String... expressionTypes) {
        for (final String type : expressionTypes) {
            filteredEditor.add(type);
        }
    }

    public EObject getContext() {
        return context;
    }

    public IStatus getStatus() {
        return status;
    }

    public void resetStatus() {
        this.status = ValidationStatus.info("");
    }
}
