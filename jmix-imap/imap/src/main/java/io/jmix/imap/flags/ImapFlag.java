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

package io.jmix.imap.flags;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.mail.Flags;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * Unified IMAP Flag
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImapFlag implements Serializable {
    protected final SystemFlag systemFlag;
    protected final String name;

    /**
     * Corresponds to standard {@link Flags.Flag#SEEN} flag
     */
    @SuppressWarnings("unused")
    public static final ImapFlag SEEN = new ImapFlag(SystemFlag.SEEN);
    /**
     * Corresponds to standard {@link Flags.Flag#ANSWERED} flag
     */
    @SuppressWarnings("unused")
    public static final ImapFlag ANSWERED = new ImapFlag(SystemFlag.ANSWERED);
    /**
     * Corresponds to standard {@link Flags.Flag#DRAFT} flag
     */
    @SuppressWarnings("unused")
    public static final ImapFlag DRAFT = new ImapFlag(SystemFlag.DRAFT);
    /**
     * Corresponds to standard {@link Flags.Flag#DELETED} flag
     */
    @SuppressWarnings("unused")
    public static final ImapFlag DELETED = new ImapFlag(SystemFlag.DELETED);
    /**
     * Corresponds to standard {@link Flags.Flag#FLAGGED} flag
     */
    @SuppressWarnings("unused")
    public static final ImapFlag IMPORTANT = new ImapFlag(SystemFlag.IMPORTANT);
    /**
     * Corresponds to standard {@link Flags.Flag#RECENT} flag
     */
    @SuppressWarnings("unused")
    public static final ImapFlag RECENT = new ImapFlag(SystemFlag.RECENT);

    /**
     * Constructs custom flag with specified name
     * @param name custom flag name
     */
    public ImapFlag(String name) {
        this(null, name);
    }

    /**
     * Constructs standard flag with specified {@link SystemFlag} value
     * @param systemFlag standard flag
     */
    public ImapFlag(SystemFlag systemFlag) {
        this(systemFlag, null);
    }

    @JsonCreator
    protected ImapFlag(@JsonProperty("systemFlag") SystemFlag systemFlag, @JsonProperty("name") String name) {
        this.systemFlag = systemFlag;
        this.name = name;
    }

    /**
     * @return name of custom flag or null for standard
     */
    public String getName() {
        return name;
    }

    /**
     * @return {@link SystemFlag} value of standard flag or null for custom
     */
    @SuppressWarnings("unused")
    public SystemFlag getSystemFlag() {
        return systemFlag;
    }

    /**
     * convert to java.mail {@link Flags} object
     */
    public Flags imapFlags() {
        return systemFlag != null ? new Flags(systemFlag.systemFlag) : new Flags(name);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImapFlag imapFlag = (ImapFlag) o;
        return systemFlag == imapFlag.systemFlag &&
                Objects.equals(name, imapFlag.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(systemFlag, name);
    }

    @Override
    public String toString() {
        return "ImapFlag{" +
                "systemFlag=" + systemFlag +
                ", name='" + name + '\'' +
                '}';
    }

    /**
     * Standard IMAP Flags
     */
    public enum SystemFlag {
        SEEN(Flags.Flag.SEEN),
        ANSWERED(Flags.Flag.ANSWERED),
        DRAFT(Flags.Flag.DRAFT),
        DELETED(Flags.Flag.DELETED),
        IMPORTANT(Flags.Flag.FLAGGED),
        RECENT(Flags.Flag.RECENT);

        private final transient Flags.Flag systemFlag;

        SystemFlag(Flags.Flag systemFlag) {
            this.systemFlag = systemFlag;
        }

        /**
         * convert from java.mail {@link Flags.Flag}
         */
        public static SystemFlag valueOf(Flags.Flag systemFlag) {
            return Arrays.stream(values()).filter(f -> f.systemFlag.equals(systemFlag)).findFirst().orElse(null);
        }
    }
}
