package innovimax.quixproc.datamodel.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import innovimax.quixproc.datamodel.generator.AGenerator;
import innovimax.quixproc.datamodel.generator.AGenerator.FileExtension;
import innovimax.quixproc.datamodel.generator.ATreeGenerator;
import innovimax.quixproc.datamodel.generator.ATreeGenerator.SpecialType;
import innovimax.quixproc.datamodel.generator.ATreeGenerator.Type;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Generator {
   FileExtension ext();
   Type type();
   SpecialType stype();
}
