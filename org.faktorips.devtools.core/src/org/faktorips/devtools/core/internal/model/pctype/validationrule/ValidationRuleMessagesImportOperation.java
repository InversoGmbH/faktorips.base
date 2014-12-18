/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.pctype.validationrule;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.values.LocalizedString;

public abstract class ValidationRuleMessagesImportOperation implements IWorkspaceRunnable {

    public static final int MSG_CODE_MISSING_MESSAGE = 1;

    public static final int MSG_CODE_ILLEGAL_MESSAGE = 2;

    public static final int MSG_CODE_MULTIPLE_USED_MESSAGECODES = 3;

    private final InputStream contents;

    private final IIpsPackageFragmentRoot root;

    private final Locale locale;

    private IProgressMonitor monitor = new NullProgressMonitor();

    private IStatus resultStatus = new Status(IStatus.OK, IpsPlugin.PLUGIN_ID, StringUtils.EMPTY);

    private ValidationRuleIdentification identification = ValidationRuleIdentification.QUALIFIED_RULE_NAME;

    private Map<String, String> contentMap;

    private List<String> importedMessageKeys;

    private MultiStatus missingMessages;

    private MultiStatus illegalMessages;

    private MultiStatus multipleUsedMessageCodes;

    public ValidationRuleMessagesImportOperation(InputStream contents, IIpsPackageFragmentRoot root, Locale locale) {
        this.contents = contents;
        this.root = root;
        this.locale = locale;
    }

    /**
     * @return Returns the resultStatus.
     */
    public IStatus getResultStatus() {
        return resultStatus;
    }

    public InputStream getContents() {
        return contents;
    }

    public IIpsPackageFragmentRoot getPackageFragmentRoot() {
        return root;
    }

    public Locale getLocale() {
        return locale;
    }

    @Override
    public void run(IProgressMonitor progressMonitor) throws CoreException {
        if (progressMonitor != null) {
            this.setMonitor(progressMonitor);
        }
        resultStatus = loadContent();
        if (resultStatus.getSeverity() != IStatus.ERROR) {
            resultStatus = importContentMap();
        }

    }

    protected abstract IStatus loadContent();

    public IProgressMonitor getMonitor() {
        return monitor;
    }

    public void setMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
    }

    public void setMethodOfIdentification(ValidationRuleIdentification identification) {
        this.identification = identification;
    }

    public ValidationRuleIdentification getMethodOfIdentification() {
        return identification;
    }

    void setKeyValueMap(Map<String, String> keyValueMap) {
        this.contentMap = keyValueMap;
    }

    protected IStatus importContentMap() throws CoreException {
        List<IIpsSrcFile> allPolicyCmptFiled = getPackageFragmentRoot().findAllIpsSrcFiles(
                IpsObjectType.POLICY_CMPT_TYPE);
        try {
            getMonitor().beginTask(Messages.ValidationRuleMessagesPropertiesImporter_status_importingMessages,
                    allPolicyCmptFiled.size() * 2 + 1);
            initResultFields();
            importValidationMessages(allPolicyCmptFiled);
            checkForIllegalMessages();
            return makeResultStatus();
        } finally {
            getMonitor().done();
        }
    }

    private void initResultFields() {
        importedMessageKeys = new ArrayList<String>();
        missingMessages = new MultiStatus(IpsPlugin.PLUGIN_ID, MSG_CODE_MISSING_MESSAGE,
                Messages.ValidationRuleMessagesPropertiesImporter_status_missingMessage, null);
        illegalMessages = new MultiStatus(IpsPlugin.PLUGIN_ID, MSG_CODE_ILLEGAL_MESSAGE,
                Messages.ValidationRuleMessagesPropertiesImporter_status_illegalMessage, null);
        multipleUsedMessageCodes = new MultiStatus(IpsPlugin.PLUGIN_ID, MSG_CODE_MULTIPLE_USED_MESSAGECODES,
                Messages.ValidationRuleCsvImporter_status_multipleUsedMessageCodes, null);
    }

    private List<String> importValidationMessages(List<IIpsSrcFile> allIpsSrcFiled) throws CoreException {
        for (IIpsSrcFile ipsSrcFile : allIpsSrcFiled) {
            if (!ipsSrcFile.isMutable()) {
                continue;
            }
            boolean dirtyState = ipsSrcFile.isDirty();
            IPolicyCmptType pcType = (IPolicyCmptType)ipsSrcFile.getIpsObject();
            importValidationMessages(pcType);
            getMonitor().worked(1);
            if (!dirtyState && ipsSrcFile.isDirty()) {
                ipsSrcFile.save(false, new SubProgressMonitor(getMonitor(), 1));
            }
        }
        return importedMessageKeys;
    }

    private void importValidationMessages(IPolicyCmptType pcType) {
        List<IValidationRule> validationRules = pcType.getValidationRules();
        for (IValidationRule validationRule : validationRules) {
            String messageKey = getMethodOfIdentification().getIdentifier(validationRule);
            String message = contentMap.get(messageKey);
            if (updateValidationMessage(validationRule, message)) {
                importedMessageKeys.add(messageKey);
            } else {
                missingMessages.add(new Status(IStatus.WARNING, IpsPlugin.PLUGIN_ID, NLS.bind(
                        Messages.ValidationRuleMessagesPropertiesImporter_warning_ruleNotFound, new String[] {
                                validationRule.getName(), pcType.getQualifiedName(), messageKey })));
            }
        }
        checkForMultipleUsedMessageCodes(validationRules);
    }

    private void checkForMultipleUsedMessageCodes(List<IValidationRule> validationRules) {
        for (IValidationRule rule : validationRules) {
            String messageKey = getMethodOfIdentification().getIdentifier(rule);
            if (importedMessageKeys.contains(messageKey)) {
                multipleUsedMessageCodes.add(new Status(IStatus.WARNING, IpsPlugin.PLUGIN_ID,
                        NLS.bind(Messages.ValidationRuleCsvImporter_warning_multipleUsedMessageCodes, messageKey,
                                rule.getName())));
            }
        }
    }

    private boolean updateValidationMessage(IValidationRule validationRule, String message) {
        if (message == null) {
            return false;
        } else {
            validationRule.getMessageText().add(new LocalizedString(getLocale(), message));
            return true;
        }
    }

    private void checkForIllegalMessages() {
        if (importedMessageKeys.size() < contentMap.size()) {
            for (Object key : contentMap.keySet()) {
                if (!importedMessageKeys.contains(key)) {
                    illegalMessages.add(new Status(IStatus.WARNING, IpsPlugin.PLUGIN_ID, NLS.bind(
                            Messages.ValidationRuleMessagesPropertiesImporter_warning_invalidMessageKey, key)));
                }
            }
        }
        getMonitor().worked(1);
    }

    private IStatus makeResultStatus() {
        MultiStatus result = new MultiStatus(IpsPlugin.PLUGIN_ID, 0,
                Messages.ValidationRuleMessagesPropertiesImporter_status_problemsDuringImport, null);
        if (!illegalMessages.isOK()) {
            result.add(illegalMessages);
        }
        if (!missingMessages.isOK()) {
            result.add(missingMessages);
        }
        if (!multipleUsedMessageCodes.isOK()) {
            result.add(multipleUsedMessageCodes);
        }
        if (result.isOK()) {
            return new Status(IStatus.OK, IpsPlugin.PLUGIN_ID, StringUtils.EMPTY);
        } else {
            return result;
        }
    }

}