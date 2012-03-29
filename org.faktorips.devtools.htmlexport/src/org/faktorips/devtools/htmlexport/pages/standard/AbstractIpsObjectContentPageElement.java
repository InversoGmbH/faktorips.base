/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.standard;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.helper.path.HtmlPathFactory;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractStandardTablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.IpsObjectMessageListTablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.KeyValueTablePageElement;
import org.faktorips.util.message.MessageList;

/**
 * <p>
 * The AbstractObjectContentPageElement represents a complete Page for an {@link IIpsObject}. Use
 * the {@link ContentPageUtil} to choose the right subclass.
 * </p>
 * 
 * @author dicker
 * 
 */
public abstract class AbstractIpsObjectContentPageElement<T extends IIpsObject> extends AbstractRootPageElement {

    private T documentedIpsObject;
    private final DocumentationContext context;

    /**
     * creates a page, which represents the given documentedIpsObject according to the given context
     * 
     */
    protected AbstractIpsObjectContentPageElement(T documentedIpsObject, DocumentationContext context) {
        this.documentedIpsObject = documentedIpsObject;
        this.context = context;
        setTitle(context.getLabel(documentedIpsObject));
    }

    @Override
    public void build() {
        super.build();

        addPageElements(new WrapperPageElement(WrapperType.BLOCK, new LinkPageElement("index", TargetType.OVERALL, //$NON-NLS-1$ 
                getContext().getMessage(HtmlExportMessages.AbstractObjectContentPageElement_overviewProject)
                        + " " + getContext().getIpsProject().getName()))); //$NON-NLS-1$

        addPageElements(new PageElementUtils().createLinkPageElement(getContext(), getDocumentedIpsObject()
                .getIpsPackageFragment(), TargetType.CLASSES, IpsUIPlugin.getLabel(getDocumentedIpsObject()
                .getIpsPackageFragment()), true));
        addPageElements(new TextPageElement(getIpsObjectTypeDisplayName() + " " //$NON-NLS-1$
                + context.getLabel(getDocumentedIpsObject()), TextType.HEADING_1));

        addTypeHierarchy();

        addPageElements(new TextPageElement(context.getLabel(getDocumentedIpsObject()), TextType.HEADING_2));

        addStructureData();

        if (!getDocumentedIpsObject().getIpsProject().equals(getContext().getIpsProject())) {
            addPageElements(TextPageElement.createParagraph(getContext().getMessage(
                    "AbstractObjectContentPageElement_project") + ": " //$NON-NLS-1$ //$NON-NLS-2$
                    + getDocumentedIpsObject().getIpsProject().getName()));
        }
        addPageElements(TextPageElement.createParagraph(getContext().getMessage(
                "AbstractObjectContentPageElement_projectFolder") + ": " //$NON-NLS-1$ //$NON-NLS-2$
                + getDocumentedIpsObject().getIpsSrcFile().getIpsPackageFragment()));

        addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.AbstractObjectContentPageElement_description), TextType.HEADING_2));
        addPageElements(new TextPageElement(
                StringUtils.isBlank(getContext().getDescription(getDocumentedIpsObject())) ? getContext().getMessage(
                        "AbstractObjectContentPageElement_noDescription") : getContext().getDescription( //$NON-NLS-1$
                        getDocumentedIpsObject()), TextType.BLOCK));

        if (getContext().showsValidationErrors()) {
            addValidationErrorsTable();
        }

        addExtensionPropertiesTable();
    }

    /**
     * Fetches the displayName of the IpsObjectType
     * <p>
     * The method getDisplayName is not useful, because it depends on the platform language and not
     * the chosen one.
     * <p>
     * If the IpsObjectType of the documented IpsObject is a subclass of IpsObjectType the method
     * getDisplayName is used with the possibility of using a wrong language
     * 
     */
    private String getIpsObjectTypeDisplayName() {
        String id = getDocumentedIpsObject().getIpsObjectType().getId();
        String messageId = "IpsObjectType_name" + id; //$NON-NLS-1$
        String message = getContext().getMessage(messageId);
        if (StringUtils.isNotBlank(message) && !messageId.equals(message)) {
            return message;
        }
        return getDocumentedIpsObject().getIpsObjectType().getDisplayName();
    }

    /**
     * adds a table with all validation messages of the {@link IpsObject}. Nothing will be shown, if
     * there are no messages.
     */
    private void addValidationErrorsTable() {

        MessageList messageList = new MessageList();

        try {
            messageList = getDocumentedIpsObject().validate(getDocumentedIpsObject().getIpsProject());
        } catch (CoreException e) {
            context.addStatus(new IpsStatus(IStatus.ERROR, "Error validating " //$NON-NLS-1$
                    + getDocumentedIpsObject().getQualifiedName(), e));
        }

        if (messageList.isEmpty()) {
            return;
        }

        AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                "AbstractObjectContentPageElement_validationErrors"), TextType.HEADING_2)); //$NON-NLS-1$

        TablePageElement tablePageElement = new IpsObjectMessageListTablePageElement(messageList, getContext());

        wrapper.addPageElements(tablePageElement);

        addPageElements(wrapper);

    }

    /**
     * adds {@link IPageElement}s for structural data like fitting ProductCmpt for a PolicyCmptType
     */
    protected void addStructureData() {
        // could be overridden
    }

    /**
     * adds {@link IPageElement}s for hierarchical data like super- and subclasses
     */
    protected void addTypeHierarchy() {
        // could be overridden
    }

    @Override
    public String getPathToRoot() {
        return HtmlPathFactory.createPathUtil(getDocumentedIpsObject()).getPathToRoot();
    }

    /**
     * returns the given table or the given alternative text, if the table is empty
     * 
     */
    IPageElement getTableOrAlternativeText(AbstractStandardTablePageElement tablePageElement, String alternativeText) {
        if (tablePageElement == null || tablePageElement.isEmpty()) {
            return new TextPageElement(alternativeText);
        }
        return tablePageElement;
    }

    /**
     * returns the documentedIpsObject
     * 
     */
    protected T getDocumentedIpsObject() {
        return documentedIpsObject;
    }

    /**
     * returns the context
     * 
     */
    protected DocumentationContext getContext() {
        return context;
    }

    @Override
    protected void createId() {
        setId(documentedIpsObject.getQualifiedName());
    }

    protected void addExtensionPropertiesTable() {
        IExtensionPropertyDefinition[] properties = getDocumentedIpsObject().getIpsModel()
                .getExtensionPropertyDefinitions(getDocumentedIpsObject().getClass(), true);

        if (ArrayUtils.isEmpty(properties)) {
            return;
        }

        KeyValueTablePageElement extensionPropertiesTable = new KeyValueTablePageElement(getContext(), getContext()
                .getMessage("AbstractIpsObjectContentPageElement_extensionPropertyKeyHeadline"), getContext() //$NON-NLS-1$
                .getMessage("AbstractIpsObjectContentPageElement_extensionPropertyValueHeadline")); //$NON-NLS-1$

        for (IExtensionPropertyDefinition iExtensionPropertyDefinition : properties) {
            Object extPropertyValue = getDocumentedIpsObject().getExtPropertyValue(
                    iExtensionPropertyDefinition.getPropertyId());
            extensionPropertiesTable.addKeyValueRow(iExtensionPropertyDefinition.getName(),
                    extPropertyValue == null ? null : extPropertyValue.toString());
        }

        AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                "AbstractIpsObjectContentPageElement_extensionProperties"), TextType.HEADING_2)); //$NON-NLS-1$

        wrapper.addPageElements(extensionPropertiesTable);

        addPageElements(wrapper);

    }

    @Override
    public boolean isContentUnit() {
        return true;
    }
}
