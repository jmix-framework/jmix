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
package io.jmix.reports.yarg.formatters.impl.streaming;

import io.jmix.reports.yarg.structure.BandData;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * One-shot supply of the streaming ("hot") band's rows for the streaming XLSX formatter. Each source
 * row map is wrapped into a transient {@link BandData} parented by the root band but NEVER attached to
 * it, so rendered rows become garbage as soon as the formatter moves on. When
 * {@code putEmptyRowIfNoDataSelected} is on, an empty source yields exactly one empty row, mirroring
 * the extractor's behavior.
 */
public class StreamingBandFeed {

    protected final String bandName;
    protected final Iterator<Map<String, Object>> rows;
    protected final BandData rootBand;

    protected final boolean putEmptyRowIfNoDataSelected;
    protected boolean consumed = false;
    /** A detached plain-map copy of the first emitted row, retained (O(1) memory) for output file name resolution after the render. */
    @Nullable
    protected BandData firstRow;

    public StreamingBandFeed(String bandName, Iterator<Map<String, Object>> rows, BandData rootBand) {
        this(bandName, rows, rootBand, true);
    }

    public StreamingBandFeed(String bandName, Iterator<Map<String, Object>> rows, BandData rootBand,
                             boolean putEmptyRowIfNoDataSelected) {
        this.bandName = bandName;
        this.rows = rows;
        this.rootBand = rootBand;
        this.putEmptyRowIfNoDataSelected = putEmptyRowIfNoDataSelected;
    }

    public String getBandName() {
        return bandName;
    }

    public Iterator<BandData> iterator() {
        if (consumed) {
            throw new IllegalStateException("Streaming band feed for [" + bandName + "] is already consumed");
        }

        consumed = true;

        return new Iterator<>() {
            private boolean anyRowSeen = false;
            private boolean emptyRowEmitted = false;

            @Override
            public boolean hasNext() {
                if (rows.hasNext()) {
                    return true;
                }
                return putEmptyRowIfNoDataSelected && !anyRowSeen && !emptyRowEmitted;
            }

            @Override
            public BandData next() {
                if (rows.hasNext()) {
                    anyRowSeen = true;
                    return wrap(rows.next());
                }
                emptyRowEmitted = true;
                return wrap(new HashMap<>());
            }
        };
    }

    /** The first emitted row, or {@code null} when nothing was emitted or the feed was not consumed yet. */
    @Nullable
    public BandData getFirstRow() {
        return firstRow;
    }

    protected BandData wrap(Map<String, Object> row) {
        BandData band = new BandData(bandName, rootBand);
        band.setData(row);
        if (firstRow == null) {
            // Copied eagerly, while the loader's transaction is still open: for JPQL entity rows the
            // copy materializes attribute values from the managed entity, so output-file-name patterns
            // referencing this band keep working after the connection is released. The rendered band
            // keeps the original map to preserve lazy path-alias semantics.
            BandData copy = new BandData(bandName, rootBand);
            copy.setData(new HashMap<>(row));
            firstRow = copy;
        }
        return band;
    }
}
