/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import innovimax.quixproc.datamodel.IQuiXStream;
import innovimax.quixproc.datamodel.QuiXCharStream;
import innovimax.quixproc.datamodel.QuiXException;
import innovimax.quixproc.datamodel.event.AQuiXEvent;

/**
 * Simple implementation of {@link Queue} interface It uses a simple
 * {@link ArrayList} and {@link ReentrantReadWriteLock} to do the job It's far
 * from efficient
 * 
 * @author innovimax
 */
public class SimpleAppendQuiXQueue<T> implements IQuiXQueue<T> {

	private static final boolean DEBUG = false;
	private final List<T> events;
	private final ReentrantReadWriteLock rwl;
	//
	private int readerCount = 0;
	private boolean closed = false;
	private boolean startWorking = false;
	//
	private static int counter = 0;
	private final int rank;
	private int maxReader;

	public SimpleAppendQuiXQueue() {
		this.events = new ArrayList<T>();
		this.rwl = new ReentrantReadWriteLock(true);
		counter++;
		this.rank = counter;
		if (DEBUG)
			System.out.println("CreateSimpleQEQ : " + this.rank);
		// Thread.dumpStack();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xmlcalabash.stream.util.shared.IQuixEventQueue#add(com.xmlcalabash.
	 * stream.util.QuixEvent)
	 */
	@Override
	public void append(final T event) {
		this.startWorking = true;
		this.rwl.writeLock().lock();
		try {
			if (this.closed)
				throw new RuntimeException("Cannot append to a closed stream");
			this.events.add(event);
		} finally {
			this.rwl.writeLock().unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xmlcalabash.stream.util.shared.IQuixEventQueue#close()
	 */
	@Override
	public void close() {
		this.rwl.writeLock().lock();
		try {
			if (this.closed)
				throw new RuntimeException("Already closed");
			this.closed = true;
		} finally {
			this.rwl.writeLock().unlock();
		}
		if (DEBUG)
			System.out.println("CreateSimpleQEQ (closed) : " + this.rank);
	}

	private final class LocalReader implements IQuiXStream<T> {
		// private final Iterator<QuixEvent> iterator;
		private int i = 0;
		private boolean readerClosed = false;

		LocalReader() {
			// this.iterator = events.iterator();
		}

		@Override
		public boolean hasNext() {
			if (this.readerClosed)
				throw new RuntimeException("Reader already closed");
			SimpleAppendQuiXQueue.this.rwl.readLock().lock();
			try {
				if (this.i < SimpleAppendQuiXQueue.this.events.size())
					return true;
				// if (iterator.hasNext()) return true;
				// si c'est faux ca depends de close
				while (this.i >= SimpleAppendQuiXQueue.this.events
						.size()/* !iterator.hasNext() */) {
					if (SimpleAppendQuiXQueue.this.closed)
						return false;
					SimpleAppendQuiXQueue.this.rwl.readLock().unlock();
					// System.out.println("hasNextBeforeYield");
					Thread.yield();
					// System.out.println("hasNextAfterYield");
					SimpleAppendQuiXQueue.this.rwl.readLock().lock();
				}
				// il n'y a pas de concurrence sur la lecture, chacun lit a son
				// rythme
				// donc si il y a un element il l'est toujours
				return true;
			} finally {
				SimpleAppendQuiXQueue.this.rwl.readLock().unlock();
			}
		}

		@Override
		public T next() {
			if (this.readerClosed)
				throw new RuntimeException("Reader already closed");
			SimpleAppendQuiXQueue.this.rwl.readLock().lock();
			try {
				if (this.i < SimpleAppendQuiXQueue.this.events.size()) {
					return SimpleAppendQuiXQueue.this.events.get(this.i++);
				}
				// if (iterator.hasNext()) return iterator.next();
				// si c'est faux ca d�pends de close
				while (this.i >= SimpleAppendQuiXQueue.this.events
						.size()/* !iterator.hasNext() */) {
					if (SimpleAppendQuiXQueue.this.closed)
						return null;
					SimpleAppendQuiXQueue.this.rwl.readLock().unlock();
					// System.out.println("nextBeforeYield");
					Thread.yield();
					// System.out.println("nextAfterYield");
					SimpleAppendQuiXQueue.this.rwl.readLock().lock();
				}
				// il n'y a pas de concurrence sur la lecture, chacun lit � son
				// rythme
				// donc si il y a un element il l'est toujours
				return SimpleAppendQuiXQueue.this.events.get(this.i++);
				// return iterator.next();
			} finally {
				SimpleAppendQuiXQueue.this.rwl.readLock().unlock();
			}
		}

		@Override
		public void close() {
			if (DEBUG)
				System.out.println("CreateSimpleQEQ (close reader " + SimpleAppendQuiXQueue.this.readerCount + ") : "
						+ SimpleAppendQuiXQueue.this.rank);
			if (!this.readerClosed) {
				this.readerClosed = true;
				SimpleAppendQuiXQueue.this.readerCount--;
				if (DEBUG)
					System.out.println("CreateSimpleQEQ (really close reader " + SimpleAppendQuiXQueue.this.readerCount
							+ ") : " + SimpleAppendQuiXQueue.this.rank);
				if (SimpleAppendQuiXQueue.this.readerCount == 0) {
					SimpleAppendQuiXQueue.this.events.clear();
					if (DEBUG)
						System.out.println("CreateSimpleQEQ (CLEAR) : " + SimpleAppendQuiXQueue.this.rank);
				}
			}
		}

	}

	// @Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xmlcalabash.stream.util.shared.IQuixEventQueue#registerReader()
	 */
	@Override
	public IQuiXStream<T> registerReader() {
		// if (startWorking) throw new RuntimeException("Cannot register reader
		// after the queue already been fed");
		if (DEBUG)
			System.out.println("CreateSimpleQEQ (open reader " + this.readerCount + ") : " + this.rank);
		this.readerCount++;
		return new LocalReader();
	}

	// register proxy reader is there for for-each loop where you never know the
	// true number of reader
	// the idea is that a future version of this object should be able to drop
	// the data
	// that would never be reused by being sure that each of the registered
	// reader
	// has read it
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xmlcalabash.stream.util.shared.IQuixEventQueue#registerProxyReader()
	 */
	@Override
	public ProxyReader<T> registerProxyReader() {

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xmlcalabash.stream.util.shared.IQuixEventQueue#setReaderCount()
	 */
	@Override
	public void setReaderCount(final int count) {
		this.readerCount = count;
	}

	// public synchronized void clear() {
	// synchronized(events) {
	// events.clear();
	// }
	// }

	// private void clean() {
	// if (readerCount == 0) {
	// events.clear();
	// }
	// }

	@Override
	public void closeReaderRegistration() {
		this.maxReader = this.readerCount;
	}

	private static final int MAX_PRODUCE = 10000000;
	private static final int LOG_MODULO = MAX_PRODUCE / 10;

	private static class SimpleProducer implements Runnable {
		private final IQuiXQueue<AQuiXEvent> qeq;

		SimpleProducer(final IQuiXQueue<AQuiXEvent> qeq) {
			this.qeq = qeq;
		}

		@Override
		public void run() {

			int i = MAX_PRODUCE;
			while (i-- > 0) {
				// try {
				this.qeq.append(AQuiXEvent.getStartDocument(QuiXCharStream.fromSequence(String.valueOf(i))));

				if (i % LOG_MODULO == 0)
					System.out.println("Produce " + i);
				// Thread.sleep(1);
				// } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				// }
			}
			this.qeq.close();
		}
	}

	private static class SimpleConsumer implements Runnable {
		private final IQuiXStream<AQuiXEvent> qs;
		private final int rank;

		SimpleConsumer(final IQuiXStream<AQuiXEvent> qs, final int rank) {
			this.qs = qs;
			this.rank = rank;
		}

		@Override
		public void run() {
			try {
				int i = 0;
				while (this.qs.hasNext()) {
					this.qs.next();
					i++;
					if (i % LOG_MODULO == 0)
						System.out.println("Consume " + this.rank);
				}
			} catch (final QuiXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.qs.close();
		}
	}

	public static void main(final String[] args) {
		System.out.println("Start");
		System.out.println("Create QuixEventQueue");
		// IQueue<QuixEvent> qeq = new SimpleAppendQueue<QuixEvent>();
		final SmartAppendQuiXQueue<AQuiXEvent> qeq = new SmartAppendQuiXQueue<AQuiXEvent>();
		final int READER_COUNT = 20;
		qeq.setReaderCount(READER_COUNT);
		System.out.println("Create SimpleProducer");
		final SimpleProducer sp = new SimpleProducer(qeq);
		for (int i = 0; i < READER_COUNT; i++) {
			System.out.println("Create SimpleConsumer");
			final SimpleConsumer sc = new SimpleConsumer(qeq.registerReader(), i);
			final Thread t = new Thread(sc);
			System.out.println("Start SimpleConsumer");
			t.start();
		}
		final Thread t = new Thread(sp);
		System.out.println("Start SimpleProducer");
		t.start();
	}

}
