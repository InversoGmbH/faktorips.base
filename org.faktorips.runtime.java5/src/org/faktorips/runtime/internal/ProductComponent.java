/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.internal;

import java.util.Calendar;

import org.faktorips.runtime.IClRepositoryObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.w3c.dom.Element;

/**
 * Base class for all product components.
 */
public abstract class ProductComponent extends RuntimeObject implements IProductComponent, IClRepositoryObject {

    // the component's id that identifies it in the repository
    private String id;

    // The repository the component uses to resolve references to other components.
    private transient IRuntimeRepository repository;

    // the component's kindId
    private String productKindId;

    // the component's versionId
    private String versionId;

    // the date at which this product component expires. Set to null indicates no
    // limitation
    private DateTime validTo;

    /**
     * Creates a new product component with the indicate id, kind id and version id.
     * 
     * @param repository The component registry the component uses to resolve references to other
     *            components.
     * @param id The component's runtime id.
     * @param productKindId The component's kind id
     * @param versionId The component's version id
     * 
     * @throws NullPointerException if repository, id, productKindId, or versionId is
     *             <code>null</code>.
     */
    public ProductComponent(IRuntimeRepository repository, String id, String productKindId, String versionId) {
        if (repository == null) {
            throw new NullPointerException("RuntimeRepositor was null!");
        }
        if (id == null) {
            throw new NullPointerException("Id was null!");
        }
        if (productKindId == null) {
            throw new NullPointerException("ProductKindId was null");
        }
        if (versionId == null) {
            throw new NullPointerException("VersionId was null");
        }
        this.repository = repository;
        this.id = id;
        this.productKindId = productKindId;
        this.versionId = versionId;
    }

    /**
     * {@inheritDoc}
     */
    public String getKindId() {
        return productKindId;
    }

    /**
     * {@inheritDoc}
     */
    public String getVersionId() {
        return versionId;
    }

    /**
     * {@inheritDoc}
     */
    public final String getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public DateTime getValidTo() {
        return validTo;
    }

    /**
     * @param validTo The validTo to set.
     */
    public void setValidTo(DateTime validTo) {
        this.validTo = validTo;
    }

    /**
     * {@inheritDoc}
     */
    public final IRuntimeRepository getRepository() {
        return repository;
    }

    /**
     * {@inheritDoc}
     */
    public final IProductComponentGeneration getGenerationBase(Calendar effectiveDate) {
        return repository.getProductComponentGeneration(id, effectiveDate);
    }

    /**
     * {@inheritDoc}
     */
    public IProductComponentGeneration getLatestProductComponentGeneration() {
        return getRepository().getLatestProductComponentGeneration(this);
    }

    /**
     * Initializes the generation with the data from the xml element.
     * 
     * @throws NullPointerException if cmptElement is <code>null</code>.
     */
    public final void initFromXml(Element cmptElement) {
        validTo = DateTime.parseIso(cmptElement.getAttribute("validTo"));
        initExtensionPropertiesFromXml(cmptElement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return id;
    }

}