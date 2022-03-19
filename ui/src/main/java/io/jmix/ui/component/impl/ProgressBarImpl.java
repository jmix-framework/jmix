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

package io.jmix.ui.component.impl;

import io.jmix.ui.component.ProgressBar;
import io.jmix.ui.component.data.ConversionException;

import javax.annotation.Nullable;

/**
 * Web realization of progress bar depending on vaadin {@link ProgressBar} component.
 * <br>
 * Note that indeterminate bar implemented here just like as determinate, but with fixed 0.0 value
 * <br>
 */
public class ProgressBarImpl extends AbstractViewComponent<com.vaadin.ui.ProgressBar, Float, Double>
        implements ProgressBar {

    private static final float DEFAULT_VALUE = 0f;

    public ProgressBarImpl() {
        component = createComponent();
        initComponent(component);
    }

    protected com.vaadin.ui.ProgressBar createComponent() {
        return new com.vaadin.ui.ProgressBar();
    }

    protected void initComponent(com.vaadin.ui.ProgressBar component) {
        component.setIndeterminate(false);
    }

    @Override
    protected void setValueToPresentation(@Nullable Float value) {
        if (hasValidationError()) {
            setValidationError(null);
        }

        component.setValue(value != null ? value : DEFAULT_VALUE);
    }

    @Override
    protected Float convertToPresentation(@Nullable Double modelValue) throws ConversionException {
        return modelValue != null ? modelValue.floatValue() : DEFAULT_VALUE;
    }

    @Override
    public boolean isIndeterminate() {
        return component.isIndeterminate();
    }

    @Override
    public void setIndeterminate(boolean indeterminate) {
        component.setIndeterminate(indeterminate);

        if (indeterminate) {
            component.setValue(0.0f);
        }
    }
}