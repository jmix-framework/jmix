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

package io.jmix.searchui.screen.entitysearcher;

import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.search.IndexProcessManager;
import io.jmix.search.SearchProperties;
import io.jmix.search.searching.impl.FieldHit;
import io.jmix.search.searching.impl.SearchResult;
import io.jmix.search.searching.impl.SearchResultEntry;
import io.jmix.search.utils.PropertyTools;
import io.jmix.ui.AppUI;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.*;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static io.jmix.ui.component.Component.Alignment.MIDDLE_LEFT;

//TODO Temporaty screen. It will be removed later
@UiController("entitySearcher.browser")
@UiDescriptor("entity-searcher-browser.xml")
public class EntitySearcherBrowser extends Screen {

    @Autowired
    protected IndexProcessManager indexManager;

    @Subscribe("reindexAllAction")
    public void onReindexAllAction(Action.ActionPerformedEvent event) {
        indexManager.scheduleReindexAll();
    }
}