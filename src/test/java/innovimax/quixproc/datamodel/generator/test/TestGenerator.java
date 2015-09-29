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

	@Test
	public void testAllXML1M() throws QuiXException {
		for (ATreeGenerator.Type gtype : EnumSet.of(ATreeGenerator.Type.HIGH_NODE_NAME_SIZE,
				ATreeGenerator.Type.HIGH_NODE_NAME_SIZE, ATreeGenerator.Type.HIGH_NODE_DENSITY,
				ATreeGenerator.Type.HIGH_NODE_DEPTH)) {
			for (SpecialType stype : SpecialType.allowedModifiers(gtype)) {
				for (Variation variation : Variation.values()) {
					AGenerator generator = AXMLGenerator.instance(gtype, stype);
					InputStream is = generator.getInputStream(1, Unit.MBYTE, variation);
					QuiXEventStreamReader xqesr = new QuiXEventStreamReader(new StreamSource(is));
					ValidQuiXTokenStream vqxs = new ValidQuiXTokenStream(xqesr);
					while (vqxs.hasNext()) {
						vqxs.next();
					}
				}
			}
		}
		assertTrue(true);
	}
	@Test
	public void testAllXML10M() throws QuiXException {
		for (ATreeGenerator.Type gtype : EnumSet.of(ATreeGenerator.Type.HIGH_NODE_NAME_SIZE,
				ATreeGenerator.Type.HIGH_NODE_NAME_SIZE, ATreeGenerator.Type.HIGH_NODE_DENSITY,
				ATreeGenerator.Type.HIGH_NODE_DEPTH)) {
			for (SpecialType stype : SpecialType.allowedModifiers(gtype)) {
				for (Variation variation : Variation.values()) {
					AGenerator generator = AXMLGenerator.instance(gtype, stype);
					InputStream is = generator.getInputStream(10, Unit.MBYTE, variation);
					QuiXEventStreamReader xqesr = new QuiXEventStreamReader(new StreamSource(is));
					ValidQuiXTokenStream vqxs = new ValidQuiXTokenStream(xqesr);
					while (vqxs.hasNext()) {
						vqxs.next();
					}
				}
			}
		}
		assertTrue(true);
	}

	@Test
	public void testAllXML100M() throws QuiXException {
		for (ATreeGenerator.Type gtype : EnumSet.of(ATreeGenerator.Type.HIGH_NODE_NAME_SIZE,
				ATreeGenerator.Type.HIGH_NODE_NAME_SIZE, ATreeGenerator.Type.HIGH_NODE_DENSITY,
				ATreeGenerator.Type.HIGH_NODE_DEPTH)) {
			for (SpecialType stype : SpecialType.allowedModifiers(gtype)) {
				for (Variation variation : Variation.values()) {
					AGenerator generator = AXMLGenerator.instance(gtype, stype);
					InputStream is = generator.getInputStream(100, Unit.MBYTE, variation);
					QuiXEventStreamReader xqesr = new QuiXEventStreamReader(new StreamSource(is));
					ValidQuiXTokenStream vqxs = new ValidQuiXTokenStream(xqesr);
					while (vqxs.hasNext()) {
						vqxs.next();
					}
				}
			}
		}
		assertTrue(true);
	}

	@Test
	public void testAllXML1G() throws QuiXException {
		for (ATreeGenerator.Type gtype : EnumSet.of(ATreeGenerator.Type.HIGH_NODE_NAME_SIZE,
				ATreeGenerator.Type.HIGH_NODE_NAME_SIZE, ATreeGenerator.Type.HIGH_NODE_DENSITY,
				ATreeGenerator.Type.HIGH_NODE_DEPTH)) {
			for (SpecialType stype : SpecialType.allowedModifiers(gtype)) {
				for (Variation variation : Variation.values()) {
					AGenerator generator = AXMLGenerator.instance(gtype, stype);
					InputStream is = generator.getInputStream(1, Unit.GBYTE, variation);
					QuiXEventStreamReader xqesr = new QuiXEventStreamReader(new StreamSource(is));
					ValidQuiXTokenStream vqxs = new ValidQuiXTokenStream(xqesr);
					while (vqxs.hasNext()) {
						vqxs.next();
					}
				}
			}
		}
		assertTrue(true);
	}

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
