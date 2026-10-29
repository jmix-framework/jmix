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

import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.service.AiConversationService;
import io.jmix.aitoolsflowuidata.converter.AiChatMessageConverter;
import io.jmix.aitoolsflowuidata.entity.AiChatMessageEntity;
import io.jmix.aitoolsflowuidata.entity.AiChatMessageEntityType;
import io.jmix.core.security.SystemAuthenticator;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AiToolsFlowuiDataTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AiToolsFlowuiDataTestConfiguration.class)
@TestPropertySource(properties = {
        "eclipselink.ddl-generation=create-tables",
        "jmix.core.work-dir=build/test-home/work"})
class AiChatMessageConverterKeepInContextTest {

    @Autowired
    AiChatMessageConverter converter;
    @Autowired
    AiConversationService conversationService;
    @Autowired
    SystemAuthenticator systemAuthenticator;

    @Test
    void convertToModel_keptEntity_mapsFlag() {
        AiChatMessage model = convert(true);

        assertTrue(model.getKeepInContext());
    }

    @Test
    void convertToModel_defaultEntity_mapsNotKept() {
        AiChatMessage model = convert(null);

        assertFalse(model.getKeepInContext());
    }

    private AiChatMessage convert(@Nullable Boolean keepInContext) {
        AiConversation conversation = systemAuthenticator.withSystem(() -> conversationService.create());
        AiChatMessageEntity entity = new AiChatMessageEntity();
        entity.setType(AiChatMessageEntityType.USER);
        entity.setContent("Hi");
        if (keepInContext != null) {
            entity.setKeepInContext(keepInContext);
        }
        return converter.convertToModel(entity, conversation);
    }
}
