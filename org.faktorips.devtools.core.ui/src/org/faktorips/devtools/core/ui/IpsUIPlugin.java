/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ExtensionPoints;
import org.faktorips.devtools.core.ImageDescriptorRegistry;
import org.faktorips.devtools.core.IpsCompositeSaveParticipant;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.Messages;
import org.faktorips.devtools.core.internal.model.ipsobject.ArchiveIpsSrcFile;
import org.faktorips.devtools.core.internal.model.pctype.ProductRelevantIcon;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.controlfactories.BooleanControlFactory;
import org.faktorips.devtools.core.ui.controlfactories.DefaultControlFactory;
import org.faktorips.devtools.core.ui.controlfactories.EnumDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditFieldChangesBroadcaster;
import org.faktorips.devtools.core.ui.editors.IIpsObjectEditorSettings;
import org.faktorips.devtools.core.ui.editors.IpsArchiveEditorInput;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorSettings;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class IpsUIPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.faktorips.devtools.core.ui"; //$NON-NLS-1$

    /**
     * The simple extension point id of the extension point
     * <i>extensionPropertyEditFieldFactory</i>;
     */
    public final static String EXTENSION_POINT_ID_EXTENSION_PROPERTY_EDIT_FIELD_FACTORY = "extensionPropertyEditFieldFactory";

    // The shared instance
    private static IpsUIPlugin plugin;

    // Factories for creating controls depending on the datatype
    private ValueDatatypeControlFactory[] controlFactories;

    // Broadcaster for broadcasting delayed change events triggerd by edit fields
    private EditFieldChangesBroadcaster editFieldChangeBroadcaster;

    private IpsObjectEditorSettings ipsEditorSettings;

    // Manager to update ips problem marker
    private IpsProblemMarkerManager ipsProblemMarkerManager;

    private Map<String, IExtensionPropertyEditFieldFactory> extensionPropertyEditFieldFactoryMap;

    private IExtensionRegistry registry;

    // registry for image descriptors
    private ImageDescriptorRegistry imageDescriptorRegistry;

    /**
     * The constructor
     */
    public IpsUIPlugin() {
    }

    /**
     * This method is protected for test purposes only.
     */
    public void setExtensionRegistry(IExtensionRegistry registry) {
        this.registry = registry;
    }

    /**
     * {@inheritDoc}
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        registry = Platform.getExtensionRegistry();
        ipsEditorSettings = new IpsObjectEditorSettings();
        ipsEditorSettings.load(getStateLocation());
        IpsCompositeSaveParticipant saveParticipant = new IpsCompositeSaveParticipant();
        saveParticipant.addSaveParticipant(ipsEditorSettings);
        ResourcesPlugin.getWorkspace().addSaveParticipant(this, saveParticipant);
        controlFactories = new ValueDatatypeControlFactory[] {
                new BooleanControlFactory(IpsPlugin.getDefault().getIpsPreferences()),
                new EnumDatatypeControlFactory(), new DefaultControlFactory() };
    }

    /**
     * {@inheritDoc}
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
        if (imageDescriptorRegistry != null) {
            imageDescriptorRegistry.dispose();
        }
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static IpsUIPlugin getDefault() {
        return plugin;
    }

    /**
     * Returns the settings for ips object editors.
     * 
     * @return
     */
    public IIpsObjectEditorSettings getIpsEditorSettings() {
        return ipsEditorSettings;
    }

    /**
     * Returns a control factory that can create controls (and edit fields) for the given datatype.
     * Returns a default factory if datatype is <code>null</code>.
     * 
     * @throws RuntimeException if no factory is found for the given datatype.
     */
    public ValueDatatypeControlFactory getValueDatatypeControlFactory(ValueDatatype datatype) {
        ValueDatatypeControlFactory[] factories = getValueDatatypeControlFactories();
        for (int i = 0; i < factories.length; i++) {
            if (factories[i].isFactoryFor(datatype)) {
                return factories[i];
            }
        }
        throw new RuntimeException(Messages.IpsPlugin_errorNoDatatypeControlFactoryFound + datatype);
    }

    /**
     * Returns all controls factories.
     */
    // TODO control factories sollten ueber einen extension point definiert sein und geladen werden.
    private ValueDatatypeControlFactory[] getValueDatatypeControlFactories() {
        return controlFactories;
    }

    /**
     * Returns a factory for creating table format controls/widgets.
     * 
     * @param tableFormat ITableFormat to test whether it has custom properties.
     * @return A Factory class which can be used to create the controls for configuring 
     * the custom properties, or <code>null</code> if the table format has no custom properties or
     * the class could not be created.
     */
    public TableFormatConfigurationCompositeFactory getTableFormatPropertiesControlFactory(ITableFormat tableFormat) {
        IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
                "org.faktorips.devtools.core.externalTableFormat"); //$NON-NLS-1$
        
        TableFormatConfigurationCompositeFactory compositeFactory = null;
        for (int i = 0; i < elements.length; i++) {
            try {
                ITableFormat format = (ITableFormat)elements[i].createExecutableExtension("class"); //$NON-NLS-1$
                if (format.getClass().equals(tableFormat.getClass())) {
                    // Found the given tableFormat in declared extensions
                    compositeFactory = (TableFormatConfigurationCompositeFactory)elements[i].createExecutableExtension("guiClass"); //$NON-NLS-1$
                    compositeFactory.setTableFormat(tableFormat);
                    break;
                }
            } catch (Exception e) {
                IpsPlugin.log(e);
            }
        }
    
        return compositeFactory;
    }
    
    /**
     * Test if the given table format class has custom properties. 
     * This method returns true if the optional <code>guiClass</code> property is defined for the externalTableFormat
     * extension belonging to the given table format and if the guiClass can be instantiated.
     * 
     * @param tableFormat ITableFormat to test whether it has custom properties.
     * @return true if the given table format has custom properties, false otherwise.
     */
    public boolean hasTableFormatCustomProperties(ITableFormat tableFormat) {
        return (getTableFormatPropertiesControlFactory(tableFormat) != null);
    }
    
    /**
     * Opens the given IpsObject in its editor.<br>
     * Returns the editor part of the opened editor. Returns <code>null</code> if no editor was
     * opened.
     * 
     * @param ipsObject
     */
    public IEditorPart openEditor(IIpsObject ipsObject) {
        if (ipsObject == null) {
            return null;
        }
        return openEditor(ipsObject.getIpsSrcFile());
    }

    /**
     * Opens an editor for the IpsObject contained in the given IpsSrcFile.<br>
     * Returns the editor part of the opened editor. Returns <code>null</code> if no editor was
     * opened.
     * 
     * @param srcFile
     */
    public IEditorPart openEditor(IIpsSrcFile srcFile) {
        if (srcFile == null) {
            return null;
        }
        if (srcFile instanceof ArchiveIpsSrcFile) {
            IWorkbench workbench = IpsPlugin.getDefault().getWorkbench();
            IEditorDescriptor editor = workbench.getEditorRegistry().getDefaultEditor(srcFile.getName());
            IpsArchiveEditorInput input = new IpsArchiveEditorInput(srcFile);
            try {
                IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
                if (page == null) {
                    return null;
                }
                return page.openEditor(input, editor.getId());
            } catch (PartInitException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        } else {
            return openEditor(srcFile.getCorrespondingFile());
        }
        return null;
    }

    /**
     * Opens the file referenced by the given IFile in an editor. The type of editor to be opened is
     * derived from the file-extension using the editor-registry. If no entry is existent, the
     * workbench opens the default editor as defined in the preferences/file-associations. If none
     * is specified the workbench guesses the filetype by looking at the file's content and opens
     * the corresponding editor.
     * <p>
     * <code>IFile</code>s containing <code>IpsSrcFiles</code>s and thus <code>IpsObject</code>s are
     * always opened in the corresponding IpsObjectEditor.
     * <p>
     * Returns the editor part of the opened editor. Returns <code>null</code> if no editor was
     * opened.
     * 
     * @see IDE#openEditor(org.eclipse.ui.IWorkbenchPage, org.eclipse.core.resources.IFile)
     * @param fileToEdit
     */
    public IEditorPart openEditor(IFile fileToEdit) {
        if (fileToEdit == null) {
            return null;
        }
        // check if the file can be edit with a corresponding ips object editor,
        // if the file is outside an ips package then the ips object editor couldn't be used
        // - the ips object could not be retrieved from the ips src file -
        // therefore open the default text editor (to edit the ips src file as xml)
        IIpsModel model = IpsPlugin.getDefault().getIpsModel();
        IIpsElement ipsElement = model.getIpsElement(fileToEdit);
        if (ipsElement instanceof IIpsSrcFile && !((IIpsSrcFile)ipsElement).exists()) {
            try {
                openWithDefaultIpsSrcTextEditor(fileToEdit);
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        } else {
            return openEditor(new FileEditorInput(fileToEdit));
        }

        return null;
    }

    /**
     * Opens an editor for the given file editor input.<br>
     * Returns the editor part of the opened editor. Returns <code>null</code> if no editor was
     * opened.
     * 
     * @see IpsPlugin#openEditor(IFile)
     * 
     * @param fileEditorInput
     */
    public IEditorPart openEditor(IFileEditorInput editorInput) {
        try {
            IFile file = editorInput.getFile();
            IWorkbench workbench = IpsPlugin.getDefault().getWorkbench();
            /*
             * For known filetypes always use the registered editor, NOT the editor specified by the
             * preferences/file-associations. This ensures that, when calling this method,
             * IpsObjects are always opened in their IpsObjectEditor and never in an xml-editor
             * (which might be the default editor for the given file).
             */
            IEditorDescriptor editor = workbench.getEditorRegistry().getDefaultEditor(file.getName());
            if (editor != null & editorInput != null) {
                return workbench.getActiveWorkbenchWindow().getActivePage().openEditor(editorInput, editor.getId());
            } else {
                /*
                 * For unknown files let IDE open the corresponding editor. This method searches the
                 * preferences/file-associations for an editor (default editor) and if none is found
                 * guesses the filetype by looking at the contents of the given file.
                 */
                return IDE.openEditor(workbench.getActiveWorkbenchWindow().getActivePage(), file, true, true);
            }
        } catch (PartInitException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return null;
    }

    /**
     * Returns the image with the indicated name from the <code>icons</code> folder and overlays
     * it with the product relevant image. If the given image is not found return the missing image
     * overlaid with the product relevant image.
     * 
     * @see IpsPlugin#getImage(String)
     * 
     * @param baseImageName The name of the image which will be overlaid with the product relevant
     *            image.
     */
    public Image getProductRelevantImage(String baseImageName) {
        String overlayedImageName = "ProductRelevantOverlay.gif_" + baseImageName; //$NON-NLS-1$
        Image image = getImageRegistry().get(overlayedImageName);
        if (image == null) {
            image = ProductRelevantIcon.createProductRelevantImage(getImage(baseImageName));
            ImageDescriptor imageDescriptor = ImageDescriptor.createFromImage(image);
            getImageRegistry().put(overlayedImageName, imageDescriptor);
        }
        return image;
    }

    /*
     * Open the given file with the default text editor. And show an information message in the
     * editors status bar to inform the user about using the text editor instead of the ips object
     * editor.
     */
    private IEditorPart openWithDefaultIpsSrcTextEditor(IFile fileToEdit) throws CoreException {
        String defaultContentTypeOfIpsSrcFilesId = "org.faktorips.devtools.core.ipsSrcFile"; //$NON-NLS-1$
        IWorkbench workbench = IpsPlugin.getDefault().getWorkbench();
        IFileEditorInput editorInput = new FileEditorInput(fileToEdit);

        IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
        IContentType contentType = contentTypeManager.getContentType(defaultContentTypeOfIpsSrcFilesId);

        IEditorDescriptor[] editors = workbench.getEditorRegistry().getEditors("", contentType); //$NON-NLS-1$
        if (editors.length != 1) {
            throw new CoreException(new IpsStatus(NLS.bind(
                    "No registered editors (or more then one) for content-type id {0} found!", //$NON-NLS-1$
                    defaultContentTypeOfIpsSrcFilesId)));
        }
        try {
            IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
            if (page == null) {
                return null;
            }
            IEditorPart editorPart = page.openEditor(editorInput, editors[0].getId());
            if (editorPart == null) {
                throw new CoreException(new IpsStatus("Error opening the default text editor!!")); //$NON-NLS-1$
            }
            // show information in the status bar about using the default text editor instead of
            // using the default ips object editor
            ((IEditorSite)editorPart.getSite()).getActionBars().getStatusLineManager().setMessage(
                    IpsUIPlugin.getDefault().getImage("size8/InfoMessage.gif"), //$NON-NLS-1$
                    Messages.IpsPlugin_infoDefaultTextEditorWasOpened);
            return editorPart;
        } catch (PartInitException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

        return null;
    }

    /**
     * Returns the edit field change broadcaster.
     */
    public EditFieldChangesBroadcaster getEditFieldChangeBroadcaster() {
        if (editFieldChangeBroadcaster == null) {
            editFieldChangeBroadcaster = new EditFieldChangesBroadcaster();
        }
        return editFieldChangeBroadcaster;
    }

    /**
     * Returns the ips problem marker manager which manages ips marker updates.
     */
    public IpsProblemMarkerManager getIpsProblemMarkerManager() {
        if (ipsProblemMarkerManager == null) {
            ipsProblemMarkerManager = new IpsProblemMarkerManager();
        }
        return ipsProblemMarkerManager;
    }

    /**
     * Returns the registered {@link IExtensionPropertyEditFieldFactory} for the provided
     * propertyId. If no factory is explicitly registered for the provided <code>propertyId</code> a
     * {@link DefaultExtensionPropertyEditFieldFactory} will be associated with the propertyId and
     * returned.
     * 
     * @param propertyId the id that identifies an extension property. For it the edit field factory
     *            will be returned.
     * @throws CoreException
     */
    public IExtensionPropertyEditFieldFactory getExtensionPropertyEditFieldFactory(String propertyId)
            throws CoreException {
        if (extensionPropertyEditFieldFactoryMap == null) {
            extensionPropertyEditFieldFactoryMap = new HashMap<String, IExtensionPropertyEditFieldFactory>();
            ExtensionPoints extensionPoints = new ExtensionPoints(registry, IpsUIPlugin.PLUGIN_ID);
            IExtension[] extensions = extensionPoints
                    .getExtension(EXTENSION_POINT_ID_EXTENSION_PROPERTY_EDIT_FIELD_FACTORY);
            for (int i = 0; i < extensions.length; i++) {
                IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
                if (configElements.length > 0) {
                    String configElPropertyId = configElements[0].getAttribute("propertyId");
                    if (StringUtils.isEmpty(configElPropertyId)) {
                        throw new CoreException(new IpsStatus(IStatus.ERROR,
                                "A problem occured while trying to load the extension: "
                                        + extensions[i].getExtensionPointUniqueIdentifier()
                                        + ". The attribute propertyId is not specified."));
                    }
                    IExtensionPropertyEditFieldFactory factory = (IExtensionPropertyEditFieldFactory)ExtensionPoints
                            .createExecutableExtension(extensions[i], configElements[0], "class",
                                    IExtensionPropertyEditFieldFactory.class);
                    if(factory != null){
                        extensionPropertyEditFieldFactoryMap.put(configElPropertyId, factory);
                    }
                }
            }
        }
        IExtensionPropertyEditFieldFactory factory = extensionPropertyEditFieldFactoryMap.get(propertyId);
        if (factory == null) {
            factory = new DefaultExtensionPropertyEditFieldFactory();
            extensionPropertyEditFieldFactoryMap.put(propertyId, factory);
        }
        return factory;
    }

    public Image getImage(ImageDescriptor descriptor) {
        return getImageDescriptorRegistry().get(descriptor);
    }

    /**
     * Returns the image with the indicated name from the <code>icons</code> folder. If no image
     * with the indicated name is found, a missing image is returned.
     * 
     * @param name The image name, e.g. <code>IpsProject.gif</code>
     */
    public Image getImage(String name) {
        return getImage(name, false);
    }

    /**
     * Returns the image with the indicated name from the <code>icons</code> folder. If no image
     * is found and <code>returnNull</code> is true, null is returned. Otherwise (no image found,
     * but <code>returnNull</code> is false), the missing image is returned.
     * 
     * @param name The name of the image.
     * @param returnNull <code>true</code> to get null as return value if the image is not found,
     *            <code>false</code> to get the missing image in this case.
     */
    public Image getImage(String name, boolean returnNull) {
        Image image = getImageRegistry().get(name);
        if (image == null) {
            URL url = getBundle().getEntry("icons/" + name); //$NON-NLS-1$
            if (url == null && returnNull) {
                return null;
            }
            ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
            getImageRegistry().put(name, descriptor);
            image = getImageRegistry().get(name);
        }
        return image;
    }

    public ImageDescriptor getImageDescriptor(String name) {
        URL url = getBundle().getEntry("icons/" + name); //$NON-NLS-1$
        return ImageDescriptor.createFromURL(url);
    }

    private ImageDescriptorRegistry getImageDescriptorRegistry() {
        // must use lazy initialization, as the current display is not necessarily
        // available when the plugin is started.
        if (this.imageDescriptorRegistry == null) {
            imageDescriptorRegistry = new ImageDescriptorRegistry(Display.getCurrent());
        }
        return imageDescriptorRegistry;
    }
}
