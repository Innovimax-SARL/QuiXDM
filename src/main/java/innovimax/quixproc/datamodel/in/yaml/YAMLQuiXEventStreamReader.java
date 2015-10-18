package innovimax.quixproc.datamodel.in.yaml;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import innovimax.quixproc.datamodel.in.json.AJSONYAMLQuiXEventStreamReader;

public class YAMLQuiXEventStreamReader extends AJSONYAMLQuiXEventStreamReader {
	public YAMLQuiXEventStreamReader() {
		super(new YAMLFactory());
	}
}
