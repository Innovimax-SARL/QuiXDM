/*
QuiXProc: efficient evaluation of XProc Pipelines.
Copyright (C) 2011-2015 Innovimax
All rights reserved.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package innovimax.quixproc.datamodel.in;

import java.util.Iterator;

import javax.xml.transform.Source;

import innovimax.quixproc.datamodel.QuiXException;
import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.event.IQuiXEventStreamReader;

public class QuiXEventStreamReader implements IQuiXEventStreamReader {

	protected final Iterator<AStreamSource> sources;
	private QuiXEventStreamReader delegate;
	protected QuiXEventStreamReader(Iterable<AStreamSource> sources) {
		this.sources = sources.iterator();
	}

	private AQuiXEvent loadSource() throws QuiXException {
		AStreamSource current = this.sources.next();
		switch(current.type) {
		case JSON:
			this.delegate = new JSONQuiXEventStreamReader(current);
			break;
		case XML:
			this.delegate = new XMLQuiXEventStreamReader(current);
			break;
		default:
			break;
		
		}
		return this.delegate.load(current);		
	}
	protected abstract AQuiXEvent load(AStreamSource current) throws QuiXException;

	@Override
	public boolean hasNext() {
		return this.state != State.FINISH;
	}

	protected enum State {
		INIT, START_SEQUENCE, START_SOURCE, END_SOURCE, FINISH
	}

	protected State state = State.INIT;

	@Override
	public AQuiXEvent next() throws QuiXException {
		AQuiXEvent event = null;
		switch(state) {
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
		return process();
	}

	protected abstract AQuiXEvent process() throws QuiXException ;

	protected AQuiXEvent processEndSource() throws QuiXException {
		AQuiXEvent event = null;
		if (this.sources.hasNext()) {
			// there is still sources
			return loadSource();
		}
		event = AQuiXEvent.getEndSequence();
		this.state = State.FINISH;
		return event;
	}

	public static QuiXEventStreamReader parse(Iterable<Source> sources2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		this.delegate.close();
	}
}
