package com.github.jparest;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Aspect
@Order(value=0)
@Component
public class ErrorTranslationAspect {
		
	private static final Log log = LogFactory.getLog(ErrorTranslationAspect.class);

	@Around("@annotation(org.springframework.web.bind.annotation.ResponseBody)")  
	public Object translateThrowable(ProceedingJoinPoint joinPoint) {
		try {
			Object[] args = joinPoint.getArgs();
			return joinPoint.proceed(args);
		}
		catch (Throwable e) {
			log.error("Unable to fulfill request: " + joinPoint.toShortString(), e);
			return new ResponseEntity<String>( e.getMessage(), INTERNAL_SERVER_ERROR );
		}		
	}
}
