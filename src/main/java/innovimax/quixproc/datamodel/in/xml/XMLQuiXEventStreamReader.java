/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.in.xml;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;

import innovimax.quixproc.datamodel.QuiXCharStream;
import innovimax.quixproc.datamodel.QuiXException;
import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.in.AQuiXEventBufferStreamReader;
import innovimax.quixproc.datamodel.in.AStreamSource;
import innovimax.quixproc.datamodel.in.AStreamSource.XMLStreamSource;
import innovimax.quixproc.datamodel.in.QuiXEventStreamReader.State;

public class XMLQuiXEventStreamReader extends AQuiXEventBufferStreamReader {

	private final XMLInputFactory ifactory;
	private XMLStreamReader sreader;
	private QuiXCharStream baseURI;

	public XMLQuiXEventStreamReader() {
		this.ifactory = XMLInputFactory.newFactory();
		this.ifactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
		this.ifactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
	}

	private AQuiXEvent load(final Source current) {
		try {
			this.sreader = this.ifactory.createXMLStreamReader(current);
		} catch (final XMLStreamException e) {
			throw new QuiXException(e);
		}
		this.baseURI = QuiXCharStream.fromSequence(current.getSystemId());
		return AQuiXEvent.getStartDocument(this.baseURI);
	}

	private QuiXCharStream charBuffer = QuiXCharStream.EMPTY;

	@Override
	public AQuiXEvent process(final CallBack callback) {
		try {
			if (!this.buffer.isEmpty()) {
				return this.buffer.poll();
			}
			AQuiXEvent event;
			if (!this.sreader.hasNext() && callback.getState() == State.START_SOURCE) {
				// special case if the buffer is empty but the document has not
				// been closed
				event = AQuiXEvent.getEndDocument(this.baseURI);
				callback.setState(State.END_SOURCE);
				return event;
			}
			if (callback.getState() == State.END_SOURCE) {
				return callback.processEndSource();
			}
			while (true) {
				final int code = this.sreader.next();
				switch (code) {
				case XMLStreamConstants.START_DOCUMENT:
					// System.out.println("START_DOCUMENT");
					// do nothing already opened so get next event
					break;
				case XMLStreamConstants.START_ELEMENT:
					// System.out.println("START_ELEMENT");
					event = AQuiXEvent.getStartElement(QuiXCharStream.fromSequence(this.sreader.getLocalName()),
							QuiXCharStream.fromSequence(this.sreader.getNamespaceURI()),
							QuiXCharStream.fromSequence(this.sreader.getPrefix()));
					event = updateText(event);
					for (int i = 0; i < this.sreader.getNamespaceCount(); i++) {
						this.buffer.add(
								AQuiXEvent.getNamespace(QuiXCharStream.fromSequence(this.sreader.getNamespacePrefix(i)),
										QuiXCharStream.fromSequence(this.sreader.getNamespaceURI(i))));
					}
					for (int i = 0; i < this.sreader.getAttributeCount(); i++) {
						this.buffer.add(AQuiXEvent.getAttribute(
								QuiXCharStream.fromSequence(this.sreader.getAttributeLocalName(i)),
								QuiXCharStream.fromSequence(this.sreader.getAttributeNamespace(i)),
								QuiXCharStream.fromSequence(this.sreader.getAttributePrefix(i)),
								QuiXCharStream.fromSequence(this.sreader.getAttributeValue(i))));
					}
					return event;
				case XMLStreamConstants.END_DOCUMENT:
					// System.out.println("END_DOCUMENT");
					event = AQuiXEvent.getEndDocument(this.baseURI);
					event = updateText(event);
					callback.setState(State.END_SOURCE);
					return event;
				case XMLStreamConstants.END_ELEMENT:
					// System.out.println("END_ELEMENT");
					event = AQuiXEvent.getEndElement(QuiXCharStream.fromSequence(this.sreader.getLocalName()),
							QuiXCharStream.fromSequence(this.sreader.getNamespaceURI()),
							QuiXCharStream.fromSequence(this.sreader.getPrefix()));
					event = updateText(event);
					return event;
				case XMLStreamConstants.ATTRIBUTE:
					// System.out.println("ATTRIBUTE");
					for (int i = 0; i < this.sreader.getAttributeCount(); i++) {
						this.buffer.add(AQuiXEvent.getAttribute(
								QuiXCharStream.fromSequence(this.sreader.getAttributeLocalName(i)),
								QuiXCharStream.fromSequence(this.sreader.getAttributeNamespace(i)),
								QuiXCharStream.fromSequence(this.sreader.getAttributePrefix(i)),
								QuiXCharStream.fromSequence(this.sreader.getAttributeValue(i))));
					}
					return this.buffer.poll();
				case XMLStreamConstants.CDATA:
					// System.out.println("CDATA");
					this.charBuffer = this.charBuffer.append(this.sreader.getText());
					// do loop
					break;
				case XMLStreamConstants.CHARACTERS:
					// System.out.println("CHARACTERS");
					this.charBuffer = this.charBuffer.append(this.sreader.getText());
					// do loop
					break;
				case XMLStreamConstants.SPACE:
					// System.out.println("SPACE");
					this.charBuffer.append(this.sreader.getText());
					// do loop
					break;
				case XMLStreamConstants.COMMENT:
					// System.out.println("COMMENT");
					event = AQuiXEvent.getComment(QuiXCharStream.fromSequence(this.sreader.getText()));
					event = updateText(event);
					return event;
				case XMLStreamConstants.PROCESSING_INSTRUCTION:
					// System.out.println("PI");
					event = AQuiXEvent.getPI(QuiXCharStream.fromSequence(this.sreader.getPITarget()),
							QuiXCharStream.fromSequence(this.sreader.getPIData()));
					event = updateText(event);
					return event;
				// case XMLStreamConstants.NAMESPACE:
				// System.out.println("NAMESPACE");
				default:
					// do loop
					break;
				}
			}
		} catch (final XMLStreamException e) {
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
	private AQuiXEvent updateText(final AQuiXEvent event) {
		if (!this.charBuffer.isEmpty()) {
			final AQuiXEvent text = AQuiXEvent.getText(this.charBuffer);
			this.charBuffer = QuiXCharStream.EMPTY;
			this.buffer.add(event);
			return text;
		}
		return event;
	}

	@Override
	public void close() {
		try {
			this.sreader.close();
		} catch (final XMLStreamException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected AQuiXEvent load(final AStreamSource current) {
		return load(((XMLStreamSource) current).asSource());
	}
}
