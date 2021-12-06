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

package datamanager;

import io.jmix.core.AccessConstraintsRegistry;
import io.jmix.core.CoreConfiguration;
import io.jmix.core.DataManager;
import io.jmix.core.accesscontext.InMemoryCrudEntityContext;
import io.jmix.core.constraint.InMemoryConstraint;
import io.jmix.core.constraint.RowLevelConstraint;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.security.SecurityConfiguration;
import io.jmix.securitydata.SecurityDataConfiguration;
import io.jmix.securitydata.constraint.ReadEntityQueryConstraint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.SecurityDataTestConfiguration;
import test_support.entity.TestOrder;

import java.util.Collections;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {CoreConfiguration.class, DataConfiguration.class, EclipselinkConfiguration.class,
                SecurityConfiguration.class, SecurityDataConfiguration.class, SecurityDataTestConfiguration.class}
)
public class DataManagerConstraintsUsageTest {

    @Autowired
    DataManager dataManager;
    @Autowired
    AccessConstraintsRegistry accessConstraintsRegistry;
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    CurrentAuthentication currentAuthentication;
    @Autowired
    SystemAuthenticator systemAuthenticator;

    @Test
    void test() {
        List<TestOrder> list;

        systemAuthenticator.begin();

        // all registered constraints
        list = dataManager.load(TestOrder.class)
                .all()
                .accessConstraints(accessConstraintsRegistry.getConstraints())
                .list();

        // all row-level restrictions
        list = dataManager.load(TestOrder.class)
                .all()
                .accessConstraints(RowLevelConstraint.class)
                .list();

        // particular built-in constraint
        ReadEntityQueryConstraint readEntityQueryConstraint = applicationContext.getBean(ReadEntityQueryConstraint.class);
        list = dataManager.load(TestOrder.class)
                .all()
                .accessConstraints(Collections.singleton(readEntityQueryConstraint))
                .list();

        // a custom constraint
        list = dataManager.load(TestOrder.class)
                .all()
                .accessConstraints(Collections.singleton(new TestConstraint(currentAuthentication.getAuthentication())))
                .list();

        systemAuthenticator.end();
    }

    public static class TestConstraint implements InMemoryConstraint<InMemoryCrudEntityContext>, RowLevelConstraint<InMemoryCrudEntityContext> {

        private Authentication authentication;

        public TestConstraint(Authentication authentication) {
            this.authentication = authentication;
        }

        @Override
        public Class<InMemoryCrudEntityContext> getContextType() {
            return InMemoryCrudEntityContext.class;
        }

        @Override
        public void applyTo(InMemoryCrudEntityContext context) {
            MetaClass entityClass = context.getEntityClass();
            if (entityClass.getJavaClass().equals(TestOrder.class)) {
                context.addReadPredicate((entity, applicationContext) -> authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(name -> name.equals("can-read-orders")));
            }
        }
    }
}
