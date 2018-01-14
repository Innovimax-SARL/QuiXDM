/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.filter;

import innovimax.quixproc.datamodel.IQuiXStream;
import innovimax.quixproc.datamodel.IQuiXToken;

public abstract class AQuiXEventStreamFilter implements IQuiXStream<IQuiXToken> {
	private final IQuiXStream<IQuiXToken> stream;

	protected AQuiXEventStreamFilter(final IQuiXStream<IQuiXToken> stream) {
		this.stream = stream;
	}

	@Override
	public boolean hasNext() {
		return this.stream.hasNext();
	}

	@Override
	public IQuiXToken next() {
		IQuiXToken item;
		while ((item = process(this.stream.next().getType())) == null)
			/* NOP */;
		return item;
	}

	@Override
	public void close() {
		this.stream.close();
	}

	protected abstract IQuiXToken process(IQuiXToken item);

}
