package innovimax.quixproc.datamodel.in.json;

import innovimax.quixproc.datamodel.QuiXException;
import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.in.AStreamSource;
import innovimax.quixproc.datamodel.in.AStreamSource.JSONStreamSource;
import innovimax.quixproc.datamodel.in.QuiXEventStreamReader;

public class JSONQuiXEventStreamReader extends QuiXEventStreamReader {

	protected JSONQuiXEventStreamReader(Iterable<AStreamSource> sources) {
		super(sources);
		// TODO Auto-generated constructor stub
	}

	public JSONQuiXEventStreamReader(JSONStreamSource current) {
		super(null);
	}

	@Override
	protected AQuiXEvent load(AStreamSource current) throws QuiXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected AQuiXEvent process() throws QuiXException {
		// TODO Auto-generated method stub
		return null;
	}

}
