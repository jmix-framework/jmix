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
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CoreConfiguration.class, TestAddon1Configuration.class, TestAppConfiguration.class})
public class FileStorageTest {

    @Autowired
    TestFileStorage fileStorage;

    @Autowired
    FileStorageLocator fileStorageLocator;

    @Test
    void testSaveLoad() throws IOException {
        URI ref = fileStorage.createReference("testfile");
        fileStorage.saveStream(ref, new ByteArrayInputStream("some content".getBytes()));

        InputStream inputStream = fileStorage.openStream(ref);

        byte[] storedFile = ByteStreams.toByteArray(inputStream);
        assertEquals("some content", new String(storedFile));
    }

    @Test
    void testLocator() {
        // usage with typed reference
        FileStorage<URI, String> fs1 = fileStorageLocator.get("testFileStorage");
        URI ref1 = fs1.createReference("testfile");
        fs1.saveStream(ref1, new ByteArrayInputStream(new byte[0]));

        // usage with untyped reference
        FileStorage fs2 = fileStorageLocator.get("testFileStorage2");
        Object ref2 = fs2.createReference("testfile");
        //noinspection unchecked
        fs2.saveStream(ref2, new ByteArrayInputStream(new byte[0]));

        // usage with concrete storage type
        TestFileStorage fsTest = fileStorageLocator.getDefault();
        URI refTest = fsTest.createReference("testfile");
        fsTest.saveStream(refTest, new ByteArrayInputStream(new byte[0]));

        assertSame(fs1, fsTest);
    }
}
