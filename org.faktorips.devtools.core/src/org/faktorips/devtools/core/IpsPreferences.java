/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core;

import java.awt.GraphicsEnvironment;
import java.awt.im.InputContext;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.apache.commons.lang.LocaleUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.util.ArgumentCheck;

/**
 * The class gives access to the plugin's preferences.
 */
public class IpsPreferences {

    /**
     * Constant identifying the refactoring mode
     */
    public final static String REFACTORING_MODE = IpsPlugin.PLUGIN_ID + ".refactoringmode"; //$NON-NLS-1$

    /**
     * Constant identifying the refactoring mode direct
     */
    public final static String REFACTORING_MODE_DIRECT = "direct"; //$NON-NLS-1$

    /**
     * Constant identifying the refactoring mode explicit
     */
    public final static String REFACTORING_MODE_EXPLICIT = "explicit"; //$NON-NLS-1$

    /**
     * Constant identifying the working mode
     */
    public final static String WORKING_MODE = IpsPlugin.PLUGIN_ID + ".workingmode"; //$NON-NLS-1$

    /**
     * Constant identifying the working mode edit
     */
    public final static String WORKING_MODE_EDIT = "edit"; //$NON-NLS-1$

    /**
     * Constant identifying the working mode browse
     */
    public final static String WORKING_MODE_BROWSE = "browse"; //$NON-NLS-1$

    /**
     * Constant identifying the preference for null-value representation
     */
    public static final String NULL_REPRESENTATION_STRING = IpsPlugin.PLUGIN_ID + ".nullRepresentationString"; //$NON-NLS-1$

    /**
     * Constant identifying the changes over time naming concept preference.
     */
    public final static String CHANGES_OVER_TIME_NAMING_CONCEPT = IpsPlugin.PLUGIN_ID + ".changesOverTimeConcept"; //$NON-NLS-1$

    /**
     * Constant identifying the default postfix for product component types
     */
    public final static String DEFAULT_PRODUCT_CMPT_TYPE_POSTFIX = IpsPlugin.PLUGIN_ID
            + ".defaultProductCmptTypePostfix"; //$NON-NLS-1$

    /**
     * Constant identifying the preference for editing the runtime id.
     */
    public final static String MODIFY_RUNTIME_ID = IpsPlugin.PLUGIN_ID + ".modifyRuntimeId"; //$NON-NLS-1$

    /**
     * Constant identifying the enable generating preference.
     */
    public final static String ENABLE_GENERATING = IpsPlugin.PLUGIN_ID + ".enableGenerating"; //$NON-NLS-1$

    /**
     * Constant that identifies the navigate to model or source generating preference.
     */
    public final static String NAVIGATE_TO_MODEL_OR_SOURCE_CODE = IpsPlugin.PLUGIN_ID + ".navigateToModel"; //$NON-NLS-1$

    /**
     * Constant that identifies the advanced team functions in product definition explorer
     * preference.
     */
    public final static String ADVANCED_TEAM_FUNCTIONS_IN_PRODUCT_DEF_EXPLORER = IpsPlugin.PLUGIN_ID
            + ".advancedTeamFunctionsInProductDefExplorer"; //$NON-NLS-1$

    /**
     * Constant that identifies the easy context menu preferencee
     */
    public final static String SIMPLE_CONTEXT_MENU = IpsPlugin.PLUGIN_ID + ".simpleContextMenu"; //$NON-NLS-1$

    /**
     * Constant that identifies the number of sections in type editors preference.
     */
    public final static String SECTIONS_IN_TYPE_EDITORS = IpsPlugin.PLUGIN_ID + ".sectionsInTypeEditors"; //$NON-NLS-1$

    /**
     * Constant that defines 2 sections in type editors preference. This constant is a value for the
     * property <code>SECTIONS_IN_TYPE_EDITORS</code>.
     */
    public final static String TWO_SECTIONS_IN_TYPE_EDITOR_PAGE = IpsPlugin.PLUGIN_ID + ".twoSections"; //$NON-NLS-1$

    /**
     * Constant that defines 4 sections in type editors preference. This constant is a value for the
     * property <code>SECTIONS_IN_TYPE_EDITORS</code>.
     */
    public final static String FOUR_SECTIONS_IN_TYPE_EDITOR_PAGE = IpsPlugin.PLUGIN_ID + ".fourSections"; //$NON-NLS-1$

    /**
     * Constant identifying the IPS test runner max heap size preference.
     */
    public final static String IPSTESTRUNNER_MAX_HEAP_SIZE = IpsPlugin.PLUGIN_ID + ".ipsTestTunnerMaxHeapSize"; //$NON-NLS-1$

    /**
     * Constant identifying the enumeration display type.
     */
    public final static String ENUM_TYPE_DISPLAY = IpsPlugin.PLUGIN_ID + ".enumTypeDisplay"; //$NON-NLS-1$

    /**
     * Constant that identifies the locale to be used for formating values of specific datatypes.
     */
    public final static String DATATYPE_FORMATTING_LOCALE = IpsPlugin.PLUGIN_ID + ".datatypeFormattingLocale"; //$NON-NLS-1$

    private final DatatypeFormatter datatypeFormatter;

    private final IPreferenceStore prefStore;

    public IpsPreferences(IPreferenceStore prefStore) {
        ArgumentCheck.notNull(prefStore);
        this.prefStore = prefStore;
        prefStore.setDefault(NULL_REPRESENTATION_STRING, "<null>"); //$NON-NLS-1$
        prefStore.setDefault(CHANGES_OVER_TIME_NAMING_CONCEPT, IChangesOverTimeNamingConvention.FAKTOR_IPS);
        prefStore.setDefault(MODIFY_RUNTIME_ID, false);
        prefStore.setDefault(REFACTORING_MODE, REFACTORING_MODE_EXPLICIT);
        prefStore.setDefault(WORKING_MODE, WORKING_MODE_EDIT);
        prefStore.setDefault(ENABLE_GENERATING, true);
        prefStore.setDefault(IPSTESTRUNNER_MAX_HEAP_SIZE, ""); //$NON-NLS-1$
        prefStore.setDefault(ENUM_TYPE_DISPLAY, EnumTypeDisplay.NAME_AND_ID.getId());
        prefStore.setDefault(ADVANCED_TEAM_FUNCTIONS_IN_PRODUCT_DEF_EXPLORER, false);
        prefStore.setDefault(SIMPLE_CONTEXT_MENU, true);
        prefStore.setDefault(SECTIONS_IN_TYPE_EDITORS, TWO_SECTIONS_IN_TYPE_EDITOR_PAGE);

        setDefaultForDatatypeFormatting(prefStore);

        datatypeFormatter = new DatatypeFormatter(this);
    }

    /**
     * Retrieves the locale of the currently used keyboard layout via {@link InputContext} or the
     * java default locale if the inputContext is unavailable (e.g. in
     * headless/server-environments). The retrieved locale is used for datatype formating.
     * 
     * @param prefStore the preference store
     */
    private void setDefaultForDatatypeFormatting(IPreferenceStore prefStore) {
        Locale defaultLocale = null;
        if (GraphicsEnvironment.isHeadless()) {
            defaultLocale = Locale.getDefault();
        } else {
            try {
                InputContext inputContext = InputContext.getInstance();
                if (inputContext != null) {
                    defaultLocale = inputContext.getLocale();
                }
            } catch (Throwable t) {
                // We also want to catch errors because on a linux system without a X-Server the
                // virtual mashine throws an InternalError!
                IpsPlugin
                        .log(new Status(IStatus.WARNING, IpsPlugin.PLUGIN_ID,
                                "Cannot load default locale from input context. Use system default locale. (Maybe there is no X Server)")); //$NON-NLS-1$
            }
            if (defaultLocale == null) {
                defaultLocale = Locale.getDefault();
            }
        }
        prefStore.setDefault(DATATYPE_FORMATTING_LOCALE, defaultLocale.toString());
    }

    public void addChangeListener(IPropertyChangeListener listener) {
        prefStore.addPropertyChangeListener(listener);
    }

    public void removeChangeListener(IPropertyChangeListener listener) {
        prefStore.removePropertyChangeListener(listener);
    }

    /**
     * Returns the naming convention used in the GUI for product changes over time.
     */
    public IChangesOverTimeNamingConvention getChangesOverTimeNamingConvention() {
        String convention = IpsPlugin.getDefault().getPreferenceStore().getString(CHANGES_OVER_TIME_NAMING_CONCEPT);
        return IpsPlugin.getDefault().getIpsModel().getChangesOverTimeNamingConvention(convention);
    }

    /**
     * Returns the string to represent null values to the user.
     */
    public final String getNullPresentation() {
        return prefStore.getString(NULL_REPRESENTATION_STRING);
    }

    /**
     * Sets the new presentation for <code>null</code>.
     */
    public final void setNullPresentation(String newPresentation) {
        prefStore.setValue(NULL_REPRESENTATION_STRING, newPresentation);
    }

    /**
     * Returns the postfix used to create a default name for a product component type for a given
     * policy component type name.
     */
    public final String getDefaultProductCmptTypePostfix() {
        return prefStore.getString(DEFAULT_PRODUCT_CMPT_TYPE_POSTFIX);
    }

    /**
     * Returns date format for valid-from and effective dates.
     * <p>
     * To be consistent with other date formats and/or input fields this {@link DateFormat} uses the
     * locale used for all data type specific formats/fields.
     * 
     * @see #getDatatypeFormattingLocale()
     */
    public DateFormat getDateFormat() {
        return getDateFormat(getDatatypeFormattingLocale());
    }

    /**
     * Convenience method to get a formatted date using the format returned by
     * {@link #getDateFormat()}
     */
    public String getFormattedDate(GregorianCalendar date) {
        return getDateFormat().format(date.getTime());
    }

    /**
     * Returns date format to format dates in specified locale.
     */
    public DateFormat getDateFormat(Locale locale) {
        /*
         * Workaround to display the year in four digits when using UK/US locales. DateFormat.SHORT
         * displays only two digits for the year number whereas DateFormat.MEDIUM displays Months as
         * a word (e.g. "April 1st 2003"). For the german locale DateFormat.MEDIUM works just fine.
         */
        DateFormat result;
        if (Locale.UK.equals(locale)) {
            result = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$
        } else if (Locale.US.equals(locale)) {
            result = new SimpleDateFormat("MM/dd/yyyy"); //$NON-NLS-1$
        } else {
            result = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        }
        result.setLenient(false);
        return result;
    }

    /**
     * Returns the value of the enable generating preference.
     */
    public boolean getEnableGenerating() {
        return prefStore.getBoolean(ENABLE_GENERATING);
    }

    public void setEnableGenerating(boolean generate) {
        prefStore.setValue(ENABLE_GENERATING, generate);
    }

    /**
     * Returns the max heap size in megabytes for the IPS test runner. This parameter specify the
     * maximum size of the memory allocation pool for the test runner. Will be used to set the Xmx
     * Java virtual machines option for the IPS test runner virtual machine.
     */
    public String getIpsTestRunnerMaxHeapSize() {
        return prefStore.getString(IPSTESTRUNNER_MAX_HEAP_SIZE);
    }

    /**
     * Returns the enumeration type display. Specifies the text display of enumeration type edit
     * fields. E.g. display id or name only, or display both.
     * 
     * @see EnumTypeDisplay
     */
    public EnumTypeDisplay getEnumTypeDisplay() {
        String id = prefStore.getString(ENUM_TYPE_DISPLAY);
        EnumTypeDisplay enumTypeDisplay = EnumTypeDisplay.getValueById(id);
        if (enumTypeDisplay == null) {
            IpsPlugin.log(new IpsStatus("Unknown enum type with id: " + id //$NON-NLS-1$
                    + ". Use default enum type display."));//$NON-NLS-1$
            enumTypeDisplay = EnumTypeDisplay.DEFAULT;
        }
        return enumTypeDisplay;
    }

    /**
     * Sets the enum type display.
     * 
     * @throws NullPointerException if etDisplay is <code>null</code>
     */
    public void setEnumTypeDisplay(EnumTypeDisplay etDisplay) {
        ArgumentCheck.notNull(etDisplay);
        prefStore.setValue(ENUM_TYPE_DISPLAY, etDisplay.getId());
    }

    /**
     * Returns whether the navigation from product component to model is active (<code>true</code>)
     * or not.
     */
    public boolean canNavigateToModelOrSourceCode() {
        return prefStore.getBoolean(NAVIGATE_TO_MODEL_OR_SOURCE_CODE);
    }

    /**
     * Sets the working mode.
     */
    public void setWorkingMode(String workingMode) {
        prefStore.setValue(WORKING_MODE, workingMode);
    }

    /**
     * Sets the refactoring mode.
     */
    public void setRefactoringMode(String refactoringMode) {
        prefStore.setValue(REFACTORING_MODE, refactoringMode);
    }

    /**
     * Returns <code>true</code> if the currently set working mode is edit, <code>false</code>
     * otherwise
     */
    public boolean isWorkingModeEdit() {
        return prefStore.getString(WORKING_MODE).equals(WORKING_MODE_EDIT);
    }

    /**
     * Returns <code>true</code> if the currently set working mode is browse, <code>false</code>
     * otherwise.
     */
    public boolean isWorkingModeBrowse() {
        return prefStore.getString(WORKING_MODE).equals(WORKING_MODE_BROWSE);
    }

    public boolean isRefactoringModeDirect() {
        // TODO need to fix: FIPS-1029
        // return prefStore.getString(REFACTORING_MODE).equals(REFACTORING_MODE_DIRECT);
        return false;
    }

    public boolean isRefactoringModeExplicit() {
        // TODO need to fix: FIPS-1029
        // return prefStore.getString(REFACTORING_MODE).equals(REFACTORING_MODE_EXPLICIT);
        return true;
    }

    public boolean canModifyRuntimeId() {
        return prefStore.getBoolean(MODIFY_RUNTIME_ID);
    }

    /**
     * @deprecated Use {@link #isAvancedTeamFunctionsForProductDefExplorerEnabled()} instead
     */
    @Deprecated
    public boolean areAvancedTeamFunctionsForProductDefExplorerEnabled() {
        return isAvancedTeamFunctionsForProductDefExplorerEnabled();
    }

    public boolean isAvancedTeamFunctionsForProductDefExplorerEnabled() {
        return prefStore.getBoolean(ADVANCED_TEAM_FUNCTIONS_IN_PRODUCT_DEF_EXPLORER);
    }

    public void setAvancedTeamFunctionsForProductDefExplorerEnabled(boolean enabled) {
        prefStore.setValue(ADVANCED_TEAM_FUNCTIONS_IN_PRODUCT_DEF_EXPLORER, enabled);
    }

    public boolean isSimpleContextMenuEnabled() {
        return prefStore.getBoolean(SIMPLE_CONTEXT_MENU) || IpsPlugin.getDefault().isProductDefinitionPerspective();
    }

    public void setSimpleContextMenuEnabled(boolean enabled) {
        prefStore.setValue(SIMPLE_CONTEXT_MENU, enabled);
    }

    /**
     * Sets the number of sections displayed on a page of a type editor. Only the predefined values
     * TWO_SECTIONS_IN_TYPE_EDITOR_PAGE and FOUR_SECTIONS_IN_TYPE_EDITOR_PAGE are allowed.
     */
    public void setSectionsInTypeEditors(String numberOfSections) {
        // identity on purpose!!
        if (!(numberOfSections == TWO_SECTIONS_IN_TYPE_EDITOR_PAGE || numberOfSections == FOUR_SECTIONS_IN_TYPE_EDITOR_PAGE)) {
            throw new IllegalArgumentException(
                    "Valid argument values are the constants TWO_SECTIONS_IN_TYPE_EDITOR_PAGE or FOUR_SECTIONS_IN_TYPE_EDITOR_PAGE of the IpsPreferences."); //$NON-NLS-1$
        }
        prefStore.setValue(SECTIONS_IN_TYPE_EDITORS, numberOfSections);
    }

    /**
     * Returns the number of sections that are displayed on one page of a type editor.
     */
    public String getSectionsInTypeEditors() {
        return prefStore.getString(SECTIONS_IN_TYPE_EDITORS);
    }

    /**
     * The default value is the locale of the default java locale instead of the configured eclipse
     * locale.
     * 
     * @return the currently configured locale for formating values of the data types Integer,
     *         Double, Date.
     */
    public Locale getDatatypeFormattingLocale() {
        String localeString = prefStore.getString(DATATYPE_FORMATTING_LOCALE);
        return LocaleUtils.toLocale(localeString);
    }

    /**
     * Sets the locale used for formating values of the data types Integer, Double, Date.
     * 
     * @param locale the new locale to be used
     */
    public void setDatatypeFormattingLocale(Locale locale) {
        prefStore.setValue(DATATYPE_FORMATTING_LOCALE, locale.toString());
    }

    /**
     * Returns the formatter for Faktor-IPS data types.
     */
    public DatatypeFormatter getDatatypeFormatter() {
        return datatypeFormatter;
    }

    /**
     * Returns date/time format for dates including the time of day.
     * <p>
     * To be consistent with other date formats and/or input fields this {@link DateFormat} uses the
     * locale used for all data type specific formats/fields.
     * 
     * @see #getDatatypeFormattingLocale()
     */
    public DateFormat getDateTimeFormat() {
        return getDateTimeFormat(getDatatypeFormattingLocale());
    }

    /**
     * Returns date/time format to format dates including the time of day in specified locale.
     */
    public DateFormat getDateTimeFormat(Locale locale) {
        DateFormat result;
        if (Locale.UK.equals(locale)) {
            result = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss"); //$NON-NLS-1$
        } else if (Locale.US.equals(locale)) {
            result = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss a"); //$NON-NLS-1$
        } else {
            result = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
        }
        result.setLenient(false);
        return result;
    }

}
