package innovimax.quixproc.datamodel.generator.csv;

import java.io.InputStreamReader;
import java.io.Reader;

import innovimax.quixproc.datamodel.QuiXCharStream;
import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.generator.AGenerator;
import innovimax.quixproc.datamodel.stream.IQuiXStreamReader;

public abstract class ACSVGenerator extends AGenerator {

	public Reader getReader(long size, Unit unit, Variation variation) {
		return new InputStreamReader(getInputStream(size, unit, variation), this.currentCharset);
	}

	public static class SimpleCSVGenerator extends ACSVGenerator {

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
			return current_size + this.patterns[current_pattern].length;
		}

		@Override
		protected byte[] getEnd() {
			return s2b("");
		}

		private final byte[][] patterns = { s2b("A,B,C\r\n") };
		private final AQuiXEvent[][] patternsE = {
				{ AQuiXEvent.getStartArray(), AQuiXEvent.getValueString(QuiXCharStream.fromSequence("A")),
						AQuiXEvent.getValueString(QuiXCharStream.fromSequence("B")),
						AQuiXEvent.getValueString(QuiXCharStream.fromSequence("C")), AQuiXEvent.getEndArray() } };

		@Override
		protected byte[][] getPatterns() {
			return this.patterns;
		}

		@Override
		protected byte[] getStart() {
			return s2b("");
		}

		private final AQuiXEvent[] startE = { AQuiXEvent.getStartTable(), AQuiXEvent.getStartArray() };
		private final AQuiXEvent[] endE = { AQuiXEvent.getEndArray(), AQuiXEvent.getEndTable() };

		@Override
		protected AQuiXEvent[] getEndEvent() {
			return this.endE;
		}

		@Override
		protected AQuiXEvent[][] getPatternsEvent() {
			return this.patternsE;
		}

		@Override
		protected AQuiXEvent[] getStartEvent() {
			return this.startE;
		}

		@Override
		protected boolean notFinishedEvent(long current_size, int current_pattern, long total) {
			// TODO Auto-generated method stub
			return false;
		}

	}
}
