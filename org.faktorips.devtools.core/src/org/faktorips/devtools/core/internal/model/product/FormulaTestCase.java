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

package org.faktorips.devtools.core.internal.model.product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.product.IFormula;
import org.faktorips.devtools.core.model.product.IFormulaTestCase;
import org.faktorips.devtools.core.model.product.IFormulaTestInputValue;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.fl.DefaultIdentifierResolver;
import org.faktorips.fl.ExcelFunctionsResolver;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.ExprEvaluator;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FormulaTestCase extends IpsObjectPart implements IFormulaTestCase {

    /** Tags */
    final static String TAG_NAME = "FormulaTestCase"; //$NON-NLS-1$
    
    private String expectedResult = ""; //$NON-NLS-1$
    
    private List formulaTestInputValues = new ArrayList(0);
    
    public FormulaTestCase(Formula parent, int id) {
        super(parent, id);
    }
    
    /**
     * {@inheritDoc}
     */
    public IFormula getFormula() {
        return (IFormula)getParent();
    }

    /**
     * {@inheritDoc}
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    /**
     * {@inheritDoc}
     */
    protected void reinitPartCollections() {
        formulaTestInputValues = new ArrayList();
    }
    
    /**
     * {@inheritDoc}
     */
    public IIpsObjectPart newPart(Class partType) {
        throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    protected IIpsObjectPart newPart(Element partEl, int id) {
        String xmlTagName = partEl.getNodeName();
        if (FormulaTestInputValue.TAG_NAME.equals(xmlTagName)) {
            return newFormulaTestInputValueInternal(id);
        }  else if (PROPERTY_EXPECTED_RESULT.equalsIgnoreCase(xmlTagName)){
            // ignore expected result nodes, will be parsed in the this#initPropertiesFromXml method
            return null;
        }
        throw new RuntimeException("Could not create part for tag name: " + xmlTagName); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected void reAddPart(IIpsObjectPart part) {
        if (part instanceof IFormulaTestInputValue) {
            formulaTestInputValues.add(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    protected void removePart(IIpsObjectPart part) {
        if (part instanceof IFormulaTestInputValue) {
            formulaTestInputValues.remove(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public IIpsElement[] getChildren() {
        List childrenList = new ArrayList(formulaTestInputValues.size());
        childrenList.addAll(formulaTestInputValues);
        return (IIpsElement[]) childrenList.toArray(new IIpsElement[0]);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute(PROPERTY_NAME);
        expectedResult = ValueToXmlHelper
                .getValueFromElement(element, StringUtils.capitalise(PROPERTY_EXPECTED_RESULT));
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_NAME, name);
        ValueToXmlHelper.addValueToElement(expectedResult, element, StringUtils.capitalise(PROPERTY_EXPECTED_RESULT));
    }
    
    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("Formula.gif"); //$NON-NLS-1$
    }
    
    /*
     * Returns the expression compiler used to compile the formula preview result.
     */
    private ExprCompiler getPreviewExprCompiler(IIpsProject ipsProject) throws CoreException {
        ExprCompiler compiler = new ExprCompiler();
        compiler.add(new ExcelFunctionsResolver(ipsProject.getExpressionLanguageFunctionsLanguage()));
        
        // add the table functions based on the table usages defined in the product cmpt type
        IProductCmptGeneration gen = getFormula().getProductCmptGeneration();
        if (gen != null) {
            compiler.add(new TableFunctionsFormulaTestResolver(getIpsProject(), gen.getTableContentUsages(), this));
        }
        
        IFormulaTestInputValue[] input = getFormulaTestInputValues();
        DefaultIdentifierResolver resolver = new DefaultIdentifierResolver();
        for (int i = 0; i < input.length; i++) {
            String storedValue = input[i].getValue();
            // get the datatype and the helper for generating the code fragment of the formula
            Datatype datatype = input[i].findDatatypeOfFormulaParameter(ipsProject);
            DatatypeHelper dataTypeHelper = getIpsProject().getDatatypeHelper(datatype);
            if (dataTypeHelper == null) {
                throw new CoreException(new IpsStatus(NLS.bind(
                        Messages.FormulaTestCase_CoreException_DatatypeNotFoundOrWrongConfigured, datatype,
                        input[i].getIdentifier())));
            }
            resolver.register(input[i].getIdentifier(), dataTypeHelper.newInstance(storedValue), datatype);
        }
        
        compileAndAddAllEnumDatatypeValueIdentifier(resolver);
        
        compiler.setIdentifierResolver(resolver);

        return compiler;
    }
    
    /*
     * Add all identifier for enum values 
     */
    private void compileAndAddAllEnumDatatypeValueIdentifier(DefaultIdentifierResolver resolver) {
        try {
            EnumDatatype[] enumTypes = getIpsProject().findEnumDatatypes();
            for (int i = 0; i < enumTypes.length; i++) {
                String valueName = enumTypes[i].getName();
                List valueIds = Arrays.asList(enumTypes[i].getAllValueIds(true));
                for (Iterator iter = valueIds.iterator(); iter.hasNext();) {
                    String id = (String)iter.next();
                    JavaCodeFragment frag = new JavaCodeFragment();
                    frag.getImportDeclaration().add(enumTypes[i].getJavaClassName());
                    DatatypeHelper helper = getIpsProject().getDatatypeHelper(enumTypes[i]);
                    frag.append(helper.newInstance(id));
                    resolver.register(valueName + "." + id, frag, enumTypes[i]); //$NON-NLS-1$
                }
            }
        }
        catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }    
    
    /**
     * {@inheritDoc}
     */
    public Object execute(IIpsProject ipsProject) throws Exception {
        ExprEvaluator processor = getExprEvaluatorInternal(ipsProject.getClassLoaderForJavaProject(), ipsProject);
        return processor.evaluate(getFormula().getExpression());
    }

    /**
     * Executes the given java code fragment.
     */
    public Object execute(JavaCodeFragment javaCodeFragment, ClassLoader classLoader, IIpsProject ipsProject) throws Exception {
        ExprEvaluator processor = getExprEvaluatorInternal(classLoader, ipsProject);
        return processor.evaluate(javaCodeFragment);
    }
    
    private ExprEvaluator getExprEvaluatorInternal(ClassLoader classLoader, IIpsProject ipsProject) throws CoreException {
        ExprCompiler compiler = getPreviewExprCompiler(ipsProject);
        return new ExprEvaluator(compiler, classLoader);
    }

    /**
     * {@inheritDoc}
     */
    public void setName(String name) {
        String oldName = this.name;
        this.name = name;
        valueChanged(oldName, name);
    }

    /**
     * {@inheritDoc}
     */
    public IFormulaTestInputValue getFormulaTestInputValue(String identifier) {
        for (Iterator it = formulaTestInputValues.iterator(); it.hasNext();) {
            IFormulaTestInputValue v = (IFormulaTestInputValue) it.next();
            if (v.getIdentifier().equals(identifier)) {
                return v;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IFormulaTestInputValue[] getFormulaTestInputValues() {
        return (IFormulaTestInputValue[]) formulaTestInputValues.toArray(new IFormulaTestInputValue[0]);
    }

    /**
     * {@inheritDoc}
     */
    public IFormulaTestInputValue newFormulaTestInputValue() {
        IFormulaTestInputValue v = newFormulaTestInputValueInternal(getNextPartId());
        objectHasChanged();
        return v;
    }

    /*
     * Creates a new formula test input value without updating the source file.
     */
    private IFormulaTestInputValue newFormulaTestInputValueInternal(int nextPartId) {
        IFormulaTestInputValue v = new FormulaTestInputValue(this, nextPartId);
        formulaTestInputValues.add(v);
        return v;
    }

    /**
     * {@inheritDoc}
     */
    public String getExpectedResult() {
        return expectedResult;
    }

    /**
     * {@inheritDoc}
     */
    public void setExpectedResult(String expectedResult) {
        String oldExpectedResult = this.expectedResult;
        this.expectedResult = expectedResult;
        valueChanged(oldExpectedResult, expectedResult);
    }

    /**
     * {@inheritDoc}
     */
    public boolean addOrDeleteFormulaTestInputValues(String[] newIdentifiers, IIpsProject ipsProject) {
        boolean changed = false;
        
        // add new or existing value on the given position
        List newListOfInputValues = new ArrayList();
        changed = updateWithAllIdentifiers(newIdentifiers, newListOfInputValues, ipsProject);
        
        // store new list
        formulaTestInputValues = newListOfInputValues;
        
        if (changed){
            objectHasChanged();
        }
        
        return changed;
    }
    
    /**
     * Adds all of input values to the given list, returns <code>true</code> if there were changes.
     */
    private boolean updateWithAllIdentifiers(String[] newIdentifiers, List newListOfInputValues, IIpsProject ipsProject){
        boolean changed = false;
        List oldInputValues = new ArrayList();
        oldInputValues.addAll(formulaTestInputValues);
        
        for (int i = 0; i < newIdentifiers.length; i++) {
            IFormulaTestInputValue inputValue = getFormulaTestInputValue(newIdentifiers[i]);
            if (inputValue == null){
                inputValue = newFormulaTestInputValue();
                inputValue.setIdentifier(newIdentifiers[i]);
                // try to set the default value depending on the corresponding value datatype
                try {
                    Datatype datatype = inputValue.findDatatypeOfFormulaParameter(ipsProject);
                    if (datatype instanceof ValueDatatype){
                        inputValue.setValue(((ValueDatatype)datatype).getDefaultValue());
                    }
                    // ignore if the datatype is not value datatype
                    //   this is a validation error see FormulaTestInputValue#validateThis method
                } catch (CoreException e) {
                    // ignore exception if the datatype wasn't found, this error will be handled as validation error
                    // see FormulaTestInputValue#validateThis method
                }
                changed = true;
            } else {
                int idxOld = formulaTestInputValues.indexOf(inputValue);
                oldInputValues.remove(inputValue);
                if (idxOld != i){
                    changed = true;
                }
            }
            newListOfInputValues.add(inputValue);
        }
        // delete old input value
        for (Iterator iter = oldInputValues.iterator(); iter.hasNext();) {
            IFormulaTestInputValue oldInputValue = (IFormulaTestInputValue)iter.next();
            oldInputValue.delete();
            changed = true;
        }
        return changed;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFormulaTestCaseEmpty() {
        if (formulaTestInputValues.size() == 0){
            return true;
        }
        for (Iterator iter = formulaTestInputValues.iterator(); iter.hasNext();) {
            FormulaTestInputValue element = (FormulaTestInputValue)iter.next();
            if (StringUtils.isNotEmpty(element.getValue())){
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String generateUniqueNameForFormulaTestCase(String nameProposal) {
        String uniqueName = nameProposal;

        int idx = 2;
        IFormulaTestCase[] ftcs = getFormula().getFormulaTestCases();
        for (int i = 0; i < ftcs.length; i++) {
            if (! (ftcs[i] == this) && ftcs[i].getName().equals(uniqueName)){
                uniqueName = nameProposal + " (" + idx++ + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                i = -1;
            }
        }
        return uniqueName;
    }

    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);
        IIpsProject ipsProject = getIpsProject();
        IFormula formula = getFormula();
        
        // check for duplicase formula test case names
        IFormulaTestCase[] ftcs = getFormula().getFormulaTestCases();
        // v2 - this code has to be moved to formula
        for (int i = 0; i < ftcs.length; i++) {
            if (! (ftcs[i] == this) && ftcs[i].getName().equals(name)){
                String text = NLS.bind(Messages.FormulaTestCase_ValidationMessage_DuplicateFormulaTestCaseName, name);
                list.add(new Message(MSGCODE_DUPLICATE_NAME, text, Message.ERROR, this, PROPERTY_NAME));
                break;
            }
        }
        
        // check that the formula test input values matches the identifier in the formula
        boolean isIdentifierMismatch = false;
        String[] identifierInFormula = formula.getParameterIdentifiersUsedInFormula(ipsProject);
        if (identifierInFormula.length != formulaTestInputValues.size()){
            isIdentifierMismatch = true;
        }
        for (int i = 0; i < identifierInFormula.length; i++) {
            if (getFormulaTestInputValue(identifierInFormula[i]) == null){
                isIdentifierMismatch = true;
                break;
            }
        }
        if (isIdentifierMismatch) {
            String text = NLS.bind(
                    Messages.FormulaTestCase_ValidationMessage_MismatchBetweenFormulaInputValuesAndIdentifierInFormula,
                    name, getFormula().getName());
            list.add(new Message(MSGCODE_IDENTIFIER_MISMATCH, text, Message.WARNING, this, PROPERTY_NAME));
        }
    }
}
