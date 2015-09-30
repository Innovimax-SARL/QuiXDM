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
package innovimax.quixproc.datamodel.in.xml;

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
import innovimax.quixproc.datamodel.in.AQuiXEventStreamReader;
import innovimax.quixproc.datamodel.in.AStreamSource;
import innovimax.quixproc.datamodel.in.QuiXEventStreamReader;

public class XMLQuiXEventStreamReader extends AQuiXEventStreamReader {

	private final XMLInputFactory ifactory;
	private XMLStreamReader sreader;
	private QuiXCharStream baseURI;
	private final Queue<AQuiXEvent> buffer = new LinkedList<AQuiXEvent>();

	public XMLQuiXEventStreamReader(AStreamSource.XMLStreamSource source) {
		this.ifactory = XMLInputFactory.newFactory();
		this.ifactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
		this.ifactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
	}


	private AQuiXEvent load(Source current) throws QuiXException {
		try {
			this.sreader = this.ifactory.createXMLStreamReader(current);
		} catch (XMLStreamException e) {
			throw new QuiXException(e);
		}
		this.baseURI = QuiXCharStream.fromSequence(current.getSystemId());
		AQuiXEvent event = AQuiXEvent.getStartDocument(this.baseURI);
		return event;
	}

	private QuiXCharStream charBuffer = QuiXCharStream.EMPTY;

	@Override
	public AQuiXEvent process(AQuiXEventStreamReader.CallBack callback) throws QuiXException {
		AQuiXEvent event = null;
		try {
			if (!buffer.isEmpty()) {
				return buffer.poll();
			}
			if (!sreader.hasNext() && callback.getState().equals(QuiXEventStreamReader.State.START_SOURCE)) {
				// special case if the buffer is empty but the document has not
				// been closed
				event = AQuiXEvent.getEndDocument(this.baseURI);
				callback.setState(QuiXEventStreamReader.State.END_SOURCE);
				return event;
			}
			if (callback.getState().equals(QuiXEventStreamReader.State.END_SOURCE)) {
				return callback.processEndSource();
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
					callback.setState(QuiXEventStreamReader.State.END_SOURCE);
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


	@Override
	protected AQuiXEvent load(AStreamSource current) throws QuiXException {
		return load(((AStreamSource.XMLStreamSource )current).asSource());
	}


	@Override
	public void reinitialize(AStreamSource current) {
		//
		this.buffer.clear();
	}
}
