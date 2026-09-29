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

package metadata;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.JmixOrder;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.datatype.AdaptiveDatatypeProvider;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.impl.AdaptiveNumberDatatype;
import io.jmix.core.metamodel.model.MetaProperty;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.addon1.TestAddon1Configuration;
import test_support.app.TestAppConfiguration;
import test_support.app.entity.adaptive_datatype.AdaptiveDatatypeEntity;
import test_support.app.entity.adaptive_datatype.TestDisplayPrefix;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CoreConfiguration.class, TestAddon1Configuration.class, TestAppConfiguration.class,
        AdaptiveDatatypeProviderTest.TestProvidersConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AdaptiveDatatypeProviderTest {

    @Autowired
    Metadata metadata;

    @Test
    void adaptiveDatatypeProvider_annotatedField_providesPropertyDatatype() {
        Datatype<?> datatype = getDatatype("code");

        assertEquals("code:abc", datatype.format("abc"));
    }

    @Test
    void adaptiveDatatypeProvider_annotatedMethod_providesPropertyDatatype() {
        Datatype<?> datatype = getDatatype("summary");

        assertEquals("summary:abc", datatype.format("abc"));
    }

    @Test
    void adaptiveDatatypeProvider_notAnnotatedProperty_keepsRegisteredDatatype() {
        Datatype<?> datatype = getDatatype("plain");

        assertEquals("string_mod", datatype.getId());
        assertEquals("abc", datatype.format("abc"));
    }

    @Test
    void adaptiveDatatypeProvider_numberFormatPresent_numberFormatWinsOverProviderWithoutOrder() {
        Datatype<?> datatype = getDatatype("amount");

        assertInstanceOf(AdaptiveNumberDatatype.class, datatype);
        assertEquals("1,234.50", datatype.format(new BigDecimal("1234.5")));
    }

    @Test
    void adaptiveDatatypeProvider_providerBeforeNumberFormatProvider_overridesNumberFormat() {
        Datatype<?> datatype = getDatatype("overriddenAmount");

        assertEquals("high:1234.5", datatype.format(new BigDecimal("1234.5")));
    }

    @Test
    void adaptiveDatatypeProvider_severalProvidersHandleProperty_highestPrecedenceWins() {
        Datatype<?> datatype = getDatatype("contested");

        assertEquals("high:abc", datatype.format("abc"));
    }

    private Datatype<?> getDatatype(String propertyName) {
        MetaProperty metaProperty = metadata.getClass(AdaptiveDatatypeEntity.class).getProperty(propertyName);
        return metaProperty.getRange().asDatatype();
    }

    @Configuration
    static class TestProvidersConfiguration {

        // Declared before the provider with higher precedence, so the result does not depend on bean registration order.
        @Bean
        PrefixDatatypeProvider prefixDatatypeProvider() {
            return new PrefixDatatypeProvider();
        }

        @Bean
        HighPrecedenceDatatypeProvider highPrecedenceDatatypeProvider() {
            return new HighPrecedenceDatatypeProvider();
        }
    }

    /**
     * Provides a datatype for every property annotated with {@link TestDisplayPrefix}. Has no order, so it is called
     * after all providers that have one.
     */
    @NullMarked
    static class PrefixDatatypeProvider implements AdaptiveDatatypeProvider {

        @Override
        public @Nullable Datatype<?> getAdaptiveDatatype(MetaProperty metaProperty, Class<?> type) {
            TestDisplayPrefix annotation = metaProperty.getAnnotatedElement().getAnnotation(TestDisplayPrefix.class);
            return annotation != null ? new TestPrefixDatatype(annotation.value()) : null;
        }
    }

    /**
     * Provides a datatype only for the {@code contested} property, which {@link PrefixDatatypeProvider} also handles,
     * and for the {@code overriddenAmount} property, which has {@code @NumberFormat}. Runs before the core provider
     * of {@code @NumberFormat}.
     */
    @Order(JmixOrder.HIGHEST_PRECEDENCE)
    @NullMarked
    static class HighPrecedenceDatatypeProvider implements AdaptiveDatatypeProvider {

        private static final Set<String> PROPERTY_NAMES = Set.of("contested", "overriddenAmount");

        @Override
        public @Nullable Datatype<?> getAdaptiveDatatype(MetaProperty metaProperty, Class<?> type) {
            return PROPERTY_NAMES.contains(metaProperty.getName()) ? new TestPrefixDatatype("high:") : null;
        }
    }

    static class TestPrefixDatatype implements Datatype<Object> {

        private final String prefix;

        TestPrefixDatatype(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public String format(@Nullable Object value) {
            return value == null ? "" : prefix + value;
        }

        @Override
        public String format(@Nullable Object value, Locale locale) {
            return format(value);
        }

        @Override
        public @Nullable Object parse(@Nullable String value) {
            return value == null ? null : value.substring(prefix.length());
        }

        @Override
        public @Nullable Object parse(@Nullable String value, Locale locale) {
            return parse(value);
        }

        @Override
        public String getId() {
            return "test_prefix";
        }

        @Override
        public Class<?> getJavaClass() {
            return Object.class;
        }
    }
}
