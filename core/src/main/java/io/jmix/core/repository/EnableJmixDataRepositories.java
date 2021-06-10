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

package io.jmix.core.repository;


import io.jmix.core.impl.repository.support.JmixDataRepositoryImpl;
import io.jmix.core.impl.repository.support.JmixRepositoriesRegistrar;
import io.jmix.core.impl.repository.support.JmixRepositoryFactoryBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.repository.query.QueryLookupStrategy;

import java.lang.annotation.*;

import static org.springframework.context.annotation.ComponentScan.Filter;

/**
 * Annotation to enable data repositories. Will scan the package of the annotated configuration class for Jmix data
 * repositories by default.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(JmixRepositoriesRegistrar.class)
public @interface EnableJmixDataRepositories {

    /**
     * Alias for the {@link #basePackages()} attribute. Allows for more concise annotation declarations e.g.:
     * {@code @EnableJmixDataRepositories("org.my.pkg")} instead of
     * {@code @EnableJmixDataRepositories(basePackages="org.my.pkg")}.
     *
     * @return list of packages that should be scanned
     */
    String[] value() default {};

    /**
     * Base packages to scan for annotated components. {@link #value()} is an alias for (and mutually exclusive with)
     * this attribute. Use {@link #basePackageClasses()} for a type-safe alternative to String-based package names.
     *
     * @return list of packages that should be scanned
     */
    String[] basePackages() default {};

    /**
     * Type-safe alternative to {@link #basePackages()} for specifying the packages to scan for annotated components.
     * The package of each class specified will be scanned. Consider creating a special no-op marker class or interface
     * in each package that serves no purpose other than being referenced by this attribute.
     *
     * @return list of packages that should be scanned
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * Specifies which types are not eligible for component scanning.
     *
     * @return filters that exclude some classes from scanning
     */
    Filter[] excludeFilters() default {};

    /**
     * Specifies which types are eligible for component scanning. Further narrows the set of candidate components from
     * everything in {@link #basePackages()} to everything in the base packages that matches the given filter or
     * filters.
     *
     * @return filters that include classes into scanning
     */
    Filter[] includeFilters() default {};

    /**
     * Returns the postfix to be used when looking up custom repository implementations. Defaults to {@literal Impl}. So
     * for a repository named {@code PersonRepository} the corresponding implementation class will be looked up scanning
     * for {@code PersonRepositoryImpl}.
     *
     * @return Postfix to be used when looking up custom repository implementations.
     */
    String repositoryImplementationPostfix() default "Impl";

    /**
     * Configures the location of where to find the Spring Data named queries properties file.
     *
     * @return Location of where to find the Spring Data named queries properties file.
     */
    String namedQueriesLocation() default "";

    /**
     * Returns the key of the {@link QueryLookupStrategy} to be used for lookup queries for query methods. Defaults to
     * {@link QueryLookupStrategy.Key#CREATE_IF_NOT_FOUND}.
     *
     * @return Key of the {@link QueryLookupStrategy} to be used for lookup queries for query methods.
     */
    QueryLookupStrategy.Key queryLookupStrategy() default QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND;

    /**
     * Returns the {@link FactoryBean} class to be used for each repository instance. Defaults to
     * {@link JmixRepositoryFactoryBean}.
     *
     * @return {@link FactoryBean} class to be used for each repository instance.
     */
    Class<?> repositoryFactoryBeanClass() default JmixRepositoryFactoryBean.class;

    /**
     * Configure the repository base class to be used to create repository proxies for this particular configuration.
     *
     * @return Repository base class to be used to create repository proxies for this particular configuration.
     */
    Class<?> repositoryBaseClass() default JmixDataRepositoryImpl.class;

    /**
     * Configures whether nested repository-interfaces (e.g. defined as inner classes) should be discovered by the
     * repository infrastructure.
     *
     * @return Flag that indicates if we should consider nested repository during scan
     */
    boolean considerNestedRepositories() default false;


}
