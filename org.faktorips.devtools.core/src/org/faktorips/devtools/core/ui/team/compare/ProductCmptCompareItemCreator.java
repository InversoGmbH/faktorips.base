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

import java.io.InputStream;

import org.eclipse.compare.HistoryItem;
import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.internal.ccvs.ui.ResourceEditionNode;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsProject;
import org.faktorips.devtools.core.internal.model.IpsSrcFileImmutable;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;

/**
 * Creates a structure/tree of ProductCmptCompareItems that is used for comparing ProductComponents.
 * <p>
 * For each product component (local, remote and a common ancestor (also remote)) a structure is
 * created based on their contents. The <code>StrucureMergeViewer</code> (by default
 * <code>StructureDiffViewer</code>) calls the <code>Differencer</code>, which compares the
 * created structures. As a result a tree of <code>DiffNode</code>s is created and displayed in
 * the <code>StructureDiffViewer</code> (topviewer in the compare window). Each
 * <code>DiffNode</code> in the result structure represents a difference/change between local and
 * remote product component. By doubleclicking such a node, a text representation of the product
 * components is displayed in the content mergeviewer (parallel scrollable textviewers at the bottom
 * of the compare window).
 * 
 * @author Stefan Widmaier
 */
public class ProductCmptCompareItemCreator implements IStructureCreator {

    public ProductCmptCompareItemCreator() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return Messages.ProductCmptCompareItemCreator_StructureViewer_title;
    }

    /**
     * Returns a tree of <code>ProductCmptCompareItem</code>s for the given input. This tree is
     * created on the basis of an <code>IIpsSrcFile</code> that might point to a local file or a
     * remote file (<code>IpsSrcFileImmutable</code>).
     * <p>
     * If the given input is a <code>ResourceNode</code>, an <code>IIpsSrcFile</code> is
     * created on the contained resource (local file). If the given input is a
     * <code>ISynchronizeModelElement</code>, an <code>IIpsSrcFile</code> is created on the
     * contained resource (local file). If the given Input is a <code>BufferedContent</code>,
     * <code>IEncodedStreamContentAccessor</code> and <code>ITypedElement</code>, an
     * <code>IpsSrcFileImmutable</code> (<code>FilteredBufferedResourceNode</code>) is created
     * reading remote contents via an input stream. {@inheritDoc}
     */
    public IStructureComparator getStructure(Object input) {
        if (input instanceof ResourceNode) {
            IResource file = ((ResourceNode)input).getResource();
            IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(file);
            if (element instanceof IIpsSrcFile) {
                return getStructureForIpsSrcFile((IIpsSrcFile)element);
            }
        } else if (input instanceof IEncodedStreamContentAccessor && input instanceof ITypedElement) {
            try {
                final IEncodedStreamContentAccessor remoteContent = (IEncodedStreamContentAccessor)input;
                // FIXME Workaround, change implementation of IpsSrcFileImmutable
                IIpsProject project = new IpsProject() {
                    public String getXmlFileCharset() {
                        try {
                            return remoteContent.getCharset();
                        } catch (CoreException e) {
                            IpsPlugin.log(e);
                        }
                        return null;
                    }
                };
                InputStream is = remoteContent.getContents();
                String name = ((ITypedElement)input).getName();
                // FIXME workaround for retrieving filename without using internal classes
                if (input instanceof ResourceEditionNode) {
                    ResourceEditionNode revision = (ResourceEditionNode)input;
                    name = revision.getRemoteResource().getName();
                }
                IpsSrcFileImmutable srcFile = new IpsSrcFileImmutable(project, name, is);
                return getStructureForIpsSrcFile(srcFile);
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        } else if (input instanceof ISynchronizeModelElement) {
            ISynchronizeModelElement modelElement = (ISynchronizeModelElement)input;
            IResource res = modelElement.getResource();
            if (res instanceof IFile) {
                IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(res);
                if (element instanceof IIpsSrcFile) {
                    return getStructureForIpsSrcFile((IIpsSrcFile)element);
                }
            }
        } else if (input instanceof HistoryItem) {
            IResource res = ((HistoryItem)input).getResource();
            IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(res);
            if (element instanceof IIpsSrcFile) {
                return getStructureForIpsSrcFile((IIpsSrcFile)element);
            }
        }
        return null;
    }

    /**
     * Returns a tree of <code>ProductCmptCompareItem</code>s. Each
     * <code>ProductCmptCompareItem</code> represents an IpsSrcFile, a ProductCmpt, a Generation,
     * a ConfigElement or a Relation.
     * 
     * @param file
     * @return
     */
    private IStructureComparator getStructureForIpsSrcFile(IIpsSrcFile file) {
        try {
            if (file.getIpsObject() instanceof IProductCmpt) {
                ProductCmptCompareItem root = new ProductCmptCompareItem(null, file);
                IProductCmpt product = (IProductCmpt)file.getIpsObject();
                ProductCmptCompareItem ipsObject = new ProductCmptCompareItem(root, product);
                // Generations of product
                IIpsObjectGeneration[] gens = product.getGenerations();
                for (int i = 0; i < gens.length; i++) {
                    ProductCmptCompareItem generation = new ProductCmptCompareItem(ipsObject, gens[i]);
                    // configElements for each generation
                    IConfigElement[] ces = ((IProductCmptGeneration)gens[i]).getConfigElements();
                    for (int j = 0; j < ces.length; j++) {
                        new ProductCmptCompareItem(generation, ces[j]);
                    }
                    // relations for each generation
                    IProductCmptRelation[] rels = ((IProductCmptGeneration)gens[i]).getRelations();
                    for (int j = 0; j < rels.length; j++) {
                        new ProductCmptCompareItem(generation, rels[j]);
                    }
                }
                // create the name, root document and ranges for all nodes
                root.initDocumentRange();
                return root;
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    /**
     * Returns null. {@inheritDoc}
     */
    public IStructureComparator locate(Object path, Object input) {
        return null;
    }

    /**
     * Returns null if node is not an <code>ProductCmptCompareItem</code>. Otherwise a
     * string-representation of the given <code>ProductCmptCompareItem</code> is returned.
     * 
     * @see ProductCmptCompareItem#getContentString() {@inheritDoc}
     */
    public String getContents(Object node, boolean ignoreWhitespace) {
        if (node instanceof ProductCmptCompareItem) {
            String content = ((ProductCmptCompareItem)node).getContentString();
            if (ignoreWhitespace) {
                return content.trim();
            }
            return content;
        }
        return null;
    }

    /**
     * Empty implementation. Nothing to be saved. {@inheritDoc}
     */
    public void save(IStructureComparator node, Object input) {
    }

}
