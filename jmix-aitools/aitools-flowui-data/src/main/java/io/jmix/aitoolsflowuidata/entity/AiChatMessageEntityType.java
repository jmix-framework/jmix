package io.jmix.aitoolsflowuidata.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public enum AiChatMessageEntityType implements EnumClass<String> {

    /**
     * User message.
     */
    USER("user"),

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
    TOOL("tool");

    private String id;

    AiChatMessageEntityType(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static AiChatMessageEntityType fromId(String id) {
        if (id == null) {
            return null;
        }
        for (AiChatMessageEntityType messageType : values()) {
            if (Objects.equals(messageType.getId(), id)) {
                return messageType;
            }
        }
        return null;
    }
}
