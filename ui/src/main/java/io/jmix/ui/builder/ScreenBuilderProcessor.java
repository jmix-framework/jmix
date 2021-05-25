/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.builder;

import io.jmix.core.annotation.Internal;
import io.jmix.ui.Screens;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.Screen;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

import static io.jmix.ui.screen.UiControllerUtils.getScreenContext;

@Internal
@Component("ui_ScreenBuilderProcessor")
public class ScreenBuilderProcessor {

    @SuppressWarnings("unchecked")
    public <S extends Screen> S buildScreen(ScreenBuilder builder) {
        FrameOwner origin = builder.getOrigin();
        Screens screens = getScreenContext(origin).getScreens();

        Screen screen;

        if (builder instanceof ScreenClassBuilder) {
            ScreenClassBuilder screenClassBuilder = (ScreenClassBuilder) builder;

            Class screenClass = screenClassBuilder.getScreenClass();
            if (screenClass == null) {
                throw new IllegalArgumentException("Screen class is not set");
            }

            screen = screens.create(screenClass, builder.getOpenMode(), builder.getOptions());

            Consumer<AfterScreenShowEvent> afterShowListener = screenClassBuilder.getAfterShowListener();
            if (afterShowListener != null) {
                screen.addAfterShowListener(new AfterShowListenerAdapter(afterShowListener));
            }

            Consumer<AfterScreenCloseEvent> afterCloseListener = screenClassBuilder.getAfterCloseListener();
            if (afterCloseListener != null) {
                screen.addAfterCloseListener(new AfterCloseListenerAdapter(afterCloseListener));
            }
        } else {
            if (builder.getScreenId() == null) {
                throw new IllegalArgumentException("Screen id is not set");
            }

            screen = screens.create(builder.getScreenId(), builder.getOpenMode(), builder.getOptions());
        }

        return (S) screen;
    }
}