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

package org.faktorips.devtools.stdbuilder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

public interface AnnotationGeneratorFactory {

    /**
     * Returns <code>true</code> if the factory is required for the given project, as the
     * appropriate annotations have to be generated.
     * <p>
     * Note: This method is called during initialization of the builder set. Hence you cannot ask
     * the IpsProject for its builder set!
     * 
     * @param ipsProject The {@link IIpsProject} for which the {@link AnnotationGeneratorFactory}
     *            may be required.
     */
    public boolean isRequiredFor(IIpsProject ipsProject);

    public IAnnotationGenerator createAnnotationGenerator(AnnotatedJavaElementType type) throws CoreException;

}
