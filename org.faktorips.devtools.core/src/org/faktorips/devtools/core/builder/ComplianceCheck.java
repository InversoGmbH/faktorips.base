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

import org.eclipse.jdt.core.JavaCore;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.values.Decimal;

/**
 * A class that provides static methods to check against the Java compiler's compliance level.
 * 
 * @author Daniel Hohenberger
 */
public class ComplianceCheck {

    /**
     * @return <code>true</code> if the compliance level is set to at least 1.5,
     *         <code>false</code> if it is set lower or not set at all.
     */
    public static boolean isComplianceLevelAtLeast5(IIpsProject project) {
        String complianceLevel = project.getJavaProject().getOption(JavaCore.COMPILER_COMPLIANCE, true);
        if (complianceLevel != null) {
            return Decimal.valueOf(complianceLevel).greaterThanOrEqual(Decimal.valueOf("1.5")); //$NON-NLS-1$
        }
        return false; // assume old Java 1.4 project if compliance level is not set.
    }
}
