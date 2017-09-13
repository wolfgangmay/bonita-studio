/**
 * Copyright (C) 2017 Bonitasoft S.A.
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
package org.bonitasoft.studio.ui.dialog;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bonitasoft.studio.common.jface.dialog.ProblemsDialog;
import org.bonitasoft.studio.common.jface.dialog.TypedLabelProvider;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

public class MultiStatusDialog extends ProblemsDialog<IStatus> {

    private MultiStatus status;

    public MultiStatusDialog(Shell parentShell, String dialogTitle, String dialogMessage, int dialogImageType,
            String[] dialogButtonLabels, MultiStatus status) {
        super(parentShell, dialogTitle, dialogMessage, dialogImageType, dialogButtonLabels);
        this.status = status;
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.studio.common.jface.dialog.ProblemsDialog#getTypedLabelProvider()
     */
    @Override
    protected TypedLabelProvider<IStatus> getTypedLabelProvider() {
        return new TypedLabelProvider<IStatus>() {

            @Override
            public String getText(IStatus element) {
                return element.getMessage();
            }

            @Override
            public Image getImage(IStatus element) {
                switch (element.getSeverity()) {
                    case IStatus.WARNING:
                        return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);
                    case IStatus.INFO:
                        return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO);
                    default:
                        return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);
                }
            }
        };
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.studio.common.jface.dialog.ProblemsDialog#getInput()
     */
    @Override
    protected Collection<IStatus> getInput() {
        return Stream
                .of(status.getChildren())
                .collect(Collectors.toList());
    }

}
