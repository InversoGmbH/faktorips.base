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

package org.faktorips.devtools.core.ui.controller;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;

/**
 * 
 */
public class DefaultUIController implements ValueChangeListener, UIController, FocusListener {

	// list of mappings between edit fields and properties of model objects.
	protected List mappings = new ArrayList();

	/**
	 * 
	 */
	public DefaultUIController() {
		super();
	}

	/**
	 * Adds an edit-field to this controller. The property with the given name
	 * has to be get- and setable at the given object.
	 * 
	 * @param field The field to link.
	 * @param object The object to get and set the property
	 * @param propertyName The name of the property
	 */
	public void add(EditField field, Object object, String propertyName) {
		PropertyDescriptor property = null;
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(object.getClass());
			PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
			for (int i = 0; i < properties.length; i++) {
				if (properties[i].getName().equals(propertyName)) {
					property = properties[i];
					break;
				}
			}
		} catch (IntrospectionException e) {
			throw new RuntimeException("Exception while introspection class " //$NON-NLS-1$
					+ object.getClass(), e);
		}
		if (property == null) {
			throw new IllegalArgumentException("Class " + object.getClass() //$NON-NLS-1$
					+ " does not have a property " + propertyName); //$NON-NLS-1$
		}
		
		FieldPropertyMappingByPropertyDescriptor mapping = new FieldPropertyMappingByPropertyDescriptor(field, object,
				property); 
		addMapping(mapping);
		
	}

	/**
	 * Creates a TextField to wrap the given Text-Object. If this Text-Object 
	 * displays a property which is not of type String the method 
	 * <code>add(EditField field, Object object, String propertyName)</code>
	 * with the appropriate EditField has to be used. 
	 * 
	 * @param text The text to link with.
	 * @param object The Object to link with.
	 * @param propertyName The name of the property to link with.
	 */
	public TextField add(Text text, Object object, String propertyName) {
		TextField field = new TextField(text);
		add(field, object, propertyName);
		return field;
	}

	protected void addMapping(FieldPropertyMapping mapping) {
		mappings.add(mapping);
		mapping.getField().addChangeListener(this);
        mapping.getField().getControl().addFocusListener(this);
	}

	public void updateModel() {
        List copy = new ArrayList(mappings); // defensive copy to avoid concurrent modification exceptions
		for (Iterator it = copy.iterator(); it.hasNext();) {
			FieldPropertyMapping mapping = (FieldPropertyMapping) it.next();
			try {
				mapping.setPropertyValue();
			} catch (Exception e) {
				IpsPlugin.log(new IpsStatus("Error updating model property " + mapping.getPropertyName() //$NON-NLS-1$
						+ " of object " + mapping.getObject(), e)); //$NON-NLS-1$
			}
		}
	}

	public void updateUI() {
        List copy = new ArrayList(mappings); // defensive copy to avoid concurrent modification exceptions
		for (Iterator it = copy.iterator(); it.hasNext();) {
			FieldPropertyMapping mapping = (FieldPropertyMapping) it.next();
			try {
				mapping.setControlValue();
			} catch (Exception e) {
				IpsPlugin.log(new IpsStatus("Error updating control for property " + mapping.getPropertyName() //$NON-NLS-1$
						+ " of object " + mapping.getObject(), e)); //$NON-NLS-1$
			}
		}
	}

	/**
     * {@inheritDoc}
	 */
	public void valueChanged(FieldValueChangedEvent e) {
        List copy = new ArrayList(mappings); // defensive copy to avoid concurrent modification exceptions
		for (Iterator it = copy.iterator(); it.hasNext();) {
			FieldPropertyMapping mapping = (FieldPropertyMapping) it.next();
			if (e.field == mapping.getField()) {
				try {
					mapping.setPropertyValue();
				} catch (Exception ex) {
					IpsPlugin.log(new IpsStatus("Error updating model property " + mapping.getPropertyName() //$NON-NLS-1$
							+ " of object " + mapping.getObject(), ex)); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * Removes the given field. After this method has returned, the field is no
	 * longer controlled by this controller, all listeners set by this
	 * controller are removed.
	 * 
	 * @param field
	 *            The field to remove.
	 */
	public void remove(EditField field) {
		ArrayList secureCopy = new ArrayList(mappings);

		for (Iterator it = secureCopy.iterator(); it.hasNext();) {
			FieldPropertyMapping mapping = (FieldPropertyMapping) it.next();
			if (mapping.getField().equals(field)) {
				mappings.remove(mapping);
				field.removeChangeListener(this);
			}
		}
	}

    /**
     * {@inheritDoc}
     */
    public void focusGained(FocusEvent e) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    public void focusLost(FocusEvent e) {
        // broadcast outstanding change events
        IpsPlugin.getDefault().getEditFieldChangeBroadcaster().broadcastLastEvent();
    }
}
