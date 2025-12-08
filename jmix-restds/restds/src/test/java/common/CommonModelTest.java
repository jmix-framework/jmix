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

package common;

import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.samples.restds.common.entity.ProductGroup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;
import test_support.BaseRestDsIntegrationTest;
import test_support.TestSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CommonModelTest extends BaseRestDsIntegrationTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    Metadata metadata;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    TransactionTemplate tx;

    @Test
    void testMetadata() {
        MetaClass metaClass = metadata.getClass(ProductGroup.class);

        String storeName = metaClass.getStore().getName();
        assertThat(storeName).isEqualTo("restService1");
    }

    @Test
    void testNoJpa() {
        try {
            tx.executeWithoutResult(transactionStatus -> {
                entityManager.find(ProductGroup.class, TestSupport.UUID_1);
            });
            fail("Exception from EntityManager expected");
        } catch (Exception e) {
            assertThat(e).hasMessageContaining("Unknown Entity bean class");
        }
    }

    @Test
    void testLoad() {
        dataManager.load(ProductGroup.class).id(TestSupport.UUID_1).one();
    }

    @Test
    void testCreateUpdateDelete() {
        ProductGroup productGroup = dataManager.create(ProductGroup.class);
        productGroup.setName("Test");
        dataManager.save(productGroup);

        ProductGroup productGroup1 = dataManager.load(ProductGroup.class).id(productGroup.getId()).one();
        assertThat(productGroup1).isEqualTo(productGroup);

        productGroup1.setName("Test updated");
        dataManager.save(productGroup1);

        ProductGroup productGroup2 = dataManager.load(ProductGroup.class).id(productGroup.getId()).one();
        assertThat(productGroup2.getName()).isEqualTo("Test updated");

        dataManager.remove(productGroup2);
        assertThat(dataManager.load(ProductGroup.class).id(productGroup.getId()).optional()).isEmpty();
    }
}
