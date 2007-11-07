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

package org.faktorips.devtools.core.internal.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IModificationStatusChangeListener;
import org.faktorips.devtools.core.model.ModificationStatusChangedEvent;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;

/**
 * Test for the listener support. 
 * 
 * @author Jan Ortmann
 */
public class IpsModelListenerTest extends AbstractIpsPluginTest {

    private TestContentChangeListener contentChangeListener;
    private TestModificationStatusChangeListener statusChangeListener;

    private IIpsProject project;
    private IPolicyCmptType type;
    private IIpsSrcFile file;
    
    public IpsModelListenerTest() {
        super();
    }

    public IpsModelListenerTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject();
        type = newPolicyCmptType(project, "Policy");
        file = type.getIpsSrcFile();
        file.save(true, null);
        
        contentChangeListener = new TestContentChangeListener();
        statusChangeListener = new TestModificationStatusChangeListener();
        IpsPlugin.getDefault().getIpsModel().addChangeListener(contentChangeListener);
        IpsPlugin.getDefault().getIpsModel().addModifcationStatusChangeListener(statusChangeListener);
        
    }
    
    protected void tearDown() {
        IpsPlugin.getDefault().getIpsModel().removeChangeListener(contentChangeListener);
        IpsPlugin.getDefault().getIpsModel().removeModificationStatusChangeListener(statusChangeListener);
    }
    
    public void testChangeIpsObjectProperty() throws CoreException {
        type.setSupertype("Super");
        assertEquals(1, contentChangeListener.count);
        assertEquals(1, statusChangeListener.count);
        assertEquals(file, statusChangeListener.lastEvent.getIpsSrcFile());
        assertEquals(file, contentChangeListener.lastEvent.getIpsSrcFile());
        assertNull(contentChangeListener.lastEvent.getPart());
        assertEquals(0, contentChangeListener.lastEvent.getMovedParts().length);
        
        type.setSupertype("NewSuper");
        assertEquals(2, contentChangeListener.count);
        assertEquals(1, statusChangeListener.count);
    }
    
    public void testChangeIpsPartProperty() throws CoreException {
        IPolicyCmptTypeAttribute attribute = type.newPolicyCmptTypeAttribute();
        file.save(true, null);
        
        contentChangeListener.count = 0;
        statusChangeListener .count = 0;
        
        attribute.setName("newName");
        assertEquals(1, contentChangeListener.count);
        assertEquals(1, statusChangeListener.count);
        assertEquals(file, statusChangeListener.lastEvent.getIpsSrcFile());
        assertEquals(file, contentChangeListener.lastEvent.getIpsSrcFile());
        assertEquals(ContentChangeEvent.TYPE_PROPERTY_CHANGED, contentChangeListener.lastEvent.getEventType());
        assertEquals(attribute, contentChangeListener.lastEvent.getPart());
        assertEquals(0, contentChangeListener.lastEvent.getMovedParts().length);
        
        attribute.setName("NewerName");
        assertEquals(2, contentChangeListener.count);
        assertEquals(1, statusChangeListener.count);
    }

    public void testAddPart() throws CoreException {
        IPolicyCmptTypeAttribute attribute = type.newPolicyCmptTypeAttribute();
        
        assertEquals(1, contentChangeListener.count);
        assertEquals(1, statusChangeListener.count);
        assertEquals(file, statusChangeListener.lastEvent.getIpsSrcFile());
        assertEquals(file, contentChangeListener.lastEvent.getIpsSrcFile());
        assertEquals(ContentChangeEvent.TYPE_PART_ADDED, contentChangeListener.lastEvent.getEventType());
        assertEquals(attribute, contentChangeListener.lastEvent.getPart());
        assertEquals(0, contentChangeListener.lastEvent.getMovedParts().length);
        
        type.newPolicyCmptTypeAttribute();
        assertEquals(2, contentChangeListener.count);
        assertEquals(1, statusChangeListener.count);
    }

    public void testDeletePart() throws CoreException {
        IPolicyCmptTypeAttribute attribute1 = type.newPolicyCmptTypeAttribute();
        IPolicyCmptTypeAttribute attribute2 = type.newPolicyCmptTypeAttribute();
        file.save(true, null);
        contentChangeListener.count = 0;
        statusChangeListener .count = 0;

        attribute1.delete();
        assertEquals(1, contentChangeListener.count);
        assertEquals(1, statusChangeListener.count);
        assertEquals(file, statusChangeListener.lastEvent.getIpsSrcFile());
        assertEquals(file, contentChangeListener.lastEvent.getIpsSrcFile());
        assertEquals(ContentChangeEvent.TYPE_PART_REMOVED, contentChangeListener.lastEvent.getEventType());
        assertEquals(attribute1, contentChangeListener.lastEvent.getPart());
        assertEquals(0, contentChangeListener.lastEvent.getMovedParts().length);
        
        attribute2.delete();
        assertEquals(2, contentChangeListener.count);
        assertEquals(1, statusChangeListener.count);
    }
    
    public void testChangeCorrespondigResource() throws Exception {
        IFile ioFile = file.getCorrespondingFile();
        InputStream is = file.getContentFromEnclosingResource();
        
        ContentsChangeListener listener = new ContentsChangeListener() {
            
            public void contentsChanged(ContentChangeEvent event) {
                assertEquals(file, contentChangeListener.lastEvent.getIpsSrcFile());
                assertEquals(ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED, contentChangeListener.lastEvent.getEventType());
                assertNull(contentChangeListener.lastEvent.getPart());
                assertEquals(0, contentChangeListener.lastEvent.getMovedParts().length);
                IpsPlugin.getDefault().getIpsModel().removeChangeListener(this);
            }
            
        };
        IpsPlugin.getDefault().getIpsModel().addChangeListener(listener);
        ioFile.setContents(is, true, false, null);
    }
    
    public void testStopRestartBroadcasting() throws Exception {
        type.setSupertype("Super");
        assertEquals(1, contentChangeListener.count);
        assertEquals(1, statusChangeListener.count);
        assertEquals(file, statusChangeListener.lastEvent.getIpsSrcFile());
        assertEquals(file, contentChangeListener.lastEvent.getIpsSrcFile());
        assertNull(contentChangeListener.lastEvent.getPart());
        assertEquals(0, contentChangeListener.lastEvent.getMovedParts().length);
        file.save(true, null);
        
        IpsModel model = (IpsModel)file.getIpsModel();
        model.stopBroadcastingChangesMadeByCurrentThread();
        model.stopBroadcastingChangesMadeByCurrentThread();
        model.resumeBroadcastingChangesMadeByCurrentThread();
        contentChangeListener.count = 0;
        statusChangeListener .count = 0;
        type.setSupertype("NewSuper");
        assertEquals(0, contentChangeListener.count);
        assertEquals(0, statusChangeListener.count);
        file.save(true, null);
        
        model.resumeBroadcastingChangesMadeByCurrentThread();
        contentChangeListener.count = 0;
        statusChangeListener .count = 0;
        type.setSupertype("NewerSuper");
        assertEquals(1, contentChangeListener.count);
        assertEquals(1, statusChangeListener.count);
    }

    class TestContentChangeListener implements ContentsChangeListener {

        int count = 0;
        ContentChangeEvent lastEvent;

        public void contentsChanged(ContentChangeEvent event) {
            lastEvent = event;
            count++;
        }
        
    }

    class TestModificationStatusChangeListener implements IModificationStatusChangeListener {

        int count = 0;
        ModificationStatusChangedEvent lastEvent;
        
        /**
         * {@inheritDoc}
         */
        public void modificationStatusHasChanged(ModificationStatusChangedEvent event) {
            lastEvent = event;
            count++;
        }
        
    }
}
