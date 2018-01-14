/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.convert;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import innovimax.quixproc.datamodel.QuiXException;
import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.event.AQuiXEvent.Attribute;
import innovimax.quixproc.datamodel.event.AQuiXEvent.Namespace;
import innovimax.quixproc.datamodel.event.IQuiXEventStreamReader;

public class QuiXEventStream2XMLStreamReader implements XMLStreamReader {
	private final IQuiXEventStreamReader qs;
	private static final boolean DEBUG = false;
	private static final int POSITION = 1;

	public QuiXEventStream2XMLStreamReader(final IQuiXEventStreamReader qs) {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		this.qs = qs;
	}

	@Override
	public Object getProperty(final String name) {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasNext() throws XMLStreamException {

		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		if (this.future != null)
			return true;
		try {
			return this.qs.hasNext();
		} catch (final QuiXException e) {
			throw new XMLStreamException(e);
		}
	}

	private AQuiXEvent current = null;
	private AQuiXEvent future = null;
	private final List<Namespace> namespaces = new ArrayList<Namespace>();
	private final List<Attribute> attributes = new ArrayList<Attribute>();

	@Override
	public int next() throws XMLStreamException {
		try {
			if (DEBUG)
				System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
			if (DEBUG)
				System.out.println("QuixStreamReader.next : (" + this.future + "," + this.current + ")");
			while (this.future != null || this.qs.hasNext()) {
				if (this.future != null) {
					this.current = this.future;
					this.future = null;
				} else {
					this.current = this.qs.next();
				}
				switch (this.current.getType()) {
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
					this.attributes.clear();
					this.namespaces.clear();
					while (true) {
						final boolean test = this.qs.hasNext();
						if (!test)
							throw new QuiXException("Impossible");
						this.future = this.qs.next();
						if (this.future.isAttribute()) {
							this.attributes.add(this.future.asAttribute());
						} else if (this.future.isNamespace()) {
							this.namespaces.add(this.future.asNamespace());
						} else {
							break;
						}
					}
					return XMLStreamConstants.START_ELEMENT;
				case END_ELEMENT:
					return XMLStreamConstants.END_ELEMENT;
				case ATTRIBUTE: // This should never happen since attribute are
								// processed
					// DO NOTHING
					break;
				case COMMENT:
					return XMLStreamConstants.COMMENT;
				case PROCESSING_INSTRUCTION:
					return XMLStreamConstants.PROCESSING_INSTRUCTION;
				case TEXT:
					return XMLStreamConstants.CHARACTERS;
				default:
				}
			}
		} catch (final QuiXException e) {
			throw new XMLStreamException(e);

		}
		return XMLStreamConstants.END_DOCUMENT;
	}

	@Override
	public void require(final int type, final String namespaceURI, final String localName) {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		throw new RuntimeException("no such method");
	}

	@Override
	public String getElementText() throws XMLStreamException {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		if (getEventType() != XMLStreamConstants.START_ELEMENT) {
			throw new XMLStreamException("parser must be on START_ELEMENT to read next text", getLocation());
		}
		int eventType = next();
		final StringBuilder content = new StringBuilder();
		while (eventType != XMLStreamConstants.END_ELEMENT) {
			switch (eventType) {
				case XMLStreamConstants.CHARACTERS:
				case XMLStreamConstants.CDATA:
				case XMLStreamConstants.SPACE:
				case XMLStreamConstants.ENTITY_REFERENCE:
					content.append(getText());
					break;
				case XMLStreamConstants.PROCESSING_INSTRUCTION:
				case XMLStreamConstants.COMMENT:
					// skipping
					break;
				case XMLStreamConstants.END_DOCUMENT:
					throw new XMLStreamException("unexpected end of document when reading element text content",
							getLocation());
				case XMLStreamConstants.START_ELEMENT:
					throw new XMLStreamException("element text content may not contain START_ELEMENT", getLocation());
				default:
					throw new XMLStreamException("Unexpected event type " + eventType, getLocation());
			}
			eventType = next();
		}
		return content.toString();
	}

	@Override
	public int nextTag() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		throw new RuntimeException("no such method");
	}

	@Override
	public void close() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		this.qs.close();
	}

	@Override
	public String getNamespaceURI(final String prefix) {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isStartElement() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		return this.current.isStartElement();
	}

	@Override
	public boolean isEndElement() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		return this.current.isEndElement();
	}

	@Override
	public boolean isCharacters() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		return this.current.isText();
	}

	@Override
	public boolean isWhiteSpace() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		return false;
	}

	@Override
	public String getAttributeValue(final String namespaceURI, final String localName) {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		// TODO compare between String and QuiXCharStream
		return this.attributes.stream().filter(attribute -> localName.equals(attribute.getLocalName().toString()) && namespaceURI.equals(attribute.getURI().toString())).findFirst().map(attribute -> attribute.getValue().toString()).orElse(null);
	}

	@Override
	public int getAttributeCount() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		return this.attributes.size();
	}

	@Override
	public QName getAttributeName(final int index) {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		return this.attributes.get(index).getQName().asQName();
	}

	@Override
	public String getAttributeNamespace(final int index) {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		return this.attributes.get(index).getURI().toString();
	}

	@Override
	public String getAttributeLocalName(final int index) {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		return this.attributes.get(index).getLocalName().toString();
	}

	@Override
	public String getAttributePrefix(final int index) {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		return this.attributes.get(index).getPrefix().toString();
	}

	@Override
	public String getAttributeType(final int index) {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		return null; // no type stored
	}

	@Override
	public String getAttributeValue(final int index) {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		return this.attributes.get(index).getValue().toString();
	}

	@Override
	public boolean isAttributeSpecified(final int index) {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		return true;
	}

	@Override
	public int getNamespaceCount() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		return this.namespaces.size();
	}

	@Override
	public String getNamespacePrefix(final int index) {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		return this.namespaces.get(index).getPrefix().toString();
	}

	@Override
	public String getNamespaceURI(final int index) {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		// TODO Auto-generated method stub
		return this.namespaces.get(index).getURI().toString();
	}

	@Override
	public NamespaceContext getNamespaceContext() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getEventType() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		if (this.current == null)
			return XMLStreamConstants.START_DOCUMENT;
		switch (this.current.getType()) {
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
		case PROCESSING_INSTRUCTION:
			return XMLStreamConstants.PROCESSING_INSTRUCTION;
		case TEXT:
			return XMLStreamConstants.CHARACTERS;
		case START_SEQUENCE: // Not Possible
			break;
		case END_SEQUENCE: // Not Possible
			break;
		default:
		}
		return 0;
	}

	@Override
	public String getText() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		// Returns the current value of the parse event as a string,
		// this returns the string value of a CHARACTERS event,
		// returns the value of a COMMENT,
		// the replacement value for an ENTITY_REFERENCE,
		// the string value of a CDATA section,
		// the string value for a SPACE event,
		// or the String value of the internal subset of the DTD.
		// If an ENTITY_REFERENCE has been resolved, any character data will be
		// reported as CHARACTERS events.
		switch (this.current.getType()) {
		case TEXT:
			return this.current.asText().getData().toString();
		case COMMENT:
			return this.current.asComment().getData().toString();
		default:
		}
		return null;
	}

	@Override
	public char[] getTextCharacters() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		final String text = getText();
		return text == null ? null : text.toCharArray();
	}

	@Override
	public int getTextCharacters(final int sourceStart, final char[] target, final int targetStart, final int length) {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName() + "+++");
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getTextStart() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getTextLength() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		// TODO Auto-generated method stub
		return getText().length();
	}

	@Override
	public String getEncoding() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasText() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		// return true if the current event has text, false otherwise
		// The following events have text:
		// CHARACTERS,DTD
		// ,ENTITY_REFERENCE, COMMENT, SPACE
		// TODO Auto-generated method stub
		return this.current.isText() || this.current.isComment();
	}

	@Override
	public Location getLocation() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
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
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		switch (this.current.getType()) {
		case START_ELEMENT:
		case END_ELEMENT:
			return this.current.asNamedEvent().getQName().asQName();
		default:
		}
		return null;
	}

	@Override
	public String getLocalName() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		switch (this.current.getType()) {
		case START_ELEMENT:
		case END_ELEMENT:
			return this.current.asNamedEvent().getLocalName().toString();
		default:
		}
		return null;
	}

	@Override
	public boolean hasName() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		return this.current.isStartElement() || this.current.isEndElement();
	}

	@Override
	public String getNamespaceURI() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		switch (this.current.getType()) {
		case START_ELEMENT:
		case END_ELEMENT:
			return this.current.asNamedEvent().getURI().toString();
		default:
		}
		return null;
	}

	@Override
	public String getPrefix() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		switch (this.current.getType()) {
		case START_ELEMENT:
		case END_ELEMENT:
			return this.current.asNamedEvent().getPrefix().toString();
		default:
		}
		return null;
	}

	@Override
	public String getVersion() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isStandalone() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean standaloneSet() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getCharacterEncodingScheme() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPITarget() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		return this.current.asPI().getTarget().toString();
	}

	@Override
	public String getPIData() {
		if (DEBUG)
			System.out.println(Thread.currentThread().getStackTrace()[POSITION].getMethodName());
		return this.current.asPI().getData().toString();
	}

}