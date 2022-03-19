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
import io.jmix.imap.entity.ImapMailBox;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Encapsulates IMAP message details:
 * <ul>
 * <li>
 * Folder name
 * </li>
 * <li>
 * UID
 * </li>
 * <li>
 * Sender
 * </li>
 * <li>
 * Recipient lists (to, cc, bcc)
 * </li>
 * <li>
 * Subject
 * </li>
 * <li>
 * Body content
 * </li>
 * <li>
 * Receive date
 * </li>
 * <li>
 * IMAP metadata flags
 * </li>
 * </ul>
 */
@JmixEntity(name = "imap_MessageDto", annotatedPropertiesOnly = true)
public class ImapMessageDto {

    @JmixId
    @JmixGeneratedValue
    protected UUID id;

    @JmixProperty(mandatory = true)
    protected Long uid;

    @JmixProperty(mandatory = true)
    protected String from;

    protected List<String> toList;

    protected List<String> ccList;

    protected List<String> bccList;

    @JmixProperty
    @InstanceName
    protected String subject;

    @JmixProperty
    protected String body;

    @JmixProperty
    protected Boolean html = false;

    protected List<String> flagsList;

    @JmixProperty
    protected Date date;

    @JmixProperty(mandatory = true)
    protected ImapMailBox mailBox;

    @JmixProperty(mandatory = true)
    protected String folderName;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    @SuppressWarnings("UnusedReturnValue")
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @SuppressWarnings("UnusedReturnValue")
    @JmixProperty
    public String getTo() {
        return toList != null ? StringUtils.join(toList, ", ") : "";
    }

    public void setToList(List<String> toList) {
        this.toList = toList;
    }

    @SuppressWarnings("UnusedReturnValue")
    @JmixProperty
    public String getCc() {
        return ccList != null ? StringUtils.join(ccList, ", ") : "";
    }

    public void setCcList(List<String> ccList) {
        this.ccList = ccList;
    }

    @SuppressWarnings("UnusedReturnValue")
    @JmixProperty
    public String getBcc() {
        return bccList != null ? StringUtils.join(bccList, ", ") : "";
    }

    public void setBccList(List<String> bccList) {
        this.bccList = bccList;
    }

    @SuppressWarnings("UnusedReturnValue")
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Boolean getHtml() {
        return html;
    }

    public void setHtml(Boolean html) {
        this.html = html;
    }

    @SuppressWarnings("UnusedReturnValue")
    @JmixProperty
    public String getFlags() {
        return flagsList != null ? flagsList.toString() : "";
    }

    public void setFlagsList(List<String> flags) {
        this.flagsList = flags;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date sendDate) {
        this.date = sendDate;
    }

    public ImapMailBox getMailBox() {
        return mailBox;
    }

    public void setMailBox(ImapMailBox mailBox) {
        this.mailBox = mailBox;
    }

    @SuppressWarnings("UnusedReturnValue")
    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("from", from).
                append("toList", toList).
                append("ccList", ccList).
                append("bccList", bccList).
                append("subject", subject).
                append("body", body).
                append("flags", flagsList).
                append("mailBoxHost", mailBox.getHost()).
                append("mailBoxPort", mailBox.getPort()).
                append("mailBoxId", mailBox.getId()).
                toString();
    }
}
