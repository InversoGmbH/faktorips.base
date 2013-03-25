/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.IValidationRuleMessageText;
import org.faktorips.devtools.stdbuilder.StdBuilderPlugin;
import org.faktorips.values.LocalizedString;

public class ValidationRuleMessagesGenerator {

    /**
     * The separator to concatenate the key. We use the minus character because this character is
     * not allowed in names.
     */
    public static final String KEY_SEPARATOR = "-";

    private final Map<String, Set<String>> pcTypeNamesToValidationRuleNamesMap = new HashMap<String, Set<String>>();

    private final MessagesProperties validationMessages = new MessagesProperties();

    private final IFile messagesPropertiesFile;

    private final ISupportedLanguage supportedLanguage;

    private final ValidationRuleMessagesPropertiesBuilder builder;

    public ValidationRuleMessagesGenerator(IFile messagesPropertiesFile, ISupportedLanguage supportedLanguage,
            ValidationRuleMessagesPropertiesBuilder builder) {
        this.messagesPropertiesFile = messagesPropertiesFile;
        this.supportedLanguage = supportedLanguage;
        this.builder = builder;
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
     * Saving the properties to the file adding the given comment. The file must already exists.
     * 
     * @param comment The comment for the properties file
     * @return true if file was modified otherwise false
     * @throws CoreException in case of any exception during writing to file
     */
    public boolean saveIfModified(String comment) throws CoreException {
        if (getValidationMessages().isModified()) {
            IFile file = getMessagesPropertiesFile();
            if (!file.exists()) {
                file.create(new ByteArrayInputStream("".getBytes()), true, null); //$NON-NLS-1$
                file.setDerived(builder.buildsDerivedArtefacts()
                        && builder.getBuilderSet().isMarkNoneMergableResourcesAsDerived());
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
        builder.writeToFile(propertyFile, inputStream, true, true);
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
        if (messageText.isEmpty() && !supportedLanguage.isDefaultLanguage()) {
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
        IValidationRuleMessageText internationalString = validationRule.getMessageText();
        LocalizedString localizedString = internationalString.get(supportedLanguage.getLocale());
        String messageText = localizedString.getValue();
        StringBuilder result = new StringBuilder();

        Matcher matcher = IValidationRuleMessageText.REPLACEMENT_PARAMETER_REGEXT.matcher(messageText);
        int lastEnd = 0;
        LinkedHashSet<String> parameterNames = internationalString.getReplacementParameters();
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
