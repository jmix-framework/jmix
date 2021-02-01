/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.gui.upload;

import com.haulmont.cuba.core.app.CubaFileStorage;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.core.global.Metadata;
import io.jmix.core.FileRef;
import io.jmix.core.TimeSource;
import io.jmix.ui.executor.TaskLifeCycle;
import io.jmix.ui.upload.TemporaryStorage;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@Component(FileUploadingAPI.NAME)
public class FileUploading implements FileUploadingAPI {
    @Autowired
    protected TemporaryStorage delegate;

    @Autowired
    protected CubaFileStorage fileStorage;

    @Autowired
    protected TimeSource timeSource;

    @Override
    public UUID saveFile(byte[] data) throws FileStorageException {
        try {
            return delegate.saveFile(data);
        } catch (io.jmix.core.FileStorageException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public UUID saveFile(InputStream stream, @Nullable UploadProgressListener listener) throws FileStorageException {
        try {
            return delegate.saveFile(stream, listener == null
                    ? null
                    : (TemporaryStorage.UploadProgressListener) listener::progressChanged);
        } catch (io.jmix.core.FileStorageException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public UUID createEmptyFile() throws FileStorageException {
        try {
            TemporaryStorage.FileInfo fileInfo = delegate.createFile();
            return fileInfo.getId();
        } catch (io.jmix.core.FileStorageException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public FileInfo createFile() throws FileStorageException {
        try {
            TemporaryStorage.FileInfo fileInfo = delegate.createFile();
            return new FileInfo(fileInfo.getFile(), fileInfo.getId());
        } catch (io.jmix.core.FileStorageException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public UUID createNewFileId() throws FileStorageException {
        try {
            TemporaryStorage.FileInfo fileInfo = delegate.createFile();
            fileInfo.getFile().delete();
            return fileInfo.getId();
        } catch (io.jmix.core.FileStorageException e) {
            throw new FileStorageException(e);
        }
    }

    @Nullable
    @Override
    public File getFile(UUID fileId) {
        return delegate.getFile(fileId);
    }

    @Nullable
    @Override
    public FileDescriptor getFileDescriptor(UUID fileId, String name) {
        File file = getFile(fileId);
        if (file == null) {
            return null;
        }
        Metadata metadata = AppBeans.get(Metadata.class);

        FileDescriptor fDesc = metadata.create(FileDescriptor.class);

        fDesc.setSize(file.length());
        fDesc.setExtension(FilenameUtils.getExtension(name));
        fDesc.setName(name);
        fDesc.setCreateDate(timeSource.currentTimestamp());

        return fDesc;
    }

    @Override
    public void deleteFile(UUID fileId) throws FileStorageException {
        try {
            delegate.deleteFile(fileId);
        } catch (io.jmix.core.FileStorageException e) {
            throw new FileStorageException(e);
        }
    }

    @Override
    public void deleteFileLink(String fileName) {
        delegate.deleteFileLink(fileName);
    }

    @Override
    public void putFileIntoStorage(UUID fileId, FileDescriptor fileDescr) throws FileStorageException {
        File file = getFile(fileId);
        if (file == null) {
            throw new FileStorageException(FileStorageException.Type.FILE_NOT_FOUND,
                    fileDescr.getName());
        }

        FileRef fileRef = fileStorage.toFileRef(fileDescr);
        try (InputStream io = new FileInputStream(file)) {
            fileStorage.getDelegate().saveStream(fileRef, io);
        } catch (FileNotFoundException e) {
            throw new FileStorageException(FileStorageException.Type.FILE_NOT_FOUND,
                    "Temp file is not found " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, fileDescr.getName());
        }

        deleteFile(fileId);
    }

    @Override
    public FileDescriptor putFileIntoStorage(TaskLifeCycle<Long> taskLifeCycle)
            throws FileStorageException, InterruptedException {
        checkNotNullArgument(taskLifeCycle);

        UUID fileId = (UUID) taskLifeCycle.getParams().get("fileId");
        String fileName = (String) taskLifeCycle.getParams().get("fileName");

        checkNotNull(fileId);
        checkNotNull(fileName);

        FileDescriptor fileDescriptor = getFileDescriptor(fileId, fileName);
        checkNotNull(fileDescriptor);
        putFileIntoStorage(fileId, fileDescriptor);

        return fileDescriptor;
    }
}