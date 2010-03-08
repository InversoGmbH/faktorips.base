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

package org.faktorips.devtools.stdbuilder;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.model.IIpsElement;

/**
 * Defines an interface for generators which create annotations for IpsElements.
 * 
 * @author Roman Grutza
 */
public interface IAnnotationGenerator {

    /**
     * Returns the standard builder set.
     */
    public StandardBuilderSet getStandardBuilderSet();

    /**
     * Returns the type of the Java Element that is being annotated. See
     * {@link AnnotatedJavaElementType} for an enumeration of possible constructs that can be
     * annotated.
     */
    public AnnotatedJavaElementType getAnnotatedJavaElementType();

    /**
     * Creates the annotations for the given ipsElement.
     */
    public JavaCodeFragment createAnnotation(IIpsElement ipsElement);
}
