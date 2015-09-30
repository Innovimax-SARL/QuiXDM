/*
QuiXProc: efficient evaluation of XProc Pipelines.
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
package innovimax.quixproc.datamodel.in;

import java.util.Arrays;

import javax.xml.transform.Source;

public abstract class AStreamSource {
	enum Type {
		XML, JSON
	}

	protected final Type type;

	protected AStreamSource(Type type) {
		this.type = type;
	}

	public static XMLStreamSource instance(Source source) {
		return new XMLStreamSource(source);
	}

	public static class XMLStreamSource extends AStreamSource {
		public final Source source;

		private XMLStreamSource(Source source) {
			super(Type.XML);
			this.source = source;
		}

		public Source asSource() {
			return this.source;
		}
	}

	public static class JSONStreamSource extends AStreamSource {

		protected JSONStreamSource() {
			super(Type.JSON);
		}

	}

	public static Iterable<AStreamSource> instances(Source[] sources) {
		AStreamSource[] asources = new AStreamSource[sources.length];
		int i = 0;
		for (javax.xml.transform.Source source : sources) {
			asources[i] = AStreamSource.instance(source);
			i++;
		}
		return Arrays.asList(asources);
	}

}
