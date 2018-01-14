/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.in;

import innovimax.quixproc.datamodel.QuiXException;
import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.in.QuiXEventStreamReader.State;

public abstract class AQuiXEventStreamReader {

	public interface CallBack {
		State getState();

		void setState(State state);

		AQuiXEvent processEndSource();
	}

	protected AQuiXEventStreamReader() {
	}

	protected abstract AQuiXEvent load(AStreamSource current);

	protected abstract AQuiXEvent process(CallBack callback);

	public abstract void reinitialize(AStreamSource current);

	public abstract void close();

}
