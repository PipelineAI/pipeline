/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.xjc;

import java.util.*;

import com.sun.codemodel.*;
import com.sun.tools.xjc.*;
import com.sun.tools.xjc.model.*;
import com.sun.tools.xjc.outline.*;
import com.sun.xml.bind.api.impl.*;

import org.w3c.dom.*;

import org.xml.sax.*;

public class UnsupportedPropertyPlugin extends Plugin {

	@Override
	public String getOptionName(){
		return "XunsupportedProperty";
	}

	@Override
	public String getUsage(){
		return null;
	}

	@Override
	public List<String> getCustomizationURIs(){
		return Collections.singletonList(JAVA_URI);
	}

	@Override
	public boolean isCustomizationTagName(String nsUri, String localName){
		return nsUri.equals(JAVA_URI) && localName.equals("unsupportedProperty");
	}

	@Override
	public boolean run(Outline outline, Options options, ErrorHandler errorHandler){
		Model model = outline.getModel();

		// XXX
		JClass exceptionClazz = (model.codeModel).ref(UnsupportedOperationException.class);

		Collection<? extends ClassOutline> clazzes = outline.getClasses();
		for(ClassOutline clazz : clazzes){
			List<CPluginCustomization> customizations = PluginUtil.getAllCustomizations(clazz.target, this);

			for(CPluginCustomization customization : customizations){
				Element element = customization.element;

				String property = element.getAttribute("property");
				if(property == null){
					throw new IllegalArgumentException();
				}

				String propertyName = NameConverter.standard.toPropertyName(property);

				String name = element.getAttribute("name");
				if(name == null){
					throw new IllegalArgumentException();
				}

				JClass nameClazz = (clazz.implClass).owner().ref(name);

				JMethod getter = (clazz.implClass).method(JMod.PUBLIC, nameClazz, "get" + propertyName);
				getter.body()._throw(JExpr._new(exceptionClazz));

				JMethod setter = (clazz.implClass).method(JMod.PUBLIC, void.class, "set" + propertyName);
				setter.param(nameClazz, property);
				setter.body()._throw(JExpr._new(exceptionClazz));

				customization.markAsAcknowledged();
			}
		}

		return true;
	}

	private static final String JAVA_URI = "http://java.sun.com/java";
}
