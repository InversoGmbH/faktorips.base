/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.product.ProductCmpt;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptGenerationPolicyCmptTypeDelta;
import org.faktorips.devtools.core.ui.editors.DescriptionPage;
import org.faktorips.devtools.core.ui.editors.IIpsObjectEditorSettings;
import org.faktorips.devtools.core.ui.editors.TimedIpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.productcmpt.deltapresentation.ProductCmptDeltaDialog;
import org.faktorips.values.DateUtil;

/**
 * Editor to a edit a product component.
 * 
 * @author Jan Ortmann
 * @author Thorsten Guenther
 */
public class ProductCmptEditor extends TimedIpsObjectEditor {

    /*
     * Setting key for user's decision not to choose a new product component type, because the old
     * can't be found.
     */
    private final static String SETTING_WORK_WITH_MISSING_TYPE = "workWithMissingType";

    /*
     * Setting key for the working date used in the editor. This might differ from the one
     * defined in the preferences.
     */
    private final static String SETTING_WORKING_DATE = "workingDate";

    /*
     * Setting key for user's decision not to choose a new product component type, because the old
     * can't be found.
     */
    private final static String SETTING_ACTIVE_GENERATION_MANUALLY_SET = "activeGenerationManuallySet";

    private GenerationPropertiesPage generationPropertiesPage;

    private boolean isHandlingWorkingDateMismatch = false;
    
    /**
	 * Creates a new editor for product components.
	 */
	public ProductCmptEditor() {
		super();
	}
    
    /**
	 * {@inheritDoc}
	 */
	protected void addPagesForParsableSrcFile() throws PartInitException , CoreException {
		IProductCmpt cmpt = (ProductCmpt)getIpsObject();
        IIpsObjectEditorSettings settings = getSettings();
        // open the select template dialog if the templ. is missing and the data is changeable 
		if (getProductCmpt().findProductCmptType() == null 
                && couldDateBeChangedIfProductCmptTypeWasntMissing()
                && !IpsPlugin.getDefault().isTestMode()
                && !settings.getBoolean(getIpsSrcFile(), SETTING_WORK_WITH_MISSING_TYPE)) {
            String msg = NLS.bind(Messages.ProductCmptEditor_msgTemplateNotFound, cmpt.getPolicyCmptType());
            SetTemplateDialog d = new SetTemplateDialog(cmpt, getSite().getShell(), msg);
            int rc = d.open();
            if (rc==Dialog.CANCEL) {
                getSettings().put(getIpsSrcFile(), SETTING_WORK_WITH_MISSING_TYPE, true);
            }
        }
        this.generationPropertiesPage = new GenerationPropertiesPage(this);
		addPage(generationPropertiesPage);
		addPage(new ProductCmptPropertiesPage(this));
		addPage(new DescriptionPage(this));
        IIpsObjectGeneration gen = getGenerationEffectiveOnCurrentEffectiveDate();
        if (gen==null) {
            gen = cmpt.getGenerations()[cmpt.getNumOfGenerations()-1];            
        }
        setActiveGeneration(gen, false);
	}
    
    private GenerationPropertiesPage getGenerationPropertiesPage() {
        if (generationPropertiesPage.getPartControl()==null || generationPropertiesPage.getPartControl().isDisposed()) {
            return null;
        }
        return generationPropertiesPage;
    }
    
	/**
	 * Returns the product component for the sourcefile edited with this editor.
	 */
	IProductCmpt getProductCmpt() {
		try {
			return (IProductCmpt) getIpsSrcFile().getIpsObject();
		} catch (Exception e) {
			IpsPlugin.logAndShowErrorDialog(e);
			throw new RuntimeException(e);
		}
	}
    
    private GregorianCalendar getWorkingDateUsedInEditor() {
        String s = getSettings().get(getIpsSrcFile(), SETTING_WORKING_DATE);
        try {
            return DateUtil.parseIsoDateStringToGregorianCalendar(s);
        } catch (IllegalArgumentException e) {
            IpsPlugin.log(e); // if it can't be parsed we use null.
            return null;
        }
    }
    
    private void setWorkingDateUsedInEditor(GregorianCalendar date) {
        getSettings().put(getIpsSrcFile(), SETTING_WORKING_DATE, DateUtil.gregorianCalendarToIsoDateString(date));
    }
    
	/**
	 * {@inheritDoc}
	 */
	public void editorActivated() {
	    if (TRACE) {
         logMethodStarted("editorActivated()");    //$NON-NLS-1$
        }
        updateChosenActiveGeneration();
		super.editorActivated();
        if (TRACE) {
             logMethodFinished("editorActivated()");    //$NON-NLS-1$
        }
	}

    /**
     * {@inheritDoc}
     */
    protected void checkForInconsistenciesToModel() {
        if (TRACE) {
            logMethodStarted("checkForInconsistenciesToModel"); //$NON-NLS-1$
        }
        boolean allGenerationsEditabled = true;
        IIpsObjectGeneration[] gens = getProductCmpt().getGenerations();
        for (int i = 0; i < gens.length; i++) {
            IProductCmptGeneration gen = (IProductCmptGeneration)gens[i];
            if (!isGenerationEditable(gen)) {
                allGenerationsEditabled = false;
                break;
            }
        }
        if (allGenerationsEditabled) {
            super.checkForInconsistenciesToModel();
        } else {
            if (TRACE) {
                logInternal("checkForInconsistenciesToModel - no need to check, at least one of the generations is not editable."); //$NON-NLS-1$
            }
        }
        if (TRACE) {
            logMethodFinished("checkForInconsistenciesToModel"); //$NON-NLS-1$
        }
    }

	/**
	 * {@inheritDoc}
	 */
	protected String getUniformPageTitle() {
		if (!isSrcFileUsable()) {
			String filename = getIpsSrcFile()==null?"null":getIpsSrcFile().getName(); //$NON-NLS-1$
			return NLS.bind(Messages.ProductCmptEditor_msgFileOutOfSync, filename);
		}
		return Messages.ProductCmptEditor_productComponent
				+ getProductCmpt().getName();
	}

	/**
	 * Checks if the currently active generations valid-from-date matches exactly the currently set
	 * working date. If not so, a search for a matching generation is started. If nothing is found, the user
	 * is asked to create a new one. 
	 */
	private void updateChosenActiveGeneration() {
        try {
            if (!getIpsSrcFile().isContentParsable()) {
                return;
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return;
        }
		IProductCmpt prod = getProductCmpt();
		GregorianCalendar workingDate = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
		IProductCmptGeneration generation = (IProductCmptGeneration)prod.getGenerationByEffectiveDate(workingDate);

        if (generation!=null) {
            setWorkingDateUsedInEditor(workingDate);
            if (!generation.equals(getActiveGeneration())) {
                // we found a generation matching the working date, but the found one is not active,
                // so make it active.
                this.setActiveGeneration(generation, false);
            }
            return;
        }
        // no generation for the _exact_ current working date.
		if (workingDate.equals(getWorkingDateUsedInEditor())) {
			// check happned before and user decided not to create a new generation - dont bother 
			// the user with repeating questions.
            return;
		}
		IpsPreferences prefs = IpsPlugin.getDefault().getIpsPreferences();
		if (prefs.isWorkingModeBrowse()) {
			// just browsing - show the generation valid at working date
			if (!getSettings().getBoolean(getIpsSrcFile(), SETTING_ACTIVE_GENERATION_MANUALLY_SET)) {
				showGenerationEffectiveOn(prefs.getWorkingDate());
			}
			return;
		}
        handleWorkingDateMissmatch();
	}

    /**
     * {@inheritDoc}
     */
	public void propertyChange(PropertyChangeEvent event) {
        if (!isActive()) {
            super.propertyChange(event);
            return;
        }
		String property = event.getProperty();
		if (property.equals(IpsPreferences.WORKING_DATE)) {
			getSettings().put(getIpsSrcFile(), SETTING_ACTIVE_GENERATION_MANUALLY_SET, false);
			updateChosenActiveGeneration();
		} else if (property.equals(IpsPreferences.EDIT_RECENT_GENERATION)) {
            refresh();
		} else if (event.getProperty().equals(IpsPreferences.WORKING_MODE)) {
            getSettings().put(getIpsSrcFile(), SETTING_ACTIVE_GENERATION_MANUALLY_SET, false);
            // refresh is done in superclass
		}
        super.propertyChange(event);
	}

	public void setActiveGeneration(IIpsObjectGeneration generation, boolean manuallySet) {
		if (generation == null) {
			return;
		}
		if (generation!=getActiveGeneration()) {
			super.setActiveGeneration(generation);
            if (getGenerationPropertiesPage()!=null) {
                generationPropertiesPage.rebuildInclStructuralChanges();
            }
            refresh();
		}
        getSettings().put(getIpsSrcFile(), SETTING_ACTIVE_GENERATION_MANUALLY_SET, manuallySet);
	}
    
    /**
     * {@inheritDoc}
     */
    protected boolean computeDataChangeableState() {
        if (!couldDateBeChangedIfProductCmptTypeWasntMissing()) {
            return false;
        }
        try {
            return getProductCmpt().findProductCmptType()!=null;
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return false;
        }
    }
    
    boolean couldDateBeChangedIfProductCmptTypeWasntMissing() {
        return super.computeDataChangeableState();
    }
    
    /**
     * Returns <code>true</code> if the active generation is editable, otherwise <code>false</code>.
     */
    public boolean isActiveGenerationEditable() {
        return isGenerationEditable((IProductCmptGeneration)getActiveGeneration());
    }

    /**
     * Returns <code>true</code> if the given generation is editable, otherwise <code>false</code>.
     */
    public boolean isGenerationEditable(IProductCmptGeneration gen) {
        if (gen==null) {
            return false;
        }
		// if generation is not effective in the current effective date, no editing is possible
		if (!gen.equals(getGenerationEffectiveOnCurrentEffectiveDate())) {
			return false;
		}
        if (!gen.getIpsSrcFile().isMutable()) {
            return false;
        }
        if (gen.isValidFromInPast()!=null && gen.isValidFromInPast().booleanValue()) {
            IpsPreferences pref = IpsPlugin.getDefault().getIpsPreferences();
            return pref.canEditRecentGeneration();
		}
		return true;
	}
	
	private void showGenerationEffectiveOn(GregorianCalendar date) {
		IIpsObjectGeneration generation = getProductCmpt().findGenerationEffectiveOn(date);
		if (generation == null) {
			generation = getProductCmpt().getFirstGeneration();
		}
		setActiveGeneration(generation, false);
	}
	
	private void handleWorkingDateMissmatch() {
        // following if statement is there as closing the dialog triggers a window activated event
        // and handling the evant calls this method.  
        if (isHandlingWorkingDateMismatch) {
            return;
        }
        isHandlingWorkingDateMismatch = true;
		IProductCmpt cmpt = getProductCmpt();
		GenerationSelectionDialog dialog = new GenerationSelectionDialog(getContainer().getShell(), cmpt);
		dialog.open(); // closing the dialog triggers an window activation event
        isHandlingWorkingDateMismatch = false;
        int choice = GenerationSelectionDialog.CHOICE_BROWSE;
        if (IpsPlugin.getDefault().isTestMode()) {
            choice = IpsPlugin.getDefault().getTestAnswerProvider().getIntAnswer();
        } else {
            if (dialog.getReturnCode() == GenerationSelectionDialog.OK) {
                choice = dialog.getChoice();
            }
        }
        GregorianCalendar workingDate = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
        setWorkingDateUsedInEditor(workingDate);
        switch (choice) {
            case GenerationSelectionDialog.CHOICE_BROWSE:
                setActiveGeneration(cmpt.findGenerationEffectiveOn(workingDate), false);
                break;
    
            case GenerationSelectionDialog.CHOICE_CREATE:
                setActiveGeneration(cmpt.newGeneration(workingDate), false);
                break;
    
            case GenerationSelectionDialog.CHOICE_SWITCH:
                IProductCmptGeneration generation = dialog.getSelectedGeneration();
                if (generation == null) {
                    generation = (IProductCmptGeneration) getProductCmpt()
                            .getFirstGeneration();
                }
                setActiveGeneration(generation, true);
                IpsPreferences prefs = IpsPlugin.getDefault().getIpsPreferences();
                prefs.setWorkingDate(generation.getValidFrom());
                break;
    
            default:
                IpsPlugin.log(new IpsStatus("Unknown choice: " //$NON-NLS-1$
                        + dialog.getChoice()));
                break;
        }
	}
	
    /**
     * {@inheritDoc}
     * @throws CoreException 
     */
    protected Dialog createDialogToFixDifferencesToModel() throws CoreException {

        IIpsObjectGeneration[] gen = this.getProductCmpt().getGenerations();
        IProductCmptGeneration[] generations = new IProductCmptGeneration[gen.length];
        for (int i = 0; i < generations.length; i++) {
            generations[i] = (IProductCmptGeneration)gen[i];
        }
        IProductCmptGenerationPolicyCmptTypeDelta[] deltas = new IProductCmptGenerationPolicyCmptTypeDelta[generations.length];
        for (int i = 0; i < generations.length; i++) {          
                deltas[i] = ((IProductCmptGeneration)generations[i]).computeDeltaToPolicyCmptType();
        }
        
        return new ProductCmptDeltaDialog(generations, deltas, getSite().getShell());
    }
    
    /**
     * {@inheritDoc}
     */
    public void contentsChanged(final ContentChangeEvent event) {
        if (event.getIpsSrcFile().equals(getIpsSrcFile())) {
            if (event.getEventType()==ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED) {
                setWorkingDateUsedInEditor(null);
                getSettings().put(getIpsSrcFile(), SETTING_ACTIVE_GENERATION_MANUALLY_SET, false);
            }
        }
        super.contentsChanged(event);
    }

    /**
     * {@inheritDoc}
     */
    protected void refreshInclStructuralChanges() {
        try {
            getIpsSrcFile().getIpsObject();
            updateChosenActiveGeneration();
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        if (getGenerationPropertiesPage()!=null) {
            generationPropertiesPage.rebuildInclStructuralChanges();
        }
    }
    
    private void logMethodStarted(String msg) {
        logInternal("." + msg + " - started"); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
    }
    
    private void logMethodFinished(String msg) {
        logInternal("." + msg + " - finished"); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
    }
    
    private void logInternal(String msg) {
        String file = getIpsSrcFile()==null ? "null" : getIpsSrcFile().getName(); // $NON-NLS-1$ //$NON-NLS-1$
        System.out.println(getLogPrefix() + msg + ", IpsSrcFile=" + file + ", Thread=" + Thread.currentThread().getName()); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
    }
    
    private String getLogPrefix() {
        return "ProductCmptEditor"; //$NON-NLS-1$
    }
    
}
