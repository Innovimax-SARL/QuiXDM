/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.generator;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.EnumMap;

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

		public static Iterable<SpecialType> allowedModifiers(final FileExtension ext, final TreeType gtype) {
			final EnumMap<TreeType, EnumMap<SpecialType, Class<?>>> enumMap = map.get(ext);
			if (enumMap == null)
				return Collections.emptySet();
			final EnumMap<SpecialType, Class<?>> enumMap2 = enumMap.get(gtype);
			if (enumMap2 == null)
				return Collections.emptySet();
			return enumMap2.keySet();
		}
	}

	protected abstract static class ANodeNameSizeGenerator extends ATreeGenerator {

	}

	protected abstract static class AHighTextSizeGenerator extends ATreeGenerator {

	}

	protected abstract static class AHighDensityGenerator extends ATreeGenerator {

		@Override
		protected int updatePattern(final int current_pattern) {
			return (current_pattern + 1) % getPatterns().length;
		}

		@Override
		protected long updateSize(final long current_size, final int current_pattern) {
			return current_size + getPatterns()[current_pattern].length;
		}

		@Override
		protected boolean notFinished(final long current_size, final int current_pattern, final long total) {
			return current_size < total;
		}

	}

	protected abstract static class AHighNodeDepthGenerator extends ATreeGenerator {

		private int next_pattern = 0;

		@Override
		protected int updatePattern(final int current_pattern) {
			// System.out.println("update pattern " + current_pattern + " -->
			// "+this.next_pattern);

			// return internal state
			return this.next_pattern;
		}

		@Override
		protected long updateSize(final long current_size, final int current_pattern) {
			// update the size by adding open and closing tag
			// System.out.println("update size " + current_size+",
			// current_pattern "+current_pattern);
			final long result = current_size + (current_pattern == 0 ? getPatternsLength() : 0);
			// System.out.println("after update size " + result);
			return result;
		}

		private long loop = 0;

		@Override
		protected boolean notFinished(final long current_size, final int current_pattern, final long total) {
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

	public static AGenerator instance(final FileExtension ext, final TreeType gtype, final SpecialType stype)
			throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		return (AGenerator) SpecialType.map.get(ext).get(gtype).get(stype).getConstructor().newInstance();
	}

}
