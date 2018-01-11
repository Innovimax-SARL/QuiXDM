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

	protected final Type type;

	protected AStreamSource(Type type) {
		this.type = type;
	}

	public static XMLStreamSource instance(Source source) {
		return new XMLStreamSource(source);
	}

	public static final class XMLStreamSource extends AStreamSource {
		public final Source source;

		XMLStreamSource(Source source) {
			super(Type.XML);
			this.source = source;
		}

		public Source asSource() {
			return this.source;
		}
	}

	public abstract static class AJSONYAMLStreamSource extends AStreamSource {
		private final InputStream is;

		protected AJSONYAMLStreamSource(Type type, InputStream is) {
			super(type);
			this.is = is;
		}

		public InputStream asInputStream() {
			return this.is;
		}

	}

	public static class JSONStreamSource extends AJSONYAMLStreamSource {

		protected JSONStreamSource(InputStream is) {
			super(Type.JSON, is);
		}

		public static AStreamSource instance(InputStream is) {
			return new JSONStreamSource(is);
		}
	}

	public static class YAMLStreamSource extends AJSONYAMLStreamSource {

		protected YAMLStreamSource(InputStream is) {
			super(Type.YAML, is);
		}

		public static AStreamSource instance(InputStream is) {
			return new YAMLStreamSource(is);
		}

	}

	public static class CSVStreamSource extends AStreamSource {
		private final Reader r;

		protected CSVStreamSource(Reader r) {
			super(Type.CSV);
			this.r = r;
		}

		public Reader asReader() {
			return this.r;
		}

		public static AStreamSource instance(Reader r) {
			return new CSVStreamSource(r);
		}

	}

	public static class RDFStreamSource extends AStreamSource {
		private final TypedInputStream is;

		protected RDFStreamSource(TypedInputStream is) {
			super(Type.RDF);
			this.is = is;
		}

		public TypedInputStream asTypedInputStream() {
			return this.is;
		}

		public static AStreamSource instance(TypedInputStream is) {
			return new RDFStreamSource(is);
		}

	}

	public static Iterable<AStreamSource> instances(Source[] sources) {
		AStreamSource[] asources = new AStreamSource[sources.length];
		int i = 0;
		for (javax.xml.transform.Source source : sources) {
			asources[i] = instance(source);
			i++;
		}
		return Arrays.asList(asources);
	}

	public static AStreamSource instance(FileExtension ext, InputStream is) {
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
