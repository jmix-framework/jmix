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

import com.sun.mail.imap.protocol.FetchItem;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.Item;

import jakarta.mail.FetchProfile;

@SuppressWarnings("SpellCheckingInspection")
public class ThreadExtension {

    protected static final String ITEM_NAME = "X-GM-THRID";
    public static final String CAPABILITY_NAME = "X-GM-EXT-1";
    protected static final JmixProfileItem THREAD_ID_ITEM = new JmixProfileItem();
    public static final FetchItem FETCH_ITEM = new FetchItem(ITEM_NAME, THREAD_ID_ITEM) {
        @Override
        public Object parseItem(FetchResponse r) {
            return new X_GM_THRID(r);
        }
    };

    public static class X_GM_THRID implements Item {

        @SuppressWarnings({"unused"})
        final int seqnum;

        public final long x_gm_thrid;

        X_GM_THRID(FetchResponse r) {
            seqnum = r.getNumber();
            r.skipSpaces();
            x_gm_thrid = r.readLong();
        }


    }

    static class JmixProfileItem extends FetchProfile.Item {
        JmixProfileItem() {
            super(ITEM_NAME);
        }
    }

    public static class FetchProfileItem extends FetchProfile.Item {
        FetchProfileItem() {
            super(ThreadExtension.ITEM_NAME);
        }

        public static final FetchProfileItem X_GM_THRID = new FetchProfileItem();
    }
}
