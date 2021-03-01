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

package io.jmix.graphql.datafetcher;

import graphql.schema.DataFetchingEnvironment;
import io.jmix.graphql.schema.NamingUtils;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.jmix.graphql.schema.NamingUtils.SYS_ATTR_INSTANCE_NAME;

public class EnvironmentUtils {

    /**
     * @param environment gql data fetch environment
     * @return true if _instanceName should be returned for query, else false
     */
    public static boolean hasInstanceNameProperty(DataFetchingEnvironment environment) {
        return environment.getSelectionSet().getDefinitions().keySet().stream()
                .anyMatch(def -> def.equals(NamingUtils.SYS_ATTR_INSTANCE_NAME));
    }

    /**
     * @param props gql data fetch properties
     * @return true if _instanceName should be returned for query, else false
     */
    public static boolean hasInstanceNameProperty(Set<String> props) {
        return props != null && props.stream()
                .anyMatch(def -> def.equals(NamingUtils.SYS_ATTR_INSTANCE_NAME));
    }

    public static Set<String> getDotDelimitedProps(DataFetchingEnvironment environment) {
        return environment.getSelectionSet().getDefinitions().keySet().stream()
                .map(prop -> prop.replaceAll("/", "."))
                .collect(Collectors.toSet());
    }

    /**
     * Filter out system properties such _instanceName or --typeName that not exist as entity fields.
     * Such properties must not be added in fetch plan.
     * @param environment gql data fetch environment
     * @return only properties that exist in entity as fields
     */
    public static List<String> getEntityProperties(DataFetchingEnvironment environment) {
        return environment.getSelectionSet().getDefinitions().keySet().stream()
                .map(def -> def.replaceAll("/", "."))
                // remove '__typename' from fetch plan
                .filter(propertyNotMatch(NamingUtils.SYS_ATTR_TYPENAME))
                // todo fetch failed, if we need to return instanceName in nested entity,
                //  but fetch plan does not contains attrs of nested entities required to compose instanceName
                //  i.e. for garage.car.instanceName we need to request garage.car.manufacturer and garage.car.model attrs,
                //  which are required for composing Car instanceName
                // remove 'instanceName' and '*.instanceName' attrs from fetch plan - no such attr in entity
                .filter(propertyNotMatch(SYS_ATTR_INSTANCE_NAME))
                .collect(Collectors.toList());
    }


    /**
     * @param property property to check
     * @return true if property NOT match 'someProperty' and '*.someProperty'
     */
    public static Predicate<String> propertyNotMatch(String property) {
        return prop -> !prop.equals(property) && !prop.matches(".*\\." + property);
    }

    public static Set<String> getNestedProps(Set<String> props, String propName) {
        return props.stream()
                .filter(p -> p.startsWith(propName) && !p.equals(propName))
                .map(p -> p.replaceFirst("^" + propName + "\\.", ""))
                .collect(Collectors.toSet());
    }
}
