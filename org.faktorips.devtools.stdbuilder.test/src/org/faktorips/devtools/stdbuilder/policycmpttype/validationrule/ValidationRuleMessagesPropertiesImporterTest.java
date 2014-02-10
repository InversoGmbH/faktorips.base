/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.validationrule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.internal.model.pctype.ValidationRuleMessageText;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.IValidationRuleMessageText;
import org.faktorips.values.LocalizedString;
import org.junit.Test;

public class ValidationRuleMessagesPropertiesImporterTest {

    private final static String TEST_FILE = "org/faktorips/devtools/stdbuilder/policycmpttype/validationrule/validation-test-messages.properties";

    @Test
    public void testImport() throws Exception {

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(TEST_FILE);

        IIpsPackageFragmentRoot root = mock(IIpsPackageFragmentRoot.class);

        ValidationRuleMessagesPropertiesImporter importer = new ValidationRuleMessagesPropertiesImporter(inputStream,
                root, Locale.GERMAN);
        IStatus status = importer.importPropertyFile(new NullProgressMonitor());

        assertEquals(IStatus.WARNING, status.getSeverity());

        verify(root).findAllIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);
        verifyNoMoreInteractions(root);
    }

    @Test
    public void testImputStreamClose() throws Exception {
        InputStream inputStream = mock(InputStream.class);
        IIpsPackageFragmentRoot root = mock(IIpsPackageFragmentRoot.class);
        ValidationRuleMessagesPropertiesImporter importer = new ValidationRuleMessagesPropertiesImporter(inputStream,
                root, Locale.GERMAN);
        IStatus status = importer.importPropertyFile(new NullProgressMonitor());
        assertEquals(IStatus.OK, status.getSeverity());
        verify(inputStream).close();

    }

    @Test
    public void shouldImportNothing() throws Exception {

        IIpsPackageFragmentRoot root = mock(IIpsPackageFragmentRoot.class);

        Properties properties = new Properties();

        ValidationRuleMessagesPropertiesImporter importer = new ValidationRuleMessagesPropertiesImporter(
                mock(InputStream.class), root, Locale.GERMAN);
        IStatus result = importer.importProperties(properties, new NullProgressMonitor());

        assertTrue(result.isOK());

        verify(root).findAllIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);
        verifyNoMoreInteractions(root);
    }

    @Test
    public void shouldImportMessagesWithStatusOK() throws Exception {

        IIpsPackageFragmentRoot root = mock(IIpsPackageFragmentRoot.class);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(ipsSrcFile.isMutable()).thenReturn(true);

        IPolicyCmptType policyCmptType = mock(IPolicyCmptType.class);
        IValidationRule rule = mock(IValidationRule.class);

        List<IIpsSrcFile> srcFiles = new ArrayList<IIpsSrcFile>();
        srcFiles.add(ipsSrcFile);
        when(root.findAllIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE)).thenReturn(srcFiles);

        when(policyCmptType.getQualifiedName()).thenReturn("testPolicy");
        when(ipsSrcFile.getIpsObject()).thenReturn(policyCmptType);

        when(rule.getIpsObject()).thenReturn(policyCmptType);
        when(rule.getName()).thenReturn("testRule");
        when(rule.getMessageText()).thenReturn(new ValidationRuleMessageText());

        ArrayList<IValidationRule> rules = new ArrayList<IValidationRule>();
        rules.add(rule);
        when(policyCmptType.getValidationRules()).thenReturn(rules);

        Properties properties = new Properties();
        properties.setProperty("testPolicy-testRule", "TestMessage");

        ValidationRuleMessagesPropertiesImporter importer = new ValidationRuleMessagesPropertiesImporter(
                mock(InputStream.class), root, Locale.GERMAN);
        IStatus result = importer.importProperties(properties, new NullProgressMonitor());
        assertTrue(result.toString(), result.isOK());
        assertEquals(new LocalizedString(Locale.GERMAN, "TestMessage"), rule.getMessageText().get(Locale.GERMAN));

        verify(root).findAllIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);
        verifyNoMoreInteractions(root);
    }

    @Test
    public void shouldImportMessagesWithStatusIllegalMessage() throws Exception {
        IIpsPackageFragmentRoot root = mock(IIpsPackageFragmentRoot.class);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        IPolicyCmptType policyCmptType = mock(IPolicyCmptType.class);

        when(ipsSrcFile.isMutable()).thenReturn(true);
        List<IIpsSrcFile> srcFiles = new ArrayList<IIpsSrcFile>();
        srcFiles.add(ipsSrcFile);
        when(root.findAllIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE)).thenReturn(srcFiles);

        when(policyCmptType.getQualifiedName()).thenReturn("testPolicy");
        when(ipsSrcFile.getIpsObject()).thenReturn(policyCmptType);

        ArrayList<IValidationRule> rules = new ArrayList<IValidationRule>();
        when(policyCmptType.getValidationRules()).thenReturn(rules);

        Properties properties = new Properties();
        properties.setProperty("testPolicy-testRule", "TestMessage");

        ValidationRuleMessagesPropertiesImporter importer = new ValidationRuleMessagesPropertiesImporter(
                mock(InputStream.class), root, Locale.GERMAN);
        IStatus result = importer.importProperties(properties, new NullProgressMonitor());
        assertTrue(result.toString(), result.isMultiStatus());
        assertEquals(1, ((MultiStatus)result).getChildren().length);
        IStatus illegalMessageStatus = ((MultiStatus)result).getChildren()[0];
        assertEquals(ValidationRuleMessagesPropertiesImporter.MSG_CODE_ILLEGAL_MESSAGE, illegalMessageStatus.getCode());
        assertTrue(illegalMessageStatus.isMultiStatus());
        assertEquals(1, ((MultiStatus)illegalMessageStatus).getChildren().length);

        verify(root).findAllIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);
        verifyNoMoreInteractions(root);
    }

    @Test
    public void shouldImportMessagesWithStatusMissingMessage() throws Exception {
        IIpsPackageFragmentRoot root = mock(IIpsPackageFragmentRoot.class);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        IPolicyCmptType policyCmptType = mock(IPolicyCmptType.class);
        IValidationRule rule = mock(IValidationRule.class);

        when(ipsSrcFile.isMutable()).thenReturn(true);
        List<IIpsSrcFile> srcFiles = new ArrayList<IIpsSrcFile>();
        srcFiles.add(ipsSrcFile);
        when(root.findAllIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE)).thenReturn(srcFiles);

        when(policyCmptType.getQualifiedName()).thenReturn("testPolicy");
        when(ipsSrcFile.getIpsObject()).thenReturn(policyCmptType);

        when(rule.getIpsObject()).thenReturn(policyCmptType);
        when(rule.getName()).thenReturn("testRule");
        IValidationRuleMessageText messageText = mock(IValidationRuleMessageText.class);
        when(rule.getMessageText()).thenReturn(messageText);

        ArrayList<IValidationRule> rules = new ArrayList<IValidationRule>();
        rules.add(rule);
        when(policyCmptType.getValidationRules()).thenReturn(rules);

        Properties properties = new Properties();

        ValidationRuleMessagesPropertiesImporter importer = new ValidationRuleMessagesPropertiesImporter(
                mock(InputStream.class), root, Locale.GERMAN);
        IStatus result = importer.importProperties(properties, new NullProgressMonitor());
        assertTrue(result.toString(), result.isMultiStatus());
        assertEquals(1, ((MultiStatus)result).getChildren().length);
        IStatus missingMessageStatus = ((MultiStatus)result).getChildren()[0];
        assertEquals(ValidationRuleMessagesPropertiesImporter.MSG_CODE_MISSING_MESSAGE, missingMessageStatus.getCode());
        assertTrue(missingMessageStatus.isMultiStatus());
        assertEquals(1, ((MultiStatus)missingMessageStatus).getChildren().length);

        verify(root).findAllIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);
        verifyNoMoreInteractions(root);
    }
}
