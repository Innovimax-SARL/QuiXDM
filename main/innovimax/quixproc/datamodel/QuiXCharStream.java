/*
QuiXProc: efficient evaluation of XProc Pipelines.
Copyright (C) 2011-2015 Innovimax
All rights reserved.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package innovimax.quixproc.datamodel;

import java.util.ConcurrentModificationException;

/**
 * A truly Streamable CharSequence
 * 
 * The definition of streamable is a real problem for much of the people
 * Let's say that Streamable means, we don't need to store all in memory
 * It becomes a bit too simplistic
 * At least we know what is *NOT* the solution for nasty files
 * XML as JSON and any other document structure that doesn't limit it's content 
 * (remember why we moved from EDI which needs to say every size)
 * is that when you want to process such documents, some documents may
 * ends up having huge content and hence you loose the power of streaming
 * MOST IMPORTANT CASES =
 *  TODAY, a simple document 
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
