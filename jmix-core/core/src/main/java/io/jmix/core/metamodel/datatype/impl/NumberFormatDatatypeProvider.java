/*
 * Copyright 2026 Haulmont.
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

package io.jmix.core.metamodel.datatype.impl;

import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.annotation.NumberFormat;
import io.jmix.core.metamodel.datatype.AdaptiveDatatypeProvider;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.metamodel.model.MetaProperty;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Provides an {@link AdaptiveNumberDatatype} for a property of the {@link Number} type that has
 * the {@link NumberFormat} annotation.
 * <p>
 * Has the {@code JmixOrder.HIGHEST_PRECEDENCE + 100} order, so it runs before providers that have no order
 * or a lower precedence.
 */
@Component("core_NumberFormatDatatypeProvider")
@Order(JmixOrder.HIGHEST_PRECEDENCE + 100)
@NullMarked
public class NumberFormatDatatypeProvider implements AdaptiveDatatypeProvider {

    private static final Logger log = LoggerFactory.getLogger(NumberFormatDatatypeProvider.class);

    protected final FormatStringsRegistry formatStringsRegistry;

    protected final Messages messages;

    public NumberFormatDatatypeProvider(FormatStringsRegistry formatStringsRegistry, Messages messages) {
        this.formatStringsRegistry = formatStringsRegistry;
        this.messages = messages;
    }

    @Override
    public @Nullable Datatype<?> getAdaptiveDatatype(MetaProperty metaProperty, Class<?> type) {
        NumberFormat numberFormat = metaProperty.getAnnotatedElement().getAnnotation(NumberFormat.class);
        if (numberFormat == null) {
            return null;
        }
        if (!Number.class.isAssignableFrom(type)) {
            log.warn("NumberFormat annotation is ignored because {} is not a Number", metaProperty);
            return null;
        }
        return new AdaptiveNumberDatatype(type, numberFormat, formatStringsRegistry, messages);
    }
}
