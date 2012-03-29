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

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.helper.IpsObjectTypeComparator;
import org.faktorips.devtools.htmlexport.helper.filter.IIpsElementFilter;
import org.faktorips.devtools.htmlexport.helper.path.HtmlPathFactory;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;

/**
 * Creates a list with links to the pages of the given {@link IpsObject}s. The {@link IpsObject}s
 * will be filtered and sorted on the page.
 * 
 * @author dicker
 * 
 */
public abstract class AbstractIpsElementListPageElement extends AbstractRootPageElement {

    protected IIpsElement baseIpsElement;
    protected TargetType linkTarget;
    protected List<IIpsSrcFile> srcFiles;
    protected IIpsElementFilter filter = ALL_FILTER;
    private DocumentationContext context;

    /**
     * {@link IIpsElementFilter}, which accepts all {@link IIpsElement}s
     */
    protected final static IIpsElementFilter ALL_FILTER = new IIpsElementFilter() {
        @Override
        public boolean accept(IIpsElement object) {
            return true;
        }
    };

    /**
     * {@link Comparator}, which is used for sorting the {@link IIpsObject}s according to their
     * {@link IpsObjectType} and then their unqualified name.
     */
    protected final static Comparator<IIpsSrcFile> IPS_OBJECT_COMPARATOR = new Comparator<IIpsSrcFile>() {
        @Override
        public int compare(IIpsSrcFile o1, IIpsSrcFile o2) {
            IpsObjectTypeComparator ipsObjectTypeComparator = new IpsObjectTypeComparator();

            int comparationIpsObjectType = ipsObjectTypeComparator
                    .compare(o1.getIpsObjectType(), o2.getIpsObjectType());

            if (comparationIpsObjectType == 0) {
                return o1.getIpsObjectName().compareTo(o2.getIpsObjectName());
            }

            return comparationIpsObjectType;
        }
    };

    /**
     * creates an {@link AbstractIpsElementListPageElement}
     * 
     * @param baseIpsElement ipsElement, which represents the location of the page for links from
     *            the page
     * @param srcFiles unfiltered and unsorted objects to list on the page
     * @param filter for objects
     */
    public AbstractIpsElementListPageElement(IIpsElement baseIpsElement, List<IIpsSrcFile> srcFiles,
            IIpsElementFilter filter, DocumentationContext context) {
        super();
        this.baseIpsElement = baseIpsElement;
        this.srcFiles = srcFiles;
        this.filter = filter;
        this.context = context;
    }

    /**
     * creates an {@link AbstractIpsElementListPageElement}
     * 
     * @param baseIpsElement ipsElement, which represents the location of the page for links from
     *            the page
     * @param srcFiles objects to list on the page
     */
    public AbstractIpsElementListPageElement(IIpsElement baseIpsElement, List<IIpsSrcFile> srcFiles,
            DocumentationContext context) {
        this(baseIpsElement, srcFiles, ALL_FILTER, context);
    }

    /**
     * @return the {@link IIpsPackageFragment}s of all filtered objects
     */
    protected Set<IIpsPackageFragment> getRelatedPackageFragments() {
        Set<IIpsPackageFragment> packageFragments = new LinkedHashSet<IIpsPackageFragment>();
        for (IIpsSrcFile object : srcFiles) {
            if (!filter.accept(object)) {
                continue;
            }
            packageFragments.add(object.getIpsPackageFragment());
        }
        return packageFragments;
    }

    /**
     * @return the {@link IpsObjectType}s of all filtered objects
     */
    protected Set<IpsObjectType> getRelatedObjectTypes() {
        Set<IpsObjectType> packageFragments = new LinkedHashSet<IpsObjectType>();
        for (IIpsSrcFile object : srcFiles) {
            if (!filter.accept(object)) {
                continue;
            }
            packageFragments.add(object.getIpsObjectType());
        }
        return packageFragments;
    }

    /**
     * @return the target for all links
     */
    public TargetType getLinkTarget() {
        return linkTarget;
    }

    /**
     * sets the target for all links
     * 
     */
    public void setLinkTarget(TargetType linkTarget) {
        this.linkTarget = linkTarget;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement#getPathToRoot()
     */
    @Override
    public String getPathToRoot() {
        return HtmlPathFactory.createPathUtil(baseIpsElement).getPathToRoot();
    }

    public DocumentationContext getContext() {
        return context;
    }

    @Override
    public boolean isContentUnit() {
        return false;
    }
}
