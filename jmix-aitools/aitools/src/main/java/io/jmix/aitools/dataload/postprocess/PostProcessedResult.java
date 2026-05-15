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

package io.jmix.aitools.dataload.postprocess;

import io.jmix.aitools.dataload.generation.GeneratedJpqlResult;
import org.jspecify.annotations.Nullable;

/**
 * Post-processed JPQL result ready for validation, execution setup, or both.
 * <p>
 * This type wraps a {@link GeneratedJpqlResult} after the post-processing stage has applied
 * normalization rules such as pagination extraction or future parameter resolution logic.
 * <p>
 * In contrast to {@link GeneratedJpqlResult}, this type represents the prepared form that
 * callers should prefer when they need execution-oriented values such as {@code maxResults}
 * and {@code firstResult}.
 */
public class PostProcessedResult {

    protected GeneratedJpqlResult generatedJpqlResult;
    protected Integer maxResults;
    protected Integer firstResult;

    public PostProcessedResult(GeneratedJpqlResult generatedJpqlResult,
                               @Nullable Integer maxResults,
                               @Nullable Integer firstResult) {
        this.generatedJpqlResult = generatedJpqlResult;
        this.maxResults = maxResults;
        this.firstResult = firstResult;
    }

    /**
     * Returns the normalized generated result after post-processing.
     */
    public GeneratedJpqlResult getGeneratedJpqlResult() {
        return generatedJpqlResult;
    }

    /**
     * Returns the effective maximum number of rows after post-processing.
     */
    @Nullable
    public Integer getMaxResults() {
        return maxResults;
    }

    /**
     * Returns the effective row offset after post-processing.
     */
    @Nullable
    public Integer getFirstResult() {
        return firstResult;
    }

    @Override
    public String toString() {
        return "PreparedJpqlResult{" +
                "generatedJpqlResult=" + generatedJpqlResult +
                ", maxResults=" + maxResults +
                ", firstResult=" + firstResult +
                '}';
    }
}
