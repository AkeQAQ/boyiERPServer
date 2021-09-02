package com.boyi.common.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target( { java.lang.annotation.ElementType.FIELD })
public @interface ExcelAttribute {
    //对应的列名称
    String name() default ""; // default java8新特性
    //列序号
    int sort();
    //字段类型对应的格式
    String format() default "";
}
