/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.generator.annotations;

import java.util.EnumMap;

import innovimax.quixproc.datamodel.generator.AGenerator.FileExtension;
import innovimax.quixproc.datamodel.generator.ATreeGenerator.SpecialType;
import innovimax.quixproc.datamodel.generator.ATreeGenerator.TreeType;

public class TreeGeneratorRuntimeExtractor {
	public static void process(EnumMap<FileExtension, EnumMap<TreeType, EnumMap<SpecialType, Class<?>>>> map,
			Class<?> c) {
		for (Class<?> cc : c.getClasses()) {

			for (TreeGenerator generator : cc.getAnnotationsByType(TreeGenerator.class)) {
				// file extension
				final EnumMap<TreeType, EnumMap<SpecialType, Class<?>>> type;
				if (map.containsKey(generator.ext())) {
					type = map.get(generator.ext());
				} else {
					type = new EnumMap<TreeType, EnumMap<SpecialType, Class<?>>>(TreeType.class);
					map.put(generator.ext(), type);
				}
				// type
				final EnumMap<SpecialType, Class<?>> stype;
				if (type.containsKey(generator.type())) {
					stype = type.get(generator.type());
				} else {
					stype = new EnumMap<SpecialType, Class<?>>(SpecialType.class);
					type.put(generator.type(), stype);
				}
				if (stype.containsKey(generator.stype())) {
					throw new IllegalStateException(
							"There is already a declared class for such params " + generator.toString());
				}
				stype.put(generator.stype(), cc);
			}
		}
		System.out.println(map);
	}

	public static void main(String[] args) {
		// process(AXMLGenerator.class);
	}
}
