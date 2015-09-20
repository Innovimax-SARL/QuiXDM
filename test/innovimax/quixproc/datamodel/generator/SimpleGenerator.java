package innovimax.quixproc.datamodel.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SimpleGenerator {
	private static class SimpleStableInputStream extends InputStream {

		@Override
		public int read() throws IOException {
			return 0;
		}
		
	}

	private static class SimpleVariableInputStream extends InputStream {

		int i = 10;
		@Override
		public int read() throws IOException {
			i = 30 - i;
			return i;
		}
		
	}
	
	private static class SimpleBufferInputStream extends InputStream {
		final byte[] buffer = "1234567890".getBytes(); 
		int i = 0;
		@Override
		public int read() throws IOException {
			i =  (i + 1) % buffer.length;
			return buffer[i];
		}
		
	}
	private static getInputStream() {
		switch ()
	}
	enum 
	public static void main(String[] args) throws IOException {
		{
			//InputStream is = new FileInputStream(new File("/Users/innovimax/tmp/quixdm/high_density-1GB.xml"));
			InputStream is = new SimpleBufferInputStream();
			long start = System.currentTimeMillis();
			long i = 0;
			int c;
			while ((c = is.read()) != -1) {
				i++;
				if (i % 100000 == 0) {
					long now = System.currentTimeMillis();
					System.out.println(""+i*1000 / (now - start));
				}
			}
			
		}
	}

}
