/*
 * Copyright 2024 Haulmont.
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

package io.jmix.search.index.mapping;

import java.util.List;

/**
 * Class contains data to build mapping and analysis within Extended search functionality.
 *
 * @see io.jmix.search.index.annotation.ExtendedSearch
 */
public class ExtendedSearchSettings {

    protected final boolean enabled;
    protected final int edgeNGramMin;
    protected final int edgeNGramMax;

    protected final String tokenizer;
    protected final String prefixFilter;
    protected final String prefixAnalyzer;
    protected final String prefixSearchAnalyzer;
    protected final List<String> additionalFilters;

    protected ExtendedSearchSettings(Builder builder) {
        this.enabled = builder.enabled;
        this.edgeNGramMin = builder.edgeNGramMin;
        this.edgeNGramMax = builder.edgeNGramMax;
        this.tokenizer = builder.tokenizer;
        this.prefixAnalyzer = builder.prefixAnalyzer;
        this.prefixFilter = builder.prefixFilter;
        this.prefixSearchAnalyzer = builder.prefixSearchAnalyzer;
        this.additionalFilters = List.of(builder.additionalFilters);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ExtendedSearchSettings empty() {
        return builder().setEnabled(false).build();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getEdgeNGramMin() {
        return edgeNGramMin;
    }

    public int getEdgeNGramMax() {
        return edgeNGramMax;
    }

    public String getTokenizer() {
        return tokenizer;
    }

    public String getPrefixFilter() {
        return prefixFilter;
    }

    public String getPrefixAnalyzer() {
        return prefixAnalyzer;
    }

    public String getPrefixSearchAnalyzer() {
        return prefixSearchAnalyzer;
    }

    public List<String> getAdditionalFilters() {
        return additionalFilters;
    }

    public static class Builder {
        private boolean enabled = true;
        private int edgeNGramMin = 3;
        private int edgeNGramMax = 8;
        private String tokenizer = "whitespace";
        private String[] additionalFilters = {};
        private String prefixFilter = "jmix_prefix_filter";
        private String prefixAnalyzer = "jmix_prefix_analyzer";
        private String prefixSearchAnalyzer = "jmix_prefix_search_analyzer";

        protected Builder() {
        }

        public Builder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder setEdgeNGramMin(int edgeNGramMin) {
            this.edgeNGramMin = edgeNGramMin;
            return this;
        }

        public Builder setEdgeNGramMax(int edgeNGramMax) {
            this.edgeNGramMax = edgeNGramMax;
            return this;
        }

        public Builder setTokenizer(String tokenizer) {
            this.tokenizer = tokenizer;
            return this;
        }

        public Builder setAdditionalFilters(String[] additionalFilters) {
            this.additionalFilters = additionalFilters;
            return this;
        }

        public Builder setPrefixFilter(String prefixFilter) {
            this.prefixFilter = prefixFilter;
            return this;
        }

        public Builder setPrefixAnalyzer(String prefixAnalyzer) {
            this.prefixAnalyzer = prefixAnalyzer;
            return this;
        }

        public Builder setPrefixSearchAnalyzer(String prefixSearchAnalyzer) {
            this.prefixSearchAnalyzer = prefixSearchAnalyzer;
            return this;
        }

        public ExtendedSearchSettings build() {
            return new ExtendedSearchSettings(this);
        }
    }

    @Override
    public String toString() {
        return "ExtendedSearchSettings{" +
                "enabled=" + enabled +
                ", edgeNGramMin=" + edgeNGramMin +
                ", edgeNGramMax=" + edgeNGramMax +
                ", tokenizer='" + tokenizer + '\'' +
                ", prefixFilter='" + prefixFilter + '\'' +
                ", prefixAnalyzer='" + prefixAnalyzer + '\'' +
                ", prefixSearchAnalyzer='" + prefixSearchAnalyzer + '\'' +
                ", additionalFilters=" + additionalFilters +
                '}';
    }
}
