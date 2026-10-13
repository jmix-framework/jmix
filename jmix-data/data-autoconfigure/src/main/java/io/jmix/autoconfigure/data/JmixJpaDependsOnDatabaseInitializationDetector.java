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

package io.jmix.autoconfigure.data;

import jakarta.persistence.EntityManagerFactory;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.sql.init.dependency.AbstractBeansOfTypeDependsOnDatabaseInitializationDetector;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitializationDetector;
import org.springframework.orm.jpa.AbstractEntityManagerFactoryBean;

import java.util.Set;

/**
 * {@link DependsOnDatabaseInitializationDetector} that makes every {@link EntityManagerFactory} bean depend on
 * the database initializers (Liquibase and the others Spring Boot detects), so that JPA queries made while the
 * context starts see the migrated schema.
 * <p>
 * Spring Boot ships the equivalent detector in {@code spring-boot-jpa}, which Jmix does not use.
 * Registered in {@code META-INF/spring.factories}.
 */
@NullMarked
public class JmixJpaDependsOnDatabaseInitializationDetector
        extends AbstractBeansOfTypeDependsOnDatabaseInitializationDetector {

    @Override
    protected Set<Class<?>> getDependsOnDatabaseInitializationBeanTypes() {
        return Set.of(EntityManagerFactory.class, AbstractEntityManagerFactoryBean.class);
    }
}
