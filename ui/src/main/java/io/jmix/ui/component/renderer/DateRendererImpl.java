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

package io.jmix.ui.component.renderer;

import com.vaadin.ui.renderers.DateRenderer;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.impl.AbstractDataGrid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * A renderer for presenting date values.
 */
@Component(DataGrid.DateRenderer.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DateRendererImpl
        extends AbstractDataGrid.AbstractRenderer<Object, Date>
        implements DataGrid.DateRenderer {

    private Locale locale;
    private String formatString;
    private DateFormat dateFormat;

    public DateRendererImpl() {
        super("");
    }

    public DateRendererImpl(String formatString) {
        this(formatString, "");
    }

    public DateRendererImpl(String formatString, String nullRepresentation) {
        this(formatString, null, nullRepresentation);
    }

    public DateRendererImpl(String formatString, Locale locale) {
        this(formatString, locale, "");
    }

    public DateRendererImpl(String formatString, @Nullable Locale locale, String nullRepresentation) {
        super(nullRepresentation);

        this.formatString = formatString;
        this.locale = locale;
    }

    public DateRendererImpl(DateFormat dateFormat) {
        this(dateFormat, "");
    }

    public DateRendererImpl(DateFormat dateFormat, String nullRepresentation) {
        super(nullRepresentation);

        this.dateFormat = dateFormat;
    }

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        if (locale == null) {
            locale = currentAuthentication.getLocale();
        }
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

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void copy(DataGrid.Renderer existingRenderer) {
        if (existingRenderer instanceof DateRendererImpl) {
            setLocale(((DateRendererImpl) existingRenderer).getLocale());
            setNullRepresentation(((DateRendererImpl) existingRenderer).getNullRepresentation());

            if (((DateRendererImpl) existingRenderer).getFormatString() != null) {
                setFormatString(((DateRendererImpl) existingRenderer).getFormatString());
            }

            if (((DateRendererImpl) existingRenderer).getDateFormat() != null) {
                setDateFormat(((DateRendererImpl) existingRenderer).getDateFormat());
            }
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

    @Nullable
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

    @Nullable
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
