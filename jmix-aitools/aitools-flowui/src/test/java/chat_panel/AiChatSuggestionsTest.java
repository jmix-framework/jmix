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

package chat_panel;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import io.jmix.aitoolsflowui.fragment.chat.component.AiChatSuggestions;
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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The suggestions component shows a button per item and reports the one the user picks.
 */
@UiTest(viewBasePackages = {"io.jmix.aitoolsflowui.view", "test_support.view"})
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class AiChatSuggestionsTest {

    static final String ITEM_CN = "jmix-ai-chat-suggestion";

    @Autowired
    UiComponents uiComponents;

    @Test
    void items_set_rendersButtonPerItem() {
        AiChatSuggestions suggestions = uiComponents.create(AiChatSuggestions.class);

        suggestions.setItems(List.of("Summarize", "Compare"));

        List<String> texts = find(suggestions, ITEM_CN, Button.class).stream()
                .map(Button::getText)
                .toList();
        assertEquals(List.of("Summarize", "Compare"), texts);
    }

    @Test
    void items_setSameAgain_keepsTheButtons() {
        AiChatSuggestions suggestions = uiComponents.create(AiChatSuggestions.class);
        suggestions.setItems(List.of("Summarize", "Compare"));
        List<Button> shown = find(suggestions, ITEM_CN, Button.class);

        suggestions.setItems(new ArrayList<>(List.of("Summarize", "Compare")));

        List<Button> afterwards = find(suggestions, ITEM_CN, Button.class);
        assertEquals(2, afterwards.size());
        assertSame(shown.get(0), afterwards.get(0));
        assertSame(shown.get(1), afterwards.get(1));
    }

    @Test
    void items_setOther_replacesTheButtons() {
        AiChatSuggestions suggestions = uiComponents.create(AiChatSuggestions.class);
        suggestions.setItems(List.of("Summarize", "Compare"));
        Button first = find(suggestions, ITEM_CN, Button.class).get(0);

        suggestions.setItems(List.of("Summarize", "Trend"));

        List<Button> afterwards = find(suggestions, ITEM_CN, Button.class);
        assertEquals(List.of("Summarize", "Trend"), afterwards.stream().map(Button::getText).toList());
        assertNotSame(first, afterwards.get(0));
    }

    @Test
    void items_click_firesItemClickWithItem() {
        AiChatSuggestions suggestions = uiComponents.create(AiChatSuggestions.class);
        List<String> picked = new ArrayList<>();
        suggestions.addItemClickListener(event -> picked.add(event.getItem()));
        suggestions.setItems(List.of("Summarize", "Compare"));

        find(suggestions, ITEM_CN, Button.class).get(1).click();

        assertEquals(List.of("Compare"), picked);
    }

    @Test
    void items_setEmpty_leavesNoButtons() {
        AiChatSuggestions suggestions = uiComponents.create(AiChatSuggestions.class);
        suggestions.setItems(List.of("Summarize"));

        suggestions.setItems(List.of());

        assertTrue(find(suggestions, ITEM_CN, Button.class).isEmpty());
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
