/*
 * Copyright 2021 Haulmont.
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

package indexing;

import com.fasterxml.jackson.databind.JsonNode;
import io.jmix.core.*;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.search.index.EntityIndexer;
import org.elasticsearch.action.bulk.BulkRequest;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.*;
import test_support.entity.TestEmbeddableEntity;
import test_support.entity.TestEnum;
import test_support.entity.indexing.*;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {IndexingTestConfiguration.class}
)
public class EntityIndexingTest {

    @Autowired
    protected EntityIndexer entityIndexer;
    @Autowired
    protected TestBulkRequestsTracker bulkRequestsTracker;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected SystemAuthenticator authenticator;
    @Autowired
    protected IdSerialization idSerialization;
    @Autowired
    protected TestFileStorage fileStorage;

    @BeforeEach
    public void setUp() {
        bulkRequestsTracker.clear();
        authenticator.begin();
    }

    @AfterEach
    public void tearDown() {
        authenticator.end();
    }


    @Test
    @DisplayName("Indexing of entity with UUID primary key")
    public void indexUuidPk() {
        TestUuidPkEntity entity = metadata.create(TestUuidPkEntity.class);
        entity.setName("UUID PK entity");
        dataManager.save(entity);

        JsonNode jsonNode = TestJsonUtils.readJsonFromFile("indexing/test_content_uuid_pk");
        TestBulkRequestIndexActionValidationData expectedIndexAction = new TestBulkRequestIndexActionValidationData(
                "search_index_test_uuidpkentity",
                idSerialization.idToString(Id.of(entity)),
                jsonNode
        );
        TestBulkRequestValidationData expectedData = new TestBulkRequestValidationData(
                Collections.singletonList(expectedIndexAction),
                Collections.emptyList());

        entityIndexer.index(entity);
        List<BulkRequest> bulkRequests = bulkRequestsTracker.getBulkRequests();

        TestBulkRequestValidationResult result = TestBulkRequestValidator.validate(Collections.singletonList(expectedData), bulkRequests);
        Assert.assertFalse(result.toString(), result.hasFailures());
    }

    @Test
    @DisplayName("Indexing of entity with Long primary key")
    public void indexLongPk() {
        TestLongPkEntity entity = metadata.create(TestLongPkEntity.class);
        entity.setName("Long PK entity");
        dataManager.save(entity);

        JsonNode jsonNode = TestJsonUtils.readJsonFromFile("indexing/test_content_long_pk");
        TestBulkRequestIndexActionValidationData expectedIndexAction = new TestBulkRequestIndexActionValidationData(
                "search_index_test_longpkentity",
                idSerialization.idToString(Id.of(entity)),
                jsonNode
        );
        TestBulkRequestValidationData expectedData = new TestBulkRequestValidationData(
                Collections.singletonList(expectedIndexAction),
                Collections.emptyList()
        );

        entityIndexer.index(entity);
        List<BulkRequest> bulkRequests = bulkRequestsTracker.getBulkRequests();

        TestBulkRequestValidationResult result = TestBulkRequestValidator.validate(Collections.singletonList(expectedData), bulkRequests);
        Assert.assertFalse(result.toString(), result.hasFailures());
    }

    @Test
    @DisplayName("Indexing of entity with String primary key")
    public void indexStringPk() {
        TestStringPkEntity entity = metadata.create(TestStringPkEntity.class);
        entity.setName("String PK entity");
        entity.setId("string_pk_1");
        dataManager.save(entity);

        JsonNode jsonNode = TestJsonUtils.readJsonFromFile("indexing/test_content_string_pk");
        TestBulkRequestIndexActionValidationData expectedIndexAction = new TestBulkRequestIndexActionValidationData(
                "search_index_test_stringpkentity",
                idSerialization.idToString(Id.of(entity)),
                jsonNode
        );
        TestBulkRequestValidationData expectedData = new TestBulkRequestValidationData(Collections.singletonList(expectedIndexAction), Collections.emptyList());

        entityIndexer.index(entity);
        List<BulkRequest> bulkRequests = bulkRequestsTracker.getBulkRequests();

        TestBulkRequestValidationResult result = TestBulkRequestValidator.validate(Collections.singletonList(expectedData), bulkRequests);
        Assert.assertFalse(result.toString(), result.hasFailures());
    }

    @Test
    @DisplayName("Indexing of entity with Composite primary key")
    public void indexCompositePk() {
        TestCompositeKey compositeKey = metadata.create(TestCompositeKey.class);
        compositeKey.setPkName("pkName");
        compositeKey.setPkVersion(1L);
        TestCompositePkEntity entity = metadata.create(TestCompositePkEntity.class);
        entity.setName("Composite PK entity");
        entity.setId(compositeKey);
        dataManager.save(entity);

        JsonNode jsonNode = TestJsonUtils.readJsonFromFile("indexing/test_content_composite_pk");
        TestBulkRequestIndexActionValidationData expectedIndexAction = new TestBulkRequestIndexActionValidationData(
                "search_index_test_compositepkentity",
                idSerialization.idToString(Id.of(entity)),
                jsonNode
        );
        TestBulkRequestValidationData expectedData = new TestBulkRequestValidationData(Collections.singletonList(expectedIndexAction), Collections.emptyList());

        entityIndexer.index(entity);
        List<BulkRequest> bulkRequests = bulkRequestsTracker.getBulkRequests();

        TestBulkRequestValidationResult result = TestBulkRequestValidator.validate(Collections.singletonList(expectedData), bulkRequests);
        Assert.assertFalse(result.toString(), result.hasFailures());
    }

    @Test
    @DisplayName("Indexing of entity with various textual properties")
    public void indexTextualContent() {
        // Sub References
        TestTextSubRefEntity oneToOneSubRef1 = metadata.create(TestTextSubRefEntity.class);
        oneToOneSubRef1.setName("oneToOneSubRef1");
        TestTextSubRefEntity oneToOneSubRef2 = metadata.create(TestTextSubRefEntity.class);
        oneToOneSubRef2.setName("oneToOneSubRef2");
        TestTextSubRefEntity oneToOneSubRef3 = metadata.create(TestTextSubRefEntity.class);
        oneToOneSubRef3.setName("oneToOneSubRef3");

        TestTextSubRefEntity oneToManySubRef11 = metadata.create(TestTextSubRefEntity.class);
        oneToManySubRef11.setName("oneToManySubRef11");
        TestTextSubRefEntity oneToManySubRef12 = metadata.create(TestTextSubRefEntity.class);
        oneToManySubRef12.setName("oneToManySubRef12");

        TestTextSubRefEntity oneToManySubRef21 = metadata.create(TestTextSubRefEntity.class);
        oneToManySubRef21.setName("oneToManySubRef21");
        TestTextSubRefEntity oneToManySubRef22 = metadata.create(TestTextSubRefEntity.class);
        oneToManySubRef22.setName("oneToManySubRef22");

        TestTextSubRefEntity oneToManySubRef31 = metadata.create(TestTextSubRefEntity.class);
        oneToManySubRef31.setName("oneToManySubRef31");
        TestTextSubRefEntity oneToManySubRef32 = metadata.create(TestTextSubRefEntity.class);
        oneToManySubRef32.setName("oneToManySubRef32");

        // References
        TestTextRefEntity oneToOneRef = metadata.create(TestTextRefEntity.class);
        oneToOneRef.setName("oneToOneRef");
        oneToOneRef.setOneToOneRef(oneToOneSubRef1);
        oneToOneRef.setOneToManyRef(Arrays.asList(oneToManySubRef11, oneToManySubRef12));
        oneToManySubRef11.setManyToOneRef(oneToOneRef);
        oneToManySubRef12.setManyToOneRef(oneToOneRef);

        TestTextRefEntity oneToManyRef1 = metadata.create(TestTextRefEntity.class);
        oneToManyRef1.setName("oneToManyRef1");
        oneToManyRef1.setOneToOneRef(oneToOneSubRef2);
        oneToManyRef1.setOneToManyRef(Arrays.asList(oneToManySubRef21, oneToManySubRef22));
        oneToManySubRef21.setManyToOneRef(oneToManyRef1);
        oneToManySubRef22.setManyToOneRef(oneToManyRef1);

        TestTextRefEntity oneToManyRef2 = metadata.create(TestTextRefEntity.class);
        oneToManyRef2.setName("oneToManyRef2");
        oneToManyRef2.setOneToOneRef(oneToOneSubRef3);
        oneToManyRef2.setOneToManyRef(Arrays.asList(oneToManySubRef31, oneToManySubRef32));
        oneToManySubRef31.setManyToOneRef(oneToManyRef2);
        oneToManySubRef32.setManyToOneRef(oneToManyRef2);

        // Root
        TestTextRootEntity rootEntity = metadata.create(TestTextRootEntity.class);
        rootEntity.setName("rootEntity");
        rootEntity.setOneToOneRef(oneToOneRef);
        rootEntity.setOneToManyRef(Arrays.asList(oneToManyRef1, oneToManyRef2));
        oneToManyRef1.setManyToOneRef(rootEntity);
        oneToManyRef2.setManyToOneRef(rootEntity);

        dataManager.save(
                rootEntity,
                oneToOneRef,
                oneToManyRef1, oneToManyRef2,
                oneToOneSubRef1, oneToOneSubRef2, oneToOneSubRef3,
                oneToManySubRef11, oneToManySubRef12,
                oneToManySubRef21, oneToManySubRef22,
                oneToManySubRef31, oneToManySubRef32
        );

        JsonNode jsonNode = TestJsonUtils.readJsonFromFile("indexing/test_content_text_properties");
        TestBulkRequestIndexActionValidationData expectedIndexAction = new TestBulkRequestIndexActionValidationData(
                "search_index_test_textrootentity",
                idSerialization.idToString(Id.of(rootEntity)),
                jsonNode
        );
        TestBulkRequestValidationData expectedData = new TestBulkRequestValidationData(
                Collections.singletonList(expectedIndexAction),
                Collections.emptyList());

        entityIndexer.index(rootEntity);
        List<BulkRequest> bulkRequests = bulkRequestsTracker.getBulkRequests();

        TestBulkRequestValidationResult result = TestBulkRequestValidator.validate(Collections.singletonList(expectedData), bulkRequests);
        Assert.assertFalse(result.toString(), result.hasFailures());
    }

    @Test
    @DisplayName("Indexing of entity with various enum properties")
    public void indexEnumContent() {
        // Sub References
        TestEnumSubRefEntity oneToOneSubRef1 = metadata.create(TestEnumSubRefEntity.class);
        oneToOneSubRef1.setName("oneToOneSubRef1");
        oneToOneSubRef1.setEnumValue(TestEnum.OPEN);
        TestEnumSubRefEntity oneToOneSubRef2 = metadata.create(TestEnumSubRefEntity.class);
        oneToOneSubRef2.setName("oneToOneSubRef2");
        oneToOneSubRef2.setEnumValue(TestEnum.OPEN);
        TestEnumSubRefEntity oneToOneSubRef3 = metadata.create(TestEnumSubRefEntity.class);
        oneToOneSubRef3.setName("oneToOneSubRef3");
        oneToOneSubRef3.setEnumValue(TestEnum.CLOSED);

        TestEnumSubRefEntity oneToManySubRef11 = metadata.create(TestEnumSubRefEntity.class);
        oneToManySubRef11.setName("oneToManySubRef11");
        oneToManySubRef11.setEnumValue(TestEnum.OPEN);
        TestEnumSubRefEntity oneToManySubRef12 = metadata.create(TestEnumSubRefEntity.class);
        oneToManySubRef12.setName("oneToManySubRef12");
        oneToManySubRef12.setEnumValue(TestEnum.CLOSED);

        TestEnumSubRefEntity oneToManySubRef21 = metadata.create(TestEnumSubRefEntity.class);
        oneToManySubRef21.setName("oneToManySubRef21");
        oneToManySubRef21.setEnumValue(TestEnum.OPEN);
        TestEnumSubRefEntity oneToManySubRef22 = metadata.create(TestEnumSubRefEntity.class);
        oneToManySubRef22.setName("oneToManySubRef22");
        oneToManySubRef22.setEnumValue(TestEnum.CLOSED);

        TestEnumSubRefEntity oneToManySubRef31 = metadata.create(TestEnumSubRefEntity.class);
        oneToManySubRef31.setName("oneToManySubRef31");
        oneToManySubRef31.setEnumValue(TestEnum.OPEN);
        TestEnumSubRefEntity oneToManySubRef32 = metadata.create(TestEnumSubRefEntity.class);
        oneToManySubRef32.setName("oneToManySubRef32");
        oneToManySubRef32.setEnumValue(TestEnum.CLOSED);

        // References
        TestEnumRefEntity oneToOneRef = metadata.create(TestEnumRefEntity.class);
        oneToOneRef.setName("oneToOneRef");
        oneToOneRef.setEnumValue(TestEnum.OPEN);
        oneToOneRef.setOneToOneRef(oneToOneSubRef1);
        oneToOneRef.setOneToManyRef(Arrays.asList(oneToManySubRef11, oneToManySubRef12));
        oneToManySubRef11.setManyToOneRef(oneToOneRef);
        oneToManySubRef12.setManyToOneRef(oneToOneRef);

        TestEnumRefEntity oneToManyRef1 = metadata.create(TestEnumRefEntity.class);
        oneToManyRef1.setName("oneToManyRef1");
        oneToManyRef1.setEnumValue(TestEnum.OPEN);
        oneToManyRef1.setOneToOneRef(oneToOneSubRef2);
        oneToManyRef1.setOneToManyRef(Arrays.asList(oneToManySubRef21, oneToManySubRef22));
        oneToManySubRef21.setManyToOneRef(oneToManyRef1);
        oneToManySubRef22.setManyToOneRef(oneToManyRef1);

        TestEnumRefEntity oneToManyRef2 = metadata.create(TestEnumRefEntity.class);
        oneToManyRef2.setName("oneToManyRef2");
        oneToManyRef2.setEnumValue(TestEnum.CLOSED);
        oneToManyRef2.setOneToOneRef(oneToOneSubRef3);
        oneToManyRef2.setOneToManyRef(Arrays.asList(oneToManySubRef31, oneToManySubRef32));
        oneToManySubRef31.setManyToOneRef(oneToManyRef2);
        oneToManySubRef32.setManyToOneRef(oneToManyRef2);

        // Root
        TestEnumRootEntity rootEntity = metadata.create(TestEnumRootEntity.class);
        rootEntity.setName("rootEntity");
        rootEntity.setEnumValue(TestEnum.OPEN);
        rootEntity.setOneToOneRef(oneToOneRef);
        rootEntity.setOneToManyRef(Arrays.asList(oneToManyRef1, oneToManyRef2));
        oneToManyRef1.setManyToOneRef(rootEntity);
        oneToManyRef2.setManyToOneRef(rootEntity);

        dataManager.save(
                rootEntity,
                oneToOneRef,
                oneToManyRef1, oneToManyRef2,
                oneToOneSubRef1, oneToOneSubRef2, oneToOneSubRef3,
                oneToManySubRef11, oneToManySubRef12,
                oneToManySubRef21, oneToManySubRef22,
                oneToManySubRef31, oneToManySubRef32
        );

        JsonNode jsonNode = TestJsonUtils.readJsonFromFile("indexing/test_content_enum_properties");
        TestBulkRequestIndexActionValidationData expectedIndexAction = new TestBulkRequestIndexActionValidationData(
                "search_index_test_enumrootentity",
                idSerialization.idToString(Id.of(rootEntity)),
                jsonNode
        );
        TestBulkRequestValidationData expectedData = new TestBulkRequestValidationData(
                Collections.singletonList(expectedIndexAction),
                Collections.emptyList());

        entityIndexer.index(rootEntity);
        List<BulkRequest> bulkRequests = bulkRequestsTracker.getBulkRequests();

        TestBulkRequestValidationResult result = TestBulkRequestValidator.validate(Collections.singletonList(expectedData), bulkRequests);
        Assert.assertFalse(result.toString(), result.hasFailures());
    }


    @Test
    @DisplayName("Indexing of entity with various file properties")
    public void indexFileContent() {
        FileRef fileRef = fileStorage.saveStream("testFile.txt", new ByteArrayInputStream("Test file content".getBytes()));

        // Sub References
        TestFileSubRefEntity oneToOneSubRef1 = metadata.create(TestFileSubRefEntity.class);
        oneToOneSubRef1.setName("oneToOneSubRef1");
        oneToOneSubRef1.setFileValue(fileRef);
        TestFileSubRefEntity oneToOneSubRef2 = metadata.create(TestFileSubRefEntity.class);
        oneToOneSubRef2.setName("oneToOneSubRef2");
        oneToOneSubRef2.setFileValue(fileRef);
        TestFileSubRefEntity oneToOneSubRef3 = metadata.create(TestFileSubRefEntity.class);
        oneToOneSubRef3.setName("oneToOneSubRef3");
        oneToOneSubRef3.setFileValue(fileRef);

        TestFileSubRefEntity oneToManySubRef11 = metadata.create(TestFileSubRefEntity.class);
        oneToManySubRef11.setName("oneToManySubRef11");
        oneToManySubRef11.setFileValue(fileRef);
        TestFileSubRefEntity oneToManySubRef12 = metadata.create(TestFileSubRefEntity.class);
        oneToManySubRef12.setName("oneToManySubRef12");
        oneToManySubRef12.setFileValue(fileRef);

        TestFileSubRefEntity oneToManySubRef21 = metadata.create(TestFileSubRefEntity.class);
        oneToManySubRef21.setName("oneToManySubRef21");
        oneToManySubRef21.setFileValue(fileRef);
        TestFileSubRefEntity oneToManySubRef22 = metadata.create(TestFileSubRefEntity.class);
        oneToManySubRef22.setName("oneToManySubRef22");
        oneToManySubRef22.setFileValue(fileRef);

        TestFileSubRefEntity oneToManySubRef31 = metadata.create(TestFileSubRefEntity.class);
        oneToManySubRef31.setName("oneToManySubRef31");
        oneToManySubRef31.setFileValue(fileRef);
        TestFileSubRefEntity oneToManySubRef32 = metadata.create(TestFileSubRefEntity.class);
        oneToManySubRef32.setName("oneToManySubRef32");
        oneToManySubRef32.setFileValue(fileRef);

        // References
        TestFileRefEntity oneToOneRef = metadata.create(TestFileRefEntity.class);
        oneToOneRef.setName("oneToOneRef");
        oneToOneRef.setFileValue(fileRef);
        oneToOneRef.setOneToOneRef(oneToOneSubRef1);
        oneToOneRef.setOneToManyRef(Arrays.asList(oneToManySubRef11, oneToManySubRef12));
        oneToManySubRef11.setManyToOneRef(oneToOneRef);
        oneToManySubRef12.setManyToOneRef(oneToOneRef);

        TestFileRefEntity oneToManyRef1 = metadata.create(TestFileRefEntity.class);
        oneToManyRef1.setName("oneToManyRef1");
        oneToManyRef1.setFileValue(fileRef);
        oneToManyRef1.setOneToOneRef(oneToOneSubRef2);
        oneToManyRef1.setOneToManyRef(Arrays.asList(oneToManySubRef21, oneToManySubRef22));
        oneToManySubRef21.setManyToOneRef(oneToManyRef1);
        oneToManySubRef22.setManyToOneRef(oneToManyRef1);

        TestFileRefEntity oneToManyRef2 = metadata.create(TestFileRefEntity.class);
        oneToManyRef2.setName("oneToManyRef2");
        oneToManyRef2.setFileValue(fileRef);
        oneToManyRef2.setOneToOneRef(oneToOneSubRef3);
        oneToManyRef2.setOneToManyRef(Arrays.asList(oneToManySubRef31, oneToManySubRef32));
        oneToManySubRef31.setManyToOneRef(oneToManyRef2);
        oneToManySubRef32.setManyToOneRef(oneToManyRef2);

        // Root
        TestFileRootEntity rootEntity = metadata.create(TestFileRootEntity.class);
        rootEntity.setName("rootEntity");
        rootEntity.setFileValue(fileRef);
        rootEntity.setOneToOneRef(oneToOneRef);
        rootEntity.setOneToManyRef(Arrays.asList(oneToManyRef1, oneToManyRef2));
        oneToManyRef1.setManyToOneRef(rootEntity);
        oneToManyRef2.setManyToOneRef(rootEntity);

        dataManager.save(
                rootEntity,
                oneToOneRef,
                oneToManyRef1, oneToManyRef2,
                oneToOneSubRef1, oneToOneSubRef2, oneToOneSubRef3,
                oneToManySubRef11, oneToManySubRef12,
                oneToManySubRef21, oneToManySubRef22,
                oneToManySubRef31, oneToManySubRef32
        );

        JsonNode jsonNode = TestJsonUtils.readJsonFromFile("indexing/test_content_file_properties");
        TestBulkRequestIndexActionValidationData expectedIndexAction = new TestBulkRequestIndexActionValidationData(
                "search_index_test_filerootentity",
                idSerialization.idToString(Id.of(rootEntity)),
                jsonNode
        );
        TestBulkRequestValidationData expectedData = new TestBulkRequestValidationData(
                Collections.singletonList(expectedIndexAction),
                Collections.emptyList());

        entityIndexer.index(rootEntity);
        List<BulkRequest> bulkRequests = bulkRequestsTracker.getBulkRequests();

        TestBulkRequestValidationResult result = TestBulkRequestValidator.validate(Collections.singletonList(expectedData), bulkRequests);
        Assert.assertFalse(result.toString(), result.hasFailures());
    }

    @Test
    @DisplayName("Indexing of entity with various embedded properties")
    public void indexEmbeddedContent() {
        TestEmbeddableEntity embeddableEntity = metadata.create(TestEmbeddableEntity.class);
        embeddableEntity.setEnumValue(TestEnum.OPEN);
        embeddableEntity.setTextValue("Embedded text value");
        embeddableEntity.setIntValue(1);

        // Sub References
        TestEmbeddedSubRefEntity oneToOneSubRef1 = metadata.create(TestEmbeddedSubRefEntity.class);
        oneToOneSubRef1.setName("oneToOneSubRef1");
        oneToOneSubRef1.setEmbedded(embeddableEntity);
        TestEmbeddedSubRefEntity oneToOneSubRef2 = metadata.create(TestEmbeddedSubRefEntity.class);
        oneToOneSubRef2.setName("oneToOneSubRef2");
        oneToOneSubRef2.setEmbedded(embeddableEntity);
        TestEmbeddedSubRefEntity oneToOneSubRef3 = metadata.create(TestEmbeddedSubRefEntity.class);
        oneToOneSubRef3.setName("oneToOneSubRef3");
        oneToOneSubRef3.setEmbedded(embeddableEntity);

        TestEmbeddedSubRefEntity oneToManySubRef11 = metadata.create(TestEmbeddedSubRefEntity.class);
        oneToManySubRef11.setName("oneToManySubRef11");
        oneToManySubRef11.setEmbedded(embeddableEntity);
        TestEmbeddedSubRefEntity oneToManySubRef12 = metadata.create(TestEmbeddedSubRefEntity.class);
        oneToManySubRef12.setName("oneToManySubRef12");
        oneToManySubRef12.setEmbedded(embeddableEntity);

        TestEmbeddedSubRefEntity oneToManySubRef21 = metadata.create(TestEmbeddedSubRefEntity.class);
        oneToManySubRef21.setName("oneToManySubRef21");
        oneToManySubRef21.setEmbedded(embeddableEntity);
        TestEmbeddedSubRefEntity oneToManySubRef22 = metadata.create(TestEmbeddedSubRefEntity.class);
        oneToManySubRef22.setName("oneToManySubRef22");
        oneToManySubRef22.setEmbedded(embeddableEntity);

        TestEmbeddedSubRefEntity oneToManySubRef31 = metadata.create(TestEmbeddedSubRefEntity.class);
        oneToManySubRef31.setName("oneToManySubRef31");
        oneToManySubRef31.setEmbedded(embeddableEntity);
        TestEmbeddedSubRefEntity oneToManySubRef32 = metadata.create(TestEmbeddedSubRefEntity.class);
        oneToManySubRef32.setName("oneToManySubRef32");
        oneToManySubRef32.setEmbedded(embeddableEntity);

        // References
        TestEmbeddedRefEntity oneToOneRef = metadata.create(TestEmbeddedRefEntity.class);
        oneToOneRef.setName("oneToOneRef");
        oneToOneRef.setEmbedded(embeddableEntity);
        oneToOneRef.setOneToOneRef(oneToOneSubRef1);
        oneToOneRef.setOneToManyRef(Arrays.asList(oneToManySubRef11, oneToManySubRef12));
        oneToManySubRef11.setManyToOneRef(oneToOneRef);
        oneToManySubRef12.setManyToOneRef(oneToOneRef);

        TestEmbeddedRefEntity oneToManyRef1 = metadata.create(TestEmbeddedRefEntity.class);
        oneToManyRef1.setName("oneToManyRef1");
        oneToManyRef1.setEmbedded(embeddableEntity);
        oneToManyRef1.setOneToOneRef(oneToOneSubRef2);
        oneToManyRef1.setOneToManyRef(Arrays.asList(oneToManySubRef21, oneToManySubRef22));
        oneToManySubRef21.setManyToOneRef(oneToManyRef1);
        oneToManySubRef22.setManyToOneRef(oneToManyRef1);

        TestEmbeddedRefEntity oneToManyRef2 = metadata.create(TestEmbeddedRefEntity.class);
        oneToManyRef2.setName("oneToManyRef2");
        oneToManyRef2.setEmbedded(embeddableEntity);
        oneToManyRef2.setOneToOneRef(oneToOneSubRef3);
        oneToManyRef2.setOneToManyRef(Arrays.asList(oneToManySubRef31, oneToManySubRef32));
        oneToManySubRef31.setManyToOneRef(oneToManyRef2);
        oneToManySubRef32.setManyToOneRef(oneToManyRef2);

        // Root
        TestEmbeddedRootEntity rootEntity = metadata.create(TestEmbeddedRootEntity.class);
        rootEntity.setName("rootEntity");
        rootEntity.setEmbedded(embeddableEntity);
        rootEntity.setOneToOneRef(oneToOneRef);
        rootEntity.setOneToManyRef(Arrays.asList(oneToManyRef1, oneToManyRef2));
        oneToManyRef1.setManyToOneRef(rootEntity);
        oneToManyRef2.setManyToOneRef(rootEntity);

        dataManager.save(
                rootEntity,
                oneToOneRef,
                oneToManyRef1, oneToManyRef2,
                oneToOneSubRef1, oneToOneSubRef2, oneToOneSubRef3,
                oneToManySubRef11, oneToManySubRef12,
                oneToManySubRef21, oneToManySubRef22,
                oneToManySubRef31, oneToManySubRef32
        );

        JsonNode jsonNode = TestJsonUtils.readJsonFromFile("indexing/test_content_embedded_properties");
        TestBulkRequestIndexActionValidationData expectedIndexAction = new TestBulkRequestIndexActionValidationData(
                "search_index_test_embrootentity",
                idSerialization.idToString(Id.of(rootEntity)),
                jsonNode
        );
        TestBulkRequestValidationData expectedData = new TestBulkRequestValidationData(
                Collections.singletonList(expectedIndexAction),
                Collections.emptyList());

        entityIndexer.index(rootEntity);
        List<BulkRequest> bulkRequests = bulkRequestsTracker.getBulkRequests();

        TestBulkRequestValidationResult result = TestBulkRequestValidator.validate(Collections.singletonList(expectedData), bulkRequests);
        Assert.assertFalse(result.toString(), result.hasFailures());
    }
}
