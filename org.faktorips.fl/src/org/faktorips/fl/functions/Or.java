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

package org.faktorips.fl.functions;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;

/**
 * A function that provides a boolean or-operation and has the following signature <i>boolean
 * OR(boolean...)</i>.
 */
public class Or extends AbstractJavaVarArgFunction {

    public Or(String name, String description) {
        super(name, description, Datatype.PRIMITIVE_BOOLEAN, Datatype.PRIMITIVE_BOOLEAN);
    }

    @Override
    protected void compileInternal(CompilationResult<JavaCodeFragment> returnValue,
            CompilationResult<JavaCodeFragment>[] convertedArgs,
            JavaCodeFragment fragment) {
        fragment.append('(');
        for (int i = 0; i < convertedArgs.length; i++) {
            fragment.append(convertedArgs[i].getCodeFragment());

            if (i < convertedArgs.length - 1) {
                fragment.append("||"); //$NON-NLS-1$
            }
        }
        fragment.append(')');
    }

}
