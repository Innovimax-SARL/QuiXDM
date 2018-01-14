/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.in;

import java.util.LinkedList;
import java.util.Queue;

import innovimax.quixproc.datamodel.event.AQuiXEvent;

public abstract class AQuiXEventBufferStreamReader extends AQuiXEventStreamReader {
	protected final Queue<AQuiXEvent> buffer = new LinkedList<AQuiXEvent>();

	@Override
	public void reinitialize(final AStreamSource current) {
		//
		this.buffer.clear();
	}

}
