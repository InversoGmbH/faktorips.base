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

package org.faktorips.devtools.core.ui.views.testrunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsProductDefinitionPerspectiveFactory;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.actions.IpsTestAction;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestRunPane {
    private final static String[] EMPTY_STRING_ARRAY = new String[0];
    
	private Table fTable;
	
	private List tableFailureItems = new ArrayList();

    private int currErrorOrFailure = 0;
    
	private IpsTestRunnerViewPart testRunnerViewPart;
	
    private List missingTestEntries = new ArrayList();
    
	// Maps test Ids to the stored table items. 
	private Map fTableItemMap = new HashMap();
	
	public TestRunPane(Composite parent, final IpsTestRunnerViewPart testRunnerViewPart) {
		this.testRunnerViewPart = testRunnerViewPart;
		fTable = new Table(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
		fTable.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
            	if (e.item instanceof TableItem){
	            	TableItem selectedTestCase = (TableItem) e.item;
	            	showDetailsInFailurePane(selectedTestCase);
            	}
            }
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do
            }
        });
		fTable.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				if (fTable.getSelectionCount() > 0)
					if (getSelectedTestFullPath() != null){
						new OpenTestInEditorAction(testRunnerViewPart, getSelectedTestFullPath(), getSelectedTestQualifiedName(), null).run();
                    }
			}
		});
        
        buildContextMenu();
	}

	private void buildContextMenu() {
        TableMenu menuMgr = new TableMenu(); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(menuMgr);
        
        Menu menu = menuMgr.createContextMenu(fTable);
        fTable.setMenu(menu);
    }

    private class TableMenu extends MenuManager implements IMenuListener {
        public void menuAboutToShow(IMenuManager manager) {
            if (!(fTable.getItemCount() > 0) && getSelectedItem() != null){
                return;
            }
            
            Action open = new Action("actionOpen", Action.AS_PUSH_BUTTON) { //$NON-NLS-1$
                public void run() {
                    new OpenTestInEditorAction(testRunnerViewPart, testRunnerViewPart.getSelectedTestFullPath(), testRunnerViewPart.getSelectedTestQualifiedName(), null).run();
                }
            };
            open.setText(Messages.TestRunPane_Menu_GoToFile);
            manager.add(open);
            
            manager.add(new Separator());
            
            Action runAction = new Action("actionRun", Action.AS_PUSH_BUTTON) { //$NON-NLS-1$
                public void run() {
                    startSelectedTest(ILaunchManager.RUN_MODE);
                }
            };
            runAction.setText(Messages.TestRunPane_Menu_Run);
            manager.add(runAction);  
            
            // show debug entry only if the current perspective is not the produduct definition perspective
            IPerspectiveDescriptor pd =  IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective();
            if (pd != null){
                String perspective = pd.getId();
                if (! IpsProductDefinitionPerspectiveFactory.PRODUCTDEFINITIONPERSPECTIVE_ID.equals(perspective)){
                    Action debugAction = new Action("actionDebug", Action.AS_PUSH_BUTTON) { //$NON-NLS-1$
                        public void run() {
                            startSelectedTest(ILaunchManager.DEBUG_MODE);
                        } 
                    };
                    debugAction.setText(Messages.TestRunPane_MenuDebug);
                    manager.add(debugAction);  
                }
            }
        }
    }
    
    private void startSelectedTest(String mode) {
        String selectedTestQualifiedName = getSelectedTestQualifiedName();
        if (StringUtils.isEmpty(selectedTestQualifiedName)){
            return;
        } 
        
        IIpsObject ipsObject = null;
        try {
            IIpsProject ipsProject = IpsPlugin.getDefault().getIpsTestRunner().getIpsProject();
            IIpsPackageFragmentRoot[] ipsPackageFragmentRoots = ipsProject.getIpsPackageFragmentRoots();
            for (int i = 0; i < ipsPackageFragmentRoots.length; i++) {
                // currently two types of ips objects supports test:
                //  1. product cmpts
                //  2. isp test cases
                ipsObject = ipsPackageFragmentRoots[i].findIpsObject(IpsObjectType.TEST_CASE, selectedTestQualifiedName);
                if (ipsObject != null){
                    break;
                }
                ipsObject = ipsPackageFragmentRoots[i].findIpsObject(IpsObjectType.PRODUCT_CMPT, selectedTestQualifiedName);
                if (ipsObject != null){
                    break;
                }
            }
            if (ipsObject != null){
                String repositoryPackage = null;
                repositoryPackage = IpsTestAction.getRepPckNameFromPckFrgmtRoot(ipsObject.getIpsPackageFragment().getRoot());
                IpsPlugin.getDefault().getIpsTestRunner().startTestRunnerJob(repositoryPackage, selectedTestQualifiedName, mode, null);
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return;
        }
    }     
    
    /*
	 * Display the error or failure details in the failure pane.
	 */
	private void showDetailsInFailurePane(TableItem tableItem) {
		TestTableEntry testEntry = (TestTableEntry) tableItem.getData();
		String details[] = EMPTY_STRING_ARRAY;
		if (testEntry.isFailure())
			details = (String[])testEntry.getFailureDetails().toArray(new String[0]);
		else if (testEntry.isError())
			details = testEntry.getErrorDetails();
		
		testRunnerViewPart.selectionOfTestCaseChanged(details);
	}
	
	/**
	 * Returns the composite used to present the test runs.
	 */
	public Composite getComposite(){
		return fTable;
	}
	
	/**
	 * Select the first failure or error table entry.
	 * If there was no failure or error then nothing will selected.
	 */
	public void selectFirstFailureOrError(){
		if (tableFailureItems.size()==0)
            return;
        
		fTable.setSelection(fTable.indexOf((TableItem)tableFailureItems.get(0)));
		showDetailsInFailurePane((TableItem)tableFailureItems.get(0));
	}
    
    /**
     * Select the next failure or error, does nothing if no error or failure exists.
     */
    public void selectNextFailureOrError(){
        if (tableFailureItems.size()==0)
            return;
        
        currErrorOrFailure ++;
        if (currErrorOrFailure>(tableFailureItems.size()-1)){
            currErrorOrFailure = 0;
        }
        
        fTable.setSelection(fTable.indexOf((TableItem)tableFailureItems.get(currErrorOrFailure)));
        showDetailsInFailurePane((TableItem)tableFailureItems.get(currErrorOrFailure));
    }
    
    /**
     * Select the previous failure or error, does nothing if no error or failure exists.
     */
    public void selectPreviousFailureOrError(){
        if (tableFailureItems.size()==0)
            return;
        
        currErrorOrFailure --;
        
        if (currErrorOrFailure<0)
            currErrorOrFailure = tableFailureItems.size() -1;
        
        fTable.setSelection(fTable.indexOf((TableItem)tableFailureItems.get(currErrorOrFailure)));
        showDetailsInFailurePane((TableItem)tableFailureItems.get(currErrorOrFailure));
    }    

    public String[] getFailureDetailsOfSelectedTestCase(int failureIndex){
        TableItem ti = getSelectedItem();
        if (ti == null){
            return EMPTY_STRING_ARRAY;
        }
        TestTableEntry entry= (TestTableEntry) ti.getData();
        return entry.getDetailsOfSingleFailure(failureIndex);
    }
    
    public List getAllFailureDetailsOfSelectedTestCase(){
        TableItem ti = getSelectedItem();
        if (ti == null){
            return new ArrayList(0);
        }
        TestTableEntry entry= (TestTableEntry) ti.getData();
        return entry.getFailureOrErrorDetailList();
    }
    
	//
	// Methods to inform this pane about test run, end and failures
	//
	
	/**
	 * A new test run will be stared.
	 */
	public void aboutToStart() {
        missingTestEntries.clear();
		fTable.removeAll();
        fTableItemMap.clear();
        tableFailureItems.clear();
        currErrorOrFailure = 0;
	}
	
	/**
	 * The given test is about to be started.
	 */
	public void newTableEntry(String testId, String tableEntry, String fullPath) {
        if (fTableItemMap.get(testId) != null)
            // test table entry is already in the table, ignore entry
            return;
        
        TableItem tableItem = new TableItem(fTable, SWT.NONE);
		tableItem.setText(tableEntry);
		tableItem.setImage(IpsPlugin.getDefault().getImage("obj16/test.gif")); //$NON-NLS-1$

		TestTableEntry testTableEntry = new TestTableEntry(testId, tableEntry, tableItem);
		fTableItemMap.put(testId, testTableEntry);
        tableItem.setData(testTableEntry);
		// stores the given full path of the test case file
		testTableEntry.setFullPath(fullPath);
	}

	/**
	 * The given test ends.
	 */
	public void endTest(String testId, String qualifiedTestName) {
		TestTableEntry testTableEntry = (TestTableEntry) fTableItemMap.get(testId);
		if (testTableEntry == null){
            missingTestEntries.add(testId);
            return;
        }
		updateTestEntryStatusImage(testTableEntry);
	}

    /*
     * Update the image of the given test entry.
     */
    private void updateTestEntryStatusImage(TestTableEntry testTableEntry) {
        TableItem tableItem = testTableEntry.getTableItem();
		if (testTableEntry.isError()){
			tableItem.setImage(IpsPlugin.getDefault().getImage("obj16/testerr.gif")); //$NON-NLS-1$
		}else if (testTableEntry.isFailure()){
			tableItem.setImage(IpsPlugin.getDefault().getImage("obj16/testfail.gif")); //$NON-NLS-1$
		}else if (testTableEntry.isOk()){
			tableItem.setImage(IpsPlugin.getDefault().getImage("obj16/testok.gif")); //$NON-NLS-1$
		}else{
			tableItem.setImage(IpsPlugin.getDefault().getImage("obj16/test.gif")); //$NON-NLS-1$
		}
    }

	/**
	 * The given test has started.
	 * @param scrollLocked 
	 */
	public void startTest(String testId, String qualifiedTestName, boolean scrollLocked) {
		TestTableEntry testTableEntry = (TestTableEntry) fTableItemMap.get(testId);
		if (testTableEntry == null)
			return;
		
		TableItem tableItem = testTableEntry.getTableItem();
		tableItem.setImage(IpsPlugin.getDefault().getImage("obj16/testrun.gif")); //$NON-NLS-1$
        
        // select current item and scroll
        if (!scrollLocked){
            fTable.setSelection(new TableItem[]{tableItem});
        }
	}
	
	/**
	 * The given test fails with the given details. 
	 */
	public void failureTest(String testId, String failure, String[] failureDetails) {
		TestTableEntry testTableEntry = (TestTableEntry) fTableItemMap.get(testId);
		if (testTableEntry == null)
			return;
		
		testTableEntry.setStatus(TestTableEntry.FAILURE);
		testTableEntry.addFailure(failure);
        testTableEntry.addFailureDetails(failureDetails);
        
        if (!tableFailureItems.contains(testTableEntry.getTableItem()))
            tableFailureItems.add(testTableEntry.getTableItem());
	}
	
	/**
	 * There was an error while running the given test.
	 */
	public void errorInTest(String testId, String qualifiedTestName, String[] errorDetails) {
		TestTableEntry testTableEntry = (TestTableEntry) fTableItemMap.get(testId);
		if (testTableEntry == null){
			// in case of an error before starting a single test
			// create an error entry
			newTableEntry(testId, Messages.TestRunPane_ErrorStartingTest_Entry, ""); //$NON-NLS-1$
			testTableEntry = (TestTableEntry) fTableItemMap.get(testId);
		}
		TableItem tableItem = testTableEntry.getTableItem();
		tableItem.setImage(IpsPlugin.getDefault().getImage("obj16/testerr.gif")); //$NON-NLS-1$
		testTableEntry.setErrorDetails(errorDetails);
		testTableEntry.setStatus(TestTableEntry.ERROR);
		
        tableFailureItems.add(tableItem);		
	}
	
    /**
     * Returns the full path of the selected test case or an empty string if no test case is selected
     */
	String getSelectedTestFullPath() {
		TableItem item= getSelectedItem();
        if (item == null)
            return ""; //$NON-NLS-1$
        
		TestTableEntry entry= (TestTableEntry) item.getData();
		return entry.getFullPath();
	}

    String getSelectedTestQualifiedName() {
        TableItem item= getSelectedItem();
        if (item == null)
            return ""; //$NON-NLS-1$
        
        TestTableEntry entry= (TestTableEntry) item.getData();
        return entry.getQualifiedTestName();
    }
    
    /*
     * Returns the selected item in the table or <code>null</code> if no item is selected
     */
	private TableItem getSelectedItem() {
		int index= fTable.getSelectionIndex();
		if (index == -1)
			return null;
		return fTable.getItem(index);
	}
	
	
	/*
	 * Inner class to store a test case.
	 */
	private class TestTableEntry{
		public static final int ERROR = -1;
		public static final int FAILURE = 0;
		public static final int UNKNOWN = 2;
		private String testId;
		private String qualifiedTestName;
		private String fullPath;
		private TableItem tableItem;
		private int status = UNKNOWN;
		private List failures = new ArrayList();
		private List failureDetailList = new ArrayList();
		private String[] errorDetails = EMPTY_STRING_ARRAY;
		
		TestTableEntry(String testId, String qualifiedTestName, TableItem tableItem){
			this.testId = testId;
			this.qualifiedTestName = qualifiedTestName;
			this.tableItem = tableItem;
		}
        public String getTestId() {
			return testId;
		}
		public String getQualifiedTestName() {
			return qualifiedTestName;
		}
		public TableItem getTableItem() {
			return tableItem;
		}
		public String getFullPath() {
			return fullPath;
		}
		public void setFullPath(String fullPath) {
			this.fullPath = fullPath;
		}
		public void setStatus(int status) {
			this.status = status;
		}
		public boolean isError(){
			return ERROR == status;
		}
		public boolean isFailure(){
			return FAILURE == status;
		}
		public boolean isOk(){
			return status != ERROR && status != FAILURE;
		}
		public void addFailure(String failure){
			failures.add(failure);
		}
		public List getFailureDetails(){
			return failures;
		}
		public String[] getErrorDetails() {
			return errorDetails;
		}
		public void setErrorDetails(String[] errorDetails) {
			this.errorDetails = errorDetails;
		}
        public void addFailureDetails(String[] failureDetails) {
            failureDetailList.add(failureDetails);
        }
        public String[] getDetailsOfSingleFailure(int idx) {
            if (idx >= failureDetailList.size()){
                return EMPTY_STRING_ARRAY;
            } else {
                return (String[]) failureDetailList.get(idx);
            }
        }
        public List getFailureOrErrorDetailList() {
            if (errorDetails.length>0){
                ArrayList result = new ArrayList(1);
                result.add(errorDetails);
                return result;
            } else {
                return failureDetailList;
            }
        }
	}

    void checkMissingEntries() {
        if (missingTestEntries.size() == 0)
            return;
        synchronized (missingTestEntries) {
            for (Iterator iter = missingTestEntries.iterator(); iter.hasNext();) {
                String element = (String )iter.next();
                TestTableEntry testTableEntry = (TestTableEntry) fTableItemMap.get(element);
                if (testTableEntry == null){
                    missingTestEntries.add(element);
                    return;
                }
                updateTestEntryStatusImage(testTableEntry);
            }
        }
    }
}
