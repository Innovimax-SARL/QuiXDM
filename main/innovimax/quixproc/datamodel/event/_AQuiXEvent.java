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
package innovimax.quixproc.datamodel.event;

import java.util.HashMap;
import java.util.Map;

import innovimax.quixproc.datamodel.QuiXCharStream;
import innovimax.quixproc.datamodel.QuiXQName;
import innovimax.quixproc.datamodel.QuiXToken;

public abstract class AQuiXEvent implements IQuiXEvent {

	// TODO : store namespacecontext
	// TODO : store type information for PSVI

	// to enable CACHING for specific kind of type
	private final static boolean SEQUENCE_CACHING_ENABLED = false;
	private final static boolean DOCUMENT_CACHING_ENABLED = false;
	private final static boolean ELEMENT_CACHING_ENABLED = false;
	private final static boolean NAME_CACHING_ENABLED = false;

	private static long createCount = 0;
	private static long createCallCount = 0;
	private static long createDocCount = 0;
	private static long createAttrCount = 0;

	protected final QuiXToken type;

	/* constructors */
	private AQuiXEvent(QuiXToken type) {
		this.type = type;
		createCount++;
	}

	public static class StartSequence extends AQuiXEvent {
		private StartSequence() {
			super(QuiXToken.START_SEQUENCE);
		}

		public String toString() {
			return type.toString();
		}
	}

	public static class EndSequence extends AQuiXEvent {
		private EndSequence() {
			super(QuiXToken.END_SEQUENCE);
		}

		public String toString() {
			return type.toString();
		}
	}

	public static class StartDocument extends AQuiXEvent {
		private final QuiXCharStream uri;

		private StartDocument(QuiXCharStream uri) {
			super(QuiXToken.START_DOCUMENT);
			this.uri = uri;
			createDocCount++;
			// System.out.println("START DOCUMENT"+uri);
		}

		public QuiXCharStream getURI() {
			return this.uri;
		}

		public String toString() {
			return type + " " + this.uri;
		}
	}

	public static class EndDocument extends AQuiXEvent {
		private final QuiXCharStream uri;

		private EndDocument(QuiXCharStream uri) {
			super(QuiXToken.END_DOCUMENT);
			this.uri = uri;
			createDocCount++;
			// System.out.println("END DOCUMENT"+uri);
		}

		public QuiXCharStream getURI() {
			return this.uri;
		}

		public String toString() {
			return type + " " + this.uri;
		}
	}

	public static class Namespace extends AQuiXEvent {
		private final QuiXCharStream prefix;
		private final QuiXCharStream uri;

		private Namespace(QuiXCharStream prefix, QuiXCharStream uri) {
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

		public String toString() {
			return type + " " + this.prefix + ":" + this.uri;
		}
	}

	public static abstract class NamedEvent extends AQuiXEvent {
		private final QuiXQName qname;

		private NamedEvent(QuiXQName qname, QuiXToken type) {
			super(type);
			this.qname = qname;
		}

		public QuiXCharStream getLocalName() {
			return this.qname.getLocalPart();
		}

		public QuiXCharStream getFullName() {
			return (!this.qname.getPrefix().isEmpty() ? this.qname.getPrefix().append(":") : QuiXCharStream.EMPTY)
					.append(this.qname.getLocalPart());
		}

		public QuiXCharStream getURI() {
			return this.qname.getNamespaceURI();
		}

		public QuiXCharStream getPrefix() {
			return this.qname.getPrefix();
		}

		public QuiXQName getQName() {
			return qname;
		}
	}

	public static class StartElement extends NamedEvent {
		private StartElement(QuiXQName qname) {
			super(qname, QuiXToken.START_ELEMENT);
			// System.out.println("START ELEMENT"+localName);
		}

		public String toString() {
			return type + " " + getLocalName();
		}
	}

	public static class EndElement extends NamedEvent {
		private EndElement(QuiXQName qname) {
			super(qname, QuiXToken.END_ELEMENT);
			// System.out.println("END ELEMENT" + localName);
		}

		public String toString() {
			return type + " " + getLocalName();
		}
	}

	public static class Attribute extends NamedEvent {
		private final QuiXCharStream value;

		private Attribute(QuiXQName qname, QuiXCharStream value) {
			super(qname, QuiXToken.ATTRIBUTE);
			this.value = value;
			createAttrCount++;
			// System.out.println("ATTRIBUTE");
		}

		public QuiXCharStream getValue() {
			return this.value;
		}

		public String toString() {
			return type + " " + getLocalName();
		}

	}

	public static class Text extends AQuiXEvent {
		private final QuiXCharStream data;

		private Text(QuiXCharStream data) {
			super(QuiXToken.TEXT);
			this.data = data;
			// System.out.println("TEXT");
		}

		public QuiXCharStream getData() {
			return this.data;
		}

		public String toString() {
			return type + " " + getData();
		}
	}

	public static class PI extends AQuiXEvent {
		private final QuiXCharStream target;
		private final QuiXCharStream data;

		private PI(QuiXCharStream target, QuiXCharStream data) {
			super(QuiXToken.PI);
			this.target = target;
			this.data = data;
		}

		public QuiXCharStream getTarget() {
			return this.target;
		}

		public QuiXCharStream getData() {
			return this.data;
		}

		public String toString() {
			return type + " " + getTarget();
		}
	}

	public static class Comment extends AQuiXEvent {
		private final QuiXCharStream data;

		private Comment(QuiXCharStream data) {
			super(QuiXToken.COMMENT);
			this.data = data;
		}

		public QuiXCharStream getData() {
			return this.data;
		}

		public String toString() {
			return type + " " + getData();
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

	public QuiXToken getType() {
		return this.type;
	}

	/* get typed event */

	private static StartSequence newStartSequence = SEQUENCE_CACHING_ENABLED ? new StartSequence() : null;

	public static AQuiXEvent getStartSequence() {
		createCallCount++;
		StartSequence result;
		if (SEQUENCE_CACHING_ENABLED) {
			result = newStartSequence;
		} else {
			result = new StartSequence();
		}
		return result;
	}

	private static EndSequence newEndSequence = SEQUENCE_CACHING_ENABLED ? new EndSequence() : null;

	public static AQuiXEvent getEndSequence() {
		createCallCount++;
		EndSequence result;
		if (SEQUENCE_CACHING_ENABLED) {
			result = newEndSequence;
		} else {
			result = new EndSequence();
		}
		return result;
	}

	private static Map<QuiXCharStream, StartDocument> startDocumentMap = DOCUMENT_CACHING_ENABLED
			? new HashMap<QuiXCharStream, StartDocument>() : null;

	public static AQuiXEvent getStartDocument(QuiXCharStream uri) {
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

	public static AQuiXEvent getEndDocument(QuiXCharStream uri) {
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

	public static AQuiXEvent getStartElement(QuiXCharStream qName, QuiXCharStream namespace) {
		QuiXCharStream localName = qName;
		QuiXCharStream prefix = null;
		if (qName.contains(":")) {
			prefix = qName.substringBefore(":");
			localName = qName.substringAfter(":");
		}
		return getStartElement(localName, namespace, prefix);
	}

	public static AQuiXEvent getStartElement(QuiXCharStream localName, QuiXCharStream namespace,
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

	public static AQuiXEvent getEndElement(QuiXCharStream qName, QuiXCharStream namespace) {
		QuiXCharStream localName = qName;
		QuiXCharStream prefix = null;
		if (qName.contains(":")) {
			prefix = qName.substringBefore(":");
			localName = qName.substringAfter(":");
		}
		return getEndElement(localName, namespace, prefix);
	}

	public static AQuiXEvent getEndElement(QuiXCharStream localName, QuiXCharStream namespace, QuiXCharStream prefix) {
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

	public static AQuiXEvent getAttribute(QuiXCharStream qName, QuiXCharStream namespace, QuiXCharStream value) {
		QuiXCharStream localName = qName;
		QuiXCharStream prefix = null;
		if (qName.contains(":")) {
			prefix = qName.substringBefore(":");
			localName = qName.substringAfter(":");
		}
		return getAttribute(localName, namespace, prefix, value);
	}

	public static AQuiXEvent getAttribute(QuiXCharStream localName, QuiXCharStream namespace, QuiXCharStream prefix,
			QuiXCharStream value) {
		createCallCount++;
		return new Attribute(getQName(localName, namespace, prefix), value);
	}

	public static AQuiXEvent getText(QuiXCharStream text) {
		createCallCount++;
		return new Text(text);
	}

	public static AQuiXEvent getPI(QuiXCharStream target, QuiXCharStream data) {
		createCallCount++;
		return new PI(target, data);
	}

	public static AQuiXEvent getComment(QuiXCharStream comment) {
		createCallCount++;
		return new Comment(comment);
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
		return (this.type == QuiXToken.PI);
	}

	public boolean isComment() {
		return (this.type == QuiXToken.COMMENT);
	}

	public boolean isNamespace() {
		return (this.type == QuiXToken.NAMESPACE);
	}

	public AQuiXEvent getEvent() {
		return this;
	}

	/* debuging */

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
