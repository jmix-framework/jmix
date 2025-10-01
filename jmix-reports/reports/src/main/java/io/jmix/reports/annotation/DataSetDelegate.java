package io.jmix.reports.annotation;

import io.jmix.reports.delegate.FetchPlanProvider;
import io.jmix.reports.delegate.JsonInputProvider;
import io.jmix.reports.yarg.loaders.ReportDataLoader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation marks a delegate method that implements some logic related to a data set
 *   defined in the report definition class.
 * Method must conform to convention: no parameters, the result is one of supported functional interfaces.
 * Currently supported interfaces:
 * <li> {@link ReportDataLoader} - to fully delegate loading of the data</li>
 * <li> {@link FetchPlanProvider} - to build a fetch plan for entity data set</li>
 * <li> {@link JsonInputProvider} - to supply JSON input for JSON data set</li>
 *
 * @see DataSetDef
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataSetDelegate {
    /**
     * Name of the data set declared in the current report definition.
     */
    String name();
}
