package innovimax.quixproc.datamodel.in.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser.Feature;

public class JSONQuiXEventStreamReader extends AJSONYAMLQuiXEventStreamReader {
	public JSONQuiXEventStreamReader() {
		super(new JsonFactory());
		this.ifactory.enable(Feature.STRICT_DUPLICATE_DETECTION);
	}

}
