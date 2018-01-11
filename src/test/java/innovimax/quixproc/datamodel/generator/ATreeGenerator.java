/*
QuiXProc: efficient evaluation of XProc Pipelines.
Copyright (C) 2011-2018 Innovimax
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

import java.util.Collections;
import java.util.EnumMap;
import java.util.Set;

import innovimax.quixproc.datamodel.generator.annotations.TreeGeneratorRuntimeExtractor;
import innovimax.quixproc.datamodel.generator.json.AJSONGenerator;
import innovimax.quixproc.datamodel.generator.xml.AXMLGenerator;
import innovimax.quixproc.datamodel.generator.yaml.AYAMLGenerator;

public abstract class ATreeGenerator extends AGenerator {
	public enum TreeType {
		HIGH_NODE_DENSITY, HIGH_NODE_DEPTH, HIGH_NODE_NAME_SIZE, HIGH_TEXT_SIZE, SPECIFIC
	}

	public enum SpecialType {
		STANDARD, // no specific
		NAMESPACE, OPEN_CLOSE, ARRAY;
		static final EnumMap<FileExtension, EnumMap<TreeType, EnumMap<SpecialType, Class<?>>>> map = new EnumMap<FileExtension, EnumMap<TreeType, EnumMap<SpecialType, Class<?>>>>(
				FileExtension.class);

		static {
			TreeGeneratorRuntimeExtractor.process(map, AXMLGenerator.class);
			TreeGeneratorRuntimeExtractor.process(map, AJSONGenerator.class);
			TreeGeneratorRuntimeExtractor.process(map, AYAMLGenerator.class);
		}

		public static Set<SpecialType> allowedModifiers(FileExtension ext, TreeType gtype) {
			EnumMap<TreeType, EnumMap<SpecialType, Class<?>>> enumMap = map.get(ext);
			if (enumMap == null)
				return Collections.emptySet();
			EnumMap<SpecialType, Class<?>> enumMap2 = enumMap.get(gtype);
			if (enumMap2 == null)
				return Collections.emptySet();
			return enumMap2.keySet();
		}
	}

	public abstract static class ANodeNameSizeGenerator extends ATreeGenerator {

	}

	public abstract static class AHighTextSizeGenerator extends ATreeGenerator {

	}

	public abstract static class AHighDensityGenerator extends ATreeGenerator {

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

	public abstract static class AHighNodeDepthGenerator extends ATreeGenerator {

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

	public static AGenerator instance(FileExtension ext, TreeType gtype, SpecialType stype)
			throws InstantiationException, IllegalAccessException {
		return (AGenerator) SpecialType.map.get(ext).get(gtype).get(stype).newInstance();
	}

}
