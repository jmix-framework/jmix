/*
 * Copyright 2025 Haulmont.
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

package filestorage;

import io.jmix.core.DataManager;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageLocator;
import io.jmix.restds.filestorage.RestFileStorage;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.BaseRestDsIntegrationTest;
import test_support.TestSupport;
import test_support.entity.Customer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class FileStorageTest extends BaseRestDsIntegrationTest {

    @Autowired
    DataManager dataManager;
    @Autowired
    FileStorageLocator fileStorageLocator;

    LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
    }

    @Test
    void testDefaultFileStorage() {
        FileStorage fileStorage = fileStorageLocator.getDefault();
        assertThat(fileStorage).isInstanceOf(RestFileStorage.class);
    }

    @Test
    void testUploadAndDownload() throws IOException {
        FileStorage fileStorage = fileStorageLocator.getByName("restService1-fs");

        ByteArrayInputStream uploadStream = new ByteArrayInputStream(("Some content: " + now).getBytes(StandardCharsets.UTF_8));
        FileRef fileRef = fileStorage.saveStream("doc1.txt", uploadStream);
        assertThat(fileRef.getStorageName()).isEqualTo("restService1-fs");
        assertThat(fileRef.getFileName()).isEqualTo("doc1.txt");

        try (InputStream downloadStream = fileStorage.openStream(fileRef)) {
            assertThat(downloadStream).isInstanceOf(RestFileStorage.ResponseInputStream.class);
            String content = IOUtils.toString(downloadStream, StandardCharsets.UTF_8);
            assertThat(content).isEqualTo("Some content: " + now);
        }
    }

    @Test
    void testLoadFileRefAttribute() throws IOException {
        Customer customer = dataManager.load(Customer.class).id(TestSupport.UUID_1).one();

        FileRef fileRef = customer.getDocument();
        assertThat(fileRef).isNotNull();

        FileStorage fileStorage = fileStorageLocator.getByName(fileRef.getStorageName());
        assertThat(fileStorage).isInstanceOf(RestFileStorage.class);

        try (InputStream inputStream = fileStorage.openStream(fileRef)) {
            assertThat(inputStream).isNotNull();
            String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            assertThat(content).isNotBlank();
        }
    }

    @Test
    void testSaveFileRefAttribute() {
        FileStorage fileStorage = fileStorageLocator.getByName("restService1-fs");

        // Upload file
        ByteArrayInputStream uploadStream = new ByteArrayInputStream(("Some content: " + now).getBytes(StandardCharsets.UTF_8));
        FileRef fileRef = fileStorage.saveStream("doc1.txt", uploadStream);

        // Save file ref in entity
        Customer customer = dataManager.create(Customer.class);
        customer.setLastName("Customer " + now);
        customer.setDocument(fileRef);
        Customer savedCustomer = dataManager.save(customer);

        // File ref in the saved entity is the same and points to RestFileStorage
        assertThat(savedCustomer.getDocument()).isEqualTo(fileRef);

        // File ref in the loaded entity is the same and points to RestFileStorage
        Customer loadedCustomer = dataManager.load(Customer.class).id(customer.getId()).one();
        assertThat(loadedCustomer.getDocument()).isEqualTo(fileRef);
    }

    @Test
    void testFileExists() {
        FileStorage fileStorage = fileStorageLocator.getByName("restService1-fs");

        Customer customer = dataManager.load(Customer.class).id(TestSupport.UUID_1).one();
        FileRef fileRef = customer.getDocument();
        assertThat(fileStorage.fileExists(fileRef)).isTrue();

        FileRef nonExistentFileRef = new FileRef(fileStorage.getStorageName(), "/aaa/bbb.txt", "bbb.txt");
        assertThat(fileStorage.fileExists(nonExistentFileRef)).isFalse();
    }
}
