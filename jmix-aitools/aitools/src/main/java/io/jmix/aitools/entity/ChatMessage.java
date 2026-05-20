package io.jmix.aitools.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
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
@Table(name = "AITOLS_CHAT_MESSAGE", indexes = {
        @Index(name = "IDX_AITOLS_CHAT_MESSAGE_CONV", columnList = "CONVERSATION_ID"),
        @Index(name = "IDX_AITOLS_CHAT_MESSAGE", columnList = "CONVERSATION_ID, CREATED_DATE")
})
@Entity(name = "aitols_ChatMessage")
public class ChatMessage {

    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    private UUID id;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "CONVERSATION_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private AiConversation conversation;

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

    public ChatMessageType getType() {
        return type == null ? null : ChatMessageType.fromId(type);
    }

    public void setType(ChatMessageType type) {
        this.type = type == null ? null : type.getId();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public AiConversation getConversation() {
        return conversation;
    }

    public void setConversation(AiConversation conversation) {
        this.conversation = conversation;
    }
}