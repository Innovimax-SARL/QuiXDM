/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel;

public class QuiXException extends RuntimeException {

	protected QuiXException() {
		this(null, null);
	}

	protected QuiXException(String message, Throwable cause) {
		super(message, cause);
	}

	public QuiXException(String message) {
		this(message, null);
	}

	public QuiXException(Throwable cause) {
		this(null, cause);
	}

}
