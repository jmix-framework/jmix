package io.jmix.reports.annotation;

import io.jmix.reports.entity.table.TemplateTableColumn;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Part of the table template definition.
 * Represents a column in the resulting table that takes its values from a band data by key.
 * <br/>
 * Model object is {@link TemplateTableColumn}.
 * @see TableBandDef
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableColumnDef {

    /**
     * Name of a dataset property, used to fetch values from the band data.
     */
    String key();

    /**
     * Column caption.
     * Use <code>msg://group/key</code> format if localization is required.
     */
    String caption();
}