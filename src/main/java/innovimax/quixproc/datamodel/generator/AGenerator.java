/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.generator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.event.IQuiXEventStreamReader;
import innovimax.quixproc.datamodel.generator.AReversibleRandom.SimpleReversibleRandom;
import innovimax.quixproc.datamodel.stream.IQuiXStreamReader;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AGenerator {

	public enum FileExtension {
		XML, HTML, JSON, YAML, RDF, CSV
	}

	public enum Variation {
		NO_VARIATION, SEQUENTIAL, RANDOM
	}

	public enum Encoding {
		SEVEN_BITS, // 1 byte (minus the upper bit) is one char
		EIGHT_BITS, // 1 byte (including upper bit) is one char
		TWO_BYTES, // 2 bytes per char
		UNICODE
	}

	public enum FileProperty {
		REPEATABLE/*
					 * repeated part of the content makes it potentially
					 * infinite
					 */, SYMMETRIC/*
								 * the file is symmetric to its middle, usually
								 * you must provide a length
								 */, CONTENT_VARIATION/*
													 * content is varying from
													 * one occurrence to the
													 * other
													 */,
	}

	// 7BIT
	protected final Charset currentCharset = StandardCharsets.US_ASCII;

	protected byte[] s2b(final String s) {
		return s.getBytes(this.currentCharset);
	}

	protected final AReversibleRandom random;
	private final long seed;

	public enum BaseUnit {
		BYTE, EVENT
	}

	public enum Unit {
		BYTE(1, "B", BaseUnit.BYTE), KBYTE(1000, "KB", BaseUnit.BYTE), MBYTE(1000000, "MB", BaseUnit.BYTE), GBYTE(
				1000000000, "GB",
				BaseUnit.BYTE), TBYTE(1000000000000L, "TB", BaseUnit.BYTE), EVENT(1, "Ev", BaseUnit.BYTE), KEVENT(1000,
						"KEv", BaseUnit.BYTE), MEVENT(1000000, "MEv", BaseUnit.BYTE), GEVENT(1000000000, "GEv",
								BaseUnit.BYTE), TEVENT(1000000000000L, "TEv", BaseUnit.BYTE);
		private final long value;
		private final String display;
		private final BaseUnit base;

		Unit(final long value, final String display, final BaseUnit base) {
			this.value = value;
			this.display = display;
			this.base = base;
		}

		public long value() {
			return this.value;
		}

		public BaseUnit getBase() {
			return this.base;
		}

		public String display() {
			return this.display;
		}
	}
	/*
	 * protected static AGenerator instance(FileExtension type) { switch (type)
	 * { case HTML: break; case JSON: break; case XML: return new
	 * AXMLGenerator(); break; case YAML: break; default: break;
	 * 
	 * } return null; }
	 */

	public void generate(final File output, final long size) throws IOException {
		generate(output, size, Unit.BYTE, Variation.NO_VARIATION);
	}

	public void generate(final File output, final long size, final Unit unit) throws IOException {
		generate(output, size, unit, Variation.NO_VARIATION);
	}

	public void generate(final File output, final long size, final Unit unit, final Variation variation) throws IOException {
		output.getParentFile().mkdirs();
		final long total = size * unit.value();
		try (FileOutputStream fos = new FileOutputStream(output)) {
			try (BufferedOutputStream bos = new BufferedOutputStream(fos, 1000 * 1000)) {
				final byte[] start = getStart();
				final byte[][] patterns = getPatterns();
				// ensure that at minimum the size is start+end
				long current_size = start.length + getEnd().length;
				// write the start pattern
				bos.write(start);
				// System.out.println(display(start));
				int current_pattern = -1;
				while (notFinished(current_size, current_pattern, total)) {
					// move to next pattern
					current_pattern = updatePattern(current_pattern);
					// System.out.println(current_size);
					// write the alternate pattern
					final byte[] toWrite = applyVariation(variation, patterns, current_pattern);
					// System.out.println(display(toWrite));
					bos.write(toWrite);
					// update the size
					current_size = updateSize(current_size, current_pattern);
				}
				// write the end pattern
				bos.write(getEnd());
				// System.out.println(display(getEnd()));

				bos.flush();
				bos.close();
				fos.close();
			}
		}
	}

	public InputStream getInputStream(final long size, final Unit unit, final Variation variation) {
		return new GeneratorInputStream(size, unit, variation);
	}

	public final IQuiXEventStreamReader getQuiXEventStreamReader(final long size, final Unit unit, final Variation variation) {
		return new GeneratorQuiXEventStreamReader(size, unit, variation);
	}

	public abstract IQuiXStreamReader getQuiXStreamReader();

	protected abstract byte[] applyVariation(Variation variation, byte[][] bs, int pos);

	protected abstract boolean notFinished(long current_size, int current_pattern, long total);

	protected abstract boolean notFinishedEvent(long current_size, int current_pattern, long total);

	protected abstract int updatePattern(int current_pattern);

	protected abstract long updateSize(long current_size, int current_pattern);

	protected abstract byte[] getEnd();

	protected abstract byte[][] getPatterns();

	protected abstract byte[] getStart();

	protected abstract AQuiXEvent[] getEndEvent();

	protected abstract AQuiXEvent[][] getPatternsEvent();

	protected abstract AQuiXEvent[] getStartEvent();

	private enum QuiXEventStreamReaderState {
		START, CURRENT, END
	}

	public class GeneratorQuiXEventStreamReader implements IQuiXEventStreamReader {
		final long total;
		final Variation variation;
		final QuiXEventStreamReaderState state;
		final AQuiXEvent[] buffer;
		int position;
		int current_pattern;
		final long current_evsize = getStartEvent().length + getEndEvent().length;

		GeneratorQuiXEventStreamReader(final long size, final Unit unit, final Variation variation) {
			this.state = QuiXEventStreamReaderState.START;
			this.total = size * unit.value();
			this.variation = variation;
			this.buffer = getStartEvent();
			this.position = 0;
		}

		@Override
		public boolean hasNext() {
			return this.state != QuiXEventStreamReaderState.END;
		}

		@Override
		public AQuiXEvent next() {
			if (this.position < this.buffer.length) {
				return this.buffer[this.position++];
			}
			switch (this.state) {
			case START:
				if (notFinishedEvent(this.current_evsize, this.current_pattern, this.total)) {
					// TODO
				}
				break;

			case END:
				break;
			default:
				break;

			}
			return null;
		}

		@Override
		public void close() {
			// TODO Auto-generated method stub

		}

	}

	private enum InputStreamState {
		START,
		// CURRENT,
		END
	}

	class GeneratorInputStream extends InputStream {

		final byte[] start = getStart();
		final byte[][] patterns = getPatterns();
		// final byte[] end = getEnd();
		// ensure that at minimum the size is start+end
		long current_size = this.start.length + getEnd().length;
		int current_pattern = -1;
		int offset = -1;
		byte[] buffer;
		InputStreamState state;
		final long total;
		final Variation variation;

		GeneratorInputStream(final long size, final Unit unit, final Variation variation) {
			this.state = InputStreamState.START;
			this.buffer = this.start;
			// System.out.println("START : length : "+this.buffer.length+" :
			// "+display(this.buffer));

			this.total = size * unit.value();
			this.variation = variation;
		}

		@Override
		public int read(final byte[] b, final int off, final int len) {
			// System.out.println("READ : off : "+off+" ; len : "+len+" :
			// "+display(b));
			if (b == null) {
				throw new NullPointerException();
			}
			if (off < 0 || len < 0 || len > b.length - off) {
				throw new IndexOutOfBoundsException();
			}
			if (len == 0) {
				return 0;
			}
			if (this.buffer == null)
				return -1;
			if (this.offset + 1 == this.buffer.length) {
				// System.out.println("offset : "+this.offset);
				// System.out.println("length : "+len);
				update();
				// System.out.println("offset : "+this.offset);
				// System.out.println("length : "+len);
				if (this.buffer == null)
					return -1;
			}
			int total = 0;
			do {
				// System.out.println("offset : "+this.offset);
				// System.out.println("length : "+len);
				final int length = Math.min(this.buffer.length - (this.offset + 1), len - total);
				// System.out.println("length : "+length);
				System.arraycopy(this.buffer, this.offset + 1, b, off + total, length);
				total += length;
				// System.out.println("total : "+total);
				this.offset = this.offset + 1 + length - 1;
				// System.out.println("offset : "+this.offset);
				if (this.offset == this.buffer.length - 1)
					update();
				if (total == len) {
					// System.out.println("offset : "+ this.offset+"; length :
					// "+len+" : "+display(b));
					return len;

				}
				if (this.buffer == null) {
					// System.out.println("offset : "+ this.offset+"; length :
					// "+total+" : "+display(b));
					return total;
				}
			} while (true);
		}

		private void update() {
			// System.out.println(this.state);
			// offset == buffer.length
			switch (this.state) {
			case START:
				// System.out.println("NotFinished");
				if (notFinished(this.current_size, this.current_pattern, this.total)) {
					// System.out.println("NotFinished : no");
					// move to next pattern
					this.current_pattern = updatePattern(this.current_pattern);
					// System.out.println(current_size);
					// write the alternate pattern
					this.buffer = applyVariation(this.variation, this.patterns, this.current_pattern);
					// update the size
					this.current_size = updateSize(this.current_size, this.current_pattern);
					// System.out.println("Currentsize : "+this.current_size);
					this.offset = -1;
					return;
				}
				// this.state = InputStreamState.CURRENT;
				// //$FALL-THROUGH$
				// case CURRENT:
				this.buffer = getEnd();
				this.offset = -1;
				this.state = InputStreamState.END;
				return;
			case END:
				this.buffer = null;
				break;
			default:
			}
		}

		@Override
		public int read() {
			if (this.buffer == null)
				return -1;
			this.offset++;
			if (this.offset < this.buffer.length) {
				final int c = this.buffer[this.offset];
				// System.out.println("read : "+display((byte) (c & 0xFF)));
				return c;
			}
			update();
			if (this.buffer == null)
				return -1;
			if (this.buffer.length == 0)
				return -1;
			this.offset++;
			final int c = this.buffer[this.offset];
			// System.out.println("read : "+display((byte) (c & 0xFF)));
			return c;
		}

	}

	protected static String display(final byte b) {
		return Integer.toHexString(b & 0xFF) + "(" + Character.toString((char) (b & 0xFF)) + ")";
	}

	protected static String display(final byte[] bytea) {
		return IntStream.range(0, bytea.length).mapToObj((int i) -> display(bytea[i])).collect(Collectors.joining(", ", "[", "]"));
	}

	protected AGenerator() {
		this.seed = System.nanoTime();
		this.random = new SimpleReversibleRandom(this.seed, 3, 5);
	}

	protected void resetRandom() {
		this.random.setSeed(this.seed);
	}

	public static String display(final int c) {
		return display((byte) (c & 0XFF));
	}

}