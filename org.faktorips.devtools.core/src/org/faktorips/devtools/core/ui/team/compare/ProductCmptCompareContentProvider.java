/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

/**
 * Contentprovider for <code>ProductCmptCompareViewer</code>. Returns images for 
 * <code>ProductCmptCompareItem</code>s by quering the contained <code>IIpsElement</code>.
 * The getXXXContent() methods simply return the left, right respectiveley ancestor 
 * input object referenced by the given input.
 * 
 * @author Stefan Widmaier
 */
public class ProductCmptCompareContentProvider implements IMergeViewerContentProvider {

    private CompareConfiguration config;

    public ProductCmptCompareContentProvider(CompareConfiguration cc) {
        config = cc;
    }

    /**
     * {@inheritDoc}
     */
    public String getAncestorLabel(Object input) {
        return config.getAncestorLabel(input);
    }

    /**
     * Returns the image of the <code>IIpsElement</code> that is referenced by the ancestor-<code>ProductCmptCompareItem</code>.
     * {@inheritDoc}
     */
    public Image getAncestorImage(Object input) {
        if (input instanceof ICompareInput) {
            ITypedElement el = ((ICompareInput)input).getAncestor();
            if (el instanceof ProductCmptCompareItem) {
                return ((ProductCmptCompareItem)el).getImage();
            }
        }
        return null;
    }

    /**
     * Returns the ancestor-<code>ProductCmptCompareItem</code> itself. The
     * <code>TextMergeViewer</code> can use <code>ProductCmptCompareItem</code>s as
     * <code>IDocumentRange</code>s. {@inheritDoc}
     */
    public Object getAncestorContent(Object input) {
        if (input instanceof ICompareInput) {
            ITypedElement el = ((ICompareInput)input).getAncestor();
            if (el instanceof ProductCmptCompareItem) {
                return el;
            }
        }
        return null;
    }

    /**
     * Returns false. {@inheritDoc}
     */
    public boolean showAncestor(Object input) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public String getLeftLabel(Object input) {
        return config.getLeftLabel(input);
    }

    /**
     * Returns the image of the <code>IIpsElement</code> that is referenced by the left
     * <code>ProductCmptCompareItem</code>. {@inheritDoc}
     */
    public Image getLeftImage(Object input) {
        if (input instanceof ICompareInput) {
            ITypedElement el = ((ICompareInput)input).getLeft();
            if (el instanceof ProductCmptCompareItem) {
                return ((ProductCmptCompareItem)el).getImage();
            }
        }
        return null;
    }

    /**
     * Returns the left <code>ProductCmptCompareItem</code> itself. The
     * <code>TextMergeViewer</code> can use <code>ProductCmptCompareItem</code>s as
     * <code>IDocumentRange</code>s. {@inheritDoc}
     */
    public Object getLeftContent(Object input) {
        if (input instanceof ICompareInput) {
            ITypedElement el = ((ICompareInput)input).getLeft();
            if (el instanceof ProductCmptCompareItem) {
                return el;
            }
        }
        return null;
    }

    /**
     * Returns false. {@inheritDoc}
     */
    public boolean isLeftEditable(Object input) {
        return false;
    }

    /**
     * Empty implementation. Nothing to save. {@inheritDoc}
     */
    public void saveLeftContent(Object input, byte[] bytes) {
    }

    /**
     * {@inheritDoc}
     */
    public String getRightLabel(Object input) {
        return config.getRightLabel(input);
    }

    /**
     * Returns the image of the <code>IIpsElement</code> that is referenced by the right
     * <code>ProductCmptCompareItem</code>. {@inheritDoc}
     */
    public Image getRightImage(Object input) {
        if (input instanceof ICompareInput) {
            ITypedElement el = ((ICompareInput)input).getRight();
            if (el instanceof ProductCmptCompareItem) {
                return ((ProductCmptCompareItem)el).getImage();
            }
        }
        return null;
    }

    /**
     * Returns the right <code>ProductCmptCompareItem</code> itself. The
     * <code>TextMergeViewer</code> can use <code>ProductCmptCompareItem</code>s as
     * <code>IDocumentRange</code>s. {@inheritDoc}
     */
    public Object getRightContent(Object input) {
        if (input instanceof ICompareInput) {
            ITypedElement el = ((ICompareInput)input).getRight();
            if (el instanceof ProductCmptCompareItem) {
                return el;
            }
        }
        return null;
    }

    /**
     * Returns false. {@inheritDoc}
     */
    public boolean isRightEditable(Object input) {
        return false;
    }

    /**
     * Empty implementation. Nothing to save. {@inheritDoc}
     */
    public void saveRightContent(Object input, byte[] bytes) {
    }

    /**
     * Empty implementation. {@inheritDoc}
     */
    public void dispose() {
    }

    /**
     * Empty implementation. {@inheritDoc}
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

}
