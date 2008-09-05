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

package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.association.GenProdAssociation;
import org.faktorips.devtools.stdbuilder.productcmpttype.attribute.GenProdAttribute;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.util.LocalizedStringsSet;

/**
 * A builder that generates the Java source file (compilation unit) for the published interface of a
 * product component type.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptInterfaceBuilder extends BaseProductCmptTypeBuilder {

    public ProductCmptInterfaceBuilder(IIpsArtefactBuilderSet builderSet, String kindId) throws CoreException {
        super(builderSet, kindId, new LocalizedStringsSet(ProductCmptInterfaceBuilder.class));
        setMergeEnabled(true);
    }

    /**
     * {@inheritDoc}
     */
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        return getJavaNamingConvention().getPublishedInterfaceName(getConceptName(ipsSrcFile));
    }

    public String getConceptName(IIpsSrcFile ipsSrcFile) throws CoreException {
        return ipsSrcFile.getIpsObjecName();
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
        // nothing to do, generating an interface
    }

    /**
     * {@inheritDoc}
     */
    protected String getSuperclass() throws CoreException {
        return null; // no superclass, generating an interface
    }

    /**
     * {@inheritDoc}
     */
    protected String[] getExtendedInterfaces() throws CoreException {
        String javaSupertype = IProductComponent.class.getName();
        IProductCmptType supertype = (IProductCmptType)getProductCmptType().findSupertype(getIpsProject());
        if (supertype != null) {
            javaSupertype = getQualifiedClassName(supertype.getIpsSrcFile());
        }
        return new String[] { javaSupertype };
    }

    /**
     * {@inheritDoc}
     */
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("INTERFACE", getConceptName(getIpsSrcFile()), getIpsObject(), builder);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateOtherCode(JavaCodeFragmentBuilder constantsBuilder,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        generateMethodGetGeneration(methodsBuilder);
        // v2 - prufen, wann man die factory methode erzeugt. Signature koennte ja durchaus
        // generiert werden.
        // und nur die implementierung nicht!
        if (getPcType() != null && !getPcType().isAbstract()) {
            generateMethodCreatePolicyCmpt(methodsBuilder);
        }
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [javadoc]
     * public IProductGen getGeneration(Calendar effectiveDate);
     * </pre>
     */
    private void generateMethodGetGeneration(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        IChangesOverTimeNamingConvention convention = getChangesInTimeNamingConvention(getIpsSrcFile());
        String generationConceptName = convention
                .getGenerationConceptNameSingular(getLanguageUsedInGeneratedSourceCode());
        appendLocalizedJavaDoc("METHOD_GET_GENERATION", generationConceptName, getIpsObject(), methodsBuilder);
        ((StandardBuilderSet)getBuilderSet()).getGenerator(getProductCmptType()).generateSignatureGetGeneration(
                methodsBuilder);
        methodsBuilder.append(';');
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [javadoc]
     * public IPolicy createPolicy();
     * </pre>
     */
    private void generateMethodCreatePolicyCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        IPolicyCmptType policyCmptType = getPcType();
        GenPolicyCmptType genPcType = ((StandardBuilderSet)getBuilderSet()).getGenerator(policyCmptType);
        String policyCmptTypeName = genPcType.getPolicyCmptTypeName();
        appendLocalizedJavaDoc("METHOD_CREATE_POLICY_CMPT", new String[] { policyCmptTypeName }, getIpsObject(),
                methodsBuilder);
        genPcType.generateSignatureCreatePolicyCmpt(methodsBuilder);
        methodsBuilder.append(';');
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForPolicyCmptTypeAttribute(IPolicyCmptTypeAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForProductCmptTypeAttribute(org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder,
            JavaCodeFragmentBuilder constantBuilder) throws CoreException {

        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForNoneDerivedUnionAssociation(IProductCmptTypeAssociation association,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {

        // nothing to do

    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForDerivedUnionAssociationDefinition(IProductCmptTypeAssociation containerAssociation,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForDerivedUnionAssociationImplementation(IProductCmptTypeAssociation containerAssociation,
            List implementationAssociations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForTableUsage(ITableStructureUsage tsu,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForMethodDefinedInModel(IMethod method, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected GenProdAttribute createGenerator(IProductCmptTypeAttribute a, LocalizedStringsSet localizedStringsSet)
            throws CoreException {
        // return null, as this builder does not need code for product component type attributes
        return null;
    }

    /**
     * {@inheritDoc}
     */
    protected GenAttribute createGenerator(IPolicyCmptTypeAttribute a, LocalizedStringsSet localizedStringsSet)
            throws CoreException {
        // return null, as this builder does not need code for policy component type attributes
        return null;
    }

    /**
     * {@inheritDoc}
     */
    protected GenProdAssociation createGenerator(IProductCmptTypeAssociation a, LocalizedStringsSet stringsSet)
            throws CoreException {
        // return null, as this builder does not need code for product component type associations
        return null;
    }

}
