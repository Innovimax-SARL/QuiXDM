/*
QuiXProc: efficient evaluation of XProc Pipelines.
Copyright (C) 2011 Innovimax
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
package innovimax.quixproc.datamodel;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class Load implements IStream<QuixEvent> {

  private final XMLStreamReader sreader;
  private final String          baseURI;
  private final Queue<QuixEvent> buffer = new LinkedList<QuixEvent>();

  public Load(InputStream is, String baseURI) throws XMLStreamException {
    XMLInputFactory ifactory = XMLInputFactory.newFactory();
    ifactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
    ifactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
    this.sreader = ifactory.createXMLStreamReader(is);
    this.baseURI = baseURI;
  }

  @Override
  public boolean hasNext() {
    return this.state != State.FINISH;
  }

  enum State { INIT, START_SEQUENCE, START_DOCUMENT, END_DOCUMENT, FINISH }
  
  private State state = State.INIT;
  private StringBuilder charBuffer = new StringBuilder();
  
  @Override
  public QuixEvent next() throws QuixException {
    try {
      QuixEvent event = null;
      if (state.equals(State.FINISH)) {
        return null;
      }
      if (state.equals(State.INIT)) {
        event = QuixEvent.getStartSequence();
        this.state = state.START_SEQUENCE;
        return event;
      }
      if (state.equals(State.START_SEQUENCE)) {
        event = QuixEvent.getStartDocument(this.baseURI);
        this.state = State.START_DOCUMENT;
        return event;
      }
      if (!buffer.isEmpty()) {
        return buffer.poll();
      }
      if (!sreader.hasNext() && this.state.equals(State.START_DOCUMENT)) {
        // special case if the buffer is empty but the document has not been closed
        event = QuixEvent.getEndDocument(this.baseURI);
        this.state = State.END_DOCUMENT;
        return event;
      }
      if (state.equals(State.END_DOCUMENT)) {
        event = QuixEvent.getEndSequence();
        this.state = State.FINISH;
        return event;
      }
      while(true) {
      int code = sreader.next();
      QuixEvent text;
      switch (code) {
        case XMLStreamReader.START_DOCUMENT:
          // System.out.println("START_DOCUMENT");
          // do nothing already opened so get next event
          break;
        case XMLStreamReader.START_ELEMENT:
          // System.out.println("START_ELEMENT");
          event = QuixEvent.getStartElement(sreader.getLocalName(), sreader.getNamespaceURI(), sreader.getPrefix());
          event = updateText(event);          
          for(int i = 0; i < sreader.getNamespaceCount(); i++) {
            buffer.add(QuixEvent.getNamespace(sreader.getNamespacePrefix(i), sreader.getNamespaceURI(i)));
          }
          for (int i = 0; i < sreader.getAttributeCount(); i++) {
            buffer.add(QuixEvent.getAttribute(sreader.getAttributeLocalName(i), sreader.getAttributeNamespace(i), sreader.getAttributePrefix(i),
                sreader.getAttributeValue(i)));
          }
          return event;
        case XMLStreamReader.END_DOCUMENT:
          // System.out.println("END_DOCUMENT");
          event = QuixEvent.getEndDocument(this.baseURI);
          event = updateText(event);          
          this.state = State.END_DOCUMENT;
          return event;
        case XMLStreamReader.END_ELEMENT:
          // System.out.println("END_ELEMENT");
          event = QuixEvent.getEndElement(sreader.getLocalName(), sreader.getNamespaceURI(), sreader.getPrefix());
          event = updateText(event);          
          return event;
        case XMLStreamReader.ATTRIBUTE:
          // System.out.println("ATTRIBUTE");
          for (int i = 0; i < sreader.getAttributeCount(); i++) {
            buffer.add(QuixEvent.getAttribute(sreader.getAttributeLocalName(i), sreader.getAttributeNamespace(i), sreader.getAttributePrefix(i),
                sreader.getAttributeValue(i)));
          }
          return buffer.poll();
        case XMLStreamReader.CDATA:
          // System.out.println("CDATA");
          this.charBuffer.append(sreader.getText());
          // do loop
          break;
        case XMLStreamReader.CHARACTERS:
          // System.out.println("CHARACTERS");
          this.charBuffer.append(sreader.getText());
          // do loop
          break;
        case XMLStreamReader.SPACE:
          // System.out.println("SPACE");
          this.charBuffer.append(sreader.getText());
          // do loop
          break;
        case XMLStreamReader.COMMENT:
          // System.out.println("COMMENT");
          event = QuixEvent.getComment(sreader.getText());
          event = updateText(event);          
          return event;
        case XMLStreamReader.PROCESSING_INSTRUCTION:
          // System.out.println("PI");
          event = QuixEvent.getPI(sreader.getPITarget(), sreader.getPIData());
          event = updateText(event);          
          return event;
        //case XMLStreamReader.NAMESPACE:
          // System.out.println("NAMESPACE");
        default:
          // do loop
          break;
      }
      }
    } catch (XMLStreamException e) {
      throw new QuixException(e);
    }
  }
  /**
   * This function take a QuixEvent as parameter
   * If there is character event waiting, it creates it, empties the charbuffer and push the current
   * event in the stack. If not it return the parameter
   * @param event
   * @return
   */
  private QuixEvent updateText(QuixEvent event) {
    if (charBuffer.length() != 0) {
      QuixEvent text = QuixEvent.getText(charBuffer.toString());
      charBuffer.setLength(0);
      this.buffer.add(event);
      return text;
    }
    return event;
  }

  @Override
  public void close() {
    // TODO Auto-generated method stub

  }

}
