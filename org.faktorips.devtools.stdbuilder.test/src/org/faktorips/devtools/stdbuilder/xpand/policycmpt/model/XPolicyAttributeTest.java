/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.xpand.policycmpt.model;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XPolicyAttributeTest {

    @Mock
    private IPolicyCmptTypeAttribute attribute;

    @Mock
    private GeneratorModelContext modelContext;

    @Mock
    private ModelService modelService;

    private XPolicyAttribute xPolicyAttribute;

    private XPolicyCmptClass policyClass;

    @Before
    public void createXPolicyAttribute() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(attribute.getIpsProject()).thenReturn(ipsProject);
        DatatypeHelper datatypeHelper = mock(DatatypeHelper.class);
        when(ipsProject.findDatatypeHelper(anyString())).thenReturn(datatypeHelper);
        when(datatypeHelper.getDatatype()).thenReturn(ValueDatatype.BOOLEAN);

        IPolicyCmptType polType = mock(IPolicyCmptType.class);
        when(attribute.getPolicyCmptType()).thenReturn(polType);

        policyClass = mock(XPolicyCmptClass.class);
        when(modelService.getModelNode(polType, XPolicyCmptClass.class, modelContext)).thenReturn(policyClass);

        xPolicyAttribute = new XPolicyAttribute(attribute, modelContext, modelService);
    }

    @Test
    public void productGenerationGetterName() throws Exception {
        xPolicyAttribute.getProductGenerationClassName();
        verify(policyClass).getProductGenerationClassName();
    }

    @Test
    public void testIsGenerateAllowedValuesFor() throws Exception {
        xPolicyAttribute = spy(xPolicyAttribute);

        doReturn(true).when(xPolicyAttribute).isValueSetUnrestricted();
        doReturn(false).when(xPolicyAttribute).isProductRelevant();

        boolean generatedMethod = xPolicyAttribute.isGenerateGetAllowedValuesFor();
        assertEquals(false, generatedMethod);

        verify(xPolicyAttribute, never()).isValueSetEnum();
    }

    @Test
    public void testIsGenerateAllowedValuesForContentSeperatedEnum() throws Exception {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(xPolicyAttribute).isValueSetUnrestricted();
        doReturn(true).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isChangeable();

        doReturn(true).when(xPolicyAttribute).isValueSetEnum();
        doReturn(true).when(xPolicyAttribute).isDatatypeContentSeparatedEnum();

        boolean generatedMethod = xPolicyAttribute.isGenerateGetAllowedValuesFor();
        assertEquals(false, generatedMethod);
    }

    @Test
    public void testIsGenerateAllowedValuesProductRelevantAndUnrestricted() throws Exception {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(true).when(xPolicyAttribute).isValueSetUnrestricted();
        doReturn(true).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isChangeable();

        doReturn(false).when(xPolicyAttribute).isValueSetEnum();
        doReturn(false).when(xPolicyAttribute).isDatatypeContentSeparatedEnum();

        boolean generatedMethod = xPolicyAttribute.isGenerateGetAllowedValuesFor();
        assertEquals(true, generatedMethod);
    }

    @Test
    public void testIsGenerateAllowedValuesProductRelevantAndRestricted() throws Exception {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(xPolicyAttribute).isValueSetUnrestricted();
        doReturn(true).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isChangeable();

        doReturn(false).when(xPolicyAttribute).isValueSetEnum();
        doReturn(false).when(xPolicyAttribute).isDatatypeContentSeparatedEnum();

        boolean generatedMethod = xPolicyAttribute.isGenerateGetAllowedValuesFor();
        assertEquals(true, generatedMethod);
        verify(xPolicyAttribute, never()).isDatatypeContentSeparatedEnum();
    }

    @Test
    public void testIsGenerateAllowedValuesProductRelevantAndEnumButNotContentSeperated() throws Exception {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(xPolicyAttribute).isValueSetUnrestricted();
        doReturn(true).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isChangeable();

        doReturn(true).when(xPolicyAttribute).isValueSetEnum();
        doReturn(false).when(xPolicyAttribute).isDatatypeContentSeparatedEnum();

        boolean generatedMethod = xPolicyAttribute.isGenerateGetAllowedValuesFor();
        assertEquals(true, generatedMethod);
    }

    @Test
    public void testIsGenerateAllowedValuesDerived() throws Exception {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(true).when(xPolicyAttribute).isDerived();
        boolean generatedMethod = xPolicyAttribute.isGenerateGetAllowedValuesFor();
        assertEquals(false, generatedMethod);
        verify(xPolicyAttribute, never()).isValueSetEnum();
        verify(xPolicyAttribute, never()).isValueSetUnrestricted();
        verify(xPolicyAttribute, never()).isProductRelevant();
        verify(xPolicyAttribute, never()).isDatatypeContentSeparatedEnum();
    }

}
