/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.views.producttemplate;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Collections2.filter;

import java.beans.PropertyChangeEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.editors.productcmpt.PropertyValueFormatter;
import org.faktorips.devtools.core.util.Histogram;
import org.faktorips.devtools.core.util.Tree;
import org.faktorips.devtools.core.util.Tree.Node;

public class TemplatePropertyUsagePmo extends IpsObjectPartPmo {

    public static final String PROPERTY_IDENTICAL_VALUES_LABEL_TEXT = "identicalValuesLabelText"; //$NON-NLS-1$
    public static final String PROPERTY_DIFFERING_VALUES_LABEL_TEXT = "differingValuesLabelText"; //$NON-NLS-1$

    // lazily loaded
    private List<IPropertyValue> propertyValuesBasedOnTemplate;

    // lazily loaded
    private Histogram<Object, IPropertyValue> histogram;

    private SortedMap<Object, Integer> definedAbsoluteDistribution;

    public TemplatePropertyUsagePmo() {
        super();
    }

    public TemplatePropertyUsagePmo(IPropertyValue propertyValue) {
        super();
        setPropertyValue(propertyValue);
    }

    public void setPropertyValue(IPropertyValue propertyValue) {
        setIpsObjectPartContainer(propertyValue);
    }

    private boolean hasData() {
        return getTemplatePropertyValue() != null;
    }

    public String getIdenticalValuesLabelText() {
        if (hasData()) {
            return getIdenticalLabelWithData();
        } else {
            return Messages.TemplatePropertyLabelPmo_SameValue_fallbackLabel;
        }
    }

    private String getIdenticalLabelWithData() {
        String propertyName = getTemplatePropertyValue().getPropertyName();
        String formattedValue = PropertyValueFormatter.format(getTemplatePropertyValue());
        int inheritedCount = getInheritingPropertyValues().size();
        BigDecimal inheritedPercent = getInheritPercent(inheritedCount);
        return NLS.bind(Messages.TemplatePropertyUsageView_SameValue_label, new Object[] { propertyName,
                formattedValue, inheritedCount, inheritedPercent.stripTrailingZeros().toPlainString() });
    }

    private BigDecimal getInheritPercent(int inheritedCount) {
        int count = getCount();
        if (count == 0) {
            return BigDecimal.ZERO;
        } else {
            BigDecimal inheritedPercent = new BigDecimal(inheritedCount).multiply(new BigDecimal(100)).divide(
                    new BigDecimal(count), 1, RoundingMode.HALF_UP);
            return inheritedPercent;
        }
    }

    public String getDifferingValuesLabelText() {
        if (hasData()) {
            return getDifferingLabelWithData();
        } else {
            return Messages.TemplatePropertyLabelPmo_DifferingValue_fallbackLabel;
        }
    }

    private String getDifferingLabelWithData() {
        String propertyName = getTemplatePropertyValue().getPropertyName();
        return NLS.bind(Messages.TemplatePropertyUsageView_DifferingValues_Label, propertyName);
    }

    /** Returns the property which is defined in the template. */
    private IPropertyValue getTemplatePropertyValue() {
        IPropertyValue propertyValue = (IPropertyValue)getIpsObjectPartContainer();
        return propertyValue;
    }

    /** Returns the template whose property usage is displayed. */
    public IProductCmpt getTemplate() {
        return getTemplatePropertyValue().getPropertyValueContainer().getProductCmpt();
    }

    /** Returns the product components that inherit the value from the template. */
    public Collection<IPropertyValue> getInheritingPropertyValues() {
        if (hasData()) {
            return filter(getPropertyValuesBasedOnTemplate(), propertyValueStatus(TemplateValueStatus.INHERITED));
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Returns a histogram for the custom (i.e. non-inherited) values that are defined in product
     * components. The histogram's values are the custom values, the histogram's elements are the
     * product components defining these values.
     * <p>
     * Note that the values in the histogram are not {@code IPropertyValue} instances but their
     * actual values, e.g. it contains strings with table names and not {@code ITableContentUsage}
     * objects.
     */
    public Histogram<Object, IPropertyValue> getDefinedValuesHistogram() {
        if (hasData()) {
            return getHistogramInternal();
        } else {
            return Histogram.emptyHistogram();
        }
    }

    private Histogram<Object, IPropertyValue> getHistogramInternal() {
        if (histogram == null) {
            histogram = new Histogram<Object, IPropertyValue>(valueFunction(), valueComparator(),
                    getDefiningPropertyValues());
            definedAbsoluteDistribution = histogram.getAbsoluteDistribution();
        }
        return histogram;
    }

    public SortedMap<Object, Integer> getDefinedAbsoluteDistribution() {
        if (hasData()) {
            return getDefinedAbsoluteDistributionInternal();
        } else {
            return new TreeMap<Object, Integer>();
        }
    }

    private SortedMap<Object, Integer> getDefinedAbsoluteDistributionInternal() {
        if (histogram == null) {
            getDefinedValuesHistogram();
        }
        return definedAbsoluteDistribution;
    }

    public int getCount() {
        return getInheritingPropertyValues().size() + getDefinedValuesHistogram().countElements();
    }

    /** Returns the value for the property value in the template. */
    protected Object getTemplateValue() {
        IPropertyValue templatePropertyValue = findPropertyValue(getTemplate());
        return valueFunction().apply(templatePropertyValue);
    }

    /** Returns all product components that define a custom value. */
    /* private */protected Collection<IPropertyValue> getDefiningPropertyValues() {
        return filter(getPropertyValuesBasedOnTemplate(), propertyValueStatus(TemplateValueStatus.DEFINED));
    }

    /** Returns the product components that reference this PMO's template. */
    private List<IPropertyValue> getPropertyValuesBasedOnTemplate() {
        if (propertyValuesBasedOnTemplate == null) {
            propertyValuesBasedOnTemplate = findPropertyValuesBasedOnTemplate();
        }
        return propertyValuesBasedOnTemplate;
    }

    /**
     * Find property values that are based on the given template. This list includes
     * <ul>
     * <li>property values that use the given template</li>
     * <li>property values that use the given template and define a value (i.e. do not inherit the
     * value from the given template)</li>
     * <li>property values that use templates inheriting their values from the given template</li>
     * </ul>
     */
    private List<IPropertyValue> findPropertyValuesBasedOnTemplate() {
        Tree<IIpsSrcFile> templateSrcFileHierarchy = getIpsProject().findTemplateHierarchy(getTemplate());
        if (templateSrcFileHierarchy.isEmpty()) {
            return Collections.emptyList();
        }
        Tree<IProductCmpt> templateHierarchy = templateSrcFileHierarchy.transform(srcFileToProductCmpt());
        return findPropertyValuesBasedOnTemplate(templateHierarchy.getRoot());
    }

    private List<IPropertyValue> findPropertyValuesBasedOnTemplate(Node<IProductCmpt> node) {
        List<IPropertyValue> result = Lists.newArrayList();
        result.addAll(filter(Lists.transform(getProductNodes(node), nodeToPropertyValue()), notNull()));

        List<Node<IProductCmpt>> templateNodes = getTemplateNodes(node);
        for (Node<IProductCmpt> templateNode : templateNodes) {
            IPropertyValue templateValue = findPropertyValue(templateNode.getElement());
            if (definesValue(templateValue)) {
                // Include template as it defines a custom value. Product components using the
                // template do not have to be included as their value depends on template and not
                // the initial template.
                result.add(templateValue);
            } else {
                // If the template does not define a value all product components using the template
                // should be included as their values actually depend on this PMO's template.
                result.addAll(findPropertyValuesBasedOnTemplate(templateNode));
            }
        }
        return result;
    }

    /** Returns the children of the given node that hold product components (i.e. not templates). */
    private List<Node<IProductCmpt>> getProductNodes(Node<IProductCmpt> node) {
        return FluentIterable.from(node.getChildren()).filter(Predicates.not(isTemplate())).toImmutableList();
    }

    /** Returns the children of the given node that hold product templates. */
    private List<Node<IProductCmpt>> getTemplateNodes(Node<IProductCmpt> node) {
        return FluentIterable.from(node.getChildren()).filter(isTemplate()).toImmutableList();
    }

    /** Returns this PMO's property. */
    private String getPropertyName() {
        return getTemplatePropertyValue().getPropertyName();
    }

    /**
     * Returns whether or not the given property value defines a custom values (i.e. does not
     * inherit the value from its template).
     */
    private boolean definesValue(IPropertyValue value) {
        return value != null && value.getTemplateValueStatus() == TemplateValueStatus.DEFINED;
    }

    /** Function to transform an IIpsSrcFile to the IProductCmpt enclosed in it. */
    private Function<IIpsSrcFile, IProductCmpt> srcFileToProductCmpt() {
        return new Function<IIpsSrcFile, IProductCmpt>() {
            @Override
            public IProductCmpt apply(IIpsSrcFile srcFile) {
                // FindBugs does not like Preconditions.checkState...
                if (srcFile == null) {
                    throw new IllegalStateException();
                }
                return (IProductCmpt)srcFile.getIpsObject();
            }

        };
    }

    /** Function to transform a node to the IPropertyValue enclosed in it. */
    private Function<Node<IProductCmpt>, IPropertyValue> nodeToPropertyValue() {
        return new Function<Node<IProductCmpt>, IPropertyValue>() {

            @Override
            public IPropertyValue apply(Node<IProductCmpt> node) {
                // FindBugs does not like Preconditions.checkState...
                if (node == null) {
                    throw new IllegalStateException();
                }
                return findPropertyValue(node.getElement());
            }
        };
    }

    /**
     * Returns the value for this PMO's property from the given product component (or its
     * generation).
     */
    private IPropertyValue findPropertyValue(IProductCmpt productCmpt) {
        IPropertyValue propertyValue = productCmpt.getPropertyValue(getPropertyName());
        if (propertyValue != null) {
            return propertyValue;
        }

        // TODO FIPS-4433
        // IProductCmptGeneration gen = productCmpt.getGenerationEffectiveOn(effectiveDate);
        IProductCmptGeneration gen = productCmpt.getLatestProductCmptGeneration();
        if (gen != null) {
            return gen.getPropertyValue(getPropertyName());
        } else {
            return null;
        }
    }

    /**
     * Predicate that matches an IProductCmpt whose property value (for this PMO's property) has the
     * given TemplateValueStatus.
     */
    private Predicate<IPropertyValue> propertyValueStatus(final TemplateValueStatus t) {
        return new Predicate<IPropertyValue>() {

            @Override
            public boolean apply(IPropertyValue propertyValue) {
                return propertyValue != null && propertyValue.getTemplateValueStatus() == t;
            }

        };
    }

    /** Predicate that matches a node that encloses an IProductCmpt that is a template. */
    private Predicate<Node<IProductCmpt>> isTemplate() {
        return new Predicate<Node<IProductCmpt>>() {
            @Override
            public boolean apply(Node<IProductCmpt> node) {
                return node != null && node.getElement().isProductTemplate();
            }
        };
    }

    /** Returns a comparator to compare the value of property values. */
    private Comparator<Object> valueComparator() {
        return getTemplatePropertyValue().getPropertyValueType().getValueComparator();
    }

    /**
     * Returns a function to obtain the value of a property value
     */
    private Function<IPropertyValue, Object> valueFunction() {
        return getTemplatePropertyValue().getPropertyValueType().getValueGetter();
    }

    /**
     * Returns <code>true</code> if the affected IPSSrcFile is a product component or product
     * template.
     * <p>
     * Determining whether a change in some product component really affects one of the displayed
     * property values would be more effort than simply recalculating everything. Thus this PMO
     * reacts to every potential change.
     */
    @Override
    protected boolean isAffected(ContentChangeEvent event) {
        IpsObjectType ipsObjectType = event.getIpsSrcFile().getIpsObjectType();
        return ipsObjectType == IpsObjectType.PRODUCT_CMPT || ipsObjectType == IpsObjectType.PRODUCT_TEMPLATE;
    }

    @Override
    protected void partHasChanged() {
        // reset state to force update
        propertyValuesBasedOnTemplate = null;
        histogram = null;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_IDENTICAL_VALUES_LABEL_TEXT, null, null));
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_DIFFERING_VALUES_LABEL_TEXT, null, null));
    }

}
