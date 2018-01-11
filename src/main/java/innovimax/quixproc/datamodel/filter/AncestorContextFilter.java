/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.filter;

import java.util.Iterator;
import java.util.Stack;

import innovimax.quixproc.datamodel.IQuiXStream;
import innovimax.quixproc.datamodel.IQuiXToken;
import innovimax.quixproc.datamodel.QuiXQName;

public class AncestorContextFilter extends AQuiXEventStreamFilter {

	private final Stack<QuiXQName> ancestors;

	public AncestorContextFilter(IQuiXStream<IQuiXToken> stream) {
		super(stream);
		this.ancestors = new Stack<QuiXQName>();
	}

	@Override
	public IQuiXToken process(IQuiXToken item) {
		switch (item.getType()) {
		case START_ELEMENT:
			// this.ancestors.push(qevent.asNamedEvent().getQName());
			break;
		case END_ELEMENT:
			// this.ancestors.pop();
			break;
		default:
			break;
		}
		return item;
	}

	public Iterator<QuiXQName> ancestors() {
		return this.ancestors.iterator();
	}
}
