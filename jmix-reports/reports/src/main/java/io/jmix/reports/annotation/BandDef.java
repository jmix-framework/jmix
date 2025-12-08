package io.jmix.reports.annotation;

import io.jmix.reports.entity.BandDefinition;
import io.jmix.reports.entity.Orientation;

import java.lang.annotation.*;

/**
 * Defines a band - element of the report data structure.
 * This annotation can be used several times on the report class to define all report bands.
 * <br/>
 * Model object is {@link BandDefinition}.
 * @see ReportDef
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RepeatableBandDef.class)
public @interface BandDef {

    /**
     * Unique band name within the report.
     */
    String name() default "";

    /**
     * Each report must have exactly one root band definition.
     * @return true if this is a root band
     */
    boolean root() default false;

    /**
     * Name of the parent band - which is other band defined in this report.
     */
    String parent() default "";

    /**
     * @return band orientation, applicable to spreadsheet-like output types.
     */
    Orientation orientation() default Orientation.HORIZONTAL;

    /**
     * Datasets of the band. May be empty if the band has no data.
     */
    DataSetDef[] dataSets() default {};
}
