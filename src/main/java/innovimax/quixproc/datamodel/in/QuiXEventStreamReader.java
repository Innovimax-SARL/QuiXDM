/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.in;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;

import innovimax.quixproc.datamodel.QuiXException;
import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.event.IQuiXEventStreamReader;
import innovimax.quixproc.datamodel.in.AQuiXEventStreamReader.CallBack;
import innovimax.quixproc.datamodel.in.AStreamSource.Type;
import innovimax.quixproc.datamodel.in.csv.CSVQuiXEventStreamReader;
import innovimax.quixproc.datamodel.in.html.HTMLQuiXEventStreamReader;
import innovimax.quixproc.datamodel.in.json.JSONQuiXEventStreamReader;
import innovimax.quixproc.datamodel.in.rdf.RDFQuiXEventStreamReader;
import innovimax.quixproc.datamodel.in.xml.XMLQuiXEventStreamReader;
import innovimax.quixproc.datamodel.in.yaml.YAMLQuiXEventStreamReader;

public class QuiXEventStreamReader implements IQuiXEventStreamReader, CallBack {

	protected final Iterator<AStreamSource> sources;
	private final EnumMap<Type, AQuiXEventStreamReader> delegates;
	private AQuiXEventStreamReader delegate;

	public QuiXEventStreamReader(javax.xml.transform.Source... sources) {
		this(AStreamSource.instances(sources));
	}

	public QuiXEventStreamReader(AStreamSource ass) {
		this(Collections.singleton(ass));
	}

	public QuiXEventStreamReader(Iterable<AStreamSource> sources) {
		this.sources = sources.iterator();
		this.delegates = new EnumMap<Type, AQuiXEventStreamReader>(Type.class);
		this.delegate = null;
	}

	private AQuiXEvent loadSource() throws QuiXException {
		AStreamSource current = this.sources.next();
		if (this.delegates.containsKey(current.type)) {
			this.delegate = this.delegates.get(current.type);
			this.delegate.reinitialize(current);
		} else {
			switch (current.type) {
			case JSON:
				this.delegate = new JSONQuiXEventStreamReader();
				break;
			case XML:
				this.delegate = new XMLQuiXEventStreamReader();
				break;
			case YAML:
				this.delegate = new YAMLQuiXEventStreamReader();
				break;
			case HTML:
				this.delegate = new HTMLQuiXEventStreamReader();
				break;
			case CSV:
				this.delegate = new CSVQuiXEventStreamReader();
				break;
			case RDF:
				this.delegate = new RDFQuiXEventStreamReader();
				break;
			default:
				this.delegate = null;
			}
			this.delegates.put(current.type, this.delegate);
		}
		return this.delegate.load(current);
	}

	@Override
	public boolean hasNext() {
		return this.state != State.FINISH;
	}

	public static class AQuiXEventAndState {
		final AQuiXEvent event;
		final State state;

		public AQuiXEventAndState(AQuiXEvent event, State state) {
			this.event = event;
			this.state = state;
		}
	}

	public enum State {
		INIT, START_SEQUENCE, START_SOURCE, END_SOURCE, FINISH
	}

	private State state = State.INIT;

	@Override
	public void setState(State state) {
		this.state = state;
	}

	@Override
	public State getState() {
		return this.state;
	}

	@Override
	public AQuiXEvent next() throws QuiXException {
		// System.out.println(state);
		final AQuiXEvent event;
		switch (this.state) {
		case FINISH:
			return null;
		case INIT:
			event = AQuiXEvent.getStartSequence();
			this.state = State.START_SEQUENCE;
			return event;
		case START_SEQUENCE:
			if (!this.sources.hasNext()) {
				event = AQuiXEvent.getEndSequence();
				this.state = State.FINISH;
				return event;
			}
			// there is at least one source
			this.state = State.START_SOURCE;
			return loadSource();
		case END_SOURCE:
		case START_SOURCE:
			// dealt with inside process() via callback
			break;
		default:
		}
		return this.delegate.process(this);
	}

	@Override
	public AQuiXEvent processEndSource() throws QuiXException {
		if (this.sources.hasNext()) {
			// there is still sources
			return loadSource();
		}
		AQuiXEvent event = AQuiXEvent.getEndSequence();
		this.state = State.FINISH;
		return event;
	}

	@Override
	public void close() {
		for (AQuiXEventStreamReader aqxsr : this.delegates.values()) {
			aqxsr.close();
		}
	}

	public static void main(String[] args) throws QuiXException {

		QuiXEventStreamReader qesr = new QuiXEventStreamReader(
				new javax.xml.transform.stream.StreamSource("/Users/innovimax/tmp/gs1/new/1000/1000_KO_22062015.xml"),
				new javax.xml.transform.stream.StreamSource("/Users/innovimax/tmp/gs1/new/1000/1000_OK_22062015.xml"));
		while (qesr.hasNext()) {
			System.out.println(qesr.next());
		}
	}

}
