package io.jmix.reports.annotation;

import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.Orientation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a data set as part of the {@link BandDef}.
 * Additional associated logic can be declared by creating a method annotated with {@link DataSetDelegate}.
 * <br/>
 * Model object is {@link io.jmix.reports.entity.DataSet}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataSetDef {

    /**
     * Data set name (optional).
     * <br/>
     * For Crosstab {@link Orientation#CROSS}) bands, its data sets must be named strictly according to convention:
     *  <li>Column headers - <code>${band_name}_dynamic_header</code></li>
     *  <li>Row headers - <code>${band_name}_master_data</code></li>
     *  <li>Main content - <code>${band_name}</code></li>
     */
    String name() default "";

    /**
     * Type that determines how band data will be obtained.
     * Note: GROOVY is not supported here, use DELEGATE instead.
     */
    DataSetType type();

    /**
     * Additional parameters for {@link DataSetType#JPQL} type.
     */
    JsonDataSetParameters json() default @JsonDataSetParameters();

    /**
     * Additional parameters for {@link DataSetType#SINGLE} and {@link DataSetType#MULTI} types.
     */
    EntityDataSetDef entity() default @EntityDataSetDef();

    /**
     * Name of the band field used to merge data from multiple datasets inside one band.
     */
    String linkParameterName() default "";

    /**
     * Name of data store, if the SQL or JPQL query needs to be executed in the additional data store.
     */
    String dataStore() default "";

    /**
     * Flag activating Groovy template processing of the query text.
     */
    boolean processTemplate() default false;

    /**
     * Query text for {@link DataSetType#SQL} and {@link DataSetType#JPQL} types.
     */
    String query() default "";
}
