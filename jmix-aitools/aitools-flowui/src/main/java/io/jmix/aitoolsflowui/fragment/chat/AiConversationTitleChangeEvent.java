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

package io.jmix.aitoolsflowui.fragment.chat;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import io.jmix.core.annotation.Experimental;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Fired by an {@link AiConversationChat} whenever it shows a conversation title: when a conversation is bound or
 * reloaded, when it gets its automatic title, and when it is renamed. The title may be the same as before.
 */
@Experimental
@NullMarked
public class AiConversationTitleChangeEvent extends ComponentEvent<Component> {

    @Nullable
    protected final String title;

    public AiConversationTitleChangeEvent(Component source, @Nullable String title) {
        super(source, false);

        this.title = title;
    }

    /**
     * Returns the new conversation title.
     *
     * @return the new title, or {@code null} when no conversation is bound or it has no title
     */
    @Nullable
    public String getTitle() {
        return title;
    }
}
