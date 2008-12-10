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

package org.faktorips.devtools.core.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.WorkbenchRunnableAdapter;


/**
 * Base class for wizards to create a new ips object.
 */
public abstract class NewIpsObjectWizard extends Wizard implements INewIpsObjectWizard, IPageChangedListener {
    
    private IStructuredSelection selection;
    
    // first page
    private IpsObjectPage objectPage;
    
    public NewIpsObjectWizard() {
        this.setDefaultPageImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("wizards/IpsElementWizard.png")); //$NON-NLS-1$
    }
    
    /** 
     * {@inheritDoc}
     */
    public final void addPages() {
        try {
            objectPage = createFirstPage(selection); 
            addPage(objectPage);
            IWizardPage[] additionalPages = createAdditionalPages(selection);
            for (int i = 0; additionalPages != null && i < additionalPages.length; i++) {
                addPage(additionalPages[i]);
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }
    
    /**
     * In addition the default behaviour of the super class implementation it calls the 
     * finishWhenThisPageIsComplete() method on IpsObjectPages. When the finishWhenThisPageIsComplete()
     * method of one of the pages returns true this method returns true. 
     */
    public boolean canFinish() {
        // Default implementation is to check if all pages are complete.
        IWizardPage[] pages = getPages();
        for (int i = 0; i < pages.length; i++) {
            if(!pages[i].isPageComplete()){
                return false;
            }
            if(pages[i] instanceof IpsObjectPage){
                if(((IpsObjectPage)pages[i]).finishWhenThisPageIsComplete()){
                    return true;
                }
            }
        }
        return true;
    }

    
    protected abstract IpsObjectPage createFirstPage(IStructuredSelection selection) throws Exception;
    
    /**
     * To create additional pages for this wizard this method need to be overridden and the additional
     * pages are to return by it.
     *  
     * @param selection the current selection within the workbench if any
     * @return the new addtion wizard pages or <code>null</code> if no additional page exists
     * @throws Exception exceptions that are thrown by this method will be logged and shown to the user
     */
    protected IWizardPage[] createAdditionalPages(IStructuredSelection selection) throws Exception{
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    public IpsObjectType getIpsObjectType() {
        return objectPage.getIpsObjectType();
    }
    
    /** 
     * {@inheritDoc}
     */
    public final boolean performFinish() {
        final IIpsPackageFragment pack = objectPage.getIpsPackageFragment();
        IWorkspaceRunnable op= new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
                IWizardPage[] pages = getPages();
                if(pages.length > 1){
                    monitor.beginTask("Creating objects", pages.length * 4); //$NON-NLS-1$
                } else{
                    monitor.beginTask("Creating object", 4); //$NON-NLS-1$
                }
                for (int i = 0; i < pages.length; i++) {
                    if(pages[i] instanceof IpsObjectPage){
                        IpsObjectPage page = (IpsObjectPage)pages[i];
                        if(page.canCreateIpsSrcFile()){
                            IIpsSrcFile srcFile = page.createIpsSrcFile(new SubProgressMonitor(monitor, 2));
                            ArrayList modifiedIpsObjects = new ArrayList(0);
                            page.finishIpsObjects(srcFile.getIpsObject(), modifiedIpsObjects);
                            srcFile.save(true, new SubProgressMonitor(monitor, 1));
                            SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
                            for (Iterator it = modifiedIpsObjects.iterator(); it.hasNext();) {
                                IIpsObject modifiedIpsObject = (IIpsObject)it.next();
                                modifiedIpsObject.getIpsSrcFile().save(true, subMonitor);
                            }
                        }
                    }
                }
                monitor.done();
            }
        };
        try {
            ISchedulingRule rule= null;
            Job job= Platform.getJobManager().currentJob();
            if (job != null)
                rule= job.getRule();
            IRunnableWithProgress runnable= null;
            if (rule != null) {
                runnable= new WorkbenchRunnableAdapter(op, rule);
            } else {
                runnable= new WorkbenchRunnableAdapter(op, ResourcesPlugin.getWorkspace().getRoot());
            }
            getContainer().run(false, true, runnable);
        } catch (InvocationTargetException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return false;
        } catch  (InterruptedException e) {
            return false;
        }
        IIpsSrcFile srcFile = pack.getIpsSrcFile(getIpsObjectType().getFileName(objectPage.getIpsObjectName()));
        IpsUIPlugin.getDefault().openEditor(srcFile);
        return true;
    }
    
    /** 
     * {@inheritDoc}
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }

    /**
     * This wizard registeres itself as an IPageChangedListener on the IWizardContainer if it implements the 
     * IPageChangeProvider interface. Hence the IpsObjectsPages will be informed via the pageEntered method
     * when the page is about to be entered (will be shown to the user). 
     */
    public void pageChanged(PageChangedEvent event) {
        IWizardPage page = (IWizardPage)event.getSelectedPage();
        setWindowTitle(page.getTitle());
        getContainer().updateWindowTitle();
        if(page instanceof IpsObjectPage){
            try{
                ((IpsObjectPage)page).pageEntered();
            } catch(CoreException e){
                IpsPlugin.log(e);
            }
        }
    }

    /**
     * Overrides the super class method and registers this wizard as an IPageChangedListener on the
     * provided IWizardContainer if it implements the IPageChangeProvider interface.
     */
    public void setContainer(IWizardContainer wizardContainer) {
        super.setContainer(wizardContainer);
        if(wizardContainer instanceof IPageChangeProvider){
            //in case this listener has already been added remove it first and then add it again
            //this might happen if the setContainer-Method is called several times during the life cycle
            //of this wizard
            ((IPageChangeProvider)wizardContainer).removePageChangedListener(this);
            ((IPageChangeProvider)wizardContainer).addPageChangedListener(this);
        }
    }

}