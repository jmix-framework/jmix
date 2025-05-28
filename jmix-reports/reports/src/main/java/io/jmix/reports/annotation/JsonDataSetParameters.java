package io.jmix.reports.annotation;

import io.jmix.reports.entity.JsonSourceType;
import io.jmix.reports.entity.ParameterType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Additional parameters for a data set definition of {@link io.jmix.reports.entity.DataSetType#JSON} type.
 * JSON data set first loads the input from the specified source, and then extracts necessary items from the input
 *  using a JSON Path query. Result of the extraction is returned as the band data.
 * <br/>
 * Model object is {@link io.jmix.reports.entity.DataSet}.
 * @see DataSetDef
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonDataSetParameters {

    /**
     * Specifies where the JSON input comes from.
     * Note: GROOVY_SCRIPT isn't supported here, use DELEGATE instead.
     */
    JsonSourceType source() default JsonSourceType.URL;

//    /**
//     * Groovy script text returning JSON content, for {@link JsonSourceType#GROOVY_SCRIPT} source.
//     */
//    String script() default "";

    /**
     * URL where the JSON input will be loaded from, for {@link JsonSourceType#URL} source.
     */
    String url() default "";

    /**
     * JSON Path query to extract necessary data from the received JSON input (for all sources).
     * Example: <code>$.store.book[*]</code>.
     */
    String jsonPathQuery() default "";

    /**
     * Alias of the input parameter used as source for the JSON input, for {@link JsonSourceType#PARAMETER} source.
     */
    String inputParameter() default "";
}
