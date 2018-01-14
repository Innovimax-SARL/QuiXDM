/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.in.rdf;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedTriplesStream;

import innovimax.quixproc.datamodel.QuiXCharStream;
import innovimax.quixproc.datamodel.QuiXException;
import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.in.AQuiXEventBufferStreamReader;
import innovimax.quixproc.datamodel.in.AStreamSource;
import innovimax.quixproc.datamodel.in.AStreamSource.RDFStreamSource;
import innovimax.quixproc.datamodel.in.QuiXEventStreamReader.State;
import org.apache.jena.riot.system.StreamRDF;

public class RDFQuiXEventStreamReader extends AQuiXEventBufferStreamReader {
	// private PipedRDFIterator<Tuple<Node>> iter;
	private PipedRDFIterator<Triple> iter;
	private ExecutorService executor;

	@Override
	protected AQuiXEvent load(final AStreamSource current) {
		return load((RDFStreamSource) current);
	}

	private AQuiXEvent load(final RDFStreamSource source) {
		// Create a PipedRDFStream to accept input and a PipedRDFIterator to
		// consume it
		// You can optionally supply a buffer size here for the
		// PipedRDFIterator, see the documentation for details about recommended
		// buffer sizes
		// this.iter = new PipedRDFIterator<Tuple<Node>>();
		this.iter = new PipedRDFIterator<Triple>();
		// final PipedRDFStream<Tuple<Node>> tripleStream = new
		// PipedTuplesStream(this.iter);
		final StreamRDF tripleStream = new PipedTriplesStream(this.iter);
		final TypedInputStream tis = source.asTypedInputStream();
		// PipedRDFStream and PipedRDFIterator need to be on different threads
		this.executor = Executors.newSingleThreadExecutor();

		// Create a runnable for our parser thread
		final Runnable parser = () -> {
            // Call the parsing process.
            // System.out.println("started thread before");
            RDFDataMgr.parse(tripleStream, tis, Lang.N3);
            // System.out.println("started thread after");
        };

		// Start the parser on another thread
		this.executor.execute(parser);
		return AQuiXEvent.getStartRDF();
	}

	@Override
	protected AQuiXEvent process(final CallBack callback) {
		// We will consume the input on the main thread here
		// System.out.println("process");
		try {
			if (!this.buffer.isEmpty()) {
				return this.buffer.poll();
			}
			if (!this.iter.hasNext() && callback.getState() == State.START_SOURCE) {
				// special case if the buffer is empty but the document has not
				// been closed
				final AQuiXEvent event = AQuiXEvent.getEndRDF();
				callback.setState(State.END_SOURCE);
				return event;
			}
			if (callback.getState() == State.END_SOURCE) {
				return callback.processEndSource();
			}
			// this iter has next
			final Triple next = this.iter.next();
			this.buffer.add(AQuiXEvent.getSubject(QuiXCharStream.fromSequence(next.getSubject().toString())));
			this.buffer.add(AQuiXEvent.getObject(QuiXCharStream.fromSequence(next.getObject().toString())));
			this.buffer.add(AQuiXEvent.getEndPredicate(QuiXCharStream.fromSequence(next.getPredicate().toString())));
			return AQuiXEvent.getStartPredicate(QuiXCharStream.fromSequence(next.getPredicate().toString()));
			// something is bugging with Tuple
			/*
			 * 
			 * Tuple<Node> next = this.iter.next(); System.out.println("next : "
			 * +next);
			 * this.buffer.add(AQuiXEvent.getSubject(QuiXCharStream.fromSequence
			 * (next.get(0).toString())));
			 * this.buffer.add(AQuiXEvent.getObject(QuiXCharStream.fromSequence(
			 * next.get(2).toString()))); if (next.size() == 4) // handle QUAD
			 * this.buffer.add(AQuiXEvent.getGraph(QuiXCharStream.fromSequence(
			 * next.get(3).toString())));
			 * this.buffer.add(AQuiXEvent.getEndPredicate(QuiXCharStream.
			 * fromSequence(next.get(1).toString()))); return
			 * AQuiXEvent.getStartPredicate(QuiXCharStream.fromSequence(next.get
			 * (1).toString()));
			 */
		} catch (final Exception e) {
			throw new QuiXException(e);
		}
	}

	@Override
	public void close() {
		this.executor.shutdownNow();
	}

}
