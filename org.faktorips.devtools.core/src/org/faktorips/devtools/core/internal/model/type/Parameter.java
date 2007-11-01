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

package org.faktorips.devtools.core.internal.model.type;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.AtomicIpsObjectPart;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.model.type.IParameterContainer;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of the published interface.
 * 
 * @author Jan Ortmann
 */
public class Parameter extends AtomicIpsObjectPart implements IParameter {

    final static String TAG_NAME = "Parameter"; //$NON-NLS-1$

	private String datatype = ""; //$NON-NLS-1$
	
	public Parameter(IParameterContainer container, int id) {
		super(container, id);
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
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		valueChanged(oldName, name);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDatatype(String type) {
		String oldType = datatype;
		datatype = type;
		valueChanged(oldType, datatype);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDatatype() {
		return datatype;
	}

	/**
	 * {@inheritDoc}
	 */
	public Datatype findDatatype(IIpsProject ipsProject) throws CoreException {
		return ipsProject.findDatatype(datatype);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void initPropertiesFromXml(Element element, Integer id) {
		super.initPropertiesFromXml(element, id);
		name = element.getAttribute(PROPERTY_NAME);
		datatype = element.getAttribute(PROPERTY_DATATYPE);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void propertiesToXml(Element element) {
		super.propertiesToXml(element);
		element.setAttribute(PROPERTY_NAME, name);
		element.setAttribute(PROPERTY_DATATYPE, datatype);
	}

	/**
	 * {@inheritDoc}
	 */
	public Image getImage() {
		return IpsPlugin.getDefault().getImage("Parameter.gif"); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	protected void validateThis(MessageList result) throws CoreException {
		super.validateThis(result);
        IIpsProject ipsProject = getIpsProject();
        if (StringUtils.isEmpty(name)) {
            result.add(new Message("", Messages.Parameter_msg_NameEmpty, Message.ERROR, this, PROPERTY_NAME)); //$NON-NLS-1$
        } else {
	        IStatus status = JavaConventions.validateIdentifier(getName());
	        if (!(status.isOK() && ExprCompiler.isValidIdentifier(getName()))) {
	            result.add(new Message("", Messages.Parameter_msg_InvalidParameterName, Message.ERROR, this, PROPERTY_NAME)); //$NON-NLS-1$
	        }
            
        }
        ValidationUtils.checkDatatypeReference(datatype, false, this, PROPERTY_DATATYPE, "", result, ipsProject); //$NON-NLS-1$
	}

	
}
