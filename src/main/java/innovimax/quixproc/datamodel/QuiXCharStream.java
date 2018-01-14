/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel;

import java.util.ConcurrentModificationException;

import javax.xml.XMLConstants;

/**
 * A truly Streamable CharSequence
 * 
 * The definition of streamable is a real problem for much of the people Let's
 * say that Streamable means, we don't need to store all in memory It becomes a
 * bit too simplistic At least we know what is *NOT* the solution for nasty
 * files XML as JSON and any other document structure that doesn't limit it's
 * content (remember why we moved from EDI which needs to say every size) is
 * that when you want to process such documents, some documents may ends up
 * having huge content and hence you loose the power of streaming MOST IMPORTANT
 * CASES = TODAY, a simple document
 * 
 * @author innovimax
 *
 */
public abstract class QuiXCharStream {

	public static final QuiXCharStream EMPTY = fromSequence("");
	public static final QuiXCharStream NULL_NS_URI = fromSequence(XMLConstants.NULL_NS_URI);
	public static final QuiXCharStream DEFAULT_NS_PREFIX = fromSequence(XMLConstants.DEFAULT_NS_PREFIX);

	/**
	 * @return
	 * @throws ConcurrentModificationException
	 */
	@Override
	public abstract String toString();

	/**
	 * @param sequence
	 * @return
	 * @throws ConcurrentModificationException
	 */
	public abstract boolean contains(CharSequence sequence);

	/**
	 * @param sequence
	 * @return
	 * @throws ConcurrentModificationException
	 */
	public abstract QuiXCharStream substringBefore(CharSequence sequence);

	/**
	 * @param sequence
	 * @return
	 * @throws ConcurrentModificationException
	 */
	public abstract QuiXCharStream substringAfter(CharSequence sequence);

	public abstract boolean isEmpty();

	public abstract QuiXCharStream append(QuiXCharStream cs);

	public abstract QuiXCharStream append(CharSequence cs);

	public static QuiXCharStream fromSequence(final CharSequence cs) {
		return new CharSequenceQuiXCharStream(cs);
	}

	private static final class QuiXCharStreamList extends QuiXCharStream {
		final QuiXCharStream a;
		final QuiXCharStream b;

		QuiXCharStreamList(final QuiXCharStream a, final QuiXCharStream b) {
			this.a = a;
			this.b = b;
		}

		/**
		 * @return
		 * @throws ConcurrentModificationException
		 */
		@Override
		public String toString() {
			return this.a.toString() + this.b.toString();
		}

		/**
		 * @param sequence
		 * @return
		 * @throws ConcurrentModificationException
		 */
		@Override
		public boolean contains(final CharSequence sequence) {
			return this.a.contains(sequence) || this.b.contains(sequence);
		}

		/**
		 * @param sequence
		 * @return
		 * @throws ConcurrentModificationException
		 */
		@Override
		public QuiXCharStream substringBefore(final CharSequence sequence) {

			return null;
		}

		/**
		 * @param sequence
		 * @return
		 * @throws ConcurrentModificationException
		 */
		@Override
		public QuiXCharStream substringAfter(final CharSequence sequence) {
			// TODO Auto-generated method stub
			// not easy to do...
			return null;
		}

		@Override
		public boolean isEmpty() {
			return this.a.isEmpty() && this.b.isEmpty();
		}

		@Override
		public QuiXCharStream append(final QuiXCharStream cs) {
			return new QuiXCharStreamList(this, cs);
		}

		@Override
		public QuiXCharStream append(final CharSequence cs) {
			return new QuiXCharStreamList(this, new CharSequenceQuiXCharStream(cs));
		}

	}

	private static final class CharSequenceQuiXCharStream extends QuiXCharStream {
		private final CharSequence cs;

		CharSequenceQuiXCharStream(final CharSequence cs) {
			this.cs = cs;
		}

		@Override
		public String toString() {
			return this.cs.toString();
		}

		@Override
		public boolean contains(final CharSequence sequence) {
			return this.cs.toString().contains(sequence);
		}

		@Override
		public QuiXCharStream substringBefore(final CharSequence sequence) {
			final int i = this.cs.toString().indexOf(sequence.toString());
			return QuiXCharStream.fromSequence(i == -1 ? "" : this.cs.toString().substring(0, i));

		}

		@Override
		public QuiXCharStream substringAfter(final CharSequence sequence) {
			final int i = this.cs.toString().indexOf(sequence.toString());
			return QuiXCharStream.fromSequence(i == -1 ? "" : this.cs.toString().substring(i + sequence.length()));
		}

		@Override
		public boolean isEmpty() {
			return this.cs.length() == 0;
		}

		@Override
		public QuiXCharStream append(final QuiXCharStream c) {
			return new QuiXCharStreamList(this, c);
		}

		@Override
		public QuiXCharStream append(final CharSequence c) {
			return QuiXCharStream.fromSequence(this.cs.toString() + c.toString());
		}

	}

}
