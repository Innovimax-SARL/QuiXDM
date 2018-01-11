/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.shared;

import innovimax.quixproc.datamodel.IQuiXStream;

/**
 * This defines a simple append only on write interface With simple forward
 * reading reader registered
 * 
 * @author innovimax
 *
 */
public interface IQuiXQueue<T> extends ISimpleQuiXQueue<T> {
	/**
	 * This defines a proxy to which we can register
	 * 
	 * @author innovimax
	 *
	 */
	interface ProxyReader<T> {
		/**
		 * The proxy can declare it's own reader
		 * 
		 * @return
		 */
		IQuiXStream<T> registerReader();

		/**
		 * To close the proxy
		 * 
		 * @return
		 */
		void closeReaderRegistration();
	}

	/**
	 * Register a reader here to read only in forward mode
	 * 
	 * @return
	 */
	IQuiXStream<T> registerReader();

	/**
	 * Register proxy reader is there for for-each loops where you never know
	 * the true number of reader. The idea is that implementations of this
	 * interface should be able to garbage collect the data that is sure to not
	 * be read again
	 */
	ProxyReader<T> registerProxyReader();

	/**
	 * This method is called when no more call to registerReader or
	 * registerProxyReader will be done
	 */
	void closeReaderRegistration();

	/**
	 * Implementation of this interface keep a readercount
	 */
	void setReaderCount(int count);

}