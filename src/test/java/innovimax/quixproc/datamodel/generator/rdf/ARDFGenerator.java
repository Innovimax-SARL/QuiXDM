/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.generator.rdf;

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.riot.WebContent;

import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.generator.AGenerator;
import innovimax.quixproc.datamodel.stream.IQuiXStreamReader;

public abstract class ARDFGenerator extends AGenerator {

	private static final ContentType CONTENT_TYPE = WebContent.ctTurtle;

	public TypedInputStream getTypedInputStream(final long size, final Unit unit, final Variation variation) {
		return new TypedInputStream(getInputStream(size, unit, variation), CONTENT_TYPE);
	}

	public static class SimpleRDFGenerator extends ARDFGenerator {

		@Override
		public IQuiXStreamReader getQuiXStreamReader() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected byte[] applyVariation(final Variation variation, final byte[][] bs, final int pos) {
			return bs[pos];
		}

		@Override
		protected boolean notFinished(final long current_size, final int current_pattern, final long total) {
			return current_size < total;
		}

		@Override
		protected int updatePattern(final int current_pattern) {
			return 0;
		}

		@Override
		protected long updateSize(final long current_size, final int current_pattern) {
			return current_size + this.patterns[current_pattern].length;
		}

		@Override
		protected byte[] getEnd() {
			return s2b("");
		}

		final byte[][] patterns = { s2b("<a> <b> <c>.\n") };

		@Override
		protected byte[][] getPatterns() {
			return this.patterns;
		}

		@Override
		protected byte[] getStart() {
			return s2b("");
		}

		@Override
		protected boolean notFinishedEvent(final long current_size, final int current_pattern, final long total) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		protected AQuiXEvent[] getEndEvent() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected AQuiXEvent[][] getPatternsEvent() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected AQuiXEvent[] getStartEvent() {
			// TODO Auto-generated method stub
			return null;
		}

	}
}
