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

package com.haulmont.cuba.web.sys;

import com.haulmont.cuba.gui.Dialogs;
import com.vaadin.shared.ui.window.WindowMode;
import io.jmix.ui.sys.DialogsImpl;

/**
 * @deprecated Use {@link io.jmix.ui.sys.DialogsImpl} instead
 */
@Deprecated
public class CubaDialogs extends DialogsImpl implements Dialogs {

    @Override
    public OptionDialogBuilder createOptionDialog() {
        backgroundWorker.checkUIAccess();

        return new CubaOptionDialogBuilderImpl();
    }

    @Override
    public MessageDialogBuilder createMessageDialog() {
        backgroundWorker.checkUIAccess();

        return new CubaMessageDialogBuilderImpl();
    }

    public class CubaOptionDialogBuilderImpl extends OptionDialogBuilderImpl
            implements Dialogs.HasMaximized<OptionDialogBuilder> {

        @Override
        public boolean isMaximized() {
            return window.getWindowMode() == WindowMode.MAXIMIZED;
        }

        @Override
        public OptionDialogBuilder withMaximized(boolean maximized) {
            window.setWindowMode(maximized ? WindowMode.MAXIMIZED : WindowMode.NORMAL);
            return this;
        }

        @Override
        public OptionDialogBuilder maximized() {
            return withMaximized(true);
        }
    }

    public class CubaMessageDialogBuilderImpl extends MessageDialogBuilderImpl
            implements Dialogs.HasMaximized<MessageDialogBuilder> {

        @Override
        public boolean isMaximized() {
            return window.getWindowMode() == WindowMode.MAXIMIZED;
        }

        @Override
        public MessageDialogBuilder withMaximized(boolean maximized) {
            window.setWindowMode(maximized ? WindowMode.MAXIMIZED : WindowMode.NORMAL);
            return this;
        }

        @Override
        public MessageDialogBuilder maximized() {
            return withMaximized(true);
        }
    }
}
