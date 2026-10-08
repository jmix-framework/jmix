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
package io.jmix.reports.yarg.formatters;

import io.jmix.reports.yarg.formatters.impl.streaming.StreamingBandFeed;
import org.jspecify.annotations.NullMarked;

import java.util.Set;

/**
 * A {@link ReportFormatter} able to render one band from a streaming row feed. The render is split in
 * two phases so that the data source connection is held only while rows are consumed:
 * {@link #consumeData()} runs inside the streaming loader's callback, {@link #completeRendering()}
 * writes the result after the cursor and its connection are released.
 */
@NullMarked
public interface StreamingReportFormatter extends ReportFormatter {

    /**
     * Injects the feed that supplies the streaming band's rows. Must be set before {@link #consumeData()}.
     */
    void setStreamingBandFeed(StreamingBandFeed streamingBandFeed);

    /**
     * Supplies the full set of band names of the report definition, so the template parser can
     * distinguish band named ranges from service/user defined names even for bands that produced
     * no data. Optional — implementations may fall back to names derivable from the band data tree,
     * hence the no-op default.
     */
    default void setReportBandNames(Set<String> reportBandNames) {
    }

    /**
     * First phase: consumes band data (including the streaming feed) and builds the result document.
     * Must be called inside the streaming loader's callback, while the feed's cursor is alive.
     */
    void consumeData();

    /**
     * Second phase: writes or converts the built document to the output stream and releases resources.
     * Runs after the loader callback returns, so it must not touch the feed.
     */
    void completeRendering();

    /**
     * Releases the partially built result (e.g. SXSSF spool files) without writing it. Safe to call at
     * any phase and more than once; used by the engine when the streaming loader fails after the render
     * callback succeeded, so temp files do not outlive the report run. Implementations that hold no
     * intermediate resources may make this a no-op, but must still implement it so the release contract
     * is explicit.
     */
    void discard();
}
