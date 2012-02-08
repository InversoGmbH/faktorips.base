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

package org.faktorips.codegen.dthelpers;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.PrimitiveDatatypeHelper;
import org.faktorips.datatype.Datatype;

/**
 * Abstract base class for all primtive datatypes.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractPrimitiveDatatypeHelper extends AbstractDatatypeHelper implements PrimitiveDatatypeHelper {

    public AbstractPrimitiveDatatypeHelper() {
        super();
    }

    public AbstractPrimitiveDatatypeHelper(Datatype datatype) {
        super(datatype);
    }

    @Override
    public JavaCodeFragment newInstanceFromExpression(String expression) {
        return valueOfExpression(expression);
    }

    public JavaCodeFragment nullExpression() {
        throw new RuntimeException("Primitive datatype does not support null."); //$NON-NLS-1$
    }

    public JavaCodeFragment newEnumValueSetInstance(JavaCodeFragment valueCollection,
            JavaCodeFragment containsNullExpression) {

        throw new UnsupportedOperationException("Call the helper of the wrapper type instead."); //$NON-NLS-1$
    }

    public JavaCodeFragment newEnumValueSetInstance(String[] values, boolean containsNull) {
        throw new UnsupportedOperationException("Call the helper of the wrapper type instead."); //$NON-NLS-1$
    }

    @Override
    public JavaCodeFragment getToStringExpression(String fieldName) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("String.valueOf("); //$NON-NLS-1$
        fragment.append(fieldName);
        fragment.append(")"); //$NON-NLS-1$
        return fragment;
    }

}
