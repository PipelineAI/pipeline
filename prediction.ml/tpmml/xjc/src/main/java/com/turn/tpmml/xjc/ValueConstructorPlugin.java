/*
 * Copyright (c) 2010 University of Tartu
 */
package com.turn.tpmml.xjc;

import java.util.*;

import com.sun.codemodel.*;
import com.sun.tools.xjc.*;
import com.sun.tools.xjc.model.*;
import com.sun.tools.xjc.outline.*;

import org.xml.sax.*;

public class ValueConstructorPlugin extends Plugin {

	private boolean ignoreAttributes = false;

	private boolean ignoreElements = false;

	private boolean ignoreValues = false;


	@Override
	public String getOptionName(){
		return "XvalueConstructor";
	}

	@Override
	public String getUsage(){
		return null;
	}

	@Override
	public int parseArgument(Options options, String[] args, int i){
		int result = 0;

		String prefix = ("-" + getOptionName());

		for(int j = i; j < args.length; j++, result++){
			String arg = args[j];

			if(prefix.equals(arg)){
				// Ignored
			} else

			if((prefix + ":" + "ignoreAttributes").equals(arg)){
				setIgnoreAttributes(true);
			} else

			if((prefix + ":" + "ignoreElements").equals(arg)){
				setIgnoreElements(true);
			} else

			if((prefix + ":" + "ignoreValues").equals(arg)){
				setIgnoreValues(true);
			} else

			{
				break;
			}
		}

		return result;
	}

	@Override
	@SuppressWarnings (
		value = {"unused"}
	)
	public boolean run(Outline outline, Options options, ErrorHandler errorHandler){
		Collection<? extends ClassOutline> clazzes = outline.getClasses();

		for(ClassOutline clazz : clazzes){
			List<JFieldVar> superClassFields = getSuperClassFields(clazz);
			List<JFieldVar> classFields = getClassFields(clazz);

			if(superClassFields.size() > 0 || classFields.size() > 0){
				JMethod defaultConstructor = (clazz.implClass).constructor(JMod.PUBLIC);
				JInvocation defaultSuperInvocation = defaultConstructor.body().invoke("super");

				// XXX
				defaultConstructor.annotate(Deprecated.class);

				JMethod valueConstructor = (clazz.implClass).constructor(JMod.PUBLIC);
				JInvocation valueSuperInvocation = valueConstructor.body().invoke("super");

				for(JFieldVar superClassField : superClassFields){
					JVar param = valueConstructor.param(JMod.FINAL, superClassField.type(), superClassField.name());

					valueSuperInvocation.arg(param);
				}

				for(JFieldVar classField : classFields){
					JVar param = valueConstructor.param(JMod.FINAL, classField.type(), classField.name());

					valueConstructor.body().assign(JExpr.refthis(param.name()), param);
				}
			}
		}

		return true;
	}

	private List<JFieldVar> getSuperClassFields(ClassOutline clazz){
		List<JFieldVar> result = new ArrayList<JFieldVar>();

		for(ClassOutline superClazz = clazz.getSuperClass(); superClazz != null; superClazz = superClazz.getSuperClass()){
			result.addAll(0, getValueFields(superClazz));
		}

		return result;
	}

	private List<JFieldVar> getClassFields(ClassOutline clazz){
		return getValueFields(clazz);
	}

	private List<JFieldVar> getValueFields(ClassOutline clazz){
		List<JFieldVar> result = new ArrayList<JFieldVar>();

		FieldOutline[] fields = clazz.getDeclaredFields();
		for(FieldOutline field : fields){
			CPropertyInfo propertyInfo = field.getPropertyInfo();

			if(propertyInfo.isCollection()){
				continue;
			}

			JFieldVar fieldVar = (clazz.implClass.fields()).get(propertyInfo.getName(false));

			if((fieldVar.mods().getValue() & JMod.STATIC) == JMod.STATIC){
				continue;
			} // End if

			if(propertyInfo instanceof CAttributePropertyInfo && !getIgnoreAttributes()){
				CAttributePropertyInfo attributePropertyInfo = (CAttributePropertyInfo)propertyInfo;

				if(attributePropertyInfo.isRequired()){
					result.add(fieldVar);
				}
			} // End if

			if(propertyInfo instanceof CElementPropertyInfo && !getIgnoreElements()){
				CElementPropertyInfo elementPropertyInfo = (CElementPropertyInfo)propertyInfo;

				if(elementPropertyInfo.isRequired()){
					result.add(fieldVar);
				}
			} // End if

			if(propertyInfo instanceof CValuePropertyInfo && !getIgnoreValues()){

				{
					result.add(fieldVar);
				}
			}
		}

		return result;
	}

	public boolean getIgnoreAttributes(){
		return this.ignoreAttributes;
	}

	private void setIgnoreAttributes(boolean ignoreAttributes){
		this.ignoreAttributes = ignoreAttributes;
	}

	public boolean getIgnoreElements(){
		return this.ignoreElements;
	}

	private void setIgnoreElements(boolean ignoreElements){
		this.ignoreElements = ignoreElements;
	}

	public boolean getIgnoreValues(){
		return this.ignoreValues;
	}

	private void setIgnoreValues(boolean ignoreValues){
		this.ignoreValues = ignoreValues;
	}
}
