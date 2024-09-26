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

package io.jmix.search.index.fileparsing;

import com.google.common.base.Strings;
import io.jmix.core.FileRef;
import org.apache.commons.io.FilenameUtils;

import java.util.Set;

/**
 * Implements the common logic for all extension based file parser resolvers.
 */
public abstract class AbstractExtensionBasedFileParserResolver implements FileParserResolver {

    /**
     * Returns a collection of supported extensions of the supported file type. E.g. ["xlsx", "XLSX", "docx", "DOCX"].
     *
     * @return collection of supported extensions
     */
    public abstract Set<String> getSupportedExtensions();

    @Override
    public String getCriteriaDescription() {
        return String.format(
                "Parser: %s. Supported extensions: %s.",
                this.getClass().getSimpleName(),
                getSupportedExtensionsString(getSupportedExtensions()));
    }

    @Override
    public boolean supports(FileRef fileRef) {
        String fileName = fileRef.getFileName();
        String fileExtension = FilenameUtils.getExtension(fileName);
        if (Strings.isNullOrEmpty(fileExtension)) {
            return false;
        }

        return getSupportedExtensions().contains(fileExtension);
    }

    protected String getSupportedExtensionsString(Set<String> supportedExtensions) {
        return String.join(", ", supportedExtensions);
    }
}
