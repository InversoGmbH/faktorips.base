/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A collection of utility methods for xml handling.
 * 
 * @author Jan Ortmann
 */
public class XmlUtil {

    public final static String dateToXmlDateString(Date date) {
        if (date == null) {
            return ""; //$NON-NLS-1$
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return gregorianCalendarToXmlDateString(calendar);
    }

    public final static String gregorianCalendarToXmlDateString(GregorianCalendar calendar) {
        if (calendar == null) {
            return ""; //$NON-NLS-1$
        }
        int month = calendar.get(GregorianCalendar.MONTH) + 1;
        int date = calendar.get(GregorianCalendar.DATE);
        return calendar.get(GregorianCalendar.YEAR) + "-" + (month < 10 ? "0" + month : "" + month) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + "-" + (date < 10 ? "0" + date : "" + date); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Parses the given xml String to a Date.
     */
    public final static Date parseXmlDateStringToDate(String s) {
        if (s == null || s.equals("")) { //$NON-NLS-1$
            return null;
        }
        return parseXmlDateStringToGregorianCalendar(s).getTime();
    }

    /**
     * Parses the given xml String to a Gregorian calendar.
     */
    public final static GregorianCalendar parseXmlDateStringToGregorianCalendar(String s) {
        if (StringUtils.isEmpty(s)) {
            return null;
        }
        try {
            StringTokenizer tokenizer = new StringTokenizer(s, "-"); //$NON-NLS-1$
            int year = Integer.parseInt(tokenizer.nextToken());
            int month = Integer.parseInt(tokenizer.nextToken());
            int date = Integer.parseInt(tokenizer.nextToken());
            return new GregorianCalendar(year, month - 1, date);
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't parse " + s + " to a date!"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Transforms the given node to a String.
     */
    public final static String nodeToString(Node node, String encoding) throws TransformerException {
        StringWriter writer = new StringWriter();
        nodeToWriter(node, writer, encoding);
        return writer.toString();
    }

    /**
     * Transforms the given node to a string and writes in to the given writer.
     * <p>
     * The encoding that is used to transforms the string into bytes is defined by the writer. E.g.
     * a <code>OutputStreamWriter</code> is created with a Charset/encoding. With a </code>StringWriter</code>
     * no encoding is neccessary.
     * <p>
     * However, to get the encoding option set in the xml header e.g. <code><?xml version="1.0"
     * encoding="Cp1252"?></code>, it is neccessary to pass the encoding to this method. Note that
     * this method does not check, if the writer's encoding and the given encoding are the same (as
     * the encoding is not available from the writer).
     */
    public final static void nodeToWriter(Node node, Writer writer, String encoding) throws TransformerException {
        // explicit use of xalan, as we need it's indentation feature
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            transformerFactory.setAttribute("indent-number", new Integer(4));
        } catch (IllegalArgumentException e) {
            // no problem, we're using a older version
        }
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
        // both settings are necessary, to accomodate versions in Java 1.4 and 1.5
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); //$NON-NLS-1$ //$NON-NLS-2$
        DOMSource source = new DOMSource(node);
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
    }

    public final static Document getDocument(InputStream is) throws SAXException, IOException,
            ParserConfigurationException {
        return getDefaultDocumentBuilder().parse(is);
    }

    public final static DocumentBuilder getDefaultDocumentBuilder() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setExpandEntityReferences(false);
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        builder.setErrorHandler(new ErrorHandler() {
            public void error(SAXParseException e) throws SAXException {
                throw e;
            }

            public void fatalError(SAXParseException e) throws SAXException {
                throw e;
            }

            public void warning(SAXParseException e) throws SAXException {
                throw e;
            }
        });
        return builder;
    }

    public final static DocumentBuilder getValidatingDocumentBuilder() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setExpandEntityReferences(false);
        factory.setValidating(true);
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        builder.setErrorHandler(new ErrorHandler() {
            public void error(SAXParseException e) throws SAXException {
                throw e;
            }

            public void fatalError(SAXParseException e) throws SAXException {
                throw e;
            }

            public void warning(SAXParseException e) throws SAXException {
                throw e;
            }
        });
        return builder;
    }

    /**
     * Writes a XML document to a file.
     * <p>
     * See also the <a
     * href='http://developers.sun.com/sw/building/codesamples/dom/doc/DOMUtil.java'>DOMUtil.java
     * example</a>.
     * </p>
     * 
     * @throws TransformerException
     */
    public static void writeXMLtoFile(File file, Document doc, String doctype, int indentWidth, String encoding)
            throws TransformerException {
        Source src = new DOMSource(doc);
        Result res = new StreamResult(file);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            transformerFactory.setAttribute("indent-number", new Integer(4));
        } catch (IllegalArgumentException e) {
            // no problem, we're using a older version
        }
        Transformer transformer = transformerFactory.newTransformer();
        transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
        transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
        if (encoding != null) {
            transformer.setOutputProperty(OutputKeys.ENCODING, encoding); //$NON-NLS-1$
        }
        if (doctype != null) {
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype);
        }
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
        if (indentWidth > 0) {
            // both settings are necessary, to accomodate versions in Java 1.4 and 1.5
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "" + indentWidth); //$NON-NLS-1$ //$NON-NLS-2$
        }
        transformer.transform(src, res);
    }

    public final static Element getFirstElement(Node parent, String tagName) {
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element element = (Element)nl.item(i);
                if (element.getNodeName().equals(tagName)) {
                    return (Element)nl.item(i);
                }
            }
        }
        return null;
    }

    /**
     * Returns the first Element node
     */
    public final static Element getFirstElement(Node parent) {
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                return (Element)nl.item(i);
            }
        }
        return null;
    }

    /**
     * Returns the child element with the given tag name and index. The index is the position of the
     * element considering all child elements with the given tag name. In contrast to
     * Element#getElementsByTagName(String tagName) this method returns only the direct children,
     * not all descendants.
     * 
     * @param parent The parent node. qparam tagName the element tag name.
     * @param index The 0 based position of the child.
     * @return The element at the specified index
     * @throws IndexOutOfBoundsException if no element exists at the specified index.
     * 
     * @see Element#getElementsByTagName(java.lang.String)
     */
    public final static Element getElement(Node parent, String tagName, int index) {
        NodeList nl = parent.getChildNodes();
        int count = 0;
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element element = (Element)nl.item(i);
                if (element.getNodeName().equals(tagName)) {
                    if (count == index) {
                        return (Element)nl.item(i);
                    }
                    count++;
                }
            }
        }
        throw new IndexOutOfBoundsException();
    }

    /**
     * Returns the child element at the given index. The index is the position of the element
     * considering all child nodes of type element.
     * 
     * @param parent The parent node. qparam tagName the element tag name.
     * @param index The 0 based position of the child.
     * @throws IndexOutOfBoundsException if no element exists at the specified index.
     */
    public final static Element getElement(Node parent, int index) {
        NodeList nl = parent.getChildNodes();
        int count = 0;
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                if (count == index) {
                    return (Element)nl.item(i);
                }
                count++;
            }
        }
        throw new IndexOutOfBoundsException();
    }

    /**
     * Returns the node's text child node or <code>null</code> if the node hasn't got a text node.
     */
    public final static Text getTextNode(Node node) {
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.TEXT_NODE) {
                return (Text)nl.item(i);
            }
        }
        return null;
    }

    /**
     * Returns the node's first CDATA section or <code>null</code> if the node hasn't got one.
     */
    public final static CDATASection getFirstCDataSection(Node node) {
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.CDATA_SECTION_NODE) {
                return (CDATASection)nl.item(i);
            }
        }
        return null;
    }

    /**
     * Returns the node's first CDATA section if the node has one. If not, this returns the node's
     * text child node or <code>null</code> if the node hasn't got a text node.
     */
    public final static String getCDATAorTextContent(Node node) {
        if (XmlUtil.getFirstCDataSection(node) != null) {
            return XmlUtil.getFirstCDataSection(node).getData();
        } else if (XmlUtil.getTextNode(node) != null) {
            return XmlUtil.getTextNode(node).getData();
        }
        return null;
    }

    /**
     * Adds a child Element by the name given in childName to the parent and returns the child.
     */
    public final static Element addNewChild(Document doc, Node parent, String childName) {
        Element e = doc.createElement(childName);
        parent.appendChild(e);
        return e;
    }

    /**
     * Adds a TextNode containing the text to the parent and returns the TextNode.
     */
    public final static Node addNewTextChild(Document doc, Node parent, String text) {
        Node n = doc.createTextNode(text);
        parent.appendChild(n);
        return n;
    }

    /**
     * Adds a CDATASection containing the text to the parent and returns the CDATASection.
     */
    public final static Node addNewCDATAChild(Document doc, Node parent, String text) {
        Node n = doc.createCDATASection(text);
        parent.appendChild(n);
        return n;
    }

    /**
     * Adds a TextNode or, if text contains chars>127, a CDATASection containing the text to the
     * parent and returns this new child.
     */
    public final static Node addNewCDATAorTextChild(Document doc, Node parent, String text) {
        if (text == null)
            return null;
        char[] chars = text.toCharArray();
        boolean toCDATA = false;
        for (int i = 0; i < chars.length && !toCDATA; i++) {
            if (chars[i] < 32 || 126 < chars[i])
                toCDATA = true;
        }
        return toCDATA ? addNewCDATAChild(doc, parent, text) : addNewTextChild(doc, parent, text);
    }

    /**
     * Returns the value for the given property from the given parent element. The parent XML
     * element must has the following format:
     * 
     * <pre>
     *   		&lt;Parent&gt;
     *   			&lt;Property isNull=&quot;false&quot;&gt;42&lt;/Property&gt;
     *   		&lt;/Parent&gt;
     * </pre>
     * 
     * @throws NullPointerException if parent or propertyName is <code>null</code> or the parent
     *             element does not contain an element with the given propertyName.
     */
    public String getValueFromElement(Element parent, String propertyName) {
        Element propertyEl = XmlUtil.getFirstElement(parent, propertyName);
        if (propertyEl == null) {
            throw new NullPointerException();
        }
        String isNull = parent.getAttribute("isNull"); //$NON-NLS-1$
        if (Boolean.valueOf(isNull).booleanValue()) {
            return null;
        } else {
            Text textNode = getTextNode(parent);
            if (textNode == null) {
                return ""; //$NON-NLS-1$
            } else {
                return textNode.getNodeValue();
            }
        }
    }

    private XmlUtil() {
    }

}
