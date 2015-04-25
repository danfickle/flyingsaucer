package com.github.neoflyingsaucer.extend.controller.error;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class FSError 
{
	private int lineNumber;
	private LangId languageId;
	private FSErrorType errorType;
	private Object[] args;
	private String uri;
	private Class<?> sourceClass;
	private FSErrorLevel level;
	
	public static enum FSErrorLevel
	{
		TRACE,
		DEBUG,
		INFO,
		WARNING,
		ERROR;
	}
	
	public FSError(int lineNumber, LangId languageId, FSErrorType errorType, String uri, Class<?> sourceClass, FSErrorLevel level, Object... args)
	{
		this.setLineNumber(lineNumber);
		this.languageId = languageId;
		this.errorType = errorType;
		this.uri = uri;
		this.args = args;
		this.sourceClass = sourceClass;
		this.level = level;
	}

	public FSErrorLevel getLevel()
	{
		return level;
	}
	
	public int getLineNumber() 
	{
		return lineNumber;
	}

	public void setLineNumber(int lineNumber)
	{
		this.lineNumber = lineNumber;
	}

	public LangId getLanguageId()
	{
		return languageId;
	}

	public FSErrorType getErrorType()
	{
		return errorType;
	}
	
	public String getURI()
	{
		return uri;
	}

	public Object[] getArguments()
	{
		return args;
	}

	public Class<?> getSourceClass()
	{
		return sourceClass;
	}

	public String formatMessage(ResourceBundle bundle)
	{
		if (getLanguageId() == null)
			return "(null) language identifier";

		if (bundle == null)
			return "(null) resource bundle";

		String msgUnformatted = bundle.getString(getLanguageId().toString());
		if (msgUnformatted == null)
			return "(null) message for language identifier";

		return MessageFormat.format(msgUnformatted, args);
	}
}
