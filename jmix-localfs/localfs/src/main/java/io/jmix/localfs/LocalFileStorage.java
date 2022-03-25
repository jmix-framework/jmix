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

package io.jmix.localfs;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.jmix.core.CoreProperties;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageException;
import io.jmix.core.TimeSource;
import io.jmix.core.UuidProvider;
import io.jmix.core.annotation.Internal;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

@Internal
@Component("locfs_FileStorage")
public class LocalFileStorage implements FileStorage {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorage.class);

    public static final String DEFAULT_STORAGE_NAME = "fs";

    protected String storageName;
    protected String storageDir;

    @Autowired
    protected LocalFileStorageProperties properties;

    @Autowired
    protected CoreProperties coreProperties;

    @Autowired
    protected TimeSource timeSource;

    protected boolean isImmutableFileStorage;

    protected ExecutorService writeExecutor = Executors.newFixedThreadPool(5,
            new ThreadFactoryBuilder().setNameFormat("FileStorageWriter-%d").build());

    protected volatile Path[] storageRoots;

    public LocalFileStorage() {
        this(DEFAULT_STORAGE_NAME);
    }

    public LocalFileStorage(String storageName) {
        this.storageName = storageName;
    }

    /**
     * Optional constructor that allows specifying storage directory,
     * thus overriding {@link LocalFileStorageProperties#getStorageDir()} property.
     * <p>
     * It can be useful if there are more than one local file storage in an application,
     * and these storages should be using different dirs for storing files.
     */
    public LocalFileStorage(String storageName, String storageDir) {
        this(storageName);
        this.storageDir = storageDir;
    }

    @Override
    public String getStorageName() {
        return storageName;
    }

    protected String createUuidFilename(String fileName) {
        String extension = FilenameUtils.getExtension(fileName);
        if (StringUtils.isNotEmpty(extension)) {
            return UuidProvider.createUuid().toString() + "." + extension;
        } else {
            return UuidProvider.createUuid().toString();
        }
    }

    protected Path[] getStorageRoots() {
        if (storageRoots == null) {
            String storageDir = this.storageDir != null ? this.storageDir : properties.getStorageDir();
            if (StringUtils.isBlank(storageDir)) {
                String workDir = coreProperties.getWorkDir();
                Path dir = Paths.get(workDir, "filestorage");
                if (!dir.toFile().exists() && !dir.toFile().mkdirs()) {
                    throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION,
                            "Cannot create filestorage directory: " + dir.toAbsolutePath().toString());
                }
                storageRoots = new Path[]{dir};
            } else {
                List<Path> list = new ArrayList<>();
                for (String str : storageDir.split(",")) {
                    str = str.trim();
                    if (!StringUtils.isEmpty(str)) {
                        Path path = Paths.get(str);
                        if (!list.contains(path))
                            list.add(path);
                    }
                }
                storageRoots = list.toArray(new Path[0]);
            }
        }
        return storageRoots;
    }

    public long saveStream(FileRef fileRef, InputStream inputStream) {
        Path relativePath = getRelativePath(fileRef.getPath());

        Path[] roots = getStorageRoots();

        // Store to primary storage
        checkStorageDefined(roots, fileRef.getFileName());
        checkPrimaryStorageAccessible(roots, fileRef.getFileName());

        Path path = roots[0].resolve(relativePath);
        Path parentPath = path.getParent();
        if (parentPath == null) {
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION,
                    "Invalid storage root: " + path);
        }
        if (!parentPath.toFile().exists() && !parentPath.toFile().mkdirs()) {
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION,
                    "Cannot create directory: " + parentPath.toAbsolutePath());
        }

        checkFileExists(path);

        long size;
        try (OutputStream outputStream = Files.newOutputStream(path, CREATE_NEW)) {
            size = IOUtils.copyLarge(inputStream, outputStream);
            outputStream.flush();
//            writeLog(path, false);
        } catch (IOException e) {
            FileUtils.deleteQuietly(path.toFile());
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, path.toAbsolutePath().toString(), e);
        }

        // Copy file to secondary storages asynchronously
        for (int i = 1; i < roots.length; i++) {
            if (!roots[i].toFile().exists()) {
                log.error("Error saving {} into {} : directory doesn't exist", fileRef.getFileName(), roots[i]);
                continue;
            }

            Path pathCopy = roots[i].resolve(relativePath);

            writeExecutor.submit(() -> {
                try {
                    FileUtils.copyFile(path.toFile(), pathCopy.toFile(), true);
                } catch (Exception e) {
                    log.error("Error saving {} into {} : {}", fileRef.getFileName(), pathCopy, e.getMessage());
                }
            });
        }

        return size;
    }

    @Override
    public FileRef saveStream(String fileName, InputStream inputStream) {
        Path relativePath = createRelativeFilePath(fileName);
        FileRef fileRef = new FileRef(storageName, pathToString(relativePath), fileName);
        saveStream(fileRef, inputStream);
        return fileRef;
    }

    protected Path createRelativeFilePath(String fileName) {
        return createDateDirPath().resolve(createUuidFilename(fileName));
    }

    @Override
    public InputStream openStream(FileRef reference) {
        Path relativePath = getRelativePath(reference.getPath());

        Path[] roots = getStorageRoots();
        if (roots.length == 0) {
            log.error("No storage directories available");
            throw new FileStorageException(FileStorageException.Type.FILE_NOT_FOUND, reference.toString());
        }

        InputStream inputStream = null;
        for (Path root : roots) {
            Path path = root.resolve(relativePath);

            if (!path.toFile().exists()) {
                log.error("File " + path + " not found");
                continue;
            }

            try {
                inputStream = Files.newInputStream(path);
            } catch (IOException e) {
                log.error("Error opening input stream for " + path, e);
            }
        }

        if (inputStream != null) {
            return inputStream;
        } else {
            throw new FileStorageException(FileStorageException.Type.FILE_NOT_FOUND, reference.toString());
        }
    }

    @Override
    public void removeFile(FileRef reference) {
        Path[] roots = getStorageRoots();
        if (roots.length == 0) {
            log.error("No storage directories defined");
            return;
        }

        Path relativePath = getRelativePath(reference.getPath());
        for (Path root : roots) {
            Path filePath = root.resolve(relativePath);
            File file = filePath.toFile();
            if (file.exists()) {
                if (!file.delete()) {
                    throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION,
                            "Unable to delete file " + file.getAbsolutePath());
                }
            }
        }
    }

    @Override
    public boolean fileExists(FileRef reference) {
        Path[] roots = getStorageRoots();

        Path relativePath = getRelativePath(reference.getPath());
        for (Path root : roots) {
            Path filePath = root.resolve(relativePath);
            if (filePath.toFile().exists()) {
                return true;
            }
        }
        return false;
    }

    protected Path createDateDirPath() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(timeSource.currentTimestamp());
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return Paths.get(String.valueOf(year),
                StringUtils.leftPad(String.valueOf(month), 2, '0'),
                StringUtils.leftPad(String.valueOf(day), 2, '0'));
    }

    protected void checkFileExists(Path path) {
        if (Files.exists(path) && isImmutableFileStorage) {
            throw new FileStorageException(FileStorageException.Type.FILE_ALREADY_EXISTS,
                    path.toAbsolutePath().toString());
        }
    }

    protected void checkDirectoryExists(Path dir) {
        if (!dir.toFile().exists())
            throw new FileStorageException(FileStorageException.Type.STORAGE_INACCESSIBLE,
                    dir.toAbsolutePath().toString());
    }

    protected void checkPrimaryStorageAccessible(Path[] roots, String fileName) {
        if (!roots[0].toFile().exists() && !roots[0].toFile().mkdirs()) {
            log.error("Inaccessible primary storage at {}", roots[0]);
            throw new FileStorageException(FileStorageException.Type.STORAGE_INACCESSIBLE, fileName);
        }
    }

    protected void checkStorageDefined(Path[] roots, String fileName) {
        if (roots.length == 0) {
            log.error("No storage directories defined");
            throw new FileStorageException(FileStorageException.Type.STORAGE_INACCESSIBLE, fileName);
        }
    }

    /**
     * This method is mostly needed for compatibility with an old API.
     * <p>
     * If {@link #isImmutableFileStorage} is false then {@link #saveStream(FileRef, InputStream)}
     * will be overwriting existing files.
     */
    public void setImmutableFileStorage(boolean immutableFileStorage) {
        isImmutableFileStorage = immutableFileStorage;
    }

    /**
     * Converts string path to {@link Path}.
     */
    protected Path getRelativePath(String path) {
        String[] parts = path.split("/", 4);
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid path");
        }
        return Paths.get(parts[0], parts[1], parts[2], parts[3]);
    }

    /**
     * Converts path to a uniform string representation ("yyyy/mm/dd/uuid.ext").
     */
    protected String pathToString(Path path) {
        return path.toString().replace('\\', '/');
    }

    @PreDestroy
    protected void stopWriteExecutor() {
        writeExecutor.shutdown();
    }

}
