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

package org.faktorips.devtools.core.ui.controls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.DatatypeCompletionProcessor;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.editors.pctype.ParameterInfo;
import org.faktorips.devtools.core.ui.editors.pctype.ParameterListChangeListener;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;


/**
 * A special control to edit and reorder method parameters.
 * 
 * @author Jan Jan Ortmann
 */
public abstract class ChangeParametersControl extends Composite {

	private static class ParameterInfoContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return removeMarkedAsDeleted((List) inputElement);
		}
		private ParameterInfo[] removeMarkedAsDeleted(List paramInfos){
			List result= new ArrayList(paramInfos.size());
			for (Iterator iter= paramInfos.iterator(); iter.hasNext();) {
				ParameterInfo info= (ParameterInfo) iter.next();
				if (! info.isDeleted())
					result.add(info);
			}
			return (ParameterInfo[]) result.toArray(new ParameterInfo[result.size()]);
		}
		public void dispose() {
			// do nothing
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// do nothing
		}
	}

	private class ParameterInfoLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex!=MESSAGE_PROP) {
			    return null;
			}
			try {
				MessageList list = validate((ParameterInfo)element);
				return ValidationUtils.getSeverityImage(list.getSeverity());
			} catch (CoreException e) {
			    IpsPlugin.log(e);
			    return null;
			}
		}
		
		public String getColumnText(Object element, int columnIndex) {
			ParameterInfo info= (ParameterInfo) element;
			if (columnIndex == MESSAGE_PROP)
				return ""; //$NON-NLS-1$
			if (columnIndex == TYPE_PROP)
				return info.getNewTypeName();
			if (columnIndex == NEWNAME_PROP)
				return info.getNewName();
			if (columnIndex == DEFAULT_PROP) {
			    if (info.isAdded())
			        return info.getDefaultValue();
			    else
			        return "-"; //$NON-NLS-1$
			}
			throw new RuntimeException("Unknown column " + columnIndex); //$NON-NLS-1$
		}
	}

	private class ParametersCellModifier implements ICellModifier {
		public boolean canModify(Object element, String property) {
			Assert.isTrue(element instanceof ParameterInfo);
			if (property.equals(PROPERTIES[TYPE_PROP]))
				return ChangeParametersControl.this.fCanChangeTypesOfOldParameters;
			else if (property.equals(PROPERTIES[NEWNAME_PROP]))
				return ChangeParametersControl.this.fCanChangeParameterNames;
			else if (property.equals(PROPERTIES[DEFAULT_PROP]))
				return (((ParameterInfo)element).isAdded());
			Assert.isTrue(false);
			return false;
		}
		public Object getValue(Object element, String property) {
			Assert.isTrue(element instanceof ParameterInfo);
			if (property.equals(PROPERTIES[TYPE_PROP]))
				return ((ParameterInfo) element).getNewTypeName();
			else if (property.equals(PROPERTIES[NEWNAME_PROP]))
				return ((ParameterInfo) element).getNewName();
			else if (property.equals(PROPERTIES[DEFAULT_PROP]))
				return ((ParameterInfo) element).getDefaultValue();
			Assert.isTrue(false);
			return null;
		}
		public void modify(Object element, String property, Object value) {
			if (element instanceof TableItem)
				element= ((TableItem) element).getData();
			if (!(element instanceof ParameterInfo))
				return;
			ParameterInfo parameterInfo= (ParameterInfo) element;
			if (property.equals(PROPERTIES[NEWNAME_PROP])) 
				parameterInfo.setNewName((String) value);
			else if (property.equals(PROPERTIES[DEFAULT_PROP]))
				parameterInfo.setDefaultValue((String) value);
			else if (property.equals(PROPERTIES[TYPE_PROP]))
				parameterInfo.setNewTypeName((String) value);
 			else 
 				Assert.isTrue(false);
			ChangeParametersControl.this.fListener.parameterChanged(parameterInfo);
			ChangeParametersControl.this.fTableViewer.update(parameterInfo, new String[] { property });
		}
	}

	private static final String[] PROPERTIES= { "message", "type", "new", "default" }; //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final int MESSAGE_PROP= 0;
	private static final int TYPE_PROP= 1;
	private static final int NEWNAME_PROP= 2;
	private static final int DEFAULT_PROP= 3;

	// configuration parameters
	private boolean fCanChangeParameterNames = true;
	private boolean fCanChangeTypesOfOldParameters = true;
	private boolean fCanAddParameters = true;
	private boolean fCanMoveParameters = true;
	private boolean defaultValueForNewParameters = false;
	private int tableStyle = SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION;
	
	private ParameterListChangeListener fListener;

	private TableViewer fTableViewer;
	
	// the label text above the table
	private String label;
	
	// the buttons
	private Button fUpButton;
	private Button fDownButton;
	// private Button fEditButton;
	private Button fAddButton;
	private Button fRemoveButton;
	
	private List fParameterInfos;
	private IIpsProject ipsProject;

	/**
	 * @param label the label before the table or <code>null</code>
	 * @param typeContext the package in which to complete types
	 */
	public ChangeParametersControl(
	        Composite parent, 
	        int style, 
	        String label, 
	        IIpsProject pdProject) {
	    
		super(parent, style);
		this.ipsProject= pdProject;
		this.label = label;
	}
	
	public void setCanChangeParameterNames(boolean value) {
	    fCanChangeParameterNames = value;
	}
	
	public void setCanChangeParameterTypes(boolean value) {
	    fCanChangeTypesOfOldParameters = value;
	}
	
	public void setCanAddParameters(boolean value) {
	    fCanAddParameters = value;
	}
	
	public void setCanMoveParameters(boolean value) {
	    fCanMoveParameters = value;
	}
	
	public void setDefaultValueForNewParameters(boolean value) {
	    defaultValueForNewParameters = value;
	}
	
	public void setTableStyle(int style) {
	    tableStyle = style;
	}
	
    /**
     * Creates the compoiste's controls. This method has to be called by this
     * controls client, after the control has been configured via the appropiate
     * setter method, e.g. <code>setNumOfRowsHint(int rows)</code>
     */
	public void initControl() {
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		setLayout(layout);

		if (label != null) {
			Label tableLabel = new Label(this, SWT.NONE);
			GridData labelGd= new GridData();
			labelGd.horizontalSpan= 2;
			tableLabel.setLayoutData(labelGd);
			tableLabel.setText(label);
		}
		createParameterList(this);
		createButtonComposite(this);
	}
	
	private MessageList validate(ParameterInfo info) throws CoreException {
	    int index = fParameterInfos.indexOf(info);
	    return validate(index);
	}
	
	protected abstract MessageList validate(int paramIndex)
		throws CoreException;
	
	public void setParameterListChangeListener(ParameterListChangeListener listener) {
	    this.fListener = listener;
	}
 
	public void setInput(List parameterInfos) {
		ArgumentCheck.notNull(parameterInfos);
		fParameterInfos= parameterInfos;
		fTableViewer.setInput(fParameterInfos);
		if (fParameterInfos.size() > 0)
			fTableViewer.setSelection(new StructuredSelection(fParameterInfos.get(0)));
	}
	
	public List getInput() {
	    return fParameterInfos;
	}
	
	public void setEnabled(boolean enabled) {
		this.fTableViewer.getControl().setEnabled(enabled);
	}
	
	// ---- Parameter table -----------------------------------------------------------------------------------

	private void createParameterList(Composite parent) {
		TableLayoutComposite layouter= new TableLayoutComposite(parent, SWT.NONE);
		addColumnLayoutData(layouter);
		
		final Table table= new Table(layouter, tableStyle);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableColumn tc;
		tc= new TableColumn(table, SWT.NONE, MESSAGE_PROP);
		tc.setAlignment(SWT.LEFT);
		tc.setResizable(false);
		
		tc= new TableColumn(table, SWT.NONE, TYPE_PROP);
		tc.setResizable(true);
		tc.setText("Datatype"); //$NON-NLS-1$
		
		tc= new TableColumn(table, SWT.NONE, NEWNAME_PROP);
		tc.setResizable(true);
		tc.setText("Name"); //$NON-NLS-1$

		if (fCanAddParameters && defaultValueForNewParameters){
			tc= new TableColumn(table, SWT.NONE, DEFAULT_PROP);
			tc.setResizable(true);
			tc.setText("Default Value"); //$NON-NLS-1$
		}	
		
		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.widthHint= 40;
		layouter.setLayoutData(gd);

		fTableViewer= new TableViewer(table);
		fTableViewer.setUseHashlookup(true);
		fTableViewer.setContentProvider(new ParameterInfoContentProvider());
		fTableViewer.setLabelProvider(new ParameterInfoLabelProvider());
		new TableMessageHoverService(fTableViewer) {

            protected MessageList getMessagesFor(Object element) throws CoreException {
                return validate((ParameterInfo)element);
            }
		};
		
		fTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtonsEnabledState();
			}
		});

		table.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN && e.stateMask == SWT.NONE) {
					editColumnOrNextPossible(0);
					e.detail= SWT.TRAVERSE_NONE;
				}
			}
		});
		table.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.F2 && e.stateMask == SWT.NONE) {
					editColumnOrNextPossible(0);
					e.doit= false;
				}
			}
		});

		if (canEditTableCells()){
			addCellEditors();
		}	
	}

    private boolean canEditTableCells() {
        return fCanChangeParameterNames || fCanChangeTypesOfOldParameters;
    }
	
	private void editColumnOrNextPossible(int column){
		ParameterInfo[]	selected= getSelectedElements();
		if (selected.length != 1)
			return;
		int nextColumn= column;
		do {
			fTableViewer.editElement(selected[0], nextColumn);
			if (fTableViewer.isCellEditorActive())
				return;
			nextColumn= nextColumn(nextColumn);
		} while (nextColumn != column);
	}
	
	private void editColumnOrPrevPossible(int column){
		ParameterInfo[]	selected= getSelectedElements();
		if (selected.length != 1)
			return;
		int prevColumn= column;
		do {
			fTableViewer.editElement(selected[0], prevColumn);
			if (fTableViewer.isCellEditorActive())
			    return;
			prevColumn= prevColumn(prevColumn);
		} while (prevColumn != column);
	}
	
	private int nextColumn(int column) {
		return (column >= getTable().getColumnCount() - 1) ? 0 : column + 1;
	}
	
	private int prevColumn(int column) {
		return (column <= 0) ? getTable().getColumnCount() - 1 : column - 1;
	}
	
	private void addColumnLayoutData(TableLayoutComposite layouter) {
		if (fCanAddParameters && defaultValueForNewParameters){
			layouter.addColumnData(new ColumnPixelData(20, false));
			layouter.addColumnData(new ColumnWeightData(60, true));
			layouter.addColumnData(new ColumnWeightData(20, true));
			layouter.addColumnData(new ColumnWeightData(20, true));
		} else {
			layouter.addColumnData(new ColumnPixelData(20, false));
			layouter.addColumnData(new ColumnWeightData(70, true));
			layouter.addColumnData(new ColumnWeightData(30, true));
		}	
	}

	private ParameterInfo[] getSelectedElements() {
		ISelection selection= fTableViewer.getSelection();
		if (selection == null)
			return new ParameterInfo[0];

		if (!(selection instanceof IStructuredSelection))
			return new ParameterInfo[0];

		List selected= ((IStructuredSelection) selection).toList();
		return (ParameterInfo[]) selected.toArray(new ParameterInfo[selected.size()]);
	}

	// ---- Button bar --------------------------------------------------------------------------------------

	private void createButtonComposite(Composite parent) {
		Composite buttonComposite= new Composite(parent, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		GridLayout gl= new GridLayout();
		gl.marginHeight= 0;
		gl.marginWidth= 0;
		buttonComposite.setLayout(gl);

		if(fCanAddParameters)
			fAddButton= createAddButton(buttonComposite);	

		if(fCanAddParameters){
			fRemoveButton= createRemoveButton(buttonComposite);
		}
		
		if (buttonComposite.getChildren().length != 0)
			addSpacer(buttonComposite);

		if (fCanMoveParameters) {
			fUpButton= createButton(buttonComposite, Messages.ChangeParametersControl_ButtonLabel_Up, true);
			fDownButton= createButton(buttonComposite, Messages.ChangeParametersControl_ButtonLabel_Down, false);
		}
		updateButtonsEnabledState();
		if (buttonComposite.getChildren().length==0) {
		    buttonComposite.dispose();
		}
	}

	private void addSpacer(Composite parent) {
		Label label= new Label(parent, SWT.NONE);
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint= 5;
		label.setLayoutData(gd);
	}

	private void updateButtonsEnabledState() {
	    if (fUpButton!=null) {
			fUpButton.setEnabled(canMove(true));
	    }
	    if (fDownButton!=null) {
			fDownButton.setEnabled(canMove(false));
	    }
		if (fAddButton != null)
			fAddButton.setEnabled(true);	
		if (fRemoveButton != null)
			fRemoveButton.setEnabled(getTableSelectionCount() != 0);
	}

	private int getTableSelectionCount() {
		return getTable().getSelectionCount();
	}

	private int getTableItemCount() {
		return getTable().getItemCount();
	}

	public Table getTable() {
		return fTableViewer.getTable();
	}
	
	public TableViewer getTableViewer() {
	    return fTableViewer;
	}
	
	private int getTableSelectionIndex() {
	    return getTable().getSelectionIndex();
	}
	
	private Button createAddButton(Composite buttonComposite) {
		Button button= new Button(buttonComposite, SWT.PUSH);
		button.setText(Messages.ChangeParametersControl_ButtonLabel_Add);
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ParameterInfo newInfo= ParameterInfo.createInfoForAddedParameter();
				fParameterInfos.add(newInfo);
				fListener.parameterAdded(newInfo);
				fTableViewer.refresh();
				fTableViewer.getControl().setFocus();
				int row= getTableItemCount() - 1;
				getTable().setSelection(row);
				updateButtonsEnabledState();
				editColumnOrNextPossible(0);
			}
		});	
		return button;
	}

	private Button createRemoveButton(Composite buttonComposite) {
		final Button button= new Button(buttonComposite, SWT.PUSH);
		button.setText(Messages.ChangeParametersControl_ButtonLabel_Remove);
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index= getTable().getSelectionIndices()[0];
				ParameterInfo[] selected= getSelectedElements();
				for (int i= 0; i < selected.length; i++) {
					if (selected[i].isAdded())
						fParameterInfos.remove(selected[i]);
					else
						selected[i].markAsDeleted();	
				}
				restoreSelection(index);
			}
			private void restoreSelection(int index) {
				fTableViewer.refresh();
				fTableViewer.getControl().setFocus();
				int itemCount= getTableItemCount();
				if (itemCount != 0 && index >= itemCount) {
					index= itemCount - 1;
					getTable().setSelection(index);
				}
				fListener.parameterListChanged();
				updateButtonsEnabledState();
			}
		});	
		return button;
	}

	private Button createButton(Composite buttonComposite, String text, final boolean up) {
		Button button= new Button(buttonComposite, SWT.PUSH);
		button.setText(text);
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ISelection savedSelection= fTableViewer.getSelection();
				if (savedSelection == null)
					return;
				ParameterInfo[] selection= getSelectedElements();
				if (selection.length == 0)
					return;
					
				if (up) {
					moveUp(selection);
				} else {
					moveDown(selection);
				}
				fTableViewer.refresh();
				fTableViewer.setSelection(savedSelection);
				fListener.parameterListChanged();
				fTableViewer.getControl().setFocus();
			}
		});
		return button;
	}
	
	//---- editing -----------------------------------------------------------------------------------------------

	private void addCellEditors() {
		class UnfocusableTextCellEditor extends TextCellEditor {
			private Object fOriginalValue;
			SubjectControlContentAssistant fContentAssistant;
			private boolean fSaveNextModification;
			public UnfocusableTextCellEditor(Composite parent) {
				super(parent);
			}
			public void activate() {
				super.activate();
				fOriginalValue= doGetValue();
			}
			public Object getOriginalValue() {
				return fOriginalValue;
			}
			public void fireModifyEvent(Object newValue, final int property) {
				fTableViewer.getCellModifier().modify(
						((IStructuredSelection) fTableViewer.getSelection()).getFirstElement(),
						PROPERTIES[property], newValue);
			}
			protected void focusLost() {
				if (fContentAssistant != null && fContentAssistant.hasProposalPopupFocus())
					fSaveNextModification= true;
				else
					super.focusLost();
			}
			
			public void setContentAssistant(SubjectControlContentAssistant assistant, final int property) {
				fContentAssistant= assistant;
				//workaround for bugs 53629, 58777:
				text.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						if (fSaveNextModification) {
							fSaveNextModification= false;
							final String newValue= text.getText();
							fTableViewer.getCellModifier().modify(
									((IStructuredSelection) fTableViewer.getSelection()).getFirstElement(),
									PROPERTIES[property], newValue);
							editColumnOrNextPossible(property);
						}
					}
				});
			}
		}
		
		final UnfocusableTextCellEditor editors[]= new UnfocusableTextCellEditor[PROPERTIES.length];

		editors[TYPE_PROP]= new UnfocusableTextCellEditor(getTable());
		editors[NEWNAME_PROP]= new UnfocusableTextCellEditor(getTable());
		editors[DEFAULT_PROP]= new UnfocusableTextCellEditor(getTable());
		
		SubjectControlContentAssistant assistant= installParameterTypeContentAssist(editors[TYPE_PROP].getControl());
		editors[TYPE_PROP].setContentAssistant(assistant, TYPE_PROP);
		
		for (int i = 1; i < editors.length; i++) {
			final int editorColumn= i;
			final UnfocusableTextCellEditor editor = editors[i];
			// support tabbing between columns while editing:
			editor.getControl().addTraverseListener(new TraverseListener() {
				public void keyTraversed(TraverseEvent e) {
					switch (e.detail) {
						case SWT.TRAVERSE_TAB_NEXT :
							editColumnOrNextPossible(nextColumn(editorColumn));
							e.detail= SWT.TRAVERSE_NONE;
							break;

						case SWT.TRAVERSE_TAB_PREVIOUS :
							editColumnOrPrevPossible(prevColumn(editorColumn));
							e.detail= SWT.TRAVERSE_NONE;
							break;
						
						case SWT.TRAVERSE_ESCAPE :
							fTableViewer.cancelEditing();
							e.detail= SWT.TRAVERSE_NONE;
							break;
						
						case SWT.TRAVERSE_RETURN :
							editor.deactivate();
							e.detail= SWT.TRAVERSE_NONE;
							break;
							
						default :
							break;
					}
				}
			});
			// support switching rows while editing:
			editor.getControl().addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if (e.stateMask == SWT.MOD1 || e.stateMask == SWT.MOD2) {
						if (e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_DOWN) {
						    // allow starting multi-selection even if in edit mode
							editor.deactivate();
							e.doit= false;
							return;
						}
					}
					
					if (e.stateMask != SWT.NONE)
						return;
					
					switch (e.keyCode) {
					case SWT.ARROW_DOWN :
						e.doit= false;
						int nextRow= getTableSelectionIndex() + 1;
						if (nextRow >= getTableItemCount())
							break;
						getTable().setSelection(nextRow);
						editColumnOrPrevPossible(editorColumn);
						break;
						
					case SWT.ARROW_UP :
						e.doit= false;
						int prevRow= getTableSelectionIndex() - 1;
						if (prevRow < 0)
							break;
						getTable().setSelection(prevRow);
						editColumnOrPrevPossible(editorColumn);
						break;
						
					case SWT.F2 :
						e.doit= false;
						editor.deactivate();
						break;
					}
				}
			});
			
			editor.addListener(new ICellEditorListener() {
				/* bug 58540: change signature refactoring interaction: validate as you type [refactoring] 
				 * CellEditors validate on keystroke by updating model on editorValueChanged(..) */
				public void applyEditorValue() {
					//default behavior is OK
				}
				public void cancelEditor() {
					//must reset model to original value:
					editor.fireModifyEvent(editor.getOriginalValue(), editorColumn);
				}
				public void editorValueChanged(boolean oldValidState, boolean newValidState) {
					editor.fireModifyEvent(editor.getValue(), editorColumn);
				}
			});

		}
		
		fTableViewer.setCellEditors(editors);
		fTableViewer.setColumnProperties(PROPERTIES);
		fTableViewer.setCellModifier(new ParametersCellModifier());
	}

	private SubjectControlContentAssistant installParameterTypeContentAssist(Control control) {
		if (! (control instanceof Text))
			return null;
		Text text= (Text) control;
		DatatypeCompletionProcessor processor= new DatatypeCompletionProcessor();
		processor.setIpsProject(ipsProject);
		SubjectControlContentAssistant contentAssistant= CompletionUtil.createContentAssistant(processor);
		
		ContentAssistHandler.createHandlerForText(text, contentAssistant);
		return contentAssistant;
	}

	//---- change order ----------------------------------------------------------------------------------------

	private void moveUp(ParameterInfo[] selection) {
		moveUp(fParameterInfos, Arrays.asList(selection));
	}

	private void moveDown(ParameterInfo[] selection) {
		Collections.reverse(fParameterInfos);
		moveUp(fParameterInfos, Arrays.asList(selection));
		Collections.reverse(fParameterInfos);
	}

	private static void moveUp(List elements, List move) {
		List res= new ArrayList(elements.size());
		List deleted= new ArrayList();
		Object floating= null;
		for (Iterator iter= elements.iterator(); iter.hasNext();) {
			Object curr= iter.next();
			if (move.contains(curr)) {
				res.add(curr);
			} else if (((ParameterInfo) curr).isDeleted()) {
				deleted.add(curr);
			} else {
				if (floating != null)
					res.add(floating);
				floating= curr;
			}
		}
		if (floating != null) {
			res.add(floating);
		}
		res.addAll(deleted);
		elements.clear();
		for (Iterator iter= res.iterator(); iter.hasNext();) {
			elements.add(iter.next());
		}
	}

	private boolean canMove(boolean up) {
		List notDeleted= getNotDeletedInfos();
		if (notDeleted == null || notDeleted.size() == 0)
			return false;
		int[] indc= getTable().getSelectionIndices();
		if (indc.length == 0)
			return false;
		int invalid= up ? 0 : notDeleted.size() - 1;
		for (int i= 0; i < indc.length; i++) {
			if (indc[i] == invalid)
				return false;
		}
		return true;
	}
	
	private List getNotDeletedInfos(){
		if (fParameterInfos == null)
			return null;
		List result= new ArrayList(fParameterInfos.size());
		for (Iterator iter= fParameterInfos.iterator(); iter.hasNext();) {
			ParameterInfo info= (ParameterInfo) iter.next();
			if (! info.isDeleted())
				result.add(info);
		}
		return result;
	}
}
