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
package io.jmix.reports.yarg.loaders;

import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportQuery;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

/**
 * A data loader capable of streaming rows one at a time from a live cursor. The loader owns the
 * connection/transaction lifecycle: it opens the cursor, hands a row iterator to {@code work}, and
 * releases all resources when {@code work} returns (normally or exceptionally). The iterator is valid
 * ONLY inside the callback. Row maps are mutable, like {@link ReportDataLoader#loadData}'s.
 */
public interface StreamingReportDataLoader {

    <T> T loadDataStreaming(ReportQuery reportQuery, @Nullable BandData parentBand,
                            Map<String, Object> params, Function<Iterator<Map<String, Object>>, T> work);
}
