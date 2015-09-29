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
import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.event.IQuiXEvent;

public class DocumentCountFilter<T extends IQuiXEvent> extends AQuiXEventStreamFilter<T> {

	private int count;

	public DocumentCountFilter(IQuiXStream<T> stream) {
		super(stream);
		this.count = 0;
	}

	@Override
	public T process(T item) {
		AQuiXEvent qevent = item.getEvent();
		switch (qevent.getType()) {
		case START_DOCUMENT:
			this.count++;
			break;
		default:
			break;
		}
		return item;
	}

	public int getCurrentDocumentCount() {
		return this.count;
	}
}
