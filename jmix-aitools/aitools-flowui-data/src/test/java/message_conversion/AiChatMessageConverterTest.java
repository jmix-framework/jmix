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

package message_conversion;

import io.jmix.aitoolsflowui.model.AiChatMessageType;
import io.jmix.aitoolsflowuidata.converter.AiChatMessageConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AiChatMessageConverterTest {

    private final AiChatMessageConverter converter = new AiChatMessageConverter();

    @Test
    void unknownType_fallsBackToSystem() {
        // getType() returns null for a stored value that is not one of the known types; the
        // conversion must not fail with an NPE so a conversation with such a message still loads.
        assertEquals(AiChatMessageType.SYSTEM, converter.convertToModelType(null));
    }
}
