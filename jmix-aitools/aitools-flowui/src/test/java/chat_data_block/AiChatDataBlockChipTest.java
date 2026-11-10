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

package chat_data_block;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import io.jmix.aitoolsflowui.fragment.chat.datablock.AiChatDataBlockChip;
import io.jmix.core.Messages;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.AiToolsFlowuiTestConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The data block chip shows what it is given and reports the user's actions through its handler.
 */
@UiTest(viewBasePackages = {"io.jmix.aitoolsflowui.view", "test_support.view"})
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class AiChatDataBlockChipTest {

    static final String CAPTION_CN = "jmix-ai-chat-data-block-caption";
    static final String WARNING_CN = "jmix-ai-chat-data-block-warning";
    static final String REMOVE_CN = "jmix-ai-chat-data-block-remove";

    @Autowired
    UiComponents uiComponents;
    @Autowired
    Messages messages;

    @Test
    void chip_caption_isShown() {
        AiChatDataBlockChip chip = uiComponents.create(AiChatDataBlockChip.class);

        chip.setCaption("Orders · 12 rows");

        assertEquals("Orders · 12 rows", findOne(chip, CAPTION_CN, Span.class).getText());
    }

    @Test
    void chip_withoutWarning_hidesWarningLine() {
        AiChatDataBlockChip chip = uiComponents.create(AiChatDataBlockChip.class);

        chip.setWarning(null);

        assertFalse(findOne(chip, WARNING_CN, Span.class).isVisible());
    }

    @Test
    void chip_withWarning_showsWarningText() {
        AiChatDataBlockChip chip = uiComponents.create(AiChatDataBlockChip.class);

        chip.setWarning("Memory is full");

        Span warning = findOne(chip, WARNING_CN, Span.class);
        assertTrue(warning.isVisible());
        assertEquals("Memory is full", warning.getText());
    }

    @Test
    void chip_warningCleared_hidesWarningLine() {
        AiChatDataBlockChip chip = uiComponents.create(AiChatDataBlockChip.class);
        chip.setWarning("Memory is full");

        chip.setWarning(null);

        assertFalse(findOne(chip, WARNING_CN, Span.class).isVisible());
    }

    @Test
    void chip_removeClick_callsHandler() {
        AiChatDataBlockChip chip = uiComponents.create(AiChatDataBlockChip.class);
        List<String> calls = new ArrayList<>();
        chip.setRemoveHandler(() -> calls.add("removed"));

        findOne(chip, REMOVE_CN, Button.class).click();

        assertEquals(List.of("removed"), calls);
    }

    @Test
    void chip_removeButton_isLabelled() {
        AiChatDataBlockChip chip = uiComponents.create(AiChatDataBlockChip.class);

        Button remove = findOne(chip, REMOVE_CN, Button.class);

        String expected = messages.getMessage("askAiAboutData.remove");
        assertEquals(expected, remove.getAriaLabel().orElse(null));
        assertEquals(expected, remove.getTooltip().getText());
    }

    static <T extends Component> T findOne(Component root, String className, Class<T> type) {
        List<T> found = find(root, className, type);
        assertEquals(1, found.size(), "components with class " + className);
        return found.get(0);
    }

    static <T extends Component> List<T> find(Component root, String className, Class<T> type) {
        return descendants(root)
                .filter(type::isInstance)
                .filter(component -> component.getElement().getClassList().contains(className))
                .map(type::cast)
                .toList();
    }

    static Stream<Component> descendants(Component component) {
        return component.getChildren()
                .flatMap(child -> Stream.concat(Stream.of(child), descendants(child)));
    }
}
