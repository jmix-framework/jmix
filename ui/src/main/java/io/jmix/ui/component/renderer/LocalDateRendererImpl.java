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

import com.vaadin.ui.renderers.LocalDateRenderer;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.impl.AbstractDataGrid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * A renderer for presenting LocalDate values.
 */
@Component(DataGrid.LocalDateRenderer.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LocalDateRendererImpl
        extends AbstractDataGrid.AbstractRenderer<Object, LocalDate>
        implements DataGrid.LocalDateRenderer {

    private Locale locale;
    private String formatPattern;
    private DateTimeFormatter formatter;

    public LocalDateRendererImpl() {
        super("");
    }

    public LocalDateRendererImpl(String formatPattern) {
        this(formatPattern, "");
    }

    public LocalDateRendererImpl(String formatPattern, String nullRepresentation) {
        this(formatPattern, null, nullRepresentation);
    }

    public LocalDateRendererImpl(String formatPattern, Locale locale) {
        this(formatPattern, locale, "");
    }

    public LocalDateRendererImpl(String formatPattern, @Nullable Locale locale, String nullRepresentation) {
        super(nullRepresentation);

        this.formatPattern = formatPattern;
        this.locale = locale;
    }

    public LocalDateRendererImpl(DateTimeFormatter formatter) {
        this(formatter, "");
    }

    public LocalDateRendererImpl(DateTimeFormatter formatter, String nullRepresentation) {
        super(nullRepresentation);

        this.formatter = formatter;
    }

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        if (locale == null) {
            locale = currentAuthentication.getLocale();
        }
    }

    @Override
    public LocalDateRenderer getImplementation() {
        return (LocalDateRenderer) super.getImplementation();
    }

    @Override
    protected LocalDateRenderer createImplementation() {
        if (formatter == null) {
            checkNotNullArgument(formatPattern, "Format pattern may not be null");
            checkNotNullArgument(locale, "Locale may not be null");
            formatter = DateTimeFormatter.ofPattern(formatPattern, locale);
        }
        return new LocalDateRenderer(formatter, getNullRepresentation());
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void copy(DataGrid.Renderer existingRenderer) {
        if (existingRenderer instanceof LocalDateRendererImpl) {
            setNullRepresentation(((LocalDateRendererImpl) existingRenderer).getNullRepresentation());
            setLocale(((LocalDateRendererImpl) existingRenderer).getLocale());

            if (((LocalDateRendererImpl) existingRenderer).getFormatPattern() != null) {
                setFormatPattern(((LocalDateRendererImpl) existingRenderer).getFormatPattern());
            }

            if (((LocalDateRendererImpl) existingRenderer).getFormatter() != null) {
                setFormatter(((LocalDateRendererImpl) existingRenderer).getFormatter());
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
    public String getFormatPattern() {
        return formatPattern;
    }

    @Override
    public void setFormatPattern(String formatPattern) {
        checkRendererNotSet();
        this.formatPattern = formatPattern;
        this.formatter = null;
    }

    @Nullable
    @Override
    public DateTimeFormatter getFormatter() {
        return formatter;
    }

    @Override
    public void setFormatter(DateTimeFormatter formatter) {
        checkRendererNotSet();
        this.formatter = formatter;
        this.formatPattern = null;
    }
}
