/*
`QuiXProc: efficient evaluation of XProc Pipelines.
Copyright (C) 2011-2015 Innovimax
All rights reserved.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package innovimax.quixproc.datamodel.generator;

import java.io.IOException;
import java.io.InputStream;

public final class SimpleGenerator {

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
		final byte[] buffer = "1234567890".getBytes();
		int i = 0;

		@Override
		public int read() {
			this.i = (this.i + 1) % this.buffer.length;
			return this.buffer[this.i];
		}

	}

	public static void main(String[] args) throws IOException {
		// InputStream is = new FileInputStream(new
		// File("/Users/innovimax/tmp/quixdm/high_density-1GB.xml"));
		InputStream is = new SimpleBufferInputStream();
		long start = System.currentTimeMillis();
		long i = 0;
		int c;
		while ((c = is.read()) != -1) {
            i++;
            if (i % 100000 == 0) {
                long now = System.currentTimeMillis();
                System.out.println("" + i * 1000 / (now - start));
            }
        }

	}

}
