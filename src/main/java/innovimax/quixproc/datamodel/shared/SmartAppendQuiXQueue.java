/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.shared;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

//import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import innovimax.quixproc.datamodel.IQuiXStream;

public final class SmartAppendQuiXQueue<T> implements IQuiXQueue<T> {
	private static final int DEBUG_LEVEL = 0; // 0 none, 1 simple, 2 detailled
	private static int counter = 0;
	private static final Set<Integer> open = Collections.synchronizedSet(new TreeSet<Integer>());
	private LinkedItem<T> head;
	private LinkedItem<T> current;
	private int readerCount;
	private int currentReader;
	private final int rank;

	/**
	 * Item of manul LinkedList
	 * 
	 * @author innovimax
	 */
	private static class LinkedItem<T> {
		// private static class BooleanLatch {
		//
		// private static class Sync extends AbstractQueuedSynchronizer {
		// boolean isSignalled() {
		// return getState() != 0;
		// }
		//
		// protected int tryAcquireShared(int ignore) {
		// return isSignalled() ? 1 : -1;
		// }
		//
		// protected boolean tryReleaseShared(int ignore) {
		// setState(1);
		// return true;
		// }
		// }
		//
		// private final Sync sync = new Sync();
		//
		// // public boolean isSignalled() { return sync.isSignalled(); }
		// public void signal() {
		// sync.releaseShared(1);
		// }
		//
		// public void await() throws InterruptedException {
		// sync.acquireSharedInterruptibly(1);
		// }
		// }

		static final LinkedItem END = null;
		private final T event;
		// private final BooleanLatch latch;
		private Object lock;
		//
		private LinkedItem<T> next;

		LinkedItem(final T event) {
			this.event = event;
			// this.latch = new BooleanLatch();
			this.lock = new Object();
		}

		T get() {
			return this.event;
		}
		// see to get around locking http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
		LinkedItem<T> getNext() {
			try {
				// this.latch.await();
				if (this.lock != null) {
					if (this.lock != null) {
						if (this.lock != null) {
							if (this.lock != null) {
								synchronized (this.lock) {
									if (this.lock != null)
										this.lock.wait();
								}
							}
						}
					}
				}
				return this.next;
			} catch (final InterruptedException e) {
				e.printStackTrace();
				return null;
			}
		}

		void setNext(final LinkedItem<T> li) {
			this.next = li;
			// this.latch.signal();
			synchronized (this.lock) {
				this.lock.notifyAll();
				this.lock = null;
			}
		}
	}

	private static final class LocalReader<T> implements IQuiXStream<T> {
		private LinkedItem<T> current;
		// debug
		private String name;

		LocalReader(final LinkedItem<T> li) {
			this.current = li;
		}

		private void setName(final String name) {
			this.name = name;
		}

		@Override
		public boolean hasNext() {
			if (this.current == null) {
				System.out.println("hasNext => current == null in LocalReader");
				return false;
			}
			final boolean result = this.current.getNext() != LinkedItem.END;
			if (DEBUG_LEVEL > 1)
				if (!result)
					System.out.println("Reader(" + this.name + ") hasnext=false");
			return result;
		}

		@Override
		public T next() {
			this.current = this.current.getNext();
			final T event = this.current.get();
			if (DEBUG_LEVEL > 1)
				System.out.println(counter + "/" + this.name + "<-" + event);
			return event;
		}

		@Override
		public void close() {
			this.current = LinkedItem.END;
			if (DEBUG_LEVEL > 0)
				System.out.println("Reader(" + this.name + ") closed");
		}
	}

	public SmartAppendQuiXQueue() {
		this.head = new LinkedItem<T>(null);
		this.current = this.head;
		this.currentReader = 0;
		this.readerCount = 0;
		this.rank = counter++;
		if (DEBUG_LEVEL > 0)
			System.out.println("SmartAppendQueue Create " + this.rank);
		if (DEBUG_LEVEL > 0)
			open.add(this.rank);
	}

	/**
	 * !!! NOT THREAD SAFE : Only one thread should do the appending here
	 */
	@Override
	public void append(final T event) {
		if (DEBUG_LEVEL > 1)
			System.out.println(counter + "->" + event);
		final LinkedItem<T> li = new LinkedItem<T>(event);
		this.current.setNext(li);
		this.current = li;
	}

	/**
	 * !!! NOT THREAD SAFE : Only one thread should do the closing here
	 */
	@Override
	public void close() {
		this.current.setNext(LinkedItem.END);
		this.current = LinkedItem.END;
		if (DEBUG_LEVEL > 0)
			open.remove(this.rank);
		if (DEBUG_LEVEL > 0)
			System.out.println("SmartAppendQueue Close : " + this.rank + "; SmartAppend still open : " + open.size()
					+ "; Reader(" + this.currentReader + "/" + this.readerCount + ")");
	}

	@Override
	public IQuiXStream<T> registerReader() {
		final LinkedItem<T> local_head = this.head;
		if (DEBUG_LEVEL > 0)
			System.out.println("head " + this.head);
		final LocalReader<T> l = new LocalReader<T>(local_head);
		if (DEBUG_LEVEL > 0)
			l.setName(this.rank + "/" + this.currentReader + "/" + this.readerCount);
		this.currentReader++;
		if (this.readerCount > this.currentReader) {
			// do nothing there is still reader to register
			// closeReaderRegistration();
		} else if (this.readerCount == this.currentReader) {
			// we reach the maximum so clear head
			closeReaderRegistration();
		} else {
			closeReaderRegistration();
			// readerCount < currentReader
			throw new RuntimeException(
					// System.out.println(
					"readerCount < currentReader : " + this.readerCount + "," + this.currentReader);
		}
		return l;
	}

	private static final class LocalProxyReader<T> implements ProxyReader<T> {
		private LinkedItem<T> head;

		LocalProxyReader(final LinkedItem<T> head) {
			this.head = head;
		}

		@Override
		public IQuiXStream<T> registerReader() {
			return new LocalReader<T>(this.head);
		}

		@Override
		public void closeReaderRegistration() {
			this.head = LinkedItem.END;
		}
	}

	@Override
	public ProxyReader<T> registerProxyReader() {
		// TODO Auto-generated method stub
		return new LocalProxyReader<T>(this.head);
	}

	@Override
	public void setReaderCount(int count) {
        int count1 = count;
        if (count1 < 1)
			count1 = 1;
		this.readerCount = count1;
		if (DEBUG_LEVEL > 0)
			if (count1 >= 18)
				Thread.dumpStack();
		if (DEBUG_LEVEL > 0)
			System.out.println("SetReaderCount (" + this.rank + ") = " + count1);
	}

	@Override
	public void closeReaderRegistration() {
		this.head = LinkedItem.END;
		if (DEBUG_LEVEL > 0)
			System.out.println("closeReaderRegistration()");
	}

}
