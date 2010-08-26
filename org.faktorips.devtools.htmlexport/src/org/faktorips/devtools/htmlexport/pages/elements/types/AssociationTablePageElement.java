/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;

/**
 * Represents a table with the associations of an {@link IType} as rows and the attributes of the
 * associations as columns
 * 
 * @author dicker
 * 
 */
public class AssociationTablePageElement extends AbstractIpsObjectPartsContainerTablePageElement<IAssociation> {

    private final DocumentorConfiguration config;
    private final IType type;

    /**
     * Creates an {@link AssociationTablePageElement} for the specified {@link IType}
     * 
     */
    public AssociationTablePageElement(IType type, DocumentorConfiguration config) {
        super(Arrays.asList(type.getAssociations()));
        this.config = config;
        this.type = type;
    }

    @Override
    protected List<? extends PageElement> createRowWithIpsObjectPart(IAssociation association) {
        List<String> values = new ArrayList<String>();

        values.add(association.getName());

        // will be replaced with the link
        values.add(""); //$NON-NLS-1$

        /*
         * TODO AW: What is the right description to use as model elements can now have descriptions
         * in different languages?
         */
        values.add(association.getDescription());
        values.add(association.getAssociationType().getName());
        values.add(association.getAggregationKind().getName());
        values.add(association.getTargetRoleSingular());
        values.add(association.getTargetRolePlural());
        values.add(Integer.toString(association.getMinCardinality()));
        values.add(getMaxCardinalityString(association.getMaxCardinality()));
        values.add(association.isDerivedUnion() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$
        values.add(association.isSubsetOfADerivedUnion() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$
        values.add(association.isQualified() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$

        PageElement[] elements = PageElementUtils.createTextPageElements(values);

        try {
            IIpsObject target = type.getIpsProject().findIpsObject(type.getIpsObjectType(), association.getTarget());
            elements[1] = PageElementUtils.createLinkPageElement(config, target, "content", target.getName(), true); //$NON-NLS-1$
        } catch (CoreException e) {
            elements[1] = new TextPageElement(""); //$NON-NLS-1$
        }

        return Arrays.asList(elements);
    }

    private String getMaxCardinalityString(int maxCardinality) {
        if (maxCardinality == Integer.MAX_VALUE) {
            return "*"; //$NON-NLS-1$
        }
        return Integer.toString(maxCardinality);
    }

    @Override
    protected List<String> getHeadlineWithIpsObjectPart() {
        List<String> headline = new ArrayList<String>();

        headline.add(Messages.AssociationTablePageElement_headlineName);
        headline.add(Messages.AssociationTablePageElement_headlineTarget);
        headline.add(Messages.AssociationTablePageElement_headlineDescription);
        headline.add(Messages.AssociationTablePageElement_headlineAssociationType);
        headline.add(Messages.AssociationTablePageElement_headlineAggregationKind);
        headline.add(Messages.AssociationTablePageElement_headlineTargetRoleSingular);
        headline.add(Messages.AssociationTablePageElement_headlineTargetRolePlural);

        addHeadlineAndColumnLayout(headline, Messages.AssociationTablePageElement_headlineMinCardinality, Style.CENTER);
        addHeadlineAndColumnLayout(headline, Messages.AssociationTablePageElement_headlineMaxCardinality, Style.CENTER);
        addHeadlineAndColumnLayout(headline, Messages.AssociationTablePageElement_headlineDerivedUnion, Style.CENTER);
        addHeadlineAndColumnLayout(headline, Messages.AssociationTablePageElement_headlineSubsettedDerivedUnion,
                Style.CENTER);
        addHeadlineAndColumnLayout(headline, Messages.AssociationTablePageElement_headlineQualified, Style.CENTER);

        return headline;
    }
}
