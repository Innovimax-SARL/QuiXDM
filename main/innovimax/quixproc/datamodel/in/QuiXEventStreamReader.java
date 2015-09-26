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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;

import innovimax.quixproc.datamodel.QuiXCharStream;
import innovimax.quixproc.datamodel.QuiXException;
import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.event.IQuiXEventStreamReader;

public class QuiXEventStreamReader implements IQuiXEventStreamReader {

	private final Iterator<Source> sources;
	private final XMLInputFactory ifactory;
	private XMLStreamReader sreader;
	private QuiXCharStream baseURI;
	private final Queue<AQuiXEvent> buffer = new LinkedList<AQuiXEvent>();

	public QuiXEventStreamReader(Iterable<Source> sources) throws XMLStreamException {
		this.ifactory = XMLInputFactory.newFactory();
		this.ifactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
		this.ifactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
		this.sources = sources.iterator();
	}

	private void loadSource() throws XMLStreamException {
		Source current = this.sources.next();
		this.sreader = this.ifactory.createXMLStreamReader(current);
		this.baseURI = QuiXCharStream.fromSequence(current.getSystemId());
	}

	@Override
	public boolean hasNext() {
		return this.state != State.FINISH;
	}

	enum State {
		INIT, START_SEQUENCE, START_DOCUMENT, END_DOCUMENT, FINISH
	}

	private State state = State.INIT;
	private QuiXCharStream charBuffer = QuiXCharStream.EMPTY;

	@Override
	public AQuiXEvent next() throws QuiXException {
		try {
			AQuiXEvent event = null;
			if (state.equals(State.FINISH)) {
				return null;
			}
			if (state.equals(State.INIT)) {
				event = AQuiXEvent.getStartSequence();
				this.state = State.START_SEQUENCE;
				return event;
			}
			if (state.equals(State.START_SEQUENCE)) {
				if (!this.sources.hasNext()) {
					event = AQuiXEvent.getEndSequence();
					this.state = State.FINISH;
					return event;
				}
				// there is at least one source
				loadSource();
				event = AQuiXEvent.getStartDocument(this.baseURI);
				this.state = State.START_DOCUMENT;
				return event;
			}
			if (!buffer.isEmpty()) {
				return buffer.poll();
			}
			if (!sreader.hasNext() && this.state.equals(State.START_DOCUMENT)) {
				// special case if the buffer is empty but the document has not
				// been closed
				event = AQuiXEvent.getEndDocument(this.baseURI);
				this.state = State.END_DOCUMENT;
				return event;
			}
			if (state.equals(State.END_DOCUMENT)) {
				if (this.sources.hasNext()) {
					// there is still sources
					loadSource();
					event = AQuiXEvent.getStartDocument(this.baseURI);
					this.state = State.START_DOCUMENT;
					return event;
				}
				event = AQuiXEvent.getEndSequence();
				this.state = State.FINISH;
				return event;
			}
			while (true) {
				int code = sreader.next();
				switch (code) {
				case XMLStreamConstants.START_DOCUMENT:
					// System.out.println("START_DOCUMENT");
					// do nothing already opened so get next event
					break;
				case XMLStreamConstants.START_ELEMENT:
					// System.out.println("START_ELEMENT");
					event = AQuiXEvent.getStartElement(QuiXCharStream.fromSequence(sreader.getLocalName()),
							QuiXCharStream.fromSequence(sreader.getNamespaceURI()),
							QuiXCharStream.fromSequence(sreader.getPrefix()));
					event = updateText(event);
					for (int i = 0; i < sreader.getNamespaceCount(); i++) {
						buffer.add(AQuiXEvent.getNamespace(QuiXCharStream.fromSequence(sreader.getNamespacePrefix(i)),
								QuiXCharStream.fromSequence(sreader.getNamespaceURI(i))));
					}
					for (int i = 0; i < sreader.getAttributeCount(); i++) {
						buffer.add(
								AQuiXEvent.getAttribute(QuiXCharStream.fromSequence(sreader.getAttributeLocalName(i)),
										QuiXCharStream.fromSequence(sreader.getAttributeNamespace(i)),
										QuiXCharStream.fromSequence(sreader.getAttributePrefix(i)),
										QuiXCharStream.fromSequence(sreader.getAttributeValue(i))));
					}
					return event;
				case XMLStreamConstants.END_DOCUMENT:
					// System.out.println("END_DOCUMENT");
					event = AQuiXEvent.getEndDocument(this.baseURI);
					event = updateText(event);
					this.state = State.END_DOCUMENT;
					return event;
				case XMLStreamConstants.END_ELEMENT:
					// System.out.println("END_ELEMENT");
					event = AQuiXEvent.getEndElement(QuiXCharStream.fromSequence(sreader.getLocalName()),
							QuiXCharStream.fromSequence(sreader.getNamespaceURI()),
							QuiXCharStream.fromSequence(sreader.getPrefix()));
					event = updateText(event);
					return event;
				case XMLStreamConstants.ATTRIBUTE:
					// System.out.println("ATTRIBUTE");
					for (int i = 0; i < sreader.getAttributeCount(); i++) {
						buffer.add(
								AQuiXEvent.getAttribute(QuiXCharStream.fromSequence(sreader.getAttributeLocalName(i)),
										QuiXCharStream.fromSequence(sreader.getAttributeNamespace(i)),
										QuiXCharStream.fromSequence(sreader.getAttributePrefix(i)),
										QuiXCharStream.fromSequence(sreader.getAttributeValue(i))));
					}
					return buffer.poll();
				case XMLStreamConstants.CDATA:
					// System.out.println("CDATA");
					this.charBuffer.append(sreader.getText());
					// do loop
					break;
				case XMLStreamConstants.CHARACTERS:
					// System.out.println("CHARACTERS");
					this.charBuffer.append(sreader.getText());
					// do loop
					break;
				case XMLStreamConstants.SPACE:
					// System.out.println("SPACE");
					this.charBuffer.append(sreader.getText());
					// do loop
					break;
				case XMLStreamConstants.COMMENT:
					// System.out.println("COMMENT");
					event = AQuiXEvent.getComment(QuiXCharStream.fromSequence(sreader.getText()));
					event = updateText(event);
					return event;
				case XMLStreamConstants.PROCESSING_INSTRUCTION:
					// System.out.println("PI");
					event = AQuiXEvent.getPI(QuiXCharStream.fromSequence(sreader.getPITarget()),
							QuiXCharStream.fromSequence(sreader.getPIData()));
					event = updateText(event);
					return event;
				// case XMLStreamConstants.NAMESPACE:
				// System.out.println("NAMESPACE");
				default:
					// do loop
					break;
				}
			}
		} catch (XMLStreamException e) {
			throw new QuiXException(e);
		}
	}

	/**
	 * This function take a QuiXEvent as parameter If there is character event
	 * waiting, it creates it, empties the charbuffer and push the current event
	 * in the stack. If not it return the parameter
	 * 
	 * @param event
	 * @return
	 */
	private AQuiXEvent updateText(AQuiXEvent event) {
		if (!charBuffer.isEmpty()) {
			AQuiXEvent text = AQuiXEvent.getText(charBuffer);
			charBuffer = QuiXCharStream.EMPTY;
			this.buffer.add(event);
			return text;
		}
		return event;
	}

	@Override
	public void close() {
		try {
			this.sreader.close();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) throws XMLStreamException, QuiXException {
		Iterable<Source> sources = java.util.Arrays.asList(new Source[] {
		        new javax.xml.transform.stream.StreamSource("/Users/innovimax/tmp/gs1/new/1000/1000_KO_22062015.xml"),  
		        new javax.xml.transform.stream.StreamSource("/Users/innovimax/tmp/gs1/new/1000/1000_OK_22062015.xml")   
		});
		QuiXEventStreamReader qesr = new QuiXEventStreamReader(sources);
		while(qesr.hasNext()) {
		    System.out.println(qesr.next());
		}
	}
}
