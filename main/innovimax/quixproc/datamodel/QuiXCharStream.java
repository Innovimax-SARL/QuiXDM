package innovimax.quixproc.datamodel;

import java.util.ConcurrentModificationException;

/**
 * A truly Streamable CharSequence
 * 
 * @author innovimax
 *
 */
public abstract class QuiXCharStream {

	public static final QuiXCharStream EMPTY = QuiXCharStream.fromSequence("");

	@Override
	public abstract String toString() throws ConcurrentModificationException;

	public abstract boolean contains(CharSequence sequence) throws ConcurrentModificationException;

	public abstract QuiXCharStream substringBefore(CharSequence sequence) throws ConcurrentModificationException;

	public abstract QuiXCharStream substringAfter(CharSequence sequence) throws ConcurrentModificationException;

	public abstract boolean isEmpty();

	public abstract QuiXCharStream append(QuiXCharStream cs);

	public abstract QuiXCharStream append(CharSequence cs);

	public static QuiXCharStream fromSequence(CharSequence cs) {
		return new CharSequenceQuiXCharStream(cs);
	}
    private static class QuiXCharStreamList extends QuiXCharStream {
    	final QuiXCharStream a, b;
    	private QuiXCharStreamList(QuiXCharStream a, QuiXCharStream b) {
    		this.a = a;
    		this.b = b;
    	}
    	
		@Override
		public String toString() throws ConcurrentModificationException {
			return a.toString()+ b.toString();
		}

		@Override
		public boolean contains(CharSequence sequence) throws ConcurrentModificationException {
			if (a.contains(sequence)) return true;
			return b.contains(sequence);
		}

		@Override
		public QuiXCharStream substringBefore(CharSequence sequence) throws ConcurrentModificationException {
			
			return null;
		}

		@Override
		public QuiXCharStream substringAfter(CharSequence sequence) throws ConcurrentModificationException {
			// TODO Auto-generated method stub
			// not easy to do...
			return null;
		}

		@Override
		public boolean isEmpty() {
			if (!a.isEmpty()) return false;
			return b.isEmpty();
		}

		@Override
		public QuiXCharStream append(QuiXCharStream cs) {
			return new QuiXCharStreamList(this, cs);
		}

		@Override
		public QuiXCharStream append(CharSequence cs) {
			return new QuiXCharStreamList(this, new CharSequenceQuiXCharStream(cs));
		}
    	
    }
	private static class CharSequenceQuiXCharStream extends QuiXCharStream {
		private final CharSequence cs;

		private CharSequenceQuiXCharStream(CharSequence cs) {
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
			return new QuiXCharStreamList(this, cs);
		}

		@Override
		public QuiXCharStream append(CharSequence cs) {
			return QuiXCharStream.fromSequence(this.cs.toString().concat(cs.toString()));
		}

	}

}
