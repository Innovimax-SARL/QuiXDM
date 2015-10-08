package innovimax.quixproc.datamodel.generator.annotations;

import java.util.EnumMap;

import innovimax.quixproc.datamodel.generator.AGenerator.FileExtension;
import innovimax.quixproc.datamodel.generator.ATreeGenerator.SpecialType;
import innovimax.quixproc.datamodel.generator.ATreeGenerator.Type;

public class GeneratorRuntimeExtractor {
	public static EnumMap<FileExtension, EnumMap<Type, EnumMap<SpecialType, Class<?>>>> process(
			EnumMap<FileExtension, EnumMap<Type, EnumMap<SpecialType, Class<?>>>> map, Class<?> c) {
		for (Class<?> cc : c.getClasses()) {
			for (Generator generator : cc.getAnnotationsByType(Generator.class)) {
				// file extension
				final EnumMap<Type, EnumMap<SpecialType, Class<?>>> type;
				if (map.containsKey(generator.ext())) {
					type = map.get(generator.ext());
				} else {
					type = new EnumMap<Type, EnumMap<SpecialType, Class<?>>>(Type.class);
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
		return map;
	}

	public static void main(String[] args) {
		// process(AXMLGenerator.class);
	}
}
