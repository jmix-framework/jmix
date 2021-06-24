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

import graphql.GraphQLContext;
import graphql.kickstart.servlet.context.DefaultGraphQLServletContext;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.graphql.NamingUtils;
import io.leangen.graphql.spqr.spring.autoconfigure.DefaultGlobalContext;
import io.leangen.graphql.util.ContextUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jmix.graphql.NamingUtils.SYS_ATTR_INSTANCE_NAME;

@Component("gql_EnvironmentUtils")
public class EnvironmentUtils {

    @Autowired
    MetadataTools metadataTools;
    @Autowired
    private Metadata metadata;

    /**
     * @param environment gql data fetch environment
     * @return true if _instanceName should be returned for query, else false
     */
    public boolean hasInstanceNameProperty(DataFetchingEnvironment environment) {
        return getPropertyPaths(environment)
                .anyMatch(def -> def.equals(SYS_ATTR_INSTANCE_NAME));
    }

    /**
     * @param props gql data fetch properties
     * @return true if _instanceName should be returned for query, else false
     */
    public boolean hasInstanceNameProperty(Set<String> props) {
        return props != null && props.stream()
                .anyMatch(def -> def.equals(SYS_ATTR_INSTANCE_NAME));
    }

    public Set<String> getDotDelimitedProps(DataFetchingEnvironment environment) {
        return getPropertyPaths(environment)
                .collect(Collectors.toSet());
    }

    /**
     * Filter out system properties such _instanceName or --typeName that not exist as entity fields.
     * Such properties must not be added in fetch plan.
     *
     * @param environment gql data fetch environment
     * @return only properties that exist in entity as fields
     */
    public List<String> getEntityProperties(DataFetchingEnvironment environment) {
        return getPropertyPaths(environment)
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
    public Predicate<String> propertyNotMatch(String property) {
        return prop -> !prop.equals(property) && !prop.matches(".*\\." + property);
    }

    public Set<String> getNestedProps(Set<String> props, String propName) {
        return props.stream()
                .filter(p -> p.startsWith(propName) && !p.equals(propName))
                .map(p -> p.replaceFirst("^" + propName + "\\.", ""))
                .collect(Collectors.toSet());
    }

    public Stream<String> getPropertyPaths(DataFetchingEnvironment environment) {
        return getPaths(environment.getSelectionSet(), "").stream();
    }

    public List<String> getPaths(DataFetchingFieldSelectionSet selectionSet, String currentPath) {
        List<String> result = new ArrayList<>();
        selectionSet.getImmediateFields().forEach(field -> {
            // additional attrs should to be added to fetch plan for proper _instanceName compose
            if (field.getQualifiedName().equals(SYS_ATTR_INSTANCE_NAME)) {
                String metaClassName = field.getFullyQualifiedName()
                        .replace("." + field.getQualifiedName(), "");
                MetaClass metaClass = findMetaClassByOutTypeName(metaClassName);

                metadataTools.getInstanceNameRelatedProperties(metaClass).forEach(prop ->
                        result.add(StringUtils.isEmpty(currentPath)
                                ? prop.getName() : currentPath + "." + prop.getName()));
            }

            String path = StringUtils.isEmpty(currentPath)
                    ? field.getQualifiedName() : currentPath + "." + field.getQualifiedName();

            if (field.getSelectionSet().getImmediateFields().isEmpty()) {
                result.add(path);
            } else {
                result.addAll(getPaths(field.getSelectionSet(), path));
            }
        });
        return result;
    }

    @Nullable
    public static String getRemoteIPAddress(Object context) {
        if (context instanceof GraphQLContext) {
            context = ContextUtils.unwrapContext(context);
        }

        HttpServletRequest httpServletRequest = null;
        if (context instanceof DefaultGlobalContext) {
            DefaultGlobalContext<ServletWebRequest> defaultCtx = (DefaultGlobalContext<ServletWebRequest>) context;
            httpServletRequest = defaultCtx.getNativeRequest().getNativeRequest(HttpServletRequest.class);
        }

        if (context instanceof DefaultGraphQLServletContext) {
            DefaultGraphQLServletContext gqlContext = (DefaultGraphQLServletContext) context;
            httpServletRequest = gqlContext.getHttpServletRequest();
        }

        return httpServletRequest == null ? null : httpServletRequest.getRemoteAddr();
    }

    protected MetaClass findMetaClassByOutTypeName(String outTypeName) {
        MetaClass result = metadata.findClass(outTypeName);
        if (result == null) {
            result = metadata.findClass(outTypeName .replaceAll("_", "\\$"));
        }
        if (result == null) {
            throw new UnsupportedOperationException("No matched meta class found for out type " + outTypeName);
        }
        return result;
    }


}
