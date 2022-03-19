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

import com.haulmont.cuba.gui.WindowManager.OpenType;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.component.Window;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Deprecated
public interface EntityLinkField<V> extends io.jmix.ui.component.EntityLinkField<V>, Field<V> {

    /**
     * @return open type
     * @deprecated Use {@link #getOpenMode()} instead.
     */
    @Deprecated
    OpenType getScreenOpenType();

    /**
     * @param openType open type
     * @deprecated Use {@link #setOpenMode(OpenMode)} instead.
     */
    @Deprecated
    void setScreenOpenType(OpenType openType);

    /**
     * @return ScreenCloseListener or null if not set
     * @deprecated Use {@link Subscription} instead to unsubscribe.
     */
    @Deprecated
    @Nullable
    ScreenCloseListener getScreenCloseListener();

    /**
     * Sets listener to handle close window event. <b> Note, if screen extends {@link Screen} the window parameter will
     * be null. </b>
     *
     * @param closeListener a listener to set
     * @deprecated Use {@link #addEditorCloseListener(Consumer)} instead.
     */
    @Deprecated
    void setScreenCloseListener(@Nullable ScreenCloseListener closeListener);

    /**
     * Sets a custom click handler to the field.
     *
     * @param clickHandler click handler
     * @deprecated Use {@link #setCustomClickHandler(Consumer)}
     */
    @Deprecated
    void setCustomClickHandler(@Nullable EntityLinkClickHandler clickHandler);

    /**
     * Listener to handle close window event.
     *
     * @deprecated Use {@link #addEditorCloseListener(Consumer)}.
     */
    @Deprecated
    interface ScreenCloseListener {
        void windowClosed(@Nullable Window window, String actionId);
    }

    /**
     * Entity link click handler
     *
     * @deprecated Use {@link #setCustomClickHandler(Consumer)}
     */
    @Deprecated
    interface EntityLinkClickHandler {
        void onClick(io.jmix.ui.component.EntityLinkField field);
    }
}
