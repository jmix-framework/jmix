/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.gui.config;

import com.google.common.base.Strings;
import com.haulmont.cuba.gui.components.AbstractFrame;
import io.jmix.ui.screen.FrameOwner;

public class WindowConfig extends io.jmix.ui.WindowConfig {

    @Override
    protected Class<? extends FrameOwner> loadDefinedScreenClass(String className) {
        if (Strings.isNullOrEmpty(className)) {
            return AbstractFrame.class;
        }
        return super.loadDefinedScreenClass(className);
    }
}
