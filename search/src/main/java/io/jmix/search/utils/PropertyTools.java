/*
 * Copyright 2020 Haulmont.
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

package io.jmix.search.utils;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component("search_PropertyTools")
public class PropertyTools {

    private static final Logger log = LoggerFactory.getLogger(PropertyTools.class);

    public Map<String, MetaPropertyPath> findPropertyPaths(MetaClass metaClass, String pathString) {
        log.info("[IVGA] findPropertyPaths: MetaClass={}, PathString={}", metaClass, pathString);
        if(hasWildcard(pathString)) {
            return findPropertiesByWildcardPath(metaClass, pathString);
        } else {
            MetaPropertyPath propertyPath = metaClass.getPropertyPath(pathString);
            if(propertyPath == null) {
                log.warn("[IVGA] Class '{}' doesn't have property '{}'", metaClass, pathString);
                return Collections.emptyMap();
            } else {
                return Collections.singletonMap(pathString, propertyPath);
            }
        }
    }

    private Map<String, MetaPropertyPath> findPropertiesByWildcardPath(MetaClass metaClass, String path) {
        if(StringUtils.isBlank(path)) {
            return Collections.emptyMap();
        }
        String[] pathItems = path.split("\\.");
        return findByPath(metaClass, pathItems, new MetaPropertyPath(metaClass));
    }

    private Map<String, MetaPropertyPath> findByPath(MetaClass metaClass, String[] pathItems, MetaPropertyPath parentPath) {
        log.info("[IVGA] Find by path: Class={}, PathItems={}, parentPath={}", metaClass, Arrays.deepToString(pathItems), parentPath);
        if(pathItems.length == 0) {
            return Collections.emptyMap();
        }

        Map<String, MetaPropertyPath> result;

        String pathItem = pathItems[0];
        log.info("[IVGA] Path Item = {}", pathItem);
        if(pathItems.length == 1) { //Last
            log.info("[IVGA] '{}' is the last level of path", pathItem);
            if(hasWildcard(pathItem)) {
                Pattern pattern = Pattern.compile(pathItem.replace("*", ".*"));
                List<MetaProperty> localPropertiesByPattern = findLocalPropertiesByPattern(metaClass, pattern);
                result = localPropertiesByPattern.stream()
                        .filter(this::isSearchableProperty)
                        .map(property -> createPropertyPath(parentPath, property))
                        .collect(Collectors.toMap(MetaPropertyPath::toPathString, Function.identity()));
            } else {
                MetaProperty property = metaClass.findProperty(pathItem);
                if(property != null && isSearchableProperty(property)) {
                    result = new HashMap<>();
                    MetaPropertyPath newPath = createPropertyPath(parentPath, property);
                    result.put(newPath.toPathString(), newPath);
                } else {
                    result = Collections.emptyMap();
                }
            }
        } else {
            if(hasWildcard(pathItem)) {
                Pattern pattern = Pattern.compile(pathItem.replace("*", ".*"));
                List<MetaProperty> localPropertiesByPattern = findLocalPropertiesByPattern(metaClass, pattern);
                result = new HashMap<>();
                for(MetaProperty property : localPropertiesByPattern) {
                    if(isReferenceProperty(property)) {
                        MetaClass nextMetaClass = property.getRange().asClass();
                        MetaPropertyPath nextPath = createPropertyPath(parentPath, property);
                        result.putAll(findByPath(nextMetaClass, Arrays.copyOfRange(pathItems, 1, pathItems.length), nextPath));
                    }
                }

            } else {
                MetaProperty property = metaClass.findProperty(pathItem);
                if(property != null && isReferenceProperty(property)) {
                    MetaClass nextMetaClass = property.getRange().asClass();
                    MetaPropertyPath nextPath = createPropertyPath(parentPath, property);
                    result = findByPath(nextMetaClass, Arrays.copyOfRange(pathItems, 1, pathItems.length), nextPath);
                } else {
                    result = Collections.emptyMap();
                }
            }
        }
        return result;
    }

    private MetaPropertyPath createPropertyPath(MetaPropertyPath parentPath, MetaProperty property) {
        return new MetaPropertyPath(parentPath, property);
    }

    private List<MetaProperty> findLocalPropertiesByPattern(MetaClass metaClass, Pattern pattern) {
        Collection<MetaProperty> allLocalProperties = metaClass.getProperties();
        return allLocalProperties.stream()
                .filter((property) -> pattern.matcher(property.getName()).matches())
                .collect(Collectors.toList());
    }

    protected boolean isSearchableProperty(MetaProperty metaProperty) {
        //return !metaProperty.getRange().isClass(); // todo
        return true;
    }

    protected boolean isReferenceProperty(MetaProperty metaProperty) {
        return metaProperty.getRange().isClass();
    }

    protected boolean hasWildcard(String path) {
        return path.contains("*");
    }
}
