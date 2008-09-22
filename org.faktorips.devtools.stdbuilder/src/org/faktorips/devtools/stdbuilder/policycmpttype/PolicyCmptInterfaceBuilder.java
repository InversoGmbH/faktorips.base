/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.ICopySupport;
import org.faktorips.runtime.IDeltaSupport;
import org.faktorips.runtime.IDependantObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IVisitorSupport;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

public class PolicyCmptInterfaceBuilder extends BasePolicyCmptTypeBuilder {

    public PolicyCmptInterfaceBuilder(IIpsArtefactBuilderSet builderSet, String kindId) throws CoreException {
        super(builderSet, kindId, new LocalizedStringsSet(PolicyCmptInterfaceBuilder.class));
        setMergeEnabled(true);
    }

    /**
     * {@inheritDoc}
     */
    public PolicyCmptInterfaceBuilder getInterfaceBuilder() {
        return this;
    }

    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.POLICY_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
    }

    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        return getJavaNamingConvention().getPublishedInterfaceName(getPolicyCmptTypeName(ipsSrcFile));
    }

    public String getPolicyCmptTypeName(IIpsSrcFile ipsSrcFile) throws CoreException {
        String name = StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName());
        return StringUtils.capitalize(name);
    }

    /**
     * {@inheritDoc}
     */
    protected String getSuperclass() throws CoreException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    protected String[] getExtendedInterfaces() throws CoreException {
        List interfaces = new ArrayList();
        IPolicyCmptType type = getPcType();
        IPolicyCmptType supertype = (IPolicyCmptType)type.findSupertype(getIpsProject());
        if (supertype != null) {
            interfaces.add(getQualifiedClassName(supertype));
        } else {
            if (type.isConfigurableByProductCmptType()) {
                interfaces.add(IConfigurableModelObject.class.getName());
            } else {
                interfaces.add(IModelObject.class.getName());
            }
            if (isGenerateDeltaSupport()) {
                interfaces.add(IDeltaSupport.class.getName());
            }
            if (isGenerateCopySupport()) {
                interfaces.add(ICopySupport.class.getName());
            }
            if (isGenerateVisitorSupport()) {
                interfaces.add(IVisitorSupport.class.getName());
            }
            if (isGenerateChangeListenerSupport()) {
                String notificationSupportInterfaceName = ((StandardBuilderSet)getBuilderSet())
                        .getChangeListenerSupportBuilder().getNotificationSupportInterfaceName();
                if (notificationSupportInterfaceName != null) {
                    interfaces.add(notificationSupportInterfaceName);
                }
            }
        }
        if (isFirstDependantTypeInHierarchy(type)) {
            interfaces.add(IDependantObject.class.getName());
        }

        return (String[])interfaces.toArray(new String[interfaces.size()]);
    }

    /**
     * {@inheritDoc}
     */
    protected boolean generatesInterface() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
    }

    /**
     * {@inheritDoc}
     */
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) {
        appendLocalizedJavaDoc("INTERFACE", getIpsObject().getName(), getIpsObject().getDescription(), getIpsObject(),
                builder);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateOtherCode(JavaCodeFragmentBuilder constantsBuilder,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        if (getProductCmptType() != null) {
            if (hasValidProductCmptTypeName()) {
                generateMethodGetProductCmpt(methodsBuilder);
                generateMethodSetProductCmpt(methodsBuilder);
                getGenProductCmptType().generateMethodGetProductCmptGeneration(getIpsProject(), methodsBuilder);
            }
        }
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public IMotorProduct getMotorProduct();
     * </pre>
     */
    protected void generateMethodGetProductCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String[] replacements = new String[] { getProductCmptType().getName(), getPcType().getName() };
        appendLocalizedJavaDoc("METHOD_GET_PRODUCTCMPT", replacements, getPcType(), methodsBuilder);
        getGenProductCmptType().generateSignatureGetProductCmpt(methodsBuilder);
        methodsBuilder.append(";");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public void setMotorProduct(IMotorProduct motorProduct, boolean initPropertiesWithConfiguratedDefaults);
     * </pre>
     */
    protected void generateMethodSetProductCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String[] replacements = new String[] { getProductCmptType().getName(),
                StringUtils.uncapitalize(getProductCmptType().getName()), "initPropertiesWithConfiguratedDefaults" };
        appendLocalizedJavaDoc("METHOD_SET_PRODUCTCMPT", replacements, getProductCmptType(), methodsBuilder);
        getGenProductCmptType().generateSignatureSetProductCmpt(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForProductCmptTypeAttribute(IProductCmptTypeAttribute attribute,
            DatatypeHelper helper,
            JavaCodeFragmentBuilder constantBuilder,
            JavaCodeFragmentBuilder memberVarBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        // empty implementation
    }

    /**
     * Empty implementation.
     * 
     * overidden
     */
    protected void generateCodeForContainerAssociationImplementation(IPolicyCmptTypeAssociation containerAssociation,
            List subAssociations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
    }
}