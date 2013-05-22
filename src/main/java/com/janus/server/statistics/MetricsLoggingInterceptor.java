package com.janus.server.statistics;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.slf4j.Logger;

@LogMetrics
@Interceptor
public class MetricsLoggingInterceptor {

	@Inject
	private Logger logger;
	
	@AroundInvoke
	public Object doMetricsLogging(InvocationContext context) throws Exception {
		
		//this.logger this.logger = this.loggerFactory.getthis.logger(this.getClass());
		
		// don't include string creation
		String methodPoint = context.getTarget().getClass().getSuperclass().getSimpleName() + "#" + context.getMethod().getName();		
		
		long start = System.currentTimeMillis();
		
		Object resultOfCall;
		try {
			resultOfCall = context.proceed();
		} catch (Exception e) {
			this.logger.error("Exception while calling '{}': {}", methodPoint, e.getMessage());
			throw e;
		}
		
		// stop timer
		long time = System.currentTimeMillis() - start;
		
		// create and initialize log line output
		List<Object> outputLog = new LinkedList<Object>();
		outputLog.add(methodPoint);
		outputLog.add(time);

		String outputLine = "'{}' in {}ms";
		
		// if debugging is enabled print additional metrics information
		if(this.logger.isDebugEnabled()) {
			if(context.getParameters() != null && context.getParameters().length > 0) {
				outputLine += " (with parameters:";
				for(int i = 0; i < context.getParameters().length; i++) {
					
					// just print the first 4 items, and give info on how many remain
					if(i > 3) {
						outputLine = " ... and " + (context.getParameters().length - 4) + " more";
						break;
					}
					
					// add parameter
					outputLine += " '{}'";
					outputLog.add(context.getParameters()[i]);
				}
				outputLine += ")";
			}
		}
		
		// log
		this.logger.info(outputLine, outputLog.toArray());
				
		return resultOfCall;
	}
	
}

