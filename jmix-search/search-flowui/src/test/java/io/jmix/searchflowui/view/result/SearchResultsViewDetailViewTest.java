/*
 * Copyright 2026 Haulmont.
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

package io.jmix.searchflowui.view.result;

import io.jmix.core.Metadata;
import io.jmix.core.MessageTools;
import io.jmix.core.DataManager;
import io.jmix.core.FluentLoader;
import io.jmix.core.Id;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import org.mockito.ArgumentCaptor;
import java.util.UUID;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.Session;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.exception.NoSuchViewException;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.search.searching.SearchResultEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

public class SearchResultsViewDetailViewTest {

    @Test
    void hasDetailViewIsFalseWhenNoDetailViewIsRegistered() {
        SearchResultsView view = new SearchResultsView();
        view.viewRegistry = Mockito.mock(ViewRegistry.class);
        MetaClass metaClass = Mockito.mock(MetaClass.class);
        Mockito.when(view.viewRegistry.getDetailViewInfo(metaClass)).thenThrow(new NoSuchViewException("Alpha.detail"));

        Assertions.assertFalse(view.hasDetailView(metaClass));
    }

    @Test
    void reloadEntityPassesTheIdentifierValueRatherThanTheIdWrapper() {
        SearchResultsView view = new SearchResultsView();
        view.dataManager = Mockito.mock(DataManager.class);

        FluentLoader<TestEntity> loader = Mockito.mock(FluentLoader.class);
        FluentLoader.ById<TestEntity> byId = Mockito.mock(FluentLoader.ById.class);
        Mockito.when(view.dataManager.load(TestEntity.class)).thenReturn(loader);
        Mockito.when(loader.id(Mockito.any())).thenReturn(byId);
        Mockito.when(byId.fetchPlan(Mockito.anyString())).thenReturn(byId);

        MetaClass metaClass = Mockito.mock(MetaClass.class);
        Mockito.when(metaClass.getJavaClass()).thenAnswer(invocation -> TestEntity.class);

        UUID identifier = UUID.randomUUID();
        view.reloadEntity(metaClass, Id.of(identifier, TestEntity.class));

        // Only the JPA store unwraps an Id itself; every other store is handed whatever this passes.
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(loader).id(captor.capture());
        Assertions.assertEquals(identifier, captor.getValue());
    }

    @JmixEntity
    static class TestEntity {

        @JmixId
        UUID id;
    }

    @Test
    void hasDetailViewIsTrueWhenDetailViewIsRegistered() {
        SearchResultsView view = new SearchResultsView();
        view.viewRegistry = Mockito.mock(ViewRegistry.class);
        MetaClass metaClass = Mockito.mock(MetaClass.class);
        Mockito.when(view.viewRegistry.getDetailViewInfo(metaClass)).thenReturn(Mockito.mock(ViewInfo.class));

        Assertions.assertTrue(view.hasDetailView(metaClass));
    }

    @Test
    void openEntityViewShowsWarningAndSkipsNavigationWhenNoDetailViewIsRegistered() {
        SearchResultsView view = new SearchResultsView();
        view.viewRegistry = Mockito.mock(ViewRegistry.class);
        view.metadata = Mockito.mock(Metadata.class);
        view.notifications = Mockito.mock(Notifications.class);
        view.messageBundle = Mockito.mock(MessageBundle.class);
        view.messageTools = Mockito.mock(MessageTools.class);
        view.dialogWindows = Mockito.mock(DialogWindows.class);
        view.viewNavigators = Mockito.mock(ViewNavigators.class);

        String entityName = "Alpha";
        MetaClass metaClass = Mockito.mock(MetaClass.class);
        Session session = Mockito.mock(Session.class);
        Mockito.when(view.metadata.getSession()).thenReturn(session);
        Mockito.when(session.getClass(entityName)).thenReturn(metaClass);
        Mockito.when(view.viewRegistry.getDetailViewInfo(metaClass)).thenThrow(new NoSuchViewException("Alpha.detail"));
        Mockito.when(view.messageTools.getEntityCaption(metaClass)).thenReturn("Alpha");
        Mockito.when(view.messageBundle.formatMessage("noDetailView", "Alpha")).thenReturn("There is no detail view for Alpha");

        Notifications.NotificationBuilder builder = Mockito.mock(Notifications.NotificationBuilder.class);
        Mockito.when(view.notifications.create("There is no detail view for Alpha")).thenReturn(builder);
        Mockito.when(builder.withType(Notifications.Type.WARNING)).thenReturn(builder);

        SearchResultEntry entry = new SearchResultEntry("docId", "Alpha instance", entityName, Collections.emptyList());

        view.openEntityView(entry, entityName);

        Mockito.verify(builder).withType(Notifications.Type.WARNING);
        Mockito.verify(builder).show();
        Mockito.verifyNoInteractions(view.dialogWindows, view.viewNavigators);
    }
}
