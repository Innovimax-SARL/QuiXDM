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

import innovimax.quixproc.datamodel.QuiXException;
import innovimax.quixproc.datamodel.event.AQuiXEvent;

public abstract class AQuiXEventStreamReader {

	public interface CallBack {
		QuiXEventStreamReader.State getState();

		void setState(QuiXEventStreamReader.State state);

		AQuiXEvent processEndSource();
	}

	protected AQuiXEventStreamReader() {
	}

	protected abstract AQuiXEvent load(AStreamSource current) throws QuiXException;

	protected abstract AQuiXEvent process(CallBack callback) throws QuiXException;

	public abstract void reinitialize(AStreamSource current);

	public abstract void close();

}
