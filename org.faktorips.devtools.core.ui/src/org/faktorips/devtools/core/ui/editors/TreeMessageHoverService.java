/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;


/**
 * Class to show hovers for trees. 
 */
public abstract class TreeMessageHoverService extends MessageHoverService {
    
    private Tree tree;

    public TreeMessageHoverService(TreeViewer viewer) {
        super(viewer.getTree());
        this.tree = viewer.getTree();
    }

    /**
     * {@inheritDoc}
     */
    public Object getElementAt(Point point) {
        TreeItem item = tree.getItem(point);
        if (item == null) {
            return null;
        }
        return item.getData();
    }
    
    /**
     * {@inheritDoc}
     */
    public Rectangle getBoundsAt(Point point) {
        TreeItem item = tree.getItem(point);
        if (item == null) {
            return null;
        }
        return item.getBounds(0);
    }
}
