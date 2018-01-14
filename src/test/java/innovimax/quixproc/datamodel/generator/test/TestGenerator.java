/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.generator.test;

import innovimax.quixproc.datamodel.generator.csv.ACSVGenerator.SimpleCSVGenerator;
import innovimax.quixproc.datamodel.generator.rdf.ARDFGenerator.SimpleRDFGenerator;
import innovimax.quixproc.datamodel.in.AStreamSource.CSVStreamSource;
import innovimax.quixproc.datamodel.in.AStreamSource.RDFStreamSource;
import java.lang.reflect.InvocationTargetException;
import static org.hamcrest.CoreMatchers.*;
import org.junit.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.EnumSet;

import org.apache.jena.atlas.web.TypedInputStream;
import org.junit.Test;

import innovimax.quixproc.datamodel.IQuiXStream;
import innovimax.quixproc.datamodel.IQuiXToken;
import innovimax.quixproc.datamodel.ValidQuiXTokenStream;
import innovimax.quixproc.datamodel.generator.AGenerator;
import innovimax.quixproc.datamodel.generator.AGenerator.FileExtension;
import innovimax.quixproc.datamodel.generator.AGenerator.Unit;
import innovimax.quixproc.datamodel.generator.AGenerator.Variation;
import innovimax.quixproc.datamodel.generator.ATreeGenerator;
import innovimax.quixproc.datamodel.generator.ATreeGenerator.SpecialType;
import innovimax.quixproc.datamodel.generator.ATreeGenerator.TreeType;
import innovimax.quixproc.datamodel.generator.csv.ACSVGenerator;
import innovimax.quixproc.datamodel.generator.rdf.ARDFGenerator;
import innovimax.quixproc.datamodel.in.AStreamSource;
import innovimax.quixproc.datamodel.in.QuiXEventStreamReader;

public class TestGenerator {

	enum Process {
		READ_BYTE, READ_BUFFER, PARSE
	}

	private static void testAll(final FileExtension ext, final Process process, final int size, final Unit unit)
			throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		switch (ext) {
		case CSV:
			testAllCSV(ext, process, size, unit);
			break;
		case HTML:
			// TODO
			break;
		case JSON:
		case XML:
		case YAML:
			testAllTree(ext, process, size, unit);
			break;
		case RDF:
			testAllRDF(ext, process, size, unit);
			break;
		default:
			break;
		}
	}

	private static void testAllTree(final FileExtension ext, final Process process, final int size, final Unit unit)
			throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		for (final TreeType gtype : TreeType.values()) {
			for (final SpecialType stype : SpecialType.allowedModifiers(ext, gtype)) {
				for (final Variation variation : Variation.values()) {
					System.out.format("Test %s START %d %s {%s, %s, %s, %s}%n", ext, size, unit, process, gtype, stype,
							variation);
					final long start = System.currentTimeMillis();
					final AGenerator generator = ATreeGenerator.instance(ext, gtype, stype);
					final InputStream is = generator.getInputStream(size, unit, variation);
					long event = 0;
					long totalsize = 0;
					switch (process) {
					case READ_BYTE:
						int c;
						while ((c = is.read()) != -1) {
							// do nothing
							totalsize++;
							// System.out.println(AGenerator.display(c));
						}
						break;
					case READ_BUFFER:
						final byte[] buffer = new byte[1024 * 1024];
						int length;
						while ((length = is.read(buffer)) > 0) {
							// do nothing
							totalsize += length;
						}
						break;
					case PARSE:
						final QuiXEventStreamReader xqesr = new QuiXEventStreamReader(AStreamSource.instance(ext, is));
						final IQuiXStream<IQuiXToken> vqxs = new ValidQuiXTokenStream(xqesr);
						while (vqxs.hasNext()) {
							// System.out.println(
							vqxs.next()
							// )
							;

							event++;
						}
						totalsize = size * unit.value();
						break;
					default:
					}
					long time = System.currentTimeMillis() - start;
					if (time == 0)
						time++;
					final long speed = 1000 * totalsize / time;
					System.out.format("Test %s END %,dms; %,dB/s; %,dB", ext, time, speed, totalsize);
					if (event > 0) {
						final long evpers = 1000 * event / time;
						final long density = 1000 * totalsize / event;
						System.out.format("; %,dev; %,dev/s; %,dB/kev", event, evpers, density);
					}
					System.out.println();
				}
			}
		}
	}

	private static void testAllRDF(final FileExtension ext, final Process process, final int size, final Unit unit) throws IOException {
		System.out.format("Test %s START %d %s {%s, %s}%n", ext, size, unit, process, Variation.NO_VARIATION);
		final long start = System.currentTimeMillis();
		final ARDFGenerator generator = new SimpleRDFGenerator();
		final TypedInputStream is = generator.getTypedInputStream(size, unit, Variation.NO_VARIATION);
		long event = 0;
		long totalsize = 0;
		switch (process) {
		case READ_BYTE:
			int c;
			while ((c = is.read()) != -1) {
				// do nothing
				totalsize++;
				// System.out.println(AGenerator.display(c));
			}
			break;
		case READ_BUFFER:
			final byte[] buffer = new byte[1024 * 1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				// do nothing
				totalsize += length;
			}
			break;
		case PARSE:
			final QuiXEventStreamReader xqesr = new QuiXEventStreamReader(RDFStreamSource.instance(is));
			final IQuiXStream<IQuiXToken> vqxs = new ValidQuiXTokenStream(xqesr);
			while (vqxs.hasNext()) {
				// System.out.println(
				vqxs.next()
				// )
				;

				event++;
			}
			vqxs.close();
			totalsize = size * unit.value();
			break;
		default:
		}
		long time = System.currentTimeMillis() - start;
		if (time == 0)
			time++;
		final long speed = 1000 * totalsize / time;
		System.out.format("Test %s END %,dms; %,dB/s; %,dB", ext, time, speed, totalsize);
		if (event > 0) {
			final long evpers = 1000 * event / time;
			final long density = 1000 * totalsize / event;
			System.out.format("; %,dev; %,dev/s; %,dB/kev", event, evpers, density);
		}
		System.out.println();

	}

	private static void testAllCSV(final FileExtension ext, final Process process, final int size, final Unit unit) throws IOException {
		System.out.format("Test %s START %d %s {%s, %s}%n", ext, size, unit, process, Variation.NO_VARIATION);
		final long start = System.currentTimeMillis();
		final ACSVGenerator generator = new SimpleCSVGenerator();
		final Reader is = generator.getReader(size, unit, Variation.NO_VARIATION);
		long event = 0;
		long totalsize = 0;
		switch (process) {
		case READ_BYTE:
			int c;
			while ((c = is.read()) != -1) {
				// do nothing
				totalsize++;
				// System.out.println(AGenerator.display(c));
			}
			break;
		case READ_BUFFER:
			final char[] buffer = new char[1024 * 1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				// do nothing
				totalsize += length;
			}
			break;
		case PARSE:
			final QuiXEventStreamReader xqesr = new QuiXEventStreamReader(CSVStreamSource.instance(is));
			final IQuiXStream<IQuiXToken> vqxs = new ValidQuiXTokenStream(xqesr);
			while (vqxs.hasNext()) {
				// System.out.println(
				vqxs.next()
				// )
				;

				event++;
			}
			vqxs.close();
			totalsize = size * unit.value();
			break;
		default:
		}
		long time = System.currentTimeMillis() - start;
		if (time == 0)
			time++;
		final long speed = 1000 * totalsize / time;
		System.out.format("Test %s END %,dms; %,dB/s; %,dB", ext, time, speed, totalsize);
		if (event > 0) {
			final long evpers = 1000 * event / time;
			final long density = 1000 * totalsize / event;
			System.out.format("; %,dev; %,dev/s; %,dB/kev", event, evpers, density);
		}
		System.out.println();
	}

	@Test
	public void testAllXML1K() throws Exception {
		for (final Process process : Process.values()) {
			testAll(FileExtension.XML, process, 1, Unit.KBYTE);
		}
		Assert.assertThat(true, is(true));
	}

//	@Test
	public void testAllXML1GNotParse()
			throws Exception {
		for (final Process process : EnumSet.of(Process.READ_BUFFER, Process.READ_BYTE)) {
			testAll(FileExtension.XML, process, 1, Unit.GBYTE);
		}
		Assert.assertThat(true, is(true));
	}

	@Test
	public void testAllJSON100K() throws Exception {
		for (final Process process : Process.values()) {
			testAll(FileExtension.JSON, process, 100, Unit.KBYTE);
		}
		Assert.assertThat(true, is(true));
	}

	@Test
	public void testAllYAML100K() throws Exception {
		for (final Process process : Process.values()) {
			testAll(FileExtension.YAML, process, 100, Unit.KBYTE);
		}
		Assert.assertThat(true, is(true));
	}

	@Test
	public void testAllRDF100K() throws Exception {
		for (final Process process : Process.values()) {
			testAll(FileExtension.RDF, process, 100, Unit.KBYTE);
		}
		Assert.assertThat(true, is(true));
	}

	@Test
	public void testAllCSV100K() throws Exception {
		for (final Process process : Process.values()) {
			testAll(FileExtension.CSV, process, 100, Unit.KBYTE);
		}
		Assert.assertThat(true, is(true));
	}

	public static void main(final String[] args)
			throws Exception {
		for (final Process process : EnumSet.of(/* Process.READ_BUFFER, */ Process.READ_BYTE, Process.PARSE)) {
			 testAll(FileExtension.XML, process, 1, Unit.MBYTE);
			// testAll(FileExtension.JSON, process, 10, Unit.MBYTE);
			// testAll(FileExtension.XML, process, 2, Unit.MBYTE);
			// testAll(FileExtension.RDF, process, 100, Unit.MBYTE);
			// testAll(FileExtension.CSV, process, 100, Unit.MBYTE);
			//testAll(FileExtension.YAML, process, 10, Unit.MBYTE);
		}
	}

}
