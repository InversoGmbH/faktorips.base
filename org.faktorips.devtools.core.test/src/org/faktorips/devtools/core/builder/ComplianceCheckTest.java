/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.builder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * 
 * @author Daniel Hohenberger
 */
public class ComplianceCheckTest extends AbstractIpsPluginTest {

    /**
     * Test method for {@link org.faktorips.devtools.core.builder.ComplianceCheck#isComplianceLevelAtLeast5(org.faktorips.devtools.core.model.ipsproject.IIpsProject)}.
     * @throws CoreException 
     */
    public void testIsComplianceLevelAtLeast5() throws CoreException {
        IIpsProject ipsProject = newIpsProject();
        IJavaProject javaProject = ipsProject.getJavaProject();
        javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, "1.4");
        assertFalse(ComplianceCheck.isComplianceLevelAtLeast5(ipsProject));
        javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, "1.5");
        assertTrue(ComplianceCheck.isComplianceLevelAtLeast5(ipsProject));
    }

}
