package io.jmix.reports.yarg.structure;

import org.jspecify.annotations.NullMarked;

/**
 * Custom implementation of the result field formatting.
 *
 * @param <T> field type
 * @see ReportFieldFormat
 */
@FunctionalInterface
@NullMarked
public interface CustomValueFormatter<T> {

    /**
     * Convert the given value into desired string representation.
     *
     * @param value value of the result field
     * @return formatted value
     */
    String format(T value);
}
