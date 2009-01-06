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

package org.faktorips.devtools.core.internal.model.bf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.ipsobject.BaseIpsObject;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IActionBFE;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.bf.IDecisionBFE;
import org.faktorips.devtools.core.model.bf.IParameterBFE;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

public class BusinessFunction extends BaseIpsObject implements IBusinessFunction {

    private BFElementIpsObjectPartCollection simpleElements;
    private BFElementIpsObjectPartCollection actions;
    private BFElementIpsObjectPartCollection decisions;
    private BFElementIpsObjectPartCollection parameters;
    private BFElementIpsObjectPartCollection controlFlows;
    private Dimension parameterRectangleSize = new Dimension(100, 100);
    private Point parameterRectangleLocation = new Point(10, 10);

    public BusinessFunction(IIpsSrcFile file) {
        super(file);
        simpleElements = new BFElementIpsObjectPartCollection(this, BFElement.class, IBFElement.class,
                IBFElement.XML_TAG);
        actions = new BFElementIpsObjectPartCollection(this, ActionBFE.class, IActionBFE.class, IActionBFE.XML_TAG);
        decisions = new BFElementIpsObjectPartCollection(this, DecisionBFE.class, IDecisionBFE.class,
                IDecisionBFE.XML_TAG);
        parameters = new BFElementIpsObjectPartCollection(this, ParameterBFE.class, IParameterBFE.class,
                IParameterBFE.XML_TAG);
        controlFlows = new BFElementIpsObjectPartCollection(this, ControlFlow.class, IControlFlow.class,
                IControlFlow.XML_TAG);
    }

    public Dimension getParameterRectangleSize() {
        return parameterRectangleSize;
    }

    public void setParameterRectangleSize(Dimension parameterRectangleSize) {
        Dimension old = this.parameterRectangleSize;
        this.parameterRectangleSize = parameterRectangleSize;
        valueChanged(old, parameterRectangleSize);
    }

    // TODO test
    public IBFElement getStart() {
        for (IIpsObjectPart part : simpleElements.getParts()) {
            IBFElement element = (IBFElement)part;
            if (element.getType().equals(BFElementType.START)) {
                return element;
            }
        }
        return null;
    }

    // TODO test
    public IBFElement getEnd() {
        for (IIpsObjectPart part : simpleElements.getParts()) {
            IBFElement element = (IBFElement)part;
            if (element.getType().equals(BFElementType.END)) {
                return element;
            }
        }
        return null;
    }

    public Point getParameterRectangleLocation() {
        return parameterRectangleLocation;
    }

    public List<IParameterBFE> getParameterBFEs() {
        ArrayList<IParameterBFE> returnValue = new ArrayList<IParameterBFE>();
        for (IIpsObjectPart parameterBFE : parameters.getParts()) {
            returnValue.add((IParameterBFE)parameterBFE);
        }
        return returnValue;
    }

    public IParameterBFE getParameterBFE(String name) {
        for (IIpsObjectPart parameterBFE : parameters.getParts()) {
            if (parameterBFE.getName().equals(name)) {
                return (IParameterBFE)parameterBFE;
            }
        }
        return null;
    }

    public IBFElement getBFElement(Integer id) {
        if (id == null) {
            return null;
        }
        IBFElement element = (IBFElement)simpleElements.getPartById(id);
        if (element == null) {
            element = (IBFElement)actions.getPartById(id);
        }
        if (element == null) {
            element = (IBFElement)decisions.getPartById(id);
        }
        if (element == null) {
            element = (IBFElement)parameters.getPartById(id);
        }
        return element;
    }

    public IControlFlow newControlFlow() {
        return (IControlFlow)controlFlows.newPart();
    }

    public IControlFlow getControlFlow(int id) {
        IIpsObjectPart[] parts = controlFlows.getParts();
        for (IIpsObjectPart ipsObjectPart : parts) {
            if (id == ipsObjectPart.getId()) {
                return (IControlFlow)ipsObjectPart;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<IControlFlow> getControlFlows() {
        IIpsObjectPart[] controlFlowParts = controlFlows.getParts();
        ArrayList<IControlFlow> controlFlowList = new ArrayList<IControlFlow>(controlFlowParts.length);
        controlFlowList.addAll((Collection)Arrays.asList(controlFlowParts));
        return controlFlowList;
    }

    public IBFElement newEnd(Point location) {
        BFElement element = (BFElement)simpleElements.newBFElement(location, BFElementType.END);
        element.setSize(new Dimension(30, 30));
        return element;
    }

    public IBFElement newMerge(Point location) {
        return (BFElement)simpleElements.newBFElement(location, BFElementType.MERGE);
    }

    public IBFElement newStart(Point location) {
        BFElement element = (BFElement)simpleElements.newBFElement(location, BFElementType.START);
        element.setSize(new Dimension(30, 30));
        return element;
    }

    public IActionBFE newOpaqueAction(Point location) {
        ActionBFE element = (ActionBFE)actions.newBFElement(location, BFElementType.ACTION_INLINE);
        return element;
    }

    public IActionBFE newMethodCallAction(Point location) {
        ActionBFE element = (ActionBFE)actions.newBFElement(location, BFElementType.ACTION_METHODCALL);
        return element;
    }

    public IActionBFE newBusinessFunctionCallAction(Point location) {
        ActionBFE element = (ActionBFE)actions.newBFElement(location, BFElementType.ACTION_BUSINESSFUNCTIONCALL);
        return element;
    }

    public IDecisionBFE newDecision(Point location) {
        DecisionBFE element = (DecisionBFE)decisions.newBFElement(location, BFElementType.DECISION);
        return element;
    }

    public IParameterBFE newParameter() {
        ParameterBFE element = (ParameterBFE)parameters.newBFElement(null, BFElementType.PARAMETER);
        return element;
    }

    // TODO test
    @SuppressWarnings("unchecked")
    public List<IBFElement> getBFElementsWithoutParameters() {

        List<IBFElement> nodeList = new ArrayList<IBFElement>();
        IIpsObjectPart[] bFParts = simpleElements.getParts();
        nodeList.addAll((Collection)Arrays.asList(bFParts));
        bFParts = actions.getParts();
        nodeList.addAll((Collection)Arrays.asList(bFParts));
        bFParts = decisions.getParts();
        nodeList.addAll((Collection)Arrays.asList(bFParts));
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    public List<IBFElement> getBFElements() {
        List<IBFElement> nodeList = new ArrayList<IBFElement>();
        IIpsObjectPart[] bFParts = simpleElements.getParts();
        nodeList.addAll((Collection)Arrays.asList(bFParts));
        bFParts = actions.getParts();
        nodeList.addAll((Collection)Arrays.asList(bFParts));
        bFParts = decisions.getParts();
        nodeList.addAll((Collection)Arrays.asList(bFParts));
        bFParts = parameters.getParts();
        nodeList.addAll((Collection)Arrays.asList(bFParts));
        return nodeList;
    }

    public IpsObjectType getIpsObjectType() {
        return BusinessFunctionIpsObjectType.getInstance();
    }

    @Override
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        int width = Integer.parseInt(element.getAttribute("parameterRectangleWidth")); //$NON-NLS-1$
        int height = Integer.parseInt(element.getAttribute("parameterRectangleHeight")); //$NON-NLS-1$
        parameterRectangleSize = new Dimension(width, height);
        int xLocation = Integer.parseInt(element.getAttribute("parameterRectangleX")); //$NON-NLS-1$
        int yLocation = Integer.parseInt(element.getAttribute("parameterRectangleY")); //$NON-NLS-1$
        parameterRectangleLocation = new Point(xLocation, yLocation);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute("parameterRectangleWidth", String.valueOf(getParameterRectangleSize().width)); //$NON-NLS-1$
        element.setAttribute("parameterRectangleHeight", String.valueOf(getParameterRectangleSize().height)); //$NON-NLS-1$
        element.setAttribute("parameterRectangleX", String.valueOf(getParameterRectangleLocation().x)); //$NON-NLS-1$
        element.setAttribute("parameterRectangleY", String.valueOf(getParameterRectangleLocation().y)); //$NON-NLS-1$
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        validateBFElementsConnected(list);
        validateOnlyOneElementAllowed(list, BFElementType.START, MSGCODE_START_SINGLE_OCCURRENCE);
        validateOnlyOneElementAllowed(list, BFElementType.END, MSGCODE_END_SINGLE_OCCURRENCE);
        validateBFElementNameCollision(list);
    }

    private void validateBFElementNameCollision(MessageList msgList) {
        Map<String, List<IBFElement>> elements = new HashMap<String, List<IBFElement>>();

        for (IBFElement element : getBFElements()) {
            String key;
            if (element.getType().equals(BFElementType.START) || element.getType().equals(BFElementType.END)
                    || element.getType().equals(BFElementType.PARAMETER)) {
                continue;
            } else if (element.getType().equals(BFElementType.ACTION_METHODCALL)) {
                key = ((IActionBFE)element).getExecutableMethodName();
            } else if (element.getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)) {
                key = ((IActionBFE)element).getTarget();
            } else {
                // decision, merge, inline action,
                key = element.getName();
            }
            List<IBFElement> list = getValue(elements, key);
            list.add(element);
        }

        for (String key : elements.keySet()) {
            List<IBFElement> list = elements.get(key);
            if (list.size() > 1) {
                for (IBFElement element : list) {
                    if (!(checkIfOnlyMethodCallActions(list) || checkIfOnlyBusinessFunctionCallActions(list))) {
                        String text = NLS.bind(Messages.getString("BusinessFunction.duplicateNames"), key); //$NON-NLS-1$
                        msgList.add(new Message(MSGCODE_ELEMENT_NAME_COLLISION, text, Message.ERROR, element));
                    }
                }
            }
        }
    }

    private boolean checkIfOnlyMethodCallActions(List<IBFElement> list) {
        for (IBFElement element : list) {
            if (!element.getType().equals(BFElementType.ACTION_METHODCALL)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkIfOnlyBusinessFunctionCallActions(List<IBFElement> list) {
        for (IBFElement element : list) {
            if (!element.getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)) {
                return false;
            }
        }
        return true;
    }

    private List<IBFElement> getValue(Map<String, List<IBFElement>> elements, String key) {
        List<IBFElement> list = (List<IBFElement>)elements.get(key);
        if (list == null) {
            list = new ArrayList<IBFElement>();
            elements.put(key, list);
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private void validateOnlyOneElementAllowed(MessageList msgList, BFElementType type, String msgCode) {
        List<IBFElement> startElements = new ArrayList<IBFElement>();
        for (Iterator<IBFElement> it = simpleElements.iterator(); it.hasNext();) {
            IBFElement element = it.next();
            if (element.getType().equals(type)) {
                startElements.add(element);
            }
        }
        if (startElements.size() > 1) {
            String text = NLS.bind(Messages.getString("BusinessFunction.elementOnlyOnce"), type.getName()); //$NON-NLS-1$ //$NON-NLS-2$
            for (IBFElement element : startElements) {
                msgList.add(new Message(msgCode, text, Message.ERROR, element));
            }
        }
    }

    private void validateBFElementsConnected(MessageList list) {
        boolean startOrEndMissing = false;
        if (getStart() == null) {
            list.add(new Message(MSGCODE_START_DEFINITION_MISSING,
                    Messages.getString("BusinessFunction.startMissing"), Message.ERROR, this)); //$NON-NLS-1$
            startOrEndMissing = true;
        }
        if (getEnd() == null) {
            list.add(new Message(MSGCODE_END_DEFINITION_MISSING, Messages.getString("BusinessFunction.endMissing"), //$NON-NLS-1$
                    Message.ERROR, this));
            startOrEndMissing = true;
        }
        if (startOrEndMissing) {
            return;
        }

        List<IBFElement> elements = getBFElementsWithoutParameters();
        ArrayList<IBFElement> successfullyCheckedForStart = new ArrayList<IBFElement>(elements.size());
        for (IBFElement element : elements) {
            traceToStart(element, successfullyCheckedForStart, new ArrayList<IBFElement>(elements.size()));
        }

        ArrayList<IBFElement> successfullyCheckedForEnd = new ArrayList<IBFElement>(elements.size());
        for (IBFElement element : elements) {
            traceToEnd(element, successfullyCheckedForEnd, new ArrayList<IBFElement>(elements.size()));
        }

        for (IBFElement element : elements) {
            if (element.getType().equals(BFElementType.START)) {
                continue;
            }
            if (!successfullyCheckedForStart.contains(element)) {
                String text = NLS.bind(Messages.getString("BusinessFunction.elementNotConnectedWithStart"), //$NON-NLS-1$
                        element.getDisplayString());
                list.add(new Message(MSGCODE_NOT_CONNECTED_WITH_START, text, Message.ERROR, element));
            }
        }

        for (IBFElement element : elements) {
            if (element.getType().equals(BFElementType.END)) {
                continue;
            }
            if (!successfullyCheckedForEnd.contains(element)) {
                String text = NLS.bind(Messages.getString("BusinessFunction.elementNotConnectedWithEnd"), //$NON-NLS-1$
                        element.getDisplayString());
                list.add(new Message(MSGCODE_NOT_CONNECTED_WITH_END, text, Message.ERROR, element));
            }
        }
    }

    private void traceToStart(IBFElement current, List<IBFElement> successfullyChecked, List<IBFElement> currentTrace) {
        if (current == null) {
            return;
        }
        if (BFElementType.START.equals(current.getType())) {
            for (IBFElement element : currentTrace) {
                if (!successfullyChecked.contains(element)) {
                    successfullyChecked.add(element);
                }
            }
            return;
        }
        if (currentTrace.contains(current)) {
            return;
        }
        List<IControlFlow> in = current.getIncomingControlFlow();

        if (in.size() == 1) {
            IBFElement source = in.get(0).getSource();
            if (addIfPredecessorValid(source, current, successfullyChecked, currentTrace)) {
                return;
            }
            traceToStart(source, successfullyChecked, currentTrace);
        }
        for (IControlFlow controlFlow : in) {
            IBFElement source = controlFlow.getSource();
            if (addIfPredecessorValid(source, current, successfullyChecked, currentTrace)) {
                continue;
            }
            ArrayList<IBFElement> newTrace = new ArrayList<IBFElement>(currentTrace.size() + 10);
            newTrace.addAll(currentTrace);
            traceToStart(source, successfullyChecked, newTrace);
        }
    }

    private boolean addIfPredecessorValid(IBFElement source,
            IBFElement current,
            List<IBFElement> successfullyChecked,
            List<IBFElement> currentTrace) {
        if (successfullyChecked.contains(source)) {
            if (current != null && !successfullyChecked.contains(current)) {
                successfullyChecked.add(current);
            }
            return true;
        }
        currentTrace.add(current);
        return false;
    }

    private void traceToEnd(IBFElement current, List<IBFElement> successfullyChecked, List<IBFElement> currentTrace) {
        if (current == null) {
            return;
        }
        if (BFElementType.END.equals(current.getType())) {
            for (IBFElement element : currentTrace) {
                if (!successfullyChecked.contains(element)) {
                    successfullyChecked.add(element);
                }
            }
            return;
        }
        if (currentTrace.contains(current)) {
            return;
        }
        List<IControlFlow> in = current.getOutgoingControlFlow();
        if (in.size() == 1) {
            IBFElement target = in.get(0).getTarget();
            if (addIfPredecessorValid(target, current, successfullyChecked, currentTrace)) {
                return;
            }
            traceToEnd(target, successfullyChecked, currentTrace);
        }
        for (IControlFlow controlFlow : in) {
            IBFElement target = controlFlow.getTarget();
            if (addIfPredecessorValid(target, current, successfullyChecked, currentTrace)) {
                continue;
            }
            ArrayList<IBFElement> newTrace = new ArrayList<IBFElement>(currentTrace.size() + 10);
            newTrace.addAll(currentTrace);
            traceToEnd(target, successfullyChecked, newTrace);
        }
    }

    // TODO testing
    @Override
    public IDependency[] dependsOn() throws CoreException {
        List<IDependency> dependencies = new ArrayList<IDependency>();
        for (IIpsObjectPart part : actions.getParts()) {
            IActionBFE action = (IActionBFE)part;
            if (action.getType() == BFElementType.ACTION_BUSINESSFUNCTIONCALL) {
                dependencies.add(IpsObjectDependency.createReferenceDependency(getQualifiedNameType(), new QualifiedNameType(
                        action.getTarget(), BusinessFunctionIpsObjectType.getInstance())));
                continue;
            }
            if (action.getType() == BFElementType.ACTION_METHODCALL) {
                IParameterBFE param = action.getParameter();
                if(param != null){
                    String datatype = param.getDatatype();
                    if(datatype != null){
                        dependencies.add(IpsObjectDependency.createReferenceDependency(getQualifiedNameType(), new QualifiedNameType(
                                datatype, IpsObjectType.POLICY_CMPT_TYPE)));
                    }
                }
            }
        }
        return dependencies.toArray(new IDependency[dependencies.size()]);
    }

    private static class BFElementIpsObjectPartCollection extends IpsObjectPartCollection {

        @SuppressWarnings("unchecked")
        public BFElementIpsObjectPartCollection(BaseIpsObject ipsObject, Class partsClazz, Class publishedInterface,
                String xmlTag) {
            super(ipsObject, partsClazz, publishedInterface, xmlTag);
        }

        public IBFElement newBFElement(final Point location, final BFElementType type) {
            IpsObjectPartInitializer initializer = new IpsObjectPartInitializer() {

                public void initialize(IpsObjectPart part) {
                    BFElement element = (BFElement)part;
                    element.location = location;
                    element.type = type;
                }
            };
            return (BFElement)newPart(initializer);
        }
    }
}
