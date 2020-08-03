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

package io.jmix.fsfilestorage;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.jmix.core.CoreProperties;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageException;
import io.jmix.core.TimeSource;
import io.jmix.core.UuidProvider;
import io.jmix.core.common.util.URLEncodeUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

@Component("fsfs_FileStorage")
public class FileSystemFileStorage implements FileStorage<URI, String> {

    private static final Logger log = LoggerFactory.getLogger(FileSystemFileStorage.class);

    @Autowired
    protected FileSystemFileStorageProperties properties;

    @Autowired
    protected CoreProperties coreProperties;

    @Autowired
    protected TimeSource timeSource;

    protected boolean isImmutableFileStorage;

    protected ExecutorService writeExecutor = Executors.newFixedThreadPool(5,
            new ThreadFactoryBuilder().setNameFormat("FileStorageWriter-%d").build());

    protected volatile Path[] storageRoots;

    @PostConstruct
    public void init() {
        this.isImmutableFileStorage = properties.isImmutableFileStorage();
    }

    @Override
    public Class<URI> getReferenceType() {
        return URI.class;
    }

    @Override
    public URI createReference(@Nullable String filename) {
        filename = Objects.toString(filename, StringUtils.EMPTY);
        //reference = yyyy/mm/dd/uuid
        String reference = createDateDir() + "/" + createUuidFilename(filename);
        //if the filename is given, add it as an additional info
        if (StringUtils.isNotEmpty(filename)) {
            reference += "*" + URLEncodeUtils.encodeUtf8(filename);
        }
        try {
            return new URI(reference);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns the original filename for the file located by the given reference
     * or an empty string if the filename is not included in the reference.
     * <p>
     * The original filename is passed as an argument to {@link #createReference(String)}.
     */
    @Override
    public String getFileInfo(URI reference) {
        String[] parts = getReferenceParts(reference);
        String encodedFilename = StringUtils.EMPTY;
        if (parts.length > 1) {
            encodedFilename = parts[1];
        }
        return URLEncodeUtils.decodeUtf8(encodedFilename);
    }

    protected String createUuidFilename(String fileInfo) {
        String extension = FilenameUtils.getExtension(fileInfo);
        if (StringUtils.isNotEmpty(extension)) {
            return UuidProvider.createUuid().toString() + "." + extension;
        } else {
            return UuidProvider.createUuid().toString();
        }
    }

    protected Path[] getStorageRoots() {
        if (storageRoots == null) {
            String conf = properties.getStorageDir();
            if (StringUtils.isBlank(conf)) {
                String workDir = coreProperties.getWorkDir();
                Path dir = Paths.get(workDir, "filestorage");
                dir.toFile().mkdirs();
                storageRoots = new Path[]{dir};
            } else {
                List<Path> list = new ArrayList<>();
                for (String str : conf.split(",")) {
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

    @Override
    public long saveStream(URI reference, InputStream inputStream) {
        Path relativePath = getRelativePathFromURI(reference);

        Path[] roots = getStorageRoots();

        // Store to primary storage

        checkStorageDefined(roots, reference);
        checkPrimaryStorageAccessible(roots, reference);

        Path path = roots[0].resolve(relativePath);
        path.getParent().toFile().mkdirs();

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
                log.error("Error saving {} into {} : directory doesn't exist", reference, roots[i]);
                continue;
            }

            Path pathCopy = roots[i].resolve(relativePath);

            writeExecutor.submit(() -> {
                try {
                    FileUtils.copyFile(path.toFile(), pathCopy.toFile(), true);
                } catch (Exception e) {
                    log.error("Error saving {} into {} : {}", reference, pathCopy, e.getMessage());
                }
            });
        }

        return size;
    }

    @Override
    public InputStream openStream(URI reference) {
        Path relativePath = getRelativePathFromURI(reference);

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
    public void removeFile(URI reference) {
        Path[] roots = getStorageRoots();
        if (roots.length == 0) {
            log.error("No storage directories defined");
            return;
        }

        Path relativePath = getRelativePathFromURI(reference);
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
    public boolean fileExists(URI reference) {
        Path[] roots = getStorageRoots();

        Path relativePath = getRelativePathFromURI(reference);
        for (Path root : roots) {
            Path filePath = root.resolve(relativePath);
            if (filePath.toFile().exists()) {
                return true;
            }
        }
        return false;
    }

    public String createDateDir() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(timeSource.currentTimestamp());
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return year + "/"
                + StringUtils.leftPad(String.valueOf(month), 2, '0') + "/"
                + StringUtils.leftPad(String.valueOf(day), 2, '0');
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

    protected void checkPrimaryStorageAccessible(Path[] roots, URI reference) {
        if (!roots[0].toFile().exists()) {
            log.error("Inaccessible primary storage at {}", roots[0]);
            throw new FileStorageException(FileStorageException.Type.STORAGE_INACCESSIBLE, getFileInfo(reference));
        }
    }

    protected void checkStorageDefined(Path[] roots, URI reference) {
        if (roots.length == 0) {
            log.error("No storage directories defined");
            throw new FileStorageException(FileStorageException.Type.STORAGE_INACCESSIBLE, getFileInfo(reference));
        }
    }

    protected String[] getReferenceParts(URI reference) {
        String path = reference.getRawPath();
        return path.split("\\*", 2);
    }

    /**
     * Returns relative path to the file.
     */
    protected Path getRelativePathFromURI(URI encodedReference) {
        String rawReference = getReferenceParts(encodedReference)[0];
        String[] parts = rawReference.split("/", 4);
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid URI reference format");
        }
        return Paths.get(parts[0], parts[1], parts[2], parts[3]);
    }

    @PreDestroy
    protected void stopWriteExecutor() {
        writeExecutor.shutdown();
    }

}
