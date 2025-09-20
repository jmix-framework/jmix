package io.jmix.reports.annotation;

import io.jmix.reports.entity.table.TemplateTableDescription;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines structure of the template with {@link io.jmix.reports.entity.ReportOutputType#TABLE} output.
 * Model object is {@link TemplateTableDescription}.
 *
 * @see TemplateDef
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TemplateTableDef {

    /**
     * One or more bands to be included into the output.
     * For each band a separate table component will be displayed.
     */
    TableBandDef[] bands();

}
