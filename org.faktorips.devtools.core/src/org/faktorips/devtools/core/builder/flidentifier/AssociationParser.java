/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder.flidentifier;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.flidentifier.ast.AssociationNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.message.Message;

/**
 * This parser tires to match the identifier node to an association of the context type. It also
 * handles optional qualifiers or indices. The output will be an {@link AssociationNode} or a
 * subclass of this class.
 * 
 * 
 * @author dirmeier
 */
public class AssociationParser extends TypeBasedIdentifierParser {

    public AssociationParser(IExpression expression, IIpsProject ipsProject) {
        super(expression, ipsProject);
    }

    @Override
    protected boolean isAllowedType() {
        return super.isAllowedType() && super.getContextType() instanceof IPolicyCmptType;
    }

    @Override
    public IPolicyCmptType getContextType() {
        return (IPolicyCmptType)super.getContextType();
    }

    @Override
    protected IdentifierNode parseInternal() {
        IPolicyCmptType policyCmptType = getContextType();
        try {
            IAssociation association = policyCmptType.findAssociation(getAssociationName(), getIpsProject());
            if (association != null) {
                return createNodeFor(association);
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return nodeFactory().createInvalidIdentifier(
                    Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, NLS.bind(
                            Messages.AssociationParser_msgErrorWhileFindAssociation, getIdentifierPart(),
                            getContextType())));
        }
        return null;
    }

    private IdentifierNode createNodeFor(IAssociation association) {
        return nodeFactory().createAssociationNode(association, isListOfTypeContext());
    }

    protected String getAssociationName() {
        return getIdentifierPart();
    }

    @Override
    public List<IdentifierNode> getProposals(String prefix) {
        List<IAssociation> availableAssociations = getAvailableAssociations();
        ArrayList<IdentifierNode> nodes = new ArrayList<IdentifierNode>();
        for (IAssociation association : availableAssociations) {
            if (nameStartsWithIgnoreCase(association, prefix)) {
                nodes.add(createNodeFor(association));
            }
        }
        return nodes;
    }

    private boolean nameStartsWithIgnoreCase(IIpsElement ipsElement, String prefix) {
        return StringUtils.startsWithIgnoreCase(ipsElement.getName(), prefix);
    }

    private List<IAssociation> getAvailableAssociations() {
        try {
            return getContextType().findAllAssociations(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

}
