/*
QuiXProc: efficient evaluation of XProc Pipelines.
Copyright (C) 2011-2012 Innovimax
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

import java.util.Iterator;
import java.util.Stack;

import javax.xml.namespace.QName;

import innovimax.quixproc.datamodel.IEvent;
import innovimax.quixproc.datamodel.IStream;
import innovimax.quixproc.datamodel.QuixEvent;

public class AncestorContextFilter<T extends IEvent> extends AStreamFilter<T> {

  private Stack<QName> ancestors;
  public AncestorContextFilter(IStream<T> stream) {
    super(stream);
    this.ancestors = new Stack<QName>();
  }

  @Override
  public T process(T item) {
    QuixEvent qevent = item.getEvent();
    switch (qevent.getType()) {
      case START_ELEMENT :
        this.ancestors.push(qevent.asNamedEvent().getQName());
        break;
      case END_ELEMENT:
        this.ancestors.pop();
        break;
      default:  
        break;        
    }
    return item;
  }

  public Iterator<QName> ancestors() {
    return this.ancestors.iterator();
  }
}
