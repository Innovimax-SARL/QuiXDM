package innovimax.quixproc.datamodel.generator.yaml;

import innovimax.quixproc.datamodel.event.IQuiXEventStreamReader;
import innovimax.quixproc.datamodel.generator.ATreeGenerator;
import innovimax.quixproc.datamodel.generator.AGenerator.FileExtension;
import innovimax.quixproc.datamodel.generator.ATreeGenerator.SpecialType;
import innovimax.quixproc.datamodel.generator.ATreeGenerator.TreeType;
import innovimax.quixproc.datamodel.generator.annotations.TreeGenerator;
import innovimax.quixproc.datamodel.stream.IQuiXStreamReader;

public abstract class AYAMLGenerator extends ATreeGenerator {
	@TreeGenerator(ext = FileExtension.YAML, type = TreeType.HIGH_NODE_DENSITY, stype = SpecialType.STANDARD)
	public static class SimpleYAMLGenerator extends ATreeGenerator {

		@Override
		public IQuiXEventStreamReader getQuiXEventStreamReader() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IQuiXStreamReader getQuiXStreamReader() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected byte[] applyVariation(Variation variation, byte[][] bs, int pos) {
			return bs[pos];
		}

		@Override
		protected boolean notFinished(long current_size, int current_pattern, long total) {
			return current_size < total;
		}

		@Override
		protected int updatePattern(int current_pattern) {
			return 0;
		}

		@Override
		protected long updateSize(long current_size, int current_pattern) {
			return current_size+this.patterns[current_pattern].length;
		}

		@Override
		protected byte[] getEnd() {
			return "".getBytes();
		}

		final byte[][] patterns = {
				" - a\n".getBytes()
		};
		
		@Override
		protected byte[][] getPatterns() {
			return this.patterns;
		}

		@Override
		protected byte[] getStart() {
			return "a:\n".getBytes();
		}
		
	}

}
