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

package org.faktorips.devtools.core.model.productcmpttype;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;

/**
 * A type hierarchy finder that looks for {@link IProductCmptTypeMethod}s which are formula methods
 * with the specified formula name. If the flag <code>findAllSignaturesInHierarchy</code> is set to
 * true the search process is interrupted after the first method is found.
 * 
 * @author Peter Erzberger
 */
public class FormulaSignatureFinder extends TypeHierarchyVisitor<IProductCmptType> {

    private String formulaName;
    private List<IMethod> methods;
    private boolean findAllSignaturesInHierarchy = false;

    public FormulaSignatureFinder(IIpsProject ipsProject, String formulaName, boolean findAllSignaturesInHierarchy) {
        super(ipsProject);
        this.formulaName = formulaName;
        this.findAllSignaturesInHierarchy = findAllSignaturesInHierarchy;
        methods = new ArrayList<IMethod>();
    }

    /**
     * Returns the method that was found by this finder or <code>null</code> if non was found. It
     * only makes sense to call this method after visiting of the type hierarchy has been finished.
     */
    public List<IMethod> getMethods() {
        return new ArrayList<IMethod>(methods);
    }

    @Override
    protected boolean visit(IProductCmptType currentType) throws CoreException {
        IProductCmptTypeMethod formulaSignature = currentType.getFormulaSignature(formulaName);
        if (formulaSignature != null) {
            methods.add(formulaSignature);
        }
        return findAllSignaturesInHierarchy || methods.isEmpty();
    }

}
