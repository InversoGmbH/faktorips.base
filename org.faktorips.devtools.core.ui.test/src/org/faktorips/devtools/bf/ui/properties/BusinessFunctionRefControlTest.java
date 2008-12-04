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

package org.faktorips.devtools.bf.ui.properties;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;

public class BusinessFunctionRefControlTest extends AbstractIpsPluginTest {
    
    public void testGetIpsSrcFiles() throws Exception {
        UIToolkit toolkit = new UIToolkit(null);
        Display display = Display.getDefault();
        BusinessFunctionRefControl control = new BusinessFunctionRefControl(display.getActiveShell(), toolkit);
        assertEquals(0, control.getIpsSrcFiles().length);
        
        IIpsProject ipsProject = newIpsProject("TestProject");
        assertEquals(0, control.getIpsSrcFiles().length);

        IBusinessFunction bf1 = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf1");
        IBusinessFunction bf2 = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf2");
        IBusinessFunction bf3 = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf3");
        control.setIpsProject(ipsProject);
        assertEquals(3, control.getIpsSrcFiles().length);
        
        control.setCurrentBusinessFunction(bf1);
        assertEquals(2, control.getIpsSrcFiles().length);
        List<IIpsSrcFile> files = Arrays.asList(control.getIpsSrcFiles());
        assertTrue(files.contains(bf2.getIpsSrcFile()));
        assertTrue(files.contains(bf3.getIpsSrcFile()));
        assertFalse(files.contains(bf1.getIpsSrcFile()));
    }

}
