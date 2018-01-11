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
import innovimax.quixproc.datamodel.QuiXException;

public abstract class AQuiXEventStreamFilter implements IQuiXStream<IQuiXToken> {
	private final IQuiXStream<IQuiXToken> stream;

	protected AQuiXEventStreamFilter(IQuiXStream<IQuiXToken> stream) {
		this.stream = stream;
	}

	@Override
	public boolean hasNext() throws QuiXException {
		return this.stream.hasNext();
	}

	@Override
	public IQuiXToken next() throws QuiXException {
		IQuiXToken item;
		while ((item = process(this.stream.next().getType())) == null)
			/* NOP */;
		return item;
	}

	@Override
	public void close() {
		this.stream.close();
	}

	public abstract IQuiXToken process(IQuiXToken item);

}
