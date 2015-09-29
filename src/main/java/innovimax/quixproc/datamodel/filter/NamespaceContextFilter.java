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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import innovimax.quixproc.datamodel.IQuiXStream;
import innovimax.quixproc.datamodel.QuiXCharStream;
import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.event.IQuiXEvent;

public class NamespaceContextFilter<T extends IQuiXEvent> extends AQuiXEventStreamFilter<T> {

	private LinkedList<Map<QuiXCharStream, QuiXCharStream>> namespaces;

	public NamespaceContextFilter(IQuiXStream<T> stream) {
		super(stream);
		// TODO Auto-generated constructor stub
		this.namespaces = new LinkedList<Map<QuiXCharStream, QuiXCharStream>>();
	}

	private boolean needCleaning = false;

	@Override
	public T process(T item) {
		AQuiXEvent qevent = item.getEvent();
		if (needCleaning) {
			this.namespaces.pollLast();
			needCleaning = false;
		}
		switch (qevent.getType()) {
		case START_ELEMENT:
			this.namespaces.add(new TreeMap<QuiXCharStream, QuiXCharStream>());
			break;
		case END_ELEMENT:
			// differ the cleaning to the next event
			needCleaning = true;
			break;
		case NAMESPACE:
			this.namespaces.getLast().put(qevent.asNamespace().getPrefix(), qevent.asNamespace().getURI());
			break;
		}
		return item;
	}

	/**
	 * Check at the current moment if the prefix is mapped It returns null if
	 * the prefix is not mapped at this time
	 * 
	 * @param prefix
	 * @return
	 */
	public QuiXCharStream getURI(String prefix) {
		for (Iterator<Map<QuiXCharStream, QuiXCharStream>> iter = this.namespaces.descendingIterator(); iter
				.hasNext();) {
			Map<QuiXCharStream, QuiXCharStream> map = iter.next();
			if (map.containsKey(prefix))
				return map.get(prefix);
		}
		return null;
	}
}
