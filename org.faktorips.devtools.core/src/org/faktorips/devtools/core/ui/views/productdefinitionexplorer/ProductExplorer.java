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

package org.faktorips.devtools.core.ui.views.productdefinitionexplorer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.ui.actions.WrapperAction;
import org.faktorips.devtools.core.ui.views.modelexplorer.ModelContentProvider;
import org.faktorips.devtools.core.ui.views.modelexplorer.ModelExplorer;
import org.faktorips.devtools.core.ui.views.modelexplorer.ModelExplorerConfiguration;

/**
 * A <code>ModelExplorer</code> that displays productdefinition projects along with all
 * contained <code>ProductCmpt</code>s, <code>TableContents</code>, <code>TestCases</code>
 * and <code>TestCaseTypes</code>.
 *
 * @author Stefan Widmaier
 */
public class ProductExplorer extends ModelExplorer {
    public static String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.productDefinitionExplorer"; //$NON-NLS-1$

    private static final String EXCLUDE_NON_IPSPRODDEF_PROJECTS_KEY = "exclude_non_ipsproddef_projects"; //$NON-NLS-1$

    /**
     * Used for saving the current layout style and filter in a eclipse memento.
     */
    private static final String MEMENTO = "productExplorer.memento"; //$NON-NLS-1$

    private boolean excludeNoIpsProductDefinitionProjects = false;

    private ProductExplorerFilter filter;

	public ProductExplorer() {
		super();
	}

    /**
     * {@inheritDoc}
     */
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        labelProvider.setProductDefinitionLabelProvider(true);
    }

    /**
     * {@inheritDoc}
     */
    protected ModelExplorerConfiguration createConfig() {
		return new ModelExplorerConfiguration(new Class[] { IProductCmpt.class,
				ITableContents.class, ITestCase.class, IProductCmptGeneration.class}
				, new Class[]{IFile.class, IFolder.class, IProject.class});
	}

    /**
     * User a separate contentprovider for ProductDefinitionExplorer.
     * This contentprovider does not display a default package, only its contents as children
     * of the packageFragmentRoot.
     * {@inheritDoc}
     */
    protected ModelContentProvider createContentProvider() {
        return new ProductContentProvider(config, isFlatLayout);
    }

	protected void createFilters(TreeViewer tree) {
		super.createFilters(tree);
        filter = new ProductExplorerFilter();
		tree.addFilter(filter);
        filter.setExcludeNoIpsProductDefinitionProjects(excludeNoIpsProductDefinitionProjects);
	}

    protected void createContextMenu() {
        MenuManager manager = new MenuManager();
        manager.setRemoveAllWhenShown(true);
        manager.addMenuListener(new ProductMenuBuilder());

        Menu contextMenu = manager.createContextMenu(treeViewer.getControl());
        treeViewer.getControl().setMenu(contextMenu);
//         do not register contextmenue to prevent insertion of MB-Additions
    }

    private class ProductMenuBuilder extends MenuBuilder{
        private WrapperAction team_sync= new WrapperAction(treeViewer, Messages.ProductExplorer_actionSync_label
                , Messages.ProductExplorer_actionSync_tooltip
                , "org.eclipse.team.cvs.ui.CVSActionSet", "org.eclipse.team.cvs.ui.sync"); //$NON-NLS-1$ //$NON-NLS-2$
        private WrapperAction team_commit= new WrapperAction(treeViewer, Messages.ProductExplorer_actionCommit_label
                , Messages.ProductExplorer_actionCommit_tooltip
                , "org.eclipse.team.cvs.ui.CVSActionSet", "org.eclipse.team.cvs.ui.commit"); //$NON-NLS-1$ //$NON-NLS-2$
        private WrapperAction team_update= new WrapperAction(treeViewer, Messages.ProductExplorer_actionUpdate_label
                , Messages.ProductExplorer_actionUpdate_tooltip
                , "org.eclipse.team.cvs.ui.CVSActionSet", "org.eclipse.team.cvs.ui.update"); //$NON-NLS-1$ //$NON-NLS-2$

        private WrapperAction team_tag= new WrapperAction(treeViewer, Messages.ProductExplorer_actionTag_label
                , Messages.ProductExplorer_actionTag_tooltip, "TagAction.gif" //$NON-NLS-1$
                , "org.eclipse.team.cvs.ui.CVSActionSet", "org.eclipse.team.cvs.ui.tag"); //$NON-NLS-1$ //$NON-NLS-2$
        private WrapperAction team_branch= new WrapperAction(treeViewer, Messages.ProductExplorer_actionBranch_label
                , Messages.ProductExplorer_actionBranch_tooltip, "BranchAction.gif" //$NON-NLS-1$
                , "org.eclipse.team.cvs.ui.CVSActionSet", "org.eclipse.team.cvs.ui.branch"); //$NON-NLS-1$ //$NON-NLS-2$
        private WrapperAction team_switchBranch= new WrapperAction(treeViewer, Messages.ProductExplorer_actionSwitchBranch_label
                , Messages.ProductExplorer_actionSwitchBranch_tooltip
                , "org.eclipse.team.cvs.ui.CVSActionSet", "org.eclipse.team.cvs.ui.updateSwitch"); //$NON-NLS-1$ //$NON-NLS-2$
        private WrapperAction team_showResourceHistory= new WrapperAction(treeViewer, Messages.ProductExplorer_actionShowResourceHistory_label
                , Messages.ProductExplorer_actionShowResourceHistory_tooltip, "HistoryAction.gif" //$NON-NLS-1$
                , "org.eclipse.team.cvs.ui.CVSActionSet", "org.eclipse.team.cvs.ui.showHistory"); //$NON-NLS-1$ //$NON-NLS-2$

        private WrapperAction team_restoreFromRepository= new WrapperAction(treeViewer, Messages.ProductExplorer_actionRestoreFromRepositoryAction_label
                , Messages.ProductExplorer_actionRestoreFromRepositoryAction_tooltip
                , "org.eclipse.team.cvs.ui.CVSActionSet", "org.eclipse.team.ccvs.ui.restoreFromRepository"); //$NON-NLS-1$ //$NON-NLS-2$


        private WrapperAction compareWith_latest= new WrapperAction(treeViewer, Messages.ProductExplorer_actionCompareWithLatest_label
                , Messages.ProductExplorer_actionCompareWithLatest_tooltip
                , "org.eclipse.team.cvs.ui.CVSActionSet", "org.eclipse.team.cvs.ui.compareWithRemote"); //$NON-NLS-1$ //$NON-NLS-2$
        private WrapperAction compareWith_branch= new WrapperAction(treeViewer, Messages.ProductExplorer_actionCompareWithBranch_label
                , Messages.ProductExplorer_actionCompareWithBranch_tooltip
                , "org.eclipse.team.cvs.ui.CVSActionSet", "org.eclipse.team.cvs.ui.compareWithTag"); //$NON-NLS-1$ //$NON-NLS-2$
        private WrapperAction compareWith_eachOther= new WrapperAction(treeViewer, Messages.ProductExplorer_actionCompareWithEachOther_label
                , Messages.ProductExplorer_actionCompareWithEachOther_tooltip
                , null, "compareWithEachOther"); //$NON-NLS-1$
        private WrapperAction compareWith_revision= new WrapperAction(treeViewer, Messages.ProductExplorer_actionCompareWithRevision_label
                , Messages.ProductExplorer_actionCompareWithRevision_tooltip
                , "org.eclipse.team.cvs.ui.CVSActionSet", "org.eclipse.team.cvs.ui.compareWithRevision"); //$NON-NLS-1$ //$NON-NLS-2$
        private WrapperAction compareWith_localHistory= new WrapperAction(treeViewer, Messages.ProductExplorer_actionCompareWithLocalHistory_label
                , Messages.ProductExplorer_actionCompareWithLocalHistory_tooltip
                , null, "compareWithHistory"); //$NON-NLS-1$


        private WrapperAction replaceWith_latest= new WrapperAction(treeViewer, Messages.ProductExplorer_actionReplaceWithLatest_label
                , Messages.ProductExplorer_actionReplaceWithLatest_tooltip
                , "org.eclipse.team.cvs.ui.CVSActionSet", "org.eclipse.team.cvs.ui.replace"); //$NON-NLS-1$ //$NON-NLS-2$
        private WrapperAction replaceWith_branch= new WrapperAction(treeViewer, Messages.ProductExplorer_actionReplaceWithBranch_label
                , Messages.ProductExplorer_actionReplaceWithBranch_tooltip
                , "org.eclipse.team.cvs.ui.CVSActionSet", "org.eclipse.team.cvs.ui.replaceWithTag"); //$NON-NLS-1$ //$NON-NLS-2$
        private WrapperAction replaceWith_revision= new WrapperAction(treeViewer, Messages.ProductExplorer_actionReplaceWithRevision_label
                , Messages.ProductExplorer_actionReplaceWithRevision_tooltip
                , "org.eclipse.team.cvs.ui.CVSActionSet", "org.eclipse.team.cvs.ui.replaceWithRevision"); //$NON-NLS-1$ //$NON-NLS-2$
        private WrapperAction replaceWith_previousFromLocalHistory= new WrapperAction(treeViewer, Messages.ProductExplorer_actionReplaceWithPreviousFromLocalHistory_label
                , Messages.ProductExplorer_actionReplaceWithPreviousFromLocalHistory_tooltip
                , "org.eclipse.team.cvs.ui.CVSActionSet", "replaceWithPreviousFromHistory"); //$NON-NLS-1$ //$NON-NLS-2$
        private WrapperAction replaceWith_localHistory= new WrapperAction(treeViewer, Messages.ProductExplorer_actionReplaceWithLocalHistory_label
                , Messages.ProductExplorer_actionReplaceWithLocalHistory_tooltip
                , "org.eclipse.team.cvs.ui.CVSActionSet", "replaceFromHistory"); //$NON-NLS-1$ //$NON-NLS-2$

        protected void createAdditionalActions(IMenuManager manager, IStructuredSelection structuredSelection) {
            Object selected= structuredSelection.getFirstElement();
            MenuManager teamMenu = new MenuManager(Messages.ProductExplorer_subMenuTeam);
            if(config.representsProject(selected)){
                teamMenu.add(team_sync);
                teamMenu.add(team_commit);
                teamMenu.add(team_update);
                teamMenu.add(new Separator());
                teamMenu.add(team_tag);
                teamMenu.add(team_branch);
                teamMenu.add(team_switchBranch);
            }
            if(config.representsFile(selected)){
                teamMenu.add(team_showResourceHistory);
            }
            teamMenu.add(new Separator());
            teamMenu.add(team_restoreFromRepository);
            manager.add(teamMenu);

            MenuManager compareMenu = new MenuManager(Messages.ProductExplorer_subMenuCompareWith);
            compareMenu.add(compareWith_latest);
            compareMenu.add(compareWith_branch);
            // Activate compare with each other only if exactly two elements are selected.
            compareWith_eachOther.setEnabled(structuredSelection.size()==2);
            compareMenu.add(compareWith_eachOther);
            if(config.representsFile(selected)){
                compareMenu.add(compareWith_revision);
                compareMenu.add(compareWith_localHistory);
            }
            manager.add(compareMenu);

            MenuManager replaceMenu = new MenuManager(Messages.ProductExplorer_subMenuReplaceWith);
            replaceMenu.add(replaceWith_latest);
            replaceMenu.add(replaceWith_branch);
            if(config.representsFile(selected)){
                replaceMenu.add(replaceWith_revision);
                replaceMenu.add(replaceWith_previousFromLocalHistory);
                replaceMenu.add(replaceWith_localHistory);
            }
            manager.add(replaceMenu);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected boolean isModelExplorer() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        if (memento != null) {
            IMemento filterMemento = memento.getChild(MEMENTO);
            if (filterMemento != null) {
                Integer exludeNonPredDefProjects = filterMemento.getInteger(EXCLUDE_NON_IPSPRODDEF_PROJECTS_KEY);
                if (exludeNonPredDefProjects != null){
                    excludeNoIpsProductDefinitionProjects = exludeNonPredDefProjects.intValue() == 1;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void saveState(IMemento memento) {
        super.saveState(memento);
        IMemento layout = memento.createChild(MEMENTO);
        layout.putInteger(EXCLUDE_NON_IPSPRODDEF_PROJECTS_KEY, excludeNoIpsProductDefinitionProjects ? 1 : 0);
    }

    /**
     * {@inheritDoc}
     */
    protected void createAdditionalMenuEntries(IMenuManager menuManager) {
        menuManager.add(new Separator(MENU_FILTER_GROUP));
        Action showNoIpsProdDefProjectsAction = createShowNoIpsProductDefinitionAction();
        showNoIpsProdDefProjectsAction.setChecked(excludeNoIpsProductDefinitionProjects);
        menuManager.appendToGroup(MENU_FILTER_GROUP, showNoIpsProdDefProjectsAction);
    }

    /**
     * @return
     */
    private Action createShowNoIpsProductDefinitionAction() {
        return new Action(Messages.ProductExplorer_MenuShowProdDefProjectsOnly_Title, Action.AS_CHECK_BOX) {
            public ImageDescriptor getImageDescriptor() {
                return null;
            }
            public void run() {
                excludeNoIpsProductDefinitionProjects = ! excludeNoIpsProductDefinitionProjects;
                filter.setExcludeNoIpsProductDefinitionProjects(excludeNoIpsProductDefinitionProjects);
                treeViewer.refresh();
            }
            public String getToolTipText() {
                return Messages.ProductExplorer_MenuShowProdDefProjectsOnly_Tooltip;
            }
        };

    }
}
