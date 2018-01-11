/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.convert.saxon;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;

import innovimax.quixproc.datamodel.convert.QuiXConvertException;
import innovimax.quixproc.datamodel.convert.QuiXEventStream2XMLStreamReader;
import innovimax.quixproc.datamodel.event.IQuiXEventStreamReader;
import net.sf.saxon.pull.PullSource;
import net.sf.saxon.pull.StaxBridge;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

class QuiXEventStream2XdmNodeConverter {
	private final IQuiXEventStreamReader reader;
	private final DocumentBuilder db;
	private XdmNode node = null;

	private static int counter = 1;
	private final int rank = counter++;

	public QuiXEventStream2XdmNodeConverter(DocumentBuilder db, IQuiXEventStreamReader reader) {
		this.reader = reader;
		this.db = db;
	}

	public XdmNode exec() throws QuiXConvertException {
		// System.out.println("DOMConverter.exec("+rank+")");
		try {
			XMLStreamReader xer = new QuiXEventStream2XMLStreamReader(this.reader);
			try {
				StaxBridge sb = new StaxBridge();
				sb.setXMLStreamReader(xer);
				Source source = new PullSource(sb);
				this.node = this.db.build(source);
			} catch (SaxonApiException e) {
				e.printStackTrace();
			} finally {
				this.reader.close();
			}
			return this.node;
		} catch (Exception e) {
			throw new QuiXConvertException(e);
		}
	}

}