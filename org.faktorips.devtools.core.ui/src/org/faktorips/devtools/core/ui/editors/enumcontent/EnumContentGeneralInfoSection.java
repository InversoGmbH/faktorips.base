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

package org.faktorips.devtools.core.ui.editors.enumcontent;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.ArgumentCheck;

/**
 * The general info section for the enum content editor. It shows the enum type the enum content to
 * edit is built upon and provides navigation to the enum type.
 * 
 * @see EnumContentEditor
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumContentGeneralInfoSection extends IpsSection {

    /** The enum content the editor is currently editing. */
    private IEnumContent enumContent;

    /**
     * Creates a new <code>EnumContentGeneralInfoSection</code> using the specified parameters.
     * 
     * @param enumContent The enum content the enum content editor is currently editing.
     * @param parent The parent ui composite to attach this info section to.
     * @param toolkit The ui toolkit to be used to create new ui elements.
     * 
     * @throws NullPointerException If <code>enumContent</code> is <code>null</code>.
     */
    public EnumContentGeneralInfoSection(IEnumContent enumContent, Composite parent, UIToolkit toolkit) {
        super(parent, Section.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);

        ArgumentCheck.notNull(enumContent);

        this.enumContent = enumContent;

        initControls();
        setText(Messages.EnumContentGeneralInfoSection_title);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        client.setLayout(new GridLayout(1, false));
        Composite composite = toolkit.createLabelEditColumnComposite(client);

        if (IpsPlugin.getDefault().getIpsPreferences().canNavigateToModelOrSourceCode()) {
            Hyperlink link = toolkit.createHyperlink(composite, Messages.EnumContentGeneralInfoSection_linkEnumType);
            link.addHyperlinkListener(new HyperlinkAdapter() {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public void linkActivated(HyperlinkEvent event) {
                    // If the setting has been changed while the editor was opened
                    if (!(IpsPlugin.getDefault().getIpsPreferences().canNavigateToModelOrSourceCode())) {
                        return;
                    }

                    IEnumType enumType;
                    try {
                        enumType = enumContent.findEnumType(enumContent.getIpsProject());
                        if (enumType != null) {
                            IpsUIPlugin.getDefault().openEditor(enumType);
                        }
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } else {
            toolkit.createLabel(composite, Messages.EnumContentGeneralInfoSection_linkEnumType);
        }

        Label enumTypeLabel = toolkit.createLabel(composite, enumContent.getEnumType());
        bindingContext.bindContent(enumTypeLabel, enumContent, IEnumContent.PROPERTY_ENUM_TYPE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performRefresh() {
        bindingContext.updateUI();
    }

}
