/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.event;

public class QuiXMatchedEvent implements IQuiXEvent {

	/* properties */

	private final AQuiXEvent event;
	private boolean matched = true;
	private String channels = null;

	/* constructor */

	public QuiXMatchedEvent(AQuiXEvent event) {
		this.event = event;
	}

	public QuiXMatchedEvent(AQuiXEvent event, boolean matched) {
		this.event = event;
		this.matched = matched;
	}

	/* set/get properties */

	@Override
	public AQuiXEvent getEvent() {
		return this.event;
	}

	public QuiXMatchedEvent setMatched(boolean matched) {
		this.matched = matched;
		return this;
	}

	public boolean isMatched() {
		return this.matched;
	}

	@Override
	public String toString() {
		return this.event.toString() + ";" + this.matched;
	}
}
