/*
QuiXProc: efficient evaluation of XProc Pipelines.
Copyright (C) 2011-2018 Innovimax
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
package innovimax.quixproc.datamodel.event;

import innovimax.quixproc.datamodel.IQuiXStream;
import innovimax.quixproc.datamodel.IQuiXToken;
import innovimax.quixproc.datamodel.QuiXException;

public interface IQuiXEventStreamReader extends IQuiXStream<AQuiXEvent> {
	default IQuiXStream<IQuiXToken> asIQuiXTokenStream() {
		final IQuiXEventStreamReader that = this;
		return new IQuiXStream<IQuiXToken>() {

			@Override
			public boolean hasNext() throws QuiXException {
				return that.hasNext();
			}

			@Override
			public IQuiXToken next() throws QuiXException {
				return that.next();
			}

			@Override
			public void close() {
				that.close();
			}

		};
	}
}
