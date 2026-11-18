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

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.jmix.core.*;
import io.jmix.core.annotation.Internal;
import jakarta.annotation.PreDestroy;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
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

    @Value("${jmix.localfs.disable-path-check:false}")
    protected Boolean disablePathCheck;

    /**
     * @deprecated has no effect, see {@link #setImmutableFileStorage(boolean)}
     */
    @Deprecated(since = "3.2", forRemoval = true)
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

    /**
     * Saves the stream to the file denoted by the reference.
     * <p>
     * The parameters of the reference are handled as described in
     * {@link FileStorage#saveStream(String, InputStream, Map)}: a {@code jmix-sha-256} digest is verified against the
     * saved content if {@link CoreProperties#isFileDigestOnSaveEnabled()} is on and kept as it is otherwise, a
     * malformed digest and other keys starting with {@value FileDigest#RESERVED_PARAMETER_PREFIX} are rejected.
     * The reference is changed only when the digest is calculated and the reference has none: the digest is added
     * to it.
     * <p>
     * A file or directory that already exists at the path is never overwritten or deleted: the call fails with
     * {@link FileStorageException.Type#FILE_ALREADY_EXISTS}. If the save fails after the file has been created, for
     * whatever reason, the file is deleted.
     *
     * @param fileRef     file reference
     * @param inputStream input stream, must be closed in the calling code
     * @return size of the saved file in bytes
     * @throws IllegalArgumentException if the reference carries a reserved parameter other than the digest, or a
     *                                  malformed digest
     * @throws FileStorageException     if something goes wrong, the file already exists, or the content does not
     *                                  match the digest of the reference
     */
    public long saveStream(FileRef fileRef, InputStream inputStream) {
        FileDigest.checkReservedParameters(fileRef.getParameters());
        FileDigest expectedDigest = FileDigest.fromFileRef(fileRef);

        Path relativePath = getRelativePath(fileRef.getPath());

        Path[] roots = getStorageRoots();

        // Store to primary storage
        checkStorageDefined(roots, fileRef.getFileName());
        checkPrimaryStorageAccessible(roots, fileRef.getFileName());

        Path root = roots[0];
        Path path = root.resolve(relativePath);
        Path parentPath = path.getParent();
        if (parentPath == null) {
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION,
                    "Invalid storage root: " + path);
        }
        // Checked before any directory is created, so a path outside the root creates nothing
        if (isInvalidPath(path, root)) {
            log.error("Unable to save file. File '{}' is outside of root dir '{}': ", path, root);
            throw new FileStorageException(FileStorageException.Type.INVALID_PATH, path.toAbsolutePath().toString());
        }

        if (!parentPath.toFile().exists() && !parentPath.toFile().mkdirs()) {
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION,
                    "Cannot create directory: " + parentPath.toAbsolutePath());
        }

        checkFileExists(path);

        InputStream source = inputStream;
        FileDigestInputStream digestInputStream = null;
        if (coreProperties.isFileDigestOnSaveEnabled()) {
            digestInputStream = new FileDigestInputStream(inputStream);
            source = digestInputStream;
        }

        long size;
        long maxAllowedSize = properties.getMaxFileSize().toBytes();
        OutputStream outputStream = openNewFile(path);
        // The file has been created by this call, so it is deleted unless it is written and closed, whatever fails
        boolean written = false;
        try {
            try (outputStream) {
                size = IOUtils.copyLarge(source, outputStream, 0, maxAllowedSize);

                if (size >= maxAllowedSize && source.read() != IOUtils.EOF) {
                    throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION,
                            String.format("File is too large: '%s'. Max file size = %s MB is exceeded but there are unread bytes left.",
                                    path.toAbsolutePath(),
                                    properties.getMaxFileSize().toMegabytes()));
                }
                outputStream.flush();
//                writeLog(path, false);
            }
            written = true;
        } catch (IOException e) {
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, path.toAbsolutePath().toString(), e);
        } finally {
            if (!written) {
                deleteCreatedFile(path);
            }
        }

        if (digestInputStream != null) {
            FileDigest digest = digestInputStream.getDigest();
            if (expectedDigest == null) {
                digest.addTo(fileRef);
            } else if (!expectedDigest.equals(digest)) {
                deleteCreatedFile(path);
                log.warn("Content of '{}' does not match the passed digest: expected {}, actual {}",
                        path.toAbsolutePath(), expectedDigest, digest);
                throw new FileStorageException(FileStorageException.Type.DIGEST_MISMATCH,
                        path.toAbsolutePath().toString());
            }
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
                    Files.createDirectories(pathCopy.getParent());
                    // A file that already exists in the secondary root is kept, as in the primary one
                    Files.copy(path, pathCopy, StandardCopyOption.COPY_ATTRIBUTES);
                } catch (FileAlreadyExistsException e) {
                    log.error("Error saving {} into {} : the file already exists", fileRef.getFileName(), pathCopy);
                } catch (Exception e) {
                    log.error("Error saving {} into {} : {}", fileRef.getFileName(), pathCopy, e.getMessage());
                }
            });
        }

        return size;
    }

    /**
     * Creates the file and opens it for writing. Whatever exists at the path is kept: nothing has been created when
     * this fails, so nothing is deleted.
     */
    private OutputStream openNewFile(Path path) {
        try {
            return Files.newOutputStream(path, CREATE_NEW);
        } catch (IOException e) {
            // On Windows an existing directory gives AccessDeniedException rather than FileAlreadyExistsException
            if (e instanceof FileAlreadyExistsException || Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                throw new FileStorageException(FileStorageException.Type.FILE_ALREADY_EXISTS,
                        path.toAbsolutePath().toString(), e);
            }
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, path.toAbsolutePath().toString(), e);
        }
    }

    /**
     * Deletes a file this storage has just created, after the save failed. A failure to delete is logged, so that a
     * file left behind can be found.
     */
    private void deleteCreatedFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Unable to delete '{}' after an unsuccessful save", path.toAbsolutePath(), e);
        }
    }

    @Override
    public FileRef saveStream(String fileName, InputStream inputStream, Map<String, Object> parameters) {
        Path relativePath = createRelativeFilePath(fileName);
        Map<String, String> fileRefParams = Maps.toMap(parameters.keySet(), key -> parameters.get(key).toString());
        FileRef fileRef = new FileRef(storageName, pathToString(relativePath), fileName, fileRefParams);
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

            if (isInvalidPath(path, root)) {
                log.error("Unable to open file. File '{}' is outside of root dir '{}': ", path, root);
                continue;
            }

            if (!path.toFile().exists()) {
                log.error("File '{}' not found", path);
                continue;
            }

            try {
                inputStream = Files.newInputStream(path);
                break;
            } catch (IOException e) {
                log.error("Error opening input stream for {}", path, e);
            }
        }

        if (inputStream != null) {
            return inputStream;
        } else {
            throw new FileStorageException(FileStorageException.Type.FILE_NOT_FOUND, reference.toString());
        }
    }

    protected boolean isInvalidPath(Path filePath, Path rootDirectoryPath) {
        return !Boolean.TRUE.equals(disablePathCheck) && !isFileWithinRootDirectory(filePath, rootDirectoryPath);
    }

    protected boolean isFileWithinRootDirectory(Path filePath, Path rootDirectoryPath) {
        try {
            return resolveActualPath(filePath).startsWith(resolveActualPath(rootDirectoryPath));
        } catch (IOException e) {
            return false;
        }
    }

    protected Path resolveActualPath(Path path) throws IOException {
        Path absolutePath = path.toAbsolutePath();
        Path resolvedPath = absolutePath.getRoot();
        if (resolvedPath == null) {
            resolvedPath = absolutePath.getFileSystem().getPath("");
        }

        List<Path> pathParts = new ArrayList<>();
        for (Path pathPart : absolutePath) {
            pathParts.add(pathPart);
        }

        for (int i = 0; i < pathParts.size(); i++) {
            Path candidate = resolvedPath.resolve(pathParts.get(i).toString());

            if (Files.exists(candidate)) {
                resolvedPath = candidate.toRealPath();
                continue;
            }

            if (Files.isSymbolicLink(candidate)) {
                Path symbolicLinkTarget = Files.readSymbolicLink(candidate);
                Path targetPath = symbolicLinkTarget.isAbsolute()
                        ? symbolicLinkTarget
                        : resolvedPath.resolve(symbolicLinkTarget);
                resolvedPath = resolveActualPath(targetPath);
                continue;
            }

            for (int j = i; j < pathParts.size(); j++) {
                resolvedPath = resolvedPath.resolve(pathParts.get(j).toString());
            }
            return resolvedPath.normalize();
        }

        return resolvedPath.normalize();
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
            if (isInvalidPath(filePath, root)) {
                log.error("Unable to remove file. File '{}' is outside of root dir '{}': ", filePath, root);
                continue;
            }

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
            if (isInvalidPath(filePath, root)) {
                log.error("File '{}' is outside of root dir '{}': ", filePath, root);
                continue;
            }

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
        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
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
     * @deprecated has no effect: {@link #saveStream(FileRef, InputStream)} never overwrites or deletes a file or
     * directory that exists at the path, it fails with {@link FileStorageException.Type#FILE_ALREADY_EXISTS}
     */
    @Deprecated(since = "3.2", forRemoval = true)
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
