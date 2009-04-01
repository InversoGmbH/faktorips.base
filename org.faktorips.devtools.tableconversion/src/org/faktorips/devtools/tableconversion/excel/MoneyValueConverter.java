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

package org.faktorips.devtools.tableconversion.excel;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.tableconversion.ExtSystemsMessageUtil;
import org.faktorips.devtools.tableconversion.AbstractValueConverter;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.Money;

/**
 * Converter for Money
 * 
 * @author Thorsten Guenther
 */
public class MoneyValueConverter extends AbstractValueConverter {

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
                messageList.add(ExtSystemsMessageUtil.createConvertExtToIntErrorMessage(
                        "" + externalDataValue, externalDataValue.getClass().getName(), getSupportedDatatype().getQualifiedName())); //$NON-NLS-1$
                return externalDataValue.toString();
            }
		} 
		messageList.add(ExtSystemsMessageUtil.createConvertExtToIntErrorMessage(
                "" + externalDataValue, externalDataValue.getClass().getName(), getSupportedDatatype().getQualifiedName())); //$NON-NLS-1$
		return externalDataValue.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        if (!Datatype.MONEY.isParsable(ipsValue)) {
            messageList.add(ExtSystemsMessageUtil.createConvertIntToExtErrorMessage(ipsValue, getSupportedDatatype().getQualifiedName(), Money.class.getName())); //$NON-NLS-1$
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
