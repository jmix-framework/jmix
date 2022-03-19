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

package io.jmix.imapui.screen.mailbox.helper;

import io.jmix.core.Metadata;
import io.jmix.imap.ImapManager;
import io.jmix.imap.dto.ImapConnectResult;
import io.jmix.imap.dto.ImapFolderDto;
import io.jmix.imap.entity.ImapEventType;
import io.jmix.imap.entity.ImapFolder;
import io.jmix.imap.entity.ImapFolderEvent;
import io.jmix.imap.entity.ImapMailBox;
import io.jmix.imap.exception.ImapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("imap_FolderRefresher")
public class FolderRefresher {

    private final static Logger log = LoggerFactory.getLogger(FolderRefresher.class);

    @Autowired
    protected ImapManager imapManager;
    @Autowired
    protected Metadata metadata;

    public enum State {
        DELETED, NEW, UNCHANGED
    }

    public LinkedHashMap<ImapFolder, State> refresh(ImapMailBox mailBox) {
        log.info("refresh folders for {}", mailBox);
        ImapConnectResult connectResult = imapManager.testConnection(mailBox);
        if (!connectResult.isSuccess()) {
            throw connectResult.getFailure() != null ? connectResult.getFailure() : new ImapException("Cannot connect to the server");
        }
        Collection<ImapFolderDto> folderDtos = connectResult.getAllFolders();
        mailBox.setFlagsSupported(connectResult.isCustomFlagSupported());
        List<ImapFolder> folders = mailBox.getFolders();
        LinkedHashMap<ImapFolder, State> result;
        if (CollectionUtils.isEmpty(folders)) {
            log.debug("There is no folders for {}. Will add all from IMAP server, fully enabling folders that can contain messages", mailBox);
            result = new LinkedHashMap<>();
            folderDtos.stream()
                    .flatMap(dto -> folderWithChildren(dto).stream())
                    .peek(f -> {
                        if (Boolean.TRUE.equals(f.getCanHoldMessages())) {
                            enableCompletely(f);
                        }
                    }).forEach(folder -> result.put(folder, State.UNCHANGED));
        } else {
            log.debug("There are folders for {}. Will add new from IMAP server and disable missing", mailBox);
            result = mergeFolders(ImapFolderDto.flattenList(folderDtos), folders);
        }
        result.keySet()
                .forEach(imapFolder -> imapFolder.setMailBox(mailBox));

        return result;
    }

    protected LinkedHashMap<ImapFolder, State> mergeFolders(List<ImapFolderDto> folderDtos, List<ImapFolder> folders) {
        Map<String, ImapFolderDto> dtosByNames = folderDtos.stream()
                .collect(Collectors.toMap(ImapFolderDto::getFullName, Function.identity()));
        Map<String, ImapFolder> foldersByNames = folders.stream().collect(
                Collectors.toMap(ImapFolder::getName, Function.identity()));
        Map<ImapFolder, String> newFoldersWithParent = new HashMap<>(folderDtos.size());
        folderDtos.stream()
                .filter(dto -> !foldersByNames.containsKey(dto.getFullName()))
                .forEach(dto -> newFoldersWithParent.put(
                        mapDto(dto),
                        dto.getParent() != null ? dto.getParent().getFullName() : null)
                );
        List<ImapFolder> resultList = new ArrayList<>(folders.size() + newFoldersWithParent.size());
        resultList.addAll(folders);
        resultList.addAll(newFoldersWithParent.keySet());
        foldersByNames.putAll(newFoldersWithParent.keySet().stream().collect(
                Collectors.toMap(ImapFolder::getName, Function.identity()))
        );
        newFoldersWithParent.forEach((folder, parentName) -> {
            if (parentName != null) {
                folder.setParent(foldersByNames.get(parentName));
            }
            if (Boolean.TRUE.equals(folder.getCanHoldMessages())) {
                enableCompletely(folder);
            }
        });
        List<ImapFolder> deletedFolders = resultList.stream()
                .filter(folder -> !dtosByNames.containsKey(folder.getName()))
                .collect(Collectors.toList());
        log.trace("New folders:{}", newFoldersWithParent.keySet());
        log.trace("Deleted folders:{}", deletedFolders);
        List<String> folderNames = folderDtos.stream().map(ImapFolderDto::getFullName).collect(Collectors.toList());
        resultList.sort(Comparator.comparingInt(f -> folderNames.indexOf(f.getName())));

        LinkedHashMap<ImapFolder, State> result = new LinkedHashMap<>(resultList.size());
        for (ImapFolder folder : resultList) {
            result.put(
                    folder,
                    newFoldersWithParent.containsKey(folder)
                            ? State.NEW
                            : (deletedFolders.contains(folder) ? State.DELETED : State.UNCHANGED)
            );
        }

        return result;
    }

    protected List<ImapFolder> folderWithChildren(ImapFolderDto dto) {
        return folderWithChildren(dto, null);
    }

    protected List<ImapFolder> folderWithChildren(ImapFolderDto dto, ImapFolder parent) {
        log.trace("Convert dto {} to folder with children, parent of folder-{}", dto, parent);
        List<ImapFolder> result = new ArrayList<>();

        ImapFolder imapFolder = mapDto(dto);
        imapFolder.setParent(parent);

        result.add(imapFolder);
        if (!CollectionUtils.isEmpty(dto.getChildren())) {
            result.addAll(
                    dto.getChildren().stream()
                            .flatMap(child -> folderWithChildren(child, imapFolder).stream())
                            .collect(Collectors.toList())
            );
        }

        return result;
    }

    protected ImapFolder mapDto(ImapFolderDto dto) {
        ImapFolder imapFolder = metadata.create(ImapFolder.class);

        imapFolder.setName(dto.getFullName());
        imapFolder.setCanHoldMessages(Boolean.TRUE.equals(dto.getCanHoldMessages()));
        return imapFolder;
    }

    protected void enableCompletely(ImapFolder imapFolder) {
        log.trace("Set {} selected and enable all events", imapFolder);
        imapFolder.setEnabled(true);

        imapFolder.setEvents(
                Arrays.stream(ImapEventType.values()).map(eventType -> {
                    ImapFolderEvent imapEvent = metadata.create(ImapFolderEvent.class);
                    imapEvent.setEvent(eventType);
                    imapEvent.setFolder(imapFolder);
                    imapEvent.setEnabled(true);

                    return imapEvent;
                }).collect(Collectors.toList())
        );
    }
}
