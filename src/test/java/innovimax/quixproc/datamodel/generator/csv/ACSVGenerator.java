package innovimax.quixproc.datamodel.generator.csv;

import java.io.InputStreamReader;
import java.io.Reader;

import innovimax.quixproc.datamodel.event.IQuiXEventStreamReader;
import innovimax.quixproc.datamodel.generator.AGenerator;
import innovimax.quixproc.datamodel.stream.IQuiXStreamReader;

public abstract class ACSVGenerator extends AGenerator {

	public Reader getReader(long size, Unit unit, Variation variation) {
		return new InputStreamReader(getInputStream(size, unit, variation));
	}
	
	public static class SimpleCSVGenerator extends ACSVGenerator {

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

		private final byte[][] patterns = {
				"A\tB\tC\r\n".getBytes()
		};
		@Override
		protected byte[][] getPatterns() {
			return this.patterns;
		}

		@Override
		protected byte[] getStart() {
			return "".getBytes();
		}
		
	}
}
