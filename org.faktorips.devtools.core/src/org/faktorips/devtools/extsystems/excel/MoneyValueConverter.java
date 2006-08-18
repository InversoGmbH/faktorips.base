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

package org.faktorips.devtools.extsystems.excel;

import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.extsystems.IValueConverter;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.Money;

/**
 * Converter for Money
 * 
 * @author Thorsten Guenther
 */
public class MoneyValueConverter implements IValueConverter {

	/**
	 * Supported type for the externalDataValue is String.
	 * 
	 * {@inheritDoc}
	 */
	public String getIpsValue(Object externalDataValue, MessageList messageList) {
		if (externalDataValue instanceof String) {
			try {
                return Money.valueOf((String) externalDataValue).toString();
            } catch (RuntimeException e) {
                Object[] objects = new Object[3];
                objects[0] = externalDataValue;
                objects[1] = externalDataValue.getClass().getName();
                objects[2] = getSupportedDatatype().getQualifiedName();
                String msg = NLS.bind("Can not convert the external value \"{0}\" of type {1} to {2}", objects);
                messageList.add(new Message("", msg, Message.ERROR));
                return externalDataValue.toString();
            }
		} 
        
		String msg = NLS.bind("Can not convert the external value of type {0} to {1}", externalDataValue.getClass(), getSupportedDatatype().getQualifiedName());
		messageList.add(new Message("", msg, Message.ERROR));
		return externalDataValue.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        if (!Datatype.MONEY.isParsable(ipsValue)) {
            String msg = NLS.bind("Can not convert the internal value \"{0}\" of type {1} to external value.", ipsValue, getSupportedDatatype().getQualifiedName());
            messageList.add(new Message("", msg, Message.ERROR));
        }
		return ipsValue;
	}

	/**
	 * {@inheritDoc}
	 */
	public Datatype getSupportedDatatype() {
		return Datatype.MONEY;
	}

}
