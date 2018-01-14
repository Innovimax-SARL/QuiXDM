/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.event;

import java.util.HashMap;
import java.util.Map;

import innovimax.quixproc.datamodel.IQuiXToken;
import innovimax.quixproc.datamodel.QuiXCharStream;
import innovimax.quixproc.datamodel.QuiXQName;
import innovimax.quixproc.datamodel.QuiXToken;

public abstract class AQuiXEvent implements IQuiXEvent, IQuiXToken {

	// TODO : store namespace context
	// TODO : store type information for PSVI

	// to enable CACHING for specific kind of type
	private static final boolean SEQUENCE_CACHING_ENABLED = false;
	private static final boolean DOCUMENT_CACHING_ENABLED = false;
	private static final boolean ELEMENT_CACHING_ENABLED = false;
	private static final boolean NAME_CACHING_ENABLED = false;

	private static long createCount = 0;
	private static long createCallCount = 0;
	private static long createDocCount = 0;
	private static long createAttrCount = 0;

	final QuiXToken type;

	/* constructors */
	AQuiXEvent(final QuiXToken type) {
		this.type = type;
		createCount++;
	}

	public abstract static class AXMLQuiXEvent extends AQuiXEvent {
		AXMLQuiXEvent(final QuiXToken token) {
			super(token);
		}
	}

	public abstract static class AJSONQuiXEvent extends AQuiXEvent {
		AJSONQuiXEvent(final QuiXToken token) {
			super(token);
		}
	}

	public abstract static class ARDFQuiXEvent extends AQuiXEvent {
		ARDFQuiXEvent(final QuiXToken token) {
			super(token);
		}
	}

	public abstract static class ACSVQuiXEvent extends AQuiXEvent {
		ACSVQuiXEvent(final QuiXToken token) {
			super(token);
		}
	}

	public static final class StartSequence extends AQuiXEvent {
		StartSequence() {
			super(QuiXToken.START_SEQUENCE);
		}

		@Override
		public String toString() {
			return this.type.toString();
		}
	}

	public static final class EndSequence extends AQuiXEvent {
		EndSequence() {
			super(QuiXToken.END_SEQUENCE);
		}

		@Override
		public String toString() {
			return this.type.toString();
		}
	}

	public static final class StartDocument extends AXMLQuiXEvent {
		private final QuiXCharStream uri;

		StartDocument(final QuiXCharStream uri) {
			super(QuiXToken.START_DOCUMENT);
			this.uri = uri;
			createDocCount++;
			// System.out.println("START DOCUMENT"+uri);
		}

		public QuiXCharStream getURI() {
			return this.uri;
		}

		@Override
		public String toString() {
			return this.type + " " + this.uri;
		}
	}

	public static final class EndDocument extends AXMLQuiXEvent {
		private final QuiXCharStream uri;

		EndDocument(final QuiXCharStream uri) {
			super(QuiXToken.END_DOCUMENT);
			this.uri = uri;
			createDocCount++;
			// System.out.println("END DOCUMENT"+uri);
		}

		public QuiXCharStream getURI() {
			return this.uri;
		}

		@Override
		public String toString() {
			return this.type + " " + this.uri;
		}
	}

	static class StartJSON extends AJSONQuiXEvent {
		StartJSON() {
			super(QuiXToken.START_JSON);
		}
	}

	static class EndJSON extends AJSONQuiXEvent {
		EndJSON() {
			super(QuiXToken.END_JSON);
		}
	}

	static class StartObject extends AJSONQuiXEvent {
		StartObject() {
			super(QuiXToken.START_OBJECT);
		}
	}

	static class EndObject extends AJSONQuiXEvent {
		EndObject() {
			super(QuiXToken.END_OBJECT);
		}
	}

	// start array is in JSON/YAML but also in CSV/TSV
	static class StartArray extends AQuiXEvent {
		StartArray() {
			super(QuiXToken.START_ARRAY);
		}
	}

	// start array is in JSON/YAML but also in CSV/TSV
	static class EndArray extends AQuiXEvent {
		EndArray() {
			super(QuiXToken.END_ARRAY);
		}
	}

	// Table CSV / TSV

	static class StartTable extends ACSVQuiXEvent {
		StartTable() {
			super(QuiXToken.START_TABLE);
		}
	}

	static class EndTable extends ACSVQuiXEvent {
		EndTable() {
			super(QuiXToken.END_TABLE);
		}
	}

	// RDF Triple

	static class StartRDF extends ARDFQuiXEvent {
		StartRDF() {
			super(QuiXToken.START_RDF);
		}
	}

	static class EndRDF extends ARDFQuiXEvent {
		EndRDF() {
			super(QuiXToken.END_RDF);
		}
	}

	static class StartPredicate extends ARDFQuiXEvent {
		final QuiXCharStream name;

		StartPredicate(final QuiXCharStream name) {
			super(QuiXToken.START_PREDICATE);
			this.name = name;
		}
	}

	static class EndPredicate extends ARDFQuiXEvent {
		final QuiXCharStream name;

		EndPredicate(final QuiXCharStream name) {
			super(QuiXToken.END_PREDICATE);
			this.name = name;
		}
	}

	static class Subject extends ARDFQuiXEvent {
		final QuiXCharStream name;

		Subject(final QuiXCharStream name) {
			super(QuiXToken.SUBJECT);
			this.name = name;
		}
	}

	static class Object extends ARDFQuiXEvent {
		final QuiXCharStream name;

		Object(final QuiXCharStream name) {
			super(QuiXToken.OBJECT);
			this.name = name;
		}
	}

	static class Graph extends ARDFQuiXEvent {
		final QuiXCharStream name;

		Graph(final QuiXCharStream name) {
			super(QuiXToken.GRAPH);
			this.name = name;
		}
	}

	// JSON

	static class AJSONValue extends AJSONQuiXEvent {
		AJSONValue(final QuiXToken token) {
			super(token);
		}
	}

	static class ValueNull extends AJSONValue {
		ValueNull() {
			super(QuiXToken.VALUE_NULL);
		}
	}

	static class ValueTrue extends AJSONValue {
		ValueTrue() {
			super(QuiXToken.VALUE_TRUE);
		}
	}

	static class ValueFalse extends AJSONValue {
		ValueFalse() {
			super(QuiXToken.VALUE_FALSE);
		}
	}

	static class ValueNumber extends AJSONValue {
		final double value;

		ValueNumber(final double value) {
			super(QuiXToken.VALUE_NUMBER);
			this.value = value;
		}
	}

	static class ValueString extends AJSONValue {
		final QuiXCharStream value;

		ValueString(final QuiXCharStream value) {
			super(QuiXToken.VALUE_STRING);
			this.value = value;
		}
	}

	static class KeyName extends AJSONQuiXEvent {
		private final QuiXCharStream name;

		KeyName(final QuiXCharStream name) {
			super(QuiXToken.KEY_NAME);
			this.name = name;
		}
	}

	public static final class Namespace extends AXMLQuiXEvent {
		private final QuiXCharStream prefix;
		private final QuiXCharStream uri;

		Namespace(final QuiXCharStream prefix, final QuiXCharStream uri) {
			super(QuiXToken.NAMESPACE);
			this.prefix = prefix;
			this.uri = uri;
		}

		public QuiXCharStream getPrefix() {
			return this.prefix;
		}

		public QuiXCharStream getURI() {
			return this.uri;
		}

		@Override
		public String toString() {
			return this.type + " " + this.prefix + ":" + this.uri;
		}
	}

	public abstract static class NamedEvent extends AXMLQuiXEvent {
		private final QuiXQName qname;

		NamedEvent(final QuiXQName qname, final QuiXToken type) {
			super(type);
			this.qname = qname;
		}

		public QuiXCharStream getLocalName() {
			return this.qname.getLocalPart();
		}

		public QuiXCharStream getFullName() {
			return (this.qname.getPrefix().isEmpty() ? QuiXCharStream.EMPTY : this.qname.getPrefix().append(":"))
					.append(this.qname.getLocalPart());
		}

		public QuiXCharStream getURI() {
			return this.qname.getNamespaceURI();
		}

		public QuiXCharStream getPrefix() {
			return this.qname.getPrefix();
		}

		public QuiXQName getQName() {
			return this.qname;
		}
	}

	public static final class StartElement extends NamedEvent {
		StartElement(final QuiXQName qname) {
			super(qname, QuiXToken.START_ELEMENT);
			// System.out.println("START ELEMENT"+localName);
		}

		@Override
		public String toString() {
			return this.type + " " + getLocalName();
		}
	}

	public static final class EndElement extends NamedEvent {
		EndElement(final QuiXQName qname) {
			super(qname, QuiXToken.END_ELEMENT);
			// System.out.println("END ELEMENT" + localName);
		}

		@Override
		public String toString() {
			return this.type + " " + getLocalName();
		}
	}

	public static final class Attribute extends NamedEvent {
		private final QuiXCharStream value;

		Attribute(final QuiXQName qname, final QuiXCharStream value) {
			super(qname, QuiXToken.ATTRIBUTE);
			this.value = value;
			createAttrCount++;
			// System.out.println("ATTRIBUTE");
		}

		public QuiXCharStream getValue() {
			return this.value;
		}

		@Override
		public String toString() {
			return this.type + " " + getLocalName();
		}

	}

	public static final class Text extends AXMLQuiXEvent {
		private final QuiXCharStream data;

		Text(final QuiXCharStream data) {
			super(QuiXToken.TEXT);
			this.data = data;
			// System.out.println("TEXT");
		}

		public QuiXCharStream getData() {
			return this.data;
		}

		@Override
		public String toString() {
			return this.type + " " + this.data;
		}
	}

	public static final class PI extends AXMLQuiXEvent {
		private final QuiXCharStream target;
		private final QuiXCharStream data;

		PI(final QuiXCharStream target, final QuiXCharStream data) {
			super(QuiXToken.PROCESSING_INSTRUCTION);
			this.target = target;
			this.data = data;
		}

		public QuiXCharStream getTarget() {
			return this.target;
		}

		public QuiXCharStream getData() {
			return this.data;
		}

		@Override
		public String toString() {
			return this.type + " " + this.target;
		}
	}

	public static final class Comment extends AXMLQuiXEvent {
		private final QuiXCharStream data;

		Comment(final QuiXCharStream data) {
			super(QuiXToken.COMMENT);
			this.data = data;
		}

		public QuiXCharStream getData() {
			return this.data;
		}

		@Override
		public String toString() {
			return this.type + " " + this.data;
		}
	}

	public NamedEvent asNamedEvent() {
		return (NamedEvent) this;
	}

	public StartSequence asStartSequence() {
		return (StartSequence) this;
	}

	public EndSequence asEndSequence() {
		return (EndSequence) this;
	}

	public Namespace asNamespace() {
		return (Namespace) this;
	}

	public StartElement asStartElement() {
		return (StartElement) this;
	}

	public EndElement asEndElement() {
		return (EndElement) this;
	}

	public StartDocument asStartDocument() {
		return (StartDocument) this;
	}

	public EndDocument asEndDocument() {
		return (EndDocument) this;
	}

	public Text asText() {
		return (Text) this;
	}

	public Comment asComment() {
		return (Comment) this;
	}

	public PI asPI() {
		return (PI) this;
	}

	public Attribute asAttribute() {
		return (Attribute) this;
	}

	@Override
	public QuiXToken getType() {
		return this.type;
	}

	/* get typed event */

	private static final StartSequence newStartSequence = SEQUENCE_CACHING_ENABLED ? new StartSequence() : null;

	public static AQuiXEvent getStartSequence() {
		createCallCount++;
		return SEQUENCE_CACHING_ENABLED ? newStartSequence : new StartSequence();
	}

	private static final EndSequence newEndSequence = SEQUENCE_CACHING_ENABLED ? new EndSequence() : null;

	public static AQuiXEvent getEndSequence() {
		createCallCount++;
		return SEQUENCE_CACHING_ENABLED ? newEndSequence : new EndSequence();
	}

	private static final Map<QuiXCharStream, StartDocument> startDocumentMap = DOCUMENT_CACHING_ENABLED
			? new HashMap<QuiXCharStream, StartDocument>() : null;

	public static AXMLQuiXEvent getStartDocument(final QuiXCharStream uri) {
		createCallCount++;
		final StartDocument result;
		if (DOCUMENT_CACHING_ENABLED) {
			synchronized (startDocumentMap) {
				if (startDocumentMap.containsKey(uri)) {
					result = startDocumentMap.get(uri);
				} else {
					result = new StartDocument(uri);
					startDocumentMap.put(uri, result);
				}
			}
		} else {
			return new StartDocument(uri);
		}
		return result;
	}

	private static final Map<QuiXCharStream, EndDocument> endDocumentMap = DOCUMENT_CACHING_ENABLED
			? new HashMap<QuiXCharStream, EndDocument>() : null;

	public static AXMLQuiXEvent getEndDocument(final QuiXCharStream uri) {
		createCallCount++;
		final EndDocument result;
		if (DOCUMENT_CACHING_ENABLED) {
			synchronized (endDocumentMap) {
				if (endDocumentMap.containsKey(uri)) {
					result = endDocumentMap.get(uri);
				} else {
					result = new EndDocument(uri);
					endDocumentMap.put(uri, result);
				}
			}
		} else {
			return new EndDocument(uri);
		}
		return result;
	}

	public static Namespace getNamespace(final QuiXCharStream prefix, final QuiXCharStream uri) {
		return new Namespace(prefix == null ? QuiXCharStream.EMPTY : prefix, uri);
	}

	private static final Map<QuiXCharStream, QuiXQName> qNameMap = NAME_CACHING_ENABLED
			? new HashMap<QuiXCharStream, QuiXQName>() : null;

	private static QuiXQName getQName(final QuiXCharStream localName, final QuiXCharStream namespace, final QuiXCharStream pref) {
		final QuiXQName result;
		final QuiXCharStream uri = namespace == null ? QuiXCharStream.EMPTY : namespace;
		final QuiXCharStream prefix = pref == null ? QuiXCharStream.EMPTY : pref;
		if (NAME_CACHING_ENABLED) {
			QuiXCharStream key = localName;
			// here the prefix plays no role
			// if (prefix.length() > 0) key = prefix + ":" + localName;
			if (!uri.isEmpty())
				key = QuiXCharStream.fromSequence("{").append(uri).append("}").append(key);
			synchronized (qNameMap) {
				if (qNameMap.containsKey(key)) {
					result = qNameMap.get(key);
				} else {
					result = new QuiXQName(uri, localName, prefix);
					qNameMap.put(key, result);
				}
			}
		} else {
			return new QuiXQName(uri, localName, prefix);
		}
		return result;
	}

	private static final Map<QuiXQName, StartElement> startElementMap = ELEMENT_CACHING_ENABLED
			? new HashMap<QuiXQName, StartElement>() : null;

	public static AXMLQuiXEvent getStartElement(final QuiXCharStream qName, final QuiXCharStream namespace) {
		QuiXCharStream localName = qName;
		QuiXCharStream prefix = null;
		if (qName.contains(":")) {
			prefix = qName.substringBefore(":");
			localName = qName.substringAfter(":");
		}
		return getStartElement(localName, namespace, prefix);
	}

	public static AXMLQuiXEvent getStartElement(final QuiXCharStream localName, final QuiXCharStream namespace,
                                                final QuiXCharStream prefix) {
		createCallCount++;
		final StartElement result;
		final QuiXQName qname = getQName(localName, namespace, prefix);
		if (ELEMENT_CACHING_ENABLED) {
			synchronized (startElementMap) {
				if (startElementMap.containsKey(qname)) {
					result = startElementMap.get(qname);
				} else {
					result = new StartElement(qname);
					startElementMap.put(qname, result);
				}
			}
		} else {
			return new StartElement(qname);
		}
		return result;
	}

	private static final Map<QuiXQName, EndElement> endElementMap = ELEMENT_CACHING_ENABLED
			? new HashMap<QuiXQName, EndElement>() : null;

	public static AXMLQuiXEvent getEndElement(final QuiXCharStream qName, final QuiXCharStream namespace) {
		QuiXCharStream localName = qName;
		QuiXCharStream prefix = null;
		if (qName.contains(":")) {
			prefix = qName.substringBefore(":");
			localName = qName.substringAfter(":");
		}
		return getEndElement(localName, namespace, prefix);
	}

	public static AXMLQuiXEvent getEndElement(final QuiXCharStream localName, final QuiXCharStream namespace,
                                              final QuiXCharStream prefix) {
		createCallCount++;
		final EndElement result;
		final QuiXQName qname = getQName(localName, namespace, prefix);
		if (ELEMENT_CACHING_ENABLED) {
			synchronized (endElementMap) {
				if (endElementMap.containsKey(qname)) {
					result = endElementMap.get(qname);
				} else {
					result = new EndElement(qname);
					endElementMap.put(qname, result);
				}
			}
		} else {
			return new EndElement(qname);
		}
		return result;
	}

	public static AXMLQuiXEvent getAttribute(final QuiXCharStream qName, final QuiXCharStream namespace, final QuiXCharStream value) {
		QuiXCharStream localName = qName;
		QuiXCharStream prefix = null;
		if (qName.contains(":")) {
			prefix = qName.substringBefore(":");
			localName = qName.substringAfter(":");
		}
		return getAttribute(localName, namespace, prefix, value);
	}

	public static AXMLQuiXEvent getAttribute(final QuiXCharStream localName, final QuiXCharStream namespace, final QuiXCharStream prefix,
                                             final QuiXCharStream value) {
		createCallCount++;
		return new Attribute(getQName(localName, namespace, prefix), value);
	}

	public static AXMLQuiXEvent getText(final QuiXCharStream text) {
		createCallCount++;
		return new Text(text);
	}

	public static AXMLQuiXEvent getPI(final QuiXCharStream target, final QuiXCharStream data) {
		createCallCount++;
		return new PI(target, data);
	}

	public static AXMLQuiXEvent getComment(final QuiXCharStream comment) {
		createCallCount++;
		return new Comment(comment);
	}

	// JSON

	public static AJSONQuiXEvent getStartJSON() {
		return new StartJSON();
	}

	public static AJSONQuiXEvent getEndJSON() {
		return new EndJSON();
	}

	public static AJSONQuiXEvent getStartObject() {
		return new StartObject();
	}

	public static AJSONQuiXEvent getEndObject() {
		return new EndObject();
	}

	public static AQuiXEvent getStartArray() {
		return new StartArray();
	}

	public static AQuiXEvent getEndArray() {
		return new EndArray();
	}

	public static AJSONQuiXEvent getKeyName(final QuiXCharStream name) {
		return new KeyName(name);
	}

	public static AJSONQuiXEvent getValueNull() {
		return new ValueNull();
	}

	public static AJSONQuiXEvent getValueTrue() {
		return new ValueTrue();
	}

	public static AJSONQuiXEvent getValueFalse() {
		return new ValueFalse();
	}

	public static AJSONQuiXEvent getValueNumber(final double number) {
		return new ValueNumber(number);
	}

	public static AJSONQuiXEvent getValueString(final QuiXCharStream str) {
		return new ValueString(str);
	}

	// CSV/TSV

	public static ACSVQuiXEvent getStartTable() {
		return new StartTable();
	}

	public static ACSVQuiXEvent getEndTable() {
		return new EndTable();
	}

	// RDF

	public static ARDFQuiXEvent getStartRDF() {
		return new StartRDF();
	}

	public static ARDFQuiXEvent getEndRDF() {
		return new EndRDF();
	}

	public static ARDFQuiXEvent getStartPredicate(final QuiXCharStream name) {
		return new StartPredicate(name);
	}

	public static ARDFQuiXEvent getEndPredicate(final QuiXCharStream name) {
		return new EndPredicate(name);
	}

	public static ARDFQuiXEvent getSubject(final QuiXCharStream name) {
		return new Subject(name);
	}

	public static ARDFQuiXEvent getObject(final QuiXCharStream name) {
		return new Object(name);
	}

	public static ARDFQuiXEvent getGraph(final QuiXCharStream name) {
		return new Graph(name);
	}

	/* utilities */

	public boolean isStartSequence() {
		return this.type == QuiXToken.START_SEQUENCE;
	}

	public boolean isEndSequence() {
		return this.type == QuiXToken.END_SEQUENCE;
	}

	public boolean isStartDocument() {
		return this.type == QuiXToken.START_DOCUMENT;
	}

	public boolean isEndDocument() {
		return this.type == QuiXToken.END_DOCUMENT;
	}

	public boolean isStartElement() {
		return this.type == QuiXToken.START_ELEMENT;
	}

	public boolean isEndElement() {
		return this.type == QuiXToken.END_ELEMENT;
	}

	public boolean isAttribute() {
		return this.type == QuiXToken.ATTRIBUTE;
	}

	public boolean isText() {
		return this.type == QuiXToken.TEXT;
	}

	public boolean isPI() {
		return this.type == QuiXToken.PROCESSING_INSTRUCTION;
	}

	public boolean isComment() {
		return this.type == QuiXToken.COMMENT;
	}

	public boolean isNamespace() {
		return this.type == QuiXToken.NAMESPACE;
	}

	@Override
	public AQuiXEvent getEvent() {
		return this;
	}

	/* debugging */

	public static long getCreateCount() {
		return createCount;
	}

	public static long getCreateDocCount() {
		return createDocCount;
	}

	public static long getCreateAttrCount() {
		return createAttrCount;
	}

	public static long getCreateCallCount() {
		return createCallCount;
	}

	public static void resetCreateCount() {
		createCount = 0;
		createDocCount = 0;
		createAttrCount = 0;
		createCallCount = 0;
	}

}
