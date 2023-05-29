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

package com.haulmont.cuba.gui.components;

import io.jmix.ui.component.SizeUnit;

/**
 * Root of the Generic UI components hierarchy.
 * <p>
 * Note: has no direct inheritors. Used as deprecated API holder.
 *
 * @deprecated Use {@link io.jmix.ui.component.Component} instead
 */
@Deprecated
public interface Component extends io.jmix.ui.component.Component {

    /**
     * @deprecated Use {@link SizeUnit} instead.
     */
    @Deprecated
    int UNITS_PIXELS = 0;
    /**
     * @deprecated @deprecated Use {@link SizeUnit} instead.
     */
    @Deprecated
    int UNITS_PERCENTAGE = 8;
}
