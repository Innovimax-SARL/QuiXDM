/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.generator.yaml;

import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.generator.ATreeGenerator;
import innovimax.quixproc.datamodel.generator.annotations.TreeGenerator;
import innovimax.quixproc.datamodel.stream.IQuiXStreamReader;

public abstract class AYAMLGenerator extends ATreeGenerator {
	@TreeGenerator(ext = FileExtension.YAML, type = TreeType.HIGH_NODE_DENSITY, stype = SpecialType.STANDARD)
	public static class SimpleYAMLGenerator extends ATreeGenerator {

		@Override
		public IQuiXStreamReader getQuiXStreamReader() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected byte[] applyVariation(Variation variation, byte[][] bs, int pos) {
			return bs[pos];
		}

		@Override
		protected boolean notFinished(long current_size, int current_pattern, long total) {
			return current_size < total;
		}

		@Override
		protected int updatePattern(int current_pattern) {
			return 0;
		}

		@Override
		protected long updateSize(long current_size, int current_pattern) {
			return current_size + this.patterns[current_pattern].length;
		}

		@Override
		protected byte[] getEnd() {
			return s2b("");
		}

		final byte[][] patterns = { s2b(" - a\n") };

		@Override
		protected byte[][] getPatterns() {
			return this.patterns;
		}

		@Override
		protected byte[] getStart() {
			return s2b("a:\n");
		}

		@Override
		protected boolean notFinishedEvent(long current_size, int current_pattern, long total) {
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
