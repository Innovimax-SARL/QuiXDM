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

	public NodeKindFilter(IQuiXStream<IQuiXToken> stream, EnumSet<Kind> enumset) {
		super(stream);
		this.enumset = enumset;
	}

	@Override
	public IQuiXToken process(IQuiXToken item) {
		// We cannot extends the list of Kind in order to be able to assert that
		// this process terminate
		switch (item.getType()) {
		case ATTRIBUTE:
			if (this.enumset.contains(Kind.ATTRIBUTE))
				return null;
			break;
		case TEXT:
			if (this.enumset.contains(Kind.TEXT))
				return null;
			break;
		case COMMENT:
			if (this.enumset.contains(Kind.COMMENT))
				return null;
			break;
		case NAMESPACE:
			if (this.enumset.contains(Kind.NAMESPACE))
				return null;
			break;
		case PROCESSING_INSTRUCTION:
			if (this.enumset.contains(Kind.PI))
				return null;
			break;
		default:
			break;
		}
		return item;
	}

}
