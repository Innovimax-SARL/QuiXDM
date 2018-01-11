/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.in.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser.Feature;

public class JSONQuiXEventStreamReader extends AJSONYAMLQuiXEventStreamReader {
	public JSONQuiXEventStreamReader() {
		super(new JsonFactory());
		this.ifactory.enable(Feature.STRICT_DUPLICATE_DETECTION);
	}

}
