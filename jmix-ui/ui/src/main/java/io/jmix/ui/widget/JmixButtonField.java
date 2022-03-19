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

package io.jmix.ui.widget;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.v7.ui.themes.BaseTheme;

import javax.annotation.Nullable;
import java.util.Locale;

public class JmixButtonField<V> extends CustomField<V> {
    protected CaptionFormatter<V> captionFormatter;
    protected V value;

    public JmixButtonField() {
        setPrimaryStyleName("jmix-buttonfield");
    }

    @Override
    protected Component initContent() {
        Button button = new JmixButton();
        button.setStyleName(BaseTheme.BUTTON_LINK);
        return button;
    }

    @Override
    protected Button getContent() {
        return (Button) super.getContent();
    }

    @Override
    protected void doSetValue(V newValue) {
        this.value = newValue;

        if (captionFormatter == null) {
            getContent().setCaption(newValue == null ? "" : newValue.toString());
        } else {
            String caption = captionFormatter.convertToPresentation(newValue, getLocale());
            getContent().setCaption(caption);
        }
    }

    @Override
    public V getValue() {
        return value;
    }

    public CaptionFormatter<V> getCaptionFormatter() {
        return captionFormatter;
    }

    public void setCaptionFormatter(CaptionFormatter<V> captionFormatter) {
        this.captionFormatter = captionFormatter;
    }

    public void addClickListener(Button.ClickListener listener) {
        getContent().addClickListener(listener);
    }

    public void removeClickListener(Button.ClickListener listener) {
        getContent().removeClickListener(listener);
    }

    public interface CaptionFormatter<V> {
        String convertToPresentation(@Nullable V value, @Nullable Locale locale);
    }
}