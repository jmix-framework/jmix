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

package io.jmix.datatools.datamodel.engine.plantuml.impl;

import io.jmix.datatools.datamodel.engine.plantuml.PlantUmlEncoder;
import org.springframework.lang.Nullable;

import java.nio.charset.StandardCharsets;

public class PlantUmlEncoderImpl implements PlantUmlEncoder {

    protected static byte[] encodingAlphabet = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z',
            '-', '_',
            '='  // index 64 - is a padding
    };
    protected static byte padding = encodingAlphabet[64];

    @Override
    public String encode(@Nullable byte[] compressedData) {
        if (compressedData == null || compressedData.length == 0) {
            return "";
        }

        // length is always equals 4
        byte[] encodedBytes = new byte[((compressedData.length + 2) / 3) * 4];
        int outputPos = 0;

        int i = 0;
        int count = compressedData.length;

        // Main cycle: by 3 bytes for 4 symbols
        while (i + 3 <= count) {
            int b1 = compressedData[i] & 0xFF;
            int b2 = compressedData[i + 1] & 0xFF;
            int b3 = compressedData[i + 2] & 0xFF;

            int one   = b1 >> 2;
            int two   = ((b1 & 0x03) << 4) | (b2 >> 4);
            int three = ((b2 & 0x0F) << 2) | (b3 >> 6);
            int four  = b3 & 0x3F;

            encodedBytes[outputPos++] = encodingAlphabet[one];
            encodedBytes[outputPos++] = encodingAlphabet[two];
            encodedBytes[outputPos++] = encodingAlphabet[three];
            encodedBytes[outputPos++] = encodingAlphabet[four];

            i += 3;
        }

        // Remains: 2 bytes -> 3 symbols + 1 padding
        if (i + 2 == count) {
            int b1 = compressedData[i] & 0xFF;
            int b2 = compressedData[i + 1] & 0xFF;

            int one   = b1 >> 2;
            int two   = ((b1 & 0x03) << 4) | (b2 >> 4);
            int three = (b2 & 0x0F) << 2;

            encodedBytes[outputPos++] = encodingAlphabet[one];
            encodedBytes[outputPos++] = encodingAlphabet[two];
            encodedBytes[outputPos++] = encodingAlphabet[three];
            encodedBytes[outputPos++] = padding;
        }
        // Remains: 1 byte -> 2 symbols + 2 paddings
        else if (i + 1 == count) {
            int b1 = compressedData[i] & 0xFF;

            int one = b1 >> 2;
            int two = (b1 & 0x03) << 4;

            encodedBytes[outputPos++] = encodingAlphabet[one];
            encodedBytes[outputPos++] = encodingAlphabet[two];
            encodedBytes[outputPos++] = padding;
            encodedBytes[outputPos++] = padding;
        }

        // If equal to 3 - no padding needed
        return new String(encodedBytes, 0, outputPos, StandardCharsets.UTF_8);
    }
}
