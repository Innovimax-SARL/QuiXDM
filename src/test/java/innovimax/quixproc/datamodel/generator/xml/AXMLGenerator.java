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

import innovimax.quixproc.datamodel.generator.AGenerator;
import innovimax.quixproc.datamodel.generator.ATreeGenerator;

public abstract class AXMLGenerator extends ATreeGenerator {

	public static AGenerator instance(Type type, SpecialType special) {
		switch (type) {
		case HIGH_NODE_DENSITY:
			return new HighDensityGenerator();
		case HIGH_NODE_DEPTH:
			switch (special) {
			case NAMESPACE:
				return new HighDepthNamespaceGenerator();
			}
			return new HighNodeDepthGenerator();
		case HIGH_NODE_NAME_SIZE:
			switch (special) {
			case OPEN_CLOSE:
				return new HighElementNameSizeOpenClose();
			}
			return new HighElementNameSizeSingle();
		case HIGH_TEXT_SIZE:
			return new HighTextSize();
		}
		return null;
	}

	final SpecialType specialType;

	protected AXMLGenerator(Type treeType, SpecialType xmlType) {
		super(FileExtension.XML, treeType);
		this.specialType = xmlType;
	}

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
		byte r = nextStartName[(b + incr) & 0x7F];
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
		byte r = prevStartName[(b + incr) & 0x7F];
		// System.out.println("prevStartName : "+Integer.toHexString(r &
		// 0xFF)+"("+Character.toString((char) (r& 0xFF))+")" );
		return r;
	}

	public static class HighTextSize extends ATreeGenerator {
		protected HighTextSize() {
			super(FileExtension.XML, ATreeGenerator.Type.HIGH_TEXT_SIZE);
			// TODO Auto-generated constructor stub
		}

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
					bs[0][0] = nextChar(bs[0][0], incr);
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
	}

	public static class HighDensityGenerator extends ATreeGenerator.AHighDensityGenerator {
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

		public HighDensityGenerator() {
			super(FileExtension.XML);
		}

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
					bs[0][0] = nextChar(bs[0][0], incr);
					break;
				case 1:
					bs[1][1] = nextStartName(bs[1][1], incr);
					break;
				}
				return bs[pos];
			}
			return null;
		}

	}

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

		public HighNodeDepthGenerator() {
			super(AGenerator.FileExtension.XML, ATreeGenerator.Type.HIGH_NODE_DEPTH);

		}

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

		@Override
		public byte[] applyVariation(Variation variation, byte[][] bs, int pos) {
			int incr = 0;
			switch (variation) {
			case NO_VARIATION:
				return bs[pos];
			case RANDOM:
				// how to have reversible random ?
				incr = 0;
				//$FALL-THROUGH$
			case SEQUENTIAL:
				switch (pos) {
				case 0:
					bs[0][1] = nextStartName(bs[0][1], incr);
					this.isReturn = true;
					break;
				case 1:
					if (this.isReturn) {
						this.isReturn = false;
						bs[1][2] = bs[0][1];
					} else
						bs[1][2] = prevStartName(bs[1][2], incr);
					break;
				}
				return bs[pos];
			}
			return null;
		}

	}

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

		public HighDepthNamespaceGenerator() {
			// super(AXMLGenerator.Type.HIGH_DEPTH_NAMESPACE);
			super(AGenerator.FileExtension.XML, Type.HIGH_NODE_DENSITY);
		}

		@Override
		protected byte[][] getPatterns() {
			return this.patterns;
		}

		@Override
		protected int getPatternsLength() {
			return this.patterns[0].length + this.patterns[1].length;
		}

		private boolean isReturn = false;

		@Override
		public byte[] applyVariation(Variation variation, byte[][] bs, int pos) {
			int incr = 0, incr2 = 0;
			switch (variation) {
			case NO_VARIATION:
				return bs[pos];
			case RANDOM:
				// how to make reversible random
				// incr = random.nextInt(128);
				incr2 = this.random.nextInt(128);
				//$FALL-THROUGH$
			case SEQUENTIAL:
				switch (pos) {
				case 0:
					bs[0][1] = nextStartName(bs[0][1], incr);
					bs[0][10] = nextAttributeValue(bs[0][10], incr2);
					this.isReturn = true;
					break;
				case 1:
					if (this.isReturn) {
						this.isReturn = false;
						bs[1][2] = bs[0][1];
					} else
						bs[1][2] = prevStartName(bs[1][2], incr);
					break;
				}
				return bs[pos];
			}
			return null;
		}

	}

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

			public HighElementNameSizeSingle() {
				super(FileExtension.XML, SpecialType.STANDARD);
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
					bs[0][0] = nextName(bs[0][0], incr);
					return bs[pos];
				}
				return null;
			}

		}

		public static class HighElementNameSizeOpenClose extends ANodeNameSizeGenerator {

			public HighElementNameSizeOpenClose() {
				super(FileExtension.XML, SpecialType.OPEN_CLOSE);
				this.random.setSeed(0);
			}

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
						this.random.setSeed(0);
						break;
					case 2:
						bs[2][0] = nextName(bs[2][0], incr);
						break;
					}
					return bs[pos];

				}
				return null;
			}

		}
	

	private static void call(ATreeGenerator.Type gtype, SpecialType special, int size, Unit unit)
			throws IOException, XMLStreamException {
		AGenerator generator = instance(gtype, special);
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

	public static void main(String[] args) throws FileNotFoundException, IOException, XMLStreamException {
		System.out.println("nextChar\t: " + display(nextChar));
		System.out.println("nextAttributeValue\t: " + display(nextAttributeValue));
		System.out.println("nextStartName\t: " + display(nextStartName));
		System.out.println("nextName\t: " + display(nextName));
		System.out.println("prevStartName\t:" + display(prevStartName));
		if (ONE_INSTANCE) {
			// call(ATreeGenerator.Type.HIGH_NODE_DENSITY, null, 150,
			// Unit.MBYTE);
			call(ATreeGenerator.Type.HIGH_NODE_DEPTH, null, 201, Unit.MBYTE);
			// call(ATreeGenerator.Type.HIGH_NODE_DEPTH, null, 112, Unit.MBYTE);
		} else {
			for (ATreeGenerator.Type gtype : EnumSet.of(ATreeGenerator.Type.HIGH_NODE_NAME_SIZE,
					ATreeGenerator.Type.HIGH_NODE_NAME_SIZE, ATreeGenerator.Type.HIGH_NODE_DENSITY,
					ATreeGenerator.Type.HIGH_NODE_DEPTH)) {
				for (SpecialType stype : SpecialType.allowedModifiers(FileExtension.XML, gtype))
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

	protected static final boolean USE_STREAM = true;
	protected static final Process PROCESS = Process.READ_BUFFER;
	protected static final boolean ONE_INSTANCE = true;
	protected static final Variation VARIATION = Variation.SEQUENTIAL;

	public enum Process {
		READ_CHAR, READ_BUFFER, PARSE
	}
}
