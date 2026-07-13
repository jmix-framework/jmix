/*
 * Copyright 2026 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jmix.reports.yarg.reporting;

import io.jmix.reports.yarg.loaders.StreamingReportDataLoader;
import io.jmix.reports.yarg.loaders.factory.ReportLoaderFactory;
import io.jmix.reports.yarg.structure.BandOrientation;
import io.jmix.reports.yarg.structure.ReportBand;
import io.jmix.reports.yarg.structure.ReportQuery;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates the band structure of a report that uses a streaming band against the restrictions of the
 * streaming XLSX engine. Shared by the rendering engine ({@link Reporting}) and the report designer UI,
 * so an unsupported structure is rejected identically at save time and at run time.
 */
@NullMarked
public class StreamingReportValidator {

    public enum ViolationType {
        MULTIPLE_STREAMING_BANDS("a report can have only one streaming band"),
        NOT_FIRST_LEVEL("streaming band must be a first-level band (a direct child of the root band)"),
        NOT_HORIZONTAL("streaming band must be horizontal"),
        HAS_CHILDREN("streaming band must not have child bands"),
        NOT_SINGLE_QUERY("streaming band must have exactly one dataset"),
        LOADER_NOT_STREAMING("streaming band must use a loader that supports streaming (sql or jpql)"),
        NON_HORIZONTAL_BAND_IN_REPORT(
                "report with a streaming band must not contain vertical or cross bands");

        private final String description;

        ViolationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public record Violation(ViolationType type, String bandName) {

        public String describe() {
            return type.getDescription() + " [" + bandName + "]";
        }
    }

    /**
     * Returns the violations of the streaming engine's restrictions, or an empty list when the report
     * either has no streaming band or satisfies all of them.
     */
    public List<Violation> validate(ReportBand rootBand, ReportLoaderFactory loaderFactory) {
        List<Violation> violations = new ArrayList<>();
        List<ReportBand> streamingBands = new ArrayList<>();
        collectStreamingBands(rootBand, streamingBands);

        if (streamingBands.isEmpty()) {
            return violations;
        }

        if (streamingBands.size() > 1) {
            List<String> names = streamingBands.stream().map(ReportBand::getName).toList();
            violations.add(new Violation(ViolationType.MULTIPLE_STREAMING_BANDS, String.join(", ", names)));
        }

        for (ReportBand band : streamingBands) {
            validateStreamingBand(band, loaderFactory, violations);
        }
        collectNonHorizontalBands(rootBand, violations);
        return violations;
    }

    protected void collectStreamingBands(ReportBand band, List<ReportBand> streamingBands) {
        if (band.isStreaming()) {
            streamingBands.add(band);
        }

        for (ReportBand child : band.getChildren()) {
            collectStreamingBands(child, streamingBands);
        }
    }

    protected void validateStreamingBand(ReportBand band, ReportLoaderFactory loaderFactory,
                                         List<Violation> violations) {
        if (band.getParent() == null || band.getParent().getParent() != null) {
            violations.add(new Violation(ViolationType.NOT_FIRST_LEVEL, band.getName()));
        }

        if (band.getBandOrientation() != BandOrientation.HORIZONTAL) {
            violations.add(new Violation(ViolationType.NOT_HORIZONTAL, band.getName()));
        }

        if (!band.getChildren().isEmpty()) {
            violations.add(new Violation(ViolationType.HAS_CHILDREN, band.getName()));
        }

        List<ReportQuery> queries = band.getReportQueries();
        if (queries == null || queries.size() != 1) {
            violations.add(new Violation(ViolationType.NOT_SINGLE_QUERY, band.getName()));
        } else if (!isStreamingCapable(queries.get(0).getLoaderType(), loaderFactory)) {
            violations.add(new Violation(ViolationType.LOADER_NOT_STREAMING, band.getName()));
        }
    }

    protected boolean isStreamingCapable(@Nullable String loaderType, ReportLoaderFactory loaderFactory) {
        if (loaderType == null) {
            return false;
        }
        try {
            return loaderFactory.createDataLoader(loaderType) instanceof StreamingReportDataLoader;
        } catch (RuntimeException e) {
            // Unknown loader type: reported as a non-streaming loader; the dedicated
            // dataset-type validation owns the "unknown type" message.
            return false;
        }
    }

    protected void collectNonHorizontalBands(ReportBand band, List<Violation> violations) {
        BandOrientation orientation = band.getBandOrientation();
        if (orientation == BandOrientation.VERTICAL || orientation == BandOrientation.CROSS) {
            violations.add(new Violation(ViolationType.NON_HORIZONTAL_BAND_IN_REPORT, band.getName()));
        }

        for (ReportBand child : band.getChildren()) {
            collectNonHorizontalBands(child, violations);
        }
    }
}
