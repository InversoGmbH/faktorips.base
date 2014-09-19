/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsObjectPathEntryTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IpsObjectPath path;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        path = new IpsObjectPath(ipsProject);
    }

    @Test
    public void testGetIndex() throws CoreException {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        assertEquals(1, path.getEntries().length);

        IIpsObjectPathEntry entry0 = path.getEntries()[0];
        assertEquals(0, entry0.getIndex());

        IIpsObjectPathEntry entry1 = path.newArchiveEntry(ipsProject.getProject().getFile("someArchive.jar")
                .getFullPath());
        assertEquals(0, entry0.getIndex());
        assertEquals(1, entry1.getIndex());
    }

    @Test
    public void testCreateFromXml() {
        Document doc = getTestDocument();
        NodeList nl = doc.getDocumentElement().getElementsByTagName(IpsObjectPathEntry.XML_ELEMENT);
        IIpsObjectPathEntry entry = IpsObjectPathEntry
                .createFromXml(path, (Element)nl.item(0), ipsProject.getProject());
        assertEquals(IIpsObjectPathEntry.TYPE_SRC_FOLDER, entry.getType());
        assertTrue(entry.isReexported());
        entry = IpsObjectPathEntry.createFromXml(path, (Element)nl.item(1), ipsProject.getProject());
        assertEquals(IIpsObjectPathEntry.TYPE_PROJECT_REFERENCE, entry.getType());
        assertFalse(entry.isReexported());
    }

    @Test
    public void testFindIpsSrcFilesInternal_empty() throws Exception {
        IpsObjectPathEntry ipsObjectPathEntry = (IpsObjectPathEntry)ipsProject.getIpsObjectPath().getEntries()[0];
        String packName = "any.pack";

        List<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        ipsObjectPathEntry.findIpsSrcFilesInternal(IpsObjectType.IPS_SOURCE_FILE, packName, result,
                new HashSet<IIpsObjectPathEntry>());

        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindIpsSrcFilesInternal_withResults() throws Exception {
        IpsObjectPathEntry ipsObjectPathEntry = (IpsObjectPathEntry)ipsProject.getIpsObjectPath().getEntries()[0];
        String packName = "any.pack";
        IIpsPackageFragment packageFragment = ipsObjectPathEntry.getIpsPackageFragmentRoot().createPackageFragment(
                packName, true, null);
        IIpsSrcFile ipsSrcFile = packageFragment.createIpsFile(IpsObjectType.PRODUCT_CMPT, "MyFileName", true, null);

        List<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        ipsObjectPathEntry.findIpsSrcFilesInternal(IpsObjectType.PRODUCT_CMPT, packName, result,
                new HashSet<IIpsObjectPathEntry>());

        assertEquals(1, result.size());
        assertEquals(ipsSrcFile, result.get(0));
    }

    @Test
    public void testFindIpsSrcFilesInternal_noMatchingType() throws Exception {
        IpsObjectPathEntry ipsObjectPathEntry = (IpsObjectPathEntry)ipsProject.getIpsObjectPath().getEntries()[0];
        String packName = "any.pack";
        IIpsPackageFragment packageFragment = ipsObjectPathEntry.getIpsPackageFragmentRoot().createPackageFragment(
                packName, true, null);
        packageFragment.createIpsFile(IpsObjectType.PRODUCT_CMPT, "MyFileName", true, null);

        List<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        ipsObjectPathEntry.findIpsSrcFilesInternal(IpsObjectType.ENUM_TYPE, packName, result,
                new HashSet<IIpsObjectPathEntry>());

        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindIpsSrcFilesInternal_deleted() throws Exception {
        IpsObjectPathEntry ipsObjectPathEntry = (IpsObjectPathEntry)ipsProject.getIpsObjectPath().getEntries()[0];
        String packName = "any.pack2";
        IIpsPackageFragment packageFragment = ipsObjectPathEntry.getIpsPackageFragmentRoot().createPackageFragment(
                packName, true, null);
        IIpsSrcFile ipsSrcFile = packageFragment.createIpsFile(IpsObjectType.PRODUCT_CMPT, "MyFileName", true, null);
        ipsSrcFile.delete();

        List<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        ipsObjectPathEntry.findIpsSrcFilesInternal(IpsObjectType.PRODUCT_CMPT, packName, result,
                new HashSet<IIpsObjectPathEntry>());

        assertTrue(result.isEmpty());
    }

    @Test
    public void testIsReexport() throws Exception {
        IIpsObjectPathEntry ipsObjectPathEntry = path.newArchiveEntry(ipsProject.getProject()
                .getFile("someArchive.jar").getFullPath());
        assertTrue(ipsObjectPathEntry.isReexported());
        ipsObjectPathEntry.setReexported(false);
        assertFalse(ipsObjectPathEntry.isReexported());
    }
}
