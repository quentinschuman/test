package com.example.demo.es.annotation;

import java.lang.annotation.*;

/**
 * 创建索引的注解
 * @author yangkebiao
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface EsIndexs {
	
	EsIndexType[] value() default {};
	
}
