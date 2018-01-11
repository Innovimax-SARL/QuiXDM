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
	static long createDocCount = 0;
	static long createAttrCount = 0;

	protected final QuiXToken type;

	/* constructors */
	AQuiXEvent(QuiXToken type) {
		this.type = type;
		createCount++;
	}

	public abstract static class AXMLQuiXEvent extends AQuiXEvent {
		AXMLQuiXEvent(QuiXToken token) {
			super(token);
		}
	}

	public abstract static class AJSONQuiXEvent extends AQuiXEvent {
		AJSONQuiXEvent(QuiXToken token) {
			super(token);
		}
	}

	public abstract static class ARDFQuiXEvent extends AQuiXEvent {
		ARDFQuiXEvent(QuiXToken token) {
			super(token);
		}
	}

	public abstract static class ACSVQuiXEvent extends AQuiXEvent {
		ACSVQuiXEvent(QuiXToken token) {
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

		StartDocument(QuiXCharStream uri) {
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

		EndDocument(QuiXCharStream uri) {
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

	public static class StartJSON extends AJSONQuiXEvent {
		public StartJSON() {
			super(QuiXToken.START_JSON);
		}
	}

	public static class EndJSON extends AJSONQuiXEvent {
		public EndJSON() {
			super(QuiXToken.END_JSON);
		}
	}

	public static class StartObject extends AJSONQuiXEvent {
		public StartObject() {
			super(QuiXToken.START_OBJECT);
		}
	}

	public static class EndObject extends AJSONQuiXEvent {
		public EndObject() {
			super(QuiXToken.END_OBJECT);
		}
	}

	// start array is in JSON/YAML but also in CSV/TSV
	public static class StartArray extends AQuiXEvent {
		public StartArray() {
			super(QuiXToken.START_ARRAY);
		}
	}

	// start array is in JSON/YAML but also in CSV/TSV
	public static class EndArray extends AQuiXEvent {
		public EndArray() {
			super(QuiXToken.END_ARRAY);
		}
	}

	// Table CSV / TSV

	public static class StartTable extends ACSVQuiXEvent {
		public StartTable() {
			super(QuiXToken.START_TABLE);
		}
	}

	public static class EndTable extends ACSVQuiXEvent {
		public EndTable() {
			super(QuiXToken.END_TABLE);
		}
	}

	// RDF Triple

	public static class StartRDF extends ARDFQuiXEvent {
		public StartRDF() {
			super(QuiXToken.START_RDF);
		}
	}

	public static class EndRDF extends ARDFQuiXEvent {
		public EndRDF() {
			super(QuiXToken.END_RDF);
		}
	}

	public static class StartPredicate extends ARDFQuiXEvent {
		final QuiXCharStream name;

		public StartPredicate(QuiXCharStream name) {
			super(QuiXToken.START_PREDICATE);
			this.name = name;
		}
	}

	public static class EndPredicate extends ARDFQuiXEvent {
		final QuiXCharStream name;

		public EndPredicate(QuiXCharStream name) {
			super(QuiXToken.END_PREDICATE);
			this.name = name;
		}
	}

	public static class Subject extends ARDFQuiXEvent {
		final QuiXCharStream name;

		public Subject(QuiXCharStream name) {
			super(QuiXToken.SUBJECT);
			this.name = name;
		}
	}

	public static class Object extends ARDFQuiXEvent {
		final QuiXCharStream name;

		public Object(QuiXCharStream name) {
			super(QuiXToken.OBJECT);
			this.name = name;
		}
	}

	public static class Graph extends ARDFQuiXEvent {
		final QuiXCharStream name;

		public Graph(QuiXCharStream name) {
			super(QuiXToken.GRAPH);
			this.name = name;
		}
	}

	// JSON

	public static class AJSONValue extends AJSONQuiXEvent {
		AJSONValue(QuiXToken token) {
			super(token);
		}
	}

	public static class ValueNull extends AJSONValue {
		public ValueNull() {
			super(QuiXToken.VALUE_NULL);
		}
	}

	public static class ValueTrue extends AJSONValue {
		public ValueTrue() {
			super(QuiXToken.VALUE_TRUE);
		}
	}

	public static class ValueFalse extends AJSONValue {
		public ValueFalse() {
			super(QuiXToken.VALUE_FALSE);
		}
	}

	public static class ValueNumber extends AJSONValue {
		final double value;

		public ValueNumber(double value) {
			super(QuiXToken.VALUE_NUMBER);
			this.value = value;
		}
	}

	public static class ValueString extends AJSONValue {
		final QuiXCharStream value;

		public ValueString(QuiXCharStream value) {
			super(QuiXToken.VALUE_STRING);
			this.value = value;
		}
	}

	public static class KeyName extends AJSONQuiXEvent {
		private final QuiXCharStream name;

		KeyName(QuiXCharStream name) {
			super(QuiXToken.KEY_NAME);
			this.name = name;
		}
	}

	public static final class Namespace extends AXMLQuiXEvent {
		private final QuiXCharStream prefix;
		private final QuiXCharStream uri;

		Namespace(QuiXCharStream prefix, QuiXCharStream uri) {
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

		NamedEvent(QuiXQName qname, QuiXToken type) {
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
		StartElement(QuiXQName qname) {
			super(qname, QuiXToken.START_ELEMENT);
			// System.out.println("START ELEMENT"+localName);
		}

		@Override
		public String toString() {
			return this.type + " " + getLocalName();
		}
	}

	public static final class EndElement extends NamedEvent {
		EndElement(QuiXQName qname) {
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

		Attribute(QuiXQName qname, QuiXCharStream value) {
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

		Text(QuiXCharStream data) {
			super(QuiXToken.TEXT);
			this.data = data;
			// System.out.println("TEXT");
		}

		public QuiXCharStream getData() {
			return this.data;
		}

		@Override
		public String toString() {
			return this.type + " " + getData();
		}
	}

	public static final class PI extends AXMLQuiXEvent {
		private final QuiXCharStream target;
		private final QuiXCharStream data;

		PI(QuiXCharStream target, QuiXCharStream data) {
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
			return this.type + " " + getTarget();
		}
	}

	public static final class Comment extends AXMLQuiXEvent {
		private final QuiXCharStream data;

		Comment(QuiXCharStream data) {
			super(QuiXToken.COMMENT);
			this.data = data;
		}

		public QuiXCharStream getData() {
			return this.data;
		}

		@Override
		public String toString() {
			return this.type + " " + getData();
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

	private static StartSequence newStartSequence = SEQUENCE_CACHING_ENABLED ? new StartSequence() : null;

	public static AQuiXEvent getStartSequence() {
		createCallCount++;
		StartSequence result;
		result = SEQUENCE_CACHING_ENABLED ? newStartSequence : new StartSequence();
		return result;
	}

	private static EndSequence newEndSequence = SEQUENCE_CACHING_ENABLED ? new EndSequence() : null;

	public static AQuiXEvent getEndSequence() {
		createCallCount++;
		EndSequence result;
		result = SEQUENCE_CACHING_ENABLED ? newEndSequence : new EndSequence();
		return result;
	}

	private static Map<QuiXCharStream, StartDocument> startDocumentMap = DOCUMENT_CACHING_ENABLED
			? new HashMap<QuiXCharStream, StartDocument>() : null;

	public static AXMLQuiXEvent getStartDocument(QuiXCharStream uri) {
		createCallCount++;
		StartDocument result;
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
			result = new StartDocument(uri);
		}
		return result;
	}

	private static Map<QuiXCharStream, EndDocument> endDocumentMap = DOCUMENT_CACHING_ENABLED
			? new HashMap<QuiXCharStream, EndDocument>() : null;

	public static AXMLQuiXEvent getEndDocument(QuiXCharStream uri) {
		createCallCount++;
		EndDocument result;
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
			result = new EndDocument(uri);
		}
		return result;
	}

	public static Namespace getNamespace(QuiXCharStream prefix, QuiXCharStream uri) {
		return new Namespace(prefix == null ? QuiXCharStream.EMPTY : prefix, uri);
	}

	private static Map<QuiXCharStream, QuiXQName> qNameMap = NAME_CACHING_ENABLED
			? new HashMap<QuiXCharStream, QuiXQName>() : null;

	private static QuiXQName getQName(QuiXCharStream localName, QuiXCharStream namespace, QuiXCharStream pref) {
		QuiXQName result;
		QuiXCharStream uri = namespace == null ? QuiXCharStream.EMPTY : namespace;
		QuiXCharStream prefix = pref == null ? QuiXCharStream.EMPTY : pref;
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
			result = new QuiXQName(uri, localName, prefix);
		}
		return result;
	}

	private static Map<QuiXQName, StartElement> startElementMap = ELEMENT_CACHING_ENABLED
			? new HashMap<QuiXQName, StartElement>() : null;

	public static AXMLQuiXEvent getStartElement(QuiXCharStream qName, QuiXCharStream namespace) {
		QuiXCharStream localName = qName;
		QuiXCharStream prefix = null;
		if (qName.contains(":")) {
			prefix = qName.substringBefore(":");
			localName = qName.substringAfter(":");
		}
		return getStartElement(localName, namespace, prefix);
	}

	public static AXMLQuiXEvent getStartElement(QuiXCharStream localName, QuiXCharStream namespace,
			QuiXCharStream prefix) {
		createCallCount++;
		StartElement result;
		QuiXQName qname = getQName(localName, namespace, prefix);
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
			result = new StartElement(qname);
		}
		return result;
	}

	private static Map<QuiXQName, EndElement> endElementMap = ELEMENT_CACHING_ENABLED
			? new HashMap<QuiXQName, EndElement>() : null;

	public static AXMLQuiXEvent getEndElement(QuiXCharStream qName, QuiXCharStream namespace) {
		QuiXCharStream localName = qName;
		QuiXCharStream prefix = null;
		if (qName.contains(":")) {
			prefix = qName.substringBefore(":");
			localName = qName.substringAfter(":");
		}
		return getEndElement(localName, namespace, prefix);
	}

	public static AXMLQuiXEvent getEndElement(QuiXCharStream localName, QuiXCharStream namespace,
			QuiXCharStream prefix) {
		createCallCount++;
		EndElement result;
		QuiXQName qname = getQName(localName, namespace, prefix);
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
			result = new EndElement(qname);
		}
		return result;
	}

	public static AXMLQuiXEvent getAttribute(QuiXCharStream qName, QuiXCharStream namespace, QuiXCharStream value) {
		QuiXCharStream localName = qName;
		QuiXCharStream prefix = null;
		if (qName.contains(":")) {
			prefix = qName.substringBefore(":");
			localName = qName.substringAfter(":");
		}
		return getAttribute(localName, namespace, prefix, value);
	}

	public static AXMLQuiXEvent getAttribute(QuiXCharStream localName, QuiXCharStream namespace, QuiXCharStream prefix,
			QuiXCharStream value) {
		createCallCount++;
		return new Attribute(getQName(localName, namespace, prefix), value);
	}

	public static AXMLQuiXEvent getText(QuiXCharStream text) {
		createCallCount++;
		return new Text(text);
	}

	public static AXMLQuiXEvent getPI(QuiXCharStream target, QuiXCharStream data) {
		createCallCount++;
		return new PI(target, data);
	}

	public static AXMLQuiXEvent getComment(QuiXCharStream comment) {
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

	public static AJSONQuiXEvent getKeyName(QuiXCharStream name) {
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

	public static AJSONQuiXEvent getValueNumber(double number) {
		return new ValueNumber(number);
	}

	public static AJSONQuiXEvent getValueString(QuiXCharStream str) {
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

	public static ARDFQuiXEvent getStartPredicate(QuiXCharStream name) {
		return new StartPredicate(name);
	}

	public static ARDFQuiXEvent getEndPredicate(QuiXCharStream name) {
		return new EndPredicate(name);
	}

	public static ARDFQuiXEvent getSubject(QuiXCharStream name) {
		return new Subject(name);
	}

	public static ARDFQuiXEvent getObject(QuiXCharStream name) {
		return new Object(name);
	}

	public static ARDFQuiXEvent getGraph(QuiXCharStream name) {
		return new Graph(name);
	}

	/* utilities */

	public boolean isStartSequence() {
		return (this.type == QuiXToken.START_SEQUENCE);
	}

	public boolean isEndSequence() {
		return (this.type == QuiXToken.END_SEQUENCE);
	}

	public boolean isStartDocument() {
		return (this.type == QuiXToken.START_DOCUMENT);
	}

	public boolean isEndDocument() {
		return (this.type == QuiXToken.END_DOCUMENT);
	}

	public boolean isStartElement() {
		return (this.type == QuiXToken.START_ELEMENT);
	}

	public boolean isEndElement() {
		return (this.type == QuiXToken.END_ELEMENT);
	}

	public boolean isAttribute() {
		return (this.type == QuiXToken.ATTRIBUTE);
	}

	public boolean isText() {
		return (this.type == QuiXToken.TEXT);
	}

	public boolean isPI() {
		return (this.type == QuiXToken.PROCESSING_INSTRUCTION);
	}

	public boolean isComment() {
		return (this.type == QuiXToken.COMMENT);
	}

	public boolean isNamespace() {
		return (this.type == QuiXToken.NAMESPACE);
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
