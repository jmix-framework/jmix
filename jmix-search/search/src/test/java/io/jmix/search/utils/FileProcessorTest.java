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

package io.jmix.search.utils;

import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageLocator;
import io.jmix.search.exception.UnsupportedFileFormatException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileProcessorTest {

    public static final String FILE_NAME_EXAMPLE = "the-file-with-not-supported-extension.sql";

    @Test
    void extractFileContent() {
        FileStorageLocator storageLocatorMock = mock(FileStorageLocator.class);
        FileProcessor fileProcessor = new FileProcessor(storageLocatorMock);
        FileRef fileRefMock = mock(FileRef.class);
        when(fileRefMock.getFileName()).thenReturn(FILE_NAME_EXAMPLE);
        UnsupportedFileFormatException exception = assertThrows(UnsupportedFileFormatException.class, () -> fileProcessor.extractFileContent(fileRefMock));
        assertEquals("The file the-file-with-not-supported-extension.sql with 'sql' extension is not supported. Only following formats are supported: pdf, doc, docx, xls, xlsx, odt, ods, rtf, txt.", exception.getMessage());
    }
}