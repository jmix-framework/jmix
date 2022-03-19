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

package com.haulmont.cuba.gui.sys;

import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.compatibility.LegacyFragmentAdapter;
import io.jmix.core.DevelopmentException;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.component.Fragment;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.sys.FragmentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

import static org.apache.commons.lang3.reflect.ConstructorUtils.invokeConstructor;

public class CubaFragmentHelper extends FragmentHelper {

    private static final Logger log = LoggerFactory.getLogger(FragmentHelper.class);

    @Override
    public ScreenFragment createController(WindowInfo windowInfo, Fragment fragment) {
        Class screenClass = windowInfo.getControllerClass();

        if (AbstractWindow.class.isAssignableFrom(screenClass)) {
            AbstractWindow legacyScreen;
            try {
                legacyScreen = (AbstractWindow) invokeConstructor(screenClass);
            } catch (NoSuchMethodException | IllegalAccessException
                    | InvocationTargetException | InstantiationException e) {
                throw new DevelopmentException("Unable to create " + screenClass);
            }
            LegacyFragmentAdapter adapter = new LegacyFragmentAdapter(legacyScreen);

            legacyScreen.setFrame(fragment);
            adapter.setWrappedFrame(fragment);

            log.warn(
                    "Fragment class '{}' should not be inherited from AbstractWindow. " +
                            "It may cause problems with controller life cycle. " +
                            "Fragment controllers should inherit ScreenFragment.",
                    screenClass.getSimpleName());

            return adapter;
        }

        return super.createController(windowInfo, fragment);
    }
}
