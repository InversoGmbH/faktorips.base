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

package org.faktorips.devtools.core.ui.team.compare.tablecontents;

import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.ui.team.compare.AbstractCompareItemCreator;

/**
 * Structure creator for creating a tree that represents the structure of a <code>ITableContents</code> object.
 * {@inheritDoc}
 * @author Stefan Widmaier
 */
public class TableContentsCompareItemCreator extends AbstractCompareItemCreator {
    
    public TableContentsCompareItemCreator(){
        super();
    }
    
    /**
     * Returns the title for the structure-differences viewer.
     * {@inheritDoc}
     */
    public String getName() {
        return Messages.TableContentsCompareItemCreator_TableContentsStructureCompare;
    }

    /**
     * Creates a structure/tree of <code>TableContentsCompareItem</code>s from the given
     * <code>IIpsSrcFile</code> to represent an <code>ITableContents</code> object. The
     * <code>IIpsSrcFile</code>, the <code>ITableContents</code>, its generations and all
     * contained rows are each represented by a <code>TableContentsCompareItem</code>.
     * <p>
     * The returned <code>TableContentsCompareItem</code> is the root of the created structure and
     * contains the given <code>IIpsSrcFile</code>. It has exactly one child representing (and
     * referencing) the <code>ITableContents</code> contained in the srcFile. This
     * <code>TableContentsCompareItem</code> has a child for each generation the table posesses.
     * Each generation-compareitem contains multiple <code>TableContentsCompareItem</code>s
     * representing the rows (<code>IRow</code>) of the table (in the current generation).
     * 
     * {@inheritDoc}
     */
    protected IStructureComparator getStructureForIpsSrcFile(IIpsSrcFile file) {
        try {
            if (file.getIpsObject() instanceof ITableContents) {
                TableContentsCompareItem root = new TableContentsCompareItem(null, file);
                ITableContents table = (ITableContents)file.getIpsObject();
                TableContentsCompareItem ipsObject = new TableContentsCompareItem(root, table);
                // Generations for table
                IIpsObjectGeneration[] gens = table.getGenerationsOrderedByValidDate();
                for (int i = 0; i < gens.length; i++) {
                    TableContentsCompareItem generation = new TableContentsCompareItem(ipsObject, gens[i]);
                    // rows for each generation
                    IRow[] rows = ((ITableContentsGeneration)gens[i]).getRows();
                    for (int j = 0; j < rows.length; j++) {
                        new TableContentsCompareItem(generation, rows[j]);
                    }
                }
                // initialize name, root-document and ranges for all nodes
                root.init();
                return root;
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

}
