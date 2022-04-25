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

package io.jmix.flowui.sys;

import io.jmix.core.common.util.Dom4j;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Parses screen XML taking into account 'assign' elements.
 */
@Component("flowui_ScreenXmlParser")
public class ScreenXmlParser {

    public Document parseDescriptor(InputStream stream) {
        checkNotNullArgument(stream, "Input stream is null");

        String template;
        try {
            template = IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return parseDescriptor(template);
    }

    public Document parseDescriptor(String template) {
        checkNotNullArgument(template, "template is null");

        return Dom4j.readDocument(template);
    }
}
