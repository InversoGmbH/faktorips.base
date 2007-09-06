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

package org.faktorips.devtools.core.internal.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IIpsSrcFileMemento;
import org.faktorips.devtools.core.model.IModificationStatusChangeListener;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.ModificationStatusChangedEvent;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.util.message.MessageList;


/**
 *
 */
public class IpsSrcFileTest extends AbstractIpsPluginTest implements IModificationStatusChangeListener, ContentsChangeListener {
    
    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot ipsRootFolder;
    private IIpsPackageFragment ipsFolder;
    private IIpsSrcFile parsableFile; // file with parsable contents
    private IPolicyCmptType policyCmptType;
    private IIpsSrcFile unparsableFile; // file with unparsable contents
    
    private ModificationStatusChangedEvent lastModStatusEvent;
    private ContentChangeEvent lastContentChangedEvent;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        ipsRootFolder = ipsProject.getIpsPackageFragmentRoots()[0];
        ipsFolder = ipsRootFolder.createPackageFragment("folder", true, null);
        
        parsableFile = ipsFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "ParsableFile", true, null);
        policyCmptType = (IPolicyCmptType)parsableFile.getIpsObject();
        unparsableFile = ipsFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE.getFileName("UnparsableFile"), "blabla", true, null);
        unparsableFile.getCorrespondingFile().setContents(new ByteArrayInputStream("Blabla".getBytes()), true, false, null);

        parsableFile.getIpsModel().addModifcationStatusChangeListener(this);
        parsableFile.getIpsModel().addChangeListener(this);
    }
    
    protected void tearDown() throws Exception {
        parsableFile.getIpsModel().removeModificationStatusChangeListener(this);
        parsableFile.getIpsModel().removeChangeListener(this);
    }
    
    public void testSave() throws IOException, CoreException {
        policyCmptType.newAttribute();
        assertTrue(parsableFile.isDirty());
        
        lastModStatusEvent = null;
        lastContentChangedEvent = null;
        parsableFile.save(true, null);
        assertFalse(parsableFile.isDirty());
        assertNull(lastContentChangedEvent);
        assertEquals(parsableFile, lastModStatusEvent.getIpsSrcFile());
    }
    
    public void testIsContentParsable() throws CoreException {
        assertFalse(unparsableFile.isContentParsable());
        assertTrue(parsableFile.isContentParsable());
    }

    public void testDiscardChanges_ParsableContents() throws Exception {
        IPolicyCmptType type = newPolicyCmptType(this.ipsProject, "Policy");
        IIpsSrcFile file = type.getIpsSrcFile();
        type.newAttribute();
        assertEquals(1, type.getNumOfAttributes());
        assertTrue(file.isDirty());
        type.setSupertype("UnknownType");
        MessageList list = type.validate();
        assertNotNull(list.getMessageByCode(IPolicyCmptType.MSGCODE_SUPERTYPE_NOT_FOUND));
        
        file.discardChanges();
        list = type.validate();
        assertNull(list.getMessageByCode(IPolicyCmptType.MSGCODE_SUPERTYPE_NOT_FOUND));
        
        type = (IPolicyCmptType)file.getIpsObject();
        assertEquals(0, type.getNumOfAttributes());
        assertFalse(file.isDirty());
    }

    public void testGetCorrespondingResource() {
        IResource resource = parsableFile.getCorrespondingResource();
        assertTrue(resource.exists());
        assertEquals(parsableFile.getName(), resource.getName());
    }

    public void testGetCorrespondingFile() {
        IFile file = parsableFile.getCorrespondingFile();
        assertTrue(file.exists());
        assertEquals(parsableFile.getName(), file.getName());
    }
    
    public void testGetIpsObject() throws CoreException {
        IIpsObject ipsObject = parsableFile.getIpsObject();
        assertNotNull(ipsObject);
        assertTrue(ipsObject.isFromParsableFile());
        
        ipsObject.setDescription("blabla");
        assertSame(ipsObject, parsableFile.getIpsObject());
        
        ipsObject = unparsableFile.getIpsObject();
        assertNotNull(ipsObject);
        assertFalse(ipsObject.isFromParsableFile());
        
        // change from unparsable to parsable
        InputStream is = parsableFile.getCorrespondingFile().getContents();
        unparsableFile.getCorrespondingFile().setContents(is, true, true, null);
        assertSame(ipsObject, unparsableFile.getIpsObject());
        assertTrue(ipsObject.isFromParsableFile());
        
        // otherway round
        unparsableFile.getCorrespondingFile().setContents(new ByteArrayInputStream("Blabla".getBytes()), true, true, null);
        assertSame(ipsObject, unparsableFile.getIpsObject());
        assertFalse(ipsObject.isFromParsableFile());
    }

    public void testGetElementName() {
        String expectedName = IpsObjectType.POLICY_CMPT_TYPE.getFileName("ParsableFile");
        assertEquals(expectedName, parsableFile.getName());
    }

    public void testGetParent() {
    }

    public void testGetChildren() throws CoreException {
        assertEquals(0, unparsableFile.getChildren().length);
        assertEquals(1, parsableFile.getChildren().length);
        assertEquals(parsableFile.getIpsObject(), parsableFile.getChildren()[0]);
    }

    public void testHasChildren() throws CoreException {
        assertFalse(unparsableFile.hasChildren());
        assertTrue(parsableFile.hasChildren());
    }
    
    public void testIsHistoric() {
        assertFalse(parsableFile.isHistoric());
    }

    public void testNewMemento() throws CoreException {
        policyCmptType.newAttribute();
        IIpsSrcFileMemento memento = parsableFile.newMemento();
        assertEquals(true, memento.isDirty());
        assertEquals(parsableFile, memento.getIpsSrcFile());
    }
    
    public void testSetMemento() throws CoreException {
        IIpsSrcFileMemento memento = parsableFile.newMemento();
        policyCmptType.newAttribute();
        parsableFile.setMemento(memento);
        assertEquals(0, policyCmptType.getNumOfAttributes());
        assertFalse(parsableFile.isDirty());
    }

    /**
     * {@inheritDoc}
     */
    public void modificationStatusHasChanged(ModificationStatusChangedEvent event) {
        lastModStatusEvent = event;
    }

    /**
     * {@inheritDoc}
     */
    public void contentsChanged(ContentChangeEvent event) {
        this.lastContentChangedEvent = event;
    }

}
