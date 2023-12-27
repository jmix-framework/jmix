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

import org.eclipse.angus.mail.iap.ProtocolException;
import org.eclipse.angus.mail.iap.Response;
import org.eclipse.angus.mail.imap.protocol.FetchItem;
import org.eclipse.angus.mail.imap.protocol.FetchResponse;
import org.eclipse.angus.mail.imap.protocol.IMAPProtocol;
import org.eclipse.angus.mail.imap.protocol.IMAPResponse;
import org.eclipse.angus.mail.util.MailLogger;

import java.io.IOException;
import java.util.Properties;

public class JmixIMAPProtocol extends IMAPProtocol {

    JmixIMAPProtocol(String name, String host, int port, Properties props,
                     boolean isSSL, MailLogger logger) throws IOException, ProtocolException {
        super(name, host, port, props, isSSL, logger);
    }

    @Override
    public FetchItem[] getFetchItems() {
        return new FetchItem[] { ThreadExtension.FETCH_ITEM };
    }

    @Override
    public Response readResponse() throws IOException, ProtocolException {

        IMAPResponse r = new IMAPResponse(this);
        if (r.keyEquals("FETCH"))
            r = new FetchResponse(r, getFetchItems()) {
                @Override
                public boolean isNextNonSpace(char c) {
                    // we need to set UTF-8 since each response from server rely on it
                    // (see toString(byte[] buffer, int start, int end) of com.sun.mail.iap.Response
                    // and there are some attachments with non-mime encoded filename parameter containing UTF-8
                    // basically this should be resolved via support of capability 'UTF8=ACCEPT',
                    // which should be enabled during authentication
                    // however it leads to "BAD [CLIENTBUG] SELECT Folder encoding error" response while open folder
                    // for some IMAP servers, e.g. yandex
                    // see writeMailboxName method of IMAPProtocol for encoding options
                    utf8 = true;
                    return super.isNextNonSpace(c);
                }
            };
        return r;
    }

}
