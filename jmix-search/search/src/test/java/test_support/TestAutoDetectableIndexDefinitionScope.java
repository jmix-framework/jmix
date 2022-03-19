/*
 * Copyright 2021 Haulmont.
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

package test_support;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestAutoDetectableIndexDefinitionScope {

    protected List<Class<?>> classes;
    protected List<String> packages;

    private TestAutoDetectableIndexDefinitionScope(Builder builder) {
        this.classes = builder.classes;
        this.packages = builder.packages;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<Class<?>> getClasses() {
        return classes;
    }

    public List<String> getPackages() {
        return packages;
    }

    public static class Builder {

        private List<Class<?>> classes = Collections.emptyList();
        private List<String> packages = Collections.emptyList();

        public Builder packages(String... packages) {
            this.packages = Arrays.asList(packages);
            return this;
        }

        public Builder classes(Class<?>... classes) {
            this.classes = Arrays.asList(classes);
            return this;
        }

        public TestAutoDetectableIndexDefinitionScope build() {
            return new TestAutoDetectableIndexDefinitionScope(this);
        }
    }
}
