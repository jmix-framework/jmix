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

package io.jmix.imap.exception;


import javax.mail.MessagingException;

public class ImapException extends RuntimeException {

    public ImapException(String message) {
        super(message);
    }

    public ImapException(MessagingException cause) {
        super(causeDescription(cause));
        addSuppressed(cause);
    }

    public ImapException(String message, MessagingException cause) {
        super(String.format("%s caused by: %s", message, causeDescription(cause)));
        addSuppressed(cause);
    }

    protected static String causeDescription(MessagingException e) {
        String message = e.getMessage();
        return String.format("[%s]%s", e.getClass().getName(), message != null ? message : e.toString() );
    }
}
