package innovimax.quixproc.datamodel.generator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public abstract class AGenerator {
	
	enum FileProperty {
		REPEATABLE/* repeated part of the content makes it potentially infinite*/,
		SYMMETRIC/* the file is symmetric to its middle, usually you must provide a length*/,
		CONTENT_VARIATION/* content is varying from one occurrence to the other*/,
		XML
	}

	public enum GeneratorType {
		HIGH_DENSITY, HIGH_DEPTH, HIGH_DEPTH_NAMESPACE, 
		HIGH_ELEMENT_NAME_SIZE_SINGLE,
		HIGH_ELEMENT_NAME_SIZE_OPEN_CLOSE, HIGH_TEXT_SIZE, HIGH_NAMESPACE_COUNT
	}

	private final GeneratorType type;

	public enum Unit {
		BYTE(1, "B"), KBYTE(1000, "KB"), MBYTE(1000000, "MB"), GBYTE(1000000000, "GB"), TBYTE(1000000000000l, "TB");
		private long value;
		private String display;

		Unit(long value, String display) {
			this.value = value;
			this.display = display;
		}

		public long value() {
			return this.value;
		}

		public String display() {
			return this.display;
		}
	}

	protected AGenerator(GeneratorType type) {
		this.type = type;
	}

	private static AGenerator instance(GeneratorType type) {
		switch (type) {
		case HIGH_DENSITY:
			return new HighDensityGenerator();
		case HIGH_DEPTH:
			return new AHighDepthGenerator.HighDepthGenerator();
		case HIGH_DEPTH_NAMESPACE:
			return new AHighDepthGenerator.HighDepthNamespaceGenerator();
		case HIGH_ELEMENT_NAME_SIZE_SINGLE:
			return new AHighElementNameSize.HighElementNameSizeSingle();
		case HIGH_ELEMENT_NAME_SIZE_OPEN_CLOSE:
			return new AHighElementNameSize.HighElementNameSizeOpenClose();
		case HIGH_TEXT_SIZE:
		case HIGH_NAMESPACE_COUNT:
		}
		return null;
	}

	final static byte[] nextChar = initNextChar(false);
	final static byte[] nextAttributeValue = initNextChar(true);
	final static byte[] nextStartName = initNextName(true);
	final static byte[] nextName = initNextName(false);
	final static byte[] prevStartName = initPrevStartName();
	private static int nextAllowedChar(int b, boolean attributeValue) {
		if (b <= 0x20) {
			if (b <= 0xD) {
			if (b <= 0xa) {
				if (b <= 0x9) {
					return (byte)0x9;
				} 
				 return (byte)0xA;
			}
			return (byte)0xD;				
		    }
			return (byte) 0x20;
		}
		// MUST
		if (b == '<') return b+1;
		if (b == '&') return b+1;
		// MAY
		if (b == '>') return b+1;
		// attribute use quot
		if (attributeValue && b == '"') return b+1;
		return b;
	}
	private static int nextAllowedName(int b, boolean startName) {
		// NameStartChar ::= "[A-Z] | "_" | [a-z] | [#xC0-#xD6] | [#xD8-#xF6] | [#xF8
		// NameChar	   ::=   	NameStartChar | "-" | "." | [0-9] | #xB7 | [#x0300-#x036F] | [#x203F-#x2040]
		
		if (b <= 'A') {
		   if (!startName) {
			   if (b <= '-')
				   return '-';
			   if (b <= '.')
				   return '.';
			   if (b <= '0')
			   	   return '0';
			   if (b <= '9')
				   return b;
			   // FALL_TROUGH_ : return 'A'
		   }			
			return  'A';
		}
		if (b <= '_') {
			if (b <= 'Z') return b;
			return  '_';
		}
		if (b <= 'a') {
			return  'a';
		}
		if (b < 0xC0) {
			if (b <= 'z') return b;
			if (!startName) {
				return '-';
			}
			return 'A';// 0xC0;
		}
		if (b < 0xD8) {
			if (b <= 0xD6) return b;
			return  0xD8;
		}
		if (b < 0xF8) {
			if (b <= 0xF6) return b;
			return  0xF8;
		}
		// b >= 0xF8
		return b;
	}
	
	private static byte[] initNextChar(boolean attributeValue) {
		byte[] results = new byte[128];
		for(int i = 0; i < results.length; i++) {
			results[i]  = (byte) nextAllowedChar(((i & 0x7F)+1) &0x7F, attributeValue);				
		}
		return results;
	}
	
	private static byte[] initNextName(boolean startName) {
		byte[] results = new byte[128];
		for(int i = 0; i < results.length; i++) {
			results[i]  = (byte) nextAllowedName(((i & 0x7F) +1) & 0x7F, startName);				
		}
		return results;
	}
	private static byte[] initPrevStartName() {
		byte[] results = new byte[128];
		for(int i = results.length; i > 0; i--) {
			int r = nextAllowedName(((i & 0x7F) +1) & 0x7F, true);				
			results[r]  = (byte) i;	
		}
		results[0x41] = (byte) 0x7a;
		return results;
	}
	private static byte nextChar(byte b) {
//		System.out.println("nextChar : "+Integer.toHexString(b & 0xFF)+"("+Character.toString((char) (b& 0xFF))+")" );	
		byte r = nextChar[b & 0xFF];
//		System.out.println("nextChar : "+Integer.toHexString(r & 0xFF)+"("+Character.toString((char) (r& 0xFF))+")" );
		return r;
	}
	
	private static byte nextAttributeValue(byte b) {
//		System.out.println("nextChar : "+Integer.toHexString(b & 0xFF)+"("+Character.toString((char) (b& 0xFF))+")" );	
		byte r = nextAttributeValue[b & 0xFF];
//		System.out.println("nextChar : "+Integer.toHexString(r & 0xFF)+"("+Character.toString((char) (r& 0xFF))+")" );
		return r;
	}
	private static byte nextStartName(byte b) {
//		System.out.println("nextStartName : "+Integer.toHexString(b & 0xFF)+"("+Character.toString((char) (b& 0xFF))+")" );
		byte r= nextStartName[b & 0xFF];
//		System.out.println("nextStartName : "+Integer.toHexString(r & 0xFF)+"("+Character.toString((char) (r& 0xFF))+")" );
		return r;
	}

	private static byte nextName(byte b) {
//		System.out.println("nextName : "+Integer.toHexString(b & 0xFF)+"("+Character.toString((char) (b& 0xFF))+")" );
		byte r= nextName[b & 0xFF];
//		System.out.println("nextName : "+Integer.toHexString(r & 0xFF)+"("+Character.toString((char) (r& 0xFF))+")" );
		return r;
	}
	
	private static byte prevStartName(byte b) {
//		System.out.println("prevStartName : "+Integer.toHexString(b & 0xFF)+"("+Character.toString((char) (b& 0xFF))+")" );
		byte r= prevStartName[b & 0xFF];
//		System.out.println("prevStartName : "+Integer.toHexString(r & 0xFF)+"("+Character.toString((char) (r& 0xFF))+")" );
		return r;
	}
	public void generate(File output, long size, Unit unit) throws IOException {
		output.getParentFile().mkdirs();
		final long total = size * unit.value();
		FileOutputStream fos = new FileOutputStream(output);
		BufferedOutputStream bos = new BufferedOutputStream(fos, 1000 * 1000);
		final byte[] start = getStart();
		final byte[][] patterns = getPatterns();
		final byte[] end = getEnd();
		// ensure that at minimum the size is start+end
		long current_size = start.length + end.length;
		int current_pattern = -1;
		// write the start pattern
		bos.write(start);
		//System.out.println(current_size);
		while (notFinished(current_size, current_pattern, total)) {
			// move to next pattern
			current_pattern = updatePattern(current_pattern);
		    // System.out.println(current_size);
			// write the alternate pattern
			bos.write(applyRandom(patterns, current_pattern));
			// update the size
			current_size = updateSize(current_size, current_pattern);
		}
		// write the end pattern
		bos.write(end);
		bos.flush();
		bos.close();
		fos.close();
	}
	enum State { START, CURRENT, END };
	public InputStream getInputStream(long size, Unit unit) {
		return new GeneratorInputStream(size, unit);
	}
	private class GeneratorInputStream extends InputStream {

		final byte[] start = getStart();
		final byte[][] patterns = getPatterns();
		final byte[] end = getEnd();
		// ensure that at minimum the size is start+end
		long current_size = start.length + end.length;
		int current_pattern = -1;
		int offset = -1;
		byte[] buffer = null;
		State state;
		final long total;
		private GeneratorInputStream(long size, Unit unit) {
			state = State.START;
			buffer = start;
			total = size * unit.value();
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			//System.out.println("off : "+off+" ; len : "+len+" : "+display(b));
			if (b == null) {
	            throw new NullPointerException();
	        } 
			if (off < 0 || len < 0 || len > b.length - off) {
	            throw new IndexOutOfBoundsException();
	        }
			if (len == 0) {
	            return 0;
	        }
			if (buffer == null) return -1;
			int total = 0;
			if (offset +1 == buffer.length) {
				//System.out.println("offset : "+offset);
				//System.out.println("length : "+len);
				update();
				//System.out.println("offset : "+offset);
				//System.out.println("length : "+len);
				if (buffer == null) return -1;
			}
			do {
				//System.out.println("offset : "+offset);
				//System.out.println("length : "+len);
				int length = Math.min(buffer.length - (offset+1), len - total); 
				//System.out.println("length : "+length);
				System.arraycopy(buffer, offset+1, b, off+total, length);
				total += length;
				//System.out.println("total : "+total);
				offset = offset+1+length-1;
				//System.out.println("offset : "+offset);
				if (offset == buffer.length-1) update();
				if (total == len) {
					//System.out.println("length : "+len+" : "+display(b));
					return len;

				}
				if (buffer == null) {
				//	System.out.println("length : "+total+" : "+display(b));
					return total;
				}
			} while(true);			
		}

		private void update() {
			// offset == buffer.length
			switch (state) {
			case START :
 			if (notFinished(current_size, current_pattern, total)) {
				// move to next pattern
				current_pattern = updatePattern(current_pattern);
			    // System.out.println(current_size);
				// write the alternate pattern
				buffer = applyRandom(patterns, current_pattern);
				// update the size
				current_size = updateSize(current_size, current_pattern);
				offset = -1;
				return;
			}
			state = State.CURRENT;
			// FALL-THROUGH
   		    case CURRENT :	
				buffer = end;
				offset = -1;
	 			state = State.END;
	 			return;
			case END:
				buffer = null;
			}
		}
		@Override
		public int read() throws IOException {
			if (buffer == null) return -1;
			offset++;
			if (offset<buffer.length) {
				int c = buffer[offset];
				//System.out.println("read : "+display((byte) (c & 0xFF)));
				return c;
			}
			update();
			if (buffer == null) return -1;
			offset++;
			int c = buffer[offset];
			//System.out.println("read : "+display((byte) (c & 0xFF)));
			return c;
		}
		
	}

	protected abstract byte[] applyRandom(byte[][] bs, int pos);

	protected abstract boolean notFinished(long current_size, int current_pattern, long total);

	protected abstract int updatePattern(int current_pattern);

	protected abstract long updateSize(long current_size, int current_pattern);

	protected abstract byte[] getEnd();

	protected abstract byte[][] getPatterns();

	protected abstract byte[] getStart();

	private abstract static class ASimpleRootGenerator extends AGenerator {
		final byte[] start = "<r>".getBytes();
		final byte[] end = "</r>".getBytes();

		protected ASimpleRootGenerator(GeneratorType type) {
			super(type);
		}

		@Override
		protected byte[] getEnd() {
			return end;
		}

		@Override
		protected byte[] getStart() {
			return start;
		}

	}

	private static class HighDensityGenerator extends ASimpleRootGenerator {

		final byte[][] patterns = { "a".getBytes(), "<b/>".getBytes() };

		protected HighDensityGenerator() {
			super(GeneratorType.HIGH_DENSITY);			
		}

		@Override
		protected int updatePattern(int current_pattern) {
			return (current_pattern + 1) % patterns.length;
		}

		@Override
		protected long updateSize(long current_size, int current_pattern) {
			return current_size + patterns[current_pattern].length;
		}

		@Override
		protected boolean notFinished(long current_size, int current_pattern, long total) {
			return current_size < total;
		}
		
		@Override
		protected byte[][] getPatterns() {
			return patterns;
		}

		@Override
		protected byte[] applyRandom(byte[][] bs, int pos) {
			switch (pos) {
			case 0 :
				bs[0][0] = nextChar(bs[0][0]);
				break;				
			case 1 :	
				bs[1][1] = nextStartName(bs[1][1]);
				break;
			}
			return bs[pos];
		}

	}

	private static abstract class AHighDepthGenerator extends ASimpleRootGenerator {

		protected AHighDepthGenerator(GeneratorType gtype) {
			super(gtype);
		}

		private int next_pattern = 0;

		@Override
		protected int updatePattern(int current_pattern) {
			// return internal state
			return this.next_pattern;
		}

		@Override
		protected long updateSize(long current_size, int current_pattern) {
			// update the size by adding open and closing tag
			return current_size + (current_pattern == 0 ? getPatternsLength() : 0);
		}

		private long loop = 0;

		@Override
		protected boolean notFinished(long current_size, int current_pattern, long total) {
			// System.out.println(current_size + ", "+current_pattern+",
			// "+total);
			if (current_size < total) {
				loop++;
				return true;
			}
			// current_size >= total
			if (current_pattern == 0) {
				// switch pattern
				this.next_pattern = 1;
			}
			// next_pattern will be 1
			return this.loop-- > 0;
		}

		protected abstract int getPatternsLength();

		private static class HighDepthGenerator extends AHighDepthGenerator {
			final byte[][] patterns = { "<a>".getBytes(), "</a>".getBytes() };

			private HighDepthGenerator() {
				super(GeneratorType.HIGH_DEPTH);
			}

			protected byte[][] getPatterns() {
				return patterns;
			}

			protected int getPatternsLength() {
				return patterns[0].length + patterns[1].length;
			}

			private boolean isReturn = false;
			@Override
			protected byte[] applyRandom(byte[][] bs, int pos) {
				switch (pos) {
				case 0 :
					bs[0][1] = nextStartName(bs[0][1]);
					isReturn = true;
					break;				
				case 1 :	
					if (isReturn) {
						isReturn = false;
						bs[1][2] = bs[0][1];
					} else
						bs[1][2] = prevStartName(bs[1][2]);
					break;
				}
				return bs[pos];
			}

		}

		private static class HighDepthNamespaceGenerator extends AHighDepthGenerator {
			final byte[][] patterns = { "<a xmlns=\"a\">".getBytes(), "</a>".getBytes() };

			private HighDepthNamespaceGenerator() {
				super(GeneratorType.HIGH_DEPTH_NAMESPACE);
			}

			protected byte[][] getPatterns() {
				return patterns;
			}

			protected int getPatternsLength() {
				return patterns[0].length + patterns[1].length;
			}


			private boolean isReturn = false;
			@Override
			protected byte[] applyRandom(byte[][] bs, int pos) {
				switch (pos) {
				case 0 :
					bs[0][1] = nextStartName(bs[0][1]);
					bs[0][10] = nextAttributeValue(bs[0][10]);
					isReturn = true;
					break;				
				case 1 :	
					if (isReturn) {
						isReturn = false;
						bs[1][2] = bs[0][1];
					} else
						bs[1][2] = prevStartName(bs[1][2]);
					break;
				}
				return bs[pos];
			}

		}
	}
	private abstract static class AHighElementNameSize extends AGenerator {

		protected AHighElementNameSize(GeneratorType type) {
			super(type);
		}

		private static class HighElementNameSizeSingle extends AHighElementNameSize {
			@Override
			protected byte[] getEnd() {
				return "/>".getBytes();
			}

			@Override
			protected byte[][] getPatterns() {
				byte[][] result =  {"a".getBytes()};
				return result;
			}

			@Override
			protected byte[] getStart() {
				return "<_".getBytes();
			}

			@Override
			protected byte[] applyRandom(byte[][] bs, int pos) {
				bs[0][0] = nextName(bs[0][0]);
				return bs[pos];
			}

			@Override
			protected boolean notFinished(long current_size, int current_pattern, long total) {
				return current_size < total;
			}

			@Override
			protected int updatePattern(int current_pattern) {
				return 0;
			}

			@Override
			protected long updateSize(long current_size, int current_pattern) {
				return current_size+1;
			}

			protected HighElementNameSizeSingle() {
				super(GeneratorType.HIGH_ELEMENT_NAME_SIZE_SINGLE);
			}
			
		}
		private static class HighElementNameSizeOpenClose extends AHighElementNameSize {

			@Override
			protected byte[] getEnd() {
				return ">".getBytes();
			}
			private final byte[][] patterns =  {
					"a".getBytes(),
					"></_".getBytes(),
					"a".getBytes()};

			@Override
			protected byte[][] getPatterns() {
				return patterns;
			}

			@Override
			protected byte[] getStart() {
				return "<_".getBytes();
			}
			@Override
			protected byte[] applyRandom(byte[][] bs, int pos) {
				switch(pos) {
				case 0:
					bs[0][0] = nextName(bs[0][0]);					
					break;
				case 1:
					// NOP
					break;
				case 2:
					bs[2][0] = nextName(bs[2][0]);					
					break;
				}
				return bs[pos];
			}

			private long loop = 0;

			@Override
			protected boolean notFinished(long current_size, int current_pattern, long total) {
				//System.out.println(current_size + ", "+current_pattern+", "+total);
				if (current_size + patterns[1].length < total) {
					loop++;
					return true;
				}
				// current_size >= total
				if (current_pattern <= 0) {
					// switch pattern
					this.next_pattern = 1;
					return true;
				}
				if (current_pattern == 1) {
				   // switch pattern
  				   this.next_pattern = 2;
				
				}
				// next_pattern will be 2
				return this.loop-- > 0;
			}

			int next_pattern  = 0;
			@Override
			protected int updatePattern(int current_pattern) {
				return this.next_pattern;
			}

			@Override
			protected long updateSize(long current_size, int current_pattern) {
				return current_size+(current_pattern == 0 ? 2 : 0);
			}

			protected HighElementNameSizeOpenClose() {
				super(GeneratorType.HIGH_ELEMENT_NAME_SIZE_OPEN_CLOSE);
			}
			
		}
	}

	private static void call(GeneratorType gtype, int size, Unit unit) throws IOException, XMLStreamException {
		long start = System.currentTimeMillis();
		AGenerator generator = AGenerator.instance(gtype);
		if (USE_STREAM) {
		   InputStream is = generator.getInputStream(size, unit);
		   switch (PROCESS) {
		   case READ_CHAR:
		   int c;
		   while ((c = is.read()) != -1) {
//			   System.out.println(display((byte) (c & 0xFF)));
		   }
		   break;
		   case READ_BUFFER:
			   byte[] b = new byte[1024];
			   while ((c = is.read(b)) != -1) {
//				   System.out.println(display((byte) (c & 0xFF)));
			   }
			   break;
			   
		   case PARSE:
		   
		   XMLInputFactory xif = XMLInputFactory.newFactory();		   
		   XMLStreamReader xsr = xif.createXMLStreamReader(is);
		   while(xsr.hasNext()) {
			   xsr.next();
		   }
		   break;
		   }
		   
		} else {
		File f = new File(
				"/Users/innovimax/tmp/quixdm/" + gtype.name().toLowerCase() + "-" + size + unit.display() + ".xml");
		generator.generate(f, size, unit);
		System.out.print("File : " + f.getName() + "\tSize : " + f.length()+"\t\t");
		}
		System.out.println("Time : "+ (System.currentTimeMillis() - start));
	}
	private static String display(byte b) {
		return Integer.toHexString(b & 0xFF)+"("+Character.toString((char) (b& 0xFF))+")";
	}
	private static String display(byte[] bytea) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(int i = 0; i < bytea.length; i++) {
			if (i > 0) sb.append(", ");
			sb.append(display(bytea[i]));
		}
		sb.append("]");
		return sb.toString();
	}
	enum Process { READ_CHAR, READ_BUFFER, PARSE }
	final static boolean USE_STREAM = true;
	final static Process PROCESS = Process.READ_BUFFER;
	final static boolean ONE_INSTANCE = true;
	public static void main(String[] args) throws FileNotFoundException, IOException, XMLStreamException {
		System.out.println("nextChar\t: " + display(AGenerator.nextChar));
		System.out.println("nextAttributeValue\t: " +display(AGenerator.nextAttributeValue));		
		System.out.println("nextStartName\t: "+display(AGenerator.nextStartName));
		System.out.println("nextName\t: "+display(AGenerator.nextName));
		System.out.println("prevStartName\t:" +display(AGenerator.prevStartName));
		if (ONE_INSTANCE) {
		call(GeneratorType.HIGH_DENSITY, 150, Unit.MBYTE);
		call(GeneratorType.HIGH_DEPTH_NAMESPACE, 201, Unit.MBYTE);
		call(GeneratorType.HIGH_DEPTH, 112, Unit.MBYTE);
		} else {
		for (GeneratorType gtype : EnumSet.of(GeneratorType.HIGH_ELEMENT_NAME_SIZE_SINGLE,
				GeneratorType.HIGH_ELEMENT_NAME_SIZE_OPEN_CLOSE,
				GeneratorType.HIGH_DENSITY,GeneratorType.HIGH_DEPTH,
				GeneratorType.HIGH_DEPTH_NAMESPACE)) {
			for (Unit unit : EnumSet.of(Unit.BYTE, Unit.KBYTE, Unit.MBYTE, Unit.GBYTE)) {
				int[] values = { 1, 2, 5, 10, 20, 50, 100, 200, 500 };
				for (int i : values) {
					 if (unit == Unit.GBYTE && i > 1) continue;
					call(gtype, i, unit);
				}
			}
		}
		}
		
	}
}
