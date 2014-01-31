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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsProjectRefEntryTest extends AbstractIpsPluginTest {

    private static final String MY_RESOURCE_PATH = "myResourcePath";

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
    public void testFindIpsSrcFiles() throws Exception {
        IpsProject refProject = (IpsProject)newIpsProject("RefProject");
        IPolicyCmptType a = newPolicyCmptTypeWithoutProductCmptType(refProject, "a.A");
        IPolicyCmptType b = newPolicyCmptTypeWithoutProductCmptType(refProject, "a.B");

        IPolicyCmptType c = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "b.C");

        path = (IpsObjectPath)ipsProject.getIpsObjectPath();
        IpsProjectRefEntry entry = (IpsProjectRefEntry)path.newIpsProjectRefEntry(refProject);

        ArrayList<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        Set<IIpsObjectPathEntry> visitedEntries = new HashSet<IIpsObjectPathEntry>();
        entry.findIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE, result, visitedEntries);

        assertTrue(result.contains(a.getIpsSrcFile()));
        assertTrue(result.contains(b.getIpsSrcFile()));
        assertFalse(result.contains(c.getIpsSrcFile()));
    }

    @Test
    public void testFindIpsSrcFilesWithPackageFragment() throws Exception {
        IpsProject refProject = (IpsProject)newIpsProject("RefProject");

        // policy cmpt types in ref project
        IPolicyCmptType a = newPolicyCmptTypeWithoutProductCmptType(refProject, "a.b.c.A");
        IPolicyCmptType b = newPolicyCmptTypeWithoutProductCmptType(refProject, "a.b.c.B");

        // policy cmpt types in original project
        IPolicyCmptType c = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.c.C");

        // policy cmpt types in ref project
        IPolicyCmptType a2 = newPolicyCmptTypeWithoutProductCmptType(refProject, "a.b.d.A");
        IPolicyCmptType b2 = newPolicyCmptTypeWithoutProductCmptType(refProject, "a.b.d.B");

        // policy cmpt types in original project
        IPolicyCmptType c2 = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.d.C");

        path = (IpsObjectPath)ipsProject.getIpsObjectPath();
        IpsProjectRefEntry entry = (IpsProjectRefEntry)path.newIpsProjectRefEntry(refProject);

        ArrayList<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        entry.findIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE, "a.b.c", result, new HashSet<IIpsObjectPathEntry>());

        assertEquals(2, result.size());
        assertTrue(result.contains(a.getIpsSrcFile()));
        assertTrue(result.contains(b.getIpsSrcFile()));
        assertFalse(result.contains(c.getIpsSrcFile()));

        result = new ArrayList<IIpsSrcFile>();
        entry.findIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE, "a.b.d", result, new HashSet<IIpsObjectPathEntry>());

        assertEquals(2, result.size());
        assertTrue(result.contains(a2.getIpsSrcFile()));
        assertTrue(result.contains(b2.getIpsSrcFile()));
        assertFalse(result.contains(c2.getIpsSrcFile()));

    }

    @Test
    public void testInitFromXml() {
        Document doc = getTestDocument();
        IpsProjectRefEntry entry = new IpsProjectRefEntry(path);
        entry.initFromXml(doc.getDocumentElement(), ipsProject.getProject());
        assertEquals(IpsPlugin.getDefault().getIpsModel().getIpsProject("RefProject"), entry.getReferencedIpsProject());
        assertFalse(entry.isUseNWDITrackPrefix());
    }

    @Test
    public void testToXml() {
        IIpsProject refProject = IpsPlugin.getDefault().getIpsModel().getIpsProject("RefProject");
        IpsProjectRefEntry entry = new IpsProjectRefEntry(path, refProject);
        Element element = entry.toXml(newDocument());

        entry = new IpsProjectRefEntry(path);
        entry.initFromXml(element, ipsProject.getProject());
        assertEquals(refProject, entry.getReferencedIpsProject());
        assertFalse(entry.isUseNWDITrackPrefix());
    }

    @Test
    public void testValidate() throws CoreException {
        IIpsProjectProperties props = ipsProject.getProperties();
        IIpsObjectPath path = props.getIpsObjectPath();
        IIpsProject refProject = this.newIpsProject("TestProject2");
        path.newIpsProjectRefEntry(refProject);
        ipsProject.setProperties(props);

        MessageList ml = ipsProject.validate();
        assertEquals(0, ml.size());

        // validate missing project reference
        refProject = IpsPlugin.getDefault().getIpsModel().getIpsProject("none");
        path.newIpsProjectRefEntry(refProject);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertEquals(1, ml.size());
        assertNotNull(ml.getMessageByCode(IIpsObjectPathEntry.MSGCODE_MISSING_PROJECT));

        // validate empty project name
        path.removeProjectRefEntry(refProject);
        path.newIpsProjectRefEntry(null);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertEquals(1, ml.size());
        assertNotNull(ml.getMessageByCode(IIpsObjectPathEntry.MSGCODE_PROJECT_NOT_SPECIFIED));
    }

    @Test
    public void testContainsResource_true() throws Exception {
        IIpsProject referencedProject = mock(IIpsProject.class);
        when(referencedProject.containsResource(MY_RESOURCE_PATH)).thenReturn(true);
        IpsProjectRefEntry projectRefEntry = new IpsProjectRefEntry(path, referencedProject);

        assertTrue(projectRefEntry.containsResource(MY_RESOURCE_PATH));
    }

    @Test
    public void testContainsResource_false() throws Exception {
        IpsProjectRefEntry projectRefEntry = new IpsProjectRefEntry(path, ipsProject);

        assertFalse(projectRefEntry.containsResource(MY_RESOURCE_PATH));
    }

}
