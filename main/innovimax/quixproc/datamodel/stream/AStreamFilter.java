/*
QuiXProc: efficient evaluation of XProc Pipelines.
Copyright (C) 2011 Innovimax
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
package innovimax.quixproc.datamodel.stream;

import innovimax.quixproc.datamodel.IStream;
import innovimax.quixproc.datamodel.QuixException;

public abstract class AStreamFilter<IEvent> implements IStream<IEvent> {
  private IStream<IEvent> stream;
  public AStreamFilter(IStream<IEvent> stream) {
    this.stream = stream;
  }

  @Override
  public boolean hasNext() throws QuixException {
    return this.stream.hasNext();
  }

  @Override
  public IEvent next() throws QuixException {
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
