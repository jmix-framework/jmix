/*
 * Copyright 2025 Haulmont.
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

package io.jmix.reports.yarg.formatters.impl.docx;

import org.docx4j.wml.Text;

/**
 * Processes texts that have a multiline text as value.
 */
public interface MultilineTextProcessor {

    /**
     * Replaces the specified text with the multiline text content if its value is a multiline string.
     *
     * @param text a text with value
     */
    void process(Text text);


    /**
     * Replaces the text from the specified wrapper with the multiline text content if its value is a multiline string.
     *
     * @param wrapper a wrapper for the text with a value
     */
    void process(TextWrapper wrapper);
}
