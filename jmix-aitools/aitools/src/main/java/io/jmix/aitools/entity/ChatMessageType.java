package io.jmix.aitools.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

import java.util.Objects;

public enum ChatMessageType implements EnumClass<String> {

    /**
     * User message.
     */
    USER("user"),

    /**
     * Attachment message
     */
    ATTACHMENT("attachment"),

    /**
     * User's uploaded file.
     */
    USER_UPLOAD("user_upload"),

    /**
     * LLM answer.
     */
    ASSISTANT("assistant"),

    /**
     * System instruction (system prompt).
     */
    SYSTEM("system"),

    /**
     * Result of tool/function calling.
     */
    TOOL("tool"),;

    private String id;

    ChatMessageType(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static ChatMessageType fromId(String id) {
        if (id == null) {
            return null;
        }
        for (ChatMessageType messageType : values()) {
            if (Objects.equals(messageType.getId(), id)) {
                return messageType;
            }
        }
        return null;
    }
}
