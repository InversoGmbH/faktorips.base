/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.flidentifier;

import java.util.List;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.AssociationNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAssociation;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;

/**
 * JavaBuilder for an {@link AssociationNode}. Example in formula language: "policy.coverage" (get
 * all coverages from policy).
 * 
 * @author widmaier
 * @since 3.11.0
 */
public class AssociationNodeGenerator extends StdBuilderIdentifierNodeGenerator {

    public AssociationNodeGenerator(IdentifierNodeGeneratorFactory<JavaCodeFragment> nodeBuilderFactory,
            StandardBuilderSet builderSet) {
        super(nodeBuilderFactory, builderSet);
    }

    @Override
    protected CompilationResult<JavaCodeFragment> getCompilationResultForCurrentNode(IdentifierNode identifierNode,
            CompilationResult<JavaCodeFragment> contextCompilationResult) {
        AssociationNode node = (AssociationNode)identifierNode;
        return getCompilationResultForAssociation(contextCompilationResult, node);
    }

    protected CompilationResult<JavaCodeFragment> getCompilationResultForAssociation(CompilationResult<JavaCodeFragment> contextCompilationResult,
            AssociationNode node) {
        if (isListDatatypeContext(contextCompilationResult)) {
            return compileListContext(contextCompilationResult, node);
        } else {
            JavaCodeFragment javaCodeFragment = compileSingleObjectContext(contextCompilationResult.getCodeFragment(),
                    node);
            return createCompilationResult(javaCodeFragment, node);
        }
    }

    /* private */protected CompilationResult<JavaCodeFragment> compileListContext(CompilationResult<JavaCodeFragment> contextCompilationResult,
            AssociationNode node) {
        JavaCodeFragment codeFragment = compileAssociationChain(contextCompilationResult, node);
        return createCompilationResult(codeFragment, node);
    }

    private JavaCodeFragment compileAssociationChain(CompilationResult<JavaCodeFragment> contextCompilationResult,
            AssociationNode node) {

        IType sourceType = getContextBasicDatatype(contextCompilationResult);
        IType targetType = (IType)getBasicDatatype(node);
        IAssociation association = node.getAssociation();
        /*
         * Example Code:
         * 
         * new AssociationToManyHelper<IPolicy, ICoverage>() {
         * 
         * @Override protected List<ICoverage> getTargetsInternal(IPolicy sourceObject) { return
         * sourceObject.getCoverages(); } }.getTargets(javaCodeFragment)
         */
        boolean is1ToManyIgnoringQualifier = association.is1ToManyIgnoringQualifier();
        JavaCodeFragment getTargetCode = new JavaCodeFragment("new "); //$NON-NLS-1$
        if (is1ToManyIgnoringQualifier) {
            getTargetCode
                    .appendClassName(org.faktorips.runtime.formula.FormulaEvaluatorUtil.AssociationToManyHelper.class);
        } else {
            getTargetCode
                    .appendClassName(org.faktorips.runtime.formula.FormulaEvaluatorUtil.AssociationTo1Helper.class);
        }
        getTargetCode.append("<"); //$NON-NLS-1$
        String sourceClassName = getJavaClassName(sourceType);
        getTargetCode.appendClassName(sourceClassName);
        getTargetCode.append(", "); //$NON-NLS-1$
        String targetClassName = getJavaClassName(targetType);
        getTargetCode.appendClassName(targetClassName);
        getTargetCode.append(">(){@Override protected "); //$NON-NLS-1$
        if (is1ToManyIgnoringQualifier) {
            getTargetCode.appendClassName(List.class);
            getTargetCode.append("<"); //$NON-NLS-1$
        }
        getTargetCode.appendClassName(targetClassName);
        if (is1ToManyIgnoringQualifier) {
            getTargetCode.append("> getTargetsInternal("); //$NON-NLS-1$
        } else {
            getTargetCode.append(" getTargetInternal("); //$NON-NLS-1$
        }
        getTargetCode.appendClassName(sourceClassName).append(" sourceObject){return ")
                .append(compileSingleObjectContext(new JavaCodeFragment("sourceObject"), node))
                .append(";}}.getTargets(") //$NON-NLS-1$
                .append(contextCompilationResult.getCodeFragment()).append(")"); //$NON-NLS-1$

        return getTargetCode;
    }

    /* private */protected JavaCodeFragment compileSingleObjectContext(JavaCodeFragment contextCode,
            AssociationNode node) {
        JavaCodeFragment javaCodeFragment = new JavaCodeFragment(contextCode);
        javaCodeFragment.append('.');
        javaCodeFragment.append(getAssociationTargetGetterName(node.getAssociation()));
        javaCodeFragment.append("()"); //$NON-NLS-1$
        return javaCodeFragment;
    }

    private CompilationResultImpl createCompilationResult(JavaCodeFragment javaCodeFragment, AssociationNode node) {
        return new CompilationResultImpl(javaCodeFragment, node.getDatatype());
    }

    /**
     * Returns the name of the getter method generated for the given association's target(s).
     * Returns different names depending on whether the association is a 1to1 or 1toMany
     * association.
     */
    protected String getAssociationTargetGetterName(IAssociation association) {
        XPolicyAssociation xPolicyAssociation = getModelNode(association, XPolicyAssociation.class);
        return xPolicyAssociation.getMethodNameGetter();
    }
}
