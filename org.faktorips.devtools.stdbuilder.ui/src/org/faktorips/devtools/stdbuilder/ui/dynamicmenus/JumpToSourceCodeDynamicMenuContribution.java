/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.ui.dynamicmenus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.IServiceLocator;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.JavaNamingConvention;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

/**
 * A dynamic menu contribution that consists of commands that allow the user to directly jump to the
 * Java source generated for the selected {@link IIpsObjectPartContainer}.
 * 
 * @author Alexander Weickmann
 */
public class JumpToSourceCodeDynamicMenuContribution extends CompoundContributionItem implements IWorkbenchContribution {

    private static final String COMMAND_ID_OPEN_ELEMENT_IN_EDITOR = "org.eclipse.jdt.ui.commands.openElementInEditor";

    private static final String COMMAND_ID_NO_SOURCE_CODE_FOUND = "org.faktorips.devtools.stdbuilder.ui.commands.NoSourceCodeFound";

    private static final String PARAMETER_ID_ELEMENT_REF = "elementRef";

    private IServiceLocator serviceLocator;

    private StandardBuilderSet builderSet;

    @Override
    public void initialize(IServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    @Override
    protected IContributionItem[] getContributionItems() {
        IIpsElement selectedItem = getSelectedIpsElement();
        if (selectedItem instanceof IIpsSrcFile) {
            try {
                selectedItem = ((IIpsSrcFile)selectedItem).getIpsObject();
            } catch (CoreException e) {
                /*
                 * Recover from exception: If the IPS Object cannot be extracted from the source
                 * file we log the exception and show the error to the user. Then, the situation is
                 * treated as if there was no source code found.
                 */
                IpsPlugin.logAndShowErrorDialog(e);
                return getContributionItemsForNoSourceCodeFound();
            }
        }

        if (!(selectedItem instanceof IIpsObjectPartContainer)) {
            return getContributionItemsForNoSourceCodeFound();
        }

        IIpsObjectPartContainer ipsObjectPartContainer = (IIpsObjectPartContainer)selectedItem;
        builderSet = (StandardBuilderSet)ipsObjectPartContainer.getIpsProject().getIpsArtefactBuilderSet();

        return getContributionItemsForIpsObjectPartContainer(ipsObjectPartContainer);
    }

    private IContributionItem[] getContributionItemsForNoSourceCodeFound() {
        List<IContributionItem> contributionItems = new ArrayList<IContributionItem>(1);
        IContributionItem noSourceCodeFoundCommand = createNoSourceCodeFoundCommand();
        contributionItems.add(noSourceCodeFoundCommand);
        return contributionItems.toArray(new IContributionItem[1]);
    }

    private IContributionItem[] getContributionItemsForIpsObjectPartContainer(IIpsObjectPartContainer ipsObjectPartContainer) {
        /*
         * Obtain the Java types and their members which are generated for the IPS Object Part
         * Container.
         */
        Map<IType, Set<IMember>> javaTypesToJavaElements = getJavaTypesToJavaElementsMap(ipsObjectPartContainer);

        /*
         * Go over all types (that are either generated or parent of a generated member) and add an
         * "Open in Java Editor" command contribution item for each type itself as well as its
         * members.
         */
        List<IContributionItem> contributionItems = new ArrayList<IContributionItem>(javaTypesToJavaElements.size() * 3);
        List<IType> sortedJavaTypes = sortAndCheckTypes(javaTypesToJavaElements.keySet());
        for (int i = 0; i < sortedJavaTypes.size(); i++) {
            IType type = sortedJavaTypes.get(i);
            IMenuManager typeMenu = createTypeMenu(type, javaTypesToJavaElements.get(type));
            contributionItems.add(typeMenu);
        }

        if (contributionItems.isEmpty()) {
            return getContributionItemsForNoSourceCodeFound();
        } else {
            return contributionItems.toArray(new IContributionItem[contributionItems.size()]);
        }
    }

    private Map<IType, Set<IMember>> getJavaTypesToJavaElementsMap(IIpsObjectPartContainer ipsObjectPartContainer) {
        Map<IType, Set<IMember>> javaTypesToJavaElements = new LinkedHashMap<IType, Set<IMember>>(2);
        for (IJavaElement javaElement : builderSet.getGeneratedJavaElements(ipsObjectPartContainer)) {
            IType type = null;
            if (javaElement instanceof IType) {
                type = (IType)javaElement;
                addTypeIfNotPresent(javaTypesToJavaElements, type);
            } else if (javaElement instanceof IMember) {
                type = (IType)javaElement.getParent();
                addTypeIfNotPresent(javaTypesToJavaElements, type);
                Set<IMember> members = javaTypesToJavaElements.get(type);
                members.add((IMember)javaElement);
            } else {
                throw new RuntimeException("Unknown Java type.");
            }
        }
        return javaTypesToJavaElements;
    }

    private void addTypeIfNotPresent(Map<IType, Set<IMember>> javaTypesToJavaElements, IType type) {
        if (!(javaTypesToJavaElements.containsKey(type))) {
            javaTypesToJavaElements.put(type, new LinkedHashSet<IMember>());
        }
    }

    /**
     * Takes a set of {@link IType}s as input and creates / returns a sorted version of it.
     * <p>
     * The sorting algorithm ensures that first stands an interface and immediately thereafter the
     * associated implementation (if there is one). Thereby it takes the used
     * {@link JavaNamingConvention} into account. Here is an example:
     * <ol>
     * <li>IPolicy
     * <li>Policy
     * <li>IProduct
     * <li>Product
     * </ol>
     * <p>
     * Furthermore this method ensures that each type of the returned set actually exists. This way
     * it is not necessary to check the returned types for existence. However, existence of members
     * still needs to be checked.
     */
    private List<IType> sortAndCheckTypes(Set<IType> javaTypes) {
        List<IType> sortedTypes = new ArrayList<IType>(javaTypes.size());
        for (IType type : javaTypes) {
            if (isInterfaceType(type)) {
                if (type.exists()) {
                    sortedTypes.add(type);
                }
                IType implementation = getImplementationForInterface(javaTypes, type);
                if (implementation != null && implementation.exists()) {
                    sortedTypes.add(implementation);
                }
            }
        }
        return sortedTypes;
    }

    /**
     * Checks whether the given Java type is an interface type.
     * <p>
     * In contrast to {@link IType#exists()} this method uses the {@link JavaNamingConvention} and
     * the type's name for the check. This way the type does not need to be accessed which should
     * slightly increase performance and avoid certain exceptions.
     */
    private boolean isInterfaceType(IType javaType) {
        return getJavaNamingConvention().isPublishedInterfaceName(javaType.getElementName());
    }

    private IType getImplementationForInterface(Set<IType> types, IType interfaceType) {
        String searchedTypeName = getJavaNamingConvention().getImplementationClassNameForPublishedInterfaceName(
                interfaceType.getElementName());
        for (IType type : types) {
            if (type.getElementName().equals(searchedTypeName)) {
                return type;
            }
        }
        return null;
    }

    private JavaNamingConvention getJavaNamingConvention() {
        return builderSet.getJavaNamingConvention();
    }

    private IIpsElement getSelectedIpsElement() {
        IEvaluationService evaluationService = (IEvaluationService)serviceLocator.getService(IEvaluationService.class);
        IStructuredSelection selection = (IStructuredSelection)evaluationService.getCurrentState().getVariable(
                ISources.ACTIVE_MENU_SELECTION_NAME);
        TypedSelection<IIpsElement> typedSelection = TypedSelection.create(IIpsElement.class, selection);
        return typedSelection.getElement();
    }

    /**
     * Creates a menu which represents the given {@link IType} and lists the set of it's
     * {@link IMember}.
     * <p>
     * Each member is represented by a command that allows the user to open that member in a Java
     * editor.
     */
    private IMenuManager createTypeMenu(IType type, Set<IMember> members) {
        IMenuManager typeMenu = new MenuManager(getJavaElementLabel(type), getJavaElementIcon(type), null);
        for (IMember member : members) {
            if (member.exists()) {
                IContributionItem openInJavaEditorCommand = createOpenInJavaEditorCommand(member);
                typeMenu.add(openInJavaEditorCommand);
            }
        }
        if (typeMenu.isEmpty()) {
            IContributionItem noSourceCodeFoundCommand = createNoSourceCodeFoundCommand();
            typeMenu.add(noSourceCodeFoundCommand);
        }
        return typeMenu;
    }

    private IContributionItem createOpenInJavaEditorCommand(IJavaElement javaElement) {
        Map<String, Object> arguments = new HashMap<String, Object>(1);
        arguments.put(PARAMETER_ID_ELEMENT_REF, javaElement);

        return createCommand(COMMAND_ID_OPEN_ELEMENT_IN_EDITOR, arguments, getJavaElementIcon(javaElement),
                getJavaElementLabel(javaElement));
    }

    private IContributionItem createNoSourceCodeFoundCommand() {
        return createCommand(COMMAND_ID_NO_SOURCE_CODE_FOUND, null, null, null);
    }

    private IContributionItem createCommand(String commandId,
            Map<String, Object> arguments,
            ImageDescriptor icon,
            String label) {

        // @formatter:off
        CommandContributionItemParameter itemParameter = new CommandContributionItemParameter(
                serviceLocator,                               // serviceLocator
                null,                                         // id
                commandId,                                    // commandId
                arguments,                                    // arguments
                icon,                                         // icon
                null,                                         // disabledIcon
                null,                                         // hoverIcon
                label,                                        // label
                null,                                         // mnemoic
                null,                                         // tooltip
                CommandContributionItem.STYLE_PUSH,           // style
                null,                                         // helpContextId
                false                                         // visibleEnabled
        );
        // @formatter:on

        return new CommandContributionItem(itemParameter);
    }

    private String getJavaElementLabel(IJavaElement javaElement) {
        IWorkbenchAdapter workbenchAdapter = (IWorkbenchAdapter)javaElement.getAdapter(IWorkbenchAdapter.class);
        return workbenchAdapter != null ? workbenchAdapter.getLabel(javaElement) : null;
    }

    private ImageDescriptor getJavaElementIcon(IJavaElement javaElement) {
        IWorkbenchAdapter workbenchAdapter = (IWorkbenchAdapter)javaElement.getAdapter(IWorkbenchAdapter.class);
        return workbenchAdapter != null ? workbenchAdapter.getImageDescriptor(javaElement) : null;
    }

}
