package innovimax.quixproc.datamodel;

/**
 * A truly Streamable CharSequence
 * 
 * @author innovimax
 *
 */
public abstract class QuiXCharStream {

	public static final QuiXCharStream EMPTY = QuiXCharStream.fromSequence("");

	@Override
	public abstract String toString();

	public abstract boolean contains(CharSequence sequence);

	public abstract QuiXCharStream substringBefore(CharSequence sequence);

	public abstract QuiXCharStream substringAfter(CharSequence sequence);

	public abstract boolean isEmpty();

	public abstract QuiXCharStream append(QuiXCharStream cs);

	public abstract QuiXCharStream append(CharSequence cs);

	public static QuiXCharStream fromSequence(CharSequence cs) {
		return new LocalQuiXCharStream(cs);
	}

	private static class LocalQuiXCharStream extends QuiXCharStream {
		private final CharSequence cs;

		private LocalQuiXCharStream(CharSequence cs) {
			this.cs = cs;
		}

		@Override
		public String toString() {
			return this.cs.toString();
		}

		@Override
		public boolean contains(CharSequence sequence) {
			return cs.toString().contains(sequence);
		}

		@Override
		public QuiXCharStream substringBefore(CharSequence sequence) {
			int i = this.cs.toString().indexOf(sequence.toString());
			return i == -1 ? QuiXCharStream.fromSequence("")
					: QuiXCharStream.fromSequence(this.cs.toString().substring(0, i));

		}

		@Override
		public QuiXCharStream substringAfter(CharSequence sequence) {
			int i = this.cs.toString().indexOf(sequence.toString());
			return i == -1 ? QuiXCharStream.fromSequence("")
					: QuiXCharStream.fromSequence(this.cs.toString().substring(i + sequence.length()));
		}

		@Override
		public boolean isEmpty() {
			return this.cs.toString().isEmpty();
		}

		@Override
		public QuiXCharStream append(QuiXCharStream cs) {
			throw new UnsupportedOperationException();
			// return this.append(cs);
		}

		@Override
		public QuiXCharStream append(CharSequence cs) {
			return QuiXCharStream.fromSequence(this.cs.toString().concat(cs.toString()));
		}

	}

}
