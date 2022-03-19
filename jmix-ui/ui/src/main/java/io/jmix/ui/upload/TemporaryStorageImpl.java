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

package io.jmix.ui.upload;

import io.jmix.core.CoreProperties;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageException;
import io.jmix.core.FileStorageLocator;
import io.jmix.core.TimeSource;
import io.jmix.core.UuidProvider;
import io.jmix.core.annotation.Internal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@Internal
@Component("ui_TemporaryStorage")
public class TemporaryStorageImpl implements TemporaryStorage {

    private final Logger log = LoggerFactory.getLogger(TemporaryStorageImpl.class);

    protected Map<UUID, File> tempFiles = new ConcurrentHashMap<>();

    /**
     * Upload buffer size.
     * Default: 64 KB
     */
    protected static final int BUFFER_SIZE = 64 * 1024;

    protected String tempDir;

    @Autowired
    protected TimeSource timeSource;

    @Autowired
    protected FileStorageLocator fileStorageLocator;

    @Autowired
    public void setCoreProperties(CoreProperties coreProperties) {
        tempDir = coreProperties.getTempDir();
    }

    @Override
    public UUID saveFile(byte[] data) {
        checkNotNullArgument(data, "No file content");

        UUID uuid = UuidProvider.createUuid();
        File dir = new File(tempDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION,
                    "Cannot create temp directory: " + dir.getAbsolutePath());
        }
        File file = new File(dir, uuid.toString());
        try {
            if (file.exists()) {
                throw new FileStorageException(FileStorageException.Type.FILE_ALREADY_EXISTS, file.getAbsolutePath());
            }

            try (FileOutputStream os = new FileOutputStream(file)) {
                os.write(data);
            }
            tempFiles.put(uuid, file);
        } catch (RuntimeException | IOException e) {
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, file.getAbsolutePath());
        }

        return uuid;
    }

    @Override
    public UUID saveFile(InputStream stream, UploadProgressListener listener) {
        checkNotNullArgument(stream, "Null input stream for save file");

        UUID uuid = UuidProvider.createUuid();
        File dir = new File(tempDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION,
                    "Cannot create temp directory: " + dir.getAbsolutePath());
        }
        File file = new File(dir, uuid.toString());
        if (file.exists()) {
            throw new FileStorageException(FileStorageException.Type.FILE_ALREADY_EXISTS, file.getAbsolutePath());
        }

        try {
            boolean failed = false;
            try (FileOutputStream fileOutput = new FileOutputStream(file)) {
                byte buffer[] = new byte[BUFFER_SIZE];
                int bytesRead;
                int totalBytes = 0;
                while ((bytesRead = stream.read(buffer)) > 0) {
                    fileOutput.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                    if (listener != null)
                        listener.progressChanged(uuid, totalBytes);
                }
            } catch (Exception ex) {
                failed = true;
                throw ex;
            } finally {
                if (!failed)
                    tempFiles.put(uuid, file);
            }
        } catch (Exception ex) {
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, file.getAbsolutePath(), ex);
        }
        return uuid;
    }

    @Override
    public FileInfo createFile() {
        return createFileInternal();
    }

    protected FileInfo createFileInternal() {
        UUID uuid = UuidProvider.createUuid();
        File dir = new File(tempDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION,
                    "Cannot create temp directory: " + dir.getAbsolutePath());
        }
        File file = new File(dir, uuid.toString());

        if (file.exists()) {
            throw new FileStorageException(FileStorageException.Type.FILE_ALREADY_EXISTS, file.getAbsolutePath());
        }

        try {
            if (file.createNewFile())
                tempFiles.put(uuid, file);
            else
                throw new FileStorageException(FileStorageException.Type.FILE_ALREADY_EXISTS, file.getAbsolutePath());
        } catch (IOException ex) {
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, file.getAbsolutePath());
        }

        return new FileInfo(file, uuid);
    }

    @Override
    public File getFile(UUID fileId) {
        return tempFiles.get(fileId);
    }

    @Override
    public void deleteFile(UUID fileId) {
        File file = tempFiles.remove(fileId);
        if (file != null) {
            if (file.exists()) {
                boolean res = file.delete();
                if (!res)
                    log.warn("Could not delete temp file " + file.getAbsolutePath());
            }
        }
    }

    @Override
    public void deleteFileLink(String fileName) {
        Map<UUID, File> clonedFileMap = new HashMap<>(tempFiles);
        Iterator<Map.Entry<UUID, File>> iterator = clonedFileMap.entrySet().iterator();
        UUID forDelete = null;
        while ((iterator.hasNext()) && (forDelete == null)) {
            Map.Entry<UUID, File> fileEntry = iterator.next();
            if (fileEntry.getValue().getAbsolutePath().equals(fileName)) {
                forDelete = fileEntry.getKey();
            }
        }

        if (forDelete != null) {
            tempFiles.remove(forDelete);
        }
    }

    @Override
    public FileRef putFileIntoStorage(UUID fileId, String fileName, FileStorage fileStorage) {
        File file = getFile(fileId);
        if (file == null) {
            throw new FileStorageException(FileStorageException.Type.FILE_NOT_FOUND, fileName);
        }

        FileRef fileRef;
        try (InputStream io = new FileInputStream(file)) {
            fileRef = fileStorage.saveStream(fileName, io);
        } catch (FileNotFoundException e) {
            throw new FileStorageException(FileStorageException.Type.FILE_NOT_FOUND,
                    "Temp file is not found " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, fileName);
        }

        deleteFile(fileId);
        return fileRef;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FileRef putFileIntoStorage(UUID fileId, String fileName) {
        FileStorage defaultFileStorage = fileStorageLocator.getDefault();
        return putFileIntoStorage(fileId, fileName, defaultFileStorage);
    }

    public void clearTempDirectory() {
        try {
            File dir = new File(tempDir);
            File[] files = dir.listFiles();
            if (files == null)
                throw new IllegalStateException("Not a directory: " + tempDir);
            Date currentDate = timeSource.currentTimestamp();
            for (File file : files) {
                Date fileDate = new Date(file.lastModified());
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(fileDate);
                calendar.add(Calendar.DAY_OF_YEAR, 2);
                if (currentDate.compareTo(calendar.getTime()) > 0) {
                    deleteFileLink(file.getAbsolutePath());
                    if (!file.delete()) {
                        log.warn(String.format("Could not remove temp file %s", file.getName()));
                    }
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    public String showTempFiles() {
        StringBuilder builder = new StringBuilder();
        Map<UUID, File> clonedFileMap = new HashMap<>(tempFiles);
        for (Map.Entry<UUID, File> fileEntry : clonedFileMap.entrySet()) {
            builder.append(fileEntry.getKey().toString()).append(" | ");
            Date lastModified = new Date(fileEntry.getValue().lastModified());
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            builder.append(formatter.format(lastModified)).append("\n");
        }
        return builder.toString();
    }
}