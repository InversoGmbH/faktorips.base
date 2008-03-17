/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.controls.Checkbox;

/**
 * Action that allows to modify the working date (which is a part of the ips preferences)
 * with a simple click to a button in the toolbar. The modification made by the user using
 * this action has the same effect as if the user changes the working date on the
 * ips preferences page.
 * 
 * @author Thorsten Guenther
 */
public class ChangeWorkingDateAction implements IWorkbenchWindowActionDelegate {

	private Shell shell;

    private String generationConceptNamePlural;

    public class WorkingDatePmo extends PresentationModelObject {
        public static final String CAN_EDIT_RECENT_GENERATION = "editRecentGeneration"; //$NON-NLS-1$
        public static final String WORKING_DATE = "workingDate"; //$NON-NLS-1$
        
        private WorkingDateInputDialog dateInputDialog;

        public WorkingDatePmo(WorkingDateInputDialog dateInputDialog) {
            this.dateInputDialog = dateInputDialog;
        }

        public boolean isEditRecentGeneration() {
            return dateInputDialog.isCanEditRecentGeneration();
        }

        public void setEditRecentGeneration(boolean editRecentGeneration) {
            dateInputDialog.setCanEditRecentGeneration(editRecentGeneration);
        }
        
        public String getWorkingDate(){
            return dateInputDialog.getWorkingDate();
        }
        
        public void setWorkingDate(String workingDate){
            dateInputDialog.setWorkingDate(workingDate);
        }
    }

    private class WorkingDateInputDialog extends StatusDialog {
        private UIToolkit toolkit = new UIToolkit(null);
        private String workingDate;
        private boolean editRecentGeneration;
        private BindingContext bindingContext = new BindingContext();
        private Validator validator = new Validator();
        private String infoMessage;

        public WorkingDateInputDialog(Shell parent, String dialogTitle, String message, String workingDate, boolean editRecentGeneration) {
            super(parent);
            setTitle(dialogTitle);
            infoMessage = message;
            this.workingDate = workingDate;
            this.editRecentGeneration = editRecentGeneration;
            
            setHelpAvailable(false);
        }
        
        /**
         * {@inheritDoc}
         */
        protected Control createDialogArea(Composite parent) {
            Composite composite = (Composite)super.createDialogArea(parent);
            
            toolkit.createLabel(composite, infoMessage);
            
            Text text = toolkit.createText(composite);

            String labelText = NLS.bind(
                    Messages.ChangeWorkingDateAction_labelEditRecentGenerations,
                    IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention()
                    .getGenerationConceptNamePlural());
            Checkbox editRecentGenerationCheckBox = toolkit.createCheckbox(composite, labelText);

            WorkingDatePmo workingDatePmo = new WorkingDatePmo(this);
            bindingContext.bindContent(editRecentGenerationCheckBox, workingDatePmo,
                    WorkingDatePmo.CAN_EDIT_RECENT_GENERATION);
            bindingContext.bindContent(text, workingDatePmo, WorkingDatePmo.WORKING_DATE);

            bindingContext.updateUI();
            validate();
            
            return composite;
        }
        
        /**
         * @return Returns the workingDate.
         */
        public String getWorkingDate() {
            return workingDate;
        }

        /**
         * @param workingDate The workingDate to set.
         */
        public void setWorkingDate(String workingDate) {
            this.workingDate = workingDate;
            validate();
        }

        /**
         * @return Returns the canEditRecentGeneration.
         */
        public boolean isCanEditRecentGeneration() {
            return editRecentGeneration;
        }

        /**
         * @param canEditRecentGeneration The canEditRecentGeneration to set.
         */
        public void setCanEditRecentGeneration(boolean editRecentGeneration) {
            this.editRecentGeneration = editRecentGeneration;
            validate();
        }
        
        public void validate(){
            String result = validator.isValid(getWorkingDate());
            if (result != null){
                updateStatus(createStatus(IStatus.ERROR, result));
                return;
            } 

            Date date = validator.parseDate(getWorkingDate());
            if (!isCanEditRecentGeneration() && new Date().after(date)){
                updateInfoMessageCanEditRecentGenerations();
                return;
            }
            
            clearMessage();
            
        }
        
        private void clearMessage() {
            updateStatus(createStatus(IStatus.OK, "")); //$NON-NLS-1$
        }

        private void updateInfoMessageCanEditRecentGenerations() {
            updateStatus(createStatus(
                    IStatus.WARNING,
                    NLS.bind(Messages.ChangeWorkingDateAction_warningRecentGenerationReadOnlyRow1 +
                    Messages.ChangeWorkingDateAction_warningRecentGenerationReadOnlyRow2 +
                    Messages.ChangeWorkingDateAction_warningRecentGenerationReadOnlyRow3 +
                    Messages.ChangeWorkingDateAction_warningRecentGenerationReadOnlyRow4 + 
                    Messages.ChangeWorkingDateAction_warningRecentGenerationReadOnlyRow5, generationConceptNamePlural)));
            this.getShell().pack();
        }

        protected Control createContents(Composite parent) {
            Control content = super.createContents(parent);
            validate();
            centerWindow();
            return content;
        }
        
        private void centerWindow() {
            Point size = getInitialSize();
            Point location = getInitialLocation(size);
            this.getShell().setBounds(getConstrainedShellBounds(new Rectangle(location.x,
                    location.y, size.x, size.y)));            
        }

        private IStatus createStatus(int severity, String message){
            return new Status(severity, IpsPlugin.PLUGIN_ID, 0, message, null);
        }
    }

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(IWorkbenchWindow window) {
		shell = window.getShell();
	}

	/**
     * {@inheritDoc}
     */
	public void run(IAction action) {
		IpsPreferences ipsPreferences = IpsPlugin.getDefault().getIpsPreferences();
        
        generationConceptNamePlural = ipsPreferences.getChangesOverTimeNamingConvention().getGenerationConceptNamePlural();
        
        WorkingDateInputDialog dialog = new WorkingDateInputDialog(shell, Messages.ChangeWorkingDateAction_title, Messages.ChangeWorkingDateAction_description, ipsPreferences.getFormattedWorkingDate(), ipsPreferences.canEditRecentGeneration()); //$NON-NLS-1$ //$NON-NLS-1$

		if (dialog.open() == InputDialog.OK) {
			try {
				DateFormat format = ipsPreferences.getDateFormat();
				Date newDate = format.parse(dialog.getWorkingDate());
				GregorianCalendar calendar = new GregorianCalendar(); 
				calendar.setTime(newDate);
				ipsPreferences.setWorkingDate(calendar);
                ipsPreferences.setEditRecentGeneration(dialog.isCanEditRecentGeneration());
			} catch (ParseException e) {
				// should not happen if validator works correct.
				IpsPlugin.log(e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// nothing to do
	}

	/**
	 * Validates the input to be a valid date.
	 * 
	 * @author Thorsten Guenther
	 */
	private class Validator implements IInputValidator {
		DateFormat format;
		
		public Validator() {
			format = IpsPlugin.getDefault().getIpsPreferences().getDateFormat();
		}
		/**
		 * {@inheritDoc}
		 */
		public String isValid(String newText) {
			try {
				format.parse(newText);
			} catch (ParseException e) {
				String pattern;
				if (format instanceof SimpleDateFormat) {
					pattern = ((SimpleDateFormat)format).toLocalizedPattern();
				}
				else {
					pattern = ""; //$NON-NLS-1$
				}
				String msg = NLS.bind("", pattern); //$NON-NLS-1$
				return msg;
			}

			return null;
		}
        
        public Date parseDate(String workingDate){
            try {
                return format.parse(workingDate);
            } catch (ParseException e) {
                IpsPlugin.log(e);
                return null;
            }
        }
	}
}
