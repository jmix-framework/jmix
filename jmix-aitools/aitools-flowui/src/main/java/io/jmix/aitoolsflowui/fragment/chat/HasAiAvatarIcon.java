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
import com.vaadin.flow.function.SerializableSupplier;
import io.jmix.core.annotation.Experimental;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * An {@link AiChatPanel} that shows an avatar next to assistant messages and lets the host replace its icon.
 */
@Experimental
@NullMarked
public interface HasAiAvatarIcon {

    /**
     * Sets a supplier of the assistant avatar icon. The supplier must return a fresh component on every call.
     *
     * @param avatarIconSupplier supplier of the icon, or {@code null} to use the default
     */
    void setAiAvatarIconSupplier(@Nullable SerializableSupplier<Component> avatarIconSupplier);
}
