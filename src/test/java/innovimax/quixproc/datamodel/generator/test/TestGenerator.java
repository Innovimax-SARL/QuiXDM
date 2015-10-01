package innovimax.quixproc.datamodel.generator.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

import org.junit.Test;

import innovimax.quixproc.datamodel.QuiXException;
import innovimax.quixproc.datamodel.ValidQuiXTokenStream;
import innovimax.quixproc.datamodel.generator.AGenerator;
import innovimax.quixproc.datamodel.generator.AGenerator.FileExtension;
import innovimax.quixproc.datamodel.generator.AGenerator.Unit;
import innovimax.quixproc.datamodel.generator.AGenerator.Variation;
import innovimax.quixproc.datamodel.generator.ATreeGenerator;
import innovimax.quixproc.datamodel.in.AStreamSource;
import innovimax.quixproc.datamodel.in.QuiXEventStreamReader;

public class TestGenerator {
	enum Process {
		READ_BYTE, READ_BUFFER, PARSE
	}

	public static void testAll(FileExtension ext, Process process, int size, Unit unit) throws QuiXException, IOException {
		for (ATreeGenerator.Type gtype : ATreeGenerator.Type.values()) {
			for (ATreeGenerator.SpecialType stype : ATreeGenerator.SpecialType.allowedModifiers(ext, gtype)) {
				for (Variation variation : Variation.values()) {
					System.out.format("Test %s START %d %s {%s, %s, %s, %s}%n", ext, size, unit, process, gtype, stype,
							variation);
					long start = System.currentTimeMillis();
					AGenerator generator = ATreeGenerator.instance(ext, gtype, stype);
					InputStream is = generator.getInputStream(size, unit, variation);
					long event = 0;
					long totalsize = 0;
					switch (process) {
					case READ_BYTE:
						int c;
						while ((c = is.read()) != -1) {
							// do nothing
							totalsize++;
						    // System.out.println(AGenerator.display(c));
						}
						break;
					case READ_BUFFER:
						byte[] buffer = new byte[1024 * 1024];
						int length;
						while ((length = is.read(buffer)) > 0) {
							// do nothing
							totalsize += length;
						}
						break;
					case PARSE:
						QuiXEventStreamReader xqesr = new QuiXEventStreamReader(AStreamSource.instance(ext, is));
						ValidQuiXTokenStream vqxs = new ValidQuiXTokenStream(xqesr);
						while (vqxs.hasNext()) {
							//System.out.println(
									vqxs.next()
							//		)
							;
							
							event++;
						}
						totalsize = size * unit.value();
						break;
					}
					long time = System.currentTimeMillis() - start;
					if (time == 0) time++;
					long speed = 1000 * totalsize / time;
					System.out.format("Test %s END %,dms; %,dB/s; %,dB", ext, time, speed, totalsize);
					if (event > 0) {
						long evpers = 1000 * event / time;
						long density = 1000 * totalsize / event;
						System.out.format("; %,dev; %,dev/s; %,dB/kev", event, evpers, density);
					}
					System.out.println();
				}
			}
		}

	}


	@Test
	public void testAllXML50M() throws QuiXException, IOException {
		for (Process process : Process.values()) {
			testAll(FileExtension.XML, process, 50, Unit.MBYTE);
		}
		assertTrue(true);
	}

	@Test
	public void testAllXML1GNotParse() throws QuiXException, IOException {
		for (Process process : EnumSet.of(Process.READ_BUFFER, Process.READ_BYTE)) {
			testAll(FileExtension.XML, process, 1, Unit.GBYTE);
		}
		assertTrue(true);
	}

	 @Test
	 public void testAllJSON10M() throws QuiXException, IOException {
			for (Process process : Process.values()) {
				testAll(FileExtension.JSON, process, 10, Unit.MBYTE);
			}
	 assertTrue(true);
	 }

	public static void main(String[] args) throws QuiXException, IOException {
		for (Process process : EnumSet.of(/*Process.READ_BUFFER,*/ Process.READ_BYTE, Process.PARSE)) {
			//testAll(FileExtension.XML, process, 5, Unit.BYTE);
			//testAll(FileExtension.JSON, process, 10, Unit.MBYTE);
			testAll(FileExtension.XML, process, 100, Unit.KBYTE);
		}
	}
}
