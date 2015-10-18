package innovimax.quixproc.datamodel.in;

import java.util.LinkedList;
import java.util.Queue;

import innovimax.quixproc.datamodel.event.AQuiXEvent;

public abstract class AQuiXEventBufferStreamReader extends AQuiXEventStreamReader {
	protected final Queue<AQuiXEvent> buffer = new LinkedList<AQuiXEvent>();

	@Override
	public void reinitialize(AStreamSource current) {
		//
		this.buffer.clear();
	}

}
