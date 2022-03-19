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

package io.jmix.imapui.screen.folder;

import io.jmix.imap.entity.ImapFolder;
import io.jmix.imap.entity.ImapMailBox;
import io.jmix.ui.WindowParam;
import io.jmix.ui.component.TreeTable;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;

@UiController("imap_Folder.lookup")
@UiDescriptor("imap-mail-box-folder-lookup.xml")
@LookupComponent("imapFoldersTable")
public class ImapFolderLookup extends StandardLookup<ImapFolder> {

    private final static Logger log = LoggerFactory.getLogger(ImapFolderLookup.class);

    @Autowired
    protected InstanceContainer<ImapMailBox> mailBoxDc;

    @Autowired
    protected CollectionContainer<ImapFolder> imapFolderDc;

    @Autowired
    protected TreeTable<ImapFolder> imapFoldersTable;

    @WindowParam
    protected  ImapMailBox mailBox;


    @Subscribe
    public void beforeShow(BeforeShowEvent beforeShowEvent) {
        mailBoxDc.setItem(mailBox);

        String trashFolderName = mailBox.getTrashFolderName();
        if (trashFolderName != null) {
            Collection<ImapFolder> items = new ArrayList<>(imapFolderDc.getItems());

            log.debug("find trash folder {} among {}", trashFolderName, items);
            items.stream()
                    .filter(f -> f.getName().equals(trashFolderName))
                    .findFirst().ifPresent(trashFolder -> imapFoldersTable.setSelected(trashFolder));

        }
    }
}
