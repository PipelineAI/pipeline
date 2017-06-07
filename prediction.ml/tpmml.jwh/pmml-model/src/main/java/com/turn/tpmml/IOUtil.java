/*
 * Copyright (c) 2012 University of Tartu
 */
package com.turn.tpmml;

import java.io.*;

import javax.xml.bind.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class IOUtil {

	private IOUtil(){
	}

	static
	public Source createImportSource(InputSource source) throws SAXException {
		XMLReader reader = XMLReaderFactory.createXMLReader();

		ImportFilter filter = new ImportFilter(reader);

		return new SAXSource(filter, source);
	}

	@SuppressWarnings("restriction")
	static
	public PMML unmarshal(File file) throws IOException, SAXException, JAXBException {
		InputStream is = new FileInputStream(file);

		try {
			return unmarshal(is);
		} finally {
			is.close();
		}
	}

	@SuppressWarnings("restriction")
	static
	public PMML unmarshal(InputStream is) throws SAXException, JAXBException {
		return unmarshal(new InputSource(is));
	}

	@SuppressWarnings("restriction")
	static
	public PMML unmarshal(InputSource source) throws SAXException, JAXBException {
		Source importSource = createImportSource(source);

		Unmarshaller unmarshaller = getJAXBContext().createUnmarshaller();

		return (PMML)unmarshaller.unmarshal(importSource);
	}

	@SuppressWarnings("restriction")
	static
	public void marshal(PMML pmml, File file) throws IOException, JAXBException {
		OutputStream os = new FileOutputStream(file);

		try {
			marshal(pmml, os);
		} finally {
			os.close();
		}
	}

	@SuppressWarnings("restriction")
	static
	public void marshal(PMML pmml, OutputStream os) throws JAXBException {
		marshal(pmml, new StreamResult(os));
	}

	@SuppressWarnings("restriction")
	static
	public void marshal(PMML pmml, Result result) throws JAXBException {
		Marshaller marshaller = getJAXBContext().createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		marshaller.marshal(pmml, result);
	}

	@SuppressWarnings("restriction")
	static
	private JAXBContext getJAXBContext() throws JAXBException {

		if(IOUtil.jaxbCtx == null){
			IOUtil.jaxbCtx = JAXBContext.newInstance(ObjectFactory.class);
		}

		return IOUtil.jaxbCtx;
	}

	@SuppressWarnings("restriction")
	private static JAXBContext jaxbCtx = null;
}
