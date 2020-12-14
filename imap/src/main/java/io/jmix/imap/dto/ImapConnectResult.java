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

import io.jmix.imap.entity.ImapMailBox;
import io.jmix.imap.exception.ImapException;

import java.io.Serializable;
import java.util.List;

/**
 * Describes result for test connection operation holding:
 * <ul>
 *     <li>
 *     mailBox reference
 *     </li>
 *     <li>
 *     result of operation
 *     </li>
 *     <li>
 *     supported features
 *     </li>
 *     <li>
 *     all folders preserving tree structure if test succeeded
 *     </li>
 * </ul>
 */
public class ImapConnectResult implements Serializable {
    private static final long serialVersionUID = -2217624132287086972L;

    protected ImapMailBox mailBox;

    protected boolean success;
    protected ImapException failure;

    protected boolean customFlagSupported;

    protected List<ImapFolderDto> allFolders;

    public ImapConnectResult(ImapMailBox mailBox) {
        this.mailBox = mailBox;
    }

    public ImapMailBox getMailBox() {
        return mailBox;
    }

    public void setMailBox(ImapMailBox mailBox) {
        this.mailBox = mailBox;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ImapException getFailure() {
        return failure;
    }

    public void setFailure(ImapException failure) {
        this.failure = failure;
    }

    public boolean isCustomFlagSupported() {
        return customFlagSupported;
    }

    public void setCustomFlagSupported(boolean customFlagSupported) {
        this.customFlagSupported = customFlagSupported;
    }

    public List<ImapFolderDto> getAllFolders() {
        return allFolders;
    }

    public void setAllFolders(List<ImapFolderDto> allFolders) {
        this.allFolders = allFolders;
    }
}
