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

package io.jmix.dynattr.impl;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.jmix.core.*;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.PersistenceHints;
import io.jmix.data.StoreAwareLocator;
import io.jmix.dynattr.*;
import io.jmix.dynattr.impl.model.Category;
import io.jmix.dynattr.impl.model.CategoryAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;

@Component(DynAttrMetadata.NAME)
public class DynAttrMetadataImpl implements DynAttrMetadata {

    @Autowired
    protected StoreAwareLocator storeAwareLocator;
    @Autowired
    protected ExtendedEntities extendedEntities;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected DatatypeRegistry datatypeRegistry;
    @Autowired
    FetchPlans fetchPlans;

    protected volatile Cache cache;

    protected String dynamicAttributesStore = Stores.MAIN;

    private static final Logger log = LoggerFactory.getLogger(DynAttrMetadataImpl.class);

    @Override
    public Collection<AttributeDefinition> getAttributes(MetaClass metaClass) {
        return getCache().getAttributes(metaClass);
    }

    @Override
    public Optional<AttributeDefinition> getAttributeByCode(MetaClass metaClass, String code) {
        return getCache().getAttributeByCode(metaClass, code);
    }

    @Override
    public Collection<CategoryDefinition> getCategories(MetaClass metaCLass) {
        return getCache().getCategories(metaCLass);
    }

    @Override
    public void reload() {
        cache = doLoadCache();
    }

    protected Cache getCache() {
        if (cache == null) {
            Cache newCache = doLoadCache();
            if (cache == null) {
                cache = newCache;
            }
        }
        return cache;
    }

    protected Cache doLoadCache() {
        Multimap<String, CategoryDefinition> categoriesCache = HashMultimap.create();
        Map<String, Map<String, AttributeDefinition>> attributesCache = new LinkedHashMap<>();

        for (CategoryDefinition category : loadCategoryDefinitions()) {
            if (category.getEntityType() != null) {
                MetaClass metaClass = metadata.findClass(category.getEntityType());
                if (metaClass != null) {
                    metaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
                    categoriesCache.put(metaClass.getName(), category);

                    Map<String, AttributeDefinition> attributes = attributesCache.computeIfAbsent(metaClass.getName(),
                            k -> new LinkedHashMap<>());
                    for (AttributeDefinition attribute : category.getAttributeDefinitions()) {
                        attributes.put(attribute.getCode(), attribute);
                    }
                } else {
                    log.warn("Could not resolve meta class name {} for the category {}.",
                            category.getEntityType(), category.getName());
                }
            }
        }

        return new Cache(categoriesCache, attributesCache);
    }

    protected List<CategoryDefinition> loadCategoryDefinitions() {
        //noinspection ConstantConditions
        return storeAwareLocator.getTransactionTemplate(dynamicAttributesStore)
                .execute(transactionStatus -> {
                    EntityManager entityManager = storeAwareLocator.getEntityManager(dynamicAttributesStore);

                    FetchPlan fetchPlan = fetchPlans.builder(Category.class)
                            .addFetchPlan(FetchPlan.LOCAL)
                            .add("categoryAttrs", builder -> {
                                builder.addFetchPlan(FetchPlan.LOCAL);
                                builder.add("category", FetchPlan.LOCAL);
                                builder.add("defaultEntity", FetchPlan.LOCAL);
                            })
                            .build();

                    return entityManager.createQuery("select c from sys_Category c", Category.class)
                            .setHint(PersistenceHints.FETCH_PLAN, fetchPlan)
                            .getResultList().stream()
                            .map(this::buildCategoryDefinition)
                            .collect(Collectors.toList());
                });
    }

    protected CategoryDefinition buildCategoryDefinition(Category category) {
        MetaClass metaClass = extendedEntities.getOriginalOrThisMetaClass(metadata.getClass(category.getEntityType()));
        List<AttributeDefinition> attributes;
        if (category.getCategoryAttrs() != null) {
            attributes = Collections.unmodifiableList(category.getCategoryAttrs().stream()
                    .map(attr -> new CommonAttributeDefinition(attr, buildMetaProperty(attr, metaClass)))
                    .collect(Collectors.toList()));
        } else {
            attributes = Collections.emptyList();
        }
        return new CommonCategoryDefinition(category, attributes);
    }

    protected MetaProperty buildMetaProperty(CategoryAttribute categoryAttribute, MetaClass metaClass) {
        String name = DynAttrUtils.getPropertyFromAttributeCode(categoryAttribute.getCode());

        Class<?> javaClass;
        Datatype<?> datatype = null;
        MetaClass propertyMetaClass = null;
        if (categoryAttribute.getDataType() == AttributeType.ENTITY) {
            javaClass = ReflectionHelper.getClass(categoryAttribute.getEntityClass());
            propertyMetaClass = extendedEntities.getOriginalOrThisMetaClass(metadata.getClass(javaClass));
        } else {
            javaClass = DynAttrUtils.getDatatypeClass(categoryAttribute.getDataType());
            datatype = datatypeRegistry.get(javaClass);
        }
        return new DynAttrMetaProperty(name, metaClass, javaClass, propertyMetaClass, datatype);
    }

    protected class Cache {
        protected final Multimap<String, CategoryDefinition> categories;
        protected final Map<String, Map<String, AttributeDefinition>> attributes;

        public Cache(Multimap<String, CategoryDefinition> categories, Map<String, Map<String, AttributeDefinition>> attributes) {
            this.categories = categories;
            this.attributes = attributes;
        }

        public Collection<CategoryDefinition> getCategories(MetaClass metaClass) {
            Collection<CategoryDefinition> targetCategories = categories.get(
                    extendedEntities.getOriginalOrThisMetaClass(metaClass).getName());
            return Collections.unmodifiableCollection(targetCategories);
        }

        public Collection<AttributeDefinition> getAttributes(MetaClass metaClass) {
            Collection<CategoryDefinition> targetCategories = categories.get(
                    extendedEntities.getOriginalOrThisMetaClass(metaClass).getName());
            return targetCategories.stream()
                    .flatMap(c -> c.getAttributeDefinitions().stream())
                    .filter(a -> !Strings.isNullOrEmpty(a.getCode()))
                    .collect(Collectors.toList());
        }

        public Optional<AttributeDefinition> getAttributeByCode(MetaClass metaClass, String code) {
            Map<String, AttributeDefinition> targetAttributes = attributes.get(
                    extendedEntities.getOriginalOrThisMetaClass(metaClass).getName());
            AttributeDefinition attribute = null;
            if (targetAttributes != null) {
                attribute = targetAttributes.get(code);
            }

            return Optional.ofNullable(attribute);
        }
    }
}
