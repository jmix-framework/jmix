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

package xml_layout_support;

import io.jmix.flowui.kit.xml.layout.support.BaseLoaderSupport;
import org.dom4j.tree.BaseElement;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class BaseLoaderSupportTest {

    enum Sample {FIRST, SECOND}

    private BaseElement element(String attribute, String value) {
        BaseElement element = new BaseElement("button");
        element.addAttribute(attribute, value);
        return element;
    }

    @Test
    void testLoadStringEmptyToNullByDefault() {
        assertEquals(Optional.of("ok"), BaseLoaderSupport.loadString(element("text", "ok"), "text"));
        assertEquals(Optional.empty(), BaseLoaderSupport.loadString(element("text", ""), "text"));
        assertEquals(Optional.empty(), BaseLoaderSupport.loadString(new BaseElement("button"), "text"));
        assertEquals(Optional.of(""), BaseLoaderSupport.loadString(element("text", ""), "text", false));
    }

    @Test
    void testTypedLoads() {
        assertEquals(Optional.of(true), BaseLoaderSupport.loadBoolean(element("enabled", "true"), "enabled"));
        assertEquals(Optional.of(42), BaseLoaderSupport.loadInteger(element("tabIndex", "42"), "tabIndex"));
        assertEquals(Optional.of(1.5d), BaseLoaderSupport.loadDouble(element("min", "1.5"), "min"));
        assertEquals(Optional.of(Sample.SECOND),
                BaseLoaderSupport.loadEnum(element("mode", "SECOND"), Sample.class, "mode"));
    }

    @Test
    void testConsumerOverloadSkipsAbsentAttribute() {
        AtomicReference<String> holder = new AtomicReference<>("untouched");
        BaseLoaderSupport.loadString(new BaseElement("button"), "text", holder::set);
        assertEquals("untouched", holder.get());
        BaseLoaderSupport.loadString(element("text", "ok"), "text", holder::set);
        assertEquals("ok", holder.get());
    }

    @Test
    void testSplitByWhitespaceAndCommas() {
        assertEquals(List.of("a", "b", "c"), BaseLoaderSupport.split("a b,c"));
        assertEquals(List.of(), BaseLoaderSupport.split("  ,  "));
    }
}
