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
import test_support.TestCommonEntityWrapperManager;
import test_support.TestIndexingQueueItemsTracker;
import test_support.entity.*;

import java.util.Arrays;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {EntityChangeTrackingTestConfiguration.class}
)
public class EntityChangeTrackingTest {

    @Autowired
    TestIndexingQueueItemsTracker indexingQueueItemsTracker;
    @Autowired
    TestCommonEntityWrapperManager ewm;

    @BeforeEach
    public void setUp() {
        indexingQueueItemsTracker.clear();
    }

    @Test
    @DisplayName("Creation of indexed entity leads to queue item enqueueing (Soft Delete)")
    public void createIndexedEntity() {
        TestRootEntity entity = ewm.createTestRootEntity().save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(entity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Creation of indexed entity leads to queue item enqueueing (Hard Delete)")
    public void createIndexedEntityHardDelete() {
        TestRootEntityHD entityHD = ewm.createTestRootEntityHD().save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(entityHD, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Update of indexed entity leads to queue item enqueueing (Soft Delete)")
    public void updateLocalPropertyOfIndexedEntity() {
        TestRootEntity entity = ewm.createTestRootEntity().save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(entity).setTextValue("Some text value").save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(entity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Update of indexed entity leads to queue item enqueueing (Hard Delete)")
    public void updateLocalPropertyOfIndexedEntityHardDelete() {
        TestRootEntityHD entityHD = ewm.createTestRootEntityHD().save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(entityHD).setTextValue("Some text value").save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(entityHD, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Deletion of indexed entity leads to queue item enqueueing (Soft Delete)")
    public void deleteIndexedEntity() {
        TestRootEntity entity = ewm.createTestRootEntity().save();
        indexingQueueItemsTracker.clear();

        ewm.remove(entity);
        boolean enqueuedDelete = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(entity, IndexingOperation.DELETE, 1);
        Assert.assertTrue(enqueuedDelete);
    }

    @Test
    @DisplayName("Deletion of indexed entity leads to queue item enqueueing (Hard Delete)")
    public void deleteIndexedEntityHardDelete() {
        TestRootEntityHD entityHD = ewm.createTestRootEntityHD().save();
        indexingQueueItemsTracker.clear();

        ewm.remove(entityHD);
        boolean enqueuedDelete = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(entityHD, IndexingOperation.DELETE, 1);
        Assert.assertTrue(enqueuedDelete);
    }

    @Test
    @DisplayName("Deletion of not-indexed entity doesn't lead to queue item enqueueing (Soft Delete)")
    public void deleteNotIndexedEntity() {
        TestReferenceEntity entity = ewm.createTestReferenceEntity().save();
        ewm.remove(entity);
        boolean enqueuedDelete = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(entity, IndexingOperation.DELETE, 0);
        Assert.assertTrue(enqueuedDelete);
    }

    @Test
    @DisplayName("Deletion of not-indexed entity doesn't lead to queue item enqueueing (Hard Delete)")
    public void deleteNotIndexedEntityHardDelete() {
        TestRootEntityHD entityHD = ewm.createTestRootEntityHD().save();
        indexingQueueItemsTracker.clear();

        ewm.remove(entityHD);
        boolean enqueuedDelete = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(entityHD, IndexingOperation.DELETE, 1);
        Assert.assertTrue(enqueuedDelete);
    }

    @Test
    @DisplayName("Creation of not-indexed doesn't lead to queue item enqueueing (Soft Delete)")
    public void createNotIndexedEntity() {
        TestReferenceEntity entity = ewm.createTestReferenceEntity().save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(entity, IndexingOperation.INDEX, 0);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Creation of not-indexed doesn't lead to queue item enqueueing (Hard Delete)")
    public void createNotIndexedEntityHardDelete() {
        TestReferenceEntityHD entityHD = ewm.createTestReferenceEntityHD().save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(entityHD, IndexingOperation.INDEX, 0);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Update of not-indexed entity doesn't lead to queue item enqueueing (Soft Delete)")
    public void updateLocalPropertyOfNotIndexedEntity() {
        TestReferenceEntity entity = ewm.createTestReferenceEntity().save();

        ewm.wrap(entity).setTextValue("Some text value").save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(entity, IndexingOperation.INDEX, 0);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Update of not-indexed entity doesn't lead to queue item enqueueing (Hard Delete)")
    public void updateLocalPropertyOfNotIndexedEntityHardDelete() {
        TestReferenceEntityHD entityHD = ewm.createTestReferenceEntityHD().save();

        ewm.wrap(entityHD).setTextValue("Some text value").save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(entityHD, IndexingOperation.INDEX, 0);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Adding of one-to-one reference leads to queue item enqueueing (Soft Delete)")
    public void addOneToOneReference() {
        TestReferenceEntity reference = ewm.createTestReferenceEntity().save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(rootEntity).setOneToOneAssociation(reference).save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Adding of one-to-one reference leads to queue item enqueueing (Hard Delete)")
    public void addOneToOneReferenceHardDelete() {
        TestReferenceEntityHD referenceHD = ewm.createTestReferenceEntityHD().save();
        TestRootEntityHD rootEntityHD = ewm.createTestRootEntityHD().save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(rootEntityHD).setOneToOneAssociation(referenceHD).save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntityHD, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Changing of one-to-one reference leads to queue item enqueueing (Soft Delete)")
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
    @DisplayName("Changing of one-to-one reference leads to queue item enqueueing (Hard Delete)")
    public void changeOneToOneReferenceHardDelete() {
        TestReferenceEntityHD firstReferenceHD = ewm.createTestReferenceEntityHD().save();
        TestReferenceEntityHD secondReferenceHD = ewm.createTestReferenceEntityHD().save();
        TestRootEntityHD rootEntityHD = ewm.createTestRootEntityHD().setOneToOneAssociation(firstReferenceHD).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(rootEntityHD).setOneToOneAssociation(secondReferenceHD).save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntityHD, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Clearing of one-to-one reference leads to queue item enqueueing (Soft Delete)")
    public void clearOneToOneReference() {
        TestReferenceEntity reference = ewm.createTestReferenceEntity().save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToOneAssociation(reference).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(rootEntity).setOneToOneAssociation(null).save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Clearing of one-to-one reference leads to queue item enqueueing (Hard Delete)")
    public void clearOneToOneReferenceHardDelete() {
        TestReferenceEntityHD referenceHD = ewm.createTestReferenceEntityHD().save();
        TestRootEntityHD rootEntityHD = ewm.createTestRootEntityHD().setOneToOneAssociation(referenceHD).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(rootEntityHD).setOneToOneAssociation(null).save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntityHD, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Deletion of one-to-one reference leads to queue item enqueueing (Soft Delete)")
    public void deleteOneToOneReference() {
        TestReferenceEntity reference = ewm.createTestReferenceEntity().save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToOneAssociation(reference).save();
        indexingQueueItemsTracker.clear();

        ewm.remove(reference);
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Deletion of one-to-one reference leads to queue item enqueueing (Hard Delete)")
    public void deleteOneToOneReferenceHardDelete() {
        TestReferenceEntityHD referenceHD = ewm.createTestReferenceEntityHD().save();
        TestRootEntityHD rootEntityHD = ewm.createTestRootEntityHD().setOneToOneAssociation(referenceHD).save();
        indexingQueueItemsTracker.clear();

        ewm.remove(referenceHD);
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntityHD, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Update of indexed local property of one-to-one reference leads to queue item enqueueing (Soft Delete)")
    public void updateIndexedLocalPropertyOfOneToOneReference() {
        TestReferenceEntity reference = ewm.createTestReferenceEntity().save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToOneAssociation(reference).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(reference).setTextValue("Some text value").save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Update of indexed local property of one-to-one reference leads to queue item enqueueing (Hard Delete)")
    public void updateIndexedLocalPropertyOfOneToOneReferenceHardDelete() {
        TestReferenceEntityHD referenceHD = ewm.createTestReferenceEntityHD().save();
        TestRootEntityHD rootEntityHD = ewm.createTestRootEntityHD().setOneToOneAssociation(referenceHD).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(referenceHD).setTextValue("Some text value").save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntityHD, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Update of not-indexed local property of one-to-one reference doesn't lead to queue item enqueueing (Soft Delete)")
    public void updateNotIndexedLocalPropertyOfOneToOneReference() {
        TestReferenceEntity reference = ewm.createTestReferenceEntity().save();
        TestRootEntity rootEntity = ewm.createTestRootEntity().setOneToOneAssociation(reference).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(reference).setName("New Name").save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntity, IndexingOperation.INDEX, 0);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Update of not-indexed local property of one-to-one reference doesn't lead to queue item enqueueing (Hard Delete)")
    public void updateNotIndexedLocalPropertyOfOneToOneReferenceHardDelete() {
        TestReferenceEntityHD referenceHD = ewm.createTestReferenceEntityHD().save();
        TestRootEntityHD rootEntityHD = ewm.createTestRootEntityHD().setOneToOneAssociation(referenceHD).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(referenceHD).setName("New Name").save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntityHD, IndexingOperation.INDEX, 0);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Adding of one-to-one sub-reference leads to queue item enqueueing (Soft Delete)")
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
    @DisplayName("Adding of one-to-one sub-reference leads to queue item enqueueing (Hard Delete)")
    public void addOneToOneSubReferenceHardDelete() {
        TestReferenceEntityHD referenceHD = ewm.createTestReferenceEntityHD().save();
        TestRootEntityHD rootEntityHD = ewm.createTestRootEntityHD().setOneToOneAssociation(referenceHD).save();
        TestSubReferenceEntityHD subReferenceHD = ewm.createTestSubReferenceEntityHD().save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(referenceHD).setOneToOneAssociation(subReferenceHD).save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntityHD, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Changing of one-to-one sub-reference leads to queue item enqueueing (Soft Delete)")
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
    @DisplayName("Changing of one-to-one sub-reference leads to queue item enqueueing (Hard Delete)")
    public void changeOneToOneSubReferenceHardDelete() {
        TestSubReferenceEntityHD firstSubReferenceHD = ewm.createTestSubReferenceEntityHD().save();
        TestSubReferenceEntityHD secondSubReferenceHD = ewm.createTestSubReferenceEntityHD().save();
        TestReferenceEntityHD referenceHD = ewm.createTestReferenceEntityHD().setOneToOneAssociation(firstSubReferenceHD).save();
        TestRootEntityHD rootEntityHD = ewm.createTestRootEntityHD().setOneToOneAssociation(referenceHD).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(referenceHD).setOneToOneAssociation(secondSubReferenceHD).save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntityHD, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Clearing of one-to-one sub-reference leads to queue item enqueueing (Soft Delete)")
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
    @DisplayName("Clearing of one-to-one sub-reference leads to queue item enqueueing (Hard Delete)")
    public void clearOneToOneSubReferenceHardDelete() {
        TestSubReferenceEntityHD subReferenceHD = ewm.createTestSubReferenceEntityHD().save();
        TestReferenceEntityHD referenceHD = ewm.createTestReferenceEntityHD().setOneToOneAssociation(subReferenceHD).save();
        TestRootEntityHD rootEntityHD = ewm.createTestRootEntityHD().setOneToOneAssociation(referenceHD).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(referenceHD).setOneToOneAssociation(null).save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntityHD, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Deletion of one-to-one sub-reference leads to queue item enqueueing (Soft Delete)")
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
    @DisplayName("Deletion of one-to-one sub-reference leads to queue item enqueueing (Hard Delete)")
    public void deleteOneToOneSubReferenceHardDelete() {
        TestSubReferenceEntityHD subReferenceHD = ewm.createTestSubReferenceEntityHD().save();
        TestReferenceEntityHD referenceHD = ewm.createTestReferenceEntityHD().setOneToOneAssociation(subReferenceHD).save();
        TestRootEntityHD rootEntityHD = ewm.createTestRootEntityHD().setOneToOneAssociation(referenceHD).save();
        indexingQueueItemsTracker.clear();

        ewm.remove(subReferenceHD);
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntityHD, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Update of indexed local property of one-to-one sub-reference leads to queue item enqueueing (Soft Delete)")
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
    @DisplayName("Update of indexed local property of one-to-one sub-reference leads to queue item enqueueing (Hard Delete)")
    public void updateIndexedLocalPropertyOfOneToOneSubReferenceHardDelete() {
        TestSubReferenceEntityHD subReferenceHD = ewm.createTestSubReferenceEntityHD().save();
        TestReferenceEntityHD referenceHD = ewm.createTestReferenceEntityHD().setOneToOneAssociation(subReferenceHD).save();
        TestRootEntityHD rootEntityHD = ewm.createTestRootEntityHD().setOneToOneAssociation(referenceHD).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(subReferenceHD).setTextValue("Some text value").save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntityHD, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Update of not-indexed local property of one-to-one sub-reference doesn't lead to queue item enqueueing (Soft Delete)")
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
    @DisplayName("Update of not-indexed local property of one-to-one sub-reference doesn't lead to queue item enqueueing (Hard Delete)")
    public void updateNotIndexedLocalPropertyOfOneToOneSubReferenceHardDelete() {
        TestSubReferenceEntityHD subReferenceHD = ewm.createTestSubReferenceEntityHD().save();
        TestReferenceEntityHD referenceHD = ewm.createTestReferenceEntityHD().setOneToOneAssociation(subReferenceHD).save();
        TestRootEntityHD rootEntityHD = ewm.createTestRootEntityHD().setOneToOneAssociation(referenceHD).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(subReferenceHD).setName("New Name").save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntityHD, IndexingOperation.INDEX, 0);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Adding of one-to-many references leads to queue item enqueueing (Soft Delete)")
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
    @DisplayName("Adding of one-to-many references leads to queue item enqueueing (Hard Delete)")
    public void addOneToManyReferencesHardDelete() {
        TestReferenceEntityHD firstReferenceHD = ewm.createTestReferenceEntityHD().save();
        TestReferenceEntityHD secondReferenceHD = ewm.createTestReferenceEntityHD().save();
        TestRootEntityHD rootEntityHD = ewm.createTestRootEntityHD().save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(rootEntityHD).setOneToManyAssociation(Arrays.asList(firstReferenceHD, secondReferenceHD)).save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntityHD, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Changing of one-to-many references leads to queue item enqueueing (Soft Delete)")
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
    @DisplayName("Changing of one-to-many references leads to queue item enqueueing (Hard Delete)")
    public void changeOneToManyReferencesHardDelete() {
        TestReferenceEntityHD firstReferenceHD = ewm.createTestReferenceEntityHD().save();
        TestReferenceEntityHD secondReferenceHD = ewm.createTestReferenceEntityHD().save();
        TestRootEntityHD rootEntityHD = ewm.createTestRootEntityHD().setOneToManyAssociation(firstReferenceHD).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(rootEntityHD).setOneToManyAssociation(secondReferenceHD).save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntityHD, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Clearing of one-to-many references leads to queue item enqueueing (Soft Delete)")
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
    @DisplayName("Clearing of one-to-many references leads to queue item enqueueing (Hard Delete)")
    public void clearOneToManyReferencesHardDelete() {
        TestReferenceEntityHD firstReferenceHD = ewm.createTestReferenceEntityHD().save();
        TestReferenceEntityHD secondReferenceHD = ewm.createTestReferenceEntityHD().save();
        TestRootEntityHD rootEntityHD = ewm.createTestRootEntityHD().setOneToManyAssociation(firstReferenceHD, secondReferenceHD).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(rootEntityHD).setOneToManyAssociation().save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntityHD, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Deletion of some one-to-many reference from collection leads to queue item enqueueing (Soft Delete)")
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
    @DisplayName("Deletion of some one-to-many reference from collection leads to queue item enqueueing (Hard Delete)")
    public void deleteOneToManyReferenceHardDelete() {
        TestReferenceEntityHD firstReferenceHD = ewm.createTestReferenceEntityHD().save();
        TestReferenceEntityHD secondReferenceHD = ewm.createTestReferenceEntityHD().save();
        TestRootEntityHD rootEntityHD = ewm.createTestRootEntityHD().setOneToManyAssociation(firstReferenceHD, secondReferenceHD).save();
        indexingQueueItemsTracker.clear();

        ewm.remove(firstReferenceHD);
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntityHD, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Update of indexed local property of one-to-many reference leads to queue item enqueueing (Soft Delete)")
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
    @DisplayName("Update of indexed local property of one-to-many reference leads to queue item enqueueing (Hard Delete)")
    public void updateIndexedLocalPropertyOfOneToManyReferenceHardDelete() {
        TestReferenceEntityHD firstReferenceHD = ewm.createTestReferenceEntityHD().save();
        TestReferenceEntityHD secondReferenceHD = ewm.createTestReferenceEntityHD().save();
        TestRootEntityHD rootEntityHD = ewm.createTestRootEntityHD().setOneToManyAssociation(firstReferenceHD, secondReferenceHD).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(firstReferenceHD).setTextValue("Some text value").save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntityHD, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Update of not-indexed local property of one-to-many reference doesn't lead to queue item enqueueing (Soft Delete)")
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
    @DisplayName("Update of not-indexed local property of one-to-many reference doesn't lead to queue item enqueueing (Hard Delete)")
    public void updateNotIndexedLocalPropertyOfOneToManyReferenceHardDelete() {
        TestReferenceEntityHD firstReferenceHD = ewm.createTestReferenceEntityHD().save();
        TestReferenceEntityHD secondReferenceHD = ewm.createTestReferenceEntityHD().save();
        TestRootEntityHD rootEntityHD = ewm.createTestRootEntityHD().setOneToManyAssociation(firstReferenceHD, secondReferenceHD).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(firstReferenceHD).setName("New name").save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntityHD, IndexingOperation.INDEX, 0);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Adding of one-to-many sub-references leads to queue item enqueueing (Soft Delete)")
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
    @DisplayName("Adding of one-to-many sub-references leads to queue item enqueueing (Hard Delete)")
    public void addOneToManySubReferencesHardDelete() {
        TestSubReferenceEntityHD firstSubReferenceHD = ewm.createTestSubReferenceEntityHD().save();
        TestSubReferenceEntityHD secondSubReferenceHD = ewm.createTestSubReferenceEntityHD().save();
        TestReferenceEntityHD referenceHD = ewm.createTestReferenceEntityHD().save();
        TestRootEntityHD rootEntityHD = ewm.createTestRootEntityHD().setOneToManyAssociation(referenceHD).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(referenceHD).setOneToManyAssociation(firstSubReferenceHD, secondSubReferenceHD).save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntityHD, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Changing of one-to-many sub-references leads to queue item enqueueing (Soft Delete)")
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
    @DisplayName("Changing of one-to-many sub-references leads to queue item enqueueing (Hard Delete)")
    public void changeOneToManySubReferencesHardDelete() {
        TestSubReferenceEntityHD firstSubReferenceHD = ewm.createTestSubReferenceEntityHD().save();
        TestSubReferenceEntityHD secondSubReferenceHD = ewm.createTestSubReferenceEntityHD().save();
        TestReferenceEntityHD referenceHD = ewm.createTestReferenceEntityHD().setOneToManyAssociation(firstSubReferenceHD).save();
        TestRootEntityHD rootEntityHD = ewm.createTestRootEntityHD().setOneToManyAssociation(referenceHD).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(referenceHD).setOneToManyAssociation(secondSubReferenceHD).save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntityHD, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Clearing of one-to-many sub-references leads to queue item enqueueing (Soft Delete)")
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
    @DisplayName("Clearing of one-to-many sub-references leads to queue item enqueueing (Hard Delete)")
    public void clearOneToManySubReferenceHardDelete() {
        TestSubReferenceEntityHD firstSubReferenceHD = ewm.createTestSubReferenceEntityHD().save();
        TestSubReferenceEntityHD secondSubReferenceHD = ewm.createTestSubReferenceEntityHD().save();
        TestReferenceEntityHD referenceHD = ewm.createTestReferenceEntityHD().setOneToManyAssociation(firstSubReferenceHD, secondSubReferenceHD).save();
        TestRootEntityHD rootEntityHD = ewm.createTestRootEntityHD().setOneToManyAssociation(referenceHD).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(referenceHD).setOneToManyAssociation().save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntityHD, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Deletion of some one-to-many sub-reference from collection leads to queue item enqueueing (Soft Delete)")
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
    @DisplayName("Deletion of some one-to-many sub-reference from collection leads to queue item enqueueing (Hard Delete)")
    public void deleteOneToManySubReferenceHardDelete() {
        TestSubReferenceEntityHD firstSubReferenceHD = ewm.createTestSubReferenceEntityHD().save();
        TestSubReferenceEntityHD secondSubReferenceHD = ewm.createTestSubReferenceEntityHD().save();
        TestReferenceEntityHD referenceHD = ewm.createTestReferenceEntityHD().setOneToManyAssociation(firstSubReferenceHD, secondSubReferenceHD).save();
        TestRootEntityHD rootEntityHD = ewm.createTestRootEntityHD().setOneToManyAssociation(referenceHD).save();
        indexingQueueItemsTracker.clear();

        ewm.remove(firstSubReferenceHD);
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntityHD, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Update of indexed local property of one-to-many sub-reference leads to queue item enqueueing (Soft Delete)")
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
    @DisplayName("Update of indexed local property of one-to-many sub-reference leads to queue item enqueueing (Hard Delete)")
    public void updateIndexedLocalPropertyOfOneToManySubReferenceHardDelete() {
        TestSubReferenceEntityHD firstSubReferenceHD = ewm.createTestSubReferenceEntityHD().save();
        TestSubReferenceEntityHD secondSubReferenceHD = ewm.createTestSubReferenceEntityHD().save();
        TestReferenceEntityHD referenceHD = ewm.createTestReferenceEntityHD().setOneToManyAssociation(firstSubReferenceHD, secondSubReferenceHD).save();
        TestRootEntityHD rootEntityHD = ewm.createTestRootEntityHD().setOneToManyAssociation(referenceHD).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(firstSubReferenceHD).setTextValue("Some text value").save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntityHD, IndexingOperation.INDEX, 1);
        Assert.assertTrue(enqueued);
    }

    @Test
    @DisplayName("Update of not-indexed local property of one-to-many sub-reference doesn't lead to queue item enqueueing (Soft Delete)")
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

    @Test
    @DisplayName("Update of not-indexed local property of one-to-many sub-reference doesn't lead to queue item enqueueing (Hard Delete)")
    public void updateNotIndexedLocalPropertyOfOneToManySubReferenceHardDelete() {
        TestSubReferenceEntityHD firstSubReferenceHD = ewm.createTestSubReferenceEntityHD().save();
        TestSubReferenceEntityHD secondSubReferenceHD = ewm.createTestSubReferenceEntityHD().save();
        TestReferenceEntityHD referenceHD = ewm.createTestReferenceEntityHD().setOneToManyAssociation(firstSubReferenceHD, secondSubReferenceHD).save();
        TestRootEntityHD rootEntityHD = ewm.createTestRootEntityHD().setOneToManyAssociation(referenceHD).save();
        indexingQueueItemsTracker.clear();

        ewm.wrap(firstSubReferenceHD).setName("New name").save();
        boolean enqueued = indexingQueueItemsTracker.containsQueueItemsForEntityAndOperation(rootEntityHD, IndexingOperation.INDEX, 0);
        Assert.assertTrue(enqueued);
    }
}
