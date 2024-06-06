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

package io.jmix.search.index;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class IndexResult {

    protected final int size;
    protected final List<Failure> failures;

    public IndexResult(int size, List<Failure> failures) {
        this.size = size;
        this.failures = failures;
    }

    public Collection<Failure> getFailures() {
        return Collections.unmodifiableList(failures);
    }

    public Collection<String> getFailedIndexIds() {
        return failures.stream()
                .map(Failure::getId)
                .collect(Collectors.toList());
    }

    public int getTotalSize() {
        return size;
    }

    public int getFailuresSize() {
        return failures.size();
    }

    public boolean hasFailures() {
        return !failures.isEmpty();
    }

    public static class Failure {

        private final String id;
        private final String index;
        private final String cause;

        public Failure(String id, String index, String cause) {
            this.id = id;
            this.index = index;
            this.cause = cause;
        }

        public String getId() {
            return id;
        }

        public String getIndex() {
            return index;
        }

        public String getCause() {
            return cause;
        }
    }
}
