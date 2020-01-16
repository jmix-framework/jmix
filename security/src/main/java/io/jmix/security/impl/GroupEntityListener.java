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
package io.jmix.security.impl;

import io.jmix.core.EntityStates;
import io.jmix.core.Metadata;
import io.jmix.data.EntityManager;
import io.jmix.data.Persistence;
import io.jmix.data.TypedQuery;
import io.jmix.data.listener.BeforeInsertEntityListener;
import io.jmix.data.listener.BeforeUpdateEntityListener;
import io.jmix.security.entity.Group;
import io.jmix.security.entity.GroupHierarchy;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Component("jmix_GroupEntityListener")
public class GroupEntityListener implements
        BeforeInsertEntityListener<Group>,
        BeforeUpdateEntityListener<Group> {

    @Inject
    protected Persistence persistence;

    @Inject
    protected Metadata metadata;

    @Inject
    protected EntityStates entityStates;

    @Override
    public void onBeforeInsert(Group entity, EntityManager entityManager) {
        createNewHierarchy(entity, entity.getParent());
    }

    protected void createNewHierarchy(Group entity, Group parent) {
        if (parent == null) {
            entity.setHierarchyList(new ArrayList<>());

            return;
        }

        if (!entityStates.isManaged(parent) && !entityStates.isDetached(parent))
            throw new IllegalStateException("Unable to create GroupHierarchy. Commit parent group first.");

        EntityManager em = persistence.getEntityManager();

        if (entity.getHierarchyList() == null) {
            entity.setHierarchyList(new ArrayList<>());
        } else {
            entity.getHierarchyList().clear();
        }

        if (entityStates.isDetached(parent))
            parent = em.find(Group.class, parent.getId()); // refresh parent in case of detached

        int level = 0;
        if (parent.getHierarchyList() != null) {
            for (GroupHierarchy hierarchy : parent.getHierarchyList()) {
                GroupHierarchy h = metadata.create(GroupHierarchy.class);
                h.setGroup(entity);
                h.setParent(hierarchy.getParent());
                h.setLevel(level++);
                em.persist(h);
                entity.getHierarchyList().add(h);
            }
        }
        GroupHierarchy h = metadata.create(GroupHierarchy.class);
        h.setGroup(entity);
        h.setParent(parent);
        h.setLevel(level);
        em.persist(h);
        entity.getHierarchyList().add(h);
    }

    @Override
    public void onBeforeUpdate(Group entity, EntityManager entityManager) {
        if (!persistence.getTools().getDirtyFields(entity).contains("parent"))
            return;

        EntityManager em = persistence.getEntityManager();

        for (GroupHierarchy oldHierarchy : entity.getHierarchyList()) {
            em.remove(oldHierarchy);
        }
        createNewHierarchy(entity, entity.getParent());

        TypedQuery<GroupHierarchy> q = em.createQuery(
                "select h from sec_GroupHierarchy h join fetch h.group " +
                        "where h.parent.id = ?1", GroupHierarchy.class);
        q.setParameter(1, entity.getId());
        List<GroupHierarchy> list = q.getResultList();
        for (GroupHierarchy hierarchy : list) {
            Group dependentGroup = hierarchy.getGroup();
            for (GroupHierarchy depHierarchy : dependentGroup.getHierarchyList()) {
                em.remove(depHierarchy);
            }
            em.remove(hierarchy);
            createNewHierarchy(dependentGroup, dependentGroup.getParent());
        }
    }
}