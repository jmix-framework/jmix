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

package lazy_loading;

import io.jmix.core.CoreConfiguration;
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
import org.springframework.test.context.junit.jupiter.DisabledIf;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.DataTestConfiguration;
import test_support.TestContextInititalizer;
import test_support.entity.lazyloading.instance_name_reference.InrAlpha;
import test_support.entity.lazyloading.instance_name_reference.InrBeta;
import test_support.entity.lazyloading.instance_name_reference.InrGamma;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Lazy loading of the references of an entity that the default fetch plan includes because the root's instance name
 * depends on it. Wrapping of excessively loaded properties is disabled, as it would hide a load listener that does
 * not process such an entity.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {CoreConfiguration.class, DataConfiguration.class, EclipselinkConfiguration.class, DataTestConfiguration.class},
        initializers = {TestContextInititalizer.class}
)
@TestPropertySource(properties = "jmix.eclipselink.wrap-excessively-loaded-properties=false")
@DisabledIf(expression = "#{systemEnvironment['JMIX_ECLIPSELINK_DISABLELAZYLOADING'] == 'true'}")
public class InstanceNameReferenceLazyLoadingTest {

    @Autowired
    UnconstrainedDataManager dataManager;

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
    void load_noFetchPlan_lazyLoadsReferenceOfInstanceNameReference() {
        InrAlpha alpha = dataManager.load(InrAlpha.class).id(alphaId).one();

        assertEquals("gamma1", alpha.getBeta().getGamma().getName());
    }
}
