/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.studio.ui.util;

import org.bonitasoft.studio.common.repository.Messages;
import org.bonitasoft.bpm.model.process.AbstractProcess;
import org.bonitasoft.bpm.model.process.Pool;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;

public class ProcessValidationStatus extends Status {

    private final AbstractProcess process;
    private final IStatus validationStatus;

    public ProcessValidationStatus(final AbstractProcess process, final IStatus status) {
        super(status.getSeverity(), status.getPlugin(), status.getMessage());
        this.process = process;
        validationStatus = status;
    }

    @Override
    public String getMessage() {
        if (validationStatus.isMultiStatus()) {
            final int issues = countIssues();
            if (process instanceof Pool) {
                String diagramFileName = process.eResource().getURI().lastSegment();
                return String.format("%s %s %s %s (%s) ", issues,
                        issues == 1 ? Messages.issueFoundIn : Messages.issuesFoundIn, process.getName(),
                        process.getVersion(), URI.decode(diagramFileName));
            }
            return String.format("%s %s %s (%s)", issues, issues == 1 ? Messages.issueFoundIn : Messages.issuesFoundIn,
                    process.getName(), process.getVersion());
        }
        return validationStatus.getSeverity() == IStatus.ERROR
                ? String.format("%s %s %s (%s)", 1, Messages.issueFoundIn, process.getName(), process.getVersion())
                : String.format("%s (%s)", process.getName(), process.getVersion());
    }

    protected int countIssues() {
        int count = validationStatus.getChildren().length;
        for (final IStatus child : validationStatus.getChildren()) {
            if (child.getSeverity() == INFO || child.getSeverity() == WARNING || child.isOK()) {
                count--;
            }
        }
        return count;
    }

    @Override
    public boolean isOK() {
        return getSeverity() != IStatus.ERROR;
    }

    @Override
    public int getSeverity() {
        final int severity = super.getSeverity();
        return severity != ERROR ? OK : severity;
    }

    public AbstractProcess getProcess() {
        return process;
    }

}
