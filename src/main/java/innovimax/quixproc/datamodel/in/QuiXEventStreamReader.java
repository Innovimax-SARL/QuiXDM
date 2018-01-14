/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.in;

import innovimax.quixproc.datamodel.IQuiXStream;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;

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
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

public class QuiXEventStreamReader implements IQuiXEventStreamReader, CallBack {

	private final Iterator<AStreamSource> sources;
	private final EnumMap<Type, AQuiXEventStreamReader> delegates;
	private AQuiXEventStreamReader delegate;

	private QuiXEventStreamReader(final Source... sources) {
		this(AStreamSource.instances(sources));
	}

	public QuiXEventStreamReader(final AStreamSource ass) {
		this(Collections.singleton(ass));
	}

	private QuiXEventStreamReader(final Iterable<AStreamSource> sources) {
		this.sources = sources.iterator();
		this.delegates = new EnumMap<Type, AQuiXEventStreamReader>(Type.class);
		this.delegate = null;
	}

	private AQuiXEvent loadSource() {
		final AStreamSource current = this.sources.next();
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

	static class AQuiXEventAndState {
		final AQuiXEvent event;
		final State state;

		public AQuiXEventAndState(final AQuiXEvent event, final State state) {
			this.event = event;
			this.state = state;
		}
	}

	public enum State {
		INIT, START_SEQUENCE, START_SOURCE, END_SOURCE, FINISH
	}

	private State state = State.INIT;

	@Override
	public void setState(final State state) {
		this.state = state;
	}

	@Override
	public State getState() {
		return this.state;
	}

	@Override
	public AQuiXEvent next() {
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
	public AQuiXEvent processEndSource() {
		if (this.sources.hasNext()) {
			// there is still sources
			return loadSource();
		}
		final AQuiXEvent event = AQuiXEvent.getEndSequence();
		this.state = State.FINISH;
		return event;
	}

	@Override
	public void close() {
		for (final AQuiXEventStreamReader aqxsr : this.delegates.values()) {
			aqxsr.close();
		}
	}

	public static void main(final String[] args) {

		final IQuiXStream qesr = new QuiXEventStreamReader(
				new StreamSource("/Users/innovimax/tmp/gs1/new/1000/1000_KO_22062015.xml"),
				new StreamSource("/Users/innovimax/tmp/gs1/new/1000/1000_OK_22062015.xml"));
		while (qesr.hasNext()) {
			System.out.println(qesr.next());
		}
	}

}
