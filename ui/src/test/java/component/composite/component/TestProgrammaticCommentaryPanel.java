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

package component.composite.component;

import com.google.common.base.Strings;
import io.jmix.core.MetadataTools;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.ComponentContainer;
import io.jmix.ui.component.CompositeComponent;
import io.jmix.ui.component.CompositeWithCaption;
import io.jmix.ui.component.CssLayout;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.VBoxLayout;
import io.jmix.ui.component.data.DataGridItems;
import io.jmix.ui.component.data.datagrid.ContainerDataGridItems;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.model.CollectionContainer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.model_objects.CommentObject;

import java.util.function.Function;

public class TestProgrammaticCommentaryPanel extends CompositeComponent<VBoxLayout> implements CompositeWithCaption {

    public static final String NAME = "testProgrammaticCommentaryPanel";

    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected ObjectProvider<DataGrid.HtmlRenderer> htmlRendererProvider;

    protected DataGrid<CommentObject> commentsDataGrid;
    protected TextField<String> messageField;
    protected Button sendBtn;

    protected CollectionContainer<CommentObject> collectionContainer;
    protected Function<String, CommentObject> commentProvider;

    public TestProgrammaticCommentaryPanel() {
        addCreateListener(event -> {
            createComponent();
            initComponent(getComposition());
        });
    }

    protected void createComponent() {
        VBoxLayout rootPanel = uiComponents.create(VBoxLayout.class);
        rootPanel.setId("rootPanel");
        rootPanel.setMargin(true);
        rootPanel.setSpacing(true);
        rootPanel.setStyleName("commentary-panel card");
        rootPanel.setWidthFull();

        DataGrid<CommentObject> commentsDataGrid = uiComponents.create(DataGrid.of(CommentObject.class));
        commentsDataGrid.setId("commentsDataGrid");
        commentsDataGrid.setBodyRowHeight(100);
        commentsDataGrid.setColumnReorderingAllowed(false);
        commentsDataGrid.setColumnsCollapsingAllowed(false);
        commentsDataGrid.setHeaderVisible(false);
        commentsDataGrid.setSelectionMode(DataGrid.SelectionMode.NONE);
        commentsDataGrid.setWidthFull();

        CssLayout sendMessageBox = uiComponents.create(CssLayout.class);
        sendMessageBox.setId("sendMessageBox");
        sendMessageBox.setStyleName("v-component-group message-box");
        sendMessageBox.setWidthFull();

        TextField<String> messageField = uiComponents.create(TextField.TYPE_STRING);
        messageField.setId("messageField");
        messageField.setInputPrompt("Enter your message");
        messageField.setWidthFull();

        Button sendBtn = uiComponents.create(Button.class);
        sendBtn.setId("sendBtn");
        sendBtn.setCaption("Send");

        sendMessageBox.add(messageField, sendBtn);

        rootPanel.add(commentsDataGrid, sendMessageBox);
        rootPanel.expand(commentsDataGrid);

        setComposition(rootPanel);
    }

    @Override
    protected void setComposition(VBoxLayout composition) {
        super.setComposition(composition);

        commentsDataGrid = getInnerComponent("commentsDataGrid");
        messageField = getInnerComponent("messageField");
        sendBtn = getInnerComponent("sendBtn");
    }

    protected void initComponent(ComponentContainer composition) {
        commentsDataGrid.addGeneratedColumn("comment", event -> {
            CommentObject item = event.getItem();
            return "<p class=\"message-text\">" + item.getText() + "</p>";
        });
        commentsDataGrid.setRowDescriptionProvider(CommentObject::getText);

        sendBtn.addClickListener(clickEvent ->
                sendMessage());
        messageField.addEnterPressListener(enterPressEvent ->
                sendMessage());
    }

    protected void sendMessage() {
        String messageText = messageField.getValue();
        if (!Strings.isNullOrEmpty(messageText)) {
            addMessage(messageText);
            messageField.clear();
        }
    }

    protected void addMessage(String text) {
        if (getCommentProvider() == null) {
            return;
        }

        CommentObject comment = getCommentProvider().apply(text);

        DataGridItems<CommentObject> items = commentsDataGrid.getItems();
        if (items instanceof ContainerDataUnit) {
            //noinspection unchecked
            CollectionContainer<CommentObject> container = ((ContainerDataUnit<CommentObject>) items).getContainer();
            container.getMutableItems().add(comment);
        } else {
            throw new IllegalStateException("Items must implement ContainerDataUnit");
        }
    }

    public Function<String, CommentObject> getCommentProvider() {
        return commentProvider;
    }

    public void setCreateCommentProvider(Function<String, CommentObject> commentProvider) {
        this.commentProvider = commentProvider;
    }

    public CollectionContainer<CommentObject> getCollectionContainer() {
        return collectionContainer;
    }

    public void setDataContainer(CollectionContainer<CommentObject> container) {
        this.collectionContainer = container;

        commentsDataGrid.setItems(new ContainerDataGridItems<>(container));
        commentsDataGrid.getColumnNN("comment")
                .setRenderer(htmlRendererProvider.getObject());

        container.addCollectionChangeListener(event -> commentsDataGrid.scrollToEnd());
    }

    public Button getSendButton() {
        return sendBtn;
    }
}
