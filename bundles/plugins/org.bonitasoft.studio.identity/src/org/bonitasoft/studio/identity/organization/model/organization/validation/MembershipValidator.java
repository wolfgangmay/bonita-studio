/**
 *
 * $Id$
 */
package org.bonitasoft.studio.identity.organization.model.organization.validation;


/**
 * A sample validator interface for {@link org.bonitasoft.studio.organization.model.organization.Membership}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface MembershipValidator {
    boolean validate();

    boolean validateUserName(String value);
    boolean validateRoleName(String value);
    boolean validateGroupName(String value);
    boolean validateGroupParentPath(String value);
    boolean validateAssignedBy(String value);
    boolean validateAssignedDate(long value);
}
