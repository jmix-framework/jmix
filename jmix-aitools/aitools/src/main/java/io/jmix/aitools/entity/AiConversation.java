package io.jmix.aitools.entity;

import io.jmix.core.Messages;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.*;
import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "AITOLS_AI_CONVERSATION")
@Entity(name = "aitols_AiConversation")
public class AiConversation {

    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    private UUID id;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "FIRST_MESSAGE_SENT", nullable = false)
    private Boolean firstMessageSent = false;

    @Composition
    @OneToMany(mappedBy = "conversation")
    @OrderBy("createdDate ASC")
    private List<ChatMessage> messages = new ArrayList<>();

    @Composition
    @OneToMany(mappedBy = "conversation")
    @OrderBy("createdDate ASC")
    private List<AiConversationAttachment> attachments;

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

    public List<AiConversationAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AiConversationAttachment> attachments) {
        this.attachments = attachments;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @InstanceName
    @DependsOnProperties({"title", "id"})
    public String getInstanceName(Messages messages) {
        return title != null ? title : messages.getMessage(getClass(), "AiConversation.instanceName.conversation") + getId();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getFirstMessageSent() {
        return firstMessageSent;
    }

    public void setFirstMessageSent(Boolean firstMessageSent) {
        this.firstMessageSent = firstMessageSent;
    }

}
