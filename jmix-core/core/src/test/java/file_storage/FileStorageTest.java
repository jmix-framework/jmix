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

package file_storage;

import com.google.common.io.ByteStreams;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageLocator;
import io.jmix.core.CoreConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.addon1.TestAddon1Configuration;
import test_support.app.TestAppConfiguration;
import test_support.app.TestFileStorage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CoreConfiguration.class, TestAddon1Configuration.class, TestAppConfiguration.class})
public class FileStorageTest {

    @Autowired
    TestFileStorage fileStorage;

    @Autowired
    FileStorageLocator fileStorageLocator;

    @Test
    void testSaveLoad() throws IOException {
        FileRef ref = fileStorage.saveStream("testfile", new ByteArrayInputStream("some content".getBytes()));

        InputStream inputStream = fileStorage.openStream(ref);

        byte[] storedFile = ByteStreams.toByteArray(inputStream);
        assertEquals("some content", new String(storedFile));
    }

    @Test
    void testLocator() {
        FileStorage fs1 = fileStorageLocator.getByName("testFs");
        fs1.saveStream("testfile", new ByteArrayInputStream(new byte[0]));

        // usage with concrete storage type
        TestFileStorage fs2 = fileStorageLocator.getByName("testFs2");
        fs2.saveStream("testfile", new ByteArrayInputStream(new byte[0]));

        //usage with default storage
        FileStorage fsTest = fileStorageLocator.getDefault();
        fsTest.saveStream("testfile", new ByteArrayInputStream(new byte[0]));

        assertSame(fs1, fsTest);
    }
}
