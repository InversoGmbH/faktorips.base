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

package org.faktorips.devtools.core.builder;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.faktorips.devtools.core.model.ipsproject.IIpsLoggingFrameworkConnector;
import org.faktorips.util.ArgumentCheck;

/**
 * An implementation of the <code>IIpsLoggingFrameworkConnector</code> interface that connects
 * to the java.util.logging logging framework. 
 * 
 * @author Peter Erzberger
 */
public class JavaUtilLoggingFrameworkConnector implements IIpsLoggingFrameworkConnector {

    private String id = ""; //$NON-NLS-1$
    
    private String getLevelExp(int level){
        
        if(level == IIpsLoggingFrameworkConnector.LEVEL_INFO){
            return "Level.INFO"; //$NON-NLS-1$
        }
        if(level == IIpsLoggingFrameworkConnector.LEVEL_WARNING){
            return "Level.WARNING"; //$NON-NLS-1$
        }
        if(level == IIpsLoggingFrameworkConnector.LEVEL_ERROR){
            return "Level.SEVERE"; //$NON-NLS-1$
        }
        if(level == IIpsLoggingFrameworkConnector.LEVEL_DEBUG){
            return "Level.FINE"; //$NON-NLS-1$
        }
        if(level == IIpsLoggingFrameworkConnector.LEVEL_TRACE){
            return "Level.FINEST"; //$NON-NLS-1$
        }
        throw new IllegalArgumentException("The specified logging level is not defined: " + level); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    public String getLogConditionExp(int level, String loggerExpression, List usedClasses) {
        usedClasses.add(Level.class.getName());
        return loggerExpression + ".isLoggable(" + getLevelExp(level) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    private String getLevelMethodName(int level){
        if(level == IIpsLoggingFrameworkConnector.LEVEL_INFO){
            return "info"; //$NON-NLS-1$
        }
        if(level == IIpsLoggingFrameworkConnector.LEVEL_WARNING){
            return "warning"; //$NON-NLS-1$
        }
        if(level == IIpsLoggingFrameworkConnector.LEVEL_ERROR){
            return "severe"; //$NON-NLS-1$
        }
        if(level == IIpsLoggingFrameworkConnector.LEVEL_DEBUG){
            return "fine"; //$NON-NLS-1$
        }
        if(level == IIpsLoggingFrameworkConnector.LEVEL_TRACE){
            return "finest"; //$NON-NLS-1$
        }
        throw new IllegalArgumentException("The specified logging level is not defined: " + level); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public String getLogStmtForMessage(int level, String msgConstant, String loggerInstanceExp, List usedClasses) {
        StringBuffer buf = new StringBuffer();
        buf.append(loggerInstanceExp);
        buf.append("."); //$NON-NLS-1$
        buf.append(getLevelMethodName(level));
        buf.append("(\""); //$NON-NLS-1$
        buf.append(msgConstant);
        buf.append("\")"); //$NON-NLS-1$
        return buf.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String getLogStmtForMessageExp(int level, String msgExp, String loggerInstanceExp, List usedClasses) {
        StringBuffer buf = new StringBuffer();
        buf.append(loggerInstanceExp);
        buf.append("."); //$NON-NLS-1$
        buf.append(getLevelMethodName(level));
        buf.append("("); //$NON-NLS-1$
        buf.append(msgExp);
        buf.append(")"); //$NON-NLS-1$
        return buf.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String getLogStmtForThrowable(int level, String msgExp,
            String throwableExp,
            String loggerInstanceExp,
            List usedClasses) {
        usedClasses.add(Level.class.getName());
        StringBuffer buf = new StringBuffer();
        buf.append(loggerInstanceExp);
        buf.append(".log("); //$NON-NLS-1$
        buf.append(getLevelExp(level));
        buf.append(", "); //$NON-NLS-1$
        buf.append(msgExp);
        buf.append(", "); //$NON-NLS-1$
        buf.append(throwableExp);
        buf.append(")"); //$NON-NLS-1$
        return buf.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String getLoggerClassName() {
        return Logger.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    public String getLoggerInstanceStmt(String scopeExp, List usedClasses) {
        usedClasses.add(Logger.class.getName());
        StringBuffer buf = new StringBuffer();
        buf.append("Logger.getLogger("); //$NON-NLS-1$
        buf.append(scopeExp);
        buf.append(")"); //$NON-NLS-1$
        return buf.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public void setId(String id) {
        ArgumentCheck.notNull(id);
        this.id = id;
    }
}
