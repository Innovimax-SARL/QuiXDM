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

	private static final class QuiXCharStreamList extends QuiXCharStream {
		final QuiXCharStream a, b;

		QuiXCharStreamList(QuiXCharStream a, QuiXCharStream b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() throws ConcurrentModificationException {
			return this.a.toString() + this.b.toString();
		}

		@Override
		public boolean contains(CharSequence sequence) throws ConcurrentModificationException {
			return this.a.contains(sequence) || this.b.contains(sequence);
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
			return this.a.isEmpty() && this.b.isEmpty();
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

	private static final class CharSequenceQuiXCharStream extends QuiXCharStream {
		private final CharSequence cs;

		CharSequenceQuiXCharStream(CharSequence cs) {
			this.cs = cs;
		}

		@Override
		public String toString() {
			return this.cs.toString();
		}

		@Override
		public boolean contains(CharSequence sequence) {
			return this.cs.toString().contains(sequence);
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
			return this.cs.length() == 0;
		}

		@Override
		public QuiXCharStream append(QuiXCharStream c) {
			return new QuiXCharStreamList(this, c);
		}

		@Override
		public QuiXCharStream append(CharSequence c) {
			return QuiXCharStream.fromSequence(this.cs.toString() + c.toString());
		}

	}

}
