/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reports.libintegration;

import com.haulmont.yarg.exception.ReportFormattingException;
import com.haulmont.yarg.formatters.impl.inline.AbstractInliner;
import io.jmix.core.DataManager;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorageLocator;
import io.jmix.core.Metadata;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.regex.Pattern;

@Component("report_FileStorageContentInliner")
public class FileStorageContentInliner extends AbstractInliner {
    private final static String REGULAR_EXPRESSION = "\\$\\{imageFileId:([0-9]+?)x([0-9]+?)\\}";

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected FileStorageLocator fileStorageLocator;

    public FileStorageContentInliner() {
        tagPattern = Pattern.compile(REGULAR_EXPRESSION, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public Pattern getTagPattern() {
        return tagPattern;
    }

    @Override
    protected byte[] getContent(Object paramValue) {
        try {
            FileRef fileRef;
            if (paramValue instanceof FileRef) {
                fileRef = (FileRef) paramValue;
            } else {
                fileRef = getFileRef((String) paramValue);
            }

            if (fileRef != null) {
                return IOUtils.toByteArray(fileStorageLocator.getByName(fileRef.getStorageName()).openStream(fileRef));
            }

        } catch (IOException e) {
            throw new ReportFormattingException(String.format("Unable to get image from file storage. File id [%s]", paramValue), e);
        }
        return new byte[0];
    }

    @Nullable
    protected FileRef getFileRef(String uri) {
        try {
            return FileRef.fromString(uri);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}