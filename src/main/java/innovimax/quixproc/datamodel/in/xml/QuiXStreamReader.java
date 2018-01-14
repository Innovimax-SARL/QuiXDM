/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.in.xml;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;

import innovimax.quixproc.datamodel.QuiXCharStream;
import innovimax.quixproc.datamodel.QuiXException;
import innovimax.quixproc.datamodel.QuiXToken;
import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.stream.IQuiXStreamReader;
import innovimax.quixproc.datamodel.stream.QuiXStreamException;
import javax.xml.transform.stream.StreamSource;

public class QuiXStreamReader implements IQuiXStreamReader {

	private final Iterator<Source> sources;
	private int position;
	private final XMLInputFactory ifactory;
	private XMLStreamReader sreader;
	private QuiXCharStream baseURI;
	private final Queue<AQuiXEvent> buffer = new LinkedList<AQuiXEvent>();

	private QuiXStreamReader(final Iterable<Source> sources) {
		this.ifactory = XMLInputFactory.newFactory();
		this.position = 0;
		this.ifactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
		this.ifactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
		this.sources = sources.iterator();
	}

	private void loadSource() throws XMLStreamException {
		final Source current = this.sources.next();
		this.sreader = this.ifactory.createXMLStreamReader(current);
		this.baseURI = QuiXCharStream.fromSequence(current.getSystemId());
	}

	@Override
	public boolean hasNext() {
		return this.state != State.FINISH;
	}

	private enum State {
		INIT, START_SEQUENCE, START_DOCUMENT, END_DOCUMENT, FINISH
	}

	private State state = State.INIT;
	private final StringBuilder charBuffer = new StringBuilder();

	@Override
	public QuiXToken next() {
		try {
			if (this.state == State.FINISH) {
				return null;
			}
			QuiXToken event;
			if (this.state == State.INIT) {
				event = QuiXToken.START_SEQUENCE;
				this.state = State.START_SEQUENCE;
				return event;
			}
			if (this.state == State.START_SEQUENCE) {
				if (!this.sources.hasNext()) {
					event = QuiXToken.END_SEQUENCE;
					this.state = State.FINISH;
					return event;
				}
				// there is at least one source
				loadSource();
				event = QuiXToken.START_DOCUMENT;
				this.position++;
				this.state = State.START_DOCUMENT;
				return event;
			}
			if (!this.buffer.isEmpty()) {
				return this.buffer.poll().getType();
			}
			if (!this.sreader.hasNext() && this.state == State.START_DOCUMENT) {
				// special case if the buffer is empty but the document has not
				// been closed
				event = QuiXToken.END_DOCUMENT;
				this.state = State.END_DOCUMENT;
				return event;
			}
			if (this.state == State.END_DOCUMENT) {
				if (this.sources.hasNext()) {
					// there is still sources
					loadSource();
					this.position++;
					event = QuiXToken.START_DOCUMENT;
					this.state = State.START_DOCUMENT;
					return event;
				}
				event = QuiXToken.END_SEQUENCE;
				this.state = State.FINISH;
				return event;
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
					event = QuiXToken.START_ELEMENT;
					// AQuiXEvent.getStartElement(sreader.getLocalName(),
					// sreader.getNamespaceURI(), sreader.getPrefix());
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
					event = QuiXToken.END_DOCUMENT;
					// AQuiXEvent.getEndDocument(this.baseURI);
					event = updateText(event);
					this.state = State.END_DOCUMENT;
					return event;
				case XMLStreamConstants.END_ELEMENT:
					// System.out.println("END_ELEMENT");
					event = QuiXToken.END_ELEMENT;
					// AQuiXEvent.getEndElement(sreader.getLocalName(),
					// sreader.getNamespaceURI(),
					// sreader.getPrefix());
					event = updateText(event);
					return event;
				case XMLStreamConstants.ATTRIBUTE:
					// System.out.println("ATTRIBUTE");
					for (int i = 0; i < this.sreader.getAttributeCount(); i++) {
						// buffer.add(
						// QuiXToken.ATTRIBUTE
						// AQuiXEvent.getAttribute(sreader.getAttributeLocalName(i),
						// sreader.getAttributeNamespace(i),
						// sreader.getAttributePrefix(i),
						// sreader.getAttributeValue(i))
						// );
					}
					return this.buffer.poll().getType();
				case XMLStreamConstants.CDATA:
					// System.out.println("CDATA");
					this.charBuffer.append(this.sreader.getText());
					// do loop
					break;
				case XMLStreamConstants.CHARACTERS:
					// System.out.println("CHARACTERS");
					this.charBuffer.append(this.sreader.getText());
					// do loop
					break;
				case XMLStreamConstants.SPACE:
					// System.out.println("SPACE");
					this.charBuffer.append(this.sreader.getText());
					// do loop
					break;
				case XMLStreamConstants.COMMENT:
					// System.out.println("COMMENT");
					// event = AQuiXEvent.getComment(sreader.getText());
					// event = updateText(event);
					// return event;
					return null;
				case XMLStreamConstants.PROCESSING_INSTRUCTION:
					// System.out.println("PI");
					// event = AQuiXEvent.getPI(sreader.getPITarget(),
					// sreader.getPIData());
					// event = updateText(event);
					// return event;
					return null;
				// case XMLStreamConstants.NAMESPACE:
				// System.out.println("NAMESPACE");
				default:
					// do loop
					break;
				}
			}
		} catch (final XMLStreamException e) {
			throw new QuiXStreamException(e);
		}
	}

	/**
	 * This function take a QuixEvent as parameter If there is character event
	 * waiting, it creates it, empties the charbuffer and push the current event
	 * in the stack. If not it return the parameter
	 * 
	 * @param event
	 * @return
	 */
	private QuiXToken updateText(final QuiXToken event) {
		if (this.charBuffer.length() != 0) {
			// AQuiXEvent.getText(charBuffer.toString());
			this.charBuffer.setLength(0);
			// this.buffer.add(event);
			return QuiXToken.TEXT;
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
	public Object getProperty(final String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void require(final int type, final String namespaceURI, final String localName) {
		// TODO Auto-generated method stub

	}

	@Override
	public QuiXCharStream getElementText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int nextTag() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public QuiXCharStream getNamespaceURI(final String prefix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isStartElement() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEndElement() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCharacters() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isWhiteSpace() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getAttributeValue(final String namespaceURI, final String localName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getAttributeCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public QName getAttributeName(final int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAttributeNamespace(final int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAttributeLocalName(final int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAttributePrefix(final int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAttributeType(final int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAttributeValue(final int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAttributeSpecified(final int index) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getNamespaceCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getNamespacePrefix(final int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNamespaceURI(final int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NamespaceContext getNamespaceContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getEventType() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char[] getTextCharacters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getTextCharacters(final int sourceStart, final char[] target, final int targetStart, final int length) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getTextStart() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getTextLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasText() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Location getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QName getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasName() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getNamespaceURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPrefix() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isStandalone() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean standaloneSet() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getCharacterEncodingScheme() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPITarget() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPIData() {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(final String[] args) {
		final Iterable<Source> sources = Arrays
				.asList(new Source[] {
						new StreamSource(
								"/Users/innovimax/tmp/gs1/new/1000/1000_KO_22062015.xml"),
						new StreamSource(
								"/Users/innovimax/tmp/gs1/new/1000/1000_OK_22062015.xml") });
		final IQuiXStreamReader qesr = new QuiXStreamReader(sources);
		while (qesr.hasNext()) {
			System.out.println(qesr.next());
		}
	}

}
