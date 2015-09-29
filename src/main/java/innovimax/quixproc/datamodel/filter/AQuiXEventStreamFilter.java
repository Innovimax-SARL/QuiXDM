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
package innovimax.quixproc.datamodel.filter;

import innovimax.quixproc.datamodel.IQuiXStream;
import innovimax.quixproc.datamodel.QuiXException;
import innovimax.quixproc.datamodel.event.IQuiXEventStreamReader;

public abstract class AQuiXEventStreamFilter<IEvent> implements IQuiXStream<IEvent> {
	private IQuiXStream<IEvent> stream;

	public AQuiXEventStreamFilter(IQuiXStream<IEvent> stream) {
		this.stream = stream;
	}

	public AQuiXEventStreamFilter(IQuiXEventStreamReader stream) {
		this.stream = (IQuiXStream<IEvent>) stream;
	}

	@Override
	public boolean hasNext() throws QuiXException {
		return this.stream.hasNext();
	}

	@Override
	public IEvent next() throws QuiXException {
		IEvent item;
		while ((item = process(this.stream.next())) == null)
			/* NOP */;
		return item;
	}

	@Override
	public void close() {
		this.stream.close();
	}

	public abstract IEvent process(IEvent item);

}
