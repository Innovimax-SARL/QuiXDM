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
package innovimax.quixproc.datamodel;

import innovimax.quixproc.datamodel.shared.ISimpleQueue;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;

public abstract class EventConverter implements Runnable {
    private ISimpleQueue<QuixEvent> doc = null;   
    private XdmNode node = null;      
    private boolean running = true; 
    private static int counter = 1;
    private final int rank = counter++;
    
    public EventConverter(ISimpleQueue<QuixEvent> doc, XdmNode node) {  
        this.doc = doc;              
        this.node = node;
    }
    
    public void run() {                    
      //System.out.println("EventConverter.run("+rank+")");
        try {               
            startProcess();
            process();
            doc.close();
            endProcess();  
            running = false;                                     
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {          
            throw new RuntimeException(e);
        }                     
    }
    
    public boolean isRunning() {
        return running;
    }
    
    /** 	  
     * parse handler interface
     */ 
     

    private void process() {
      doc.append(QuixEvent.getStartSequence());
      String uri = ""+node.getDocumentURI();
      //System.out.println("---------->Document URI"+uri);
      doc.append(QuixEvent.getStartDocument(uri));
      processnode(node);
      doc.append(QuixEvent.getEndDocument(uri));
      doc.append(QuixEvent.getEndSequence());
    }
    
    private void processnode(XdmNode localnode) {
      switch (localnode.getNodeKind()) {
        case DOCUMENT:
          // do nothing
          for(XdmSequenceIterator iter = localnode.axisIterator(Axis.CHILD);iter.hasNext();) {
            XdmNode item = (XdmNode) iter.next();
            processnode(item);
          }
          break;
        case ELEMENT :          
          doc.append(QuixEvent.getStartElement(localnode.getNodeName().getLocalName(), localnode.getNodeName().getNamespaceURI(), localnode.getNodeName().getPrefix()));
          namespaceProcess(localnode);
          for(XdmSequenceIterator iter = localnode.axisIterator(Axis.ATTRIBUTE);iter.hasNext();) {
            XdmNode item = (XdmNode) iter.next();
            processnode(item);
          }
          for(XdmSequenceIterator iter = localnode.axisIterator(Axis.CHILD);iter.hasNext();) {
            XdmNode item = (XdmNode) iter.next();
            processnode(item);
          }
          doc.append(QuixEvent.getEndElement(localnode.getNodeName().getLocalName(), localnode.getNodeName().getNamespaceURI(), localnode.getNodeName().getPrefix()));
          break;
        case ATTRIBUTE :          
          doc.append(QuixEvent.getAttribute(localnode.getNodeName().getLocalName(), localnode.getNodeName().getNamespaceURI(), localnode.getNodeName().getPrefix(), localnode.getStringValue()));
          break;
        case TEXT:
          doc.append(QuixEvent.getText(localnode.getStringValue()));
          break;
        case COMMENT :
          doc.append(QuixEvent.getComment(localnode.getStringValue()));
          break;
        case PROCESSING_INSTRUCTION :
          doc.append(QuixEvent.getPI(localnode.getNodeName().getLocalName(), localnode.getStringValue()));
          break;
        case NAMESPACE :          
          // no op
          break;
      }
    }
    
    private void namespaceProcess(XdmNode node) {
      NodeInfo inode = node.getUnderlyingNode();
      NamespaceBinding[] inscopeNS = 
          inode.getDeclaredNamespaces(null);
          //NamespaceIterator.getInScopeNamespaceCodes(inode);

      if (inscopeNS.length > 0) {
          for (int pos = 0; pos < inscopeNS.length; pos++) {
              NamespaceBinding ns = inscopeNS[pos];
              String pfx = ns.getPrefix();
              String uri = ns.getURI();
              doc.append(QuixEvent.getNamespace(pfx, uri));              
           }
       }

      // Careful, we're messing with the namespace bindings
      // Make sure the nameCode is right...
      //int nameCode = inode.getNameCode();
      //int typeCode = inode.getTypeAnnotation() & NamePool.FP_MASK;
      //String pfx = pool.getPrefix(nameCode);
      //String uri = pool.getURI(nameCode);

      //if (excludeDefault && "".equals(pfx) && !usesDefaultNS) {
          //nameCode = pool.allocate("", "", pool.getLocalName(nameCode));
      //}

      //tree.addStartElement(nameCode, typeCode, newNS);
      //tree.addAttributes(node);


    }
    
    public abstract void startProcess();
    public abstract void endProcess();
               
}