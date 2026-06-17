package io.jmix.aitoolsflowuidata.entity;

import io.jmix.core.Messages;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.*;
import io.jmix.eclipselink.lazyloading.NotInstantiatedList;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "AITLS_AI_CONVERSATION_ENTITY")
@Entity(name = "aitls_AiConversationEntity")
public class AiConversationEntity {

    @Id
    @Column(name = "ID", nullable = false)
    @JmixGeneratedValue
    private UUID id;

    @NotNull
    @Column(name = "TITLE", nullable = false)
    private String title;

    @NotNull
    @Column(name = "USERNAME", nullable = false)
    private String username;

    @Composition
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.REMOVE)
    @OrderBy("createdDate ASC")
    private List<AiChatMessageEntity> messages = new NotInstantiatedList<>();

    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    private OffsetDateTime createdDate;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

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

    public List<AiChatMessageEntity> getMessages() {
        return messages;
    }

    public void setMessages(List<AiChatMessageEntity> messages) {
        this.messages = messages;
    }

    @InstanceName
    @DependsOnProperties({"title", "id"})
    public String getInstanceName(Messages messages) {
        return title != null ? title : messages.getMessage(getClass(), "AiConversationEntity.instanceName.conversation") + getId();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
