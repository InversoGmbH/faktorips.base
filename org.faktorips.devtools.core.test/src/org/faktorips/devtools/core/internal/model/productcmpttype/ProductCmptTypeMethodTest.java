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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class ProductCmptTypeMethodTest extends AbstractIpsPluginTest {

    private IProductCmptType productCmptType;

    private IProductCmptTypeMethod method;

    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
        productCmptType = newProductCmptType(ipsProject, "Type");
        method = productCmptType.newProductCmptTypeMethod();
    }

    @Test
    public void testValidate_FormulaMustntBeAbstract() throws CoreException {
        method.setFormulaSignatureDefinition(true);
        method.setAbstract(true);
        MessageList result = method.validate(method.getIpsProject());
        assertNotNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_MUSTNT_BE_ABSTRACT));

        method.setAbstract(false);
        result = method.validate(method.getIpsProject());
        assertNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_MUSTNT_BE_ABSTRACT));

        method.setFormulaSignatureDefinition(false);
        method.setAbstract(true);
        result = method.validate(method.getIpsProject());
        assertNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_MUSTNT_BE_ABSTRACT));

        method.setAbstract(false);
        result = method.validate(method.getIpsProject());
        assertNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_MUSTNT_BE_ABSTRACT));
    }

    @Test
    public void testValidate_FormulaNameIsMissing() throws CoreException {
        method.setFormulaSignatureDefinition(false);
        method.setFormulaName("someName");
        MessageList result = method.validate(method.getIpsProject());
        assertNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_NAME_IS_EMPTY));

        method.setFormulaSignatureDefinition(true);
        result = method.validate(method.getIpsProject());
        assertNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_NAME_IS_EMPTY));

        method.setFormulaName("");
        result = method.validate(method.getIpsProject());
        assertNotNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_NAME_IS_EMPTY));

        method.setFormulaSignatureDefinition(false);
        result = method.validate(method.getIpsProject());
        assertNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_NAME_IS_EMPTY));
    }

    @Test
    public void testValidate_DatatypeMustBeAValueDatatypeForFormulaSignature() throws CoreException {
        method.setDatatype("void");
        method.setFormulaSignatureDefinition(false);
        MessageList result = method.validate(method.getIpsProject());
        assertNull(result
                .getMessageByCode(IProductCmptTypeMethod.MSGCODE_DATATYPE_MUST_BE_A_VALUEDATATYPE_FOR_FORMULA_SIGNATURES));
        method.setFormulaSignatureDefinition(true);
        result = method.validate(method.getIpsProject());
        assertNotNull(result
                .getMessageByCode(IProductCmptTypeMethod.MSGCODE_DATATYPE_MUST_BE_A_VALUEDATATYPE_FOR_FORMULA_SIGNATURES));

        method.setFormulaSignatureDefinition(false);
        method.setDatatype(productCmptType.getQualifiedName());
        result = method.validate(method.getIpsProject());
        assertNull(result
                .getMessageByCode(IProductCmptTypeMethod.MSGCODE_DATATYPE_MUST_BE_A_VALUEDATATYPE_FOR_FORMULA_SIGNATURES));
        method.setFormulaSignatureDefinition(true);
        result = method.validate(method.getIpsProject());
        assertNotNull(result
                .getMessageByCode(IProductCmptTypeMethod.MSGCODE_DATATYPE_MUST_BE_A_VALUEDATATYPE_FOR_FORMULA_SIGNATURES));

        method.setDatatype("Integer");
        result = method.validate(method.getIpsProject());
        assertNull(result
                .getMessageByCode(IProductCmptTypeMethod.MSGCODE_DATATYPE_MUST_BE_A_VALUEDATATYPE_FOR_FORMULA_SIGNATURES));
    }

    @Test
    public void testInitFromXml() {
        Element docElement = getTestDocument().getDocumentElement();
        method.setFormulaSignatureDefinition(false);
        method.initFromXml(XmlUtil.getElement(docElement, "Method", 0));
        assertTrue(method.isFormulaSignatureDefinition());
        assertEquals("Premium", method.getFormulaName());
        assertEquals("42", method.getId());
        assertEquals("calcPremium", method.getName());
        assertEquals("Money", method.getDatatype());
        assertEquals(Modifier.PUBLIC, method.getModifier());
        assertTrue(method.isAbstract());
        assertTrue(method.isOverloadsFormula());
    }

    @Test
    public void testToXmlDocument() {
        method = productCmptType.newProductCmptTypeMethod(); // => id=1, because it's the second method
        method.setName("getAge");
        method.setModifier(Modifier.PUBLIC);
        method.setDatatype("Decimal");
        method.setFormulaSignatureDefinition(true);
        method.setAbstract(true);
        method.setFormulaName("Premium");
        IParameter param0 = method.newParameter();
        param0.setName("p0");
        param0.setDatatype("Decimal");
        IParameter param1 = method.newParameter();
        param1.setName("p1");
        param1.setDatatype("Money");
        method.setOverloadsFormula(true);
        method.setCategory("foo");

        Element element = method.toXml(newDocument());

        IProductCmptTypeMethod copy = productCmptType.newProductCmptTypeMethod();
        copy.initFromXml(element);
        assertTrue(copy.isFormulaSignatureDefinition());
        assertEquals("Premium", copy.getFormulaName());
        IParameter[] copyParams = copy.getParameters();
        assertEquals(method.getId(), copy.getId());
        assertEquals("getAge", copy.getName());
        assertEquals("Decimal", copy.getDatatype());
        assertEquals(Modifier.PUBLIC, copy.getModifier());
        assertTrue(copy.isAbstract());
        assertEquals(2, copyParams.length);
        assertEquals("p0", copyParams[0].getName());
        assertEquals("Decimal", copyParams[0].getDatatype());
        assertEquals("p1", copyParams[1].getName());
        assertEquals("Money", copyParams[1].getDatatype());
        assertTrue(copy.isOverloadsFormula());
        assertEquals("foo", copy.getCategory());
    }

    @Test
    public void testFindOverloadedFormulaMethod() throws CoreException {
        IProductCmptType aType = newProductCmptType(ipsProject, "AType");
        IProductCmptTypeMethod aMethod = aType.newProductCmptTypeMethod();
        aMethod.setName("calculate");
        aMethod.setDatatype(Datatype.STRING.toString());
        aMethod.setFormulaName("formula");
        aMethod.setFormulaSignatureDefinition(true);
        aMethod.setModifier(Modifier.PUBLIC);
        aMethod.newParameter(Datatype.STRING.toString(), "param1");
        aMethod.newParameter(Datatype.INTEGER.toString(), "param2");

        IProductCmptType bType = newProductCmptType(ipsProject, "BType");
        bType.setSupertype(aType.getQualifiedName());
        IProductCmptTypeMethod bMethod = bType.newProductCmptTypeMethod();
        bMethod.setName("calculate");
        bMethod.setDatatype(Datatype.STRING.toString());
        bMethod.setFormulaName("formula");
        bMethod.setFormulaSignatureDefinition(true);
        bMethod.setModifier(Modifier.PUBLIC);
        bMethod.newParameter(Datatype.STRING.toString(), "param1");
        bMethod.newParameter(Datatype.INTEGER.toString(), "param2");

        IProductCmptTypeMethod overloadedFormulaMethod = bMethod.findOverloadedFormulaMethod(ipsProject);
        assertNull(overloadedFormulaMethod);

        bMethod.setOverloadsFormula(true);
        overloadedFormulaMethod = bMethod.findOverloadedFormulaMethod(ipsProject);
        assertEquals(aMethod, overloadedFormulaMethod);

        bType.setSupertype(null);
        overloadedFormulaMethod = bMethod.findOverloadedFormulaMethod(ipsProject);
        assertNull(overloadedFormulaMethod);

        bType.setSupertype(aType.getQualifiedName());
        bMethod.setFormulaSignatureDefinition(false);
        assertNull(overloadedFormulaMethod);
    }

    @Test
    public void testOverloadsFormula() throws CoreException {
        IProductCmptType bType = newProductCmptType(ipsProject, "BType");
        IProductCmptTypeMethod bMethod = bType.newProductCmptTypeMethod();
        bMethod.setName("calculate");
        bMethod.setDatatype(Datatype.STRING.toString());
        bMethod.setFormulaName("formula");
        bMethod.setFormulaSignatureDefinition(true);
        bMethod.setModifier(Modifier.PUBLIC);
        bMethod.newParameter(Datatype.STRING.toString(), "param1");
        bMethod.newParameter(Datatype.INTEGER.toString(), "param2");

        assertFalse(bMethod.isOverloadsFormula());

        bMethod.setOverloadsFormula(true);
        assertTrue(bMethod.isOverloadsFormula());
    }

    @Test
    public void testValidateOverLoadedFormulaSignatureNotInSupertypeHierarchy() throws CoreException {
        IProductCmptType aType = newProductCmptType(ipsProject, "AType");
        IProductCmptTypeMethod aMethod = aType.newProductCmptTypeMethod();
        aMethod.setName("calculate");
        aMethod.setDatatype(Datatype.STRING.toString());
        aMethod.setFormulaName("formula");
        aMethod.setFormulaSignatureDefinition(true);
        aMethod.setModifier(Modifier.PUBLIC);
        aMethod.newParameter(Datatype.STRING.toString(), "param1");
        aMethod.newParameter(Datatype.INTEGER.toString(), "param2");

        IProductCmptType bType = newProductCmptType(ipsProject, "BType");
        bType.setSupertype(aType.getQualifiedName());
        IProductCmptTypeMethod bMethod = bType.newProductCmptTypeMethod();
        bMethod.setName("calculate");
        bMethod.setDatatype(Datatype.STRING.toString());
        bMethod.setFormulaName("formula");
        bMethod.setFormulaSignatureDefinition(true);
        bMethod.setModifier(Modifier.PUBLIC);
        bMethod.newParameter(Datatype.STRING.toString(), "param1");
        bMethod.newParameter(Datatype.INTEGER.toString(), "param2");
        bMethod.setOverloadsFormula(true);

        MessageList msgList = bMethod.validate(ipsProject);
        Message msg = msgList
                .getMessageByCode(IProductCmptTypeMethod.MSGCODE_NO_FORMULA_WITH_SAME_NAME_IN_TYPE_HIERARCHY);
        assertNull(msg);

        aMethod.setFormulaName("formula2");
        msgList = bMethod.validate(ipsProject);
        msg = msgList.getMessageByCode(IProductCmptTypeMethod.MSGCODE_NO_FORMULA_WITH_SAME_NAME_IN_TYPE_HIERARCHY);
        assertNotNull(msg);
    }

    @Test
    public void testIsPolicyCmptTypeProperty() {
        assertFalse(method.isPolicyCmptTypeProperty());
    }

    @Test
    public void testIsPropertyFor() throws CoreException {
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "Product");
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();
        IPropertyValue propertyValue = generation.newFormula(method);

        assertTrue(method.isPropertyFor(propertyValue));
    }

}
