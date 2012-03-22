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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;

/**
 * Product cmpt editor input based on file editor input. Contains product cmpt relevant information
 * for the product cmpt editor.
 * 
 * @author Joerg Ortmann
 */
public class ProductCmptEditorInput extends FileEditorInput {

    /**
     * Creates a product cmpt editor input with a given generation.<br>
     * Could be used to open a product cmpt and initially showing the given generation.
     */
    public static IFileEditorInput createWithGeneration(IProductCmptGeneration productCmptGeneration) {
        return new ProductCmptEditorInput(productCmptGeneration);
    }

    private final IProductCmptGeneration productCmptGeneration;

    private ProductCmptEditorInput(IFile file) {
        super(file);
        productCmptGeneration = null;
    }

    private ProductCmptEditorInput(IProductCmptGeneration productCmptGeneration) {
        super(productCmptGeneration.getIpsObject().getIpsSrcFile().getCorrespondingFile());
        this.productCmptGeneration = productCmptGeneration;
    }

    /**
     * Returns the {@link IProductCmptGeneration} to be initially opened by the editor or
     * {@code null} if unspecified.
     */
    public IProductCmptGeneration getProductCmptGeneration() {
        return productCmptGeneration;
    }

}
