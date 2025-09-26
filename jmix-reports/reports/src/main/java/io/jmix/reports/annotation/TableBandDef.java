package io.jmix.reports.annotation;

import io.jmix.reports.entity.table.TemplateTableBand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Part of the table template definition.
 * Represents a report band whose data needs to be displayed in the resulting table.
 * <br/>
 * Model object is {@link TemplateTableBand}.
 * @see TemplateTableDef
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableBandDef {

    /**
     * Name of a report band declared in the current report definition.
     */
    String bandName();

    /**
     * Columns displaying fields from the band.
     */
    TableColumnDef[] columns();
}
