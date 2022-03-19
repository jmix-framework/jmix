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

package com.haulmont.cuba.gui.presentation;

import io.jmix.ui.presentation.TablePresentations;
import io.jmix.ui.presentation.model.TablePresentation;
import org.dom4j.Element;

import javax.annotation.Nullable;

/**
 * @deprecated Use {@link TablePresentations} instead.
 */
@Deprecated
public interface Presentations extends TablePresentations {

    /**
     * Returns user settings for the selected presentation or <code>null</code>
     * if the presentation doesn't exist or if the presentation doesn't contain any settings.
     *
     * @deprecated Use {@link #getSettingsString(TablePresentation)} instead.
     */
    @Nullable
    @Deprecated
    Element getSettings(TablePresentation p);

    /**
     * Sets user settings for the selected presentation.
     *
     * @deprecated Use {@link #setSettings(TablePresentation, String)} instead.
     */
    @Deprecated
    void setSettings(TablePresentation p, Element e);
}
