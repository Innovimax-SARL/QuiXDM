package innovimax.quixproc.datamodel.generator.annotations;

import java.util.EnumMap;

import innovimax.quixproc.datamodel.generator.AGenerator;
import innovimax.quixproc.datamodel.generator.ATreeGenerator;
import innovimax.quixproc.datamodel.generator.xml.AXMLGenerator;

public class GeneratorRuntimeExtractor {
  public static EnumMap<AGenerator.FileExtension, EnumMap<ATreeGenerator.Type, EnumMap<ATreeGenerator.SpecialType, Class<?>>>> process(EnumMap<AGenerator.FileExtension, EnumMap<ATreeGenerator.Type, EnumMap<ATreeGenerator.SpecialType, Class<?>>>> map, Class<?> c) {
	  for(Class<?> cc : c.getClasses()) {

	  for(Generator generator : cc.getAnnotationsByType(Generator.class)) {
//		  System.out.println("foo");
		  // file extension
		  final EnumMap<ATreeGenerator.Type, EnumMap<ATreeGenerator.SpecialType, Class<?>>> type;
		  if (map.containsKey(generator.ext())) {
			  type = map.get(generator.ext());
		  } else {
//			  System.out.println("foo1");
			  type = new EnumMap<ATreeGenerator.Type, EnumMap<ATreeGenerator.SpecialType, Class<?>>>(ATreeGenerator.Type.class);
			  map.put(generator.ext(), type);
		  }
		  // type
		  final EnumMap<ATreeGenerator.SpecialType, Class<?>> stype;
		  if (type.containsKey(generator.type())) {
			  stype = type.get(generator.type());
		  } else {
//			  System.out.println("foo2");
			  stype = new EnumMap<ATreeGenerator.SpecialType, Class<?>>(ATreeGenerator.SpecialType.class);
			  type.put(generator.type(), stype);
		  }
		  if (stype.containsKey(generator.stype())) {
			  throw new IllegalStateException("There is already a declared class for such params " + generator.toString());
		  } else {
//			  System.out.println("foo3");
			  stype.put(generator.stype(), cc);
		  }
	  }
	  }
	  System.out.println(map);
	  return map;
  }
  public static void main(String[] args) {
//	process(AXMLGenerator.class);
}
}
