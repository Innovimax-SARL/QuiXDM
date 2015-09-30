package innovimax.quixproc.datamodel.in.json;

import innovimax.quixproc.datamodel.QuiXException;
import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.in.AQuiXEventStreamReader;
import innovimax.quixproc.datamodel.in.AStreamSource;
import innovimax.quixproc.datamodel.in.AStreamSource.JSONStreamSource;

public class JSONQuiXEventStreamReader extends AQuiXEventStreamReader {


	public JSONQuiXEventStreamReader(JSONStreamSource current) {
		// todo
	}

	@Override
	protected AQuiXEvent load(AStreamSource current) throws QuiXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected AQuiXEvent process(CallBack callback) throws QuiXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reinitialize(AStreamSource current) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}


}
