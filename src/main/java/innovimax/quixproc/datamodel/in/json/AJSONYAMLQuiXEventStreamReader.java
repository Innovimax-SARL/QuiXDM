/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.in.json;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import innovimax.quixproc.datamodel.QuiXCharStream;
import innovimax.quixproc.datamodel.QuiXException;
import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.in.AQuiXEventStreamReader;
import innovimax.quixproc.datamodel.in.AStreamSource;
import innovimax.quixproc.datamodel.in.AStreamSource.AJSONYAMLStreamSource;
import innovimax.quixproc.datamodel.in.QuiXEventStreamReader.State;

public abstract class AJSONYAMLQuiXEventStreamReader extends AQuiXEventStreamReader {
	final JsonFactory ifactory;
	private JsonParser iparser;

	protected AJSONYAMLQuiXEventStreamReader(final JsonFactory factory) {
		this.ifactory = factory;
	}

	@Override
	protected AQuiXEvent load(final AStreamSource current) {
		return load(((AJSONYAMLStreamSource) current).asInputStream());
	}

	private AQuiXEvent load(final InputStream current) {
		try {
			this.iparser = this.ifactory.createParser(current);
		} catch (final IOException e) {
			throw new QuiXException(e);
		}
		return AQuiXEvent.getStartJSON();
	}

	@Override
	protected AQuiXEvent process(final CallBack callback) {
		// System.out.println("process");
		try {
			if (callback.getState() == State.END_SOURCE) {
				return callback.processEndSource();
			}
			while (true) {
				final JsonToken token = this.iparser.nextToken();
				if (token == null) {
					callback.setState(State.END_SOURCE);
					return AQuiXEvent.getEndJSON();
				}
				switch (token) {
				case END_ARRAY:
					return AQuiXEvent.getEndArray();
				case END_OBJECT:
					return AQuiXEvent.getEndObject();
				case FIELD_NAME:
					return AQuiXEvent.getKeyName(QuiXCharStream.fromSequence(this.iparser.getCurrentName()));
				case NOT_AVAILABLE:
					break;
				case START_ARRAY:
					return AQuiXEvent.getStartArray();
				case START_OBJECT:
					return AQuiXEvent.getStartObject();
				case VALUE_EMBEDDED_OBJECT:
					break;
				case VALUE_FALSE:
					return AQuiXEvent.getValueFalse();
				case VALUE_NULL:
					return AQuiXEvent.getValueNull();
				case VALUE_NUMBER_FLOAT:
					return AQuiXEvent.getValueNumber(this.iparser.getDoubleValue());
				case VALUE_NUMBER_INT:
					return AQuiXEvent.getValueNumber(this.iparser.getDoubleValue());
				case VALUE_STRING:
					return AQuiXEvent.getValueString(QuiXCharStream.fromSequence(this.iparser.getText()));
				case VALUE_TRUE:
					return AQuiXEvent.getValueTrue();
				default:
					break;

				}
				throw new QuiXException("Unknown event " + token);
			}
		} catch (final IOException e) {
			throw new QuiXException(e);
		}

	}

	@Override
	public void reinitialize(final AStreamSource current) {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

}
