package org.faktorips.devtools.core.ui.bf.edit;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.ui.bf.model.commands.ChangeConstraintCommand;
import org.faktorips.devtools.core.ui.bf.model.commands.CreateBFElementCommand;
import org.faktorips.devtools.core.ui.bf.model.commands.ParameterFigureConstraintCommand;

/**
 * An implementation of {@link XYLayoutEditPolicy} that provides the implementations for creation of 
 * commands and policies.
 * 
 * @author Peter Erzberger
 */
public class BusinessFunctionXYLayoutEditPolicy extends XYLayoutEditPolicy {

    @Override
    protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
        if(child instanceof ParameterEditPart){
            return new ParameterFigureConstraintCommand((IBusinessFunction)child.getModel(), (Rectangle)constraint);
        }
        return new ChangeConstraintCommand((IBFElement)child.getModel(), (Rectangle)constraint);
    }

    @Override
    protected Command getCreateCommand(CreateRequest request) {
        BFElementType bfElementType = (BFElementType)request.getNewObject();
        Rectangle constraint = (Rectangle)getConstraintFor(request);
        CreateBFElementCommand create = new CreateBFElementCommand(bfElementType, (IBusinessFunction)getHost()
                .getModel(), constraint.getLocation());
        create.setLabel(Messages.getString("BusinessFunctionXYLayoutEditPolicy.createBFE")); //$NON-NLS-1$
        return create;
    }

    protected EditPolicy createChildEditPolicy(EditPart child) {
        ResizableEditPolicy policy = (ResizableEditPolicy)super.createChildEditPolicy(child);
        if (child instanceof EndEditPart || child instanceof StartEditPart) {
            policy.setResizeDirections(PositionConstants.NONE);
        }
        if(child instanceof ParameterEditPart){
            policy.setDragAllowed(false);
        }
        return policy;
    }

}
