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

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;


/**
 * 
 */
public abstract class TableMessageHoverService {
    
    private Hover hover;
    private MouseTrackListener mouseTrackListener; 
    private final StructuredViewer viewer;
    private final Control viewerControl;

    public TableMessageHoverService(StructuredViewer viewer) {
        this.viewer = viewer;
        viewerControl = viewer.getControl();
        mouseTrackListener = new TableMouseTrackListener();
        viewerControl.addMouseTrackListener(mouseTrackListener);
        viewerControl.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                dispose();
            }
            
        });
    }
    
    /**
     * Disposes the service ( and the hover if it is currently showing).
     */
    public void dispose() {
        if (hover!=null) {
            hover.setVisible(false);
            hover.dispose();
            hover = null;
        }
        if (mouseTrackListener!=null && viewerControl!=null && !viewerControl.isDisposed()) {
            viewerControl.removeMouseTrackListener(mouseTrackListener);
            mouseTrackListener = null;
        }
    }
    
    /**
     * Returns the messages that for the given element.
     *  
     * @throws CoreException
     */
    protected abstract MessageList getMessagesFor(Object element) throws CoreException;
    
    class TableMouseTrackListener implements MouseTrackListener {
        public void mouseEnter(MouseEvent e) {
        }

        public void mouseExit(MouseEvent e) {
            if (hover!=null) {
                hover.setVisible(false);
                hover.dispose();
                hover = null;
            }
        }

        public void mouseHover(MouseEvent e) {
        	Object element = null;
        	Rectangle itemBounds = null;
        	if (viewerControl instanceof Table) {
        		Table table = (Table) viewerControl;
        		TableItem item = table.getItem(new Point(e.x, e.y));
        		if (item==null) {
        			hideHover();
        			return;
        		}
        		TableViewer tableViewer = (TableViewer)viewer;
        		element = tableViewer.getElementAt(table.indexOf(item));
        		itemBounds = item.getBounds(0);
        	} else if (viewerControl instanceof Tree) {
        		Tree tree = (Tree)viewerControl;
        		TreeItem item = tree.getItem(new Point(e.x, e.y));
        		if (item == null) {
        			hideHover();
        			return;
        		}
        		element = item.getData();
        		itemBounds = item.getBounds();
        	} else {
        		hideHover();
        		return;
        	}
        	
            MessageList list;
            try {
                list = getMessagesFor(element);
            } catch (CoreException coreE) {
                IpsPlugin.log(coreE);
                list = new MessageList();
            }
            if (list.getSeverity()==Message.NONE) {
                hideHover();
                return;
            }
            showHover(itemBounds, list.getText());
        }
        
        private void showHover(Rectangle itemBounds, String text) {
            if (hover==null) {
                hover = new Hover(viewerControl.getShell());
            }
            Point hoverPos = viewerControl.toDisplay(itemBounds.x, itemBounds.y);
            hover.setText(text);
            hover.setLocation(hoverPos);
            hover.setVisible(true);
        }
        
        private void hideHover() {
            if (hover!=null) {
                hover.setVisible(false);
                hover.dispose();
                hover = null;
            }
        }

    }
    
    /**
	 * An info Hover to display a message at a given display relative
	 * position.
	 */
	class Hover {
		/**
		 * Distance of info hover arrow from left side.
		 */
		private int HD= 10;
		/**
		 * Width of info hover arrow.
		 */
		private int HW= 8;
		/**
		 * Height of info hover arrow.
		 */
		private int HH= 10;
		/**
		 * Margin around info hover text.
		 */
		private int LABEL_MARGIN= 2;
		/**
		 * This info hover's shell.
		 */
		Shell fHoverShell;
		
		/**
		 * The info hover text.
		 */
		String fText= ""; //$NON-NLS-1$
		
		Hover(final Shell shell) {
			final Display display= shell.getDisplay();
			fHoverShell= new Shell(shell, SWT.NO_TRIM | SWT.ON_TOP | SWT.NO_FOCUS);
			fHoverShell.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
			fHoverShell.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			fHoverShell.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent pe) {
					pe.gc.drawText(fText, LABEL_MARGIN, LABEL_MARGIN);
					
					//if (!fgCarbon)
						pe.gc.drawPolygon(getPolygon(true));
				}
			});
		}
		
		int[] getPolygon(boolean border) {
			Point e= getExtent();
			if (border) {
				return new int[] { 0,0, e.x-1,0, e.x-1,e.y-1, HD+HW,e.y-1, HD+HW/2,e.y+HH-1, HD,e.y-1, 0,e.y-1, 0,0 };
			} else {
				return new int[] { 0,0, e.x,  0, e.x,  e.y,   HD+HW,e.y,   HD+HW/2,e.y+HH,   HD,e.y,   0,e.y,   0,0 };
			}
		}
		
		void dispose() {
			if (!fHoverShell.isDisposed())
				fHoverShell.dispose();
		}
		
		void setVisible(boolean visible) {
			if (visible) {
				if (!fHoverShell.isVisible())
					fHoverShell.setVisible(true);
			} else {
				if (fHoverShell.isVisible())
					fHoverShell.setVisible(false);
			}
		}
			
		void setText(String t) {
			if (t == null)
				t= ""; //$NON-NLS-1$
			if (! t.equals(fText)) {
				Point oldSize= getExtent();
				fText= t;
				fHoverShell.redraw();
				Point newSize= getExtent();
				if (!oldSize.equals(newSize)) {
					Region region= new Region();
					region.add(getPolygon(false));
					fHoverShell.setRegion(region);
				}		
			}
		}

		boolean isVisible() {
			return fHoverShell.isVisible();
		}
		
		void setLocation(Point position) {
			int height= getExtent().y;
			fHoverShell.setLocation(position.x + (HD+HW/2), position.y - height - 5);
		}
		
		Point getExtent() {
			GC gc= new GC(fHoverShell);
			Point e= gc.textExtent(fText, SWT.DRAW_DELIMITER | SWT.DRAW_TAB);
			gc.dispose();
			e.x+= LABEL_MARGIN*2;
			e.y+= LABEL_MARGIN*2;
			return e;
		}
	}

    
}
