/*
 * Copyright (c) 2020 Haulmont.
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
 *
 */

package com.haulmont.cuba.web.app.folders;

import com.haulmont.cuba.core.entity.AbstractSearchFolder;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowManagerProvider;
import com.haulmont.cuba.gui.WindowParams;
import com.haulmont.cuba.gui.components.Filter;
import com.haulmont.cuba.gui.components.filter.FilterDelegate;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.data.impl.DsContextImplementation;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import com.haulmont.cuba.security.entity.FilterEntity;
import com.haulmont.cuba.security.entity.SearchFolder;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.component.HasTablePresentations;
import io.jmix.ui.component.Window;
import io.jmix.ui.sys.ValuePathHelper;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Deprecated
@Component(Folders.NAME)
public class FoldersBean implements Folders {

    private static final Logger log = LoggerFactory.getLogger(FoldersBean.class);

    @Autowired
    protected Messages messages;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected WindowConfig windowConfig;
    @Autowired
    protected WindowManagerProvider windowManagerProvider;

    @Override
    public void openFolder(AbstractSearchFolder folder) {
        if (StringUtils.isBlank(folder.getFilterComponentId())) {
            log.warn("Unable to open folder: componentId is blank");
            return;
        }

        String[] strings = ValuePathHelper.parse(folder.getFilterComponentId());
        String screenId = strings[0];

        WindowInfo windowInfo = windowConfig.getWindowInfo(screenId);

        Map<String, Object> params = new HashMap<>();

        WindowParams.DISABLE_AUTO_REFRESH.set(params, true);
        WindowParams.DISABLE_RESUME_SUSPENDED.set(params, true);

        if (!StringUtils.isBlank(folder.getTabName())) {
            WindowParams.DESCRIPTION.set(params, messages.getMainMessage(folder.getTabName()));
        } else {
            WindowParams.DESCRIPTION.set(params, messages.getMainMessage(folder.getName()));
        }

        WindowParams.FOLDER_ID.set(params, folder.getId());

        WindowManager wm = windowManagerProvider.get();
        Window window = wm.openWindow(windowInfo, WindowManager.OpenType.NEW_TAB, params);

        Filter filterComponent = null;

        if (strings.length > 1) {
            String filterComponentId = StringUtils.join(Arrays.copyOfRange(strings, 1, strings.length), '.');

            filterComponent = (Filter) window.getComponentNN(filterComponentId);

            FilterEntity filterEntity = metadata.create(FilterEntity.class);
            filterEntity.setFolder(folder);
            filterEntity.setComponentId(folder.getFilterComponentId());
            filterEntity.setName(folder.getLocName());

            filterEntity.setXml(folder.getFilterXml());
            filterEntity.setApplyDefault(BooleanUtils.isNotFalse(folder.getApplyDefault()));
            if (folder instanceof SearchFolder) {
                filterEntity.setIsSet(((SearchFolder) folder).getIsSet());
            }
            filterComponent.setFilterEntity(filterEntity);
            filterComponent.switchFilterMode(FilterDelegate.FilterMode.GENERIC_MODE);
        }

        if (filterComponent != null && folder instanceof SearchFolder) {
            SearchFolder searchFolder = (SearchFolder) folder;
            if (searchFolder.getPresentationId() != null) {
                ((HasTablePresentations) filterComponent.getApplyTo())
                        .applyPresentation(searchFolder.getPresentationId());
            }
        }

        if (window.getFrameOwner() instanceof LegacyFrame) {
            DsContext dsContext = ((LegacyFrame) window.getFrameOwner()).getDsContext();
            if (dsContext != null) {
                ((DsContextImplementation) dsContext).resumeSuspended();
            }
        }
    }
}
