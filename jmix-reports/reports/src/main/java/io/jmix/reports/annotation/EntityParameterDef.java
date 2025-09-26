package io.jmix.reports.annotation;

import io.jmix.reports.entity.ParameterType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Additional parameters for an input parameter definition of {@link ParameterType#ENTITY}
 * and {@link ParameterType#ENTITY_LIST} types.
 * <br/>
 * Model object is {@link io.jmix.reports.entity.ReportInputParameter}.
 * @see InputParameterDef
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityParameterDef {

    /**
     * Entity class of the parameter.
     */
    Class<?> entityClass() default void.class;

    /**
     * Kind of UI component used to input {@link ParameterType#ENTITY} parameter value when launching report from UI.
     */
    EntityInputComponent component() default EntityInputComponent.LOOKUP_VIEW;

    /**
     * <code>Join</code> clause to be added to the JPQL query that loads options
     */
    String optionsQueryJoin() default "";

    /**
     * <code>Where</code> clause to be added to the JPQL query that loads options
     */
    String optionsQueryWhere() default "";

    /**
     * Id of the Lookup View opened to pick {@link ParameterType#ENTITY} or {@link ParameterType#ENTITY_LIST} parameter value.
     */
    String lookupViewId() default "";
}
