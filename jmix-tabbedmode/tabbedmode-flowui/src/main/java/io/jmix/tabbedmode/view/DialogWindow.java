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

package io.jmix.tabbedmode.view;

import com.vaadin.flow.component.button.Button;
import io.jmix.flowui.view.AbstractDialogWindow;
import io.jmix.flowui.view.View;
import org.springframework.lang.Nullable;

/**
 * Wrapper class representing a {@link View} opened as a dialog window.
 *
 * @param <V> a view type
 */
public class DialogWindow<V extends View<?>> extends AbstractDialogWindow<V> {

    public DialogWindow(V view) {
        super(view);
    }

    @Nullable
    @Override
    protected Button createHeaderCloseButton() {
        return TabbedModeViewUtils.isCloseable(view)
                ? super.createHeaderCloseButton()
                : null;
    }
}
