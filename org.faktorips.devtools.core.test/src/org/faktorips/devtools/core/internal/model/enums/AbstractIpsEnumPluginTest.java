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

package org.faktorips.devtools.core.internal.model.enums;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.w3c.dom.Document;

/**
 * Base test for all enum tests providing a simple enum model with a gender enum.
 * <p>
 * There is a gender enum type, the values are stored separated from the enum type in a gender enum
 * content object.
 * <p>
 * Utility methods and helpful string constants are provided.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public abstract class AbstractIpsEnumPluginTest extends AbstractIpsPluginTest {

    protected final String STRING_DATATYPE_NAME = "String";
    protected final String INTEGER_DATATYPE_NAME = "Integer";
    protected final String BOOLEAN_DATATYPE_NAME = "Boolean";

    protected final String GENDER_ENUM_TYPE_NAME = "GenderEnumType";
    protected final String GENDER_ENUM_ATTRIBUTE_ID_NAME = "Id";
    protected final String GENDER_ENUM_ATTRIBUTE_NAME_NAME = "Name";
    protected final String GENDER_ENUM_CONTENT_NAME = "GenderEnumContent";

    protected final String GENDER_ENUM_LITERAL_MALE_ID = "m";
    protected final String GENDER_ENUM_LITERAL_FEMALE_ID = "w";
    protected final String GENDER_ENUM_LITERAL_MALE_NAME = "male";
    protected final String GENDER_ENUM_LITERAL_FEMALE_NAME = "female";

    protected final String DEFAULT_PACKAGE_FRAGMENT = "";

    protected IIpsProject ipsProject;

    protected IEnumType genderEnumType;
    protected IEnumAttribute genderEnumAttributeId;
    protected IEnumAttribute genderEnumAttributeName;
    protected IEnumContent genderEnumContent;
    protected IEnumValue genderEnumValueMale;
    protected IEnumValue genderEnumValueFemale;

    protected IEnumType paymentMode;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject("EnumTestProject");

        createGenderEnum();
        initGenderEnumValues();

        createPaymentModeEnum();
    }

    private void createPaymentModeEnum() throws Exception {
        paymentMode = newEnumType(ipsProject, "PaymentMode");
        paymentMode.setAbstract(false);
        paymentMode.setValuesArePartOfModel(true);

        IEnumAttribute id = paymentMode.newEnumAttribute();
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setInherited(false);
        id.setLiteralNameAttribute(true);
        id.setUniqueIdentifier(true);
        id.setName("id");
        IEnumValue value1 = paymentMode.newEnumValue();
        IEnumAttributeValue value1id = value1.getEnumAttributeValues().get(0);
        value1id.setValue("monthly");
        IEnumValue value2 = paymentMode.newEnumValue();
        IEnumAttributeValue value2id = value2.getEnumAttributeValues().get(0);
        value2id.setValue("annually");
    }

    private void createGenderEnum() throws CoreException {
        genderEnumType = newEnumType(ipsProject, GENDER_ENUM_TYPE_NAME);
        genderEnumType.setAbstract(false);
        genderEnumType.setValuesArePartOfModel(false);
        genderEnumType.setSuperEnumType("");

        genderEnumAttributeId = genderEnumType.newEnumAttribute();
        genderEnumAttributeId.setName(GENDER_ENUM_ATTRIBUTE_ID_NAME);
        genderEnumAttributeId.setDatatype(STRING_DATATYPE_NAME);
        genderEnumAttributeId.setLiteralNameAttribute(true);
        genderEnumAttributeId.setUniqueIdentifier(true);
        genderEnumAttributeName = genderEnumType.newEnumAttribute();
        genderEnumAttributeName.setName(GENDER_ENUM_ATTRIBUTE_NAME_NAME);
        genderEnumAttributeName.setDatatype(STRING_DATATYPE_NAME);

        genderEnumContent = newEnumContent(ipsProject, GENDER_ENUM_CONTENT_NAME);
        genderEnumContent.setEnumType(genderEnumType.getQualifiedName());

        genderEnumValueMale = genderEnumContent.newEnumValue();
        genderEnumValueFemale = genderEnumContent.newEnumValue();

        genderEnumType.setEnumContentPackageFragment(DEFAULT_PACKAGE_FRAGMENT);
    }

    private void initGenderEnumValues() {
        IEnumAttributeValue tempAttributeValueRef;

        tempAttributeValueRef = genderEnumValueMale.getEnumAttributeValues().get(0);
        tempAttributeValueRef.setValue(GENDER_ENUM_LITERAL_MALE_ID);
        tempAttributeValueRef = genderEnumValueMale.getEnumAttributeValues().get(1);
        tempAttributeValueRef.setValue(GENDER_ENUM_LITERAL_MALE_NAME);

        tempAttributeValueRef = genderEnumValueFemale.getEnumAttributeValues().get(0);
        tempAttributeValueRef.setValue(GENDER_ENUM_LITERAL_FEMALE_ID);
        tempAttributeValueRef = genderEnumValueFemale.getEnumAttributeValues().get(1);
        tempAttributeValueRef.setValue(GENDER_ENUM_LITERAL_FEMALE_NAME);
    }

    protected Document createXmlDocument(String xmlTag) throws ParserConfigurationException {
        Document xmlDocument = getDocumentBuilder().newDocument();
        xmlDocument.appendChild(xmlDocument.createElement(xmlTag));
        return xmlDocument;
    }
}
