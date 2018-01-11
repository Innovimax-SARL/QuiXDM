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

public class DocumentCountFilter extends AQuiXEventStreamFilter {

	private int count;

	public DocumentCountFilter(IQuiXStream<IQuiXToken> stream) {
		super(stream);
		this.count = 0;
	}

	@Override
	public IQuiXToken process(IQuiXToken item) {
		switch (item.getType()) {
		case START_DOCUMENT:
			this.count++;
			break;
		default:
			break;
		}
		return item;
	}

	public int getCurrentDocumentCount() {
		return this.count;
	}
}
