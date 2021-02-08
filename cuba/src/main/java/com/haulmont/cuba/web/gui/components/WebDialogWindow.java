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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.DialogOptions;
import com.haulmont.cuba.gui.WindowContext;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.CubaComponentsHelper;
import com.haulmont.cuba.gui.components.Window;
import com.vaadin.ui.AbstractOrderedLayout;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.SizeUnit;
import io.jmix.ui.component.WindowMode;
import io.jmix.ui.component.impl.DialogWindowImpl;
import io.jmix.ui.screen.UiControllerUtils;

@Deprecated
public class WebDialogWindow extends DialogWindowImpl implements Window {

    protected DialogOptions dialogOptions; // lazily initialized

    @Override
    public WindowContext getContext() {
        return (WindowContext) super.getContext();
    }

    @Override
    public WindowManager getWindowManager() {
        return (WindowManager) UiControllerUtils.getScreenContext(getFrameOwner()).getScreens();
    }

    @Override
    public DialogOptions getDialogOptions() {
        if (dialogOptions == null) {
            dialogOptions = new WebDialogOptions();
        }
        return dialogOptions;
    }

    @Override
    public void expand(Component childComponent, String height, String width) {
        com.vaadin.ui.Component expandedComponent = childComponent.unwrapComposition(com.vaadin.ui.Component.class);
        CubaComponentsHelper.expand((AbstractOrderedLayout) getContainer(), expandedComponent, height, width);
    }

    /**
     * Compatibility layer class.
     */
    @Deprecated
    protected class WebDialogOptions extends DialogOptions {
        @Override
        public Float getWidth() {
            return getDialogWidth();
        }

        @Override
        public SizeUnit getWidthUnit() {
            return getDialogWidthUnit();
        }

        @Override
        protected DialogOptions setWidth(Float width, SizeUnit sizeUnit) {
            if (width != null && sizeUnit != null) {
                setDialogWidth(width + sizeUnit.getSymbol());
            } else if (width != null) {
                setDialogWidth(width + "px");
            }

            super.setWidth(width, sizeUnit);

            return this;
        }

        @Override
        public Float getHeight() {
            return getDialogHeight();
        }

        @Override
        public SizeUnit getHeightUnit() {
            return getDialogHeightUnit();
        }

        @Override
        protected DialogOptions setHeight(Float height, SizeUnit sizeUnit) {
            if (height != null && sizeUnit != null) {
                setDialogHeight(height + sizeUnit.getSymbol());
            } else if (height != null) {
                setDialogHeight(height + "px");
            }

            super.setHeight(height, sizeUnit);

            return this;
        }

        @Override
        public Boolean getModal() {
            return WebDialogWindow.this.isModal();
        }

        @Override
        public DialogOptions setModal(Boolean modal) {
            WebDialogWindow.this.setModal(modal);

            return this;
        }

        @Override
        public Boolean getResizable() {
            return WebDialogWindow.this.isResizable();
        }

        @Override
        public DialogOptions setResizable(Boolean resizable) {
            WebDialogWindow.this.setResizable(resizable);

            return this;
        }

        @Override
        public Boolean getCloseable() {
            return WebDialogWindow.this.isCloseable();
        }

        @Override
        public DialogOptions setCloseable(Boolean closeable) {
            WebDialogWindow.this.setCloseable(closeable);

            return this;
        }

        @Override
        public DialogOptions center() {
            WebDialogWindow.this.center();

            return this;
        }

        @Override
        public Integer getPositionX() {
            return WebDialogWindow.this.getPositionX();
        }

        @Override
        public DialogOptions setPositionX(Integer positionX) {
            WebDialogWindow.this.setPositionX(positionX);
            return this;
        }

        @Override
        public Integer getPositionY() {
            return WebDialogWindow.this.getPositionY();
        }

        @Override
        public DialogOptions setPositionY(Integer positionY) {
            WebDialogWindow.this.setPositionY(positionY);
            return this;
        }

        @Override
        public DialogOptions setMaximized(Boolean maximized) {
            setWindowMode(maximized ? WindowMode.MAXIMIZED : WindowMode.NORMAL);
            return this;
        }

        @Override
        public Boolean getMaximized() {
            return getWindowMode() == WindowMode.MAXIMIZED;
        }
    }
}
