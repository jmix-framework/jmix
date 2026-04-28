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

package io.jmix.grapesjs.widget.grapesjshtmleditorcomponent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GrapesJsHtmlSanitizerTest {

    @Test
    public void testRemoveJavaScriptFromHtml() {
        String html = "<body><div onclick=\"alert(1)\">Text</div>"
                + "<script>alert(2)</script>"
                + "<a href=\"javascript:alert(3)\">link</a></body>";

        String sanitizedHtml = GrapesJsHtmlSanitizer.sanitize(html);

        assertFalse(sanitizedHtml.contains("<script"));
        assertFalse(sanitizedHtml.contains("onclick"));
        assertFalse(sanitizedHtml.contains("javascript:"));
        assertTrue(sanitizedHtml.contains(">Text<"));
        assertTrue(sanitizedHtml.contains(">link<"));
    }

    @Test
    public void testKeepFreeMarkerPseudoTags() {
        String html = "<body><#if user??><p>${user.name}</p><@renderButton type=\"primary\"/></#if></body>";

        String sanitizedHtml = GrapesJsHtmlSanitizer.sanitize(html);

        assertTrue(sanitizedHtml.contains("<#if user??>"));
        assertTrue(sanitizedHtml.contains("${user.name}"));
        assertTrue(sanitizedHtml.contains("<@renderButton type=\"primary\"/>"));
        assertTrue(sanitizedHtml.contains("</#if>"));
    }

    @Test
    public void testKeepNullValueAsNull() {
        assertNull(GrapesJsHtmlSanitizer.sanitize(null));
    }
}
