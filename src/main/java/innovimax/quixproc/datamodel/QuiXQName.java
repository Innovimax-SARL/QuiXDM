/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel;

import java.io.Serializable;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * <p>
 * {@code QuiXQName} represents a <strong>qualified name</strong> as defined in
 * the XML specifications: <a href="http://www.w3.org/TR/xmlschema-2/#QName">XML
 * Schema Part2: Datatypes specification</a>,
 * <a href="http://www.w3.org/TR/REC-xml-names/#ns-qualnames">Namespaces in
 * XML</a>, <a href="http://www.w3.org/XML/xml-names-19990114-errata">Namespaces
 * in XML Errata</a>.
 * </p>
 *
 * <p>
 * The value of a {@code QuiXQName} contains a <strong>Namespace URI</strong>,
 * <strong>local part</strong> and <strong>prefix</strong>.
 * </p>
 * 
 * <p>
 * {@code QuiXQName} is QuiXCharStream friendly implementation of {@code QName}
 * </p>
 *
 * <p>
 * The prefix is included in {@code QName} to retain lexical information
 * <strong><em>when present</em></strong> in an
 * {@link javax.xml.transform.Source XML input source}. The prefix is <strong>
 * <em>NOT</em></strong> used in {@link #equals(Object) QName.equals(Object)} or
 * to compute the {@link #hashCode() QName.hashCode()}. Equality and the hash
 * code are defined using <strong><em>only</em></strong> the Namespace URI and
 * local part.
 * </p>
 *
 * <p>
 * If not specified, the Namespace URI is set to {@link XMLConstants#NULL_NS_URI
 * XMLConstants.NULL_NS_URI}. If not specified, the prefix is set to
 * {@link XMLConstants#DEFAULT_NS_PREFIX XMLConstants.DEFAULT_NS_PREFIX}.
 * </p>
 *
 * <p>
 * {@code QuiXQName} is immutable.
 * </p>
 *
 * @author innovimax
 * @version new
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#QName"> XML Schema Part2:
 *      Datatypes specification</a>
 * @see <a href="http://www.w3.org/TR/REC-xml-names/#ns-qualnames"> Namespaces
 *      in XML</a>
 * @see <a href="http://www.w3.org/XML/xml-names-19990114-errata"> Namespaces in
 *      XML Errata</a>
 */
public class QuiXQName implements Serializable {

	private final QuiXCharStream namespaceURI;

	private final QuiXCharStream localPart;

	/**
	 * <p>
	 * prefix of this {@code QName}.
	 * </p>
	 */
	private final QuiXCharStream prefix;

	/**
	 * <p>
	 * {@code QuiXQName} constructor specifying the Namespace URI and local
	 * part.
	 * </p>
	 *
	 * <p>
	 * If the Namespace URI is {@code null}, it is set to
	 * {@link XMLConstants#NULL_NS_URI XMLConstants.NULL_NS_URI}. This value
	 * represents no explicitly defined Namespace as defined by the
	 * <a href="http://www.w3.org/TR/REC-xml-names/#ns-qualnames">Namespaces in
	 * XML</a> specification. This action preserves compatible behavior with
	 * QName 1.0. Explicitly providing the {@link XMLConstants#NULL_NS_URI
	 * XMLConstants.NULL_NS_URI} value is the preferred coding style.
	 * </p>
	 *
	 * <p>
	 * If the local part is {@code null} an {@code IllegalArgumentException} is
	 * thrown. A local part of "" is allowed to preserve compatible behavior
	 * with QName 1.0.
	 * </p>
	 *
	 * <p>
	 * When using this constructor, the prefix is set to
	 * {@link XMLConstants#DEFAULT_NS_PREFIX XMLConstants.DEFAULT_NS_PREFIX}.
	 * </p>
	 *
	 * <p>
	 * The Namespace URI is not validated as a
	 * <a href="http://www.ietf.org/rfc/rfc2396.txt">URI reference</a>. The
	 * local part is not validated as a
	 * <a href="http://www.w3.org/TR/REC-xml-names/#NT-NCName">NCName</a> as
	 * specified in <a href="http://www.w3.org/TR/REC-xml-names/">Namespaces in
	 * XML</a>.
	 * </p>
	 *
	 * @param namespaceURI
	 *            Namespace URI of the {@code QName}
	 * @param localPart
	 *            local part of the {@code QName}
	 *
	 * @throws IllegalArgumentException
	 *             When {@code localPart} is {@code null}
	 *
	 * @see QName(String namespaceURI, String localPart, String prefix)
	 *      javax.xml.namespace.QName(String namespaceURI, String localPart, String prefix)
	 */
	public QuiXQName(final QuiXCharStream namespaceURI, final QuiXCharStream localPart) {
		this(namespaceURI, localPart, QuiXCharStream.DEFAULT_NS_PREFIX);
	}

	/**
	 * <p>
	 * {@code QuiXQName} constructor specifying the Namespace URI, local part
	 * and prefix.
	 * </p>
	 *
	 * <p>
	 * If the Namespace URI is {@code null}, it is set to
	 * {@link XMLConstants#NULL_NS_URI XMLConstants.NULL_NS_URI}. This value
	 * represents no explicitly defined Namespace as defined by the
	 * <a href="http://www.w3.org/TR/REC-xml-names/#ns-qualnames">Namespaces in
	 * XML</a> specification. This action preserves compatible behavior with
	 * QName 1.0. Explicitly providing the {@link XMLConstants#NULL_NS_URI
	 * XMLConstants.NULL_NS_URI} value is the preferred coding style.
	 * </p>
	 *
	 * <p>
	 * If the local part is {@code null} an {@code IllegalArgumentException} is
	 * thrown. A local part of "" is allowed to preserve compatible behavior
	 * with QName 1.0.
	 * </p>
	 *
	 * <p>
	 * If the prefix is {@code null}, an {@code IllegalArgumentException} is
	 * thrown. Use {@link XMLConstants#DEFAULT_NS_PREFIX
	 * XMLConstants.DEFAULT_NS_PREFIX} to explicitly indicate that no prefix is
	 * present or the prefix is not relevant.
	 * </p>
	 *
	 * <p>
	 * The Namespace URI is not validated as a
	 * <a href="http://www.ietf.org/rfc/rfc2396.txt">URI reference</a>. The
	 * local part and prefix are not validated as a
	 * <a href="http://www.w3.org/TR/REC-xml-names/#NT-NCName">NCName</a> as
	 * specified in <a href="http://www.w3.org/TR/REC-xml-names/">Namespaces in
	 * XML</a>.
	 * </p>
	 *
	 * @param namespaceURI
	 *            Namespace URI of the {@code QName}
	 * @param localPart
	 *            local part of the {@code QName}
	 * @param prefix
	 *            prefix of the {@code QName}
	 *
	 * @throws IllegalArgumentException
	 *             When {@code localPart} or {@code prefix} is {@code null}
	 */
	public QuiXQName(final QuiXCharStream namespaceURI, final QuiXCharStream localPart, final QuiXCharStream prefix) {

		// map null Namespace URI to default
		// to preserve compatibility with QName 1.0
		this.namespaceURI = namespaceURI == null ? QuiXCharStream.NULL_NS_URI : namespaceURI;

		// local part is required.
		// "" is allowed to preserve compatibility with QName 1.0
		if (localPart == null) {
			throw new IllegalArgumentException("local part cannot be \"null\" when creating a QName");
		}
		this.localPart = localPart;

		// prefix is required
		if (prefix == null) {
			throw new IllegalArgumentException("prefix cannot be \"null\" when creating a QName");
		}
		this.prefix = prefix;
	}

	/**
	 * <p>
	 * {@code QuiXQName} constructor specifying the local part.
	 * </p>
	 *
	 * <p>
	 * If the local part is {@code null} an {@code IllegalArgumentException} is
	 * thrown. A local part of "" is allowed to preserve compatible behavior
	 * with QName 1.0.
	 * </p>
	 *
	 * <p>
	 * When using this constructor, the Namespace URI is set to
	 * {@link XMLConstants#NULL_NS_URI XMLConstants.NULL_NS_URI} and the prefix
	 * is set to {@link XMLConstants#DEFAULT_NS_PREFIX
	 * XMLConstants.DEFAULT_NS_PREFIX}.
	 * </p>
	 *
	 * <p>
	 * <em>In an XML context, all Element and Attribute names exist
	 * in the context of a Namespace.  Making this explicit during the
	 * construction of a {@code QName} helps prevent hard to diagnosis XML
	 * validity errors. The constructors
	 * {@link #QName(String namespaceURI, String localPart) QName(String
	 * namespaceURI, String localPart)} and
	 * {@link #QName(String namespaceURI, String localPart, String prefix)} are
	 * preferred.</em>
	 * </p>
	 *
	 * <p>
	 * The local part is not validated as a
	 * <a href="http://www.w3.org/TR/REC-xml-names/#NT-NCName">NCName</a> as
	 * specified in <a href="http://www.w3.org/TR/REC-xml-names/">Namespaces in
	 * XML</a>.
	 * </p>
	 *
	 * @param localPart
	 *            local part of the {@code QName}
	 *
	 * @throws IllegalArgumentException
	 *             When {@code localPart} is {@code null}
	 *
	 * @see #QName(String namespaceURI, String localPart) QName(String
	 *      namespaceURI, String localPart)
	 * @see #QName(String namespaceURI, String localPart, String prefix)
	 *      QName(String namespaceURI, String localPart, String prefix)
	 */
	public QuiXQName(final QuiXCharStream localPart) {
		this(QuiXCharStream.NULL_NS_URI, localPart, QuiXCharStream.DEFAULT_NS_PREFIX);
	}

	/**
	 * <p>
	 * Namespace URI of this {@code QName}.
	 * </p>
	 */ /**
	 * <p>
	 * Get the Namespace URI of this {@code QName}.
	 * </p>
	 *
	 * @return Namespace URI of this {@code QName}
	 */
	public QuiXCharStream getNamespaceURI() {
		return this.namespaceURI;
	}

	/**
	 * <p>
	 * local part of this {@code QName}.
	 * </p>
	 */ /**
	 * <p>
	 * Get the local part of this {@code QName}.
	 * </p>
	 *
	 * @return local part of this {@code QName}
	 */
	public QuiXCharStream getLocalPart() {
		return this.localPart;
	}

	/**
	 * <p>
	 * Get the prefix of this {@code QName}.
	 * </p>
	 *
	 * <p>
	 * The prefix assigned to a {@code QName} might <strong>
	 * <em>NOT</em></strong> be valid in a different context. For example, a
	 * {@code QName} may be assigned a prefix in the context of parsing a
	 * document but that prefix may be invalid in the context of a different
	 * document.
	 * </p>
	 *
	 * @return prefix of this {@code QName}
	 */
	public QuiXCharStream getPrefix() {
		return this.prefix;
	}

	/**
	 * <p>
	 * Test this {@code QName} for equality with another {@code Object}.
	 * </p>
	 *
	 * <p>
	 * If the {@code Object} to be tested is not a {@code QName} or is
	 * {@code null}, then this method returns {@code false}.
	 * </p>
	 *
	 * <p>
	 * Two {@code QName}s are considered equal if and only if both the Namespace
	 * URI and local part are equal. This method uses {@code String.equals()} to
	 * check equality of the Namespace URI and local part. The prefix is
	 * <strong><em>NOT</em></strong> used to determine equality.
	 * </p>
	 *
	 * <p>
	 * This method satisfies the general contract of
	 * {@link Object#equals(Object) Object.equals(Object)}
	 * </p>
	 *
	 * @param objectToTest
	 *            the {@code Object} to test for equality with this
	 *            {@code QName}
	 * @return {@code true} if the given {@code Object} is equal to this
	 *         {@code QName} else {@code false}
	 */
	@Override
	public final boolean equals(final Object objectToTest) {
		if (objectToTest == this) {
			return true;
		}

		if (!(objectToTest instanceof QuiXQName)) {
			return false;
		}

		final QuiXQName qName = (QuiXQName) objectToTest;

		return this.getLocalPart().equals(qName.getLocalPart()) && this.getNamespaceURI().equals(qName.getNamespaceURI());
	}

	/**
	 * <p>
	 * Generate the hash code for this {@code QName}.
	 * </p>
	 *
	 * <p>
	 * The hash code is calculated using both the Namespace URI and the local
	 * part of the {@code QName}. The prefix is <strong> <em>NOT</em></strong>
	 * used to calculate the hash code.
	 * </p>
	 *
	 * <p>
	 * This method satisfies the general contract of {@link Object#hashCode()
	 * Object.hashCode()}.
	 * </p>
	 *
	 * @return hash code for this {@code QName} {@code Object}
	 */
	@Override
	public final int hashCode() {
		return this.getNamespaceURI().hashCode() ^ this.getLocalPart().hashCode();
	}

	/**
	 * <p>
	 * {@code String} representation of this {@code QName}.
	 * </p>
	 *
	 * <p>
	 * The commonly accepted way of representing a {@code QName} as a
	 * {@code String} was <a href="http://jclark.com/xml/xmlns.htm">defined</a>
	 * by James Clark. Although this is not a <em>standard</em> specification,
	 * it is in common use, e.g.
	 * {@link javax.xml.transform.Transformer#setParameter(String name, Object value)}
	 * . This implementation represents a {@code QName} as: "{" + Namespace URI
	 * + "}" + local part. If the Namespace URI
	 * {@code .equals(XMLConstants.NULL_NS_URI)}, only the local part is
	 * returned. An appropriate use of this method is for debugging or logging
	 * for human consumption.
	 * </p>
	 *
	 * <p>
	 * Note the prefix value is <strong><em>NOT</em></strong> returned as part
	 * of the {@code String} representation.
	 * </p>
	 *
	 * <p>
	 * This method satisfies the general contract of {@link Object#toString()
	 * Object.toString()}.
	 * </p>
	 *
	 * @return {@code String} representation of this {@code QName}
	 */
	@Override
	public String toString() {
		// TODO convert XMLConstants to a local one
		if (this.getNamespaceURI().equals(QuiXCharStream.NULL_NS_URI)) {
			return this.getLocalPart().toString();
		}
		return "{" + this.getNamespaceURI() + '}' + this.getLocalPart();
	}

	/**
	 * <p>
	 * {@code QName} derived from parsing the formatted {@code String} .
	 * </p>
	 *
	 * <p>
	 * If the {@code String} is {@code null} or does not conform to
	 * {@link #toString() QName.toString()} formatting, an
	 * {@code IllegalArgumentException} is thrown.
	 * </p>
	 *
	 * <p>
	 * <em>The {@code String} <strong>MUST</strong> be in the form returned
	 * by {@link #toString() QName.toString()}.</em>
	 * </p>
	 *
	 * <p>
	 * The commonly accepted way of representing a {@code QName} as a
	 * {@code String} was <a href="http://jclark.com/xml/xmlns.htm">defined</a>
	 * by James Clark. Although this is not a <em>standard</em> specification,
	 * it is in common use, e.g.
	 * {@link javax.xml.transform.Transformer#setParameter(String name, Object value)}
	 * . This implementation parses a {@code String} formatted as: "{" +
	 * Namespace URI + "}" + local part. If the Namespace URI
	 * {@code .equals(XMLConstants.NULL_NS_URI)}, only the local part should be
	 * provided.
	 * </p>
	 *
	 * <p>
	 * The prefix value <strong><em>CANNOT</em></strong> be represented in the
	 * {@code String} and will be set to {@link XMLConstants#DEFAULT_NS_PREFIX
	 * XMLConstants.DEFAULT_NS_PREFIX}.
	 * </p>
	 *
	 * <p>
	 * This method does not do full validation of the resulting {@code QName}.
	 * <p>
	 * The Namespace URI is not validated as a
	 * <a href="http://www.ietf.org/rfc/rfc2396.txt">URI reference</a>. The
	 * local part is not validated as a
	 * <a href="http://www.w3.org/TR/REC-xml-names/#NT-NCName">NCName</a> as
	 * specified in <a href="http://www.w3.org/TR/REC-xml-names/">Namespaces in
	 * XML</a>.
	 * </p>
	 *
	 * @param qNameAsString
	 *            {@code String} representation of the {@code QName}
	 *
	 * @throws IllegalArgumentException
	 *             When {@code qNameAsString} is {@code null} or malformed
	 *
	 * @return {@code QName} corresponding to the given {@code String}
	 * @see #toString() QName.toString()
	 */
	public static QuiXQName valueOf(final String qNameAsString) {

		// null is not valid
		if (qNameAsString == null) {
			throw new IllegalArgumentException("cannot create QName from \"null\" or \"\" String");
		}

		// "" local part is valid to preserve compatible behavior with QName 1.0
		if (qNameAsString.isEmpty()) {
			return new QuiXQName(QuiXCharStream.NULL_NS_URI, QuiXCharStream.fromSequence(qNameAsString),
					QuiXCharStream.DEFAULT_NS_PREFIX);
		}

		// local part only?
		if (qNameAsString.charAt(0) != '{') {
			return new QuiXQName(QuiXCharStream.NULL_NS_URI, QuiXCharStream.fromSequence(qNameAsString),
					QuiXCharStream.DEFAULT_NS_PREFIX);
		}

		// Namespace URI improperly specified?
		if (qNameAsString.startsWith('{' + XMLConstants.NULL_NS_URI + '}')) {
			throw new IllegalArgumentException("Namespace URI .equals(XMLConstants.NULL_NS_URI), " + ".equals(\""
					+ XMLConstants.NULL_NS_URI + "\"), " + "only the local part, " + '"'
					+ qNameAsString.substring(2 + XMLConstants.NULL_NS_URI.length()) + "\", " + "should be provided.");
		}

		// Namespace URI and local part specified
		final int endOfNamespaceURI = qNameAsString.indexOf('}');
		if (endOfNamespaceURI == -1) {
			throw new IllegalArgumentException(
					"cannot create QName from \"" + qNameAsString + "\", missing closing \"}\"");
		}
		return new QuiXQName(QuiXCharStream.fromSequence(qNameAsString.substring(1, endOfNamespaceURI)),
				QuiXCharStream.fromSequence(qNameAsString.substring(endOfNamespaceURI + 1)),
				QuiXCharStream.DEFAULT_NS_PREFIX);
	}

	public QName asQName() {
		return new QName(this.getNamespaceURI().toString(), this.getLocalPart().toString(), this.prefix.toString());
	}

}
