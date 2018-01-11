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
package innovimax.quixproc.datamodel.filter;

import java.util.EnumSet;

import innovimax.quixproc.datamodel.IQuiXStream;
import innovimax.quixproc.datamodel.IQuiXToken;

public class NodeKindFilter extends AQuiXEventStreamFilter {
	enum Kind {
		ATTRIBUTE, TEXT, COMMENT, PI, NAMESPACE
	}

	private final EnumSet<Kind> enumset;

	public NodeKindFilter(IQuiXStream<IQuiXToken> stream, EnumSet<Kind> enumset) {
		super(stream);
		this.enumset = enumset;
	}

	@Override
	public IQuiXToken process(IQuiXToken item) {
		// We cannot extends the list of Kind in order to be able to assert that
		// this process terminate
		switch (item.getType()) {
		case ATTRIBUTE:
			if (this.enumset.contains(Kind.ATTRIBUTE))
				return null;
			break;
		case TEXT:
			if (this.enumset.contains(Kind.TEXT))
				return null;
			break;
		case COMMENT:
			if (this.enumset.contains(Kind.COMMENT))
				return null;
			break;
		case NAMESPACE:
			if (this.enumset.contains(Kind.NAMESPACE))
				return null;
			break;
		case PROCESSING_INSTRUCTION:
			if (this.enumset.contains(Kind.PI))
				return null;
			break;
		default:
			break;
		}
		return item;
	}

}
