/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere. Alle Rechte vorbehalten. Dieses Programm und
 * alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, etc.) dürfen nur unter
 * den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung
 * Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann. Mitwirkende: Faktor Zehn GmbH -
 * initial API and implementation
 **************************************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.EmptyBuilderSet;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * A class that hold information about IIpsArtefactBuilderSets that are registered with the
 * corresponding extension point.
 * 
 * @see IpsModel#getIpsArtefactBuilderSetInfos()
 * @author Peter Erzberger
 */
public class IpsArtefactBuilderSetInfo implements IIpsArtefactBuilderSetInfo{

    private Class builderSetClass;
    private String builderSetId;
    private String builderSetLabel;
    private String namespace;
    private String builderSetClassName;
    private Map propertyDefinitions;


    private IpsArtefactBuilderSetInfo(String namespace, String builderSetClassName, String builderSetId, String builderSetLabel,
            Map propertyDefinitions) {
        super();
        this.namespace = namespace;
        this.builderSetClassName = builderSetClassName;
        this.builderSetId = builderSetId;
        this.builderSetLabel = builderSetLabel;
        this.propertyDefinitions = propertyDefinitions;
    }

    /**
     * Creates an IIpsArtefactBuilderSet if one has been registered with the provided
     * <code>builderSetId</code> at the artefact builder set extension point. Otherwise an
     * <code>EmptyBuilderSet</code> will be returned.
     */
    public IIpsArtefactBuilderSet create(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);
        
        try {
            IIpsArtefactBuilderSet builderSet = (IIpsArtefactBuilderSet)getBuilderSetClass().newInstance();
            builderSet.setId(getBuilderSetId());
            builderSet.setLabel(getBuilderSetLabel());
            builderSet.setIpsProject(ipsProject);
            return builderSet;
        } catch(ClassCastException e){
            IpsPlugin.log(new IpsStatus("The registered builder set " + getBuilderSetClass() +  //$NON-NLS-1$
                    " doesn't implement the " + IIpsArtefactBuilderSet.class + " interface.", e)); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (InstantiationException e) {
            IpsPlugin.log(new IpsStatus("Unable to instantiate the builder set " + getBuilderSetClass(), e)); //$NON-NLS-1$

        } catch (IllegalAccessException e) {
            IpsPlugin.log(new IpsStatus("Unable to instantiate the builder set " + getBuilderSetClass(), e)); //$NON-NLS-1$
        }
        return new EmptyBuilderSet();
    }
    
    /**
     * {@inheritDoc}
     */
    public IIpsArtefactBuilderSetConfigModel createDefaultConfiguration(IIpsProject ipsProject){
        IpsArtefactBuilderSetConfigModel configModel = new IpsArtefactBuilderSetConfigModel();
        for (Iterator it = propertyDefinitions.values().iterator(); it.hasNext();) {
            IIpsBuilderSetPropertyDef propertyDef = (IIpsBuilderSetPropertyDef)it.next();
            configModel.setPropertyValue(propertyDef.getName(), propertyDef.getDefaultValue(ipsProject), propertyDef.getDescription());
        }
        return configModel;
    }

    
    /*
     * Returns the class of the IIpsArtefactBuilderSet implementation class.
     */
    private Class getBuilderSetClass() {
        if (builderSetClass == null) {
            try {
                builderSetClass = Platform.getBundle(namespace).loadClass(builderSetClassName); 
            } catch (ClassNotFoundException e) {
                IpsPlugin.log(new IpsStatus("Unable to load the IpsArtefactBuilderSet class " + builderSetClassName + //$NON-NLS-1$
                        " with the id " + builderSetId)); //$NON-NLS-1$
            }
        }
        return builderSetClass;
    }

    /**
     * Returns the id by which the <code>IIpsArtefactBuilderSet</code> is registered with the
     * system.
     */
    public String getBuilderSetId() {
        return builderSetId;
    }

    /**
     * Returns the label for the corresponding <code>IIpsArtefactBuilderSet</code>.
     */
    public String getBuilderSetLabel() {
        return builderSetLabel;
    }

    /**
     * Returns the property definition object for the specified name or <code>null</code> if it doesn't exist.
     */
    public IIpsBuilderSetPropertyDef getPropertyDefinition(String name){
        return (IIpsBuilderSetPropertyDef)propertyDefinitions.get(name);
    }

    /**
     * Returns the properties defined for this IpsArtefactBuilderSet.
     */
    public IIpsBuilderSetPropertyDef[] getPropertyDefinitions() {
        return (IIpsBuilderSetPropertyDef[])propertyDefinitions.values()
                .toArray(new IIpsBuilderSetPropertyDef[propertyDefinitions.size()]);
    }

    /**
     * Validates the provided IIpsArtefactBuilderSetConfig against this definition. Especially the
     * properties of the configuration are checked for their existence and the correct value.
     */
    public MessageList validateIpsArtefactBuilderSetConfig(IIpsProject ipsProject, IIpsArtefactBuilderSetConfigModel builderSetConfig) {
        MessageList msgList = new MessageList();
        
        String[] names = builderSetConfig.getPropertyNames();
        for (int i = 0; i < names.length; i++) {
            Message msg = validateIpsBuilderSetPropertyValue(ipsProject, names[i], builderSetConfig.getPropertyValue(names[i]));
            if(msg != null){
                msgList.add(msg);
            }
        }
        return msgList;
    }

    /**
     * Validates the property value of the property of an IpsArtefactBuilderSetConfig specified by the propertyName. It returns <code>null</code>
     * if validation is correct otherwise a {@link Message} object is returned.
     */
    //TODO translate messages
    public Message validateIpsBuilderSetPropertyValue(IIpsProject ipsProject, String propertyName, String propertyValue) {
        
        IIpsBuilderSetPropertyDef propertyDef = (IIpsBuilderSetPropertyDef)propertyDefinitions.get(propertyName);
        if(propertyDef == null){
            String text = "The builder set " + builderSetId + " doesn't support the property " + propertyName;
            return new Message(MSG_CODE_PROPERTY_NOT_SUPPORTED, text, Message.ERROR);
        }
        Message msg = propertyDef.validateValue(propertyValue);
        if(msg != null){
            return msg;
        }
        String disableValue = propertyDef.getDisableValue(ipsProject);
        if (!propertyDef.isAvailable(ipsProject) && !((disableValue == null && propertyValue == null) || disableValue.equals(propertyValue))) {
            return new Message(MSG_CODE_PROPERTY_NO_JDK_COMPLIANCE,
                    "This property is not in accordance with the JDK compliance level of this java project.",
                    Message.ERROR);
        }
        return null;
    }
    
    private final static Map retrieveBuilderSetProperties(IExtensionRegistry registry, IExtension extension, String builderSetId, IIpsModel ipsModel, IConfigurationElement element, ILog logger){
        IConfigurationElement[] builderSetPropertyDefElements = element.getChildren("builderSetPropertyDef");
        HashMap builderSetPropertyDefs = new HashMap();
        for (int j = 0; j < builderSetPropertyDefElements.length; j++) {
            IIpsBuilderSetPropertyDef propertyDef = IpsBuilderSetPropertyDef
            .loadExtensions(builderSetPropertyDefElements[j], registry, builderSetId, logger, ipsModel);
            if(propertyDef != null){
                builderSetPropertyDefs.put(propertyDef.getName(), propertyDef);
            }
        }
        return builderSetPropertyDefs;
    }
    
    /**
     * Loads IpsArtefactBuilderSetInfos from the extension registry.
     */
    public final static void loadExtensions(IExtensionRegistry registry, ILog logger, List builderSetInfoList, IIpsModel ipsModel) {
        IExtensionPoint point = registry.getExtensionPoint(IpsPlugin.PLUGIN_ID, "artefactbuilderset"); //$NON-NLS-1$
        IExtension[] extensions = point.getExtensions();

        for (int i = 0; i < extensions.length; i++) {
            IExtension extension = extensions[i];
            IConfigurationElement[] configElements = extension.getConfigurationElements();
            if (configElements.length > 0) {
                IConfigurationElement element = configElements[0];
                if (element.getName().equals("builderSet")) { //$NON-NLS-1$
                    if (StringUtils.isEmpty(extension.getUniqueIdentifier())) {
                        logger.log(new IpsStatus("The identifier of the IpsArtefactBuilderSet extension is empty")); //$NON-NLS-1$
                        continue;
                    }
                    String builderSetClassName = element.getAttribute("class"); //$NON-NLS-1$
                    if (StringUtils.isEmpty(builderSetClassName)) {
                        logger.log(new IpsStatus(
                                "The class attribute of the IpsArtefactBuilderSet extension with the extension id " + //$NON-NLS-1$
                                        extension.getUniqueIdentifier() + " is not specified."));//$NON-NLS-1$
                        continue;
                    }

                    Map builderSetPropertyDefs = retrieveBuilderSetProperties(registry, extension, extension
                            .getUniqueIdentifier(), ipsModel, element, logger);
                    builderSetInfoList.add(new IpsArtefactBuilderSetInfo(extension.getNamespaceIdentifier(), builderSetClassName, extension
                            .getUniqueIdentifier(), extension.getLabel(), builderSetPropertyDefs));
                }
            }
        }
    }
}
