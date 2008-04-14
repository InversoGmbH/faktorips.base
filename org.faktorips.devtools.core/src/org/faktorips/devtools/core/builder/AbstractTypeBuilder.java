/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere. Alle Rechte vorbehalten. Dieses Programm und
 * alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, etc.) dürfen nur unter
 * den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung
 * Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann. Mitwirkende: Faktor Zehn GmbH -
 * initial API and implementation
 **************************************************************************************************/

package org.faktorips.devtools.core.builder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Base class for IPS-Type builders.
 * 
 * @author Peter Erzberger
 */
public abstract class AbstractTypeBuilder extends DefaultJavaSourceFileBuilder {

    public AbstractTypeBuilder(IIpsArtefactBuilderSet builderSet, String kindId, LocalizedStringsSet localizedStringsSet) {
        super(builderSet, kindId, localizedStringsSet);
    }

    protected final void generateCodeForJavatype() throws CoreException {
        TypeSection mainSection = getMainTypeSection();
        mainSection.setClassModifier(getClassModifier());
        mainSection.setSuperClass(getSuperclass());
        mainSection.setExtendedInterfaces(getExtendedInterfaces());
        mainSection.setUnqualifiedName(getUnqualifiedClassName());
        mainSection.setClass(!generatesInterface());

        generateCodeForProductCmptTypeAttributes(mainSection);
        generateCodeForPolicyCmptTypeAttributes(mainSection);
        generateCodeForAssociations(mainSection.getMemberVarBuilder(), mainSection.getMethodBuilder());
        generateCodeForMethodsDefinedInModel(mainSection.getMethodBuilder());
        generateConstructors(mainSection.getConstructorBuilder());
        generateTypeJavadoc(mainSection.getJavaDocForTypeBuilder());
        generateConstants(mainSection.getConstantBuilder());
        generateOtherCode(mainSection.getConstantBuilder(), mainSection.getMemberVarBuilder(), mainSection
                .getMethodBuilder());
        generateCodeForJavatype(mainSection);
    }

    /**
     * Returns the abbreviation for the generation (changes over time) concept.
     * 
     * @param element An ips element needed to access the ipsproject where the neccessary configuration
     * information is stored.
     * 
     * @see org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention
     */
    public String getAbbreviationForGenerationConcept(IIpsElement element) {
        return getChangesInTimeNamingConvention(element).
            getGenerationConceptNameAbbreviation(getLanguageUsedInGeneratedSourceCode(element));
    }
    
    protected abstract void generateCodeForPolicyCmptTypeAttributes(TypeSection typeSection) throws CoreException;
    
    /**
     * Template method to extend the generateCodeForJavatype() method.
     */
    protected void generateCodeForJavatype(TypeSection mainSection) throws CoreException {
    }

    /**
     * A hook to generate code that is not based on attributes, associations, rules and
     * methods.
     */
    protected abstract void generateOtherCode(
            JavaCodeFragmentBuilder constantsBuilder,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;


    /**
     * Constructors are supposed to be generated within this method. Especially the generated
     * code should be passed to the provided builder.
     * 
     * @throws CoreException exceptions during generation time can be wrapped into CoreExceptions and 
     *  propagated by this method
     */
    protected abstract void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException;

    /**
     * Generates the sourcecode for all methods defined in the policy component type.
     * 
     * @throws CoreException
     */
    protected final void generateCodeForMethodsDefinedInModel(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        IMethod[] methods = ((IType)getIpsObject()).getMethods();
        for (int i = 0; i < methods.length; i++) {
            IMethod method = methods[i];
            if (!method.validate(getIpsProject()).containsErrorMsg()) {
                generateCodeForMethodDefinedInModel(method, methodsBuilder);
            }
        }
    }

    /**
     * Generates the sourcecode for the indicated method.
     */
    protected abstract void generateCodeForMethodDefinedInModel(
            IMethod method,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;
    
    
    protected abstract void generateCodeForAssociations(JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    /**
     * Returns the qualified name of the superclass or <code>null</code> if the class being
     * generated is not derived from a class or is an interface.
     */
    protected abstract String getSuperclass() throws CoreException;

    /**
     * Subclasses of this builder can generate inner classes within this method. Inner classes are
     * created by calling the <code>createInnerClassSection()</code> method.
     * 
     * @throws CoreException exceptions during generation time can be wrapped into CoreExceptions
     *             and propagated by this method
     */
    protected void generateInnerClasses() throws CoreException {

    }

    /**
     * Constants are supposed to be generated within this method. Especially the generated code
     * should be passed to the provided builder.
     * 
     * @throws CoreException exceptions during generation time can be wrapped into CoreExceptions
     *             and propagated by this method
     */
    protected void generateConstants(JavaCodeFragmentBuilder builder) throws CoreException {
    }

    /**
     * Generates the Javadoc for the Java class or interface.
     * 
     * @param builder The builder to use to generate the Javadoc via it's javadoc method.
     */
    protected abstract void generateTypeJavadoc(JavaCodeFragmentBuilder builder) throws CoreException;

    /**
     * Returns true if an interface is generated, false if a class is generated.
     */
    protected abstract boolean generatesInterface();

    /**
     * Returns the class modifier.
     * 
     * @see java.lang.reflect.Modifier
     */
    protected int getClassModifier() throws CoreException {
        return ((IType)getIpsObject()).isAbstract() ? java.lang.reflect.Modifier.PUBLIC
                | java.lang.reflect.Modifier.ABSTRACT : java.lang.reflect.Modifier.PUBLIC;
    }

    /**
     * Returns the qualified name of the interfaces the generated class or interface extends.
     * Returns an empty array if no interfaces are extended
     */
    protected abstract String[] getExtendedInterfaces() throws CoreException;

    protected abstract IProductCmptType getProductCmptType() throws CoreException;
    
    /*
     * Generates the code for all product component type attributes.
     */
    private void generateCodeForProductCmptTypeAttributes(TypeSection typeSection) throws CoreException {
        
        IProductCmptType productCmptType = getProductCmptType();
        if (productCmptType==null) {
            return;
        }
        IProductCmptTypeAttribute[] attributes = productCmptType.getProductCmptTypeAttributes();
        for (int i = 0; i < attributes.length; i++) {
            IProductCmptTypeAttribute a = attributes[i];
            if (!a.isValid()) {
                continue;
            }
            try {
                Datatype datatype = a.findDatatype(getIpsProject());
                DatatypeHelper helper = getIpsProject().getDatatypeHelper(datatype);
                if (helper == null) {
                    throw new CoreException(new IpsStatus("No datatype helper found for datatype " + datatype));             //$NON-NLS-1$
                }
                generateCodeForProductCmptTypeAttribute(a, helper, typeSection.getConstantBuilder(), typeSection.getMemberVarBuilder(), typeSection.getMethodBuilder());
            } catch (Exception e) {
                throw new CoreException(new IpsStatus(IStatus.ERROR,
                        "Error building attribute " + attributes[i].getName() + " of " //$NON-NLS-1$ //$NON-NLS-2$
                                + getQualifiedClassName(getIpsObject().getIpsSrcFile()), e));
            }
        }
    }

    /**
     * This method is called from the abstract builder if the product component attribute is valid and
     * therefore code can be generated.
     * <p>
     * @param attribute The attribute sourcecode should be generated for.
     * @param datatypeHelper The datatype code generation helper for the attribute's datatype.
     * @param fieldsBuilder The code fragment builder to build the member variabales section.
     * @param methodsBuilder The code fragment builder to build the method section.
     */
    protected abstract void generateCodeForProductCmptTypeAttribute(
            org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute attribute, 
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder constantBuilder,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    protected abstract IPolicyCmptType getPcType() throws CoreException;
    

}
