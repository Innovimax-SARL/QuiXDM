/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.event;

import innovimax.quixproc.datamodel.IQuiXStream;
import innovimax.quixproc.datamodel.IQuiXToken;

public interface IQuiXEventStreamReader extends IQuiXStream<AQuiXEvent> {
	default IQuiXStream<IQuiXToken> asIQuiXTokenStream() {
		final IQuiXEventStreamReader that = this;
		return new IQuiXStream<IQuiXToken>() {

			@Override
			public boolean hasNext() {
				return that.hasNext();
			}

			@Override
			public IQuiXToken next() {
				return that.next();
			}

			@Override
			public void close() {
				that.close();
			}

		};
	}
}
