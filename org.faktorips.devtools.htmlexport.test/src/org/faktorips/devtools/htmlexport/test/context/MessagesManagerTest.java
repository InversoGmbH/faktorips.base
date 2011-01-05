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

package org.faktorips.devtools.htmlexport.test.context;

import java.util.Locale;

import junit.framework.TestCase;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.MessagesManager;

public class MessagesManagerTest extends TestCase {
    private DocumentationContext context;
    private String key;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        context = new DocumentationContext();

        key = "ProjectOverviewPageElement_project";
    }

    public void testProjectOverviewPageElementProjectEn() {
        context.setDescriptionLocale(Locale.UK);

        MessagesManager manager = new MessagesManager(context);

        assertEquals("Project", manager.getMessage(key));
        assertTrue(context.getExportStatus().isOK());
    }

    public void testProjectOverviewPageElementProjectDe() {
        context.setDescriptionLocale(Locale.GERMAN);

        MessagesManager manager = new MessagesManager(context);

        assertEquals("Projekt", manager.getMessage(key));
        assertTrue(context.getExportStatus().isOK());
    }

    public void testProjectOverviewPageElementProjectNotUsedLocaleWithFallback() {
        context.setDescriptionLocale(Locale.TRADITIONAL_CHINESE);

        MessagesManager manager = new MessagesManager(context);

        assertFalse(context.getExportStatus().isOK());

        // result depends on PlatformLanguage (English or German)
        String expectedResult = manager.getPlatformLanguage().equals("en") ? "Project" : "Projekt";

        assertEquals(expectedResult, manager.getMessage(key));
    }

    public void testProjectOverviewPageElementProjectNotUsedKey() {
        String wrongKey = "definitiv ungültiger name für eine property";
        context.setDescriptionLocale(Locale.UK);

        MessagesManager manager = new MessagesManager(context);

        assertTrue(context.getExportStatus().isOK());
        assertEquals(wrongKey, manager.getMessage(wrongKey));
        assertFalse(context.getExportStatus().isOK());
    }
}