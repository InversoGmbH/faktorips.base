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

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.util.ListElementMover;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Element;

/**
 * A collection of ips object parts. This class is used together with BaseIpsObject and
 * BaseIpsObjectPart to ease the development of new ips object subclasses. An ips object part
 * collection is a collection of parts of the same type. E.g. a collection holds only methods or
 * only attributes but not both. As opposed to an IpsObjectPartContainer which is a container for
 * ips object parts of any kind.
 * 
 * @see IpsObjectPartContainer
 * @see BaseIpsObject
 * @see BaseIpsObjectPart
 * 
 * @since 2.0
 * 
 * @author Jan Ortmann
 */
public class IpsObjectPartCollection {

    private IpsObjectPartContainer parent;
    private String xmlTag;
    private Class partsBaseClass;
    private Class partsPublishedInterface;
    private Constructor constructor;

    private List parts = new ArrayList();

    public IpsObjectPartCollection(BaseIpsObject ipsObject, Class partsClazz, Class publishedInterface, String xmlTag) {
        this(partsClazz, publishedInterface, xmlTag);
        ArgumentCheck.notNull(ipsObject);
        this.parent = ipsObject;
        ipsObject.addPartCollection(this);
    }

    public IpsObjectPartCollection(BaseIpsObjectPart ipsObjectPart, Class partsClazz, Class publishedInterface,
            String xmlTag) {
        this(partsClazz, publishedInterface, xmlTag);
        ArgumentCheck.notNull(ipsObjectPart);
        this.parent = ipsObjectPart;
        ipsObjectPart.addPartCollection(this);
    }

    private IpsObjectPartCollection(Class partsClazz, Class publishedInterface, String xmlTag) {
        ArgumentCheck.notNull(partsClazz);
        ArgumentCheck.notNull(publishedInterface);
        ArgumentCheck.notNull(xmlTag);
        this.partsBaseClass = partsClazz;
        this.partsPublishedInterface = publishedInterface;
        this.xmlTag = xmlTag;
        constructor = getConstructor();
    }

    private Constructor getConstructor() {
        Constructor[] constructors = partsBaseClass.getConstructors();
        for (int i = 0; i < constructors.length; i++) {
            Class[] params = constructors[i].getParameterTypes();
            if (params.length != 2) {
                continue;
            }
            if (params[1].equals(Integer.TYPE)) {
                if (IIpsObjectPartContainer.class.isAssignableFrom(params[0])) {
                    return constructors[i];
                }
                if (IIpsObject.class.isAssignableFrom(params[0])) {
                    return constructors[i];
                }
            }
        }
        throw new RuntimeException(this + ", Part class hasn't got an appropriate constructor."); //$NON-NLS-1$
    }

    public void clear() {
        parts.clear();
    }

    public int size() {
        return parts.size();
    }

    public IIpsObjectPart getPart(int index) {
        return (IIpsObjectPart)parts.get(index);
    }

    public Iterator iterator() {
        return parts.iterator();
    }

    public Object[] toArray(Object[] emptyArray) {
        return parts.toArray(emptyArray);
    }

    /**
     * Returns the underlying list that stores the parts.
     */
    public List getBackingList() {
        return parts;
    }

    public IIpsObjectPart[] getParts() {
        return (IIpsObjectPart[])parts.toArray(new IIpsObjectPart[parts.size()]);
    }

    /**
     * Returns the part with the given name contained in this collection. If more than one part with
     * the name exist, the first part with the name is returned. Returns <code>null</code> if no
     * part with the given name exists or name is <code>null</code>.
     */
    public IIpsObjectPart getPartByName(String name) {
        if (name == null) {
            return null;
        }
        for (Iterator it = parts.iterator(); it.hasNext();) {
            IIpsObjectPart part = (IIpsObjectPart)it.next();
            if (name.equals(part.getName())) {
                return part;
            }
        }
        return null;

    }

    /**
     * Returns the part with the given id contained in this collection. Returns <code>null</code> if
     * no part with the given id exists.
     */
    public IIpsObjectPart getPartById(int id) {
        for (Iterator it = parts.iterator(); it.hasNext();) {
            IIpsObjectPart part = (IIpsObjectPart)it.next();
            if (id == part.getId()) {
                return part;
            }
        }
        return null;
    }

    /**
     * This method creates a new <code>IpsObjectPart</code> according to the configuration of this
     * object. The provided initializer can set the new <code>IpsObjectPart</code> into a valid
     * state before a <code>ContentChangeEvent.TYPE_PART_ADDED</code> is fired. This method is not
     * part of the published interface. Subclasses that want to provide a factory method for a
     * specific <code>IpsObjectPart</code> can use this method and add their method to the published
     * interface if desired.
     * 
     * @return the new IpsPart
     */
    protected IpsObjectPart newPart(IpsObjectPartInitializer initializer) {
        IpsObjectPart part = newPartInternal(parent.getNextPartId());
        initializer.initialize(part);
        parent.partWasAdded(part);
        return part;
    }

    /**
     * {@inheritDoc}
     */
    public IpsObjectPart newPart() {
        IpsObjectPart newPart = newPartInternal(parent.getNextPartId());
        parent.partWasAdded(newPart);
        return newPart;
    }

    public IpsObjectPart newPart(Element el, int id) {
        if (xmlTag.equals(el.getNodeName())) {
            return newPartInternal(id);
        }
        return null;
    }

    public IpsObjectPart newPart(Class clazz) {
        if (partsPublishedInterface.isAssignableFrom(clazz)) {
            return newPart();
        }
        return null;
    }

    /**
     * Returns <code>true</code> if the part was contained in this collection (before this call) and
     * was removed. Returns <code>false</code> otherwise.
     */
    public boolean readdPart(IIpsObjectPart part) {
        if (this.partsBaseClass.isAssignableFrom(part.getClass())) {
            parts.add(part);
            return true;
        }
        return false;
    }

    public boolean removePart(IIpsObjectPart part) {
        return parts.remove(part);
    }

    /**
     * Creates a new part without updating the src file. Subclasses have to instantiate a new object
     * of the concrete subclass of IpsObjectPart.
     */
    private IpsObjectPart newPartInternal(int id) {
        try {
            IpsObjectPart newPart = (IpsObjectPart)constructor.newInstance(new Object[] { parent, new Integer(id) });
            parts.add(newPart);
            return newPart;
        } catch (Exception e) {
            throw new RuntimeException(this + ", Error creating new instance via constructor " + constructor, e); //$NON-NLS-1$
        }
    }

    /**
     * Moves the parts indicated by the given indexes one position up or down in their containing
     * list.
     * 
     * @param indexes The indexes that specify the elements to move.
     * @param up Flag indicating whether to move the elements one position up (<code>true</code>) or
     *            down (<code>false</code>).
     * 
     * @return The new indexes of the moved elements.
     */
    public int[] moveParts(int[] indexes, boolean up) {
        ListElementMover mover = new ListElementMover(parts);
        int[] newIndexes = mover.move(indexes, up);
        parent.partsMoved(getParts());
        return newIndexes;
    }

    public String toString() {
        return "Part collection for " + partsBaseClass.getName(); //$NON-NLS-1$
    }

    /**
     * An implementations of this interface is required by the newPart(IIpsObjectPartCollection)
     * method. It is supposed to be used for additional initialization of a new IpsObjectPart before
     * its creation and addition to the according IpsObject is communicated to
     * ContentChangeListeners.
     * 
     * @author Peter Erzberger
     */
    public interface IpsObjectPartInitializer {

        /**
         * Initializes the provided IpsObjectPart.
         */
        public void initialize(IpsObjectPart part);
    }
}
