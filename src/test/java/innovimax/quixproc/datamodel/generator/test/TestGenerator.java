package innovimax.quixproc.datamodel.generator.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

import innovimax.quixproc.datamodel.QuiXException;
import innovimax.quixproc.datamodel.ValidQuiXTokenStream;
import innovimax.quixproc.datamodel.generator.AGenerator;
import innovimax.quixproc.datamodel.generator.AGenerator.Unit;
import innovimax.quixproc.datamodel.generator.AGenerator.Variation;
import innovimax.quixproc.datamodel.generator.ATreeGenerator;
import innovimax.quixproc.datamodel.generator.xml.AXMLGenerator;
import innovimax.quixproc.datamodel.generator.xml.AXMLGenerator.SpecialType;
import innovimax.quixproc.datamodel.in.QuiXEventStreamReader;

public class TestGenerator {
	enum Process { READ_BYTE, READ_BUFFER, PARSE }
	public static void testAll(Process process, int size, Unit unit) throws QuiXException, IOException {
		for (ATreeGenerator.Type gtype : EnumSet.of(ATreeGenerator.Type.HIGH_NODE_NAME_SIZE,
				ATreeGenerator.Type.HIGH_NODE_NAME_SIZE, ATreeGenerator.Type.HIGH_NODE_DENSITY,
				ATreeGenerator.Type.HIGH_NODE_DEPTH)) {
			for (SpecialType stype : SpecialType.allowedModifiers(gtype)) {
				for (Variation variation : Variation.values()) {
					System.out.format("Test START %d %s {%s, %s, %s, %s}%n",size,unit,process,gtype,stype,variation);
					long start = System.currentTimeMillis();
					AGenerator generator = AXMLGenerator.instance(gtype, stype);
					InputStream is = generator.getInputStream(size, unit, variation);
					long event = 0;
					long totalsize = 0;
					switch(process) {
					case READ_BYTE:
						int c;
						while((c = is.read()) != -1) {
							// do nothing
							totalsize++;
							// System.out.println(c);
						}
						break;
					case READ_BUFFER:
						byte[] buffer = new byte[1024*1024];
						int length;
						while ((length = is.read(buffer)) > 0) {
							// do nothing
							totalsize+=length;
						}
						break;
					case PARSE:
						QuiXEventStreamReader xqesr = new QuiXEventStreamReader(new StreamSource(is));
						ValidQuiXTokenStream vqxs = new ValidQuiXTokenStream(xqesr);
						while (vqxs.hasNext()) {
							vqxs.next();
							event++;
						}
						totalsize = size*unit.value();
						break;					
					}
					long time = System.currentTimeMillis() - start;
					long speed = 1000*totalsize / time;
					System.out.format("Test END %,dms; %,dB/s", time, speed);
					if (event > 0) {
						long evpers = 1000*event / time;
						long density = totalsize / event;
						System.out.format("; %,dev; %,dev/s; %,dB/ev", event, evpers, density);
					}
					System.out.println();
				}
			}
		}

	}
//	@Test
//	public void testAllXML1M() throws QuiXException {
//		testAll(1, Unit.MBYTE);
//		assertTrue(true);
//	}
//	@Test
//	public void testAllXML10M() throws QuiXException {
//		testAll(10, Unit.MBYTE);
//		assertTrue(true);
//	}

	@Test
	public void testAllXML50M() throws QuiXException, IOException {
		for(Process process : Process.values()) {
			testAll(Process.PARSE, 50, Unit.MBYTE);
		}
		assertTrue(true);
	}

//	@Test
//	public void testAllXML1G() throws QuiXException {
//      		testAll(1, Unit.GBYTE);
//		assertTrue(true);
//	}

	public static void main(String[] args) throws QuiXException, IOException {
			for(Process process : Process.values()) {
			testAll(process, 10, Unit.MBYTE);
			}
	}
}
