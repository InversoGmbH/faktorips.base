/*******************************************************************************
 * Copyright (c) 2007 Faktor Zehn GmbH und andere.
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

package org.faktorips.devtools.core.ui.dialogs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 *
 * @author Markus Blum
 */
public class IpsProjectSortOrdersPMTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot rootPackage;
    private IpsProjectSortOrdersPM sortOrderPM;

    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject("TestProject");
        rootPackage = ipsProject.getIpsPackageFragmentRoots()[0];

        rootPackage.createPackageFragment("products.hausrat", true, null);
        rootPackage.createPackageFragment("products.kranken", true, null);
        rootPackage.createPackageFragment("products.kranken.leistungsarten", true, null);
        rootPackage.createPackageFragment("products.kranken.leistungsarten.fix", true, null);
        rootPackage.createPackageFragment("products.kranken.leistungsarten.optional", true, null);
        rootPackage.createPackageFragment("products.kranken.vertragsarten", true, null);
        rootPackage.createPackageFragment("products.kranken.gruppenarten", true, null);
        rootPackage.createPackageFragment("products.unfall", true, null);
        rootPackage.createPackageFragment("products.haftpflicht", true, null);

        sortOrderPM = new IpsProjectSortOrdersPM(ipsProject);
    }

    public void testGetChildrenBasic() {
        IIpsPackageFragment fragment = rootPackage.getIpsPackageFragment("products.hausrat");
        Object[] elements = sortOrderPM.getChildren(fragment);
        assertEquals(0, elements.length);

        fragment = rootPackage.getDefaultIpsPackageFragment();
        elements = sortOrderPM.getChildren(fragment);
        assertEquals(1, elements.length);
        assertEquals("products", ((IIpsPackageFragment)elements[0]).getName());

        fragment = rootPackage.getIpsPackageFragment("products.kranken");
        elements = sortOrderPM.getChildren(fragment);
        assertEquals(3, elements.length);
        assertEquals("products.kranken.gruppenarten", ((IIpsPackageFragment)elements[0]).getName());
        assertEquals("products.kranken.leistungsarten", ((IIpsPackageFragment)elements[1]).getName());
        assertEquals("products.kranken.vertragsarten", ((IIpsPackageFragment)elements[2]).getName());
    }

    public void testGetChildrenExtended() throws IOException, CoreException {
        sortOrderPM.dispose();

        List list = new ArrayList();
        list.add("vertragsarten");
        list.add("gruppenarten");
        list.add("leistungsarten");

        IIpsPackageFragment fragment = rootPackage.getIpsPackageFragment("products.kranken");
        createPackageOrderFile((IFolder) fragment.getCorrespondingResource(), list);

        list.clear();
        list.add("kranken");
        list.add("unfall");
        list.add("hausrat");
        list.add("haftpflicht");

        fragment = rootPackage.getIpsPackageFragment("products");
        createPackageOrderFile((IFolder) fragment.getCorrespondingResource(), list);

        rootPackage.createPackageFragment("products.kranken.notlisted", true, null);

        fragment = rootPackage.getIpsPackageFragment("products.hausrat");
        Object[] elements = sortOrderPM.getChildren(fragment);
        assertEquals(0, elements.length);

        fragment = rootPackage.getDefaultIpsPackageFragment();
        elements = sortOrderPM.getChildren(fragment);
        assertEquals(1, elements.length);
        assertEquals("products", ((IIpsPackageFragment)elements[0]).getName());

        fragment = rootPackage.getIpsPackageFragment("products.kranken");
        elements = sortOrderPM.getChildren(fragment);

        assertEquals(4, elements.length);
        assertEquals("products.kranken.vertragsarten", ((IIpsPackageFragment)elements[0]).getName());
        assertEquals("products.kranken.gruppenarten", ((IIpsPackageFragment)elements[1]).getName());
        assertEquals("products.kranken.leistungsarten", ((IIpsPackageFragment)elements[2]).getName());
        assertEquals("products.kranken.notlisted", ((IIpsPackageFragment)elements[3]).getName());

        fragment = rootPackage.getIpsPackageFragment("products");
        elements = sortOrderPM.getChildren(fragment);
        assertEquals(4, elements.length);
        assertEquals("products.kranken", ((IIpsPackageFragment)elements[0]).getName());
        assertEquals("products.unfall", ((IIpsPackageFragment)elements[1]).getName());
        assertEquals("products.hausrat", ((IIpsPackageFragment)elements[2]).getName());
        assertEquals("products.haftpflicht", ((IIpsPackageFragment)elements[3]).getName());

        list.clear();
        list.add("kranken");
        list.add("unfall");
        list.add("hausrat");
        list.add("haftpflicht");
        list.add("notlisted");
        createPackageOrderFile((IFolder) fragment.getCorrespondingResource(), list);

        elements = sortOrderPM.getChildren(fragment);
        assertEquals(4, elements.length);
        assertEquals("products.kranken", ((IIpsPackageFragment)elements[0]).getName());
        assertEquals("products.unfall", ((IIpsPackageFragment)elements[1]).getName());
        assertEquals("products.hausrat", ((IIpsPackageFragment)elements[2]).getName());
        assertEquals("products.haftpflicht", ((IIpsPackageFragment)elements[3]).getName());
    }

    public void testGetParent() {
        IIpsPackageFragment fragment = rootPackage.getIpsPackageFragment("products.hausrat");
        IIpsPackageFragment parent = rootPackage.getIpsPackageFragment("products");
        assertEquals(parent, sortOrderPM.getParent(fragment));

        fragment = rootPackage.getIpsPackageFragment("products");
        parent = rootPackage.getDefaultIpsPackageFragment();
        assertEquals(parent, sortOrderPM.getParent(fragment));

        fragment = rootPackage.getDefaultIpsPackageFragment();
        assertEquals(null, sortOrderPM.getParent(fragment));
    }

    public void testHasChildren() {
        IIpsPackageFragment fragment = rootPackage.getIpsPackageFragment("products.hausrat");
        assertFalse(sortOrderPM.hasChildren(fragment));

        fragment = rootPackage.getIpsPackageFragment("products.kranken");
        assertTrue(sortOrderPM.hasChildren(fragment));

        fragment = rootPackage.getDefaultIpsPackageFragment();
        assertTrue(sortOrderPM.hasChildren(fragment));
    }

    public void testGetElements() {
        Object[] elements = sortOrderPM.getElements(sortOrderPM);

        assertEquals(elements.length, 1);
        IIpsPackageFragment fragment = (IIpsPackageFragment)elements[0];
        assertTrue(fragment.isDefaultPackage());
    }

    public void testMoveUp() throws IOException, CoreException {
        sortOrderPM.dispose();

        List list = new ArrayList();
        list.add("vertragsarten");
        list.add("gruppenarten");
        list.add("leistungsarten");

        IIpsPackageFragment fragment = rootPackage.getIpsPackageFragment("products.kranken");
        createPackageOrderFile((IFolder) fragment.getCorrespondingResource(), list);

        list.clear();
        list.add("kranken");
        list.add("unfall");
        list.add("hausrat");
        list.add("haftpflicht");

        fragment = rootPackage.getIpsPackageFragment("products");
        createPackageOrderFile((IFolder) fragment.getCorrespondingResource(), list);

        fragment = rootPackage.getIpsPackageFragment("products.kranken");
        IIpsPackageFragment[] elements = (IIpsPackageFragment[])sortOrderPM.getChildren(fragment);

        IIpsPackageFragment selectedFragment = rootPackage.getIpsPackageFragment("products.kranken.leistungsarten");
        sortOrderPM.moveUp(selectedFragment, 1);

        elements = (IIpsPackageFragment[])sortOrderPM.getChildren(fragment);
        assertEquals(3, elements.length);
        assertEquals("products.kranken.vertragsarten", ((IIpsPackageFragment)elements[0]).getName());
        assertEquals("products.kranken.leistungsarten", ((IIpsPackageFragment)elements[1]).getName());
        assertEquals("products.kranken.gruppenarten", ((IIpsPackageFragment)elements[2]).getName());

        selectedFragment = rootPackage.getIpsPackageFragment("products.kranken.vertragsarten");
        sortOrderPM.moveUp(selectedFragment, 2);

        elements = (IIpsPackageFragment[])sortOrderPM.getChildren(fragment);
        assertEquals(3, elements.length);
        assertEquals("products.kranken.vertragsarten", ((IIpsPackageFragment)elements[0]).getName());
        assertEquals("products.kranken.leistungsarten", ((IIpsPackageFragment)elements[1]).getName());
        assertEquals("products.kranken.gruppenarten", ((IIpsPackageFragment)elements[2]).getName());

        selectedFragment = rootPackage.getIpsPackageFragment("products.kranken.gruppenarten");
        sortOrderPM.moveUp(selectedFragment, 1);

        elements = (IIpsPackageFragment[])sortOrderPM.getChildren(fragment);
        assertEquals(3, elements.length);
        assertEquals("products.kranken.vertragsarten", ((IIpsPackageFragment)elements[0]).getName());
        assertEquals("products.kranken.gruppenarten", ((IIpsPackageFragment)elements[1]).getName());
        assertEquals("products.kranken.leistungsarten", ((IIpsPackageFragment)elements[2]).getName());
    }

    public void testMoveDown() throws IOException, CoreException {
        sortOrderPM.dispose();

        List list = new ArrayList();
        list.add("vertragsarten");
        list.add("gruppenarten");
        list.add("leistungsarten");

        IIpsPackageFragment fragment = rootPackage.getIpsPackageFragment("products.kranken");
        createPackageOrderFile((IFolder) fragment.getCorrespondingResource(), list);

        list.clear();
        list.add("kranken");
        list.add("unfall");
        list.add("hausrat");
        list.add("haftpflicht");

        fragment = rootPackage.getIpsPackageFragment("products");
        createPackageOrderFile((IFolder) fragment.getCorrespondingResource(), list);

        IIpsPackageFragment selectedFragment = rootPackage.getIpsPackageFragment("products.kranken.leistungsarten");
        sortOrderPM.moveDown(selectedFragment, 1);

        fragment = rootPackage.getIpsPackageFragment("products.kranken");
        IIpsPackageFragment[] elements = (IIpsPackageFragment[])sortOrderPM.getChildren(fragment);
        assertEquals(3, elements.length);
        assertEquals("products.kranken.vertragsarten", ((IIpsPackageFragment)elements[0]).getName());
        assertEquals("products.kranken.gruppenarten", ((IIpsPackageFragment)elements[1]).getName());
        assertEquals("products.kranken.leistungsarten", ((IIpsPackageFragment)elements[2]).getName());

        selectedFragment = rootPackage.getIpsPackageFragment("products.kranken.vertragsarten");
        sortOrderPM.moveDown(selectedFragment, elements.length + 3);

        elements = (IIpsPackageFragment[])sortOrderPM.getChildren(fragment);
        assertEquals(3, elements.length);
        assertEquals("products.kranken.gruppenarten", ((IIpsPackageFragment)elements[0]).getName());
        assertEquals("products.kranken.leistungsarten", ((IIpsPackageFragment)elements[1]).getName());
        assertEquals("products.kranken.vertragsarten", ((IIpsPackageFragment)elements[2]).getName());

        selectedFragment = rootPackage.getIpsPackageFragment("products.kranken.gruppenarten");
        sortOrderPM.moveDown(selectedFragment, 1);

        elements = (IIpsPackageFragment[])sortOrderPM.getChildren(fragment);
        assertEquals(3, elements.length);
        assertEquals("products.kranken.leistungsarten", ((IIpsPackageFragment)elements[0]).getName());
        assertEquals("products.kranken.gruppenarten", ((IIpsPackageFragment)elements[1]).getName());
        assertEquals("products.kranken.vertragsarten", ((IIpsPackageFragment)elements[2]).getName());
    }

    public void testRestore() throws IOException, CoreException {
        sortOrderPM.dispose();

        List list = new ArrayList();
        list.add("vertragsarten");
        list.add("gruppenarten");
        list.add("leistungsarten");

        IIpsPackageFragment fragment = rootPackage.getIpsPackageFragment("products.kranken");
        createPackageOrderFile((IFolder) fragment.getCorrespondingResource(), list);

        list.clear();
        list.add("kranken");
        list.add("unfall");
        list.add("hausrat");
        list.add("haftpflicht");

        fragment = rootPackage.getIpsPackageFragment("products");
        createPackageOrderFile((IFolder) fragment.getCorrespondingResource(), list);
        fragment = rootPackage.getIpsPackageFragment("products.kranken");
        IIpsPackageFragment[] elements = (IIpsPackageFragment[])sortOrderPM.getChildren(fragment);

        sortOrderPM.restore();

        elements = (IIpsPackageFragment[])sortOrderPM.getChildren(fragment);
        assertEquals(3, elements.length);
        assertEquals("products.kranken.gruppenarten", ((IIpsPackageFragment)elements[0]).getName());
        assertEquals("products.kranken.leistungsarten", ((IIpsPackageFragment)elements[1]).getName());
        assertEquals("products.kranken.vertragsarten", ((IIpsPackageFragment)elements[2]).getName());
    }

    public void testSaveSortDefDelta() throws IOException, CoreException {
        sortOrderPM.dispose();

        List list = new ArrayList();
        list.add("kranken");
        list.add("unfall");
        list.add("hausrat");
        list.add("haftpflicht");

        IIpsPackageFragment fragment = rootPackage.getIpsPackageFragment("products");
        createPackageOrderFile((IFolder) fragment.getCorrespondingResource(), list);
        fragment = rootPackage.getIpsPackageFragment("products.kranken");
        IIpsPackageFragment[] elements = (IIpsPackageFragment[])sortOrderPM.getChildren(fragment);
        fragment = rootPackage.getIpsPackageFragment("products.kranken.gruppenarten");
        sortOrderPM.moveDown(fragment, 1);

        sortOrderPM.saveSortDefDelta();
        sortOrderPM.dispose();
        fragment = rootPackage.getIpsPackageFragment("products.kranken");
        elements = (IIpsPackageFragment[])sortOrderPM.getChildren(fragment);
        assertEquals(3, elements.length);
        assertEquals("products.kranken.leistungsarten", ((IIpsPackageFragment)elements[0]).getName());
        assertEquals("products.kranken.gruppenarten", ((IIpsPackageFragment)elements[1]).getName());
        assertEquals("products.kranken.vertragsarten", ((IIpsPackageFragment)elements[2]).getName());

        fragment = rootPackage.getIpsPackageFragment("products");
        elements = (IIpsPackageFragment[])sortOrderPM.getChildren(fragment);
        assertEquals(4, elements.length);
        assertEquals("products.kranken", ((IIpsPackageFragment)elements[0]).getName());
        assertEquals("products.unfall", ((IIpsPackageFragment)elements[1]).getName());
        assertEquals("products.hausrat", ((IIpsPackageFragment)elements[2]).getName());
        assertEquals("products.haftpflicht", ((IIpsPackageFragment)elements[3]).getName());

        sortOrderPM.restore();
        sortOrderPM.saveSortDefDelta();
        sortOrderPM.dispose();
        fragment = rootPackage.getIpsPackageFragment("products.kranken");
        elements = (IIpsPackageFragment[])sortOrderPM.getChildren(fragment);
        assertEquals(3, elements.length);
        assertEquals("products.kranken.gruppenarten", ((IIpsPackageFragment)elements[0]).getName());
        assertEquals("products.kranken.leistungsarten", ((IIpsPackageFragment)elements[1]).getName());
        assertEquals("products.kranken.vertragsarten", ((IIpsPackageFragment)elements[2]).getName());
    }
}
