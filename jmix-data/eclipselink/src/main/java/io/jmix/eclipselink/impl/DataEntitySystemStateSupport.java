/*
 * Copyright 2019 Haulmont.
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

package io.jmix.eclipselink.impl;

import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.eclipselink.impl.lazyloading.AbstractValueHolder;
import io.jmix.eclipselink.impl.lazyloading.IndirectListWrapper;
import io.jmix.eclipselink.impl.lazyloading.IndirectSetWrapper;
import org.eclipse.persistence.indirection.IndirectList;
import org.eclipse.persistence.indirection.IndirectSet;
import org.eclipse.persistence.internal.queries.EntityFetchGroup;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.lang.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static io.jmix.eclipselink.impl.lazyloading.ValueHoldersSupport.*;

public class DataEntitySystemStateSupport extends EntitySystemStateSupport {

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected EntityStates entityStates;

    public void copySystemState(Entity src, Entity dst) {
        super.copySystemState(src, dst);

        if (src instanceof FetchGroupTracker && dst instanceof FetchGroupTracker) {
            FetchGroup srcFetchGroup = ((FetchGroupTracker) src)._persistence_getFetchGroup();
            ((FetchGroupTracker) dst)._persistence_setFetchGroup(srcFetchGroup);
        }
    }

    public void mergeSystemState(Entity src, Entity dst) {
        super.copySystemState(src, dst);

        if (src instanceof FetchGroupTracker && dst instanceof FetchGroupTracker) {
            FetchGroup srcFetchGroup = ((FetchGroupTracker) src)._persistence_getFetchGroup();
            FetchGroup dstFetchGroup = ((FetchGroupTracker) dst)._persistence_getFetchGroup();
            if (dstFetchGroup == null && entityStates.isNew(dst)) {
                // dst is a new entity replaced by committed one
                ((FetchGroupTracker) dst)._persistence_setFetchGroup(srcFetchGroup);
            } else {
                ((FetchGroupTracker) dst)._persistence_setFetchGroup(mergeFetchGroups(srcFetchGroup, dstFetchGroup));
            }
        }
    }

    @Override
    public void mergeLazyLoadingState(Entity src, Entity dst, MetaProperty metaProperty,
                                      Function<Collection<Object>, Collection<Object>> collectionWrapFunction) {
        if (!metadataTools.isEmbedded(metaProperty)) {
            if (metaProperty.getRange().getCardinality().isMany()) {
                Object value = getCollectionProperty(src, metaProperty.getName());

                if (value instanceof IndirectList) {
                    //noinspection unchecked
                    Collection<Object> collection = (Collection<Object>) value;
                    //noinspection unchecked
                    IndirectList<Object> indirectList = (IndirectList<Object>) value;

                    Collection<Object> wrappedCollection =
                            new IndirectListWrapper<>((List<Object>) collectionWrapFunction.apply(collection), indirectList);

                    setCollectionProperty(dst, metaProperty.getName(), wrappedCollection);
                } else if (value instanceof IndirectSet) {
                    //noinspection unchecked
                    Collection<Object> collection = (Collection<Object>) value;
                    //noinspection unchecked
                    IndirectSet<Object> indirectSet = (IndirectSet<Object>) value;

                    Collection<Object> wrappedCollection =
                            new IndirectSetWrapper<>((Set<Object>) collectionWrapFunction.apply(collection), indirectSet);

                    setCollectionProperty(dst, metaProperty.getName(), wrappedCollection);
                }
            } else {
                Object valueHolder = getSingleValueHolder(src, metaProperty.getName());
                if (valueHolder instanceof AbstractValueHolder) {
                    setSingleValueHolder(dst, metaProperty.getName(), valueHolder);
                }
            }
        }
    }

    @Nullable
    protected FetchGroup mergeFetchGroups(@Nullable FetchGroup first, @Nullable FetchGroup second) {
        Set<String> attributes = new HashSet<>();
        if (first == null || second == null) {
            return null;
        }
        attributes.addAll(getFetchGroupAttributes(first));
        attributes.addAll(getFetchGroupAttributes(second));
        return new JmixEntityFetchGroup(new EntityFetchGroup(attributes), entityStates);
    }

    protected Collection<String> getFetchGroupAttributes(FetchGroup fetchGroup) {
        Set<String> result = new HashSet<>();
        traverseFetchGroupAttributes(result, fetchGroup, "");
        return result;
    }

    protected void traverseFetchGroupAttributes(Set<String> set, FetchGroup fetchGroup, String prefix) {
        for (String attribute : fetchGroup.getAttributeNames()) {
            FetchGroup group = fetchGroup.getGroup(attribute);
            if (group != null) {
                traverseFetchGroupAttributes(set, group, prefix + attribute + ".");
            } else {
                set.add(prefix + attribute);
            }
        }
    }
}
