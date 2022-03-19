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

package io.jmix.auditui.screen.snapshot;

import io.jmix.audit.snapshot.EntityDifferenceManager;
import io.jmix.audit.snapshot.EntitySnapshotManager;
import io.jmix.audit.snapshot.model.*;
import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.accesscontext.EntityAttributeContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@UiController("snapshotDiff")
@UiDescriptor("snapshot-diff.xml")
public class SnapshotDiffViewer extends ScreenFragment {
    @Autowired
    private CollectionContainer<EntityPropertyDifferenceModel> diffCt;
    @Autowired
    private InstanceContainer<EntityDifferenceModel> entityDiffCt;
    @Autowired
    private CollectionContainer<EntitySnapshotModel> snapshotsCt;
    @Autowired
    private EntityDifferenceManager entityDifferenceManager;
    @Autowired
    private Table<EntitySnapshotModel> snapshotsTable;
    @Autowired
    private EntitySnapshotManager entitySnapshotManager;
    @Autowired
    private HBoxLayout itemStateField;
    @Autowired
    private Metadata metadata;
    @Autowired
    private TreeTable<EntityPropertyDifferenceModel> diffTable;
    @Autowired
    private Label<String> valuesHeader;
    @Autowired
    private Label<String> itemStateLabel;
    @Autowired
    private Messages messages;
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    private Form diffValuesField;

    @Subscribe
    public void onInit(InitEvent event) {
        diffTable.setStyleProvider(new DiffStyleProvider());
        diffTable.setIconProvider(new DiffIconProvider());

        diffCt.addItemChangeListener(e -> {
            boolean valuesVisible = (e.getItem() != null) && (e.getItem().hasStateValues());
            boolean stateVisible = (e.getItem() != null) && (e.getItem().hasStateValues() && e.getItem().itemStateVisible());

            valuesHeader.setVisible(stateVisible || valuesVisible);
            itemStateField.setVisible(stateVisible);
            diffValuesField.setVisible(valuesVisible);

            if (e.getItem() != null) {
                EntityPropertyDifferenceModel.ItemState itemState = e.getItem().getItemState();
                if (itemState != EntityPropertyDifferenceModel.ItemState.Normal) {
                    String messageCode = itemState.toString();
                    itemStateLabel.setValue(messages.getMessage(EntityPropertyDifferenceModel.ItemState.class, messageCode));
                    itemStateLabel.setVisible(true);
                } else {
                    itemStateField.setVisible(false);
                }
            }
        });

    }

    private Collection<EntityPropertyDifferenceModel> loadTree(EntityDifferenceModel diff) {
        if (diff == null) {
            return Collections.emptyList();
        }
        List<EntityPropertyDifferenceModel> entityPropertyDifferenceModels = new ArrayList<>();
        for (EntityPropertyDifferenceModel childPropertyDiff : diff.getPropertyDiffs()) {
            EntityPropertyDifferenceModel differenceModel = loadPropertyDiff(childPropertyDiff, entityPropertyDifferenceModels);
            if (differenceModel != null) {
                entityPropertyDifferenceModels.add(differenceModel);
            }
        }
        return entityPropertyDifferenceModels;
    }

    private EntityPropertyDifferenceModel loadPropertyDiff(EntityPropertyDifferenceModel propertyDiff, List<EntityPropertyDifferenceModel> entityPropertyDifferenceModels) {
        if (propertyDiff == null) {
            return null;
        }
        MetaClass propMetaClass = metadata.getClass(propertyDiff.getMetaClassName());
        CrudEntityContext entityContext = new CrudEntityContext(propMetaClass);
        accessManager.applyRegisteredConstraints(entityContext);
        EntityAttributeContext attributeContext = new EntityAttributeContext(propMetaClass, propertyDiff.getPropertyName());
        accessManager.applyRegisteredConstraints(attributeContext);
        if (!entityContext.isReadPermitted()
                || !attributeContext.canView()) {
            return null;
        }

        if (propertyDiff instanceof EntityClassPropertyDifferenceModel) {
            EntityClassPropertyDifferenceModel classPropertyDiff = (EntityClassPropertyDifferenceModel) propertyDiff;
            for (EntityPropertyDifferenceModel childPropertyDiff : classPropertyDiff.getPropertyDiffs()) {
                EntityPropertyDifferenceModel entityPropertyDifferenceModel = loadPropertyDiff(childPropertyDiff, entityPropertyDifferenceModels);
                if (entityPropertyDifferenceModel != null) {
                    entityPropertyDifferenceModels.add(entityPropertyDifferenceModel);
                    entityPropertyDifferenceModel.setParentProperty(propertyDiff);
                }
            }
        } else if (propertyDiff instanceof EntityCollectionPropertyDifferenceModel) {
            EntityCollectionPropertyDifferenceModel collectionPropertyDiff = (EntityCollectionPropertyDifferenceModel) propertyDiff;
            for (EntityPropertyDifferenceModel childPropertyDiff : collectionPropertyDiff.getAddedEntities()) {
                EntityPropertyDifferenceModel entityPropertyDifferenceModel = loadPropertyDiff(childPropertyDiff, entityPropertyDifferenceModels);
                if (entityPropertyDifferenceModel != null) {
                    entityPropertyDifferenceModels.add(entityPropertyDifferenceModel);
                    entityPropertyDifferenceModel.setParentProperty(propertyDiff);
                }
            }

            for (EntityPropertyDifferenceModel childPropertyDiff : collectionPropertyDiff.getModifiedEntities()) {
                EntityPropertyDifferenceModel entityPropertyDifferenceModel = loadPropertyDiff(childPropertyDiff, entityPropertyDifferenceModels);
                if (entityPropertyDifferenceModel != null) {
                    entityPropertyDifferenceModels.add(entityPropertyDifferenceModel);
                    entityPropertyDifferenceModel.setParentProperty(propertyDiff);
                }
            }

            for (EntityPropertyDifferenceModel childPropertyDiff : collectionPropertyDiff.getRemovedEntities()) {
                EntityPropertyDifferenceModel entityPropertyDifferenceModel = loadPropertyDiff(childPropertyDiff, entityPropertyDifferenceModels);
                if (entityPropertyDifferenceModel != null) {
                    entityPropertyDifferenceModels.add(entityPropertyDifferenceModel);
                    entityPropertyDifferenceModel.setParentProperty(propertyDiff);
                }
            }
        }
        return propertyDiff;
    }

    public void loadVersions(Object entity) {
        snapshotsCt.setItems(entitySnapshotManager.getSnapshots(entity));
        snapshotsTable.repaint();
    }

    @Subscribe("compareBtn")
    public void onCompareBtnClick(Button.ClickEvent event) {
        entityDiffCt.setItem(null);

        EntitySnapshotModel firstSnap = null;
        EntitySnapshotModel secondSnap = null;

        Set selected = snapshotsTable.getSelected();
        Object[] selectedItems = selected.toArray();
        if ((selected.size() == 2)) {
            firstSnap = (EntitySnapshotModel) selectedItems[0];
            secondSnap = (EntitySnapshotModel) selectedItems[1];
        } else if (selected.size() == 1) {
            secondSnap = (EntitySnapshotModel) selectedItems[0];
            firstSnap = entitySnapshotManager.getLastEntitySnapshot(metadata.getClass(secondSnap.getEntityMetaClass()), secondSnap.getEntityId());
            if (firstSnap == secondSnap)
                firstSnap = null;
        }

        EntityDifferenceModel diff = null;
        if ((secondSnap != null) || (firstSnap != null)) {
            diff = entityDifferenceManager.getDifference(firstSnap, secondSnap);
            entityDiffCt.setItem(diff);
        }

        diffCt.setItems(loadTree(diff));
        diffTable.expandAll();
    }

}