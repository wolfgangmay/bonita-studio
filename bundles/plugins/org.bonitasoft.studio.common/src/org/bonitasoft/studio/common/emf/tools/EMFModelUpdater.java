/**
 * Copyright (C) 2018 Bonitasoft S.A.
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
package org.bonitasoft.studio.common.emf.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.bonitasoft.studio.common.Activator;
import org.bonitasoft.studio.common.Strings;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;

/**
 * This class is responsible of updating a concerte EMF model (with uuids) using a uuid free representation (EMF copy) of
 * that same model
 * The goal is to avoid uuid regeneration at each model changes when editing tyhe model using a Wizard
 */
public class EMFModelUpdater<T extends EObject> {

    private T source;
    private T workingCopy;
    private CustomCopier copier = new CustomCopier();
    private List<EObjectFeature> synched = new ArrayList<>();
    private Set<String> manyFeaturesSynched = new HashSet<>();
    private boolean keepUUIDConsistency;

    /**
     * Original (before edition) EMF object with UUID
     */
    public EMFModelUpdater<T> from(T source) {
        this.source = source;
        this.workingCopy = (T) copier.copy(source);
        copier.copyReferences();
        keepUUIDConsistency = shouldKeepUUIDConsistency(source);
        return this;
    }

    private boolean shouldKeepUUIDConsistency(T eObject) {
        String uuid = getEObjectID(eObject);
        return Strings.hasText(uuid) && !uuid.equals("/");
    }

    public T getWorkingCopy() {
        return workingCopy;
    }

    /**
     * Applies the changes to the source object limiting UUID changes
     */
    public void update() {
        TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(source);
        if (editingDomain != null) {
            editingDomain.getCommandStack().execute(createUpdateCommand(editingDomain));
        } else {
            doUpdate(source, workingCopy);
        }
    }

    private void doUpdate(EObject sourceObject, EObject updatedObject) {
        synched.clear();
        manyFeaturesSynched.clear();
        deepEObjectUpdate(sourceObject, updatedObject);
    }

    public void editWorkingCopy(T value) {
        doUpdate(workingCopy, value);
    }

    public RecordingCommand createUpdateCommand(TransactionalEditingDomain editingDomain) {
        return new RecordingCommand(editingDomain) {

            @Override
            protected void doExecute() {
                doUpdate(source, workingCopy);
            }
        };
    }

    private void deepEObjectUpdate(EObject source, EObject target) {
        source.eClass()
                .getEAllStructuralFeatures()
                .stream()
                .filter(EAttribute.class::isInstance)
                .filter(feature -> {
                    if (!target.eClass().getEAllStructuralFeatures().contains(feature)) {
                        BonitaStudioLog
                                .warning(String.format("Cannot update EObject value: %s does not have a %s feature.",
                                        target.eClass().getName(), feature.getName()), Activator.PLUGIN_ID);
                        return false;
                    }
                    return true;
                })
                .forEach(feature -> source.eSet(feature, target.eGet(feature)));

        source.eClass()
                .getEAllStructuralFeatures()
                .stream()
                .filter(EReference.class::isInstance)
                .filter(feature -> {
                    if (!target.eClass().getEAllStructuralFeatures().contains(feature)) {
                        BonitaStudioLog
                                .warning(String.format("Cannot update EObject value: %s does not have a %s feature.",
                                        target.eClass().getName(), feature.getName()), Activator.PLUGIN_ID);
                        return false;
                    }
                    return true;
                })
                .forEach(feature -> {
                    if (!keepUUIDConsistency) {
                        EcoreUtil.replace(source, target);
                        return;
                    }

                    if (feature.isMany()) {
                        String uuid = getEObjectID(source);
                        String key = uuid + feature.getName();
                        if (!manyFeaturesSynched.contains(key)) {
                            manyFeaturesSynched.add(key);
                            handleManyCase(source, target, feature);
                        }
                    } else {
                        Object sourceRef = source.eGet(feature);
                        Object targetRef = target.eGet(feature);
                        if ((sourceRef == null || sourceRef instanceof EObject) && targetRef instanceof EObject
                                && !synched.contains(EObjectFeature.create(source, feature))
                                && !EcoreUtil.equals((EObject) sourceRef, (EObject) targetRef)) {
                            synched.add(EObjectFeature.create(source, feature));
                            if (sourceRef != null
                                    && Objects.equals(((EObject) sourceRef).eClass(), ((EObject) targetRef).eClass())) {
                                deepEObjectUpdate((EObject) sourceRef, (EObject) targetRef);
                            } else {
                                source.eSet(feature, targetRef);
                            }
                        }
                    }
                });
    }

    static class EObjectFeature {

        private EObject eObject;
        private EStructuralFeature feature;

        private EObjectFeature(EObject eObject, EStructuralFeature feature) {
            this.eObject = eObject;
            this.feature = feature;
        }

        static EObjectFeature create(EObject eObject, EStructuralFeature feature) {
            return new EObjectFeature(eObject, feature);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((eObject == null) ? 0 : eObject.hashCode());
            result = prime * result + ((feature == null) ? 0 : feature.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            EObjectFeature other = (EObjectFeature) obj;
            if (eObject == null) {
                if (other.eObject != null)
                    return false;
            } else if (!eObject.equals(other.eObject))
                return false;
            if (feature == null) {
                if (other.feature != null)
                    return false;
            } else if (!feature.equals(other.feature))
                return false;
            return true;
        }

    }

    @SuppressWarnings("unchecked")
    private void handleManyCase(EObject source, EObject target, EStructuralFeature feature) {
        List sourceList = (List) source.eGet(feature);
        List targetList = (List) target.eGet(feature);

        //Remove deleted element
        sourceList.removeIf(
                sourceElement -> findEObject(targetList, getEObjectID((EObject) sourceElement)) == null);

        List<EObject> alreadyExistingEObjects = new ArrayList<>();
        //Update existing element
        for (Object sourceElement : sourceList) {
            EObject targetElement = findEObject(targetList, getEObjectID((EObject) sourceElement));
            if (sourceElement instanceof EObject
                    && targetElement instanceof EObject) {
                deepEObjectUpdate((EObject) sourceElement,
                        targetElement);
                alreadyExistingEObjects.add(targetElement);
            }
        }

        //Add new element
        for (Object targetElement : targetList) {
            if (!alreadyExistingEObjects.contains(targetElement)
                    && targetElement instanceof EObject
                    && getEObjectID((EObject) targetElement) == null) {//Add new Object
                sourceList.add(targetList.indexOf(targetElement), EcoreUtil.copy((T) targetElement));
            }
        }

        //Reorder list
        for (EObject element : alreadyExistingEObjects) {
            int sourceIndex = sourceList.indexOf(findEObject(sourceList, getEObjectID((EObject) element)));
            int targetIndex = targetList.indexOf(element);
            if (sourceIndex != -1 && sourceIndex != targetIndex) {
                if (sourceList instanceof EList) {
                    ((EList) sourceList).move(targetIndex, sourceIndex);
                }
            }
        }
    }

    private EObject findEObject(List<EObject> targetList, String eObjectID) {
        return eObjectID == null ? null : targetList.stream()
                .filter(eObject -> Objects.equals(eObjectID, getEObjectID(eObject)))
                .findFirst()
                .orElse(null);
    }

    private String getEObjectID(EObject eObject) {
        return eObject.eAdapters().stream()
                .filter(UUIDAdapter.class::isInstance)
                .map(UUIDAdapter.class::cast)
                .findFirst()
                .map(UUIDAdapter::getUUID)
                .orElse(ModelHelper.getEObjectID(eObject));
    }

    class CustomCopier extends Copier {

        /*
         * (non-Javadoc)
         * @see org.eclipse.emf.ecore.util.EcoreUtil.Copier#copy(org.eclipse.emf.ecore.EObject)
         */
        @Override
        public EObject copy(EObject eObject) {
            EObject copy = super.copy(eObject);
            String uuid = eObject.eAdapters().stream()
                    .filter(UUIDAdapter.class::isInstance)
                    .map(UUIDAdapter.class::cast)
                    .findFirst()
                    .map(UUIDAdapter::getUUID)
                    .orElse(ModelHelper.getEObjectID(eObject));
            if (uuid != null) {
                copy.eAdapters().add(new UUIDAdapter(uuid));
            }
            return copy;
        }
    }

    class UUIDAdapter implements Adapter {

        private String uuid;

        public UUIDAdapter(String uuid) {
            this.uuid = uuid;
        }

        public String getUUID() {
            return uuid;
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.emf.common.notify.Adapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
         */
        @Override
        public void notifyChanged(Notification notification) {
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.emf.common.notify.Adapter#getTarget()
         */
        @Override
        public Notifier getTarget() {
            return null;
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.emf.common.notify.Adapter#setTarget(org.eclipse.emf.common.notify.Notifier)
         */
        @Override
        public void setTarget(Notifier newTarget) {

        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.emf.common.notify.Adapter#isAdapterForType(java.lang.Object)
         */
        @Override
        public boolean isAdapterForType(Object type) {
            return false;
        }

    }

    public boolean hasChanged() {
        return !EcoreUtil.equals(workingCopy, source);
    }

}
