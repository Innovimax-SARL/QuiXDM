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
package innovimax.quixproc.datamodel.generator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import innovimax.quixproc.datamodel.event.IQuiXEventStreamReader;
import innovimax.quixproc.datamodel.generator.AReversibleRandom.SimpleReversibleRandom;
import innovimax.quixproc.datamodel.stream.IQuiXStreamReader;

public abstract class AGenerator {

	public enum FileExtension {
		XML, HTML, JSON, YAML
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

	protected final AReversibleRandom random;
	private final long seed;

	public enum Unit {
		BYTE(1, "B"), KBYTE(1000, "KB"), MBYTE(1000000, "MB"), GBYTE(1000000000, "GB"), TBYTE(1000000000000L, "TB");
		private final long value;
		private final String display;

		Unit(long value, String display) {
			this.value = value;
			this.display = display;
		}

		public long value() {
			return this.value;
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

	public void generate(File output, long size) throws IOException {
		generate(output, size, Unit.BYTE, Variation.NO_VARIATION);
	}

	public void generate(File output, long size, Unit unit) throws IOException {
		generate(output, size, unit, Variation.NO_VARIATION);
	}

	public void generate(File output, long size, Unit unit, Variation variation) throws IOException {
		output.getParentFile().mkdirs();
		final long total = size * unit.value();
		FileOutputStream fos = new FileOutputStream(output);
		BufferedOutputStream bos = new BufferedOutputStream(fos, 1000 * 1000);
		final byte[] start = getStart();
		final byte[][] patterns = getPatterns();
		// ensure that at minimum the size is start+end
		long current_size = start.length + getEnd().length;
		int current_pattern = -1;
		// write the start pattern
		bos.write(start);
		// System.out.println(display(start));
		while (notFinished(current_size, current_pattern, total)) {
			// move to next pattern
			current_pattern = updatePattern(current_pattern);
			// System.out.println(current_size);
			// write the alternate pattern
			byte[] toWrite = applyVariation(variation, patterns, current_pattern);
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

	public InputStream getInputStream(long size, Unit unit, Variation variation) {
		return new GeneratorInputStream(size, unit, variation);
	}

	public abstract IQuiXEventStreamReader getQuiXEventStreamReader();

	public abstract IQuiXStreamReader getQuiXStreamReader();

	protected abstract byte[] applyVariation(Variation variation, byte[][] bs, int pos);

	protected abstract boolean notFinished(long current_size, int current_pattern, long total);

	protected abstract int updatePattern(int current_pattern);

	protected abstract long updateSize(long current_size, int current_pattern);

	protected abstract byte[] getEnd();

	protected abstract byte[][] getPatterns();

	protected abstract byte[] getStart();

	private enum InputStreamState {
		START, CURRENT, END
	}

	public class GeneratorInputStream extends InputStream {

		final byte[] start = getStart();
		final byte[][] patterns = getPatterns();
		// final byte[] end = getEnd();
		// ensure that at minimum the size is start+end
		long current_size = this.start.length + getEnd().length;
		int current_pattern = -1;
		int offset = -1;
		byte[] buffer = null;
		InputStreamState state;
		final long total;
		final Variation variation;

		public GeneratorInputStream(long size, Unit unit, Variation variation) {
			this.state = InputStreamState.START;
			this.buffer = this.start;
			// System.out.println("START : length : "+this.buffer.length+" :
			// "+display(this.buffer));

			this.total = size * unit.value();
			this.variation = variation;
		}

		@Override
		public int read(byte[] b, int off, int len) {
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
			int total = 0;
			if (this.offset + 1 == this.buffer.length) {
				// System.out.println("offset : "+this.offset);
				// System.out.println("length : "+len);
				update();
				// System.out.println("offset : "+this.offset);
				// System.out.println("length : "+len);
				if (this.buffer == null)
					return -1;
			}
			do {
				// System.out.println("offset : "+this.offset);
				// System.out.println("length : "+len);
				int length = Math.min(this.buffer.length - (this.offset + 1), len - total);
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
					this.offset = -1;
					return;
				}
				this.state = InputStreamState.CURRENT;
				//$FALL-THROUGH$
			case CURRENT:
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
				int c = this.buffer[this.offset];
				// System.out.println("read : "+display((byte) (c & 0xFF)));
				return c;
			}
			update();
			if (this.buffer == null)
				return -1;
			this.offset++;
			int c = this.buffer[this.offset];
			// System.out.println("read : "+display((byte) (c & 0xFF)));
			return c;
		}

	}

	public static String display(byte b) {
		return Integer.toHexString(b & 0xFF) + "(" + Character.toString((char) (b & 0xFF)) + ")";
	}

	protected static String display(byte[] bytea) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < bytea.length; i++) {
			if (i > 0)
				sb.append(", ");
			sb.append(display(bytea[i]));
		}
		sb.append("]");
		return sb.toString();
	}

	protected AGenerator() {
		this.seed = System.nanoTime();
		this.random = new SimpleReversibleRandom(this.seed, 3, 5);
	}

	protected void resetRandom() {
		this.random.setSeed(this.seed);
	}

	public static String display(int c) {
		return display((byte) (c & 0XFF));
	}

}