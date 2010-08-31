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

package org.faktorips.util.message;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.SystemUtils;

/**
 * A set of <code>Message</code>s.
 * 
 * @see Message
 * 
 * @author Jan Ortmann
 */
public class MessageList implements Iterable<Message> {

    /**
     * Creates a copy from the message list and replaces all references to the old object with the
     * new object.
     * 
     * @param list the list to copy
     * @param oldObject the old object reference that should be replaced.
     * @param newObject the object reference to set
     */
    public final static MessageList createCopy(MessageList list, Object oldObject, Object newObject) {
        if (list.isEmpty()) {
            return list;
        }
        MessageList newList = new MessageList();
        int numOfMsg = list.getNoOfMessages();
        for (int i = 0; i < numOfMsg; i++) {
            newList.add(Message.createCopy(list.getMessage(i), oldObject, newObject));
        }
        return newList;
    }

    private List<Message> messages = new ArrayList<Message>(0);

    /**
     * Creates an empty message list.
     */
    public MessageList() {
        // Provide default constructor.
    }

    /**
     * Creates a message list that contains the given message. <code>null</code> will be ignored as
     * a parameter value.
     */
    public MessageList(Message msg) {
        add(msg);
    }

    /**
     * Adds the message to the list. <code>null</code> will be ignored as a parameter value.
     */
    public void add(Message msg) {
        if (msg == null) {
            return;
        }
        messages.add(msg);
    }

    /**
     * Adds the messages in the given list to this list.
     * 
     * @throws IllegalArgumentException if msgList is null.
     */
    public void add(MessageList msgList) {
        if (msgList == null) {
            return;
        }
        int max = msgList.getNoOfMessages();
        for (int i = 0; i < max; i++) {
            add(msgList.getMessage(i));
        }
    }

    /**
     * Copies the messages from the given list to this list and sets the message's invalid object
     * properties.
     * 
     * @param msgList the list to copy the messages from.
     * @param invalidObjectProperty the object and it's property that the messages refer to.
     * @param override <code>true</code> if the invalidObjectProperty should be set in all messages.
     *            <code>false</code> if the invalidObjectProperty is set only for messages that do
     *            not contain any invalid object property information.
     */
    public void add(MessageList msgList, ObjectProperty invalidObjectProperty, boolean override) {
        if (msgList == null) {
            return;
        }
        int max = msgList.getNoOfMessages();
        for (int i = 0; i < max; i++) {
            Message msg = msgList.getMessage(i);
            if (override || msg.getInvalidObjectProperties().length == 0) {
                add(new Message(msg.getCode(), msg.getText(), msg.getSeverity(), invalidObjectProperty));
            } else {
                add(msg);
            }
        }
    }

    /**
     * Returns true if the list is empty.
     */
    public boolean isEmpty() {
        return messages.isEmpty();
    }

    /**
     * Returns the total number of messages in the list.
     */
    public int getNoOfMessages() {
        return messages.size();
    }

    /**
     * Returns the number of messages in this list that have the indicated severity.
     */
    public int getNoOfMessages(int severity) {
        List<Message> msgList = new ArrayList<Message>(messages.size());
        for (int i = 0; i < messages.size(); i++) {
            Message msg = messages.get(i);
            if (msg.getSeverity() == severity) {
                msgList.add(msg);
            }
        }
        return msgList.size();
    }

    /**
     * Returns the message at the indicated index (indexing starts with 0).
     * 
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public Message getMessage(int index) {
        return messages.get(index);
    }

    /**
     * Returns the first message with the given severity or null if none is found.
     */
    public Message getFirstMessage(int severity) {
        for (Message msg : messages) {
            if (msg.getSeverity() == severity) {
                return msg;
            }
        }
        return null;
    }

    /**
     * Returns the message with the highest severity or <code>null</code> if the list does not
     * contain any message. If more than one message with the highest severity exists, the first one
     * is returned.
     */
    public Message getMessageWithHighestSeverity() {
        int highestSeverity = getSeverity();
        return getFirstMessage(highestSeverity);
    }

    /**
     * Returns the first message in the list that has the indicated message code. Returns null, if
     * the list does not contain such a message.
     */
    public Message getMessageByCode(String code) {
        for (int i = 0; i < getNoOfMessages(); i++) {
            if (getMessage(i).getCode().equals(code)) {
                return getMessage(i);
            }
        }
        return null;
    }

    /**
     * Returns the message list's severity. This is the maximum severity of the list's messages. If
     * the list does not contain any messages, the method returns 0.
     */
    public int getSeverity() {
        int severity = 0;
        for (int i = 0; i < getNoOfMessages(); i++) {
            if (getMessage(i).getSeverity() > severity) {
                severity = getMessage(i).getSeverity();
                if (severity == Message.ERROR) {
                    return severity;
                }
            }
        }
        return severity;
    }

    /**
     * Returns the text of all messages in the list, separated by the system's default line
     * separator.
     */
    public String getText() {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < getNoOfMessages(); i++) {
            if (i > 0) {
                s.append(SystemUtils.LINE_SEPARATOR);
            }
            s.append(getMessage(i).getText());
        }
        return s.toString();

    }

    /**
     * Returns true if one the messages in the list is an error message, otherwise false.
     */
    public boolean containsErrorMsg() {
        for (int i = 0; i < getNoOfMessages(); i++) {
            if (getMessage(i).getSeverity() == Message.ERROR) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a new list with the messages in this list that belong to the given object (any
     * property). Returns an empty list if no such message is found.
     */
    public MessageList getMessagesFor(Object object) {
        return getMessagesFor(object, null);
    }

    /**
     * Returns a new list with the messages in this list that belong to the given object and
     * property and the property is of the given index. Returns an empty list if no such message is
     * found.
     */
    public MessageList getMessagesFor(Object object, String property, int index) {
        MessageList result = new MessageList();
        for (int i = 0; i < getNoOfMessages(); i++) {
            Message msg = getMessage(i);
            ObjectProperty[] op = msg.getInvalidObjectProperties();
            for (ObjectProperty element : op) {
                if (element.getObject().equals(object)) {
                    if (property == null) {
                        result.add(msg);
                        break;
                    }
                    if (property.equals(element.getProperty())) {
                        if (index < 0 || element.getIndex() == index) {
                            result.add(msg);
                            break;
                        }
                    }
                }
            }
        }
        return result;

    }

    /**
     * Returns a new list with the messages in this list that belong to the given object and
     * property. Returns an empty list if no such message is found.
     */
    public MessageList getMessagesFor(Object object, String property) {
        return getMessagesFor(object, property, -1);
    }

    /**
     * Returns a new <tt>MessageList</tt> containing only the <tt>Message</tt>s with the indicated
     * severity.
     */
    public MessageList getMessages(int severity) {
        MessageList messageList = new MessageList();
        for (Message message : messages) {
            if (message.getSeverity() == severity) {
                messageList.add(message);
            }
        }
        return messageList;
    }

    /**
     * Returns all messages in the list separated by a line separator.
     */
    @Override
    public String toString() {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < getNoOfMessages(); i++) {
            s.append(getMessage(i).toString() + SystemUtils.LINE_SEPARATOR);
        }
        return s.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof MessageList)) {
            return false;
        }

        MessageList other = (MessageList)o;

        if (this.getNoOfMessages() != other.getNoOfMessages()) {
            return false;
        }

        for (int i = 0; i < other.getNoOfMessages(); i++) {
            Message message = messages.get(i);
            Message otherMessage = other.messages.get(i);
            if (!((message == null) ? otherMessage == null : message.equals(otherMessage))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + getNoOfMessages();
        for (Message message : messages) {
            int c = (message == null) ? 0 : message.hashCode();
            result = 31 * result + c;
        }
        return result;
    }

    /**
     * Deletes all messages this list contains.
     */
    public void clear() {
        messages.clear();
    }

    public Iterator<Message> iterator() {
        return messages.iterator();
    }

}