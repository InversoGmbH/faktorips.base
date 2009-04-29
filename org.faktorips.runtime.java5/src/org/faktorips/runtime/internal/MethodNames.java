/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import org.faktorips.runtime.ICopySupport;
import org.faktorips.runtime.IDeltaSupport;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IModelObjectVisitor;
import org.faktorips.runtime.IVisitorSupport;
import org.faktorips.runtime.test.IpsTestCase2;
import org.w3c.dom.Element;

/**
 * Gives access to the method names defined in the runtime that the generated code refers to,
 * e.g. by implementing a method with the given name. The reference to the acutal
 * method is documented in the Javadoc "see" tags.
 * <p>
 * The constants are used by the code generator.
 * 
 * @author Jan Ortmann
 */
public class MethodNames {

    /**
     * @see org.faktorips.runtime.IConfigurableModelObject#getEffectiveFromAsCalendar()
     */
    public final static String GET_EFFECTIVE_FROM_AS_CALENDAR = "getEffectiveFromAsCalendar";

    /**
     * @see org.faktorips.runtime.internal.AbstractConfigurableModelObject#effectiveFromHasChanged()
     */
    public final static String EFFECTIVE_FROM_HAS_CHANGED = "effectiveFromHasChanged";

    /**
     * @see org.faktorips.runtime.IProductComponent#getId()
     */
    public final static String GET_PRODUCT_COMPONENT_ID = "getId";

    /**
     * @see org.faktorips.runtime.IConfigurableModelObject#getProductComponent()
     */
    public final static String GET_PRODUCT_COMPONENT = "getProductComponent";

    /**
     * @see org.faktorips.runtime.IConfigurableModelObject#setProductComponent()
     */
    public final static String SET_PRODUCT_COMPONENT = "setProductComponent";

    /**
     * @see org.faktorips.runtime.IRuntimeRepository#getExistingProductComponent(String)
     */
    public final static String GET_EXISTING_PRODUCT_COMPONENT = "getExistingProductComponent";

    /**
     * @see org.faktorips.runtime.IProductComponent#createPolicyComponent()
     */
    public final static String CREATE_POLICY_COMPONENT = "createPolicyComponent";

    /**
     * @see ProductComponent#getRepository()
     * @see ProductComponentGeneration#getRepository()
     * @see IpsTestCase2#getRepository()
     */
    public final static String GET_REPOSITORY = "getRepository";

    /**
     * @see org.faktorips.runtime.IRuntimeRepository#isModifiable()
     */
    public final static String IS_MODIFIABLE = "isModifiable";

    /**
     * @see org.faktorips.runtime.IModelObjectPart#getParentModelObject()
     */
    public final static String GET_PARENT = "getParentModelObject";

    /**
     * @see AbstractModelObjectPart#setParentModelObjectInternal(IModelObject)
     */
    public final static String SET_PARENT = "setParentModelObjectInternal";

    /**
     * @see AbstractModelObject#removeChildModelObjectInternal(IModelObject)
     */
    public final static String REMOVE_CHILD_MODEL_OBJECT_INTERNAL = "removeChildModelObjectInternal";


    public final static String GET_PRODUCT_CMPT_GENERATION = "getProductCmptGeneration";

    public final static String COPY_PRODUCT_CMPT_AND_GENERATION_INTERNAL = "copyProductCmptAndGenerationInternal";

    /**
     * @see org.faktorips.runtime.IRuntimeRepository#getExistingProductComponentGeneration(String, Calendar)
     */
    public final static String GET_EXISTING_PRODUCT_COMPONENT_GENERATION = "getExistingProductComponentGeneration";

    /**
     * @see AbstractConfigurableModelObject#initPropertiesFromXml(HashMap)
     */
    public final static String INIT_PROPERTIES_FROM_XML = "initPropertiesFromXml";

    /**
     * @see AbstractConfigurableModelObject#createChildFromXml(Element)
     */
    public final static String CREATE_CHILD_FROM_XML = "createChildFromXml";

    /**
     * @see AbstractConfigurableModelObject#createUnresolvedReference(Object, String, String)
     */
    public final static String CREATE_UNRESOLVED_REFERENCE = "createUnresolvedReference";

    /**
     * notifyChangeListeners(PropertyChangeEvent)
     */
    public final static String NOTIFIY_CHANGE_LISTENERS = "notifyChangeListeners";

    /**
     * @see AbstractModelObject#existsChangeListenerToBeInformed()
     */
    public final static String EXISTS_CHANGE_LISTENER_TO_BE_INFORMED = "existsChangeListenerToBeInformed";

    /**
     * @see ProductComponentGeneration#getValidFrom(TimeZone)
     */
    public final static String GET_VALID_FROM = "getValidFrom";

    /**
     * @see IDeltaSupport#computeDelta(org.faktorips.runtime.IModelObject, org.faktorips.runtime.IDeltaComputationOptions)
     */
    public final static String COMPUTE_DELTA = "computeDelta";

    /**
     * @see ICopySupport#newCopy
     */
    public final static String NEW_COPY = "newCopy";

    /**
     * @see ModelObjectDelta#checkPropertyChange(..)
     */
    public final static String MODELOBJECTDELTA_CHECK_PROPERTY_CHANGE = "checkPropertyChange";

    /**
     * @see ModelObjectDelta#createChildDeltas(..)
     */
    public final static String MODELOBJECTDELTA_CREATE_CHILD_DELTAS = "createChildDeltas";

    /**
     * @see ModelObjectDelta#newEmptyDelta(org.faktorips.runtime.IModelObject, org.faktorips.runtime.IModelObject)
     */
    public final static String MODELOBJECTDELTA_NEW_EMPTY_DELTA = "newEmptyDelta";

    /**
     * @see ModelObjectDelta#newDelta(org.faktorips.runtime.IModelObject, org.faktorips.runtime.IModelObject)
     */
    public final static String MODELOBJECTDELTA_NEW_DELTA = "newDelta";

    /**
     * @see IVisitorSupport#accept
     */
    public final static String ACCEPT_VISITOR = "accept";

    /**
     * @see IModelObjectVisitor#visit(org.faktorips.runtime.IModelObject)
     */
    public final static String VISITOR_VISIT = "visit";
}
