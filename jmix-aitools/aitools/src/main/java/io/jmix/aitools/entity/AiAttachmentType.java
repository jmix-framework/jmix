package io.jmix.aitools.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

import java.util.Objects;

public enum AiAttachmentType implements EnumClass<String> {

    AI_GENERATED("ag_generated"),
    USER_UPLOADED("user_uploaded");

    private final String id;

    AiAttachmentType(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static AiAttachmentType fromId(String id) {
        if (id == null) {
            return null;
        }
        for (AiAttachmentType attachmentType : values()) {
            if (Objects.equals(attachmentType.getId(), id)) {
                return attachmentType;
            }
        }
        return null;
    }
}
