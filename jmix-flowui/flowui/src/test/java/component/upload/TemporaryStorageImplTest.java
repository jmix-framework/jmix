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

package component.upload;

import io.jmix.core.CoreProperties;
import io.jmix.flowui.upload.TemporaryStorage;
import io.jmix.flowui.upload.TemporaryStorageImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TemporaryStorageImplTest {

    /**
     * Reproduces the race in temp directory creation: several files uploaded in parallel
     * used to hit a non-atomic {@code !dir.exists() && !dir.mkdirs()} check, so all threads
     * but one failed with "Cannot create temp directory" while the directory did not exist yet.
     */
    @Test
    void testConcurrentCreateFileWhenTempDirDoesNotExist(@TempDir Path baseDir)
            throws InterruptedException {
        // A not-yet-existing subdirectory: the directory must be created by createFile() itself.
        File tempDir = baseDir.resolve("jmix-temp").toFile();
        assertTrue(!tempDir.exists());

        CoreProperties coreProperties = mock(CoreProperties.class);
        when(coreProperties.getTempDir()).thenReturn(tempDir.getAbsolutePath());

        TemporaryStorageImpl storage = new TemporaryStorageImpl();
        storage.setCoreProperties(coreProperties);

        int threads = 16;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        List<TemporaryStorage.FileInfo> created = new CopyOnWriteArrayList<>();
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    created.add(storage.createFile());
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Release all threads at once to maximize contention on directory creation.
        startLatch.countDown();
        assertTrue(doneLatch.await(30, TimeUnit.SECONDS), "Timed out waiting for uploads");
        executor.shutdownNow();

        assertTrue(errors.isEmpty(), () -> "Concurrent createFile() failed: " + errors);
        assertEquals(threads, created.size());
        assertTrue(tempDir.isDirectory());
    }
}
