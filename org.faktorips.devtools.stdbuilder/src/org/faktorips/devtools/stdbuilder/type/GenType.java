/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.type;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.JavaGeneratorForIpsPart;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

/**
 * Abstract base class for the generators for <tt>IPolicyCmptType</tt> and <tt>IProductCmptType</tt>
 * .
 * 
 * @author Peter Erzberger
 */
public abstract class GenType extends JavaGeneratorForIpsPart {

    protected static final String[] EMPTY_STRING_ARRAY = new String[0];

    private final Map<IIpsObjectPart, GenTypePart> generatorsByPart;

    private final StandardBuilderSet builderSet;

    private final IType type;

    public GenType(IType type, StandardBuilderSet builderSet, LocalizedStringsSet localizedStringsSet) {
        super(type, localizedStringsSet);

        ArgumentCheck.notNull(type, this);
        ArgumentCheck.notNull(builderSet, this);

        this.type = type;
        this.builderSet = builderSet;
        generatorsByPart = new HashMap<IIpsObjectPart, GenTypePart>();
    }

    public IType getType() {
        return type;
    }

    public StandardBuilderSet getBuilderSet() {
        return builderSet;
    }

    public String getPackageName(boolean forInterface) {
        return getPackageName(type, builderSet, forInterface);
    }

    private static String getPackageName(IType type, StandardBuilderSet builderSet, boolean forInterface) {
        if (type != null) {
            try {
                if (forInterface) {
                    return builderSet.getPackageNameForMergablePublishedArtefacts(type.getIpsSrcFile());
                }
                return builderSet.getPackageNameForMergableInternalArtefacts(type.getIpsSrcFile());
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public String getQualifiedName(boolean forInterface) {
        return getQualifiedName(type, builderSet, getPackageName(forInterface), forInterface);
    }

    public static String getQualifiedName(IType type, StandardBuilderSet builderSet, boolean forInterface) {
        if (type != null) {
            return getQualifiedName(type, builderSet, getPackageName(type, builderSet, forInterface), forInterface);
        }
        return null;
    }

    private static String getQualifiedName(IType type,
            StandardBuilderSet builderSet,
            String packageName,
            boolean forInterface) {

        StringBuffer buf = new StringBuffer();
        buf.append(packageName);
        buf.append('.');
        buf.append(getUnqualifiedClassName(type, builderSet, forInterface));
        return buf.toString();
    }

    public static String getUnqualifiedClassName(IType type, StandardBuilderSet builderSet, boolean forInterface) {
        if (forInterface) {
            return builderSet.getJavaNamingConvention().getPublishedInterfaceName(type.getName());

        }
        return StringUtil.getFilenameWithoutExtension(type.getName());
    }

    /**
     * Returns the unqualified name for Java class generated by this builder for the given ips
     * source file.
     * 
     * TODO description does not match signature
     * 
     * @param ipsSrcFile the ips source file
     * @return the qualified class name
     * @throws CoreException is delegated from calls to other methods
     */
    public String getUnqualifiedClassName(boolean forInterface) throws CoreException {
        return getUnqualifiedClassName(type, builderSet, forInterface);
    }

    /**
     * Returns the abbreviation for the generation (changes over time) concept.
     * 
     * @see org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention
     * @see org.faktorips.devtools.core.builder.AbstractTypeBuilder#getAbbreviationForGenerationConcept
     */
    public String getAbbreviationForGenerationConcept() {
        return getChangesInTimeNamingConvention().getGenerationConceptNameAbbreviation(
                getLanguageUsedInGeneratedSourceCode());
    }

    /**
     * Returns the naming convention for product changes over time.
     * 
     * @see org.faktorips.devtools.core.builder.JavaSourceFileBuilder#getChangesInTimeNamingConvention
     */
    public IChangesOverTimeNamingConvention getChangesInTimeNamingConvention() {
        return builderSet.getIpsProject().getChangesInTimeNamingConventionForGeneratedCode();
    }

    /**
     * Returns the language in that variables, methods are named and and Java docs are written in.
     * 
     * @see IIpsArtefactBuilderSet#getLanguageUsedInGeneratedSourceCode()
     */
    @Override
    public Locale getLanguageUsedInGeneratedSourceCode() {
        return builderSet.getLanguageUsedInGeneratedSourceCode();
    }

    /**
     * Returns the name (singular form) for the generation (changes over time) concept.
     * 
     * @see org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention
     * @see org.faktorips.devtools.core.builder.AbstractPcTypeBuilder#getNameForGenerationConcept
     */
    public String getNameForGenerationConcept() {
        return getChangesInTimeNamingConvention().getGenerationConceptNameSingular(
                getLanguageUsedInGeneratedSourceCode());
    }

    /**
     * Returns the getter method to access a property/attribute value.
     * 
     * @since 2.0
     */
    @Override
    public String getMethodNameGetPropertyValue(String propName, Datatype datatype) {
        return getJavaNamingConvention().getGetterMethodName(propName, datatype);
    }

    protected final Map<IIpsObjectPart, GenTypePart> getGeneratorsByPart() {
        return generatorsByPart;
    }

    @Override
    public void getGeneratedJavaElementsForImplementation(List<IJavaElement> javaElements,
            org.eclipse.jdt.core.IType generatedJavaType,
            IIpsElement ipsElement) {

        getGeneratedJavaElements(javaElements, generatedJavaType, ipsElement, false);
    }

    @Override
    public void getGeneratedJavaElementsForPublishedInterface(List<IJavaElement> javaElements,
            org.eclipse.jdt.core.IType generatedJavaType,
            IIpsElement ipsElement) {

        getGeneratedJavaElements(javaElements, generatedJavaType, ipsElement, true);
    }

    /**
     * Adds the generated <tt>IJavaElement</tt>s for the given <tt>IIpsElement</tt> to the provided
     * list.
     */
    private void getGeneratedJavaElements(List<IJavaElement> javaElements,
            org.eclipse.jdt.core.IType generatedJavaType,
            IIpsElement ipsElement,
            boolean forInterface) {

        if (ipsElement instanceof IType) {
            getGeneratedJavaElementsForType(javaElements, generatedJavaType, forInterface);

        } else if (ipsElement instanceof IIpsObjectPart) {
            GenTypePart genTypePart = getGenTypePart(ipsElement);
            if (genTypePart != null) {
                if (forInterface) {
                    genTypePart.getGeneratedJavaElementsForPublishedInterface(javaElements, generatedJavaType,
                            ipsElement);
                } else {
                    genTypePart.getGeneratedJavaElementsForImplementation(javaElements, generatedJavaType, ipsElement);
                }
            }
        }
    }

    private GenTypePart getGenTypePart(IIpsElement ipsElement) {
        return getGeneratorsByPart().get(ipsElement);
    }

    /**
     * Subclass implementation that must add the <tt>IJavaElement</tt>s that are generated for the
     * IPS type to the provided list.
     * 
     * @param javaElements The list to add the generated <tt>IJavaElement</tt>s to.
     * @param generatedJavaType The Java type that the calling builder is generating.
     * @param forInterface Flag indicating whether the generated <tt>IJavaElement</tt>s for the
     *            published interface instead of the implementation are requested.
     */
    protected abstract void getGeneratedJavaElementsForType(List<IJavaElement> javaElements,
            org.eclipse.jdt.core.IType generatedJavaType,
            boolean forInterface);

}