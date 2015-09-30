package innovimax.quixproc.datamodel.in.json;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;

import innovimax.quixproc.datamodel.QuiXException;
import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.in.AQuiXEventStreamReader;
import innovimax.quixproc.datamodel.in.AStreamSource;
import innovimax.quixproc.datamodel.in.AStreamSource.JSONStreamSource;

public class JSONQuiXEventStreamReader extends AQuiXEventStreamReader {
	private final JsonFactory ifactory;
	public JSONQuiXEventStreamReader(JSONStreamSource current) {
		 this.ifactory = new JsonFactory();
		 ifactory.s
		// todo
	}
	@Override
	protected AQuiXEvent load(AStreamSource current) throws QuiXException {
		return load(((AStreamSource.JSONStreamSource) current).asInputStream());
	}

	protected AQuiXEvent load(InputStream current) throws QuiXException {
		try {
			this.ifactory.createParser(current);
		} catch (JsonParseException e) {
			throw new QuiXException(e);
		} catch (IOException e) {
			throw new QuiXException(e);
		}
		
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
