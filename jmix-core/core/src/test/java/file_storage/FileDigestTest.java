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

import io.jmix.core.FileDigest;
import io.jmix.core.FileDigestInputStream;
import io.jmix.core.FileRef;
import org.junit.jupiter.api.Test;
import test_support.app.TestFileStorage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileDigestTest {

    static final String ABC_HEX = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";
    static final String SOME_CONTENT_HEX = "290f493c44f5d63d06b374d0a5abd292fae38b92cab2fae5efefe1b0e9347f56";
    static final String EMPTY_HEX = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    @Test
    void fileDigestInputStream_readFully_sha256OfContent() throws IOException {
        try (FileDigestInputStream in = new FileDigestInputStream(stream("abc"))) {
            in.readAllBytes();

            FileDigest digest = in.getDigest();
            assertEquals(FileDigest.SHA_256, digest.getAlgorithm());
            assertEquals(ABC_HEX, digest.getValue());
            assertEquals(digest, in.getDigest());
        }
    }

    @Test
    void fileDigestInputStream_emptyStream_sha256OfEmpty() throws IOException {
        try (FileDigestInputStream in = new FileDigestInputStream(stream(""))) {
            in.readAllBytes();

            assertEquals(EMPTY_HEX, in.getDigest().getValue());
        }
    }

    @Test
    void fileDigestInputStream_skip_includesSkippedBytes() throws IOException {
        try (FileDigestInputStream in = new FileDigestInputStream(stream("abc"))) {
            assertEquals('a', in.read());
            assertEquals(1, in.skip(1));
            assertEquals('c', in.read());
            assertEquals(-1, in.read());

            assertEquals(ABC_HEX, in.getDigest().getValue());
        }
    }

    @Test
    void of_unsupportedAlgorithm_throws() {
        assertThrows(IllegalArgumentException.class, () -> FileDigest.of("sha-512", "00"));
    }

    @Test
    void fromFileRef_sha256Parameter_returnsLowercasedDigest() {
        FileRef reference = new FileRef("fs", "2026/11/02/x.txt", "x.txt",
                Map.of("jmix-sha-256", ABC_HEX.toUpperCase()));

        FileDigest digest = FileDigest.fromFileRef(reference);

        assertEquals(FileDigest.SHA_256, digest.getAlgorithm());
        assertEquals(ABC_HEX, digest.getValue());
    }

    @Test
    void fromFileRef_noSupportedParameter_returnsNull() {
        assertNull(FileDigest.fromFileRef(new FileRef("fs", "x.txt", "x.txt", Map.of("size", "3"))));
        assertNull(FileDigest.fromFileRef(new FileRef("fs", "x.txt", "x.txt", Map.of("sha-256", ABC_HEX))));
        assertNull(FileDigest.fromFileRef(new FileRef("fs", "x.txt", "x.txt", Map.of("jmix-sha-512", "00"))));
    }

    @Test
    void addTo_fileRefString_roundTrips() {
        FileRef reference = new FileRef("fs", "2026/11/02/x.txt", "Отчёт.txt",
                Map.of("a", "1", "sha-256", "app"));

        FileDigest.of(FileDigest.SHA_256, ABC_HEX).addTo(reference);

        assertTrue(reference.toString().endsWith("&jmix-sha-256=" + ABC_HEX));
        FileRef parsed = FileRef.fromString(reference.toString());
        assertEquals("Отчёт.txt", parsed.getFileName());
        assertEquals(Map.of("a", "1", "sha-256", "app", "jmix-sha-256", ABC_HEX), parsed.getParameters());
    }

    @Test
    void checkNoReservedParameters_reservedKey_throws() {
        for (String key : List.of("jmix-sha-256", "jmix-md5", "jmix-other")) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> FileDigest.checkNoReservedParameters(List.of("size", key)));
            assertTrue(e.getMessage().contains(key), e.getMessage());
        }
    }

    @Test
    void checkNoReservedParameters_otherKeys_passes() {
        assertDoesNotThrow(() -> FileDigest.checkNoReservedParameters(
                List.of("size", "sha-256", "md5", "crc32c", "Jmix-sha-256", "jmix")));
    }

    @Test
    void calculateDigest_defaultMethod_readsThroughOpenStream() {
        TestFileStorage fileStorage = new TestFileStorage("testFs");
        FileRef reference = fileStorage.saveStream("testfile", stream("some content"));

        FileDigest digest = fileStorage.calculateDigest(reference, FileDigest.SHA_256);

        assertEquals(SOME_CONTENT_HEX, digest.getValue());
    }

    @Test
    void calculateDigest_unsupportedAlgorithm_throwsBeforeOpeningStream() {
        AtomicInteger openStreamCalls = new AtomicInteger();
        TestFileStorage fileStorage = new TestFileStorage("testFs") {
            @Override
            public InputStream openStream(FileRef reference) {
                openStreamCalls.incrementAndGet();
                return super.openStream(reference);
            }
        };
        FileRef missing = new FileRef("testFs", "missing.txt", "missing.txt");

        assertThrows(IllegalArgumentException.class, () -> fileStorage.calculateDigest(missing, "md5"));
        assertEquals(0, openStreamCalls.get());
    }

    private static InputStream stream(String s) {
        return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
    }
}
