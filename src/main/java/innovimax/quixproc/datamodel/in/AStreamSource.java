/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.in;

import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.jena.atlas.web.TypedInputStream;

import innovimax.quixproc.datamodel.generator.AGenerator.FileExtension;

public abstract class AStreamSource {
	enum Type {
		XML, JSON, YAML, HTML, CSV, RDF
	}

	final Type type;

	AStreamSource(final Type type) {
		this.type = type;
	}

	private static XMLStreamSource instance(final Source source) {
		return new XMLStreamSource(source);
	}

	public static final class XMLStreamSource extends AStreamSource {
		final Source source;

		XMLStreamSource(final Source source) {
			super(Type.XML);
			this.source = source;
		}

		public Source asSource() {
			return this.source;
		}
	}

	public abstract static class AJSONYAMLStreamSource extends AStreamSource {
		private final InputStream is;

		AJSONYAMLStreamSource(final Type type, final InputStream is) {
			super(type);
			this.is = is;
		}

		public InputStream asInputStream() {
			return this.is;
		}

	}

	static class JSONStreamSource extends AJSONYAMLStreamSource {

		JSONStreamSource(final InputStream is) {
			super(Type.JSON, is);
		}

		static AStreamSource instance(final InputStream is) {
			return new JSONStreamSource(is);
		}
	}

	static class YAMLStreamSource extends AJSONYAMLStreamSource {

		YAMLStreamSource(final InputStream is) {
			super(Type.YAML, is);
		}

		static AStreamSource instance(final InputStream is) {
			return new YAMLStreamSource(is);
		}

	}

	public static class CSVStreamSource extends AStreamSource {
		private final Reader r;

		CSVStreamSource(final Reader r) {
			super(Type.CSV);
			this.r = r;
		}

		public Reader asReader() {
			return this.r;
		}

		public static AStreamSource instance(final Reader r) {
			return new CSVStreamSource(r);
		}

	}

	public static class RDFStreamSource extends AStreamSource {
		private final TypedInputStream is;

		RDFStreamSource(final TypedInputStream is) {
			super(Type.RDF);
			this.is = is;
		}

		public TypedInputStream asTypedInputStream() {
			return this.is;
		}

		public static AStreamSource instance(final TypedInputStream is) {
			return new RDFStreamSource(is);
		}

	}

	public static Iterable<AStreamSource> instances(final Source[] sources) {
		final AStreamSource[] asources = new AStreamSource[sources.length];
		int i = 0;
		for (final Source source : sources) {
			asources[i] = instance(source);
			i++;
		}
		return Arrays.asList(asources);
	}

	public static AStreamSource instance(final FileExtension ext, final InputStream is) {
		switch (ext) {
		case HTML:
			break;
		case JSON:
			return JSONStreamSource.instance(is);
		case XML:
			return instance(new StreamSource(is));
		case YAML:
			return YAMLStreamSource.instance(is);
		default:
			break;

		}
		return null;
	}

}
