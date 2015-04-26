package com.github.neoflyingsaucer.extend.controller.cancel;

public class FSCancelController 
{
	private static final ThreadLocal<FSCancelHandler> THREAD_CANCEL_HANDLER = new ThreadLocal<FSCancelHandler>() {
		@Override
		protected FSCancelHandler initialValue() {
			return new FSDefaultCancelHandler();
		}
	};
	
	private FSCancelController() { }
	
	public static void cancelOpportunity(Class<?> sourceClass)
	{
		FSCancelHandler handler = THREAD_CANCEL_HANDLER.get();
		handler.cancelOpportunity(sourceClass);
	}
	
	public static void setThreadCancelHandler(FSCancelHandler handler)
	{
		assert(handler != null);
		THREAD_CANCEL_HANDLER.set(handler);
	}
}
