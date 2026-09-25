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

package io.jmix.quartz.autoconfigure;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

public class QuartzEnvironmentPostProcessorTest {

    static final String USE_DB_LOCKS_PROPERTY = "spring.quartz.properties.org.quartz.jobStore.useDBLocks";
    static final String JOB_STORE_CLASS_PROPERTY = "spring.quartz.properties.org.quartz.jobStore.class";
    static final String DRIVER_DELEGATE_PROPERTY = "spring.quartz.properties.org.quartz.jobStore.driverDelegateClass";

    private final QuartzEnvironmentPostProcessor postProcessor = new QuartzEnvironmentPostProcessor();

    @Test
    void testDbLockingEnabledForDefaultJdbcJobStore() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("main.datasource.url", "jdbc:hsqldb:mem:test");

        postProcessor.postProcessEnvironment(environment, new SpringApplication());

        Assertions.assertEquals("true", environment.getProperty(USE_DB_LOCKS_PROPERTY));
        Assertions.assertEquals("io.jmix.quartz.impl.JmixLocalDataSourceJobStore",
                environment.getProperty(JOB_STORE_CLASS_PROPERTY));
    }

    @Test
    void testNothingSetForMemoryJobStore() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("main.datasource.url", "jdbc:hsqldb:mem:test")
                .withProperty("spring.quartz.job-store-type", "memory");

        postProcessor.postProcessEnvironment(environment, new SpringApplication());

        Assertions.assertNull(environment.getProperty(USE_DB_LOCKS_PROPERTY));
        Assertions.assertNull(environment.getProperty(JOB_STORE_CLASS_PROPERTY));
    }

    @Test
    void testExplicitDbLockingValueIsRespected() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("main.datasource.url", "jdbc:hsqldb:mem:test")
                .withProperty(USE_DB_LOCKS_PROPERTY, "false");

        postProcessor.postProcessEnvironment(environment, new SpringApplication());

        Assertions.assertEquals("false", environment.getProperty(USE_DB_LOCKS_PROPERTY));
        //the Jmix job store without the explicit request behaves as the standard one
        Assertions.assertEquals("io.jmix.quartz.impl.JmixLocalDataSourceJobStore",
                environment.getProperty(JOB_STORE_CLASS_PROPERTY));
    }

    @Test
    void testExplicitJobStoreClassIsRespected() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("main.datasource.url", "jdbc:hsqldb:mem:test")
                .withProperty(JOB_STORE_CLASS_PROPERTY, "com.company.CustomJobStore");

        postProcessor.postProcessEnvironment(environment, new SpringApplication());

        Assertions.assertEquals("com.company.CustomJobStore", environment.getProperty(JOB_STORE_CLASS_PROPERTY));
        Assertions.assertEquals("true", environment.getProperty(USE_DB_LOCKS_PROPERTY));
    }

    @Test
    void testDriverDelegateAndDbLockingSetTogether() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("main.datasource.url", "jdbc:postgresql://localhost/test");

        postProcessor.postProcessEnvironment(environment, new SpringApplication());

        Assertions.assertEquals("org.quartz.impl.jdbcjobstore.PostgreSQLDelegate",
                environment.getProperty(DRIVER_DELEGATE_PROPERTY));
        Assertions.assertEquals("true", environment.getProperty(USE_DB_LOCKS_PROPERTY));
    }
}
