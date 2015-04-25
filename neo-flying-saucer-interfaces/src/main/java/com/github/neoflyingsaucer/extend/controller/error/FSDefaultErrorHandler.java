package com.github.neoflyingsaucer.extend.controller.error;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.neoflyingsaucer.extend.useragent.LangId;

public class FSDefaultErrorHandler implements FSErrorHandler 
{
	public ResourceBundle bundle;
	private Map<Class<?>, Logger> loggers = new HashMap<Class<?>, Logger>();
	
	public FSDefaultErrorHandler(Locale locale)
	{
		this.bundle = ResourceBundle.getBundle("languages.ErrorMessages", locale);
	}
	
	@Override
	public void onError(FSError error) 
	{
		String msg = error.formatMessage(bundle);
		Logger logger;
		
		if (loggers.containsKey(error.getSourceClass()))
		{
			logger = loggers.get(error.getSourceClass());
		}
		else
		{
			logger = LoggerFactory.getLogger(error.getSourceClass());
			loggers.put(error.getSourceClass(), logger);
		}
		
		if (error.getErrorType() == FSErrorType.CSS_ERROR)
		{
			String cssMsg = bundle.getString(LangId.CSS_ERROR.toString());
			String cssMsgFormatted = MessageFormat.format(cssMsg, error.getLineNumber());

			msg = cssMsgFormatted + '\n' + msg;
		}
		
		switch(error.getLevel())
		{
		case DEBUG:
			logger.debug(msg);
			break;
		case ERROR:
			logger.error(msg);
			break;
		case INFO:
			logger.info(msg);
			break;
		case TRACE:
			logger.trace(msg);
			break;
		case WARNING:
			logger.warn(msg);
			break;
		default:
			logger.error(msg);
			break;
		}
	}
}
