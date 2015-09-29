package innovimax.quixproc.datamodel.generator.test;

import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

import innovimax.quixproc.datamodel.QuiXException;
import innovimax.quixproc.datamodel.ValidQuiXTokenStream;
import innovimax.quixproc.datamodel.generator.AGenerator;
import innovimax.quixproc.datamodel.generator.AGenerator.Unit;
import innovimax.quixproc.datamodel.generator.AGenerator.Variation;
import innovimax.quixproc.datamodel.generator.ATreeGenerator;
import innovimax.quixproc.datamodel.generator.xml.AXMLGenerator;
import innovimax.quixproc.datamodel.in.QuiXEventStreamReader;

public class TestGenerator {

	@Test
	public void testXML() throws QuiXException {
		AGenerator generator = AXMLGenerator.instance(ATreeGenerator.Type.HIGH_NODE_DENSITY, AXMLGenerator.SpecialType.STANDARD);
		InputStream is = generator.getInputStream(1, Unit.MBYTE, Variation.NO_VARIATION);
		QuiXEventStreamReader xqesr = new QuiXEventStreamReader(new StreamSource(is));
		ValidQuiXTokenStream vqxs = new ValidQuiXTokenStream(xqesr);
		while(vqxs.hasNext()) {
			vqxs.next();
		}
	}

}
