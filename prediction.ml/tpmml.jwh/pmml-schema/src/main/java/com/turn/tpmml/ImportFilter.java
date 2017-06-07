/*
 * Copyright (c) 2009 University of Tartu
 */
package com.turn.tpmml;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class ImportFilter extends XMLFilterImpl {

	public ImportFilter(){
	}

	public ImportFilter(XMLReader parent){
		super(parent);
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes attributes) throws SAXException{
		super.startElement(filterNamespaceURI(namespaceURI), filterLocalName(namespaceURI, localName), qualifiedName, attributes);
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qualifiedName) throws SAXException {
		super.endElement(filterNamespaceURI(namespaceURI), filterLocalName(namespaceURI, localName), qualifiedName);
	}

	private String filterNamespaceURI(String namespaceURI){
		Version version = forNamespaceURI(namespaceURI);

		if((version).compareTo(Version.PMML_3_0) >= 0 && (version).compareTo(Version.PMML_4_3) <= 0){
			return ImportFilter.version.getNamespaceURI();
		}

		return namespaceURI;
	}

	private String filterLocalName(String namespaceURI, String localName){
		Version version = forNamespaceURI(namespaceURI);

		if((version).equals(Version.PMML_4_0)){

			if(("Trend").equals(localName)){
				return "Trend_ExpoSmooth";
			}
		}

		return localName;
	}

	static
	private Version forNamespaceURI(String namespaceURI){

		if(("").equals(namespaceURI)){
			return ImportFilter.version;
		}

		return Version.forNamespaceURI(namespaceURI);
	}

	private static final Version version = Version.PMML_4_1;
}
