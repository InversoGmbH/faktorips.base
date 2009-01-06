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

package org.faktorips.devtools.extsystems.excel;

import java.math.BigDecimal;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.extsystems.ExtSystemsMessageUtil;
import org.faktorips.devtools.extsystems.IValueConverter;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.Decimal;

/**
 * Converter for Decimal
 * 
 * @author Thorsten Guenther
 */
public class DecimalValueConverter implements IValueConverter {

	/**
	 * Supported types for the externalDataValue are String and Number.
	 * 
	 * {@inheritDoc}
	 */
	public String getIpsValue(Object externalDataValue, MessageList messageList) {
        if (externalDataValue instanceof String) {
            try {
                return Decimal.valueOf((String)externalDataValue).toString();
            } catch (RuntimeException e) {
                messageList.add(ExtSystemsMessageUtil.createConvertExtToIntErrorMessage("" + externalDataValue, externalDataValue //$NON-NLS-1$
                        .getClass().getName(), getSupportedDatatype().getQualifiedName()));
                return externalDataValue.toString();
            }
        } else if (externalDataValue instanceof Number) {
            return Decimal.valueOf(new BigDecimal(((Number)externalDataValue).toString())).toString();
        }
        messageList.add(ExtSystemsMessageUtil.createConvertExtToIntErrorMessage("" + externalDataValue, externalDataValue.getClass() //$NON-NLS-1$
                .getName(), getSupportedDatatype().getQualifiedName()));
        return externalDataValue.toString();
    }

	/**
	 * {@inheritDoc}
	 */
	public Object getExternalDataValue(String ipsValue, MessageList messageList) {
        try {
            return Decimal.valueOf(ipsValue);
        } catch (RuntimeException e) {
            messageList.add(ExtSystemsMessageUtil.createConvertIntToExtErrorMessage(ipsValue, Decimal.class.getName(),
                    getSupportedDatatype().getQualifiedName()));
        }
        return ipsValue;
	}

	/**
	 * {@inheritDoc}
	 */
	public Datatype getSupportedDatatype() {
		return Datatype.DECIMAL;
	}

}
