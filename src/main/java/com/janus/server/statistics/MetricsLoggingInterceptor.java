package com.janus.server.statistics;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@LogMetrics
@Interceptor
public class MetricsLoggingInterceptor {

	//@Inject
	//private Logger logger;
	
	@AroundInvoke
	public Object doMetricsLogging(InvocationContext context) throws Exception {
		
		Logger logger = LoggerFactory.getLogger(this.getClass());
		
		// don't include string creation
		String methodPoint = context.getMethod().getDeclaringClass().getSimpleName() + "#" + context.getMethod().getName();		
		
		long start = System.currentTimeMillis();
		
		Object resultOfCall;
		try {
			resultOfCall = context.proceed();
		} catch (Exception e) {
			logger.error("Exception: {}", e.getMessage());
			throw e;
		}
		
		long time = System.currentTimeMillis() - start;
		logger.info("Calling '{}' took {}ms", methodPoint, time);
				
		return resultOfCall;
	}
	
}
