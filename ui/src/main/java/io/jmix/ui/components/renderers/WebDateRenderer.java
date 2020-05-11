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

import com.vaadin.ui.renderers.DateRenderer;
import io.jmix.core.AppBeans;
import io.jmix.core.Entity;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.components.DataGrid;
import io.jmix.ui.components.impl.WebAbstractDataGrid;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static io.jmix.core.commons.util.Preconditions.checkNotNullArgument;

/**
 * A renderer for presenting date values.
 */
public class WebDateRenderer extends WebAbstractDataGrid.AbstractRenderer<Entity, Date> implements DataGrid.DateRenderer {

    private Locale locale;
    private String formatString;
    private DateFormat dateFormat;

    public WebDateRenderer() {
        super("");
        locale = AppBeans.get(CurrentAuthentication.class).getLocale();
    }

    public WebDateRenderer(String formatString) {
        this(formatString, "");
    }

    public WebDateRenderer(String formatString, String nullRepresentation) {
        this(formatString, AppBeans.get(CurrentAuthentication.class).getLocale(), nullRepresentation);
    }

    public WebDateRenderer(String formatString, Locale locale) {
        this(formatString, locale, "");
    }

    public WebDateRenderer(String formatString, Locale locale, String nullRepresentation) {
        super(nullRepresentation);

        this.formatString = formatString;
        this.locale = locale;
    }

    public WebDateRenderer(DateFormat dateFormat) {
        this(dateFormat, "");
    }

    public WebDateRenderer(DateFormat dateFormat, String nullRepresentation) {
        super(nullRepresentation);

        this.dateFormat = dateFormat;
    }

    @Override
    public DateRenderer getImplementation() {
        return (DateRenderer) super.getImplementation();
    }

    @Override
    protected DateRenderer createImplementation() {
        if (dateFormat == null) {
            checkNotNullArgument(formatString, "Format string may not be null");
            checkNotNullArgument(locale, "Locale may not be null");
            dateFormat = new SimpleDateFormat(formatString, locale);
        }
        return new DateRenderer(dateFormat, getNullRepresentation());
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
    public String getFormatString() {
        return formatString;
    }

    @Override
    public void setFormatString(String formatString) {
        checkRendererNotSet();
        this.formatString = formatString;
        this.dateFormat = null;
    }

    @Override
    public DateFormat getDateFormat() {
        return dateFormat;
    }

    @Override
    public void setDateFormat(DateFormat dateFormat) {
        checkRendererNotSet();
        this.dateFormat = dateFormat;
        this.formatString = null;
    }
}
