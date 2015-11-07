package innovimax.quixproc.datamodel.in.csv;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import innovimax.quixproc.datamodel.QuiXCharStream;
import innovimax.quixproc.datamodel.QuiXException;
import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.in.AQuiXEventBufferStreamReader;
import innovimax.quixproc.datamodel.in.AStreamSource;
import innovimax.quixproc.datamodel.in.AStreamSource.CSVStreamSource;
import innovimax.quixproc.datamodel.in.QuiXEventStreamReader.State;

public class CSVQuiXEventStreamReader extends AQuiXEventBufferStreamReader {

	private CSVParser parser;
	private Iterator<CSVRecord> iter;

	public CSVQuiXEventStreamReader() {
	}

	@Override
	protected AQuiXEvent load(AStreamSource current) throws QuiXException {
		return load((CSVStreamSource) current);
	}

	private AQuiXEvent load(CSVStreamSource source) throws QuiXException {

		try {
			this.parser = CSVFormat.EXCEL.parse(source.asReader());
			this.iter = this.parser.iterator();
		} catch (IOException e) {
			throw new QuiXException(e);
		}
		this.buffer.add(AQuiXEvent.getStartArray());
		return AQuiXEvent.getStartTable();
	}

	@Override
	protected AQuiXEvent process(CallBack callback) throws QuiXException {
		try {
			if (!this.buffer.isEmpty()) {
				return this.buffer.poll();
			}
			AQuiXEvent event = null;
			if (!this.iter.hasNext() && callback.getState() == State.START_SOURCE) {
				// special case if the buffer is empty but the document has not
				// been closed
				event = AQuiXEvent.getEndArray();
				this.buffer.add(AQuiXEvent.getEndTable());
				callback.setState(State.END_SOURCE);
				return event;
			}
			if (callback.getState() == State.END_SOURCE) {
				return callback.processEndSource();
			}
			// this iter has next
			CSVRecord next = this.iter.next();
			for (String cell : next) {
				this.buffer.add(AQuiXEvent.getValueString(QuiXCharStream.fromSequence(cell)));
			}
			this.buffer.add(AQuiXEvent.getEndArray());
			return AQuiXEvent.getStartArray();
		} catch (Exception e) {
			throw new QuiXException(e);
		}
	}

	@Override
	public void reinitialize(AStreamSource current) {
		try {
			this.parser.close();
			this.parser = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		try {
			this.parser.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
