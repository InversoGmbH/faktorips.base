/*******************************************************************************
�* Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
�*
�* Alle Rechte vorbehalten.
�*
�* Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
�* Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
�* Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
�* genutzt werden, die Bestandteil der Auslieferung ist und auch unter
�* � http://www.faktorips.org/legal/cl-v01.html
�* eingesehen werden kann.
�*
�* Mitwirkende:
�* � Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
�*
�*******************************************************************************/

package org.faktorips.util;

import org.faktorips.util.ArgumentCheck;

import junit.framework.TestCase;

/**
 *
 */
public class ArgumentCheckTest extends TestCase {

    public void testIsSubclassOf() {
        ArgumentCheck.isSubclassOf(this.getClass(), TestCase.class);
        try {
            ArgumentCheck.isSubclassOf(String.class, TestCase.class);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIsInstanceOf() {
        ArgumentCheck.isInstanceOf(this, TestCase.class);
        try {
            ArgumentCheck.isInstanceOf(this, String.class);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
    
    public void testIsNullArray(){
    	String[] ids = new String[3];
    	try{
    		ArgumentCheck.notNull(ids);
    		fail();
    	}
    	catch(RuntimeException e){
    		//an exception is excepted to be thrown
    	}
    	
    	ids[0] = "";
    	ids[1] = "";
    	ids[2] = "";
    	
    	//expected to pass
    	ArgumentCheck.notNull(ids);
    }
    
    public void testIsNullArrayContext(){
    	String[] ids = new String[3];
    	try{
    		ArgumentCheck.notNull(ids, this);
    		fail();
    	}
    	catch(RuntimeException e){
    		//an exception is excepted to be thrown
    	}
    	
    	ids[0] = "";
    	ids[1] = "";
    	ids[2] = "";
    	
    	//expected to pass
    	ArgumentCheck.notNull(ids, this);
    }

}
