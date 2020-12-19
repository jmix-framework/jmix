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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.mail.Flags;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FlagsConverter {

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String convertToString(List<ImapFlag> flags) {
        try {
            return OBJECT_MAPPER.writeValueAsString(flags);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Can't convert flags " + flags, e);
        }
    }

    public static List<ImapFlag> convertToImapFlags(Flags flags) {
        Flags.Flag[] systemFlags = flags.getSystemFlags();
        String[] userFlags = flags.getUserFlags();
        List<ImapFlag> internalFlags = new ArrayList<>(systemFlags.length + userFlags.length);
        for (Flags.Flag systemFlag : systemFlags) {
            internalFlags.add(new ImapFlag(ImapFlag.SystemFlag.valueOf(systemFlag)));
        }
        for (String userFlag : userFlags) {
            internalFlags.add(new ImapFlag(userFlag));
        }
        return internalFlags;
    }

    public static List<ImapFlag> convertToImapFlags(String flags) {
        try {
            return OBJECT_MAPPER.readValue(flags, new TypeReference<List<ImapFlag>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Can't parse flags from string " + flags, e);
        }
    }

    public static Flags convertToFlags(List<ImapFlag> imapFlags) {
        Flags flags = new Flags();
        imapFlags.forEach(imapFlag -> flags.add(imapFlag.imapFlags()));
        return flags;
    }
}
