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

package io.jmix.aitoolsflowuidata.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import java.time.OffsetDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "AITLS_AI_CHAT_MESSAGE_ENTITY", indexes = {
        @Index(name = "IDX_AITLS_AI_CHAT_MSG_ENT_CONV", columnList = "CONVERSATION_ID"),
        @Index(name = "IDX_AITLS_AI_CHAT_MSG_ENT", columnList = "CONVERSATION_ID, CREATED_DATE")
})
@Entity(name = "aitls_AiChatMessageEntity")
public class AiChatMessageEntity {

    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    private UUID id;

    @JoinColumn(name = "CONVERSATION_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private AiConversationEntity conversation;

    @InstanceName
    @Column(name = "CONTENT")
    @Lob
    private String content;

    @NotNull
    @Column(name = "TYPE_", nullable = false)
    private String type;

    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    private OffsetDateTime createdDate;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Nullable
    public OffsetDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(OffsetDateTime createdDate) {
        this.createdDate = createdDate;
    }

    @Nullable
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AiChatMessageEntityType getType() {
        return type == null ? null : AiChatMessageEntityType.fromId(type);
    }

    public void setType(AiChatMessageEntityType type) {
        this.type = type == null ? null : type.getId();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public AiConversationEntity getConversation() {
        return conversation;
    }

    public void setConversation(AiConversationEntity conversation) {
        this.conversation = conversation;
    }
}