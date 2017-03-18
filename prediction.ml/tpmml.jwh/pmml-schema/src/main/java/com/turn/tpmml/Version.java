/*
 * Copyright (c) 2012 University of Tartu
 */
package com.turn.tpmml;

public enum Version {
	PMML_3_0("http://www.dmg.org/PMML-3_0"),
	PMML_3_1("http://www.dmg.org/PMML-3_1"),
	PMML_3_2("http://www.dmg.org/PMML-3_2"),
	PMML_4_0("http://www.dmg.org/PMML-4_0"),
	PMML_4_1("http://www.dmg.org/PMML-4_1"),
	PMML_4_2("http://www.dmg.org/PMML-4_2"),
	PMML_4_3("http://www.dmg.org/PMML-4_3"),
	;

	private String namespaceUri = null;


	private Version(String namespaceUri){
		setNamespaceURI(namespaceUri);
	}

	public String getNamespaceURI(){
		return this.namespaceUri;
	}

	private void setNamespaceURI(String namespaceUri){
		this.namespaceUri = namespaceUri;
	}

	static
	public Version forNamespaceURI(String namespaceURI){
		Version[] versions = Version.values();

		for(Version version : versions){

			if((version.getNamespaceURI()).equals(namespaceURI)){
				return version;
			}
		}

		throw new IllegalArgumentException(namespaceURI);
	}
}
