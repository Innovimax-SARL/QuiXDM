/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.convert;

import innovimax.quixproc.datamodel.QuiXException;

public class QuiXConvertException extends QuiXException {

	public QuiXConvertException() {
		this(null, null);
	}

	public QuiXConvertException(String message) {
		this(message, null);
	}

	public QuiXConvertException(Throwable cause) {
		this(null, cause);
	}

	private QuiXConvertException(String message, Throwable cause) {
		super(message, cause);
	}

}
