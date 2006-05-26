/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class DynamicValueDatatypeTest extends AbstractIpsPluginTest {

	private IIpsProject ipsProject;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		ipsProject = newIpsProject("TestProject");
	}

	public void testCreateFromXml_NoneEnumType() {
		Element docEl = getTestDocument().getDocumentElement();
		Element el = XmlUtil.getElement(docEl, "Datatype", 0);
		DynamicValueDatatype type = DynamicValueDatatype.createFromXml(ipsProject, el);
		assertTrue(type instanceof DynamicValueDatatype);
		assertEquals("foo.bar.MyDate", type.getAdaptedClassName());
		assertEquals("getMyDate", type.getValueOfMethodName());
		assertEquals("isMyDate", type.getIsParsableMethodName());
		assertEquals("isMyDate", type.getIsParsableMethodName());
	}

	public void testCreateFromXml_EnumType() {
		Element docEl = getTestDocument().getDocumentElement();
		Element el = XmlUtil.getElement(docEl, "Datatype", 1);
		DynamicEnumDatatype type = (DynamicEnumDatatype)DynamicValueDatatype.createFromXml(ipsProject, el);
		assertEquals("foo.bar.PaymentMode", type.getAdaptedClassName());
		assertEquals("getPaymentMode", type.getValueOfMethodName());
		assertEquals("isPaymentMode", type.getIsParsableMethodName());
		assertEquals("getId", type.getToStringMethodName());
		assertEquals("getName", type.getGetNameMethodName());
		assertEquals("getPaymentModes", type.getAllValuesMethodName());
		assertTrue(type.isSupportingNames());
		assertEquals("n", type.getSpecialNullValue()); 
	}
	
}
