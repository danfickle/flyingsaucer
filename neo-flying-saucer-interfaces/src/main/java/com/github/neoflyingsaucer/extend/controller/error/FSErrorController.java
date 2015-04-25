package com.github.neoflyingsaucer.extend.controller.error;

import java.util.Locale;

import com.github.neoflyingsaucer.extend.controller.error.FSError.FSErrorLevel;
import com.github.neoflyingsaucer.extend.useragent.LangId;

public class FSErrorController 
{
	private static final ThreadLocal<FSErrorHandler> THREAD_ERROR_HANDLER = new ThreadLocal<FSErrorHandler>() {
		@Override
		protected FSErrorHandler initialValue() {
			return new FSDefaultErrorHandler(Locale.US);
		}
	};
	
	public static void onError(FSError err)
	{
		FSErrorHandler errorHandler = THREAD_ERROR_HANDLER.get();
		errorHandler.onError(err);
	}
	
	public static void log(Class<?> sourceClass, FSErrorLevel level, LangId languageId, Object... args)
	{
		FSError err = new FSError(-1, languageId, FSErrorType.LOGGING, null, sourceClass, level, args);
		onError(err);
	}
	
	public static void setThreadErrorHandler(FSErrorHandler handler)
	{
		THREAD_ERROR_HANDLER.set(handler);
	}
}
