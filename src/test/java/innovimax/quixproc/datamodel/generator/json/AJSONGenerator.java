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
package innovimax.quixproc.datamodel.generator.json;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import innovimax.quixproc.datamodel.event.IQuiXEventStreamReader;
import innovimax.quixproc.datamodel.generator.AGenerator;
import innovimax.quixproc.datamodel.generator.ATreeGenerator;
import innovimax.quixproc.datamodel.stream.IQuiXStreamReader;
import innovimax.quixproc.datamodel.generator.AGenerator.Variation;

public abstract class AJSONGenerator extends ATreeGenerator {

	protected AJSONGenerator(ATreeGenerator.Type treeType) {
		super(FileExtension.JSON, treeType);
	}

	private static byte[] initNextChar() {
		byte[] results = new byte[128];
		for (int i = 0; i < results.length; i++) {
			results[i] = (byte) nextAllowedChar(((i & 0x7F) + 1) & 0x7F);
		}
		return results;
	}

	static final byte[] nextChar = initNextChar();
	static final byte[] nextDigit = initNextDigit();

	private static byte[] initNextDigit() {
		byte[] results = new byte[10];
		for (int i = 0; i < results.length; i++) {
			results[i] = (byte) ('0' + ((i + 1) % 10));
		}
		return results;
	}

	private static int nextAllowedChar(int b) {
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

		BoxedArray(byte[][] array, int selector, int start) {
			this.array = array;
			this.selector = selector;
			this.start = start;
			this.end = start;
			this.character = array[selector][start];
		}

		void nextUnique() {
			int pos = this.end;
			while (pos >= this.start) {
				byte r = this.array[this.selector][pos];
				byte s = nextChar(r, 0);
				this.array[this.selector][pos] = s;
				if (s != this.character) {
					return;
				}
				// s == this.character
				// move up
				pos--;
			}
			// if here we have to extend the buffer
			byte[] replace = new byte[this.array[this.selector].length + 1];
			System.arraycopy(this.array[this.selector], 0, replace, 0, this.start + 1);
			System.arraycopy(this.array[this.selector], this.start, replace, this.start + 1,
					replace.length - this.start - 1);
			this.end++;
			this.array[this.selector] = replace;
		}
	}

	static byte nextChar(byte b, int incr) {
		// System.out.println("nextChar : "+Integer.toHexString(b &
		// 0xFF)+"("+Character.toString((char) (b& 0xFF))+")" );
		byte r = nextChar[(b + incr) & 0x7F];
		// System.out.println("nextChar : "+Integer.toHexString(r &
		// 0xFF)+"("+Character.toString((char) (r& 0xFF))+")" );
		return r;
	}

	static byte nextDigit(byte b, int incr) {
		byte r = nextDigit[(b + incr) % 10];
		return r;
	}

	public static AGenerator instance(ATreeGenerator.Type type, SpecialType stype) {
		switch (type) {
		case HIGH_NODE_DENSITY:
			return new AJSONGenerator.HighDensityGenerator();
		case HIGH_NODE_DEPTH:
			return new AJSONGenerator.HighDepthGenerator();
		case HIGH_NODE_NAME_SIZE:
			// return new
			// AXMLGenerator.AHighElementNameSize.HighElementNameSizeSingle();
		case HIGH_TEXT_SIZE:
		case SPECIFIC:
			break;
		default:
			break;
		}
		return null;
	}

/*
	public static class HighNodeNameSizeGenerator extends ATreeGenerator.ANodeNameSizeGenerator {

		protected HighNodeNameSizeGenerator(FileExtension ext, SpecialType xmlType) {
			super(ext, xmlType);
		}
		
	}
	*/
	public static class HighTextSizeGenerator extends ATreeGenerator.AHighTextSizeGenerator {
		protected HighTextSizeGenerator(FileExtension ext, SpecialType sType) {
			super(ext, sType);
		}

		final byte[] start = "{".getBytes();
		final byte[][] end = {
				"}".getBytes(),
				"\"}".getBytes()
		};
		int choose_end = 0;

		@Override
		protected byte[] getEnd() {
			return this.end[choose_end];
		}

		@Override
		protected byte[] getStart() {
			return this.start;
		}

		final byte[][] patterns = { "\"\":\"".getBytes(),
				"a".getBytes()};

		@Override
		protected byte[][] getPatterns() {
			return this.patterns;
		}

		@Override
		public byte[] applyVariation(Variation variation, byte[][] bs, int pos) {
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
				}
				return bs[pos];
			}
			return null;
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
			return current_size + 1;
		}

		@Override
		public IQuiXEventStreamReader getQuiXEventStreamReader() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IQuiXStreamReader getQuiXStreamReader() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

	
	public static class HighDensityGenerator extends ATreeGenerator.AHighDensityGenerator {
	
		final byte[] start = "{".getBytes();
		final byte[][] end = {
				"}".getBytes(),
				"]}".getBytes()
		};

		int choose_end = 0;
		@Override
		protected byte[] getEnd() {
			return this.end[choose_end];
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
		//		"\"A\":1".getBytes(), // first used only once
		//		",\"A\":1".getBytes() 
				"\"\":[".getBytes(), // first used only once
				"{}".getBytes(), 
				",{}".getBytes() 				
		};

//		private final BoxedArray baA = new BoxedArray(this.patterns, 1, 2);

		public HighDensityGenerator() {
			super(FileExtension.JSON);
		}

		@Override
		protected byte[][] getPatterns() {
			return this.patterns;
		}

		@Override
		protected int updatePattern(int current_pattern) {
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
		public byte[] applyVariation(Variation variation, byte[][] bs, int pos) {
			int incr = 0;
			// IMPORTANT : the uniqueness is mandatory
			// it doesn't depends on applyRandom hence
//			if (pos == 1)
//				this.baA.nextUnique();
			switch (variation) {
			case NO_VARIATION:
				return bs[pos];
			case RANDOM:
				//incr = this.random.nextInt(10);
				//$FALL-THROUGH$
			case SEQUENTIAL:
				switch (pos) {
				case 0:
				case 1:
					// no op
					break;
				case 2:
					//bs[pos][1] = nextDigit(bs[pos][1], incr);
					break;
				}
				return bs[pos];
			}
			return null;
		}

		@Override
		public IQuiXEventStreamReader getQuiXEventStreamReader() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IQuiXStreamReader getQuiXStreamReader() {
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
	public static class HighDepthGenerator extends AHighNodeDepthGenerator {
		final byte[] start = "{".getBytes();
		final byte[] end = "}".getBytes();

		@Override
		protected byte[] getEnd() {
			return this.end;
		}

		@Override
		protected byte[] getStart() {
			return this.start;
		}

		final byte[][] patterns = { "\"\":{".getBytes(), "}".getBytes() };

		public HighDepthGenerator() {
			super(AGenerator.FileExtension.JSON, ATreeGenerator.Type.HIGH_NODE_DEPTH);
		}

		@Override
		protected byte[][] getPatterns() {
			return this.patterns;
		}

		@Override
		protected int getPatternsLength() {
			return this.patterns[0].length + this.patterns[1].length;
		}

		@Override
		public byte[] applyVariation(Variation variation, byte[][] bs, int pos) {
			int incr = 0;
			switch (variation) {
			case NO_VARIATION:
				return bs[pos];
			case RANDOM:
				//incr = this.random.nextInt(128);
				//$FALL-THROUGH$
			case SEQUENTIAL:
				switch (pos) {
				case 0:
				//	bs[pos][1] = nextChar(bs[pos][1], incr);
					break;
				case 1:
					// no op
					break;
				}
				return bs[pos];
			}
			return null;
		}

		@Override
		public IQuiXEventStreamReader getQuiXEventStreamReader() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IQuiXStreamReader getQuiXStreamReader() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	public static void main(String[] args) throws JsonParseException, IOException {

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
		JsonFactory f = new JsonFactory();
		f.disable(JsonParser.Feature.ALLOW_COMMENTS);
		f.disable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
		// AGenerator generator = instance(ATreeGenerator.Type.HIGH_DENSITY);
		AGenerator generator = instance(ATreeGenerator.Type.HIGH_NODE_DEPTH, SpecialType.STANDARD);

		InputStream is = generator.getInputStream(50, Unit.MBYTE, Variation.NO_VARIATION);
		if (false) {
			int c;
			while ((c = is.read()) != -1) {
				System.out.println(display((byte) (c & 0xFF)));
			}
		} else {

			JsonParser p = f.createParser(is);
			p.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);

			while (p.nextToken() != JsonToken.END_OBJECT) {
				//
			}
		}

	}
}
