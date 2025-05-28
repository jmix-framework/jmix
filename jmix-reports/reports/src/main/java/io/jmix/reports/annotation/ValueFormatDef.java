package io.jmix.reports.annotation;

import java.lang.annotation.*;

/**
 * Defines a value format - element of the report structure.
 * Value formats are used to fine-tune output formatting for any field produced by the report.
 * <br/>
 * This annotation can be used several times on the report class if it has several value formats.
 * Additional associated logic can be declared by creating a method annotated with {@link ValueFormatDelegate}.
 * <br/>
 * Model object is {@link io.jmix.reports.entity.ReportValueFormat}.
 * @see ReportDef
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RepeatableValueFormatDef.class)
public @interface ValueFormatDef {

    /**
     * Name of the report band containing the field.
     */
    String band();

    /**
     * Name of the field to be formatted.
     */
    String field();

    /**
     * Field format.
     * For number values specify the format according to the {@link java.text.DecimalFormat} rules,
     * for dates - {@link java.text.SimpleDateFormat}.
     * Built-in formats for inserting an image, html blocks and others are also available - check the documentation.
     */
    String format() default "";

//    /**
//     * Groovy script formatting the field value.
//     * Note: Consider using {@link io.jmix.reports.yarg.structure.CustomValueFormatter} delegate instead.
//     */
//    String script() default "";
}
