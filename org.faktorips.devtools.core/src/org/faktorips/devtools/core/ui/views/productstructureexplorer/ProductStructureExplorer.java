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

package org.faktorips.devtools.core.ui.views.productstructureexplorer;



import java.text.DateFormat;
import java.util.GregorianCalendar;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.ui.actions.FindProductReferencesAction;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;
import org.faktorips.devtools.core.ui.views.IpsElementDragListener;
import org.faktorips.devtools.core.ui.views.IpsElementDropListener;
import org.faktorips.devtools.core.ui.views.IpsProblemsLabelDecorator;
import org.faktorips.devtools.core.ui.views.TreeViewerDoubleclickListener;

/**
 * Navigate all Products defined in the active Project.
 * 
 * @author guenther
 *
 */
public class ProductStructureExplorer extends ViewPart implements ContentsChangeListener, IShowInSource,
        IResourceChangeListener, IPropertyChangeListener {
    public static String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.productStructureExplorer"; //$NON-NLS-1$

    private static String MENU_INFO_GROUP = "goup.info"; //$NON-NLS-1$
    private static String MENU_FILTER_GROUP = "goup.filter"; //$NON-NLS-1$
    
    // Used for saving the current layout style in a eclipse memento.
    private static final String LAYOUT_AND_FILTER_MEMENTO = "layoutandfilter"; //$NON-NLS-1$
    private static final String CHECK_MENU_STATE = "checkedmenus"; //$NON-NLS-1$
    
    private TreeViewer tree; 
    private IIpsSrcFile file;
    private ProductStructureContentProvider contentProvider;
    private ProductStructureLabelProvider labelProvider;
    private GenerationRootNode rootNode;
    private Label errormsg;

    private boolean showRelationNode = false;
    private boolean showTableStructureRoleName = false;
    private boolean showReferencedTable = true;
    
    /*
     * Class to represent the root tree node to inform about the current working date.
     */
    class GenerationRootNode extends ViewerLabel {
        private IProductCmpt productCmpt;
        private GregorianCalendar workingDate;
        private String generationText;
        
        public GenerationRootNode() {
            super("", null); //$NON-NLS-1$
        }
        
        public void refreshText() {
            if (productCmpt == null) {
                return;
            }
            workingDate = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
            generationText = IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNameSingular(); 
            
            DateFormat format = IpsPlugin.getDefault().getIpsPreferences().getDateFormat();
            String formatedWorkingDate = format.format(workingDate.getTime());
            String label = NLS.bind(Messages.ProductStructureContentProvider_treeNodeText_GenerationCurrentWorkingDate,
                    formatedWorkingDate);
            this.setText(label);
            this.setImage(IpsPlugin.getDefault().getImage("WorkingDate.gif")); //$NON-NLS-1$
        }

        public void storeProductCmpt(IProductCmpt productCmpt){
            this.productCmpt = productCmpt;
            refreshText();
        }
        
        public String getGenerationText() {
            return generationText;
        }

        public GregorianCalendar getWorkingDate() {
            return workingDate;
        }
        
        public String getProductCmptNoGenerationLabel(IProductCmpt productCmpt){
            String label = productCmpt.getName();
            if (null == productCmpt.findGenerationEffectiveOn(getWorkingDate())) {
                // no generations avaliable,
                // show additional text to inform that no generations exists
                label = NLS.bind(Messages.ProductStructureExplorer_label_NoGenerationForCurrentWorkingDate, label, getGenerationText());
            }
            return label;
        }
    }
    
    private class ProductCmptDropListener extends IpsElementDropListener {

        public void dragEnter(DropTargetEvent event) {
            event.detail = DND.DROP_LINK;
        }

        public void drop(DropTargetEvent event) {
            IIpsElement[] transferred = super.getTransferedElements(event.currentDataType);
            if (transferred.length > 0 && transferred[0] instanceof IIpsSrcFile) {
                try {
                    showStructure((IIpsSrcFile)transferred[0]);
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }
        }

        public void dropAccept(DropTargetEvent event) {
            event.detail = DND.DROP_LINK;
        }
    }
    
    public ProductStructureExplorer() {
        IpsPlugin.getDefault().getIpsModel().addChangeListener(this);

        // add as resource listener because refactoring-actions like move or rename
        // does not cause a model-changed-event.
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        
        IpsPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
    }
    
    /**
     * {@inheritDoc}
     */
    public void init(IViewSite site) throws PartInitException {
    	super.init(site);
    }
    
    private void initMenu(IMenuManager menuManager) {
        menuManager.add(new Separator(MENU_INFO_GROUP));
        Action showRelationNodeAction = createShowRelationNodeAction();
        showRelationNodeAction.setChecked(showRelationNode);
        menuManager.appendToGroup(MENU_INFO_GROUP, showRelationNodeAction);
        Action showRoleNameAction = createShowTableRoleNameAction();        
        showRoleNameAction.setChecked(showTableStructureRoleName);
        menuManager.appendToGroup(MENU_INFO_GROUP, showRoleNameAction);        
        
        menuManager.add(new Separator(MENU_FILTER_GROUP));
        Action showReferencedTableAction = createShowReferencedTables();
        showReferencedTableAction.setChecked(showReferencedTable);
        menuManager.appendToGroup(MENU_FILTER_GROUP, showReferencedTableAction);        
    }

    private Action createShowReferencedTables() {
        return new Action(Messages.ProductStructureExplorer_menuShowReferencedTables_name, Action.AS_CHECK_BOX) {
            public ImageDescriptor getImageDescriptor() {
                return null;
            }
            public void run() {
                contentProvider.setShowTableContents(!contentProvider.isShowTableContents());
                showReferencedTable = contentProvider.isShowTableContents();
                refresh();
            }
            public String getToolTipText() {
                return Messages.ProductStructureExplorer_menuShowReferencedTables_tooltip;
            }
        };
    }

    private Action createShowTableRoleNameAction() {
        return new Action(Messages.ProductStructureExplorer_menuShowTableRoleName_name, Action.AS_CHECK_BOX) {
            public ImageDescriptor getImageDescriptor() {
                return null;
            }
            public void run() {
                labelProvider.setShowTableStructureUsageName(!labelProvider.isShowTableStructureUsageName());
                showTableStructureRoleName = labelProvider.isShowTableStructureUsageName();
                refresh();
            }
            public String getToolTipText() {
                return Messages.ProductStructureExplorer_menuShowTableRoleName_tooltip;
            }
        };
    }

    private Action createShowRelationNodeAction() {
        return new Action(Messages.ProductStructureExplorer_menuShowRelationNodes_name, Action.AS_CHECK_BOX) {
            public ImageDescriptor getImageDescriptor() {
                return IpsPlugin.getDefault().getImageDescriptor("ShowRelationTypeNodes.gif"); //$NON-NLS-1$
            }
            public void run() {
                contentProvider.setRelationTypeShowing(!contentProvider.isRelationTypeShowing());
                showRelationNode = contentProvider.isRelationTypeShowing();
                refresh();
            }
            public String getToolTipText() {
                return Messages.ProductStructureExplorer_tooltipToggleRelationTypeNodes;
            }
        };
    }

    private void initToolBar(IToolBarManager toolBarManager) {
        Action refreshAction= new Action() {
            public ImageDescriptor getImageDescriptor() {
                return IpsPlugin.getDefault().getImageDescriptor("Refresh.gif"); //$NON-NLS-1$
            }
            
            public void run() {
                refresh();
                tree.expandAll();
            }

            public String getToolTipText() {
                return Messages.ProductStructureExplorer_tooltipRefreshContents;
            }
        };
        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
        IWorkbenchAction retargetAction = ActionFactory.REFRESH.create(getViewSite().getWorkbenchWindow());
        retargetAction.setImageDescriptor(refreshAction.getImageDescriptor());
        retargetAction.setToolTipText(refreshAction.getToolTipText());
        getViewSite().getActionBars().getToolBarManager().add(retargetAction);

        // collapse all action
        toolBarManager.add(new Action() {
            public void run() {
                tree.collapseAll();
            }
        
            public ImageDescriptor getImageDescriptor() {
                return IpsPlugin.getDefault().getImageDescriptor("CollapseAll.gif"); //$NON-NLS-1$
            }

            public String getToolTipText() {
                return Messages.ProductStructureExplorer_menuCollapseAll_toolkit;
            }
        });
        
        // clear action
        toolBarManager.add(new Action() {
            public void run() {
                tree.setInput(null);
                tree.refresh();
                showEmptyMessage();
            }
        
            public ImageDescriptor getImageDescriptor() {
                return IpsPlugin.getDefault().getImageDescriptor("Clear.gif"); //$NON-NLS-1$
            }

            public String getToolTipText() {
                return Messages.ProductStructureExplorer_tooltipClear;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
	public void createPartControl(Composite parent) {
        parent.setLayout(new GridLayout(1, true));
        errormsg = new Label(parent, SWT.WRAP);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutData.exclude = true;
        errormsg.setLayoutData(layoutData);
        errormsg.setVisible(false);

        // dnd for label
        DropTarget dropTarget = new DropTarget(errormsg, DND.DROP_LINK);
        dropTarget.addDropListener(new ProductCmptDropListener());
        dropTarget.setTransfer(new Transfer[] { FileTransfer.getInstance()});
        
        tree = new TreeViewer(parent);
        tree.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        rootNode = new GenerationRootNode();
        contentProvider = new ProductStructureContentProvider(false);
        contentProvider.setRelationTypeShowing(showRelationNode);
        contentProvider.setShowTableContents(showReferencedTable);
        
        contentProvider.setGenerationRootNode(rootNode);
        tree.setContentProvider(contentProvider);

        labelProvider = new ProductStructureLabelProvider();
        labelProvider.setGenerationRootNode(rootNode);
        tree.setLabelProvider(new DecoratingLabelProvider(labelProvider, new IpsProblemsLabelDecorator()));
        labelProvider.setShowTableStructureUsageName(showTableStructureRoleName);
        
        tree.addDoubleClickListener(new TreeViewerDoubleclickListener(tree));
        tree.expandAll();
        tree.addDragSupport(DND.DROP_LINK, new Transfer[] { FileTransfer.getInstance() }, new IpsElementDragListener(
                tree));
        tree.addDropSupport(DND.DROP_LINK, new Transfer[] { FileTransfer.getInstance() }, new ProductCmptDropListener());

        MenuManager menumanager = new MenuManager();
        menumanager.setRemoveAllWhenShown(false);
        menumanager.add(new OpenEditorAction(tree));
        menumanager.add(new FindProductReferencesAction(tree));

        Menu menu = menumanager.createContextMenu(tree.getControl());
        tree.getControl().setMenu(menu);
        getSite().setSelectionProvider(tree);
        
        showEmptyMessage();

        IActionBars actionBars = getViewSite().getActionBars();
        initMenu(actionBars.getMenuManager());
        initToolBar(actionBars.getToolBarManager());        
    }

    /**
     * {@inheritDoc}
     */
	public void setFocus() {
        //nothing to do.
	}

    /**
     * Displays the structure of the product component defined by the given file. 
     * 
     * @param selectedItems The selection to display
     * @throws CoreException 
     */
    public void showStructure(IIpsSrcFile file) throws CoreException {
    	if(file!=null && file.getIpsObjectType()==IpsObjectType.PRODUCT_CMPT){
    		showStructure((IProductCmpt) file.getIpsObject());
    	}
    }

    /**
     * Displays the structure of the given product component.
     */
    public void showStructure(IProductCmpt product) {
    	if (product == null) {
    		return;
    	}
        
        if (errormsg == null) {
            // return if called before the explorer is shown
            return;
        }
        
    	this.file = product.getIpsSrcFile();
        try {
            rootNode.storeProductCmpt(product);
            showTreeInput(product.getStructure(product.getIpsProject()));
		} catch (CycleInProductStructureException e) {
			handleCircle(e);
		}
    }

    private void refresh() {
        Control ctrl = tree.getControl();
        
        if (ctrl == null || ctrl.isDisposed()) {
        	return;
        }
        
        try {
            Runnable runnable = new Runnable() {
                public void run() {
                    if (!tree.getControl().isDisposed()) {
                        Object input = tree.getInput();
                        if (input instanceof IProductCmptTreeStructure) {
                            try {
                                ((IProductCmptTreeStructure)input).refresh();
                            } catch (CycleInProductStructureException e) {
                                handleCircle(e);
                                return;
                            }
                            showTreeInput(input);
                        } else {
                            showEmptyMessage();
                        }
                    }
                }
            };

            ctrl.setRedraw(false);
            ctrl.getDisplay().syncExec(runnable);
        } finally {
            ctrl.setRedraw(true);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public ShowInContext getShowInContext() {
        ShowInContext context = new ShowInContext(null, tree.getSelection());
        return context;
    }
    
    private void handleCircle(CycleInProductStructureException e) {
		IpsPlugin.log(e);
		((GridData)tree.getTree().getLayoutData()).exclude = true;
		String msg = Messages.ProductStructureExplorer_labelCircleRelation;
		IIpsElement[] cyclePath = e.getCyclePath();
		StringBuffer path = new StringBuffer();
		
        // don't show first element if the first elemet is no product relevant node (e.g. effective
        // date info node)
        IIpsElement[] cyclePathCpy;
        if (cyclePath[0] == null) {
            cyclePathCpy = new IIpsElement[cyclePath.length -1];
            System.arraycopy(cyclePath, 1, cyclePathCpy, 0, cyclePath.length -1);
        } else {
            cyclePathCpy = new IIpsElement[cyclePath.length];
            System.arraycopy(cyclePath, 0, cyclePathCpy, 0, cyclePath.length);
        }
        
        for (int i = cyclePathCpy.length-1; i >= 0; i--) {
			path.append(cyclePathCpy[i] == null?"":cyclePathCpy[i].getName()); //$NON-NLS-1$
			if (i%2 != 0) {
				path.append(" -> "); //$NON-NLS-1$
			}
			else if (i%2 == 0 && i > 0) {
				path.append(":"); //$NON-NLS-1$
			}
		}

        String message = msg + " " + path; //$NON-NLS-1$
        showErrorMsg(message);
    }

    /**
     * {@inheritDoc}
     */
    public void contentsChanged(ContentChangeEvent event) {
        if (file == null || !event.getIpsSrcFile().equals(file)) {
            // no contents set or event concerncs another source file - nothing to refresh.
            return;
        }
        int type = event.getEventType();
        IIpsObjectPart part = event.getPart();
        
        // refresh only for relevant changes
        if (part instanceof ITableContentUsage || part instanceof IProductCmptLink
                || type == ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED) {
            postRefresh();
        }
    }

    private void postRefresh() {
        getViewSite().getShell().getDisplay().asyncExec(new Runnable(){
            public void run() {
                refresh();
            }
        });
    }
    
    /**
     * {@inheritDoc}
     */
	public void resourceChanged(IResourceChangeEvent event) {
        if (file == null) {
            return;
        }
        postRefresh();
    }
	
    /**
     * If the working date changed update the content of the view.
     * 
     * {@inheritDoc}
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getProperty().equals(IpsPreferences.WORKING_DATE)){
            try {
                showStructure(file);
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        IpsPlugin.getDefault().getIpsModel().removeChangeListener(this);
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        IpsPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
        super.dispose();
    }

    private void showErrorMsg(String message) {
        tree.getTree().setVisible(false);
        errormsg.setText(message); 
        errormsg.setVisible(true);
        ((GridData)errormsg.getLayoutData()).exclude = false;
        errormsg.getParent().layout();
    }
    
    private void showTreeInput(Object input) {
        errormsg.setVisible(false);
        ((GridData)errormsg.getLayoutData()).exclude = true;
        
        tree.getTree().setVisible(true);
        ((GridData)tree.getTree().getLayoutData()).exclude = false;
        tree.getTree().getParent().layout();
        
        tree.setInput(input);
        tree.expandAll();

        rootNode.refreshText();
    }    

    private void showEmptyMessage() {
        showErrorMsg(Messages.ProductStructureExplorer_infoMessageEmptyView_1 +
                Messages.ProductStructureExplorer_infoMessageEmptyView_2);
    }

    /**
     * {@inheritDoc}
     */
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        if (memento != null) {
            IMemento layout = memento.getChild(LAYOUT_AND_FILTER_MEMENTO);
            if (layout != null) {
                Integer checkedMenuState = layout.getInteger(CHECK_MENU_STATE);
                if (checkedMenuState != null){
                    intitMenuStateFields(checkedMenuState.intValue());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void saveState(IMemento memento) {
        super.saveState(memento);
        int checkedMenuState = evalMenuStates();
        IMemento layout = memento.createChild(LAYOUT_AND_FILTER_MEMENTO);
        layout.putInteger(CHECK_MENU_STATE, checkedMenuState);
    }
    
    private void intitMenuStateFields(int checkedMenuState){
        showReferencedTable = (checkedMenuState & 1) > 0;
        showTableStructureRoleName = (checkedMenuState & 2) > 0;
        showRelationNode = (checkedMenuState & 4) > 0;
    }
    
    private int evalMenuStates(){
        return ((showReferencedTable?1:0) | (showTableStructureRoleName?2:0) | (showRelationNode?4:0));
    }
}
