/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.filter;

import java.util.EnumSet;

import innovimax.quixproc.datamodel.IQuiXStream;
import innovimax.quixproc.datamodel.IQuiXToken;

public class NodeKindFilter extends AQuiXEventStreamFilter {
	enum Kind {
		ATTRIBUTE, TEXT, COMMENT, PI, NAMESPACE
	}

	private final EnumSet<Kind> enumset;

	public NodeKindFilter(final IQuiXStream<IQuiXToken> stream, final EnumSet<Kind> enumset) {
		super(stream);
		this.enumset = enumset;
	}

	@Override
	public IQuiXToken process(final IQuiXToken item) {
		// We cannot extends the list of Kind in order to be able to assert that
		// this process terminate
		switch (item.getType()) {
		case ATTRIBUTE:
			if (this.enumset.contains(Kind.ATTRIBUTE))
				return null;
			return item;
		case TEXT:
			if (this.enumset.contains(Kind.TEXT))
				return null;
			return item;
		case COMMENT:
			if (this.enumset.contains(Kind.COMMENT))
				return null;
			return item;
		case NAMESPACE:
			if (this.enumset.contains(Kind.NAMESPACE))
				return null;
			return item;
		case PROCESSING_INSTRUCTION:
			if (this.enumset.contains(Kind.PI))
				return null;
			return item;
		default:
			return item;
		}
	}

}
