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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IFormulaTestCase;
import org.faktorips.devtools.core.model.productcmpt.IFormulaTestInputValue;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.ProblemImageDescriptor;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.CompositeUIController;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.table.BeanTableCellModifier;
import org.faktorips.devtools.core.ui.table.ColumnChangeListener;
import org.faktorips.devtools.core.ui.table.ColumnIdentifier;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Composite to display a table of formula test cases and their details.
 * 
 * @author Joerg Ortmann
 */
public class FormulaTestCaseControl extends Composite implements ColumnChangeListener, IDataChangeableReadWriteAccess {
    private static final int IDX_COLUMN_IMAGE = 0;
    private static final int IDX_COLUMN_NAME = 1;
    private static final int IDX_COLUMN_EXPECTED_RESULT = 2;
    private static final int IDX_COLUMN_ACTUAL_RESULT = 3;
    
    private static final String PROPERTY_ACTUAL_RESULT = "actualResult"; //$NON-NLS-1$
    
    private static final int TEST_ERROR = 1;
    private static final int TEST_FAILURE = 2;
    private static final int TEST_OK = 3;
    private static final int TEST_UNKNOWN = 4;
    
    private UIToolkit uiToolkit;
    
    private Image empytImage;
    
    private HashMap cachedProblemImageDescriptors = new HashMap();
    
    /* Controller of the dependent ips object part */
    private IpsObjectUIController uiController;
    
    /*
     * Composite controler contains the dependent object part ui controller and the dummy contoler
     * to update the ui for this composite e.g. the actual value will be set afer executing the
     * formula
     */
    private CompositeUIController compositeUiController;
    
    /* The formula test cases which are displayed in the table */
    private List formulaTestCases = new ArrayList();

    /* The config element the displayed formula test cases belongs to */ 
    private IFormula formula;
    
    private IIpsProject ipsProject;
    
    /* Contains the table viewer to display and edit the formula test cases */
    private TableViewer formulaTestCaseTableViewer;
    
    /*
     * Contains the table to display the details of the currently selected formula test case which
     * is selected in the formula test case table
     */
    private FormulaTestInputValuesControl formulaTestInputValuesControl;
    
    /* The status bar which contains the corresponding color of the last test run */
    private Control testStatusBar;
    
    /* Buttons */
    private Button btnNewFormulaTestCase;
    private Button btnDeleteFormulaTestCase;
    private Button btnUpdateFormulaTestCase;
    private Button btnMoveFormulaTestCaseUp;
    private Button btnMoveFormulaTestCaseDown;
    
    /* Contains the colors for the test status */
    private Color failureColor;
    private Color okColor;
    
    /* Indicates errors or failures during the calculation */
    private boolean isCalculationErrorOrFailure;
    private boolean dataChangeable;
    
    /*
     * Extended data which is displayed beside the model data in the table
     */
    public class ExtDataForFormulaTestCase {
        private IFormulaTestCase formulaTestCase;
        
        private String actualResult = ""; //$NON-NLS-1$
        private String message = ""; //$NON-NLS-1$
        
        public ExtDataForFormulaTestCase(IFormulaTestCase formulaTestCase) {
            super();
            this.formulaTestCase = formulaTestCase;
        }
        public String getActualResult() {
            return actualResult;
        }
        public void setActualResult(String actualResult) {
            this.actualResult = actualResult;
        }
        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
        
        public IFormulaTestCase getFormulaTestCase(){
            return formulaTestCase;
        }
        
        public IIpsElement getParent(){
            return formulaTestCase.getParent();
        }
        
        public IFormula getFormula(){
            return formulaTestCase.getFormula();
        }

        public String getName() {
            return formulaTestCase.getName();
        }

        public void setName(String name) {
            formulaTestCase.setName(name);
        }
        
        public String getExpectedResult(){
            return formulaTestCase.getExpectedResult();
        }

        public void setExpectedResult(String expectedResult){
            formulaTestCase.setExpectedResult(expectedResult);
        }
        
        public MessageList validate() throws CoreException{
            return formulaTestCase.validate(formula.getIpsProject());
        }
        
        public Object execute() throws Exception{
            return formulaTestCase.execute(formulaTestCase.getIpsProject());
        }
        
        public boolean addOrDeleteFormulaTestInputValues(String[] newIdentifier){
            return formulaTestCase.addOrDeleteFormulaTestInputValues(newIdentifier, formulaTestCase.getIpsProject());
        }
        
        public IFormulaTestInputValue[] getFormulaTestInputValues(){
            return formulaTestCase.getFormulaTestInputValues();
        }
        
        public void delete(){
            formulaTestCase.delete();
        }
    }
   
    /*
     * Returns the status error, failure, or ok of the given formula test case
     */
    private int getFormulaTestCaseTestStatus(ExtDataForFormulaTestCase formulaTestCase){
        String actualResult = formulaTestCase.getActualResult();
        if (StringUtils.isEmpty(actualResult)){
            return TEST_UNKNOWN;
        }
        String expectedResult = formatValue(formulaTestCase.getFormulaTestCase(), formulaTestCase.getExpectedResult());
        if (StringUtils.isEmpty(expectedResult)){
            if (StringUtils.isNotEmpty(actualResult)){
                return TEST_ERROR;
            }
        } else {
            if (StringUtils.isNotEmpty(actualResult)){
                if (compareResult(actualResult, expectedResult)){
                    return TEST_OK;
                } else {
                    return TEST_FAILURE;
                }
            } else {
                return TEST_ERROR;
            }
        }
        if (actualResult != null){
            return TEST_ERROR;
        }
        return TEST_UNKNOWN;
    }

    private boolean compareResult(String actualResult, String expectedResult) {
        try {
            ValueDatatype datatype = formula.findValueDatatype(ipsProject);
            if (datatype == null){
                throw new CoreException(new IpsStatus("Result datatype not found for formula: " + formula.getName()));
            }
            return datatype.compare(actualResult, expectedResult) == 0;
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return false;
        }
        
    }
    
    /*
     * Label provider for the formula test input value.
     */
    private class FormulaTestCaseTblLabelProvider extends LabelProvider implements ITableLabelProvider{
        public Image getColumnImage(Object element, int columnIndex) {
            if (! (element instanceof ExtDataForFormulaTestCase)){
                return null;
            }
            try {
                switch (columnIndex) {
                    case IDX_COLUMN_IMAGE:
                        ExtDataForFormulaTestCase formulaTestCase = (ExtDataForFormulaTestCase) element;
                        Image defaultImage = empytImage;
                        int result = getFormulaTestCaseTestStatus(formulaTestCase);
                        if (result == TEST_ERROR){
                            defaultImage = IpsPlugin.getDefault().getImage("obj16/testerr.gif"); //$NON-NLS-1$
                        } else if (result == TEST_OK){
                            defaultImage = IpsPlugin.getDefault().getImage("obj16/testok.gif"); //$NON-NLS-1$
                        } else if (result == TEST_FAILURE){
                            defaultImage = IpsPlugin.getDefault().getImage("obj16/testfail.gif"); //$NON-NLS-1$
                        }
                        MessageList msgList = formulaTestCase.validate();
                        // displays the validation image in the name column
                        return getImageForMsgList(defaultImage, msgList);
                }
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof ExtDataForFormulaTestCase){
                ExtDataForFormulaTestCase ftc = (ExtDataForFormulaTestCase) element;
                IFormula formula = ftc.getFormula();
                ValueDatatype vd = ValueDatatype.STRING;
                try {
                    vd = formula.findValueDatatype(ipsProject);
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
                
                if (columnIndex == IDX_COLUMN_NAME) {
                    return getTextInNullPresentationIfNull(ftc.getName());
                } else if (columnIndex == IDX_COLUMN_EXPECTED_RESULT) {
                    return IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter().formatValue(vd, ftc.getExpectedResult());
                } else if (columnIndex == IDX_COLUMN_ACTUAL_RESULT) {
                    return (String)ftc.getActualResult();
                }
            }
            return null;
        }
        
        private String getTextInNullPresentationIfNull(String value) {
            if (value==null) {
                value= IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
            }
            return value;
        } 
    }
    
    public FormulaTestCaseControl(Composite parent, UIToolkit uiToolkit,
            IpsObjectUIController uiController, IFormula formula) {
        
        super(parent, SWT.NONE);
        ArgumentCheck.notNull(new Object[]{ parent, uiToolkit, uiController, formula});
        this.formula = formula;
        this.ipsProject = formula.getIpsProject();
        this.uiToolkit = uiToolkit;
        this.uiController = uiController;
        this.empytImage = new Image(getShell().getDisplay(), 16, 16);

        // create images for ok and failure indicators
        //   colors are taken from the JUnit test runner to show a corporate identify for test support
        failureColor = new Color(getDisplay(), 159, 63, 63);
        okColor = new Color(getDisplay(), 95, 191, 95);
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        empytImage.dispose();
        
        for (Iterator iter = cachedProblemImageDescriptors.values().iterator(); iter.hasNext();) {
            ProblemImageDescriptor problemImageDescriptor = (ProblemImageDescriptor)iter.next();
            Image problemImage = IpsPlugin.getDefault().getImage(problemImageDescriptor);
            if (problemImage != null){
                problemImage.dispose();
            }
        }
        cachedProblemImageDescriptors.clear();   
        
        failureColor.dispose();
        okColor.dispose();
        
        super.dispose();
    }

    /**
     * Sets and updates the to be displaying formula test cases.
     */    
    public void storeFormulaTestCases(List newFormulaTestCases) {
        boolean changed = true;
        if (newFormulaTestCases.size() == formulaTestCases.size()) {
            changed = false;
            for (int i = 0; i < newFormulaTestCases.size(); i++) {
                if (!newFormulaTestCases.get(i).equals(
                        ((ExtDataForFormulaTestCase)formulaTestCases.get(i)).getFormulaTestCase())) {
                    changed = true;
                    break;
                }
            }
        }
        if (changed){
            this.formulaTestCases.clear();
            
            for (Iterator iter = newFormulaTestCases.iterator(); iter.hasNext();) {
                formulaTestCases.add(new ExtDataForFormulaTestCase((IFormulaTestCase)iter.next()));
            }
        } else {
            formulaTestCaseTableViewer.refresh();
            updateStatusOfUpdateButton(getSelectedFormulaTestCase());
        }
        calculateAndStoreActualResult();
        setTableInput(formulaTestCases);
        repackAndResfreshForumlaTestCaseTable();
    }

    /**
     * Creates the compoiste's controls. This method has to be called by this
     * controls client, after the control has been configured via the appropriate
     * setter method.
     */
    public void initControl() {
        setLayout(uiToolkit.createNoMarginGridLayout(1, false));
        setLayoutData(new GridData(GridData.FILL_BOTH));

        testStatusBar = uiToolkit.createVerticalSpacer(this, 5);
        
        Group formulaTestCaseGroup = uiToolkit.createGroup(this, Messages.FormulaTestCaseControl_GroupLabel_TestCases);

        Composite formulaTestCaseArea = uiToolkit.createComposite(formulaTestCaseGroup);
        formulaTestCaseArea.setLayout(uiToolkit.createNoMarginGridLayout(2, false));
        formulaTestCaseArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        createFormulaTestCaseTable(formulaTestCaseArea, uiToolkit);

        // create buttons
        Composite btns = uiToolkit.createComposite(formulaTestCaseArea);
        btns.setLayout(uiToolkit.createNoMarginGridLayout(1, true));
        btns.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        
        Button btnExecFormulaTestCase = uiToolkit.createButton(btns, Messages.FormulaTestCaseControl_Button_ExecuteAll);
        btnExecFormulaTestCase.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true ));
        btnExecFormulaTestCase.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                executeClicked();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });  

        uiToolkit.createVerticalSpacer(btns, 5);
        uiToolkit.createHorizonzalLine(btns);
        uiToolkit.createVerticalSpacer(btns, 5);
        
        if (formula != null){
            btnNewFormulaTestCase = uiToolkit.createButton(btns, Messages.FormulaTestCaseControl_Button_New);
            btnNewFormulaTestCase.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true ));
            btnNewFormulaTestCase.addSelectionListener(new SelectionListener() {
                public void widgetSelected(SelectionEvent e) {
                    newClicked();
                }
                public void widgetDefaultSelected(SelectionEvent e) {
                }
            });  
        }
        
        btnDeleteFormulaTestCase = uiToolkit.createButton(btns, Messages.FormulaTestCaseControl_Button_Delete);
        btnDeleteFormulaTestCase.setEnabled(false);
        btnDeleteFormulaTestCase.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true ));
        btnDeleteFormulaTestCase.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                deleteClicked();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        btnMoveFormulaTestCaseUp = uiToolkit.createButton(btns, Messages.FormulaTestCaseControl_Button_Up);
        btnMoveFormulaTestCaseUp.setEnabled(false);
        btnMoveFormulaTestCaseUp.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true ));
        btnMoveFormulaTestCaseUp.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                moveFormulaTestInputValues(getSelectedFormulaTestCase(), true);
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        btnMoveFormulaTestCaseDown = uiToolkit.createButton(btns, Messages.FormulaTestCaseControl_Button_Down);
        btnMoveFormulaTestCaseDown.setEnabled(false);
        btnMoveFormulaTestCaseDown.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true ));
        btnMoveFormulaTestCaseDown.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                moveFormulaTestInputValues(getSelectedFormulaTestCase(), false);
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        uiToolkit.createVerticalSpacer(btns, 5);
        
        btnUpdateFormulaTestCase = uiToolkit.createButton(btns, Messages.FormulaTestCaseControl_Button_Update);
        btnUpdateFormulaTestCase.setEnabled(false);
        btnUpdateFormulaTestCase.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, true ));
        btnUpdateFormulaTestCase.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                updateClicked();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        btnUpdateFormulaTestCase
                .setToolTipText(Messages.FormulaTestCaseControl_ToolTip_BtnUpdate);
        
        // create the formula test detail table, to display and editing the formula test input values
        compositeUiController = new CompositeUIController();
        compositeUiController.add(uiController);
        
        Group formulaTestInputGroup = uiToolkit.createGroup(this, Messages.FormulaTestCaseControl_GroupLabel_TestInput);
        formulaTestInputValuesControl = new FormulaTestInputValuesControl(formulaTestInputGroup, uiToolkit,
                compositeUiController);
        formulaTestInputValuesControl.setCanCalulateResult(true);
        formulaTestInputValuesControl.setCanStoreExpectedResult(false);
        formulaTestInputValuesControl.setCanStoreFormulaTestCaseAsNewFormulaTestCase(false);
        formulaTestInputValuesControl.initControl();
    }

    /*
     * Creates a new formula test case.
     */
    private void newClicked() {
        ArgumentCheck.notNull(formula);
        IFormulaTestCase newFormulaTestCase = formula.newFormulaTestCase();
        String name = newFormulaTestCase.generateUniqueNameForFormulaTestCase(Messages.FormulaTestInputValuesControl_DefaultFormulaTestCaseName);
        newFormulaTestCase.setName(name);
        try {
            String[] identifiers = formula.getParameterIdentifiersUsedInFormula(ipsProject);
            for (int i = 0; i < identifiers.length; i++) {
                IFormulaTestInputValue newInputValue = newFormulaTestCase.newFormulaTestInputValue();
                newInputValue.setIdentifier(identifiers[i]);
            }
            if (uiController != null) {
                uiController.updateUI();
            }
            
            formulaTestCaseTableViewer.setSelection(new StructuredSelection(newFormulaTestCase));
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /*
     * Moves the given formula test case up or down
     */
    private void moveFormulaTestInputValues(ExtDataForFormulaTestCase formulaTestCase, boolean up){
        int[] selectedIndexes = null;
        IFormula formula = formulaTestCase.getFormula();
        IFormulaTestCase[] ftcs = formula.getFormulaTestCases();
        for (int i = 0; i < ftcs.length; i++) {
            if (ftcs[i].equals(formulaTestCase.getFormulaTestCase())){
                selectedIndexes = new int[]{i};
                break;
            }
        }
        if (selectedIndexes != null){
            formula.moveFormulaTestCases(selectedIndexes, up);
            uiController.updateUI();
        }
    }
    
    /*
     * Update the currently selected formula test case store new formula test input values and
     * delete unnecessary parameters
     */
    protected void updateClicked() {
        ExtDataForFormulaTestCase selElement = getSelectedFormulaTestCase();
        try {
            IFormula formula = selElement.getFormula();
            String messageForChangeInfoDialog = buildMessageForUpdateInformation(selElement);
            if (selElement.addOrDeleteFormulaTestInputValues(formula.getParameterIdentifiersUsedInFormula(ipsProject))) {
                // there were changes, thus trigger that the input value table will be refreshed
                selectionFormulaTestCaseChanged(selElement);
                MessageDialog.openInformation(getShell(),
                        Messages.FormulaTestCaseControl_InformationDialogUpdateInputValues_Title,
                        messageForChangeInfoDialog);
            }

            // refresh ui
            repackAndResfreshForumlaTestCaseTable();  
            uiController.updateUI();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /* 
     * Generates and returns the message which informs about the new and deleted parameters
     */
    private String buildMessageForUpdateInformation(ExtDataForFormulaTestCase selElement) throws CoreException {
        IFormula formula = selElement.getFormula();
        String[] identifiersInFormula = formula.getParameterIdentifiersUsedInFormula(ipsProject);
        List idsInFormula = new ArrayList(identifiersInFormula.length);
        idsInFormula.addAll(Arrays.asList(identifiersInFormula));
        List idsInTestCase = new ArrayList();
        IFormulaTestInputValue[] inputValues = selElement.getFormulaTestInputValues();
        for (int i = 0; i < inputValues.length; i++) {
            boolean found = false;
            for (int j = idsInFormula.size()-1; j >= 0; j--) {
                if (idsInFormula.get(j).equals(inputValues[i].getIdentifier())){
                    idsInFormula.remove(j);
                    found = true;
                    break;
                }
            }
            if (!found){
                idsInTestCase.add(inputValues[i].getIdentifier());
            }
        }
        String newParams = ""; //$NON-NLS-1$
        for (Iterator iter = idsInFormula.iterator(); iter.hasNext();) {
            newParams += newParams.length() > 0 ? ", " : ""; //$NON-NLS-1$ //$NON-NLS-2$
            newParams += iter.next();
        }
        String delParams = ""; //$NON-NLS-1$
        for (Iterator iter = idsInTestCase.iterator(); iter.hasNext();) {
            delParams += delParams.length() > 0 ? ", " : ""; //$NON-NLS-1$ //$NON-NLS-2$
            delParams += iter.next();
        }
        String messageNewParameter = NLS.bind(
                Messages.FormulaTestCaseControl_InformationDialogUpdateInputValues_NewValueParams, newParams);
        String messageDelParameter = NLS.bind(
                Messages.FormulaTestCaseControl_InformationDialogUpdateInputValues_DeletedValueParams, delParams);
        String messageForChangeInfoDialog = Messages.FormulaTestCaseControl_InformationDialogUpdateInputValues_TextTop
                + (idsInFormula.size() > 0 ? messageNewParameter : "") //$NON-NLS-1$
                + (idsInFormula.size() > 0 && idsInTestCase.size() > 0 ? "\n" : "") //$NON-NLS-1$ //$NON-NLS-2$
                + (idsInTestCase.size() > 0 ? messageDelParameter : ""); //$NON-NLS-1$
        return messageForChangeInfoDialog;
    }
    
    /*
     * Execute the formula for all formula test cases
     */
    protected void executeClicked() {
        isCalculationErrorOrFailure = false;
        
        if (formulaTestCases.size() > 0
                && !FormulaTestInputValuesControl.checkPrecondition(this.getShell(), 
                        ((ExtDataForFormulaTestCase)formulaTestCases.get(0)).getFormulaTestCase())) {
            return;
        }
        
        Runnable calculate = new Runnable() {
            public void run() {
                if (isDisposed())
                    return;
                for (Iterator iter = formulaTestCases.iterator(); iter.hasNext();) {
                    ExtDataForFormulaTestCase element = (ExtDataForFormulaTestCase)iter.next();
                    Object result = ""; //$NON-NLS-1$
                    try {
                        IFormula formula = element.getFormula();
                        MessageList mlformula = formula.validate(formula.getIpsProject());
                        if (formula.isValid()) {
                            MessageList ml = element.validate();
                            if (ml.getNoOfMessages() == 0) {
                                result = element.execute();
                            }
                        } else {
                            element.setMessage(mlformula.getFirstMessage(Message.ERROR).getText());
                        }
                        ValueDatatype vd = formula.findValueDatatype(ipsProject);
                        element.setActualResult(IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter().formatValue(
                                vd, (String)(result == null ? null : result.toString())));
                    } catch (Exception e) {
                        IpsPlugin.logAndShowErrorDialog(e);
                    }
                    int testResultStatus = getFormulaTestCaseTestStatus(element);
                    if (testResultStatus != TEST_OK){
                        isCalculationErrorOrFailure = true;
                    }
                }
                repackAndResfreshForumlaTestCaseTable();
            }
        };
        BusyIndicator.showWhile(getDisplay(), calculate);

        if (isCalculationErrorOrFailure){
            testStatusBar.setBackground(failureColor);
        } else {
            testStatusBar.setBackground(okColor);
        }
    }

    /*
     * The delete button was clicked
     */
    private void deleteClicked() {
        ExtDataForFormulaTestCase selElement = getSelectedFormulaTestCase();
        if (selElement == null){
            return;
        }
        // get the object which will be selected after the delete
        int idxBeforeLast = formulaTestCases.size()>1?formulaTestCases.size()-2:0;
        ExtDataForFormulaTestCase nextElement = (ExtDataForFormulaTestCase) formulaTestCases.get(idxBeforeLast);
        for (int i = formulaTestCases.size()-1; i >= 0 ; i--) {
            if (selElement.equals(formulaTestCases.get(i))){
                break;
            }
            nextElement = (ExtDataForFormulaTestCase) formulaTestCases.get(i);
        }
        
        formulaTestCases.remove(selElement);
        selElement.delete();
        repackAndResfreshForumlaTestCaseTable();
        
        // select the next object which was evaluated before
        if (nextElement!=null){
            formulaTestCaseTableViewer.setSelection(new StructuredSelection(nextElement));
        } 
        if (formulaTestCases.size() == 0){
            formulaTestInputValuesControl.storeFormulaTestCase(null);
        }
        uiController.updateUI();
    }

    /*
     * Returns the first selected formula test case or <code>null</code> if nothing is selected.
     */
    private ExtDataForFormulaTestCase getSelectedFormulaTestCase(){
        ISelection selection = formulaTestCaseTableViewer.getSelection();
        if (selection instanceof IStructuredSelection) {
            ExtDataForFormulaTestCase selectedFromulaTestCase = (ExtDataForFormulaTestCase)((IStructuredSelection)selection)
                    .getFirstElement();
            return selectedFromulaTestCase;
        }
        return null;
    }
    
    /*
     * Creates the table to dipsplay and editing the formula test case.
     */
    private void createFormulaTestCaseTable(Composite c, UIToolkit uiToolkit) {
        Table table = new Table(c, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible (true);
        table.setLinesVisible (true);
        
        // create the columns of the table
        TableColumn column = new TableColumn(table, SWT.LEFT);
        column.setText(""); //$NON-NLS-1$
        column = new TableColumn(table, SWT.LEFT);
        column.setText(Messages.FormulaTestCaseControl_TableTestCases_Column_Name);
        column = new TableColumn(table, SWT.LEFT);
        column.setText(Messages.FormulaTestCaseControl_TableTestCases_ColumnExpectedResult);
        column = new TableColumn(table, SWT.LEFT);
        column.setText(Messages.FormulaTestCaseControl_TableTestCases_Column_ActualResult);
        
        // Create the viewer and connect it to the view
        formulaTestCaseTableViewer = new TableViewer(table);
        formulaTestCaseTableViewer.setContentProvider (new ArrayContentProvider());
        formulaTestCaseTableViewer.setLabelProvider (new FormulaTestCaseTblLabelProvider());
        
        // create the cell editor
        createTableCellModifier(uiToolkit);
        
        hookFormulaTestCaseTableListener();     
        
        setTableInput(new ArrayList());
        
        // pack the table
        repackAndResfreshForumlaTestCaseTable();
    }

    private void createTableCellModifier(UIToolkit uiToolkit) {
        ValueDatatype resultValueDatatype = ValueDatatype.STRING;
        try {
            resultValueDatatype = formula.findValueDatatype(ipsProject);
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        BeanTableCellModifier tableCellModifier = new BeanTableCellModifier(formulaTestCaseTableViewer, this);
        tableCellModifier.initModifier(uiToolkit, new String[] { null, IFormulaTestCase.PROPERTY_NAME,
                IFormulaTestCase.PROPERTY_EXPECTED_RESULT, PROPERTY_ACTUAL_RESULT }, new ValueDatatype[] { null,
                ValueDatatype.STRING, resultValueDatatype, null });
        tableCellModifier.addListener(this);
    }

    /*
     * Adds the listener to the formula test case table
     */
    private void hookFormulaTestCaseTableListener() {
        // add listener to the table view
        formulaTestCaseTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            Object prevObject;
            public void selectionChanged(SelectionChangedEvent event) {
                if (event.getSelection() instanceof IStructuredSelection){
                    final Object selObject = ((IStructuredSelection)event.getSelection()).getFirstElement();
                    if (prevObject != selObject) {
                        prevObject = selObject;
                        selectionFormulaTestCaseChanged((ExtDataForFormulaTestCase)selObject);
                    }
                }
            }
        });
        
        new TableMessageHoverService(formulaTestCaseTableViewer) {
            protected MessageList getMessagesFor(Object element) throws CoreException {
                if (element instanceof ExtDataForFormulaTestCase) {
                    ExtDataForFormulaTestCase ftc = (ExtDataForFormulaTestCase)element;
                    MessageList ml = validateElement(ftc.getFormulaTestCase());
                    int status = getFormulaTestCaseTestStatus(ftc);
                    if (status == TEST_FAILURE) {
                        Object actualResult = ftc.getActualResult();
                        String actualResultStr = actualResult == null ? ""+null : actualResult.toString(); //$NON-NLS-1$
                        String text = NLS.bind(Messages.FormulaTestCaseControl_TestFailureMessage_ExpectedButWas, formatValue(ftc.getFormulaTestCase(), ftc
                                .getExpectedResult()), actualResultStr);
                        Message msg = new Message("NONE", text, Message.INFO, this, PROPERTY_ACTUAL_RESULT); //$NON-NLS-1$
                        ml.add(msg);
                    } else if (status == TEST_ERROR) {
                        String message = ftc.getMessage();
                        if (StringUtils.isNotEmpty(message) && StringUtils.isNotEmpty(ftc.getExpectedResult())) {
                            // the expected result is not empty, but there is an error (message),
                            // display the error
                            Message msg = new Message("NONE", message, Message.INFO, this, PROPERTY_ACTUAL_RESULT); //$NON-NLS-1$
                            ml.add(msg);
                        } else if (StringUtils.isEmpty(ftc.getExpectedResult())) {
                            String text = Messages.FormulaTestCaseControl_TestError_NoExpectedResultGiven;
                            Message msg = new Message("NONE", text, Message.INFO, this, PROPERTY_ACTUAL_RESULT); //$NON-NLS-1$
                            ml.add(msg);
                        } else {
                            Object actualResult = ftc.getActualResult();
                            String actualResultStr = actualResult == null ? ""+null : actualResult.toString(); //$NON-NLS-1$
                            String text = NLS.bind(Messages.FormulaTestCaseControl_TestFailureMessage_ExpectedButWas, ftc
                                    .getExpectedResult(), actualResultStr);
                            Message msg = new Message("NONE", text, Message.INFO, this, PROPERTY_ACTUAL_RESULT); //$NON-NLS-1$
                            ml.add(msg);
                        }
                    }
                    return ml;
                } else
                    return null;
            }
        };
    }

    private String formatValue(IFormulaTestCase ftc, String value) {
        String expectedResult = null;
        try {
            expectedResult = ftc.getExpectedResult();
            IFormula formula = ftc.getFormula();
            expectedResult = IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter().formatValue(formula.findValueDatatype(ipsProject),
                    (String)(expectedResult == null ? null : expectedResult.toString()));
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return expectedResult;
    }
    
    /*
     * Method to indicate that the selection in the formula test case table has changed
     */
    protected void selectionFormulaTestCaseChanged(ExtDataForFormulaTestCase selectedFormulaTestCase) {
        if (selectedFormulaTestCase == null) {
            btnDeleteFormulaTestCase.setEnabled(false);
            btnMoveFormulaTestCaseUp.setEnabled(false);
            btnMoveFormulaTestCaseDown.setEnabled(false);
            btnUpdateFormulaTestCase.setEnabled(false);
            return;
        }
        formulaTestInputValuesControl.storeFormulaTestCase(selectedFormulaTestCase.getFormulaTestCase());
        
        btnDeleteFormulaTestCase.setEnabled(true);
        btnMoveFormulaTestCaseUp.setEnabled(true);
        btnMoveFormulaTestCaseDown.setEnabled(true);
        updateStatusOfUpdateButton(selectedFormulaTestCase);
        
        // if the data is not changeable disable all buttons in any case
        if (!isDataChangeable()){
            btnDeleteFormulaTestCase.setEnabled(false);
            btnMoveFormulaTestCaseUp.setEnabled(false);
            btnMoveFormulaTestCaseDown.setEnabled(false);
            btnUpdateFormulaTestCase.setEnabled(false);
        }
    }

    /*
     * Updates the status of the update button to enabled or disabled. Enable the button if
     * there is an mismatch between the formulas and the formula test case parameters
     */
    private void updateStatusOfUpdateButton(ExtDataForFormulaTestCase selectedFormulaTestCase) {
        if (btnUpdateFormulaTestCase == null){
            return;
        }
        btnUpdateFormulaTestCase.setEnabled(false);
        if (selectedFormulaTestCase == null){
            return;
        }
        try {
            btnUpdateFormulaTestCase.setEnabled(false);
            MessageList ml;
            ml = selectedFormulaTestCase.validate();
            if (ml.getMessageByCode(IFormulaTestCase.MSGCODE_IDENTIFIER_MISMATCH) != null){
                btnUpdateFormulaTestCase.setEnabled(true);
            }             
        } catch (CoreException e) {
            // exception ignored, the validation exception will not be diplayed here
        }
    }
    
    /*
     * Repacks the columns in the table
     */
    private void repackAndResfreshForumlaTestCaseTable() {
        for (int i = 0, n = formulaTestCaseTableViewer.getTable().getColumnCount(); i < n; i++) {
            formulaTestCaseTableViewer.getTable().getColumn(i).pack();
        }
        resetTestRunColor();
        formulaTestCaseTableViewer.refresh();
    }

    private void setTableInput(List formulaTestCases) {
        formulaTestCaseTableViewer.setInput(formulaTestCases);
    }

    private void resetTestRunColor() {
        // resets the color of the last test run
        testStatusBar.setBackground(getBackground());
    }

    /*
     * Returns the image for the given message list (e.g. if there is an error return a problem image)
     */
    private Image getImageForMsgList(Image defaultImage, MessageList msgList) {
        // get the cached problem descriptor for the base image
        String key = getKey(defaultImage, msgList.getSeverity());
        ProblemImageDescriptor descriptor = (ProblemImageDescriptor) cachedProblemImageDescriptors.get(key);
        if (descriptor == null && defaultImage != null){
            descriptor = new ProblemImageDescriptor(defaultImage, msgList.getSeverity());
            cachedProblemImageDescriptors.put(key, descriptor);
        }
        return IpsPlugin.getDefault().getImage(descriptor);
    } 
    
    /*
     * Returns an unique key for the given image and severity compination.
     */
    private String getKey(Image image, int severity) {
        if (image == null){
            return null;
        }
        return image.hashCode() + "_" + severity; //$NON-NLS-1$
    }     

    /*
     * Performs and returns validation messages on the given element.
     */
    private MessageList validateElement(Object element) throws CoreException{
        MessageList messageList = new MessageList();
        // validate element
        if (element instanceof IIpsObjectPartContainer){
            messageList.add(((IIpsObjectPartContainer)element).validate(ipsProject));
        }
        return messageList;
    }
    
    public void valueChanged(ColumnIdentifier columnIdentifier, Object value){
        // resets the color of the last test run
        ExtDataForFormulaTestCase tc = getSelectedFormulaTestCase();
        if (tc != null){
            tc.setActualResult(""); //$NON-NLS-1$
        }
        resetTestRunColor();
    }
    
    private void calculateAndStoreActualResult(){
        ExtDataForFormulaTestCase tc = getSelectedFormulaTestCase();
        if (tc != null){
            tc.setActualResult((String)formulaTestInputValuesControl.calculateFormulaIfValid(ipsProject)); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setDataChangeable(boolean changeable) {
        this.dataChangeable = changeable;
        
        // trigger update state of buttons
        selectionFormulaTestCaseChanged(getSelectedFormulaTestCase());
        
        uiToolkit.setDataChangeable(btnNewFormulaTestCase, changeable);
        formulaTestInputValuesControl.setDataChangeable(changeable);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDataChangeable() {
        return dataChangeable;
    }
}
