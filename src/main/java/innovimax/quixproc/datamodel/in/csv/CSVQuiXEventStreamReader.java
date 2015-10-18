package innovimax.quixproc.datamodel.in.csv;

import innovimax.quixproc.datamodel.QuiXException;
import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.in.AQuiXEventStreamReader;
import innovimax.quixproc.datamodel.in.AStreamSource;

public class CSVQuiXEventStreamReader extends AQuiXEventStreamReader  {
	
	public CSVQuiXEventStreamReader() {
	}
	@Override
	protected AQuiXEvent load(AStreamSource current) throws QuiXException {
		
		return AQuiXEvent.getStartTable();
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
