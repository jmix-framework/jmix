/*
 * Copyright 2020 Haulmont.
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
package com.haulmont.cuba.core.app;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.core.global.Metadata;
import io.jmix.core.FileRef;
import io.jmix.core.TimeSource;
import io.jmix.localfs.LocalFileStorage;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@Component(FileStorageAPI.NAME)
public class CubaFileStorage implements FileStorageAPI {

    @Autowired
    protected LocalFileStorage delegate;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected TimeSource timeSource;

    @Override
    public long saveStream(FileDescriptor fileDescr, InputStream inputStream) throws FileStorageException {
        checkFileDescriptor(fileDescr);
        try {
            return delegate.saveStream(toFileRef(fileDescr), inputStream);
        } catch (io.jmix.core.FileStorageException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public void saveFile(FileDescriptor fileDescr, byte[] data) throws FileStorageException {
        checkNotNullArgument(data, "File content is null");
        saveStream(fileDescr, new ByteArrayInputStream(data));
    }

    @Override
    public void removeFile(FileDescriptor fileDescr) throws FileStorageException {
        checkFileDescriptor(fileDescr);
        try {
            delegate.removeFile(toFileRef(fileDescr));
        } catch (io.jmix.core.FileStorageException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public InputStream openStream(FileDescriptor fileDescr) throws FileStorageException {
        checkFileDescriptor(fileDescr);
        try {
            return delegate.openStream(toFileRef(fileDescr));
        } catch (io.jmix.core.FileStorageException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public byte[] loadFile(FileDescriptor fileDescr) throws FileStorageException {
        InputStream inputStream = openStream(fileDescr);
        try {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, fileDescr.getId().toString(), e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    @Override
    public boolean fileExists(FileDescriptor fileDescr) throws FileStorageException {
        checkFileDescriptor(fileDescr);
        try {
            return delegate.fileExists(toFileRef(fileDescr));
        } catch (io.jmix.core.FileStorageException e) {
            throw new FileStorageException(e);
        }
    }

    public FileRef toFileRef(FileDescriptor fileDescriptor) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fileDescriptor.getCreateDate());
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        String datePath = year + "/"
                + StringUtils.leftPad(String.valueOf(month), 2, '0') + "/"
                + StringUtils.leftPad(String.valueOf(day), 2, '0');

        String fileExtension = StringUtils.isNoneBlank(fileDescriptor.getExtension())
                ? "." + fileDescriptor.getExtension()
                : StringUtils.EMPTY;

        String path = datePath + "/" + fileDescriptor.getId() + fileExtension;
        return new FileRef(delegate.getStorageName(), path, fileDescriptor.getName());
    }

    public LocalFileStorage getDelegate() {
        return delegate;
    }

    protected void checkFileDescriptor(FileDescriptor fd) {
        if (fd == null || fd.getCreateDate() == null) {
            throw new IllegalArgumentException("A FileDescriptor instance with populated 'createDate' attribute must be provided");
        }
    }
}