/*
QuiXProc: efficient evaluation of XProc Pipelines.
Copyright (C) 2011-2015 Innovimax
All rights reserved.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package innovimax.quixproc.datamodel.in;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import innovimax.quixproc.datamodel.IStream;
import innovimax.quixproc.datamodel.QuixException;
//import innovimax.quixproc.datamodel.QuixEvent.Attribute;
//import innovimax.quixproc.datamodel.QuixEvent.Namespace;
import innovimax.quixproc.datamodel.event.QuixEvent;

public class QuixEventStreamReader implements XMLStreamReader {
  private final IStream<QuixEvent> qs;
  private final static boolean DEBUG = false;
  private final static int POSITION = 1;
  public QuixEventStreamReader(IStream<QuixEvent> qs) {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    this.qs = qs;
  }

  @Override
  public Object getProperty(String name) throws IllegalArgumentException {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean hasNext() throws XMLStreamException {
    
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    if (future != null) return true;
    try {
      return qs.hasNext();
    } catch (QuixException e) {
      throw new XMLStreamException(e);
    }
  }

  private QuixEvent                 current    = null;
  private QuixEvent                 future     = null;
  private List<QuixEvent.Namespace> namespaces = new ArrayList<QuixEvent.Namespace>();
  private List<QuixEvent.Attribute> attributes = new ArrayList<QuixEvent.Attribute>();

  @Override
  public int next() throws XMLStreamException {
    try {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    if (DEBUG) System.out.println("QuixStreamReader.next : ("+future+","+current+")");
    while (future != null || qs.hasNext()) {
      if (future != null) {
        current = future;
        future = null;
      } else {
        current = qs.next();
      }
      switch (current.getType()) {
        case START_SEQUENCE:
          // DO NOTHING : should be already processed by caller
          break;
        case END_SEQUENCE:
          // DO NOTHING : should be already processed by caller
          break;
        case START_DOCUMENT:
          return XMLStreamConstants.START_DOCUMENT;
        case END_DOCUMENT:
          return XMLStreamConstants.END_DOCUMENT;
        case START_ELEMENT:
          // get the attributes if any
          attributes.clear();
          namespaces.clear();
          while (true) {
            boolean test = qs.hasNext();
            if (!test) throw new QuixException("Impossible");
            future = qs.next();
            if (future.isAttribute()) {
              attributes.add(future.asAttribute());
            } else if (future.isNamespace()) {
              namespaces.add(future.asNamespace());
            } else {
              break;
            }
          }
          return XMLStreamConstants.START_ELEMENT;
        case END_ELEMENT:
          return XMLStreamConstants.END_ELEMENT;
        case ATTRIBUTE: // This should never happen since attribute are processed
          // DO NOTHING
          break;
        case COMMENT:
          return XMLStreamConstants.COMMENT;
        case PI:
          return XMLStreamConstants.PROCESSING_INSTRUCTION;
        case TEXT:
          return XMLStreamConstants.CHARACTERS;
      }
    }
    } catch(QuixException e) {
      throw new XMLStreamException(e);
      
    }
    return XMLStreamConstants.END_DOCUMENT;
  }

  @Override
  public void require(int type, String namespaceURI, String localName) {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    throw new RuntimeException("no such method");
  }

  @Override
  public String getElementText() throws XMLStreamException {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    if (getEventType() != XMLStreamConstants.START_ELEMENT) { throw new XMLStreamException("parser must be on START_ELEMENT to read next text", getLocation()); }
    int eventType = next();
    StringBuffer content = new StringBuffer();
    while (eventType != XMLStreamConstants.END_ELEMENT) {
      if (eventType == XMLStreamConstants.CHARACTERS || eventType == XMLStreamConstants.CDATA || eventType == XMLStreamConstants.SPACE
          || eventType == XMLStreamConstants.ENTITY_REFERENCE) {
        content.append(getText());
      } else if (eventType == XMLStreamConstants.PROCESSING_INSTRUCTION || eventType == XMLStreamConstants.COMMENT) {
        // skipping
      } else if (eventType == XMLStreamConstants.END_DOCUMENT) {
        throw new XMLStreamException("unexpected end of document when reading element text content", getLocation());
      } else if (eventType == XMLStreamConstants.START_ELEMENT) {
        throw new XMLStreamException("element text content may not contain START_ELEMENT", getLocation());
      } else {
        throw new XMLStreamException("Unexpected event type " + eventType, getLocation());
      }
      eventType = next();
    }
    return content.toString();
  }

  @Override
  public int nextTag() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    throw new RuntimeException("no such method");
  }

  @Override
  public void close() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    this.qs.close();
  }

  @Override
  public String getNamespaceURI(String prefix) {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isStartElement() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    return current.isStartElement();
  }

  @Override
  public boolean isEndElement() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    return current.isEndElement();
  }

  @Override
  public boolean isCharacters() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    return current.isText();
  }

  @Override
  public boolean isWhiteSpace() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
   return false;
  }

  @Override
  public String getAttributeValue(String namespaceURI, String localName) {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    for (QuixEvent.Attribute attribute : attributes) {
      if (localName.equals(attribute.getLocalName()) && namespaceURI.equals(attribute.getURI())) return attribute.getValue();
    }
    return null;
  }

  @Override
  public int getAttributeCount() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    return this.attributes.size();
  }

  @Override
  public QName getAttributeName(int index) {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    return this.attributes.get(index).getQName();
  }

  @Override
  public String getAttributeNamespace(int index) {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    return this.attributes.get(index).getURI();
  }

  @Override
  public String getAttributeLocalName(int index) {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    return this.attributes.get(index).getLocalName();
  }

  @Override
  public String getAttributePrefix(int index) {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    return this.attributes.get(index).getPrefix();
  }

  @Override
  public String getAttributeType(int index) {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    return null; // no type stored
  }

  @Override
  public String getAttributeValue(int index) {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    return this.attributes.get(index).getValue();
  }

  @Override
  public boolean isAttributeSpecified(int index) {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    return true;
  }

  @Override
  public int getNamespaceCount() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    return this.namespaces.size();
  }

  @Override
  public String getNamespacePrefix(int index) {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    return this.namespaces.get(index).getPrefix();
  }

  @Override
  public String getNamespaceURI(int index) {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    // TODO Auto-generated method stub
    return this.namespaces.get(index).getURI();
  }

  @Override
  public NamespaceContext getNamespaceContext() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getEventType() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    if (current == null) return XMLStreamConstants.START_DOCUMENT;
    switch (current.getType()) {
      case START_DOCUMENT:
        return XMLStreamConstants.START_DOCUMENT;
      case END_DOCUMENT:
        return XMLStreamConstants.END_DOCUMENT;
      case START_ELEMENT:
        return XMLStreamConstants.START_ELEMENT;
      case END_ELEMENT:
        return XMLStreamConstants.END_ELEMENT;
      case ATTRIBUTE:
        return XMLStreamConstants.ATTRIBUTE; // Not really possible
      case COMMENT:
        return XMLStreamConstants.COMMENT;
      case PI:
        return XMLStreamConstants.PROCESSING_INSTRUCTION;
      case TEXT:
        return XMLStreamConstants.CHARACTERS;
      case START_SEQUENCE: // Not Possible
        break;
      case END_SEQUENCE: // Not Possible
        break;
    }
    return 0;
  }

  @Override
  public String getText() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    // Returns the current value of the parse event as a string,
    // this returns the string value of a CHARACTERS event,
    // returns the value of a COMMENT,
    // the replacement value for an ENTITY_REFERENCE,
    // the string value of a CDATA section,
    // the string value for a SPACE event,
    // or the String value of the internal subset of the DTD.
    // If an ENTITY_REFERENCE has been resolved, any character data will be reported as CHARACTERS events.
    switch (current.getType()) {
      case TEXT:
        return current.asText().getData();
      case COMMENT:
        return current.asComment().getData();
    }
    return null;
  }

  @Override
  public char[] getTextCharacters() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    String text = getText();
    return (text == null ? null : text.toCharArray());
  }

  @Override
  public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName()+"+++");
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getTextStart() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getTextLength() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    // TODO Auto-generated method stub
    return getText().length();
  }

  @Override
  public String getEncoding() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean hasText() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    // return true if the current event has text, false otherwise
    // The following events have text:
    // CHARACTERS,DTD
    // ,ENTITY_REFERENCE, COMMENT, SPACE
    // TODO Auto-generated method stub
    return current.isText() || current.isComment();
  }

  @Override
  public Location getLocation() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    // TODO Put real info here
    return new Location() {

      @Override
      public int getLineNumber() {
        // TODO Auto-generated method stub
        return 0;
      }

      @Override
      public int getColumnNumber() {
        // TODO Auto-generated method stub
        return 0;
      }

      @Override
      public int getCharacterOffset() {
        // TODO Auto-generated method stub
        return 0;
      }

      @Override
      public String getPublicId() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public String getSystemId() {
        // TODO Auto-generated method stub
        return null;
      }

    };
  }

  @Override
  public QName getName() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    switch (current.getType()) {
      case START_ELEMENT:
      case END_ELEMENT:
        return current.asNamedEvent().getQName();
    }
    return null;
  }

  @Override
  public String getLocalName() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    switch (current.getType()) {
      case START_ELEMENT:
      case END_ELEMENT:
        return current.asNamedEvent().getLocalName();
    }
    return null;
  }

  @Override
  public boolean hasName() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    return current.isStartElement() || current.isEndElement();
  }

  @Override
  public String getNamespaceURI() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    switch (current.getType()) {
      case START_ELEMENT:
      case END_ELEMENT:
        return current.asNamedEvent().getURI();
    }
    return null;
  }

  @Override
  public String getPrefix() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    switch (current.getType()) {
      case START_ELEMENT:
      case END_ELEMENT:
        return current.asNamedEvent().getPrefix();
    }
    return null;
  }

  @Override
  public String getVersion() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isStandalone() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean standaloneSet() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String getCharacterEncodingScheme() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getPITarget() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
    return current.asPI().getTarget();
  }

  @Override
  public String getPIData() {
    if (DEBUG) System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
   return current.asPI().getData();
  }

}