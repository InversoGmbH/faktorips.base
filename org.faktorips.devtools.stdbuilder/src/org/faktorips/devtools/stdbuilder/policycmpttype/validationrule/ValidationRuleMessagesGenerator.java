/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.policycmpttype.validationrule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.internal.model.InternationalString;
import org.faktorips.devtools.core.model.IInternationalString;
import org.faktorips.devtools.core.model.ILocalizedString;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.stdbuilder.StdBuilderPlugin;
import org.faktorips.devtools.stdbuilder.policycmpttype.MessagesProperties;

public class ValidationRuleMessagesGenerator {

    /**
     * The separator to concatenate the key. We use the minus character because this character is
     * not allowed in names.
     */
    public static final String KEY_SEPARATOR = "-";

    // matching a text that follows '{' and is followed by '}' or ','
    public static final Pattern REPLACEMENT_PARAMETER_REGEXT = Pattern.compile("(?<=(\\{))[\\p{L}0-9]+(?=([,\\}]))");

    private final Map<String, Set<String>> pcTypeNamesToValidationRuleNamesMap = new HashMap<String, Set<String>>();

    private final MessagesProperties validationMessages = new MessagesProperties();

    private final IFile messagesPropertiesFile;

    private final Locale locale;

    public ValidationRuleMessagesGenerator(IFile messagesPropertiesFile, Locale locale) {
        this.messagesPropertiesFile = messagesPropertiesFile;
        this.locale = locale;
        try {
            if (messagesPropertiesFile.exists()) {
                getValidationMessages().load(messagesPropertiesFile.getContents());
            }
        } catch (CoreException e) {
            StdBuilderPlugin.log(e);
        }
    }

    public static String getMessageKey(IValidationRule validationRule) {
        IIpsObject ipsObject = validationRule.getIpsObject();
        String qualifiedName = ipsObject.getQualifiedName();
        String ruleName = validationRule.getName();
        return getMessageKey(qualifiedName, ruleName);
    }

    public static String getMessageKey(String policyCmptTypeQName, String ruleName) {
        return policyCmptTypeQName + KEY_SEPARATOR + ruleName;
    }

    /**
     * Extracting the replacement parameters from given messageText. The replacement parameters are
     * defined curly braces. In contrast to the replacement parameters used in {@link MessageFormat}
     * , these parameters could have names and not only indices. However you could use additional
     * format information separated by comma as used by {@link MessageFormat}.
     * 
     * @param internationalString the {@link InternationalString} containing all message texts
     * @param makeJavaIdentifier true to make parameter names to valid java identifier
     */
    public static LinkedHashSet<String> getReplacementParameters(IInternationalString internationalString,
            boolean makeJavaIdentifier) {
        LinkedHashSet<String> result = new LinkedHashSet<String>();
        for (ILocalizedString localizedString : internationalString.values()) {
            String text = localizedString.getValue();
            Matcher matcher = ValidationRuleMessagesGenerator.REPLACEMENT_PARAMETER_REGEXT.matcher(text);
            while (matcher.find()) {
                String parameterName = matcher.group();
                if (makeJavaIdentifier && !Character.isJavaIdentifierStart(parameterName.charAt(0))) {
                    parameterName = "p" + parameterName;
                }
                result.add(parameterName);
            }
        }
        return result;
    }

    /**
     * Saving the properties to the file adding the given comment. The file must already exists.
     * 
     * @param comment The comment for the properties file
     * @param markFileAsDerived Specify whether the file have to be marked as derived or not
     * @return true if file was modified otherwise false
     * @throws CoreException in case of any exception during writing to file
     */
    public boolean saveIfModified(String comment, boolean markFileAsDerived) throws CoreException {
        if (getValidationMessages().isModified()) {
            IFile file = getMessagesPropertiesFile();
            if (!file.exists()) {
                file.create(new ByteArrayInputStream("".getBytes()), true, null); //$NON-NLS-1$
                file.setDerived(markFileAsDerived);
            }
            storeMessagesToFile(file, getValidationMessages(), comment);
            return true;
        } else {
            return false;
        }
    }

    public void clearMessages() {
        getValidationMessages().clear();
        pcTypeNamesToValidationRuleNamesMap.clear();
    }

    private void storeMessagesToFile(IFile propertyFile, MessagesProperties messages, String comments)
            throws CoreException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        messages.store(outputStream, comments);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        propertyFile.setContents(inputStream, true, true, new NullProgressMonitor());
    }

    public void loadMessages() throws CoreException {
        if (messagesPropertiesFile.exists()) {
            getValidationMessages().load(messagesPropertiesFile.getContents());
        } else {
            getValidationMessages().clear();
        }
    }

    /**
     * @return Returns the messagesPropertiesFile.
     */
    public IFile getMessagesPropertiesFile() {
        return messagesPropertiesFile;
    }

    public void generate(IPolicyCmptType ipsObject) {
        IPolicyCmptType policyCmptType = ipsObject;
        List<IValidationRule> validationRules = policyCmptType.getValidationRules();
        Set<String> ruleNames = getRuleNames(policyCmptType.getQualifiedName());
        deleteMessagesForDeletedRules(policyCmptType.getQualifiedName(), validationRules, ruleNames);
        for (IValidationRule validationRule : validationRules) {
            addValidationRuleMessage(validationRule, ruleNames);
        }
    }

    /**
     * Getting the name of the policy component type, a list of rules, stored in this policy
     * component types and a list of ruleNames that were stored in the type during last build. This
     * method deletes the message for every rule that is located in the ruleNameSet but not in the
     * list of validation rules.
     * 
     * @param pcTypeName The name of the policy component type used to get the message key
     * @param validationRules the rules that are actually stored in the policy component type
     * @param ruleNameSet the set of rule name that were in the policy component type during last
     *            build
     */
    void deleteMessagesForDeletedRules(String pcTypeName, List<IValidationRule> validationRules, Set<String> ruleNameSet) {
        for (String ruleName : new HashSet<String>(ruleNameSet)) {
            boolean foundRule = false;
            for (IValidationRule rule : validationRules) {
                if (rule.getName().equals(ruleName)) {
                    foundRule = true;
                    break;
                }
            }
            if (!foundRule) {
                String key = getMessageKey(pcTypeName, ruleName);
                validationMessages.remove(key);
                ruleNameSet.remove(ruleName);
            }
        }
    }

    public void deleteAllMessagesFor(String pcTypeName) {
        Set<String> ruleNames = getRuleNames(pcTypeName);
        for (String ruleName : new HashSet<String>(ruleNames)) {
            String key = getMessageKey(pcTypeName, ruleName);
            validationMessages.remove(key);
            ruleNames.remove(ruleName);
        }
    }

    void addValidationRuleMessage(IValidationRule validationRule, Set<String> ruleNames) {
        String messageText = getMessageText(validationRule);
        if (messageText == null) {
            return;
        }
        getValidationMessages().put(getMessageKey(validationRule), messageText);
        ruleNames.add(validationRule.getName());
    }

    /**
     * Getting the message text from {@link IValidationRule} and convert the replace parameters to
     * match java {@link MessageFormat}
     * 
     * @param validationRule The validationRule holding the message text
     * @return the text of validationRule with converted replacement parameters
     */
    String getMessageText(IValidationRule validationRule) {
        IInternationalString internationalString = validationRule.getMessageText();
        ILocalizedString localizedString = internationalString.get(locale);
        if (localizedString == null) {
            return null;
        }
        String messageText = localizedString.getValue();
        if (messageText == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();

        Matcher matcher = REPLACEMENT_PARAMETER_REGEXT.matcher(messageText);
        int lastEnd = 0;
        LinkedHashSet<String> parameterNames = getReplacementParameters(internationalString, false);
        while (matcher.find()) {
            result.append(messageText.substring(lastEnd, matcher.start()));
            String parameterName = matcher.group();
            Integer argumentIndex = getIndexOf(parameterName, parameterNames);
            result.append(argumentIndex);
            lastEnd = matcher.end();
        }
        result.append(messageText.substring(lastEnd));
        return result.toString();
    }

    private Integer getIndexOf(String parameterName, LinkedHashSet<String> parameterNames) {
        int i = 0;
        for (String string : parameterNames) {
            if (string.equals(parameterName)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    Set<String> getRuleNames(String pcTypeName) {
        Set<String> ruleNames = pcTypeNamesToValidationRuleNamesMap.get(pcTypeName);
        if (ruleNames == null) {
            ruleNames = new HashSet<String>();
            pcTypeNamesToValidationRuleNamesMap.put(pcTypeName, ruleNames);
        }
        return ruleNames;
    }

    /**
     * @return Returns the validationMessages.
     */
    public MessagesProperties getValidationMessages() {
        return validationMessages;
    }

}
