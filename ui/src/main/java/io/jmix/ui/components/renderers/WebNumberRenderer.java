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

package io.jmix.ui.components.renderers;

import com.vaadin.ui.renderers.NumberRenderer;
import com.vaadin.ui.renderers.Renderer;
import io.jmix.core.AppBeans;
import io.jmix.core.Entity;
import io.jmix.core.security.UserSessionSource;
import io.jmix.ui.components.DataGrid;
import io.jmix.ui.components.impl.WebAbstractDataGrid;

import java.text.NumberFormat;
import java.util.Locale;

import static io.jmix.core.commons.util.Preconditions.checkNotNullArgument;

/**
 * A renderer for presenting number values.
 */
public class WebNumberRenderer extends WebAbstractDataGrid.AbstractRenderer<Entity, Number> implements DataGrid.NumberRenderer {

    private Locale locale;
    private NumberFormat numberFormat;
    private String formatString;

    public WebNumberRenderer() {
        super("");
        locale = AppBeans.get(UserSessionSource.class).getLocale();
    }

    public WebNumberRenderer(NumberFormat numberFormat) {
        this(numberFormat, "");
    }

    public WebNumberRenderer(NumberFormat numberFormat, String nullRepresentation) {
        super(nullRepresentation);

        this.numberFormat = numberFormat;
    }

    public WebNumberRenderer(String formatString) throws IllegalArgumentException {
        this(formatString, AppBeans.get(UserSessionSource.class).getLocale());
    }

    public WebNumberRenderer(String formatString, Locale locale) throws IllegalArgumentException {
        this(formatString, locale, "");
    }

    public WebNumberRenderer(String formatString, Locale locale, String nullRepresentation) {
        super(nullRepresentation);

        this.formatString = formatString;
        this.locale = locale;
    }

    @Override
    public NumberRenderer getImplementation() {
        return (NumberRenderer) super.getImplementation();
    }

    @Override
    protected Renderer<Number> createImplementation() {
        if (numberFormat != null) {
            return new NumberRenderer(numberFormat, getNullRepresentation());
        } else {
            checkNotNullArgument(formatString, "Format string may not be null");
            checkNotNullArgument(locale, "Locale may not be null");
            return new NumberRenderer(formatString, locale, getNullRepresentation());
        }
    }

    @Override
    public String getNullRepresentation() {
        return super.getNullRepresentation();
    }

    @Override
    public void setNullRepresentation(String nullRepresentation) {
        super.setNullRepresentation(nullRepresentation);
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setLocale(Locale locale) {
        checkRendererNotSet();
        this.locale = locale;
    }

    @Override
    public NumberFormat getNumberFormat() {
        return numberFormat;
    }

    @Override
    public void setNumberFormat(NumberFormat numberFormat) {
        checkRendererNotSet();
        this.numberFormat = numberFormat;
        this.formatString = null;
    }

    @Override
    public String getFormatString() {
        return formatString;
    }

    @Override
    public void setFormatString(String formatString) {
        checkRendererNotSet();
        this.formatString = formatString;
        this.numberFormat = null;
    }
}
