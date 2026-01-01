package com.phms.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的接口方法
 *
 * @author PHMS
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperateLog {

    /**
     * 操作模块
     */
    String module() default "";

    /**
     * 操作类型：add=新增 update=修改 delete=删除 query=查询
     */
    String type() default "";

    /**
     * 操作描述
     */
    String description() default "";
}
