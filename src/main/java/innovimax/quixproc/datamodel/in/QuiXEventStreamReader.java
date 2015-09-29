package innovimax.quixproc.datamodel.in;

import java.util.EnumMap;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import innovimax.quixproc.datamodel.QuiXException;
import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.event.IQuiXEventStreamReader;
import innovimax.quixproc.datamodel.in.json.JSONQuiXEventStreamReader;
import innovimax.quixproc.datamodel.in.xml.XMLQuiXEventStreamReader;

public class QuiXEventStreamReader implements IQuiXEventStreamReader, AQuiXEventStreamReader.CallBack {
	
	protected final Iterator<AStreamSource> sources;
	private final EnumMap<AStreamSource.Type, AQuiXEventStreamReader> delegates;
	private AQuiXEventStreamReader delegate;
	
	public QuiXEventStreamReader(javax.xml.transform.Source... sources) {
		this(AStreamSource.instances(sources));
	}

	
	public QuiXEventStreamReader(Iterable<AStreamSource> sources) {
		this.sources = sources.iterator();
		this.delegates = new EnumMap<AStreamSource.Type, AQuiXEventStreamReader>(AStreamSource.Type.class);
		this.delegate = null;
	}
	private AQuiXEvent loadSource() throws QuiXException {
		AStreamSource current = this.sources.next();
		switch (current.type) {
		case JSON:
			if (!delegates.containsKey(current.type)) {
			  this.delegate = new JSONQuiXEventStreamReader((AStreamSource.JSONStreamSource )current);
			} else {
			  this.delegate = this.delegates.get(current.type);
			  this.delegate.reinitialize(current);
			}
			break;
		case XML:
			if (!delegates.containsKey(current.type)) {
  				this.delegate = new XMLQuiXEventStreamReader((AStreamSource.XMLStreamSource )current);
				} else {
				  this.delegate = this.delegates.get(current.type);
				  this.delegate.reinitialize(current);
				}
			break;
		default:
			this.delegate = null;
			break;
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
		AQuiXEvent event = null;
		switch (state) {
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
		}
		return  this.delegate.process(this);
	}
	@Override
	public AQuiXEvent processEndSource() throws QuiXException {
		AQuiXEvent event = null;
		if (this.sources.hasNext()) {
			// there is still sources
			return loadSource();
		}
		event = AQuiXEvent.getEndSequence();
		this.state = State.FINISH;
		return event;
	}

	@Override
	public void close() {
		for(AQuiXEventStreamReader aqxsr : this.delegates.values()) {
			aqxsr.close();
		}	
	}
	
	public static void main(String[] args) throws XMLStreamException, QuiXException {

		QuiXEventStreamReader qesr = new QuiXEventStreamReader(
				new javax.xml.transform.stream.StreamSource(
				"/Users/innovimax/tmp/gs1/new/1000/1000_KO_22062015.xml"), 
				new javax.xml.transform.stream.StreamSource(
						"/Users/innovimax/tmp/gs1/new/1000/1000_OK_22062015.xml"));
		while (qesr.hasNext()) {
			System.out.println(qesr.next());
		}
	}


}
