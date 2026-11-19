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

package lazy_loading.disabled_lazy_loading;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.EntityStates;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;
import test_support.DataTestConfiguration;
import test_support.TestContextInititalizer;
import test_support.entity.lazyloading.instance_name_reference.InrAlpha;
import test_support.entity.lazyloading.instance_name_reference.InrBeta;
import test_support.entity.lazyloading.instance_name_reference.InrGamma;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * With lazy loading disabled, the attributes that the fetch plan does not include fail with the Jmix message also on
 * a reference that the default fetch plan includes because the root's instance name depends on it.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {CoreConfiguration.class, DataConfiguration.class, EclipselinkConfiguration.class, DataTestConfiguration.class},
        initializers = {TestContextInititalizer.class}
)
@TestPropertySource(properties = "jmix.eclipselink.disable-lazy-loading=true")
public class NoLLInstanceNameReferenceTest {

    static final String NOT_FETCHED_MESSAGE = "Attribute [gamma] is not fetched";

    @Autowired
    UnconstrainedDataManager dataManager;

    @Autowired
    EntityStates entityStates;

    @Autowired
    TransactionTemplate transactionTemplate;

    @Autowired
    JdbcTemplate jdbc;

    UUID alphaId;

    @BeforeEach
    void setUp() {
        InrGamma gamma = dataManager.create(InrGamma.class);
        gamma.setName("gamma1");

        InrBeta beta = dataManager.create(InrBeta.class);
        beta.setName("beta1");
        beta.setGamma(gamma);

        InrAlpha alpha = dataManager.create(InrAlpha.class);
        alpha.setName("alpha1");
        alpha.setBeta(beta);

        dataManager.saveWithoutReload(gamma, beta, alpha);
        alphaId = alpha.getId();
    }

    @AfterEach
    void tearDown() {
        jdbc.update("delete from INR_ALPHA");
        jdbc.update("delete from INR_BETA");
        jdbc.update("delete from INR_GAMMA");
    }

    @Test
    void load_noFetchPlan_failsOnUnfetchedAttributeOfInstanceNameReference() {
        InrAlpha alpha = dataManager.load(InrAlpha.class).id(alphaId).one();
        InrBeta beta = alpha.getBeta();

        IllegalStateException e = assertThrows(IllegalStateException.class, beta::getGamma);
        assertTrue(e.getMessage().startsWith(NOT_FETCHED_MESSAGE), e.getMessage());
    }

    @Test
    void load_noFetchPlanInTransaction_failsOnUnfetchedAttributeOfInstanceNameReference() {
        transactionTemplate.executeWithoutResult(status -> {
            InrAlpha alpha = dataManager.load(InrAlpha.class).id(alphaId).one();
            InrBeta beta = alpha.getBeta();
            assertTrue(entityStates.isDetached(beta));

            IllegalStateException e = assertThrows(IllegalStateException.class, beta::getGamma);
            assertTrue(e.getMessage().startsWith(NOT_FETCHED_MESSAGE), e.getMessage());
        });
    }
}
