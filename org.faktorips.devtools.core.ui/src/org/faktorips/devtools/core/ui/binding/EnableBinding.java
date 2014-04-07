/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.binding;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Binding between the enable property of a SWT control and a boolean property of an abitrary
 * object, usually a domain model object or presentation model object.
 * 
 * @author Jan Ortmann
 */
public class EnableBinding extends ControlPropertyBinding {

    private Object expectedValue;

    public EnableBinding(Control control, Object object, String property, Object expectedValue) {
        super(control, object, property, null);
        this.expectedValue = expectedValue;
    }

    /**
     * This method updates the enabled state of the control referenced by {@link #getControl()}. The
     * disabled state of a control should be overcontrol an the data changeable state that may be
     * already set by {@link UIToolkit#setDataChangeable(Control, boolean)}. But the data changeable
     * state should not be overcontrolled by an enabled state. This behaviour is important because
     * some controls have different behaviour in read-onlny and disabled state. For example a
     * {@link Text} control is set by {@link Text#setEditable(boolean)} to read-only state but
     * {@link Text#setEnabled(boolean)} in disabled state. That leads to following logik:
     * <ul>
     * <li>Control is read-only, enabled-binding set enabled: control stays read-only</li>
     * <li>Control is read-write, enabled-binding set enabled: control stays enabled</li>
     * <li>Control is read-only, enabled-binding set disabled: control set disabled</li>
     * <li>Control is read-write, enabled-binding set disabled: control set disabled</li>
     * </ul>
     */
    @Override
    public void updateUiIfNotDisposed(String nameOfChangedProperty) {
        try {
            Object value = getProperty().getReadMethod().invoke(getObject(), new Object[0]);
            boolean enabled = value != null && value.equals(expectedValue);
            if (isDataChangeable() || !enabled) {
                getControl().setEnabled(enabled);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isDataChangeable() {
        return UIToolkit.isDataChangeable(getControl());
    }
}
