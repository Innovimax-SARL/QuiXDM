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
package innovimax.quixproc.datamodel.generator.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import innovimax.quixproc.datamodel.event.IQuiXEventStreamReader;
import innovimax.quixproc.datamodel.generator.AGenerator;
import innovimax.quixproc.datamodel.generator.ATreeGenerator;
import innovimax.quixproc.datamodel.generator.ATreeGenerator.AHighDensityGenerator;
import innovimax.quixproc.datamodel.generator.ATreeGenerator.Type;
import innovimax.quixproc.datamodel.generator.annotations.Generator;
import innovimax.quixproc.datamodel.stream.IQuiXStreamReader;

public abstract class AXMLGenerator extends ATreeGenerator {

	static final byte[] nextChar = initNextChar(false);
	static final byte[] nextAttributeValue = initNextChar(true);
	static final byte[] nextStartName = initNextName(true);
	static final byte[] nextName = initNextName(false);
	static final byte[] prevStartName = initPrevStartName();

	private static int nextAllowedChar(int b, boolean attributeValue) {
		if (b <= 0x20) {
			if (b <= 0xD) {
				if (b <= 0xa) {
					if (b <= 0x9) {
						return (byte) 0x9;
					}
					return (byte) 0xA;
				}
				return (byte) 0xD;
			}
			return (byte) 0x20;
		}
		// MUST
		if (b == '<')
			return b + 1;
		if (b == '&')
			return b + 1;
		// MAY
		if (b == '>')
			return b + 1;
		// attribute use quot
		if (attributeValue && b == '"')
			return b + 1;
		return b;
	}

	private static int nextAllowedName(int b, boolean startName) {
		// NameStartChar ::= "[A-Z] | "_" | [a-z] | [#xC0-#xD6] | [#xD8-#xF6] |
		// [#xF8
		// NameChar ::= NameStartChar | "-" | "." | [0-9] | #xB7 |
		// [#x0300-#x036F] | [#x203F-#x2040]

		if (b <= 'A') {
			if (!startName) {
				if (b <= '-')
					return '-';
				if (b <= '.')
					return '.';
				if (b <= '0')
					return '0';
				if (b <= '9')
					return b;
				// FALL_TROUGH_ : return 'A'
			}
			return 'A';
		}
		if (b <= '_') {
			if (b <= 'Z')
				return b;
			return '_';
		}
		if (b <= 'a') {
			return 'a';
		}
		if (b < 0xC0) {
			if (b <= 'z')
				return b;
			if (!startName) {
				return '-';
			}
			return 'A';// 0xC0;
		}
		if (b < 0xD8) {
			if (b <= 0xD6)
				return b;
			return 0xD8;
		}
		if (b < 0xF8) {
			if (b <= 0xF6)
				return b;
			return 0xF8;
		}
		// b >= 0xF8
		return b;
	}

	private static byte[] initNextChar(boolean attributeValue) {
		byte[] results = new byte[128];
		for (int i = 0; i < results.length; i++) {
			results[i] = (byte) nextAllowedChar(((i & 0x7F) + 1) & 0x7F, attributeValue);
		}
		return results;
	}

	private static byte[] initNextName(boolean startName) {
		byte[] results = new byte[128];
		for (int i = 0; i < results.length; i++) {
			results[i] = (byte) nextAllowedName(((i & 0x7F) + 1) & 0x7F, startName);
		}
		return results;
	}

	private static byte[] initPrevStartName() {
		byte[] results = new byte[128];
		for (int i = results.length; i > 0; i--) {
			int r = nextAllowedName(((i & 0x7F) + 1) & 0x7F, true);
			results[r] = (byte) i;
		}
		results[0x41] = (byte) 0x7a;
		return results;
	}

	static byte nextChar(byte b, int incr) {
		// System.out.println("nextChar : "+Integer.toHexString(b &
		// 0xFF)+"("+Character.toString((char) (b& 0xFF))+")" );
		byte r = nextChar[(b + incr) & 0x7F];
		// System.out.println("nextChar : "+Integer.toHexString(r &
		// 0xFF)+"("+Character.toString((char) (r& 0xFF))+")" );
		return r;
	}

	static byte nextAttributeValue(byte b, int incr) {
		// System.out.println("nextChar : "+Integer.toHexString(b &
		// 0xFF)+"("+Character.toString((char) (b& 0xFF))+")" );
		byte r = nextAttributeValue[(b + incr) & 0x7F];
		// System.out.println("nextChar : "+Integer.toHexString(r &
		// 0xFF)+"("+Character.toString((char) (r& 0xFF))+")" );
		return r;
	}

	static byte nextStartName(byte b, int incr) {
		// System.out.println("nextStartName : "+Integer.toHexString(b &
		// 0xFF)+"("+Character.toString((char) (b& 0xFF))+")" );
		byte r = b;
		do {
			r = nextStartName[r & 0x7F];
		} while (incr-- > 0);
		// System.out.println("nextStartName : "+Integer.toHexString(r &
		// 0xFF)+"("+Character.toString((char) (r& 0xFF))+")" );
		return r;
	}

	static byte nextName(byte b, int incr) {
		// System.out.println("nextName : "+Integer.toHexString(b &
		// 0xFF)+"("+Character.toString((char) (b& 0xFF))+")" );
		byte r = nextName[(b + incr) & 0x7F];
		// System.out.println("nextName : "+Integer.toHexString(r &
		// 0xFF)+"("+Character.toString((char) (r& 0xFF))+")" );
		return r;
	}

	static byte prevStartName(byte b, int incr) {
		// System.out.println("prevStartName : "+Integer.toHexString(b &
		// 0xFF)+"("+Character.toString((char) (b& 0xFF))+")" );
		byte r = b;
		do {
			r = prevStartName[r & 0x7F];
		} while (incr-- > 0);
		// System.out.println("prevStartName : "+Integer.toHexString(r &
		// 0xFF)+"("+Character.toString((char) (r& 0xFF))+")" );
		return r;
	}

	@Generator(ext=FileExtension.XML, type=Type.HIGH_TEXT_SIZE, stype=SpecialType.STANDARD)
	public static class HighTextSize extends AHighTextSizeGenerator {

		final byte[] start = "<r>".getBytes();
		final byte[] end = "</r>".getBytes();

		@Override
		protected byte[] getEnd() {
			return this.end;
		}

		@Override
		protected byte[] getStart() {
			return this.start;
		}

		final byte[][] patterns = { "a".getBytes() };

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
				case 0:
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

	@Generator(ext=FileExtension.XML, type=Type.HIGH_NODE_DENSITY, stype=SpecialType.STANDARD)
	public static class HighNodeDensityGenerator extends AHighDensityGenerator {
		final byte[] start = "<r>".getBytes();
		final byte[] end = "</r>".getBytes();

		@Override
		protected byte[] getEnd() {
			return this.end;
		}

		@Override
		protected byte[] getStart() {
			return this.start;
		}

		final byte[][] patterns = { "a".getBytes(), "<b/>".getBytes() };

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
				case 0:
					bs[pos][0] = nextChar(bs[pos][0], incr);
					break;
				case 1:
					bs[pos][1] = nextStartName(bs[pos][1], incr);
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
	@Generator(ext=FileExtension.XML, type=Type.HIGH_NODE_DEPTH, stype=SpecialType.STANDARD)
	public static class HighNodeDepthGenerator extends AHighNodeDepthGenerator {
		final byte[] start = "<r>".getBytes();
		final byte[] end = "</r>".getBytes();

		@Override
		protected byte[] getEnd() {
			return this.end;
		}

		@Override
		protected byte[] getStart() {
			return this.start;
		}

		final byte[][] patterns = { "<a>".getBytes(), "</a>".getBytes() };

		@Override
		protected byte[][] getPatterns() {
			return this.patterns;
		}

		@Override
		protected int getPatternsLength() {

			int result = this.patterns[0].length + this.patterns[1].length;
			// System.out.println("get patterns lenght " +result);
			return result;
		}

		private boolean isReturn = false;
		boolean directionForward = true;
		@Override
		public byte[] applyVariation(Variation variation, byte[][] bs, int pos) {
			int incr = 0;
			switch (variation) {
			case NO_VARIATION:
				return bs[pos];
			case RANDOM:
				// how to have reversible random ?

				incr = this.random.nextInt(128, this.directionForward);
				//$FALL-THROUGH$
			case SEQUENTIAL:
				switch (pos) {
				case 0:
					bs[pos][1] = nextStartName(bs[pos][1], incr);
					//System.out.println("+"+incr);
					this.isReturn = true;
					break;
				case 1:
					if (this.isReturn) {
						this.isReturn = false;
						bs[pos][2] = bs[0/*take the previous*/][1];
						//resetRandom();
						this.random.prev();
						this.directionForward = false;
					} else {
						//System.out.println("-"+incr);
						bs[pos][2] = prevStartName(bs[pos][2], incr);
					}
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

	@Generator(ext=FileExtension.XML, type=Type.HIGH_NODE_DEPTH, stype=SpecialType.NAMESPACE)
	public static class HighDepthNamespaceGenerator extends AHighNodeDepthGenerator {
		final byte[] start = "<r>".getBytes();
		final byte[] end = "</r>".getBytes();

		@Override
		protected byte[] getEnd() {
			return this.end;
		}

		@Override
		protected byte[] getStart() {
			return this.start;
		}

		final byte[][] patterns = { "<a xmlns=\"a\">".getBytes(), "</a>".getBytes() };


		@Override
		protected byte[][] getPatterns() {
			return this.patterns;
		}

		@Override
		protected int getPatternsLength() {
			return this.patterns[0].length + this.patterns[1].length;
		}

		private boolean isReturn = false;
		boolean directionForward = true;
		@Override
		public byte[] applyVariation(Variation variation, byte[][] bs, int pos) {
			int incr = 0, incr2 = 0;
			switch (variation) {
			case NO_VARIATION:
				return bs[pos];
			case RANDOM:
				incr = this.random.nextInt(128, this.directionForward);
				incr2 = this.random.nextInt(128, this.directionForward);
				//System.out.println(incr + ","+incr2);
				//$FALL-THROUGH$
			case SEQUENTIAL:
				switch (pos) {
				case 0:
					bs[pos][1] = nextStartName(bs[pos][1], incr);
					//System.out.println("+"+incr);
					bs[pos][10] = nextAttributeValue(bs[pos][10], incr2);
					this.isReturn = true;
					break;
				case 1:
					if (this.isReturn) {
						this.isReturn = false;
						bs[pos][2] = bs[0/*previous*/][1];
						//						resetRandom();
						this.random.prev();
						this.random.prev();						
						this.directionForward = false;
					} else {
						//System.out.println("-"+incr2);
						bs[pos][2] = prevStartName(bs[pos][2], incr2);
					}
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

	@Generator(ext=FileExtension.XML, type=Type.HIGH_NODE_NAME_SIZE, stype=SpecialType.STANDARD)
	public static class HighElementNameSizeSingle extends ANodeNameSizeGenerator {
		@Override
		protected byte[] getEnd() {
			return "/>".getBytes();
		}

		@Override
		protected byte[][] getPatterns() {
			byte[][] result = { "a".getBytes() };
			return result;
		}

		@Override
		protected byte[] getStart() {
			return "<_".getBytes();
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
		public byte[] applyVariation(Variation variation, byte[][] bs, int pos) {
			int incr = 0;
			switch (variation) {
			case NO_VARIATION:
				return bs[pos];
			case RANDOM:
				incr = this.random.nextInt(128);
				//$FALL-THROUGH$
			case SEQUENTIAL:
				bs[pos][0] = nextName(bs[pos][0], incr);
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
	
	@Generator(ext=FileExtension.XML, type=Type.HIGH_NODE_NAME_SIZE, stype=SpecialType.OPEN_CLOSE)
	public static class HighElementNameSizeOpenClose extends ANodeNameSizeGenerator {

		@Override
		protected byte[] getEnd() {
			return ">".getBytes();
		}

		private final byte[][] patterns = { "a".getBytes(), "></_".getBytes(), "a".getBytes() };

		@Override
		protected byte[][] getPatterns() {
			return this.patterns;
		}

		@Override
		protected byte[] getStart() {
			return "<_".getBytes();
		}

		private long loop = 0;

		@Override
		protected boolean notFinished(long current_size, int current_pattern, long total) {
			// System.out.println(current_size + ", "+current_pattern+",
			// "+total);
			if (current_size + this.patterns[1].length < total) {
				this.loop++;
				return true;
			}
			// current_size >= total
			if (current_pattern <= 0) {
				// switch pattern
				this.next_pattern = 1;
				return true;
			}
			if (current_pattern == 1) {
				// switch pattern
				this.next_pattern = 2;

			}
			// next_pattern will be 2
			return this.loop-- > 0;
		}

		int next_pattern = 0;

		@Override
		protected int updatePattern(int current_pattern) {
			return this.next_pattern;
		}

		@Override
		protected long updateSize(long current_size, int current_pattern) {
			return current_size + (current_pattern == 0 ? 2 : 0);
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
				case 0:
					bs[0][0] = nextName(bs[0][0], incr);
					break;
				case 1:
					// NOP
					resetRandom();
					break;
				case 2:
					bs[2][0] = nextName(bs[2][0], incr);
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


	private static void call(Type gtype, SpecialType special, int size, Unit unit)
			throws IOException, XMLStreamException, InstantiationException, IllegalAccessException {
		AGenerator generator = instance(FileExtension.XML, gtype, special);
		call(generator, gtype.name(), size, unit);
	}

	private static void call(AGenerator generator, String gtypename, int size, Unit unit)
			throws IOException, XMLStreamException {
		long start = System.currentTimeMillis();
		if (USE_STREAM) {
			InputStream is = generator.getInputStream(size, unit, VARIATION);
			switch (PROCESS) {
			case READ_CHAR:
				int c;
				while ((c = is.read()) != -1) {
					// System.out.println(display((byte) (c & 0xFF)));
				}
				break;
			case READ_BUFFER:
				byte[] b = new byte[1024];
				while ((c = is.read(b)) != -1) {
					// System.out.println(display((byte) (c & 0xFF)));
				}
				break;
			case PARSE:
				XMLInputFactory xif = XMLInputFactory.newFactory();
				XMLStreamReader xsr = xif.createXMLStreamReader(is);
				while (xsr.hasNext()) {
					xsr.next();
				}
				break;
			}

		} else {
			File f = new File(
					"/Users/innovimax/tmp/quixdm/" + gtypename.toLowerCase() + "-" + size + unit.display() + ".xml");
			generator.generate(f, size, unit, VARIATION);
			System.out.print("File : " + f.getName() + "\tSize : " + f.length() + "\t\t");
		}
		System.out.println("Time : " + (System.currentTimeMillis() - start));
	}

	public static void main(String[] args) throws FileNotFoundException, IOException, XMLStreamException, InstantiationException, IllegalAccessException {
		System.out.println("nextChar\t: " + display(nextChar));
		System.out.println("nextAttributeValue\t: " + display(nextAttributeValue));
		System.out.println("nextStartName\t: " + display(nextStartName));
		System.out.println("nextName\t: " + display(nextName));
		System.out.println("prevStartName\t:" + display(prevStartName));
		if (ONE_INSTANCE) {
			// call(ATreeGenerator.Type.HIGH_NODE_DENSITY, null, 150,
			// Unit.MBYTE);
			call(Type.HIGH_NODE_DEPTH, null, 201, Unit.MBYTE);
			// call(ATreeGenerator.Type.HIGH_NODE_DEPTH, null, 112, Unit.MBYTE);
		} else {
			for (Type gtype : EnumSet.of(Type.HIGH_NODE_NAME_SIZE,
					Type.HIGH_NODE_NAME_SIZE, Type.HIGH_NODE_DENSITY,
					Type.HIGH_NODE_DEPTH)) {
				for (SpecialType stype : SpecialType.allowedModifiers(FileExtension.XML, gtype)) {
					for (Unit unit : EnumSet.of(Unit.BYTE, Unit.KBYTE, Unit.MBYTE, Unit.GBYTE)) {
						int[] values = { 1, 2, 5, 10, 20, 50, 100, 200, 500 };
						for (int i : values) {
							if (unit == Unit.GBYTE && i > 1)
								continue;
							call(gtype, stype, i, unit);
						}
					}
				}
			}
		}
	}

	protected static final boolean USE_STREAM = true;
	protected static final Process PROCESS = Process.READ_BUFFER;
	protected static final boolean ONE_INSTANCE = true;
	protected static final Variation VARIATION = Variation.SEQUENTIAL;

	public enum Process {
		READ_CHAR, READ_BUFFER, PARSE
	}
}
