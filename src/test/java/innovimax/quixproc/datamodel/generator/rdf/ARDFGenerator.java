package innovimax.quixproc.datamodel.generator.rdf;


import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.riot.WebContent;

import innovimax.quixproc.datamodel.event.IQuiXEventStreamReader;
import innovimax.quixproc.datamodel.generator.AGenerator;
import innovimax.quixproc.datamodel.stream.IQuiXStreamReader;

public abstract class ARDFGenerator extends AGenerator {
	
	private final static ContentType CONTENT_TYPE = WebContent.ctTurtle; 

	public TypedInputStream getTypedInputStream(long size, Unit unit, Variation variation) {
	  return new TypedInputStream(getInputStream(size, unit, variation), CONTENT_TYPE);
    }
	
	public static class SimpleRDFGenerator extends ARDFGenerator {

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
			return s2b("");
		}
		final byte[][] patterns = {
				s2b("<a> <b> <c>.\n")
		};
		@Override
		protected byte[][] getPatterns() {
			return this.patterns;
		}

		@Override
		protected byte[] getStart() {
			return s2b("");
		}
		
	}
}
