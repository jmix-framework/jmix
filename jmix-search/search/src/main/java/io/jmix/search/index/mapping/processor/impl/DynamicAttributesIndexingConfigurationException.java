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

package io.jmix.search.index.mapping.processor.impl;

import java.util.List;

public class DynamicAttributesIndexingConfigurationException extends Exception {
    private static final String MESSAGE = "Index configuration can't be parsed. " +
            "The 'included%s' parameter value '%s' conflicts with the 'excluded%s' parameter value '%s'.";
    public DynamicAttributesIndexingConfigurationException(
            ConflictType conflictType,
            List<String> includedParts,
            List<String> excludedParts) {

        super(String.format(
                MESSAGE,
                conflictType.getMessagePart(),
                String.join(", ", includedParts),
                conflictType.getMessagePart(),
                String.join(", ", excludedParts)
        ));
    }

    public enum ConflictType{
        CATEGORIES("Categories"), FIELDS("Fields");

        ConflictType(String messagePart) {
            this.messagePart = messagePart;
        }
        private final String messagePart;

        public String getMessagePart() {
            return messagePart;
        }
    }
}
