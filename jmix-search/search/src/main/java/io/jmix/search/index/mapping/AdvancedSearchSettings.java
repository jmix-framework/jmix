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

public class AdvancedSearchSettings { //todo naming

    protected final boolean enabled;
    protected final int edgeNGramMin;
    protected final int edgeNGramMax;

    protected AdvancedSearchSettings(Builder builder) {
        this.enabled = builder.enabled;
        this.edgeNGramMin = builder.edgeNGramMin;
        this.edgeNGramMax = builder.edgeNGramMax;
    }

    public static Builder builder() {
        return new Builder();
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

    public static class Builder {
        private boolean enabled = true;
        private int edgeNGramMin = 3;
        private int edgeNGramMax = 8;

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

        public AdvancedSearchSettings build() {
            return new AdvancedSearchSettings(this);
        }
    }
}
