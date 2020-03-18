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

package io.jmix.data;

import io.jmix.core.EntityStates;
import io.jmix.core.EntitySystemStateSupport;
import io.jmix.core.Metadata;
import io.jmix.core.Entity;
import io.jmix.core.metamodel.model.MetadataObject;
import io.jmix.data.impl.JmixEntityFetchGroup;
import org.eclipse.persistence.internal.queries.EntityFetchGroup;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.FetchGroupTracker;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class DataEntitySystemStateSupport extends EntitySystemStateSupport {

    @Inject
    protected Metadata metadata;

    @Inject
    protected EntityStates entityStates;

    public void copySystemState(Entity<?> src, Entity<?> dst) {
        super.copySystemState(src, dst);

        if (src instanceof FetchGroupTracker && dst instanceof FetchGroupTracker) {
            FetchGroup srcFetchGroup = ((FetchGroupTracker) src)._persistence_getFetchGroup();
            ((FetchGroupTracker) dst)._persistence_setFetchGroup(srcFetchGroup);
        }

    }

    public void mergeSystemState(Entity<?> src, Entity<?> dst) {
        super.copySystemState(src, dst);

        if (src instanceof FetchGroupTracker && dst instanceof FetchGroupTracker) {
            FetchGroup srcFetchGroup = ((FetchGroupTracker) src)._persistence_getFetchGroup();
            if (!entityStates.isNew(src) && srcFetchGroup == null) {
                // case when merging entity returned from DataManager.commit
                srcFetchGroup = suggestFetchGroup(src);
            }
            FetchGroup dstFetchGroup = ((FetchGroupTracker) dst)._persistence_getFetchGroup();
            if (dstFetchGroup == null) {
                // dst is a new entity replaced by committed one
                ((FetchGroupTracker) dst)._persistence_setFetchGroup(srcFetchGroup);
            } else {
                ((FetchGroupTracker) dst)._persistence_setFetchGroup(mergeFetchGroups(srcFetchGroup, dstFetchGroup));
            }
        }
    }

    protected FetchGroup suggestFetchGroup(Entity<?> entity) {
        Set<String> attributes = metadata.getClass(entity.getClass()).getProperties().stream()
                .filter(metaProperty ->
                        !metaProperty.getRange().isClass() || entityStates.isLoaded(entity, metaProperty.getName()))
                .map(MetadataObject::getName)
                .collect(Collectors.toSet());

        return new JmixEntityFetchGroup(new EntityFetchGroup(attributes));
    }

    protected FetchGroup mergeFetchGroups(@Nullable FetchGroup first, @Nullable FetchGroup second) {
        Set<String> attributes = new HashSet<>();
        if (first != null)
            attributes.addAll(getFetchGroupAttributes(first));
        if (second != null)
            attributes.addAll(getFetchGroupAttributes(second));
        return new JmixEntityFetchGroup(new EntityFetchGroup(attributes));
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
