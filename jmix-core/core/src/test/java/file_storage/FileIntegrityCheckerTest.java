/*
 * Copyright 2026 Haulmont.
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

package file_storage;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.FileDigest;
import io.jmix.core.FileIntegrityCheckResult;
import io.jmix.core.FileIntegrityChecker;
import io.jmix.core.FileIntegrityStatus;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageException;
import io.jmix.core.FileStorageLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.addon1.TestAddon1Configuration;
import test_support.app.TestAppConfiguration;
import test_support.app.TestFailingFileStorage;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CoreConfiguration.class, TestAddon1Configuration.class, TestAppConfiguration.class})
public class FileIntegrityCheckerTest {

    static final String ABC = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";
    static final String SOME_CONTENT = "290f493c44f5d63d06b374d0a5abd292fae38b92cab2fae5efefe1b0e9347f56";

    static final String DIGEST_KEY = "jmix-sha-256";

    @Autowired
    FileIntegrityChecker fileIntegrityChecker;

    @Autowired
    FileStorageLocator fileStorageLocator;

    @Autowired
    TestFailingFileStorage failingFileStorage;

    @AfterEach
    void tearDown() {
        failingFileStorage.setFileExists(true);
        failingFileStorage.setFileExistsFails(false);
    }

    @Test
    void check_digestMatches_valid() {
        FileRef reference = save("digestMatches.txt", "some content", Map.of(DIGEST_KEY, SOME_CONTENT));
        String referenceString = reference.toString();

        FileIntegrityCheckResult result = fileIntegrityChecker.check(reference);

        assertEquals(FileIntegrityStatus.VALID, result.getStatus());
        assertEquals(FileDigest.of(FileDigest.SHA_256, SOME_CONTENT), result.getExpected());
        assertEquals(result.getExpected(), result.getActual());
        assertEquals(reference, result.getFileRef());
        assertEquals(referenceString, reference.toString());
    }

    @Test
    void check_contentDiffers_modified() {
        FileRef reference = save("contentDiffers.txt", "some content", Map.of(DIGEST_KEY, ABC));

        FileIntegrityCheckResult result = fileIntegrityChecker.check(reference);

        assertEquals(FileIntegrityStatus.MODIFIED, result.getStatus());
        assertEquals(ABC, result.getExpected().getValue());
        assertEquals(SOME_CONTENT, result.getActual().getValue());
    }

    @Test
    void check_uppercaseReference_valid() {
        FileRef reference = save("uppercaseReference.txt", "some content",
                Map.of(DIGEST_KEY, SOME_CONTENT.toUpperCase()));

        FileIntegrityCheckResult result = fileIntegrityChecker.check(reference);

        assertEquals(FileIntegrityStatus.VALID, result.getStatus());
    }

    @Test
    void check_malformedReference_modified() {
        FileRef reference = save("malformedReference.txt", "some content", Map.of(DIGEST_KEY, "not-a-digest"));

        FileIntegrityCheckResult result = fileIntegrityChecker.check(reference);

        assertEquals(FileIntegrityStatus.MODIFIED, result.getStatus());
        assertEquals("not-a-digest", result.getExpected().getValue());
        assertEquals(SOME_CONTENT, result.getActual().getValue());
    }

    @Test
    void check_noReference_noReference() {
        FileRef reference = save("noReference.txt", "some content", Map.of());

        FileIntegrityCheckResult result = fileIntegrityChecker.check(reference);

        assertEquals(FileIntegrityStatus.NO_REFERENCE, result.getStatus());
        assertNull(result.getExpected());
        assertNull(result.getActual());
    }

    @Test
    void check_applicationSha256Parameter_noReference() {
        FileRef reference = save("applicationSha256Parameter.txt", "some content", Map.of("sha-256", ABC));

        FileIntegrityCheckResult result = fileIntegrityChecker.check(reference);

        assertEquals(FileIntegrityStatus.NO_REFERENCE, result.getStatus());
        assertNull(result.getExpected());
        assertNull(result.getActual());
    }

    @Test
    void check_fileRemoved_missing() {
        FileRef reference = save("fileRemoved.txt", "some content", Map.of(DIGEST_KEY, SOME_CONTENT));
        fileStorage().removeFile(reference);

        FileIntegrityCheckResult result = fileIntegrityChecker.check(reference);

        assertEquals(FileIntegrityStatus.MISSING, result.getStatus());
        assertEquals(SOME_CONTENT, result.getExpected().getValue());
        assertNull(result.getActual());
    }

    @Test
    void check_fileRemovedWithoutReference_missing() {
        FileRef reference = save("fileRemovedWithoutReference.txt", "some content", Map.of());
        fileStorage().removeFile(reference);

        FileIntegrityCheckResult result = fileIntegrityChecker.check(reference);

        assertEquals(FileIntegrityStatus.MISSING, result.getStatus());
        assertNull(result.getExpected());
        assertNull(result.getActual());
    }

    @Test
    void check_readFailsForExistingFile_throws() {
        FileRef reference = new FileRef("testFsFailing", "x", "x.txt", Map.of(DIGEST_KEY, ABC));

        FileStorageException exception = assertThrows(FileStorageException.class,
                () -> fileIntegrityChecker.check(reference));

        assertEquals(FileStorageException.Type.IO_EXCEPTION, exception.getType());
    }

    @Test
    void check_readFailsForMissingFile_missing() {
        FileRef reference = new FileRef("testFsFailing", "x", "x.txt", Map.of(DIGEST_KEY, ABC));
        failingFileStorage.setFileExists(false);

        FileIntegrityCheckResult result = fileIntegrityChecker.check(reference);

        assertEquals(FileIntegrityStatus.MISSING, result.getStatus());
        assertEquals(ABC, result.getExpected().getValue());
        assertNull(result.getActual());
    }

    @Test
    void check_readAndFileExistsFail_throwsReadFailureWithSuppressedFileExistsFailure() {
        FileRef reference = new FileRef("testFsFailing", "x", "x.txt", Map.of(DIGEST_KEY, ABC));
        failingFileStorage.setFileExistsFails(true);

        FileStorageException exception = assertThrows(FileStorageException.class,
                () -> fileIntegrityChecker.check(reference));

        assertEquals(FileStorageException.Type.IO_EXCEPTION, exception.getType());
        assertEquals(1, exception.getSuppressed().length);
        FileStorageException suppressed = (FileStorageException) exception.getSuppressed()[0];
        assertEquals(FileStorageException.Type.STORAGE_INACCESSIBLE, suppressed.getType());
    }

    @Test
    void check_unknownStorage_throws() {
        FileRef reference = new FileRef("noSuchFs", "x", "x.txt");

        assertThrows(IllegalArgumentException.class, () -> fileIntegrityChecker.check(reference));
    }

    FileRef save(String fileName, String content, Map<String, Object> parameters) {
        return fileStorage().saveStream(fileName,
                new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), parameters);
    }

    FileStorage fileStorage() {
        return fileStorageLocator.getByName("testFs");
    }
}
