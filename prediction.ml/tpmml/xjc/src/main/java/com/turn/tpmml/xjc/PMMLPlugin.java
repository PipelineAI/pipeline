/*
 * Copyright (c) 2009 University of Tartu
 */
package com.turn.tpmml.xjc;

import java.util.*;

import com.sun.codemodel.*;
import com.sun.tools.xjc.*;
import com.sun.tools.xjc.model.*;
import com.sun.tools.xjc.outline.*;
import com.sun.xml.bind.v2.model.core.*;
import com.sun.xml.xsom.*;

import org.xml.sax.ErrorHandler;

public class PMMLPlugin extends Plugin {

	@Override
	public String getOptionName(){
		return "Xpmml";
	}

	@Override
	public String getUsage(){
		return null;
	}

	@Override
	public void postProcessModel(Model model, ErrorHandler errorHandler){
		super.postProcessModel(model, errorHandler);

		Collection<CClassInfo> classInfos = (model.beans()).values();
		for(CClassInfo classInfo : classInfos){
			boolean hasExtension = false;

			CTypeRef extension = null;

			Collection<CPropertyInfo> propertyInfos = classInfo.getProperties();
			for(CPropertyInfo propertyInfo : propertyInfos){
				String publicName = propertyInfo.getName(true);
				String privateName = propertyInfo.getName(false);

				if(propertyInfo.isCollection()){

					// Will be renamed to "Extensions"
					if((privateName).equalsIgnoreCase("Extension")){
						hasExtension |= true;
					} // End if

					if((privateName).contains("And") || (privateName).contains("Or")){
						propertyInfo.setName(true, "Content");
						propertyInfo.setName(false, "content");

						extension = extractExtension(propertyInfo);
					} else

					if((privateName).equalsIgnoreCase("Content")){
						extension = extractExtension(propertyInfo);
					} else

					{
						if(privateName.endsWith("array") || privateName.endsWith("Array")){
							publicName += "s";
							privateName += "s";
						} else

						{
							publicName = JJavaName.getPluralForm(publicName);
							privateName = JJavaName.getPluralForm(privateName);
						}

						propertyInfo.setName(true, publicName);
						propertyInfo.setName(false, privateName);
					}
				} else

				{
					if((privateName).equals("isScorable")){
						propertyInfo.setName(true, "Scorable");
						propertyInfo.setName(false, "scorable");
					}
				}
			}

			if(hasExtension){
				extension = null;
			} // End if

			if(extension != null){
				CElementPropertyInfo elementPropertyInfo = new CElementPropertyInfo("Extensions", CElementPropertyInfo.CollectionMode.REPEATED_ELEMENT, ID.NONE, null, null, null, null, false);
				(elementPropertyInfo.getTypes()).add(extension);

				(classInfo.getProperties()).add(0, elementPropertyInfo);
			}
		}
	}

	@Override
	public boolean run(Outline outline, Options options, ErrorHandler errorHandler){
		return true;
	}

	private CTypeRef extractExtension(CPropertyInfo propertyInfo){
		CTypeRef result = null;

		if(propertyInfo instanceof CElementPropertyInfo){
			CElementPropertyInfo elementPropertyInfo = (CElementPropertyInfo)propertyInfo;

			Iterator<? extends CTypeRef> types = (elementPropertyInfo.getTypes()).iterator();
			while(types.hasNext()){
				CTypeRef type = types.next();

				if(isExtension(type.getTarget())){
					result = type;

					types.remove();
				}
			}
		} else

		if(propertyInfo instanceof CReferencePropertyInfo){
			CReferencePropertyInfo referencePropertyInfo = (CReferencePropertyInfo)propertyInfo;

			Iterator<CElement> elements = (referencePropertyInfo.getElements()).iterator();
			while(elements.hasNext()){
				CElement element = elements.next();

				if(isExtension(element)){
					result = new CTypeRef((CClassInfo)element, (XSElementDecl)element.getSchemaComponent());

					elements.remove();
				}
			}
		}

		return result;
	}

	private boolean isExtension(Object object){

		if(object instanceof CClassInfo){
			CClassInfo classInfo = (CClassInfo)object;

			return (classInfo.fullName()).equals("com.turn.tpmml.Extension");
		}

		return false;
	}
}
