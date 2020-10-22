/*
 * Copyright (c) 2008-2019 Haulmont.
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

package component.composite.loader;

import com.google.common.base.Strings;
import component.composite.component.TestCommentaryPanel;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.xml.layout.loader.AbstractComponentLoader;
import org.dom4j.Element;

@SuppressWarnings({"rawtypes", "unchecked"})
public class TestCommentaryPanelLoader extends AbstractComponentLoader<TestCommentaryPanel> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(TestCommentaryPanel.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        loadCaption(resultComponent, element);
        loadWidth(resultComponent, element);
        loadHeight(resultComponent, element);

        loadDataContainer(resultComponent, element);
    }

    private void loadDataContainer(TestCommentaryPanel resultComponent, Element element) {
        String containerId = this.element.attributeValue("dataContainer");
        if (Strings.isNullOrEmpty(containerId)) {
            throw new GuiDevelopmentException("CommentaryPanel element doesn't have 'dataContainer' attribute",
                    context, "CommentaryPanel ID", element.attributeValue("id"));
        }

        FrameOwner frameOwner = getComponentContext().getFrame().getFrameOwner();
        ScreenData screenData = UiControllerUtils.getScreenData(frameOwner);
        InstanceContainer container = screenData.getContainer(containerId);
        if (container instanceof CollectionContainer) {
            resultComponent.setDataContainer((CollectionContainer) container);
        } else {
            throw new GuiDevelopmentException("Not a CollectionContainer: " + containerId, context);
        }
    }
}
