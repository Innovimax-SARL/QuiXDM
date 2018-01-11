/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.shared;

public interface ISimpleQuiXQueue<T> {

	/**
	 * append an event on this queue
	 * 
	 * @param event
	 */
	void append(T event);

	/**
	 * close the queue
	 */
	void close();

}