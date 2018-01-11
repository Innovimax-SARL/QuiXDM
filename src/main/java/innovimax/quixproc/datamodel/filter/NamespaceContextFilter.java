/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.filter;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import innovimax.quixproc.datamodel.IQuiXStream;
import innovimax.quixproc.datamodel.IQuiXToken;
import innovimax.quixproc.datamodel.QuiXCharStream;

public class NamespaceContextFilter extends AQuiXEventStreamFilter {

	private final Deque<Map<QuiXCharStream, QuiXCharStream>> namespaces;

	public NamespaceContextFilter(IQuiXStream<IQuiXToken> stream) {
		super(stream);
		// TODO Auto-generated constructor stub
		this.namespaces = new LinkedList<Map<QuiXCharStream, QuiXCharStream>>();
	}

	private boolean needCleaning = false;

	@Override
	public IQuiXToken process(IQuiXToken item) {
		if (this.needCleaning) {
			this.namespaces.pollLast();
			this.needCleaning = false;
		}
		switch (item.getType()) {
		case START_ELEMENT:
			this.namespaces.add(new TreeMap<QuiXCharStream, QuiXCharStream>());
			break;
		case END_ELEMENT:
			// differ the cleaning to the next event
			this.needCleaning = true;
			break;
		case NAMESPACE:
			// this.namespaces.getLast().put(qevent.asNamespace().getPrefix(),
			// qevent.asNamespace().getURI());
			break;
		default:
		}
		return item;
	}

	/**
	 * Check at the current moment if the prefix is mapped It returns null if
	 * the prefix is not mapped at this time
	 * 
	 * @param prefix
	 * @return
	 */
	public QuiXCharStream getURI(String prefix) {
		for (Iterator<Map<QuiXCharStream, QuiXCharStream>> iter = this.namespaces.descendingIterator(); iter
				.hasNext();) {
			Map<QuiXCharStream, QuiXCharStream> map = iter.next();
			// TODO prefix is String and map of QuiXCharStream
			if (map.containsKey(QuiXCharStream.fromSequence(prefix)))
				return map.get(prefix);
		}
		return null;
	}
}
