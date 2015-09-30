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

	public static void testAll(int size, Unit unit) throws QuiXException {
		for (ATreeGenerator.Type gtype : EnumSet.of(ATreeGenerator.Type.HIGH_NODE_NAME_SIZE,
				ATreeGenerator.Type.HIGH_NODE_NAME_SIZE, ATreeGenerator.Type.HIGH_NODE_DENSITY,
				ATreeGenerator.Type.HIGH_NODE_DEPTH)) {
			for (SpecialType stype : SpecialType.allowedModifiers(gtype)) {
				for (Variation variation : Variation.values()) {
					System.out.println("Test START "+size+" "+unit+": "+gtype+", "+stype+", "+variation);
					long start = System.currentTimeMillis();
					AGenerator generator = AXMLGenerator.instance(gtype, stype);
					
					InputStream is = generator.getInputStream(size, unit, variation);
					QuiXEventStreamReader xqesr = new QuiXEventStreamReader(new StreamSource(is));
					ValidQuiXTokenStream vqxs = new ValidQuiXTokenStream(xqesr);
					long event = 0;
					while (vqxs.hasNext()) {
						vqxs.next();
					}
					long time = System.currentTimeMillis() - start;
					long speed = 1000*size*unit.value() / time;
					long evpers = 1000*event / time;
					System.out.println("Test END "+time+"ms; "+speed+"B/s; "+event+"ev; "+evpers+"ev/s");
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
	public void testAllXML50M() throws QuiXException {
	testAll(50, Unit.MBYTE);
		assertTrue(true);
	}

//	@Test
//	public void testAllXML1G() throws QuiXException {
//      		testAll(1, Unit.GBYTE);
//		assertTrue(true);
//	}

	public static void main(String[] args) throws QuiXException, IOException {
		for (ATreeGenerator.Type gtype : EnumSet.of(ATreeGenerator.Type.HIGH_NODE_NAME_SIZE,
				ATreeGenerator.Type.HIGH_NODE_NAME_SIZE, ATreeGenerator.Type.HIGH_NODE_DENSITY,
				ATreeGenerator.Type.HIGH_NODE_DEPTH)) {
			for (SpecialType stype : SpecialType.allowedModifiers(gtype)) {
				//System.out.println(gtype+", "+stype);			
				for (Variation variation : Variation.values()) {
					AGenerator generator = AXMLGenerator.instance(gtype, stype);
					System.out.println(gtype+", "+stype+", "+variation);
					InputStream is = generator.getInputStream(10, Unit.MBYTE, variation);
//					if (false) {
//						int c;
//						while ((c = is.read()) != -1) {
//							 System.out.println(AGenerator.display((byte) (c & 0xFF)));
//						}
//                
//					} else {
					QuiXEventStreamReader xqesr = new QuiXEventStreamReader(new StreamSource(is));
					ValidQuiXTokenStream vqxs = new ValidQuiXTokenStream(xqesr);
					while (vqxs.hasNext()) {
						vqxs.next();
					}
//					}
				}
			}
		}

	}
}
