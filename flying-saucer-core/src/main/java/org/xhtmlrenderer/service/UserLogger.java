package org.xhtmlrenderer.service;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.util.LangId;

/*
 * These loggers are designed to be used to output messages to the user. This may differ
 * from the application owner and there may be different users on different threads.
 * By using a thread local we can set the language on a per thread basis.
 * USAGE:
 *   UserLogger.CSS_ISSUES_LOG.warn(UserLogger.getMsg(LangId.INVALID_CSS_VALUE, arg1, arg2));
 */
public class UserLogger
{
	public static final Logger LHTML = LoggerFactory.getLogger("HTML_ISSUES_USER_LOG");
	public static final Logger LCSS = LoggerFactory.getLogger("CSS_ISSUES_USER_LOG");	
	public static final Logger LHTTP = LoggerFactory.getLogger("HTTP_ISSUES_USER_LOG");	

	public static final ThreadLocal<ResourceBundle> rb = new ThreadLocal<ResourceBundle>() 
	{
		protected ResourceBundle initialValue() 
		{
			return ResourceBundle.getBundle("languages.ErrorMessages", Locale.US);
		}
	};
	
	public static void setLanguageForCurrentThread(Locale lang)
	{
		rb.set(ResourceBundle.getBundle("languages.ErrorMessages", lang));
	}
	
	public static String i18n(LangId msg, Object... args)
	{
		if (rb.get().containsKey(msg.toString()))
		{
			return MessageFormat.format(rb.get().getString(msg.toString()), args);
		}
		
		return "No message for given identifier";
	}
}
