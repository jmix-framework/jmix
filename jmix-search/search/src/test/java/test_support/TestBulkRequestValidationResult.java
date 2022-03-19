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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TestBulkRequestValidationResult {

    private final List<Failure> failures = new ArrayList<>();

    public void addFailure(Failure failure) {
        failures.add(failure);
    }

    public boolean hasFailures() {
        return !failures.isEmpty();
    }

    @Override
    public String toString() {
        return failures.stream()
                .map(Failure::getMessage)
                .collect(Collectors.joining("\n"));
    }

    public static class Failure {
        private final String message;

        private Failure(String message) {
            this.message = message;
        }

        public static Failure create(String message) {
            return new Failure(message);
        }

        public String getMessage() {
            return message;
        }
    }
}
