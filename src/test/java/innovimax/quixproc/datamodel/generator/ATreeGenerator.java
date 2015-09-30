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

public abstract class ATreeGenerator extends AGenerator {
	public enum Type {
		HIGH_NODE_DENSITY, HIGH_NODE_DEPTH, HIGH_NODE_NAME_SIZE, HIGH_TEXT_SIZE, SPECIFIC
	}

	private Type treeType;

	protected ATreeGenerator(FileExtension type, Type treeType) {
		super(type);
		this.treeType = treeType;
	}

	protected ATreeGenerator(FileExtension type) {
		super(type);
		this.treeType = Type.SPECIFIC;
	}

	public static abstract class AHighDensityGenerator extends ATreeGenerator {

		public AHighDensityGenerator(FileExtension type) {
			super(type, Type.HIGH_NODE_DENSITY);
		}

		@Override
		protected int updatePattern(int current_pattern) {
			return (current_pattern + 1) % getPatterns().length;
		}

		@Override
		protected long updateSize(long current_size, int current_pattern) {
			return current_size + getPatterns()[current_pattern].length;
		}

		@Override
		protected boolean notFinished(long current_size, int current_pattern, long total) {
			return current_size < total;
		}

	}

	public static abstract class AHighNodeDepthGenerator extends ATreeGenerator {

		protected AHighNodeDepthGenerator(FileExtension ext, ATreeGenerator.Type gtype) {
			super(ext, gtype);
		}

		private int next_pattern = 0;

		@Override
		protected int updatePattern(int current_pattern) {
			// System.out.println("update pattern " + current_pattern + " -->
			// "+this.next_pattern);

			// return internal state
			return this.next_pattern;
		}

		@Override
		protected long updateSize(long current_size, int current_pattern) {
			// update the size by adding open and closing tag
			// System.out.println("update size " + current_size+",
			// current_pattern "+current_pattern);
			long result = current_size + (current_pattern == 0 ? getPatternsLength() : 0);
			// System.out.println("after update size " + result);
			return result;
		}

		private long loop = 0;

		@Override
		protected boolean notFinished(long current_size, int current_pattern, long total) {
			// System.out.println("not finished " + current_size + ",
			// "+current_pattern+", "+total);
			// try {
			// System.in.read();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			if (current_size < total) {
				this.loop++;
				// System.out.println("not finished loop " + loop);
				return true;
			}
			// current_size >= total
			if (current_pattern == 0) {
				// switch pattern
				this.next_pattern = 1;
			}
			// next_pattern will be 1
			return this.loop-- > 0;
		}

		protected abstract int getPatternsLength();

	}

}
