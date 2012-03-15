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

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.GenericValueDatatype;

/**
 * {@link DatatypeHelper} for {@link GenericValueDatatype}.
 */
public class GenericValueDatatypeHelper extends AbstractDatatypeHelper {

    public GenericValueDatatypeHelper(GenericValueDatatype datatype) {
        super(datatype);
    }

    private GenericValueDatatype getGenericValueDatatype() {
        return (GenericValueDatatype)getDatatype();
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        JavaCodeFragment code = new JavaCodeFragment();
        code.appendClassName(getGenericValueDatatype().getJavaClassName());
        code.append('.');
        code.append(getGenericValueDatatype().getValueOfMethodName());
        code.append('(');
        code.append(expression);
        code.append(')');
        return code;
    }

    @Override
    public JavaCodeFragment nullExpression() {
        GenericValueDatatype datatype = getGenericValueDatatype();
        JavaCodeFragment code = new JavaCodeFragment();
        if (!datatype.hasNullObject()) {
            code.append("null"); //$NON-NLS-1$
            return code;
        }
        code.appendClassName(datatype.getJavaClassName());
        code.append('.');
        code.append(datatype.getValueOfMethodName());
        code.append('(');
        if (datatype.getNullObjectId() == null) {
            code.append("null"); //$NON-NLS-1$
        } else {
            code.appendQuoted(datatype.getNullObjectId());
        }
        code.append(')');
        return code;
    }

    public JavaCodeFragment newInstance(String value) {
        if (value == null) {
            return nullExpression();
        }
        return valueOfExpression('"' + value + '"');
    }

    @Override
    public JavaCodeFragment getToStringExpression(String fieldName) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append(fieldName);
        fragment.append("==null?null:"); //$NON-NLS-1$
        fragment.append(fieldName);
        fragment.append("."); //$NON-NLS-1$
        fragment.append(((GenericValueDatatype)getDatatype()).getToStringMethodName());
        fragment.append("()"); //$NON-NLS-1$
        return fragment;
    }

}
