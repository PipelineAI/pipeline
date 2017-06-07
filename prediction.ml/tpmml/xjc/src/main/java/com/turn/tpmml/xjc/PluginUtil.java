/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.xjc;

import java.util.*;

import com.sun.tools.xjc.*;
import com.sun.tools.xjc.model.*;

import org.w3c.dom.*;

public class PluginUtil {

	private PluginUtil(){
	}

	static
	public CPluginCustomization getCustomization(CCustomizable customizable, Plugin plugin){
		List<CPluginCustomization> customizations = getAllCustomizations(customizable, plugin);

		if(customizations.size() == 0){
			return null;
		} else

		if(customizations.size() == 1){
			return customizations.get(0);
		}

		throw new IllegalStateException();
	}

	static
	public List<CPluginCustomization> getAllCustomizations(CCustomizable customizable, Plugin plugin){
		List<CPluginCustomization> result = new ArrayList<CPluginCustomization>();

		Iterator<CPluginCustomization> it = (customizable.getCustomizations()).iterator();
		while(it.hasNext()){
			CPluginCustomization customization = it.next();

			Element element = customization.element;

			if(plugin.isCustomizationTagName(element.getNamespaceURI(), element.getLocalName())){
				result.add(customization);
			}
		}

		return result;
	}
}
