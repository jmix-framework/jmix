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

package io.jmix.datatools.datamodel.engine;

/**
 * PlantUML encoder that adds support for encoding diagram description, compressed by deflate algorithm,
 * in a Base64-like format for further sending as a URL parameter
 */
public interface PlantUMLEncoder {

    /**
     * Provides encoding in a base64-like format
     * @param compressedData a base64-encoded, compressed by deflate algorithm, string description of the chart
     * to be re-encoded
     * @return a string description of the chart, encoded in a base64-like format
     */
    String encode(byte[] compressedData);
}
