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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.ITimedIpsObject;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IDeleteListener;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;


/**
 * A section that displays a timed pdobject's generations.
 */
public class GenerationsSection extends SimpleIpsPartsSection{

	/**
	 * The page owning this section.
	 */
	private ProductCmptPropertiesPage page;
	
	/**
	 * Create a new Section to display generations.
	 * @param page The page owning this section.
	 * @param parent The composit which is parent for this section
	 * @param toolkit The toolkit to help creating the ui
	 */
    public GenerationsSection(
            ProductCmptPropertiesPage page, 
            Composite parent,
            UIToolkit toolkit) {
        super(page.getProductCmpt(), parent, Section.TITLE_BAR, 
        		IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNamePlural(), toolkit);
        this.page = page;
    }

    /**
     * {@inheritDoc}
     */
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new GenerationsComposite((ITimedIpsObject)getIpsObject(), parent, toolkit);
    }
    
    /**
     * Set the active generation (which means, the generation to show/edit) in the editor. If the 
     * generation to set would not be editable, the user is asked if a switch is really wanted.
     */
    private void setActiveGeneration(IProductCmptGeneration generation) {
        if (generation==null) {
            return;
        }
        if (generation==page.getProductCmptEditor().getGenerationEffectiveOnCurrentEffectiveDate()) {
            page.getProductCmptEditor().setActiveGeneration(generation, false);
            return;
        }
        if (IpsPlugin.getDefault().getIpsPreferences().isWorkingModeBrowse()){
            page.getProductCmptEditor().setActiveGeneration(generation, false);
            return;
        }
        if (!IpsPlugin.getDefault().getIpsPreferences().canEditRecentGeneration()
                && generation.getValidFrom().before(new GregorianCalendar())) {
            page.getProductCmptEditor().setActiveGeneration(generation, false);
            return;
        }
		String genName = IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNameSingular();
		String title = NLS.bind(Messages.GenerationsSection_titleShowGeneration, genName);
		Object[] args = new Object[3];
		args[0] = genName;
		args[1] = generation.getName();
		args[2] = IpsPlugin.getDefault().getIpsPreferences().getFormattedWorkingDate();
		String message = NLS.bind(Messages.GenerationsSection_msgShowGeneration, args);	    		
        
        MessageDialog dlg = new MessageDialog(page.getSite().getShell(), title, null, message, MessageDialog.QUESTION, 
                new String[] {Messages.GenerationsSection_buttonChangeEffectiveDate, Messages.GenerationsSection_buttonKeepEffectiveDate, Messages.GenerationsSection_buttonCancel}, 0);
        int result = dlg.open();
        if (result == 2) {
            return; // cancel
        }
        if (result == 0) {
            IpsPlugin.getDefault().getIpsPreferences().setWorkingDate(generation.getValidFrom());
        }
        page.getProductCmptEditor().setActiveGeneration(generation, true);
    }
    
    private IProductCmptGeneration getActiveGeneration() {
    	return (IProductCmptGeneration)page.getProductCmptEditor().getActiveGeneration();
    }
    
    /**
     * A composite that shows a policy component's attributes in a viewer and 
     * allows to edit attributes in a dialog, create new attributes and delete attributes.
     */
    public class GenerationsComposite extends IpsPartsComposite implements IDeleteListener {

        public GenerationsComposite(ITimedIpsObject ipsObject, Composite parent,
                UIToolkit toolkit) {
            super(ipsObject, parent, false, true, true, false, true, toolkit);

            super.setEditDoubleClickListenerEnabled(false);
            
            getViewer().getControl().addMouseListener(new MouseAdapter() {
				public void mouseDoubleClick(MouseEvent e) {
					Object selected = ((IStructuredSelection)getViewer().getSelection()).getFirstElement();
					if (selected instanceof IProductCmptGeneration) {
						setActiveGeneration((IProductCmptGeneration)selected);
					}
				}
            });
            
			addDeleteListener(this);
        }
        
        public ITimedIpsObject getTimedIpsObject() {
            return (ITimedIpsObject)getIpsObject();
        }
        
        /**
         * {@inheritDoc}
         */
        protected IStructuredContentProvider createContentProvider() {
            return new ContentProvider();
        }
        
        /**
         * {@inheritDoc}
         */
        protected ILabelProvider createLabelProvider() {
			return new LabelProvider();
		}

        /**
         * {@inheritDoc}
         */
        protected IIpsObjectPart newIpsPart() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new GenerationEditDialog((IProductCmptGeneration)part, shell);
        }

		/**
		 * {@inheritDoc}
		 */
		public void aboutToDelete(IIpsObjectPart part) {
			if (page.getProductCmpt().getGenerations().length == 2) {
				super.deleteButton.setEnabled(false);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void deleted(IIpsObjectPart part) {
			page.getProductCmptEditor().setActiveGeneration(getSelectedGeneration(), true);
		}
		
		private IProductCmptGeneration getSelectedGeneration() {
			IIpsObjectPart selected = getSelectedPart();
			if (selected instanceof IProductCmptGeneration) {
				return (IProductCmptGeneration)selected;
			}
			return null;
		}
		
		/**
		 * {@inheritDoc}
		 */
		protected void updateButtonEnabledStates() {
			super.updateButtonEnabledStates();
			deleteButton.setEnabled(page.getProductCmpt().getGenerations().length>1);
		}
    	
    	private class ContentProvider implements IStructuredContentProvider {
    		public Object[] getElements(Object inputElement) {
    			 return getTimedIpsObject().getGenerations();
    		}
    		public void dispose() {
    			// nothing todo
    		}
    		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    			// nothing todo
    		}
    	}
    	
    	private class LabelProvider extends DefaultLabelProvider  {

			public String getText(Object element) {
				if (!(element instanceof IProductCmptGeneration)) {
					return super.getText(element);
				}
                IProductCmptGeneration gen = (IProductCmptGeneration)element;
                String comment = ""; //$NON-NLS-1$
                if (page.getProductCmptEditor().isEffectiveOnCurrentEffectiveDate(gen)) {
                    comment = comment + Messages.GenerationsSection_validFrom + IpsPlugin.getDefault().getIpsPreferences().getFormattedWorkingDate();
                }
                Boolean validFromInPast = gen.isValidFromInPast(); 
                if ((validFromInPast==null || validFromInPast.booleanValue()) 
                    && !IpsPlugin.getDefault().getIpsPreferences().canEditRecentGeneration()) {
                    if (!comment.equals("")) { //$NON-NLS-1$
                        comment = comment + ","; //$NON-NLS-1$
                    }
                    comment = comment + Messages.GenerationsSection_validFromInPast;
                }
				return super.getText(element) + comment;
			}

			public Image getImage(Object element) {
                if (!(element instanceof IProductCmptGeneration)) {
                    return super.getImage(element);
                }
				IProductCmptGeneration generation = (IProductCmptGeneration)element;
				Image image = super.getImage(element); 
				if (getActiveGeneration()==generation) {
					return image;
				} else {
					return page.getProductCmptEditor().getUneditableGenerationImage(image);
				}
			}
    	}
    }
    
}