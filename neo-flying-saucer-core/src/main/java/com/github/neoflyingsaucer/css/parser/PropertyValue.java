package com.github.neoflyingsaucer.css.parser;

import java.util.List;

import org.w3c.dom.css.RGBColor;

import com.github.neoflyingsaucer.css.constants.CSSPrimitiveUnit;
import com.github.neoflyingsaucer.css.constants.IdentValue;
import com.github.neoflyingsaucer.css.parser.PropertyValueImp.CSSValueType;

public interface PropertyValue
{
	public CSSPrimitiveUnit getPrimitiveTypeN();
	public CSSValueType getCssValueTypeN();
	
	public float getFloatValue();
	public FSFunction getFunction();
	public IdentValue getIdentValue();
	public List<?> getValues();
	public Token getOperator();
	public String[] getStringArrayValue();
	public FSColor getFSColor();
	public RGBColor getRGBColorValue();
	
	public void setIdentValue(IdentValue identValue);
	public void setOperator(Token operatorToken);
	public void setStringArrayValue(String[] strings);
	public short getPropertyValueType();

	public String getFingerprint();
	public String getStringValue();
	public String getCssText();
}
