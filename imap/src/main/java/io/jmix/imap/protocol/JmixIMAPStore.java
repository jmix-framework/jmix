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

package io.jmix.imap.protocol;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.IMAPProtocol;

import javax.mail.Session;
import javax.mail.URLName;
import java.io.IOException;

public class JmixIMAPStore extends IMAPStore {
    public JmixIMAPStore(Session session, URLName url) {
        super(session, url);
    }

    protected JmixIMAPStore(Session session, URLName url,
                            String name, boolean isSSL) {
        super(session, url, name, isSSL);
    }

    @Override
    protected IMAPProtocol newIMAPProtocol(String host, int port) throws IOException, ProtocolException {
        return new JmixIMAPProtocol(name, host, port,
                session.getProperties(),
                isSSL,
                logger
        );
    }
}
