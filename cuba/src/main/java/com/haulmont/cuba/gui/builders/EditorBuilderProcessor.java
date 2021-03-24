/*
 * Copyright 2021 Haulmont.
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

package com.haulmont.cuba.gui.builders;

import com.haulmont.cuba.gui.WindowParams;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.builder.EditorBuilder;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.MapScreenOptions;
import io.jmix.ui.screen.ScreenOptions;

import java.util.HashMap;

/**
 * @deprecated Use {@link io.jmix.ui.builder.EditorBuilderProcessor} instead.
 */
@Deprecated
public class EditorBuilderProcessor extends io.jmix.ui.builder.EditorBuilderProcessor {

    @Override
    protected <E> ScreenOptions getOptionsForScreen(String editorScreenId, E entity, EditorBuilder<E> builder) {
        ScreenOptions options = super.getOptionsForScreen(editorScreenId, entity, builder);

        WindowInfo windowInfo = windowConfig.getWindowInfo(editorScreenId);
        if (LegacyFrame.class.isAssignableFrom(windowInfo.getControllerClass())
                && options == FrameOwner.NO_OPTIONS) {
            HashMap<String, Object> paramsMap = new HashMap<>();
            paramsMap.put(WindowParams.ITEM.name(), entity);
            options = new MapScreenOptions(paramsMap);
        }

        return options;
    }
}
