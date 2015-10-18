package innovimax.quixproc.datamodel.in.csv;

import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import innovimax.quixproc.datamodel.QuiXException;
import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.in.AQuiXEventStreamReader;
import innovimax.quixproc.datamodel.in.AStreamSource;
import innovimax.quixproc.datamodel.in.AStreamSource.CSVStreamSource;

public class CSVQuiXEventStreamReader extends AQuiXEventStreamReader  {
	
	private CSVParser parser;
	public CSVQuiXEventStreamReader() {
	}
	@Override
	protected AQuiXEvent load(AStreamSource current) throws QuiXException {
	  return load((CSVStreamSource ) current);
	}
	private AQuiXEvent load(CSVStreamSource source) throws QuiXException {
		
		try {
			this.parser = CSVFormat.EXCEL.parse(source.asReader());
		} catch (IOException e) {
			throw new QuiXException(e);
		}
		return AQuiXEvent.getStartTable();
	}

	@Override
	protected AQuiXEvent process(CallBack callback) throws QuiXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reinitialize(AStreamSource current) {
		try {
			this.parser.close();
			this.parser = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	@Override
	public void close() {
		try {
			this.parser.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
	}

}
