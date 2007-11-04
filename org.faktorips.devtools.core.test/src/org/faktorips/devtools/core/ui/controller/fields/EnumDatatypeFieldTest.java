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

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;

/**
 * 
 * @author Jan Ortmann
 */
public class EnumDatatypeFieldTest extends AbstractIpsPluginTest {

    private EnumDatatype datatype;
    private IEnumValueSet valueSet;
    private EnumDatatypeField field;
    private String nullRepresentation;
    private Shell shell;
    
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject();
        IPolicyCmptType type = newPolicyCmptType(project, "Policy");
        IPolicyCmptTypeAttribute a = type.newPolicyCmptTypeAttribute();
        datatype = new PaymentMode();
        a.setValueSetType(ValueSetType.ENUM);
        valueSet = (IEnumValueSet)a.getValueSet();
        valueSet.addValue(PaymentMode.ANNUAL_ID);
        valueSet.addValue(PaymentMode.MONTHLY_ID);
        
        nullRepresentation = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        shell = PlatformUI.getWorkbench().getDisplay().getShells()[0];
        assertNotNull(shell);
    }
    
    public void testWithDisplayTypeName() {
        IpsPlugin.getDefault().getIpsPreferences().setEnumTypeDisplay(EnumTypeDisplay.NAME);
        Combo c = new Combo(shell, SWT.READ_ONLY);
        field = new EnumDatatypeField(c, datatype);
        
        String[] items = field.getCombo().getItems();
        assertEquals(3, items.length);
        assertEquals(nullRepresentation, items[0]);
        assertEquals(PaymentMode.ANNUAL_NAME, items[1]);
        assertEquals(PaymentMode.MONTHLY_NAME, items[2]);
        
        field.setValue(PaymentMode.ANNUAL_ID);
        assertEquals(PaymentMode.ANNUAL_ID, field.getValue());
        assertEquals(PaymentMode.ANNUAL_NAME, field.getText());
        
        field.setValue(nullRepresentation);
        assertEquals(null, field.getValue());
        assertEquals(nullRepresentation, field.getText());
        
        field.setValue("unknownValue");
        items = c.getItems();
        assertEquals(4, items.length);
        assertEquals(nullRepresentation, items[0]);
        assertEquals(PaymentMode.ANNUAL_NAME, items[1]);
        assertEquals(PaymentMode.MONTHLY_NAME, items[2]);
        assertEquals("unknownValue", items[3]);
        
        field.setValue(PaymentMode.ANNUAL_ID);
        field.setValue("unknownValue");
        items = c.getItems();
        assertEquals(4, items.length);
    }
    
    public void testWithDisplayTypeId() {
        IpsPlugin.getDefault().getIpsPreferences().setEnumTypeDisplay(EnumTypeDisplay.ID);
        Combo c = new Combo(shell, SWT.READ_ONLY);
        field = new EnumDatatypeField(c, datatype);
        
        String[] items = field.getCombo().getItems();
        assertEquals(3, items.length);
        assertEquals(nullRepresentation, items[0]);
        assertEquals(PaymentMode.ANNUAL_ID, items[1]);
        assertEquals(PaymentMode.MONTHLY_ID, items[2]);
        
        field.setValue(PaymentMode.ANNUAL_ID);
        assertEquals(PaymentMode.ANNUAL_ID, field.getValue());
        assertEquals(PaymentMode.ANNUAL_ID, field.getText());
        
        field.setValue(nullRepresentation);
        assertEquals(null, field.getValue());
        assertEquals(nullRepresentation, field.getText());
        
        field.setValue("unknownValue");
        items = c.getItems();
        assertEquals(4, items.length);
        assertEquals(nullRepresentation, items[0]);
        assertEquals(PaymentMode.ANNUAL_ID, items[1]);
        assertEquals(PaymentMode.MONTHLY_ID, items[2]);
        assertEquals("unknownValue", items[3]);
        
        field.setValue(PaymentMode.ANNUAL_ID);
        field.setValue("unknownValue");
        items = c.getItems();
        assertEquals(4, items.length);
    }
    
    public void testWithDisplayTypeNameAndId() {
        IpsPreferences prefs = IpsPlugin.getDefault().getIpsPreferences();
        prefs.setEnumTypeDisplay(EnumTypeDisplay.NAME_AND_ID);
        Combo c = new Combo(shell, SWT.READ_ONLY);
        field = new EnumDatatypeField(c, datatype);
        
        String annualNameAndId = prefs.formatValue(datatype, PaymentMode.ANNUAL_ID);
        String monthlyNameAndId = prefs.formatValue(datatype, PaymentMode.MONTHLY_ID);
        
        String[] items = field.getCombo().getItems();
        assertEquals(3, items.length);
        assertEquals(nullRepresentation, items[0]);
        assertEquals(annualNameAndId, items[1]);
        assertEquals(monthlyNameAndId, items[2]);
        
        field.setValue(PaymentMode.ANNUAL_ID);
        assertEquals(PaymentMode.ANNUAL_ID, field.getValue());
        assertEquals(annualNameAndId, field.getText());
        
        field.setValue(nullRepresentation);
        assertEquals(null, field.getValue());
        assertEquals(nullRepresentation, field.getText());
        
        field.setValue("unknownValue");
        items = c.getItems();
        assertEquals(4, items.length);
        assertEquals(nullRepresentation, items[0]);
        assertEquals(annualNameAndId, items[1]);
        assertEquals(monthlyNameAndId, items[2]);
        assertEquals("unknownValue", items[3]);
        
        field.setValue(PaymentMode.ANNUAL_ID);
        field.setValue("unknownValue");
        items = c.getItems();
        assertEquals(4, items.length);
    }
}
