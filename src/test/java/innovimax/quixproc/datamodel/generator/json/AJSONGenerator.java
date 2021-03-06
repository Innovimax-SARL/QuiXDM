/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.generator.json;

import java.io.InputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonToken;

import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.generator.AGenerator;
import innovimax.quixproc.datamodel.generator.ATreeGenerator;
import innovimax.quixproc.datamodel.generator.annotations.TreeGenerator;
import innovimax.quixproc.datamodel.stream.IQuiXStreamReader;

public abstract class AJSONGenerator extends ATreeGenerator {

	private static byte[] initNextChar() {
		final byte[] results = new byte[128];
		for (int i = 0; i < results.length; i++) {
			results[i] = (byte) nextAllowedChar(((i & 0x7F) + 1) & 0x7F);
		}
		return results;
	}

	private static final byte[] nextChar = initNextChar();
	private static final byte[] nextDigit = initNextDigit();

	private static byte[] initNextDigit() {
		final byte[] results = new byte[10];
		for (int i = 0; i < results.length; i++) {
			results[i] = (byte) ('0' + ((i + 1) % 10));
		}
		return results;
	}

	private static int nextAllowedChar(final int b) {
		if (b <= 0x20) {
			// if (b <= 0xD) {
			// if (b <= 0xa) {
			// if (b <= 0x9) {
			// return (byte) 0x9;
			// }
			// return (byte) 0xA;
			// }
			// return (byte) 0xD;
			// }
			return (byte) 0x20;

		}
		// no backslash
		if (b == '\\')
			return b + 1;
		// no quote
		if (b == '"')
			return b + 1;
		return b;
	}

	private static class BoxedArray {
		final byte[][] array;
		final int start;
		final int selector;
		final byte character;
		int end;

		BoxedArray(final byte[][] array, final int selector, final int start) {
			this.array = array;
			this.selector = selector;
			this.start = start;
			this.end = start;
			this.character = array[selector][start];
		}

		void nextUnique() {
			int pos = this.end;
			while (pos >= this.start) {
				final byte r = this.array[this.selector][pos];
				final byte s = nextChar(r, 0);
				this.array[this.selector][pos] = s;
				if (s != this.character) {
					return;
				}
				// s == this.character
				// move up
				pos--;
			}
			// if here we have to extend the buffer
			final byte[] replace = new byte[this.array[this.selector].length + 1];
			System.arraycopy(this.array[this.selector], 0, replace, 0, this.start + 1);
			System.arraycopy(this.array[this.selector], this.start, replace, this.start + 1,
					replace.length - this.start - 1);
			this.end++;
			this.array[this.selector] = replace;
		}
	}

	private static byte nextChar(final byte b, final int incr) {
		// System.out.println("nextChar : "+Integer.toHexString(b &
		// 0xFF)+"("+Character.toString((char) (b& 0xFF))+")" );
		final byte r = nextChar[(b + incr) & 0x7F];
		// System.out.println("nextChar : "+Integer.toHexString(r &
		// 0xFF)+"("+Character.toString((char) (r& 0xFF))+")" );
		return r;
	}

	static byte nextDigit(final byte b, final int incr) {
		return nextDigit[(b + incr) % 10];
	}

	/*
	 * @Generator(ext=FileExtension.JSON, type=Type.HIGH_NODE_NAME_SIZE,
	 * stype=SpecialType.STANDARD) public static class HighNodeNameSizeGenerator
	 * extends ATreeGenerator.ANodeNameSizeGenerator {
	 * 
	 * protected HighNodeNameSizeGenerator(FileExtension ext, SpecialType
	 * xmlType) { super(ext, xmlType); }
	 * 
	 * }
	 */
	// @Generator(ext=FileExtension.JSON, type=Type.HIGH_TEXT_SIZE,
	// stype=SpecialType.STANDARD)
	public static class HighTextSizeGenerator extends AHighTextSizeGenerator {

		final byte[] start = s2b("{");
		final byte[][] end = { s2b("}"), s2b("\"}") };
		final int choose_end = 0;

		@Override
		protected byte[] getEnd() {
			return this.end[this.choose_end];
		}

		@Override
		protected byte[] getStart() {
			return this.start;
		}

		final byte[][] patterns = { s2b("\"\":\""), s2b("a") };

		@Override
		protected byte[][] getPatterns() {
			return this.patterns;
		}

		@Override
		public byte[] applyVariation(final Variation variation, final byte[][] bs, final int pos) {
			int incr = 0;
			switch (variation) {
			case NO_VARIATION:
				return bs[pos];
			case RANDOM:
				incr = this.random.nextInt(128);
				//$FALL-THROUGH$
			case SEQUENTIAL:
				switch (pos) {
				case 1:
					bs[pos][0] = nextChar(bs[pos][0], incr);
					break;
				default:
				}
				return bs[pos];
			default:
			}
			return null;
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
			return current_size + 1;
		}

		@Override
		public IQuiXStreamReader getQuiXStreamReader() {
			// TODO Auto-generated method stub
			return null;
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

	@TreeGenerator(ext = FileExtension.JSON, type = TreeType.HIGH_NODE_DENSITY, stype = SpecialType.STANDARD)
	public static class HighDensityGenerator extends AHighDensityGenerator {

		final byte[] start = s2b("{");
		final byte[][] end = { s2b("}"), s2b("]}") };

		int choose_end = 0;

		@Override
		protected byte[] getEnd() {
			return this.end[this.choose_end];
		}

		@Override
		protected byte[] getStart() {
			return this.start;
		}

		// {} => 2 smallest
		// {"A":1} => 7
		// {"A":1,"B":1} => 13
		// {"A":[1,1,1,1,1]}
		// since key must be different at some point you need bigger key
		// ..."AA":1, ...
		// more or less 7 bytes per key value looks like the densest

		private final byte[][] patterns = { // empty object is allowed
				// "\"A\":1".getBytes(), // first used only once
				// ",\"A\":1".getBytes()
				s2b("\"\":["), // first used only once
				s2b("{}"), s2b(",{}") };

		// private final BoxedArray baA = new BoxedArray(this.patterns, 1, 2);

		@Override
		protected byte[][] getPatterns() {
			return this.patterns;
		}

		@Override
		protected int updatePattern(final int current_pattern) {
			if (current_pattern == 2)
				return 2;
			if (current_pattern == 1)
				return 2;
			if (current_pattern == 0)
				return 1;
			this.choose_end = 1;
			return 0;
		}

		@Override
		public byte[] applyVariation(final Variation variation, final byte[][] bs, final int pos) {
			// int incr = 0;
			// IMPORTANT : the uniqueness is mandatory
			// it doesn't depends on applyRandom hence
			// if (pos == 1)
			// this.baA.nextUnique();
			switch (variation) {
			case NO_VARIATION:
				return bs[pos];
			case RANDOM:
				// incr = this.random.nextInt(10);
				//$FALL-THROUGH$
			case SEQUENTIAL:
				switch (pos) {
				case 0:
				case 1:
					// no op
					break;
				case 2:
					// bs[pos][1] = nextDigit(bs[pos][1], incr);
					break;
				default:
				}
				return bs[pos];
			default:
			}
			return null;
		}

		@Override
		public IQuiXStreamReader getQuiXStreamReader() {
			// TODO Auto-generated method stub
			return null;
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

	// high depth
	// {} -> 2
	// {"":{}} -> 7
	// {"":{"":{}}} -> 12
	// start "{"
	// end "}"
	// '"":{' and '}'
	public abstract static class AHighDepthGenerator extends AHighNodeDepthGenerator {

		@Override
		protected int getPatternsLength() {
			return getPatterns()[0].length + getPatterns()[1].length;
		}

		@Override
		public byte[] applyVariation(final Variation variation, final byte[][] bs, final int pos) {
			// int incr = 0;
			switch (variation) {
			case NO_VARIATION:
				return bs[pos];
			case RANDOM:
				// incr = this.random.nextInt(128);
				//$FALL-THROUGH$
			case SEQUENTIAL:
				switch (pos) {
				case 0:
					// bs[pos][1] = nextChar(bs[pos][1], incr);
					break;
				case 1:
					// no op
					break;
				default:
				}
				return bs[pos];
			default:
			}
			return null;
		}

		@Override
		public IQuiXStreamReader getQuiXStreamReader() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	@TreeGenerator(ext = FileExtension.JSON, type = TreeType.HIGH_NODE_DEPTH, stype = SpecialType.STANDARD)
	protected static class HighDepthGeneratorObject extends AHighDepthGenerator {

		final byte[] start = s2b("{");
		final byte[] end = s2b("}");

		@Override
		protected byte[] getEnd() {
			return this.end;
		}

		@Override
		protected byte[] getStart() {
			return this.start;
		}

		final byte[][] patterns = { s2b("\"\":{"), s2b("}") };

		@Override
		protected byte[][] getPatterns() {
			return this.patterns;
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

	@TreeGenerator(ext = FileExtension.JSON, type = TreeType.HIGH_NODE_DEPTH, stype = SpecialType.ARRAY)
	protected static class HighDepthGeneratorArray extends AHighDepthGenerator {

		final byte[] start = s2b("{\"\":");
		final byte[] end = s2b("}");

		@Override
		protected byte[] getEnd() {
			return this.end;
		}

		@Override
		protected byte[] getStart() {
			return this.start;
		}

		final byte[][] patterns = { s2b("["), s2b("]") };

		@Override
		protected byte[][] getPatterns() {
			return this.patterns;
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

	public static void main(final String[] args)
			throws Exception {

		/*
		 * final byte[][] patterns = { // empty object is allowed
		 * 
		 * "\"A\":1".getBytes(), // first used only once ",\"A\":1".getBytes()
		 * }; BoxedArray baA = new BoxedArray(patterns, 1, 2); for (int i = 0; i
		 * <Integer.MAX_VALUE; i++) { baA.nextUnique(); }
		 * 
		 * 
		 * System.out.println(display(patterns[1]));
		 */
		final JsonFactory f = new JsonFactory();
		f.disable(Feature.ALLOW_COMMENTS);
		f.disable(Feature.ALLOW_SINGLE_QUOTES);
		// AGenerator generator = instance(ATreeGenerator.Type.HIGH_DENSITY);
		final AGenerator generator = instance(FileExtension.JSON, TreeType.HIGH_NODE_DEPTH, SpecialType.STANDARD);

		final InputStream is = generator.getInputStream(50, Unit.MBYTE, Variation.NO_VARIATION);
		if (false) {
			int c;
			while ((c = is.read()) != -1) {
				System.out.println(display((byte) (c & 0xFF)));
			}
		} else {
			final JsonParser p = f.createParser(is);
			p.enable(Feature.STRICT_DUPLICATE_DETECTION);

			while (p.nextToken() != JsonToken.END_OBJECT) {
				//
			}
		}
	}
}
