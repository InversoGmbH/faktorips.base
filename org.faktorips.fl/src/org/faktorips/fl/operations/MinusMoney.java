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

package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;

/**
 * Minus (-) operator for datatype Money.
 */
public class MinusMoney extends AbstractUnaryJavaOperation {

    public MinusMoney() {
        super(Datatype.MONEY, "-"); //$NON-NLS-1$
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.UnaryOperation#generate(org.faktorips.fl.CompilationResult)
     */
    @Override
    public CompilationResultImpl generate(CompilationResultImpl result) {
        result.getCodeFragment().append(".multiply(-1)"); //$NON-NLS-1$
        return result;
    }

}
