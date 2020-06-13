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

package io.jmix.uidata;

import io.jmix.core.Metadata;
import io.jmix.core.UuidProvider;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.common.xmlparsing.Dom4jTools;
import io.jmix.core.entity.BaseUser;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.*;
import io.jmix.uidata.entity.UiTablePresentation;
import io.jmix.uidata.entity.UiSetting;
import io.jmix.ui.settings.UserSettingService;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.*;

public class UserSettingServiceImpl implements UserSettingService {

    @Autowired
    protected CurrentAuthentication authentication;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected Security security;

    @Autowired
    protected Dom4jTools dom4JTools;

    @PersistenceContext
    protected EntityManager entityManager;

    protected TransactionTemplate transaction;

    @Autowired
    protected void setTransactionManager(PlatformTransactionManager transactionManager) {
        transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public String loadSetting(String name) {
        Preconditions.checkNotNullArgument(name);

        String value = transaction.execute(status -> {
            UiSetting us = findUserSettings(name);
            return us == null ? null : us.getValue();
        });

        return value;
    }

    @Override
    public void saveSetting(String name, @Nullable String value) {
        Preconditions.checkNotNullArgument(name);

        transaction.executeWithoutResult(status -> {
            UiSetting us = findUserSettings(name);
            if (us == null) {
                us = metadata.create(UiSetting.class);
                us.setUserLogin(authentication.getUser().getUsername());
                us.setName(name);
                us.setValue(value);

                entityManager.persist(us);
            } else {
                us.setValue(value);
            }
        });
    }

    @Override
    public void deleteSettings(String name) {
        Preconditions.checkNotNullArgument(name);

        transaction.executeWithoutResult(status -> {
            UiSetting us = findUserSettings(name);
            if (us != null) {
                entityManager.remove(us);
            }
        });
    }

    @Override
    public void copySettings(BaseUser fromUser, BaseUser toUser) {
        Preconditions.checkNotNullArgument(fromUser);
        Preconditions.checkNotNullArgument(toUser);

        MetaClass metaClass = metadata.getClass(UiSetting.class);

        if (!security.isEntityOpPermitted(metaClass, EntityOp.CREATE)) {
            throw new AccessDeniedException(PermissionType.ENTITY_OP, metaClass.getName());
        }

        transaction.executeWithoutResult(status -> {
            Query deleteSettingsQuery = entityManager.createQuery("delete from ui_Setting s where s.userLogin = ?1");
            deleteSettingsQuery.setParameter(1, toUser.getUsername());
            deleteSettingsQuery.executeUpdate();
        });

        Map<UUID, UiTablePresentation> presentationsMap = copyPresentations(fromUser, toUser);

        /* todo folders panel
        copyUserFolders(fromUser, toUser, presentationsMap);

        todo filter
        Map<UUID, FilterEntity> filtersMap = copyFilters(fromUser, toUser);
        */

        transaction.executeWithoutResult(status -> {
            TypedQuery<UiSetting> q = entityManager.
                    createQuery("select s from ui_Setting s where s.userLogin = ?1", UiSetting.class);
            q.setParameter(1, fromUser.getUsername());
            List<UiSetting> fromUserSettings = q.getResultList();

            for (UiSetting currSetting : fromUserSettings) {
                UiSetting newSetting = metadata.create(UiSetting.class);
                newSetting.setUserLogin(toUser.getUsername());
                newSetting.setName(currSetting.getName());

                try {
                    Document doc = dom4JTools.readDocument(currSetting.getValue());

                    List<Element> components = doc.getRootElement().element("components").elements("component");
                    for (Element component : components) {
                        Attribute presentationAttr = component.attribute("presentation");
                        if (presentationAttr != null) {
                            UUID presentationId = UuidProvider.fromString(presentationAttr.getValue());
                            UiTablePresentation newPresentation = presentationsMap.get(presentationId);
                            if (newPresentation != null) {
                                presentationAttr.setValue(newPresentation.getId().toString());
                            }
                        }
                        /* todo filter
                        Element defaultFilterEl = component.element("defaultFilter");
                        if (defaultFilterEl != null) {
                            Attribute idAttr = defaultFilterEl.attribute("id");
                            if (idAttr != null) {
                                UUID filterId = UuidProvider.fromString(idAttr.getValue());
                                FilterEntity newFilter = filtersMap.get(filterId);
                                if (newFilter != null) {
                                    idAttr.setValue(newFilter.getId().toString());
                                }
                            }
                        }*/
                    }

                    newSetting.setValue(dom4JTools.writeDocument(doc, true));
                } catch (Exception e) {
                    newSetting.setValue(currSetting.getValue());
                }
                entityManager.persist(newSetting);
            }
        });
    }

    @Override
    public void deleteScreenSettings(Set<String> screens) {
        transaction.executeWithoutResult(status -> {
            TypedQuery<UiSetting> selectQuery = entityManager.createQuery(
                    "select e from ui_Setting e where e.user.id = ?1",
                    UiSetting.class);
            selectQuery.setParameter(1, authentication.getUser().getUsername());

            List<UiSetting> userSettings = selectQuery.getResultList();

            for (UiSetting userSetting : userSettings) {
                if (screens.contains(userSetting.getName())) {
                    entityManager.remove(userSetting);
                }
            }
        });
    }

    @Nullable
    protected UiSetting findUserSettings(String name) {
        TypedQuery<UiSetting> q = entityManager.createQuery(
                "select s from ui_Setting s where s.userLogin = ?1 and s.name =?2",
                UiSetting.class);
        q.setParameter(1, authentication.getUser().getUsername());
        q.setParameter(2, name);

        List result = q.getResultList();
        if (result.isEmpty()) {
            return null;
        }

        return (UiSetting) result.get(0);
    }

    protected Map<UUID, UiTablePresentation> copyPresentations(BaseUser fromUser, BaseUser toUser) {
        Map<UUID, UiTablePresentation> resultMap = transaction.execute(status -> {
            // delete existing
            Query delete = entityManager.createQuery("delete from ui_TablePresentation p where p.userLogin = ?1");
            delete.setParameter(1, toUser.getUsername());
            delete.executeUpdate();

            // copy settings
            TypedQuery<UiTablePresentation> selectQuery = entityManager.createQuery(
                    "select p from ui_TablePresentation p where p.userLogin = ?1", UiTablePresentation.class);
            selectQuery.setParameter(1, fromUser.getUsername());
            List<UiTablePresentation> presentations = selectQuery.getResultList();

            Map<UUID, UiTablePresentation> presentationMap = new HashMap<>();
            for (UiTablePresentation presentation : presentations) {
                UiTablePresentation newPresentation = metadata.create(UiTablePresentation.class);
                newPresentation.setUserLogin(toUser.getUsername());
                newPresentation.setComponentId(presentation.getComponentId());
                newPresentation.setAutoSave(presentation.getAutoSave());
                newPresentation.setName(presentation.getName());
                newPresentation.setSettings(presentation.getSettings());
                presentationMap.put(presentation.getId(), newPresentation);
                entityManager.persist(newPresentation);
            }
            return presentationMap;
        });

        return resultMap;
    }

    /* todo folders panel
    protected void copyUserFolders(User fromUser, User toUser, Map<UUID, Presentation> presentationsMap) {
        try (Transaction tx = persistence.createTransaction()) {
            MetaClass effectiveMetaClass = metadata.getExtendedEntities().getEffectiveMetaClass(SearchFolder.class);
            EntityManager em = persistence.getEntityManager();
            try {
                em.setSoftDeletion(false);
                Query deleteSettingsQuery = em.createQuery(
                        String.format("delete from %s s where s.user.id = ?1", effectiveMetaClass.getName())
                );

                deleteSettingsQuery.setParameter(1, toUser.getId());
                deleteSettingsQuery.executeUpdate();
            } finally {
                em.setSoftDeletion(true);
            }
            TypedQuery<SearchFolder> q = em.createQuery(
                    String.format("select s from %s s where s.user.id = ?1", effectiveMetaClass.getName()),
                    SearchFolder.class);
            q.setParameter(1, fromUser.getId());

            List<SearchFolder> fromUserFolders = q.getResultList();
            Map<SearchFolder, SearchFolder> copiedFolders = new HashMap<>();
            for (SearchFolder searchFolder : fromUserFolders) {
                copyFolder(searchFolder, toUser, copiedFolders, presentationsMap);
            }
            tx.commit();
        }
    }

    protected SearchFolder copyFolder(SearchFolder searchFolder,
                                      User toUser,
                                      Map<SearchFolder, SearchFolder> copiedFolders,
                                      Map<UUID, Presentation> presentationsMap) {
        SearchFolder newFolder;
        if (searchFolder.getUser() == null)
            return searchFolder;
        newFolder = copiedFolders.get(searchFolder);
        if (newFolder != null)
            return null;
        newFolder = metadata.create(SearchFolder.class);
        newFolder.setUser(toUser);
        newFolder.setApplyDefault(searchFolder.getApplyDefault());
        newFolder.setFilterComponentId(searchFolder.getFilterComponentId());
        newFolder.setFilterXml(searchFolder.getFilterXml());
        newFolder.setItemStyle(searchFolder.getItemStyle());
        newFolder.setName(searchFolder.getName());
        newFolder.setTabName(searchFolder.getTabName());
        newFolder.setSortOrder(searchFolder.getSortOrder());
        newFolder.setIsSet(searchFolder.getIsSet());
        newFolder.setEntityType(searchFolder.getEntityType());
        SearchFolder copiedFolder = copiedFolders.get(searchFolder.getParent());
        if (searchFolder.getParent() != null) {
            if (copiedFolder != null) {
                newFolder.setParent(copiedFolder);
            } else {
                SearchFolder newParent = getParent((SearchFolder) searchFolder.getParent(), toUser, copiedFolders, presentationsMap);
                newFolder.setParent(newParent);
            }
        }
        if (searchFolder.getPresentation() != null) {
            if (searchFolder.getPresentation().getUser() == null) {
                newFolder.setPresentation(searchFolder.getPresentation());
            } else {
                Presentation newPresentation = presentationsMap.get(searchFolder.getPresentation().getId());
                newFolder.setPresentation(newPresentation);
            }
        }
        copiedFolders.put(searchFolder, newFolder);
        EntityManager em = persistence.getEntityManager();
        em.persist(newFolder);
        return newFolder;
    }

    protected SearchFolder getParent(SearchFolder parentFolder, User toUser, Map<SearchFolder, SearchFolder> copiedFolders, Map<UUID, Presentation> presentationMap) {
        if (parentFolder == null) {
            return null;
        }
        if (parentFolder.getUser() == null) {
            return parentFolder;
        }
        return copyFolder(parentFolder, toUser, copiedFolders, presentationMap);
    }

    protected Map<UUID, FilterEntity> copyFilters(User fromUser, User toUser) {
        Map<UUID, FilterEntity> filtersMap = new HashMap<>();

        try (Transaction tx = persistence.createTransaction()) {
            MetaClass effectiveMetaClass = metadata.getExtendedEntities().getEffectiveMetaClass(FilterEntity.class);

            EntityManager em = persistence.getEntityManager();
            try {
                em.setSoftDeletion(false);
                Query deleteFiltersQuery = em.createQuery(
                        String.format("delete from %s f where f.user.id = ?1", effectiveMetaClass.getName())
                );
                deleteFiltersQuery.setParameter(1, toUser.getId());
                deleteFiltersQuery.executeUpdate();
            } finally {
                em.setSoftDeletion(true);
            }

            TypedQuery<FilterEntity> q = em.createQuery(
                    String.format("select f from %s f where f.user.id = ?1", effectiveMetaClass.getName()),
                    FilterEntity.class);
            q.setParameter(1, fromUser.getId());
            List<FilterEntity> fromUserFilters = q.getResultList();

            for (FilterEntity filter : fromUserFilters) {
                FilterEntity newFilter = metadata.create(FilterEntity.class);
                newFilter.setUser(toUser);
                newFilter.setCode(filter.getCode());
                newFilter.setName(filter.getName());
                newFilter.setComponentId(filter.getComponentId());
                newFilter.setXml(filter.getXml());
                filtersMap.put(filter.getId(), newFilter);
                em.persist(newFilter);
            }

            tx.commit();
            return filtersMap;
        }
    }*/
}
