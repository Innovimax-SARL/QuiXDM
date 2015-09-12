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
package innovimax.quixproc.datamodel.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import innovimax.quixproc.datamodel.IQuixStream;
import innovimax.quixproc.datamodel.QuixException;
import innovimax.quixproc.datamodel.event.AQuixEvent;

/**
 * Simple implementation of {@link Queue} interface It uses a simple {@link ArrayList} and {@link ReentrantReadWriteLock}
 * to do the job It's far from efficient
 * 
 * @author innovimax
 */
public class SimpleAppendQuixQueue<T> implements IQuixQueue<T> {

  private final static boolean         DEBUG = false;
  private final List<T>                events;
  private final ReentrantReadWriteLock rwl;
  //
  private int                          readerCount  = 0;
  private boolean                      closed       = false;
  private boolean                      startWorking = false;
  //
  private static int                   counter      = 0;
  private final int                    rank;
  private int                          maxReader;

  public SimpleAppendQuixQueue() {
    events = new ArrayList<T>();
    rwl = new ReentrantReadWriteLock(true);
    counter++;
    rank = counter;
    if (DEBUG) System.out.println("CreateSimpleQEQ : " + rank);
//    Thread.dumpStack();
  }

  /*
   * (non-Javadoc)
   * @see com.xmlcalabash.stream.util.shared.IQuixEventQueue#add(com.xmlcalabash.stream.util.QuixEvent)
   */
  @Override
  public void append(T event) {
    startWorking = true;
    rwl.writeLock().lock();
    try {
      if (closed) throw new RuntimeException("Cannot append to a closed stream");
      events.add(event);
    } finally {
      rwl.writeLock().unlock();
    }
  }

  /*
   * (non-Javadoc)
   * @see com.xmlcalabash.stream.util.shared.IQuixEventQueue#close()
   */
  @Override
  public void close() {
    rwl.writeLock().lock();
    try {
      if (closed) throw new RuntimeException("Already closed");
      closed = true;
    } finally {
      rwl.writeLock().unlock();
    }
    if (DEBUG) System.out.println("CreateSimpleQEQ (closed) : " + rank);
  }

  private class LocalReader implements IQuixStream<T> {
    // private final Iterator<QuixEvent> iterator;
    private int     i            = 0;
    private boolean readerClosed = false;

    private LocalReader() {
      // this.iterator = events.iterator();
    }

    @Override
    public boolean hasNext() {
      if (readerClosed) throw new RuntimeException("Reader already closed");
      rwl.readLock().lock();
      try {
        if (i < events.size()) return true;
        // if (iterator.hasNext()) return true;
        // si c'est faux ca d�pends de close
        while (i >= events.size()/* !iterator.hasNext() */) {
          if (closed) return false;
          rwl.readLock().unlock();
//          System.out.println("hasNextBeforeYield");
          Thread.yield();
//          System.out.println("hasNextAfterYield");
          rwl.readLock().lock();
        }
        // il n'y a pas de concurrence sur la lecture, chacun lit � son rythme
        // donc si il y a un element il l'est toujours
        return true;
      } finally {
        rwl.readLock().unlock();
      }
    }

    @Override
    public T next() {
      if (readerClosed) throw new RuntimeException("Reader already closed");
      rwl.readLock().lock();
      try {
        if (i < events.size()) { return events.get(i++); }
        // if (iterator.hasNext()) return iterator.next();
        // si c'est faux ca d�pends de close
        while (i >= events.size()/* !iterator.hasNext() */) {
          if (closed) return null;
          rwl.readLock().unlock();
//          System.out.println("nextBeforeYield");
          Thread.yield();
//          System.out.println("nextAfterYield");
          rwl.readLock().lock();
        }
        // il n'y a pas de concurrence sur la lecture, chacun lit � son rythme
        // donc si il y a un element il l'est toujours
        return events.get(i++);
        // return iterator.next();
      } finally {
        rwl.readLock().unlock();
      }
    }

    @Override
    public void close() {
      if (DEBUG) System.out.println("CreateSimpleQEQ (close reader "+readerCount+") : " + rank);
      if (!readerClosed) {
        readerClosed = true;
        readerCount--;
        if (DEBUG) System.out.println("CreateSimpleQEQ (really close reader "+readerCount+") : " + rank);
        if (readerCount == 0) {
          events.clear();
          if (DEBUG) System.out.println("CreateSimpleQEQ (CLEAR) : " + rank);
        }
      }
    }

  }

  // @Override
  /*
   * (non-Javadoc)
   * @see com.xmlcalabash.stream.util.shared.IQuixEventQueue#registerReader()
   */
  @Override
  public IQuixStream<T> registerReader() {
    // if (startWorking) throw new RuntimeException("Cannot register reader after the queue already been fed");
    if (DEBUG) System.out.println("CreateSimpleQEQ (open reader "+readerCount+") : " + rank);
    readerCount++;
    return new LocalReader();
  }

  // register proxy reader is there for for-each loop where you never know the true number of reader
  // the idea is that a future version of this object should be able to drop the data
  // that would never be reused by being sure that each of the registered reader
  // has read it
  /*
   * (non-Javadoc)
   * @see com.xmlcalabash.stream.util.shared.IQuixEventQueue#registerProxyReader()
   */
  @Override
  public ProxyReader<T> registerProxyReader() {

    return null;
  }

  /*
   * (non-Javadoc)
   * @see com.xmlcalabash.stream.util.shared.IQuixEventQueue#setReaderCount()
   */
   @Override
   public void setReaderCount(int count) {
     readerCount = count;
   }

  // public synchronized void clear() {
  // synchronized(events) {
  // events.clear();
  // }
  // }

//  private void clean() {
//    if (readerCount == 0) {
//      events.clear();
//    }
//  }

  @Override
  public void closeReaderRegistration() {
    this.maxReader = readerCount;
  }
  final static int MAX_PRODUCE = 10000000;
  final static int LOG_MODULO  = MAX_PRODUCE / 10;
  
  private static class SimpleProducer implements Runnable {
    private final IQuixQueue<AQuixEvent> qeq;

    SimpleProducer(IQuixQueue<AQuixEvent> qeq) {
      this.qeq = qeq;
    }

    @Override
    public void run() {
      
      int i = MAX_PRODUCE;
      while (i-- > 0) {
//        try {
          qeq.append(AQuixEvent.getStartDocument(""+i));
          
          if (i % LOG_MODULO == 0) System.out.println("Produce " + i);
//          Thread.sleep(1);
//        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
//          e.printStackTrace();
//        }
      }
      qeq.close();
    }
  }

  private static class SimpleConsumer implements Runnable {
    private final IQuixStream<AQuixEvent> qs;
    private final int        rank;

    SimpleConsumer(IQuixStream<AQuixEvent> qs, int rank) {
      this.qs = qs;
      this.rank = rank;
    }

    @Override
    public void run() {
      try {
        int i = 0;
        while (qs.hasNext()) {
          qs.next();
          i++;
          if (i % LOG_MODULO == 0) System.out.println("Consume " + rank);
        }
      } catch (QuixException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      qs.close();
    }
  }

  public static void main(String[] args) {
    System.out.println("Start");
    System.out.println("Create QuixEventQueue");
//  IQueue<QuixEvent> qeq = new SimpleAppendQueue<QuixEvent>();
    SmartAppendQuixQueue<AQuixEvent> qeq = new SmartAppendQuixQueue<AQuixEvent>();
    final int READER_COUNT = 20;
    qeq.setReaderCount(READER_COUNT);
    System.out.println("Create SimpleProducer");
    SimpleProducer sp = new SimpleProducer(qeq);
    for (int i = 0; i < READER_COUNT; i++) {
      System.out.println("Create SimpleConsumer");
      SimpleConsumer sc = new SimpleConsumer(qeq.registerReader(), i);
      Thread t = new Thread(sc);
      System.out.println("Start SimpleConsumer");
      t.start();
    }
    Thread t = new Thread(sp);
    System.out.println("Start SimpleProducer");
    t.start();
  }
 
}
