/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.xml.layout.loader.component.datagrid;

import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.function.ValueProvider;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.ComponentLoader.Context;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import static com.google.common.base.Strings.isNullOrEmpty;

@Component("flowui_NumberRendererProvider")
public class NumberRendererProvider extends AbstractFormattableRendererProvider<NumberRenderer<?>> {

    public static final String NAME = "numberRenderer";

    protected FormatStringsRegistry formatStringsRegistry;

    public NumberRendererProvider(LoaderSupport loaderSupport,
                                  CurrentAuthentication currentAuthentication,
                                  FormatStringsRegistry formatStringsRegistry) {
        super(loaderSupport, currentAuthentication);

        this.formatStringsRegistry = formatStringsRegistry;
    }

    @Override
    public boolean supports(String rendererName) {
        return NAME.equals(rendererName);
    }

    @Override
    public NumberRenderer<?> createRenderer(Element element, MetaPropertyPath metaPropertyPath, Context context) {
        String format = loaderSupport
                .loadResourceString(element, "format", context.getMessageGroup())
                .orElse(null);
        String numberFormat = loaderSupport
                .loadResourceString(element, "numberFormat", context.getMessageGroup())
                .orElse(null);

        if (isNullOrEmpty(format) && isNullOrEmpty(numberFormat)) {
            throw new GuiDevelopmentException("'format' or 'numberFormat' required", context,
                    "Element", element.getName());
        } else if (!isNullOrEmpty(format) && !isNullOrEmpty(numberFormat)) {
            throw new GuiDevelopmentException("Two formats are specified together, required only one: " +
                    "'format' or 'numberFormat'", context, "Element", element.getName());
        }

        String nullRepresentation = loaderSupport
                .loadResourceString(element, "nullRepresentation", context.getMessageGroup())
                .orElse("");

        if (!isNullOrEmpty(format)) {
            return createRendererInternal(metaPropertyPath, format, nullRepresentation);
        } else {
            return createRendererInternal(metaPropertyPath, createNumberFormat(numberFormat), nullRepresentation);
        }
    }

    protected NumberFormat createNumberFormat(String format) {
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getNumberInstance(currentAuthentication.getLocale());
        FormatStrings formatStrings = formatStringsRegistry.getFormatStrings(currentAuthentication.getLocale());

        decimalFormat.setDecimalFormatSymbols(formatStrings.getFormatSymbols());
        decimalFormat.applyPattern(format);
        return decimalFormat;
    }

    @Override
    protected NumberRenderer<?> createRendererInternal(MetaPropertyPath propertyPath,
                                                       String format, String nullRepresentation) {
        return new NumberRenderer<>(
                (ValueProvider<Object, Number>) item ->
                        EntityValues.getValueEx(item, propertyPath),
                format, currentAuthentication.getLocale(), nullRepresentation
        );
    }

    protected NumberRenderer<?> createRendererInternal(MetaPropertyPath propertyPath,
                                                       NumberFormat numberFormat, String nullRepresentation) {
        return new NumberRenderer<>(
                (ValueProvider<Object, Number>) item ->
                        EntityValues.getValueEx(item, propertyPath),
                numberFormat, nullRepresentation);
    }
}
