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