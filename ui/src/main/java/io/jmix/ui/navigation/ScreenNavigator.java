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

package io.jmix.ui.navigation;

import io.jmix.ui.AppUI;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

/**
 * A facade bean that is intended for screen navigation using all available {@link NavigationHandler} beans.
 */
@Component("ui_ScreenNavigator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ScreenNavigator {

    @Autowired
    protected List<NavigationHandler> navigationHandlers;

    protected AppUI ui;

    @SuppressWarnings("unused")
    public ScreenNavigator(AppUI ui) {
        this.ui = ui;
    }

    public void handleScreenNavigation(NavigationState requestedState) {
        for (NavigationHandler handler : navigationHandlers)
            if (handler.doHandle(requestedState, ui))
                return;
    }
}
