/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.builder.naming.JavaClassNaming;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IJavaNamingConvention;
import org.junit.Before;

/**
 * Abstract base class that can be used by tests for the standard builder.
 * 
 * @author Alexander Weickmann
 */
public abstract class AbstractStdBuilderTest extends AbstractIpsPluginTest {

    protected IIpsProject ipsProject;

    /** A list that can be used by test cases to store the list of Java elements generated. */
    protected List<IJavaElement> generatedJavaElements;

    protected StandardBuilderSet builderSet;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject();
        generatedJavaElements = new ArrayList<IJavaElement>();
        builderSet = (StandardBuilderSet)ipsProject.getIpsArtefactBuilderSet();
    }

    @Override
    protected void setTestArtefactBuilderSet(IIpsProjectProperties properties, IIpsProject project)
            throws CoreException {

        properties.setBuilderSetId(StandardBuilderSet.ID);
    }

    /**
     * Returns the generated Java implementation class for the given {@link IIpsObject}.
     */
    protected final IType getGeneratedJavaClass(IIpsObject ipsObject, boolean derivedSource, String conceptName) {

        String javaTypeName = ipsObject.getIpsProject().getJavaNamingConvention()
                .getImplementationClassName(conceptName);
        return getGeneratedJavaType(ipsObject, false, derivedSource, javaTypeName);
    }

    /**
     * Returns the generated Java enum for the given {@link IIpsObject}.
     */
    protected final IType getGeneratedJavaEnum(IIpsObject ipsObject, boolean derivedSource, String conceptName) {

        return getGeneratedJavaType(ipsObject, true, derivedSource, conceptName);
    }

    /**
     * Returns the generated published Java interface for the given {@link IIpsObject}.
     */
    protected final IType getGeneratedJavaInterface(IIpsObject ipsObject, boolean derivedSource, String conceptName) {

        String javaTypeName = ipsObject.getIpsProject().getJavaNamingConvention()
                .getPublishedInterfaceName(conceptName);
        return getGeneratedJavaType(ipsObject, true, derivedSource, javaTypeName);
    }

    private final IType getGeneratedJavaType(IIpsObject ipsObject,
            boolean published,
            boolean derivedSource,
            String javaTypeName) {

        try {
            IFolder outputFolder = ipsObject.getIpsPackageFragment().getRoot().getArtefactDestination(derivedSource);
            IPackageFragmentRoot javaRoot = ipsObject.getIpsProject().getJavaProject()
                    .getPackageFragmentRoot(outputFolder);
            String packageName = builderSet.getPackageName(ipsObject.getIpsSrcFile(), published, !derivedSource);
            IPackageFragment javaPackage = javaRoot.getPackageFragment(packageName);
            ICompilationUnit javaCompilationUnit = javaPackage.getCompilationUnit(javaTypeName
                    + JavaClassNaming.JAVA_EXTENSION);
            return javaCompilationUnit.getType(javaTypeName);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Expects a specific {@link IType} to be added to the list of generated Java elements.
     * 
     * @param type The {@link IType} that is expected to be contained in the generated Java elements
     */
    protected final void expectType(IType type) {
        assertTrue(generatedJavaElements.contains(type));
    }

    /**
     * Expects a specific {@link IField} to be added to the list of generated Java elements.
     * 
     * @param index The position at which the field is expected in the list of generated Java
     *            elements
     * @param javaType The Java type the expected field belongs to
     * @param fieldName The name of the expected field
     */
    protected final void expectField(int index, IType javaType, String fieldName) {
        IField field = javaType.getField(fieldName);
        assertEquals(field, generatedJavaElements.get(index));
    }

    /**
     * Expects a specific {@IMethod} to be added to the list of generated Java elements.
     * 
     * @param javaType The Java type the expected method belongs to
     * @param methodName The name of the expected method
     * @param parameterTypeSignatures The parameter type signatures of the expected method (use the
     *            <tt>xxxParam(...)</tt> methods offered by this class)
     */
    protected final void expectMethod(IType javaType, String methodName, String... parameterTypeSignatures) {
        IMethod method = javaType.getMethod(methodName, parameterTypeSignatures);
        assertTrue(generatedJavaElements.contains(method));
    }

    protected final IJavaNamingConvention getJavaNamingConvention() {
        return ipsProject.getJavaNamingConvention();
    }

}
