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

import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.function.ValueProvider;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component("ui_LocalDateTimeRendererProvider")
public class LocalDateTimeRendererProvider extends AbstractFormattableRendererProvider<LocalDateTimeRenderer<?>> {

    public static final String NAME = "localDateTimeRenderer";

    public LocalDateTimeRendererProvider(LoaderSupport loaderSupport,
                                         CurrentAuthentication currentAuthentication) {
        super(loaderSupport, currentAuthentication);
    }

    @Override
    public boolean supports(String rendererName) {
        return NAME.equals(rendererName);
    }

    @Override
    protected LocalDateTimeRenderer<?> createRendererInternal(MetaPropertyPath propertyPath,
                                                              String format, String nullRepresentation) {
        return new LocalDateTimeRenderer<>(
                (ValueProvider<Object, LocalDateTime>) item ->
                        EntityValues.getValueEx(item, propertyPath),
                format, currentAuthentication.getLocale(), nullRepresentation
        );
    }
}
