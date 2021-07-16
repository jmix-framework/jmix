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

package change_tracking;

import io.jmix.search.index.queue.impl.IndexingOperation;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.EntityChangeTrackingTestConfiguration;
import test_support.TestEntityWrapperManager;
import test_support.TestIndexingQueueItemsTracker;
import test_support.entity.TestReferenceEntity;
import test_support.entity.TestRootEntity;
import test_support.entity.TestSubReferenceEntity;

import java.util.Arrays;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {EntityChangeTrackingTestConfiguration.class}
)
public class EntityChangeTrackingTest {

    @Autowired
    TestIndexingQueueItemsTracker indexingQueueItemsTracker;
    @Autowired
    TestEntityWrapperManager ewm;

    @BeforeEach
    public void setUp() {
        indexingQueueItemsTracker.clear();
    }

    @Test
    @DisplayName("Creation of indexed entity leads to queue item enqueueing")
    public void createIndexedEntity() {
        TestRootEntity entity = ewm.createTestRootEntity().save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(entity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Update of indexed entity leads to queue item enqueueing")
    public void updateLocalPropertyOfIndexedEntity() {
        TestRootEntity entity = ewm.createTestRootEntity().save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(entity).setTextValue("Some text value").save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(entity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Deletion of indexed entity leads to queue item enqueueing")
    public void deleteIndexedEntity() {
        TestRootEntity entity = ewm.createTestRootEntity().save();
        indexingQueueItemsTracker.clear();

        ewm.remove(entity);
        boolean enqueuedDelete = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(entity, IndexingOperation.DELETE, 1);
        Assert.assertTrue(enqueuedDelete);
    }

    @Test
    @DisplayName("Deletion of not-indexed entity doesn't lead to queue item enqueueing")
    public void deleteNotIndexedEntity() {
        TestReferenceEntity entity = ewm.createTestReferenceEntity().save();
        ewm.remove(entity);
        boolean enqueuedDelete = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(entity, IndexingOperation.DELETE, 0);
        Assert.assertTrue(enqueuedDelete);
    }

    @Test
    @DisplayName("Creation of not-indexed doesn't lead to queue item enqueueing")
    public void createNotIndexedEntity() {
        TestReferenceEntity entity = ewm.createTestReferenceEntity().save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(entity, IndexingOperation.INDEX, 0);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Update of not-indexed entity doesn't lead to queue item enqueueing")
    public void updateLocalPropertyOfNotIndexedEntity() {
        TestReferenceEntity entity = ewm.createTestReferenceEntity().save();

        ewm.wrap(entity).setTextValue("Some text value").save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(entity, IndexingOperation.INDEX, 0);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Adding of one-to-one reference leads to queue item enqueueing")
    public void addOneToOneReference() {
        TestReferenceEntity reference = ewm.createTestReferenceEntity().save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(rootEntity).setOneToOneAssociation(reference).save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Changing of one-to-one reference leads to queue item enqueueing")
    public void changeOneToOneReference() {
        TestReferenceEntity firstReference = ewm.createTestReferenceEntity().save();
        TestReferenceEntity secondReference = ewm.createTestReferenceEntity().save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToOneAssociation(firstReference).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(rootEntity).setOneToOneAssociation(secondReference).save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Clearing of one-to-one reference leads to queue item enqueueing")
    public void clearOneToOneReference() {
        TestReferenceEntity reference = ewm.createTestReferenceEntity().save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToOneAssociation(reference).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(rootEntity).setOneToOneAssociation(null).save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Deletion of one-to-one reference leads to queue item enqueueing")
    public void deleteOneToOneReference() {
        TestReferenceEntity reference = ewm.createTestReferenceEntity().save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToOneAssociation(reference).save();
        indexingQueueItemsTracker.clear();

        ewm.remove(reference);
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Update of indexed local property of one-to-one reference leads to queue item enqueueing")
    public void updateIndexedLocalPropertyOfOneToOneReference() {
        TestReferenceEntity reference = ewm.createTestReferenceEntity().save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToOneAssociation(reference).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(reference).setTextValue("Some text value").save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Update of not-indexed local property of one-to-one reference doesn't lead to queue item enqueueing")
    public void updateNotIndexedLocalPropertyOfOneToOneReference() {
        TestReferenceEntity reference = ewm.createTestReferenceEntity().save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToOneAssociation(reference).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(reference).setName("New Name").save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 0);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Adding of one-to-one sub-reference leads to queue item enqueueing")
    public void addOneToOneSubReference() {
        TestReferenceEntity reference = ewm.createTestReferenceEntity().save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToOneAssociation(reference).save();
        TestSubReferenceEntity subReference = ewm.createTestSubReferenceEntity().save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(reference).setOneToOneAssociation(subReference).save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Changing of one-to-one sub-reference leads to queue item enqueueing")
    public void changeOneToOneSubReference() {
        TestSubReferenceEntity firstSubReference = ewm.createTestSubReferenceEntity().save();
        TestSubReferenceEntity secondSubReference = ewm.createTestSubReferenceEntity().save();
        TestReferenceEntity reference = ewm.createTestReferenceEntity().setOneToOneAssociation(firstSubReference).save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToOneAssociation(reference).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(reference).setOneToOneAssociation(secondSubReference).save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Clearing of one-to-one sub-reference leads to queue item enqueueing")
    public void clearOneToOneSubReference() {
        TestSubReferenceEntity subReference = ewm.createTestSubReferenceEntity().save();
        TestReferenceEntity reference = ewm.createTestReferenceEntity().setOneToOneAssociation(subReference).save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToOneAssociation(reference).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(reference).setOneToOneAssociation(null).save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Deletion of one-to-one sub-reference leads to queue item enqueueing")
    public void deleteOneToOneSubReference() {
        TestSubReferenceEntity subReference = ewm.createTestSubReferenceEntity().save();
        TestReferenceEntity reference = ewm.createTestReferenceEntity().setOneToOneAssociation(subReference).save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToOneAssociation(reference).save();
        indexingQueueItemsTracker.clear();

        ewm.remove(subReference);
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Update of indexed local property of one-to-one sub-reference leads to queue item enqueueing")
    public void updateIndexedLocalPropertyOfOneToOneSubReference() {
        TestSubReferenceEntity subReference = ewm.createTestSubReferenceEntity().save();
        TestReferenceEntity reference = ewm.createTestReferenceEntity().setOneToOneAssociation(subReference).save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToOneAssociation(reference).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(subReference).setTextValue("Some text value").save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Update of not-indexed local property of one-to-one sub-reference doesn't lead to queue item enqueueing")
    public void updateNotIndexedLocalPropertyOfOneToOneSubReference() {
        TestSubReferenceEntity subReference = ewm.createTestSubReferenceEntity().save();
        TestReferenceEntity reference = ewm.createTestReferenceEntity().setOneToOneAssociation(subReference).save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToOneAssociation(reference).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(subReference).setName("New Name").save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 0);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Adding of one-to-many references leads to queue item enqueueing")
    public void addOneToManyReferences() {
        TestReferenceEntity firstReference = ewm.createTestReferenceEntity().save();
        TestReferenceEntity secondReference = ewm.createTestReferenceEntity().save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(rootEntity).setOneToManyAssociation(Arrays.asList(firstReference, secondReference)).save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Changing of one-to-many references leads to queue item enqueueing")
    public void changeOneToManyReferences() {
        TestReferenceEntity firstReference = ewm.createTestReferenceEntity().save();
        TestReferenceEntity secondReference = ewm.createTestReferenceEntity().save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToManyAssociation(firstReference).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(rootEntity).setOneToManyAssociation(secondReference).save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Clearing of one-to-many references leads to queue item enqueueing")
    public void clearOneToManyReferences() {
        TestReferenceEntity firstReference = ewm.createTestReferenceEntity().save();
        TestReferenceEntity secondReference = ewm.createTestReferenceEntity().save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToManyAssociation(firstReference, secondReference).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(rootEntity).setOneToManyAssociation().save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Deletion of some one-to-many reference from collection leads to queue item enqueueing")
    public void deleteOneToManyReference() {
        TestReferenceEntity firstReference = ewm.createTestReferenceEntity().save();
        TestReferenceEntity secondReference = ewm.createTestReferenceEntity().save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToManyAssociation(firstReference, secondReference).save();
        indexingQueueItemsTracker.clear();

        ewm.remove(firstReference);
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Update of indexed local property of one-to-many reference leads to queue item enqueueing")
    public void updateIndexedLocalPropertyOfOneToManyReference() {
        TestReferenceEntity firstReference = ewm.createTestReferenceEntity().save();
        TestReferenceEntity secondReference = ewm.createTestReferenceEntity().save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToManyAssociation(firstReference, secondReference).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(firstReference).setTextValue("Some text value").save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Update of not-indexed local property of one-to-many reference doesn't lead to queue item enqueueing")
    public void updateNotIndexedLocalPropertyOfOneToManyReference() {
        TestReferenceEntity firstReference = ewm.createTestReferenceEntity().save();
        TestReferenceEntity secondReference = ewm.createTestReferenceEntity().save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToManyAssociation(firstReference, secondReference).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(firstReference).setName("New name").save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 0);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Adding of one-to-many sub-references leads to queue item enqueueing")
    public void addOneToManySubReferences() {
        TestSubReferenceEntity firstSubReference = ewm.createTestSubReferenceEntity().save();
        TestSubReferenceEntity secondSubReference = ewm.createTestSubReferenceEntity().save();
        TestReferenceEntity reference = ewm.createTestReferenceEntity().save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToManyAssociation(reference).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(reference).setOneToManyAssociation(firstSubReference, secondSubReference).save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Changing of one-to-many sub-references leads to queue item enqueueing")
    public void changeOneToManySubReferences() {
        TestSubReferenceEntity firstSubReference = ewm.createTestSubReferenceEntity().save();
        TestSubReferenceEntity secondSubReference = ewm.createTestSubReferenceEntity().save();
        TestReferenceEntity reference = ewm.createTestReferenceEntity().setOneToManyAssociation(firstSubReference).save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToManyAssociation(reference).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(reference).setOneToManyAssociation(secondSubReference).save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Clearing of one-to-many sub-references leads to queue item enqueueing")
    public void clearOneToManySubReference() {
        TestSubReferenceEntity firstSubReference = ewm.createTestSubReferenceEntity().save();
        TestSubReferenceEntity secondSubReference = ewm.createTestSubReferenceEntity().save();
        TestReferenceEntity reference = ewm.createTestReferenceEntity().setOneToManyAssociation(firstSubReference, secondSubReference).save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToManyAssociation(reference).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(reference).setOneToManyAssociation().save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Deletion of some one-to-many sub-reference from collection leads to queue item enqueueing")
    public void deleteOneToManySubReference() {
        TestSubReferenceEntity firstSubReference = ewm.createTestSubReferenceEntity().save();
        TestSubReferenceEntity secondSubReference = ewm.createTestSubReferenceEntity().save();
        TestReferenceEntity reference = ewm.createTestReferenceEntity().setOneToManyAssociation(firstSubReference, secondSubReference).save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToManyAssociation(reference).save();
        indexingQueueItemsTracker.clear();

        ewm.remove(firstSubReference);
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Update of indexed local property of one-to-many sub-reference leads to queue item enqueueing")
    public void updateIndexedLocalPropertyOfOneToManySubReference() {
        TestSubReferenceEntity firstSubReference = ewm.createTestSubReferenceEntity().save();
        TestSubReferenceEntity secondSubReference = ewm.createTestSubReferenceEntity().save();
        TestReferenceEntity reference = ewm.createTestReferenceEntity().setOneToManyAssociation(firstSubReference, secondSubReference).save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToManyAssociation(reference).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(firstSubReference).setTextValue("Some text value").save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Update of not-indexed local property of one-to-many sub-reference doesn't lead to queue item enqueueing")
    public void updateNotIndexedLocalPropertyOfOneToManySubReference() {
        TestSubReferenceEntity firstSubReference = ewm.createTestSubReferenceEntity().save();
        TestSubReferenceEntity secondSubReference = ewm.createTestSubReferenceEntity().save();
        TestReferenceEntity reference = ewm.createTestReferenceEntity().setOneToManyAssociation(firstSubReference, secondSubReference).save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToManyAssociation(reference).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(firstSubReference).setName("New name").save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 0);
        Assert.assertTrue(enqueued);
    }
}
