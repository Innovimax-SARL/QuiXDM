/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.generator.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.generator.AGenerator;
import innovimax.quixproc.datamodel.generator.ATreeGenerator;
import innovimax.quixproc.datamodel.generator.annotations.TreeGenerator;
import innovimax.quixproc.datamodel.stream.IQuiXStreamReader;

public abstract class AXMLGenerator extends ATreeGenerator {

	private static final byte[] nextChar = initNextChar(false);
	private static final byte[] nextAttributeValue = initNextChar(true);
	private static final byte[] nextStartName = initNextName(true);
	private static final byte[] nextName = initNextName(false);
	private static final byte[] prevStartName = initPrevStartName();

	private static int nextAllowedChar(final int b, final boolean attributeValue) {
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

	private static int nextAllowedName(final int b, final boolean startName) {
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

	private static byte[] initNextChar(final boolean attributeValue) {
		final byte[] results = new byte[128];
		for (int i = 0; i < results.length; i++) {
			results[i] = (byte) nextAllowedChar(((i & 0x7F) + 1) & 0x7F, attributeValue);
		}
		return results;
	}

	private static byte[] initNextName(final boolean startName) {
		final byte[] results = new byte[128];
		for (int i = 0; i < results.length; i++) {
			results[i] = (byte) nextAllowedName(((i & 0x7F) + 1) & 0x7F, startName);
		}
		return results;
	}

	private static byte[] initPrevStartName() {
		final byte[] results = new byte[128];
		for (int i = results.length; i > 0; i--) {
			final int r = nextAllowedName(((i & 0x7F) + 1) & 0x7F, true);
			results[r] = (byte) i;
		}
		results[0x41] = 0x7a;
		return results;
	}

	private static byte nextChar(final byte b, final int incr) {
		// System.out.println("nextChar : "+Integer.toHexString(b &
		// 0xFF)+"("+Character.toString((char) (b& 0xFF))+")" );
		final byte r = nextChar[(b + incr) & 0x7F];
		// System.out.println("nextChar : "+Integer.toHexString(r &
		// 0xFF)+"("+Character.toString((char) (r& 0xFF))+")" );
		return r;
	}

	private static byte nextAttributeValue(final byte b, final int incr) {
		// System.out.println("nextChar : "+Integer.toHexString(b &
		// 0xFF)+"("+Character.toString((char) (b& 0xFF))+")" );
		final byte r = nextAttributeValue[(b + incr) & 0x7F];
		// System.out.println("nextChar : "+Integer.toHexString(r &
		// 0xFF)+"("+Character.toString((char) (r& 0xFF))+")" );
		return r;
	}

	private static byte nextStartName(final byte b, int incr) {
		// System.out.println("nextStartName : "+Integer.toHexString(b &
		// 0xFF)+"("+Character.toString((char) (b& 0xFF))+")" );
		int incr1 = incr;
		byte r = b;
		do {
			r = nextStartName[r & 0x7F];
		} while (incr1-- > 0);
		// System.out.println("nextStartName : "+Integer.toHexString(r &
		// 0xFF)+"("+Character.toString((char) (r& 0xFF))+")" );
		return r;
	}

	private static byte nextName(final byte b, final int incr) {
		// System.out.println("nextName : "+Integer.toHexString(b &
		// 0xFF)+"("+Character.toString((char) (b& 0xFF))+")" );
		final byte r = nextName[(b + incr) & 0x7F];
		// System.out.println("nextName : "+Integer.toHexString(r &
		// 0xFF)+"("+Character.toString((char) (r& 0xFF))+")" );
		return r;
	}

	private static byte prevStartName(final byte b, int incr) {
		// System.out.println("prevStartName : "+Integer.toHexString(b &
		// 0xFF)+"("+Character.toString((char) (b& 0xFF))+")" );
		int incr1 = incr;
		byte r = b;
		do {
			r = prevStartName[r & 0x7F];
		} while (incr1-- > 0);
		// System.out.println("prevStartName : "+Integer.toHexString(r &
		// 0xFF)+"("+Character.toString((char) (r& 0xFF))+")" );
		return r;
	}

	@TreeGenerator(ext = FileExtension.XML, type = TreeType.HIGH_TEXT_SIZE, stype = SpecialType.STANDARD)
	public static class HighTextSize extends AHighTextSizeGenerator {

		final byte[] start = s2b("<r>");
		final byte[] end = s2b("</r>");

		@Override
		protected byte[] getEnd() {
			return this.end;
		}

		@Override
		protected byte[] getStart() {
			return this.start;
		}

		final byte[][] patterns = { s2b("a") };

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
				case 0:
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

	@TreeGenerator(ext = FileExtension.XML, type = TreeType.HIGH_NODE_DENSITY, stype = SpecialType.STANDARD)
	public static class HighNodeDensityGenerator extends AHighDensityGenerator {
		final byte[] start = s2b("<r>");
		final byte[] end = s2b("</r>");

		@Override
		protected byte[] getEnd() {
			return this.end;
		}

		@Override
		protected byte[] getStart() {
			return this.start;
		}

		final byte[][] patterns = { s2b("a"), s2b("<b/>") };

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
				case 0:
					bs[pos][0] = nextChar(bs[pos][0], incr);
					break;
				case 1:
					bs[pos][1] = nextStartName(bs[pos][1], incr);
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

	@TreeGenerator(ext = FileExtension.XML, type = TreeType.HIGH_NODE_DEPTH, stype = SpecialType.STANDARD)
	public static class HighNodeDepthGenerator extends AHighNodeDepthGenerator {
		final byte[] start = s2b("<r>");
		final byte[] end = s2b("</r>");

		@Override
		protected byte[] getEnd() {
			return this.end;
		}

		@Override
		protected byte[] getStart() {
			return this.start;
		}

		final byte[][] patterns = { s2b("<a>"), s2b("</a>") };

		@Override
		protected byte[][] getPatterns() {
			return this.patterns;
		}

		@Override
		protected int getPatternsLength() {

			final int result = this.patterns[0].length + this.patterns[1].length;
			// System.out.println("get patterns length " +result);
			return result;
		}

		private boolean isReturn = false;
		boolean directionForward = true;

		@Override
		public byte[] applyVariation(final Variation variation, final byte[][] bs, final int pos) {
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
					// System.out.println("+"+incr);
					this.isReturn = true;
					break;
				case 1:
					if (this.isReturn) {
						this.isReturn = false;
						bs[pos][2] = bs[0/* take the previous */][1];
						// resetRandom();
						this.random.prev();
						this.directionForward = false;
					} else {
						// System.out.println("-"+incr);
						bs[pos][2] = prevStartName(bs[pos][2], incr);
					}
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

	@TreeGenerator(ext = FileExtension.XML, type = TreeType.HIGH_NODE_DEPTH, stype = SpecialType.NAMESPACE)
	public static class HighDepthNamespaceGenerator extends AHighNodeDepthGenerator {
		final byte[] start = s2b("<r>");
		final byte[] end = s2b("</r>");

		@Override
		protected byte[] getEnd() {
			return this.end;
		}

		@Override
		protected byte[] getStart() {
			return this.start;
		}

		final byte[][] patterns = { s2b("<a xmlns=\"a\">"), s2b("</a>") };

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
		public byte[] applyVariation(final Variation variation, final byte[][] bs, final int pos) {
			int incr = 0;
			int incr2 = 0;
			switch (variation) {
			case NO_VARIATION:
				return bs[pos];
			case RANDOM:
				incr = this.random.nextInt(128, this.directionForward);
				incr2 = this.random.nextInt(128, this.directionForward);
				// System.out.println(incr + ","+incr2);
				//$FALL-THROUGH$
			case SEQUENTIAL:
				switch (pos) {
				case 0:
					bs[pos][1] = nextStartName(bs[pos][1], incr);
					// System.out.println("+"+incr);
					bs[pos][10] = nextAttributeValue(bs[pos][10], incr2);
					this.isReturn = true;
					break;
				case 1:
					if (this.isReturn) {
						this.isReturn = false;
						bs[pos][2] = bs[0/* previous */][1];
						// resetRandom();
						this.random.prev();
						this.random.prev();
						this.directionForward = false;
					} else {
						// System.out.println("-"+incr2);
						bs[pos][2] = prevStartName(bs[pos][2], incr2);
					}
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

	@TreeGenerator(ext = FileExtension.XML, type = TreeType.HIGH_NODE_NAME_SIZE, stype = SpecialType.STANDARD)
	public static class HighElementNameSizeSingle extends ANodeNameSizeGenerator {
		@Override
		protected byte[] getEnd() {
			return s2b("/>");
		}

		@Override
		protected byte[][] getPatterns() {
			return new byte[][]{ s2b("a") };
		}

		@Override
		protected byte[] getStart() {
			return s2b("<_");
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
		public byte[] applyVariation(final Variation variation, final byte[][] bs, final int pos) {
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

	@TreeGenerator(ext = FileExtension.XML, type = TreeType.HIGH_NODE_NAME_SIZE, stype = SpecialType.OPEN_CLOSE)
	public static class HighElementNameSizeOpenClose extends ANodeNameSizeGenerator {

		@Override
		protected byte[] getEnd() {
			return s2b(">");
		}

		private final byte[][] patterns = { s2b("a"), s2b("></_"), s2b("a") };

		@Override
		protected byte[][] getPatterns() {
			return this.patterns;
		}

		@Override
		protected byte[] getStart() {
			return s2b("<_");
		}

		private long loop = 0;

		@Override
		protected boolean notFinished(final long current_size, final int current_pattern, final long total) {
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
		protected int updatePattern(final int current_pattern) {
			return this.next_pattern;
		}

		@Override
		protected long updateSize(final long current_size, final int current_pattern) {
			return current_size + (current_pattern == 0 ? 2 : 0);
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

	private static void call(final TreeType gtype, final SpecialType special, final int size, final Unit unit)
			throws IOException, XMLStreamException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		final AGenerator generator = instance(FileExtension.XML, gtype, special);
		call(generator, gtype.name(), size, unit);
	}

	private static void call(final AGenerator generator, final String gtypename, final int size, final Unit unit)
			throws IOException, XMLStreamException {
		final long start = System.currentTimeMillis();
		if (USE_STREAM) {
			final InputStream is = generator.getInputStream(size, unit, VARIATION);
			switch (PROCESS) {
			case READ_CHAR:
				int c;
				while ((c = is.read()) != -1) {
					// System.out.println(display((byte) (c & 0xFF)));
				}
				break;
			case READ_BUFFER:
				final byte[] b = new byte[1024];
				int d;
				while ((d = is.read(b)) != -1) {
					// System.out.println(display((byte) (d & 0xFF)));
				}
				break;
			case PARSE:
				final XMLInputFactory xif = XMLInputFactory.newFactory();
				final XMLStreamReader xsr = xif.createXMLStreamReader(is);
				while (xsr.hasNext()) {
					xsr.next();
				}
				break;
			default:
			}
		} else {
			final File f = new File(
					"/Users/innovimax/tmp/quixdm/" + gtypename.toLowerCase() + "-" + size + unit.display() + ".xml");
			generator.generate(f, size, unit, VARIATION);
			System.out.print("File : " + f.getName() + "\tSize : " + f.length() + "\t\t");
		}
		System.out.println("Time : " + (System.currentTimeMillis() - start));
	}

	public static void main(final String[] args) throws Exception {
		System.out.println("nextChar\t: " + display(nextChar));
		System.out.println("nextAttributeValue\t: " + display(nextAttributeValue));
		System.out.println("nextStartName\t: " + display(nextStartName));
		System.out.println("nextName\t: " + display(nextName));
		System.out.println("prevStartName\t:" + display(prevStartName));
		if (ONE_INSTANCE) {
			// call(ATreeGenerator.Type.HIGH_NODE_DENSITY, null, 150,
			// Unit.MBYTE);
			call(TreeType.HIGH_NODE_DEPTH, null, 201, Unit.MBYTE);
			// call(ATreeGenerator.Type.HIGH_NODE_DEPTH, null, 112, Unit.MBYTE);
		} else {
			for (final TreeType gtype : EnumSet.of(TreeType.HIGH_NODE_NAME_SIZE, TreeType.HIGH_NODE_NAME_SIZE,
					TreeType.HIGH_NODE_DENSITY, TreeType.HIGH_NODE_DEPTH)) {
				for (final SpecialType stype : SpecialType.allowedModifiers(FileExtension.XML, gtype)) {
					for (final Unit unit : EnumSet.of(Unit.BYTE, Unit.KBYTE, Unit.MBYTE, Unit.GBYTE)) {
						final int[] values = { 1, 2, 5, 10, 20, 50, 100, 200, 500 };
						for (final int i : values) {
							if (unit == Unit.GBYTE && i > 1)
								continue;
							call(gtype, stype, i, unit);
						}
					}
				}
			}
		}
	}

	private static final boolean USE_STREAM = true;
	private static final Process PROCESS = Process.READ_BUFFER;
	private static final boolean ONE_INSTANCE = true;
	private static final Variation VARIATION = Variation.SEQUENTIAL;

	public enum Process {
		READ_CHAR, READ_BUFFER, PARSE
	}
}
