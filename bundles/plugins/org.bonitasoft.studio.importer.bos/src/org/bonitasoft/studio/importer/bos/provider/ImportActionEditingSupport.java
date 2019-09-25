package org.bonitasoft.studio.importer.bos.provider;

import java.util.Objects;

import org.bonitasoft.studio.importer.bos.model.AbstractFileModel;
import org.bonitasoft.studio.importer.bos.model.ConflictStatus;
import org.bonitasoft.studio.importer.bos.model.ImportAction;
import org.bonitasoft.studio.importer.bos.model.SmartImportFileStoreModel;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;

public class ImportActionEditingSupport extends EditingSupport {

    private final TreeViewer viewer;
    private final ComboBoxViewerCellEditor editor;

    public ImportActionEditingSupport(TreeViewer viewer) {
        super(viewer);
        this.viewer = viewer;
        this.editor = new ComboBoxViewerCellEditor(viewer.getTree(), SWT.BORDER | SWT.READ_ONLY);
        editor.setActivationStyle(ComboBoxViewerCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION);
        editor.setContentProvider(ArrayContentProvider.getInstance());
        editor.setLabelProvider(new LabelProvider());
        editor.setInput(ImportAction.values());
        IViewerObservableValue mainSelection = ViewersObservables.observeSingleSelection(viewer);
        editor.getViewer().addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof ImportAction && Objects.equals((element), ImportAction.SMART_IMPORT)) {
                    return mainSelection.getValue() instanceof SmartImportFileStoreModel
                            && ((SmartImportFileStoreModel) mainSelection.getValue()).isSmartImportable();
                }
                return true;
            }
        });
        mainSelection.addValueChangeListener(e -> editor.getViewer().refresh()); // trigger the above filter when the selection change -> each element of the main viewer has its available choices
    }

    @Override
    protected CellEditor getCellEditor(Object element) {
        return editor;
    }

    @Override
    protected boolean canEdit(Object element) {
        return element instanceof AbstractFileModel
                && ((AbstractFileModel) element).getStatus() == ConflictStatus.CONFLICTING;
    }

    @Override
    protected Object getValue(Object element) {
        if (element instanceof AbstractFileModel) {
            return ((AbstractFileModel) element).getImportAction();
        }
        return ImportAction.OVERWRITE;
    }

    @Override
    protected void setValue(Object element, Object value) {
        if (element instanceof AbstractFileModel) {
            ((AbstractFileModel) element)
                    .setImportAction((ImportAction) value);
            viewer.update(element, null);
        }
    }
}
