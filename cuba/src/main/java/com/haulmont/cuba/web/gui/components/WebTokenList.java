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

import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.components.LookupPickerField;
import com.haulmont.cuba.gui.components.TokenList;
import io.jmix.core.JmixEntity;
import com.haulmont.cuba.gui.components.CaptionMode;
import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.component.DialogWindow;
import io.jmix.ui.component.Window;
import io.jmix.ui.screen.Screen;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;

@Deprecated
public class WebTokenList<V extends JmixEntity> extends io.jmix.ui.component.impl.WebTokenList<V> implements TokenList<V> {

    protected OpenType lookupOpenType = OpenType.THIS_TAB;

    @Override
    public void addValidator(Consumer<? super Collection<V>> validator) {
        addValidator(validator::accept);
    }

    @Override
    public void removeValidator(Consumer<Collection<V>> validator) {
        removeValidator(validator::accept);
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    @Override
    public String getOptionsCaptionProperty() {
        return ((LookupPickerField) entityComboBox).getCaptionProperty();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setOptionsCaptionProperty(@Nullable String optionsCaptionProperty) {
        ((LookupPickerField) entityComboBox).setCaptionProperty(optionsCaptionProperty);
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    @Override
    public CaptionMode getOptionsCaptionMode() {
        return ((LookupPickerField) entityComboBox).getCaptionMode();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setOptionsCaptionMode(@Nullable CaptionMode optionsCaptionMode) {
        ((LookupPickerField) entityComboBox).setCaptionMode(optionsCaptionMode);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void createEntityComboBox() {
        entityComboBox = uiComponents.create(LookupPickerField.class);
        entityComboBox.addValueChangeListener(lookupSelectListener);
    }

    @Override
    public OpenType getLookupOpenMode() {
        return lookupOpenType;
    }

    @Override
    public void setLookupOpenMode(OpenType lookupOpenMode) {
        Preconditions.checkNotNullArgument(lookupOpenMode);

        lookupOpenType = lookupOpenMode;
        launchMode = lookupOpenMode.getOpenMode();
    }

    @Override
    protected Screen createLookupScreen(@Nullable Runnable afterLookupSelect) {
        Screen lookupScreen = super.createLookupScreen(afterLookupSelect);

        if (lookupOpenType != null) {
            applyOpenTypeParameters(lookupScreen.getWindow(), lookupOpenType);
        }

        return lookupScreen;
    }

    @Deprecated
    protected void applyOpenTypeParameters(Window window, OpenType openType) {
        if (window instanceof DialogWindow) {
            DialogWindow dialogWindow = (DialogWindow) window;

            if (openType.getCloseOnClickOutside() != null) {
                dialogWindow.setCloseOnClickOutside(openType.getCloseOnClickOutside());
            }
            if (openType.getMaximized() != null) {
                dialogWindow.setWindowMode(openType.getMaximized() ? DialogWindow.WindowMode.MAXIMIZED : DialogWindow.WindowMode.NORMAL);
            }
            if (openType.getModal() != null) {
                dialogWindow.setModal(openType.getModal());
            }
            if (openType.getResizable() != null) {
                dialogWindow.setResizable(openType.getResizable());
            }
            if (openType.getWidth() != null) {
                dialogWindow.setDialogWidth(openType.getWidthString());
            }
            if (openType.getHeight() != null) {
                dialogWindow.setDialogHeight(openType.getHeightString());
            }
        }

        if (openType.getCloseable() != null) {
            window.setCloseable(openType.getCloseable());
        }
    }
}
