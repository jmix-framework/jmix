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

package io.jmix.imap.dto;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Encapsulates IMAP folder details:
 * <ul>
 *     <li>
 *     name
 *     </li>
 *     <li>
 *     full name
 *     </li>
 *     <li>
 *     tree structure place using children and parent
 *     </li>
 *     <li>
 *     flag to determine whether this folder can contain messages
 *     </li>
 * </ul>
 */
@JmixEntity(name = "imap_FolderDto")
public class ImapFolderDto {

    @JmixId
    @JmixGeneratedValue
    protected UUID id;

    @JmixProperty(mandatory = true)
    protected String name;

    @JmixProperty(mandatory = true)
    protected String fullName;

    @JmixProperty(mandatory = true)
    protected Boolean canHoldMessages;

    @JmixProperty
    protected List<ImapFolderDto> children;

    @JmixProperty
    protected ImapFolderDto parent;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<ImapFolderDto> getChildren() {
        return children;
    }

    public void setChildren(List<ImapFolderDto> children) {
        this.children = children;
    }

    public ImapFolderDto getParent() {
        return parent;
    }

    public void setParent(ImapFolderDto parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @InstanceName
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Boolean getCanHoldMessages() {
        return canHoldMessages;
    }

    public void setCanHoldMessages(Boolean canHoldMessages) {
        this.canHoldMessages = canHoldMessages;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("name", name).
                append("fullName", fullName).
                append("canHoldMessages", canHoldMessages).
                append("children", children).
                toString();
    }

    public static List<ImapFolderDto> flattenList(Collection<ImapFolderDto> folderDtos) {
        List<ImapFolderDto> result = new ArrayList<>(folderDtos.size());

        for (ImapFolderDto folderDto : folderDtos) {
            addFolderWithChildren(result, folderDto);
        }

        return result;
    }

    private static void addFolderWithChildren(List<ImapFolderDto> foldersList, ImapFolderDto folder) {
        foldersList.add(folder);
        List<ImapFolderDto> children = folder.getChildren();
        if (children != null) {
            for (ImapFolderDto childFolder : children) {
                addFolderWithChildren(foldersList, childFolder);
            }
        }
    }
}
