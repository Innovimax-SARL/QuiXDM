/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.generator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

final class SimpleGenerator {

	private static class SimpleStableInputStream extends InputStream {

		@Override
		public int read() {
			return 0;
		}

	}

	private static class SimpleVariableInputStream extends InputStream {

		int i = 10;

		@Override
		public int read() {
			this.i = 30 - this.i;
			return this.i;
		}

	}

	private static class SimpleBufferInputStream extends InputStream {
		final byte[] buffer = "1234567890".getBytes(StandardCharsets.US_ASCII);
		int i = 0;

		@Override
		public int read() {
			this.i = (this.i + 1) % this.buffer.length;
			return this.buffer[this.i];
		}

	}

	public static void main(final String[] args) throws IOException {
		// InputStream is = new FileInputStream(new
		// File("/Users/innovimax/tmp/quixdm/high_density-1GB.xml"));
		final InputStream is = new SimpleBufferInputStream();
		final long start = System.currentTimeMillis();
		long i = 0;
		int c;
		while ((c = is.read()) != -1) {
			i++;
			if (i % 100000 == 0) {
				final long now = System.currentTimeMillis();
				System.out.println(i * 1000 / (now - start));
			}
		}

	}

}
