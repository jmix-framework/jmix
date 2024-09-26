/*
 * Copyright 2024 Haulmont.
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

package io.jmix.search.index.fileparsing.resolvers;

import io.jmix.search.index.fileparsing.AbstractExtensionBasedFileParserResolver;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.microsoft.OfficeParser;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component("search_LegacyMSOfficeDocumentsParserResolver")
@Order(100)
public class LegacyMSOfficeDocumentsParserResolver extends AbstractExtensionBasedFileParserResolver {

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of("doc", "xls");
    }

    @Override
    public Parser getParser() {
        return new OfficeParser();
    }
}