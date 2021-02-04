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

package com.haulmont.cuba.security.app;

import com.haulmont.cuba.security.entity.FilterEntity;
import com.haulmont.cuba.security.entity.SearchFolder;
import io.jmix.core.UuidProvider;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.uidata.UserSettingServiceImpl;
import io.jmix.uidata.entity.UiSetting;
import io.jmix.uidata.entity.UiTablePresentation;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Nullable;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Deprecated
public class UserSettingServiceBean extends UserSettingServiceImpl {

    @Override
    public void copySettings(UserDetails fromUser, UserDetails toUser) {
        Preconditions.checkNotNullArgument(fromUser);
        Preconditions.checkNotNullArgument(toUser);

        MetaClass metaClass = metadata.getClass(UiSetting.class);

        CrudEntityContext entityContext = new CrudEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);

        if (!entityContext.isCreatePermitted()) {
            throw new AccessDeniedException("entity", metaClass.getName());
        }

        transaction.executeWithoutResult(status -> {
            Query deleteSettingsQuery =
                    entityManager.createQuery("delete from ui_Setting s where s.username = ?1");
            deleteSettingsQuery.setParameter(1, toUser.getUsername());
            deleteSettingsQuery.executeUpdate();
        });

        Map<UUID, UiTablePresentation> presentationsMap = copyPresentations(fromUser, toUser);

        copyUserFolders(fromUser, toUser, presentationsMap);

        Map<UUID, FilterEntity> filtersMap = copyFilters(fromUser, toUser);

        transaction.executeWithoutResult(status -> {
            TypedQuery<UiSetting> q = entityManager.
                    createQuery("select s from ui_Setting s where s.username = ?1", UiSetting.class);
            q.setParameter(1, fromUser.getUsername());
            List<UiSetting> fromUserSettings = q.getResultList();

            for (UiSetting currSetting : fromUserSettings) {
                UiSetting newSetting = metadata.create(UiSetting.class);
                newSetting.setUsername(toUser.getUsername());
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
                        }
                    }

                    newSetting.setValue(dom4JTools.writeDocument(doc, true));
                } catch (Exception e) {
                    newSetting.setValue(currSetting.getValue());
                }
                entityManager.persist(newSetting);
            }
        });
    }

    protected void copyUserFolders(UserDetails fromUser,
                                   UserDetails toUser,
                                   Map<UUID, UiTablePresentation> presentationsMap) {
        transaction.executeWithoutResult(status -> {
            Query deleteSettingsQuery =
                    entityManager.createQuery("delete from sec$SearchFolder s where s.username = ?1");
            deleteSettingsQuery.setParameter(1, toUser.getUsername());
            deleteSettingsQuery.executeUpdate();
        });

        transaction.executeWithoutResult(status -> {
            TypedQuery<SearchFolder> q =
                    entityManager.createQuery("select s from sec$SearchFolder s where s.username = ?1",
                            SearchFolder.class);
            q.setParameter(1, fromUser.getUsername());

            List<SearchFolder> fromUserFolders = q.getResultList();
            Map<SearchFolder, SearchFolder> copiedFolders = new HashMap<>();
            for (SearchFolder searchFolder : fromUserFolders) {
                copyFolder(searchFolder, toUser, copiedFolders, presentationsMap);
            }
        });
    }

    @Nullable
    protected SearchFolder copyFolder(SearchFolder searchFolder,
                                      UserDetails toUser,
                                      Map<SearchFolder, SearchFolder> copiedFolders,
                                      Map<UUID, UiTablePresentation> presentationsMap) {
        SearchFolder newFolder;
        if (searchFolder.getUsername() == null) {
            return searchFolder;
        }

        newFolder = copiedFolders.get(searchFolder);
        if (newFolder != null) {
            return null;
        }

        newFolder = metadata.create(SearchFolder.class);
        newFolder.setUsername(toUser.getUsername());
        newFolder.setApplyDefault(searchFolder.getApplyDefault());
        newFolder.setFilterComponentId(searchFolder.getFilterComponentId());
        newFolder.setFilterXml(searchFolder.getFilterXml());
        newFolder.setItemStyle(searchFolder.getItemStyle());
        newFolder.setName(searchFolder.getName());
        newFolder.setTabName(searchFolder.getTabName());
        newFolder.setSortOrder(searchFolder.getSortOrder());
        newFolder.setIsSet(searchFolder.getIsSet());
        newFolder.setEntityType(searchFolder.getEntityType());
        if (searchFolder.getParent() != null) {
            SearchFolder copiedFolder = copiedFolders.get(searchFolder.getParent());
            if (copiedFolder != null) {
                newFolder.setParent(copiedFolder);
            } else {
                SearchFolder newParent = getParent((SearchFolder) searchFolder.getParent(),
                        toUser, copiedFolders, presentationsMap);
                newFolder.setParent(newParent);
            }
        }
        if (searchFolder.getPresentationId() != null) {
            newFolder.setPresentationId(searchFolder.getPresentationId());
        }
        copiedFolders.put(searchFolder, newFolder);
        entityManager.persist(newFolder);
        return newFolder;
    }

    @Nullable
    protected SearchFolder getParent(SearchFolder parentFolder,
                                     UserDetails toUser,
                                     Map<SearchFolder, SearchFolder> copiedFolders,
                                     Map<UUID, UiTablePresentation> presentationMap) {
        if (parentFolder == null) {
            return null;
        }
        if (parentFolder.getUsername() == null) {
            return parentFolder;
        }
        return copyFolder(parentFolder, toUser, copiedFolders, presentationMap);
    }

    protected Map<UUID, FilterEntity> copyFilters(UserDetails fromUser, UserDetails toUser) {
        Map<UUID, FilterEntity> filtersMap = new HashMap<>();

        transaction.executeWithoutResult(status -> {
            Query deleteFiltersQuery = entityManager.createQuery("delete from sec$Filter f where f.username = ?1");
            deleteFiltersQuery.setParameter(1, toUser.getUsername());
            deleteFiltersQuery.executeUpdate();
        });

        transaction.executeWithoutResult(status -> {
            TypedQuery<FilterEntity> q =
                    entityManager.createQuery("select f from sec$Filter f where f.username = ?1",
                            FilterEntity.class);
            q.setParameter(1, fromUser.getUsername());

            List<FilterEntity> fromUserFilters = q.getResultList();
            for (FilterEntity filter : fromUserFilters) {
                FilterEntity newFilter = metadata.create(FilterEntity.class);
                newFilter.setUsername(toUser.getUsername());
                newFilter.setCode(filter.getCode());
                newFilter.setName(filter.getName());
                newFilter.setComponentId(filter.getComponentId());
                newFilter.setXml(filter.getXml());
                filtersMap.put(filter.getId(), newFilter);
                entityManager.persist(newFilter);
            }
        });

        return filtersMap;
    }
}
