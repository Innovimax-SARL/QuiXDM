/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.convert.saxon;

import innovimax.quixproc.datamodel.QuiXCharStream;
import innovimax.quixproc.datamodel.event.AQuiXEvent;
import innovimax.quixproc.datamodel.shared.ISimpleQuiXQueue;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;

public abstract class AXdmNode2QuiXEventStreamConverter implements Runnable {
	private final ISimpleQuiXQueue<AQuiXEvent> doc;
	private final XdmNode node;
	private boolean running = true;
	private static int counter = 1;
//	private final int rank = counter++;

	protected AXdmNode2QuiXEventStreamConverter(final ISimpleQuiXQueue<AQuiXEvent> doc, final XdmNode node) {
		this.doc = doc;
		this.node = node;
	}

	@Override
	public void run() {
		// System.out.println("EventConverter.run("+rank+")");
		try {
			startProcess();
			process();
			this.doc.close();
			endProcess();
			this.running = false;
		} catch (final RuntimeException e) {
			throw e;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isRunning() {
		return this.running;
	}

	/**
	 * parse handler interface
	 */

	private void process() {
		this.doc.append(AQuiXEvent.getStartSequence());
		final String uri = String.valueOf(this.node.getDocumentURI());
		// System.out.println("---------->Document URI"+uri);
		this.doc.append(AQuiXEvent.getStartDocument(QuiXCharStream.fromSequence(uri)));
		processnode(this.node);
		this.doc.append(AQuiXEvent.getEndDocument(QuiXCharStream.fromSequence(uri)));
		this.doc.append(AQuiXEvent.getEndSequence());
	}

	private void processnode(final XdmNode localnode) {
		switch (localnode.getNodeKind()) {
		case DOCUMENT:
			// do nothing
			for (final XdmSequenceIterator iter = localnode.axisIterator(Axis.CHILD); iter.hasNext();) {
				final XdmNode item = (XdmNode) iter.next();
				processnode(item);
			}
			break;
		case ELEMENT:
			this.doc.append(
					AQuiXEvent.getStartElement(QuiXCharStream.fromSequence(localnode.getNodeName().getLocalName()),
							QuiXCharStream.fromSequence(localnode.getNodeName().getNamespaceURI()),
							QuiXCharStream.fromSequence(localnode.getNodeName().getPrefix())));
			namespaceProcess(localnode);
			for (final XdmSequenceIterator iter = localnode.axisIterator(Axis.ATTRIBUTE); iter.hasNext();) {
				final XdmNode item = (XdmNode) iter.next();
				processnode(item);
			}
			for (final XdmSequenceIterator iter = localnode.axisIterator(Axis.CHILD); iter.hasNext();) {
				final XdmNode item = (XdmNode) iter.next();
				processnode(item);
			}
			this.doc.append(
					AQuiXEvent.getEndElement(QuiXCharStream.fromSequence(localnode.getNodeName().getLocalName()),
							QuiXCharStream.fromSequence(localnode.getNodeName().getNamespaceURI()),
							QuiXCharStream.fromSequence(localnode.getNodeName().getPrefix())));
			break;
		case ATTRIBUTE:
			this.doc.append(AQuiXEvent.getAttribute(QuiXCharStream.fromSequence(localnode.getNodeName().getLocalName()),
					QuiXCharStream.fromSequence(localnode.getNodeName().getNamespaceURI()),
					QuiXCharStream.fromSequence(localnode.getNodeName().getPrefix()),
					QuiXCharStream.fromSequence(localnode.getStringValue())));
			break;
		case TEXT:
			this.doc.append(AQuiXEvent.getText(QuiXCharStream.fromSequence(localnode.getStringValue())));
			break;
		case COMMENT:
			this.doc.append(AQuiXEvent.getComment(QuiXCharStream.fromSequence(localnode.getStringValue())));
			break;
		case PROCESSING_INSTRUCTION:
			this.doc.append(AQuiXEvent.getPI(QuiXCharStream.fromSequence(localnode.getNodeName().getLocalName()),
					QuiXCharStream.fromSequence(localnode.getStringValue())));
			break;
		case NAMESPACE:
			// no op
			break;
		default:
		}
	}

	private void namespaceProcess(final XdmNode node) {
		final NodeInfo inode = node.getUnderlyingNode();
		final NamespaceBinding[] inscopeNS = inode.getDeclaredNamespaces(null);
		// NamespaceIterator.getInScopeNamespaceCodes(inode);

		if (inscopeNS.length > 0) {
			for (final NamespaceBinding ns : inscopeNS) {
				final String pfx = ns.getPrefix();
				final String uri = ns.getURI();
				this.doc.append(
						AQuiXEvent.getNamespace(QuiXCharStream.fromSequence(pfx), QuiXCharStream.fromSequence(uri)));
			}
		}

		// Careful, we're messing with the namespace bindings
		// Make sure the nameCode is right...
		// int nameCode = inode.getNameCode();
		// int typeCode = inode.getTypeAnnotation() & NamePool.FP_MASK;
		// String pfx = pool.getPrefix(nameCode);
		// String uri = pool.getURI(nameCode);

		// if (excludeDefault && "".equals(pfx) && !usesDefaultNS) {
		// nameCode = pool.allocate("", "", pool.getLocalName(nameCode));
		// }

		// tree.addStartElement(nameCode, typeCode, newNS);
		// tree.addAttributes(node);

	}

	public abstract void startProcess();

	public abstract void endProcess();

}