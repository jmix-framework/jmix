/*
 * Copyright (c) 2008-2018 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jmix.reports.yarg.formatters.factory.inline;

import io.jmix.reports.yarg.formatters.impl.inline.BitmapContentInliner;
import io.jmix.reports.yarg.formatters.impl.inline.ContentInliner;
import io.jmix.reports.yarg.formatters.impl.inline.HtmlContentInliner;
import io.jmix.reports.yarg.formatters.impl.inline.ImageContentInliner;

import java.util.ArrayList;
import java.util.List;

public class DefaultInlinersProvider implements ReportInlinersProvider {

    protected List<ContentInliner> contentInliners;

    public DefaultInlinersProvider() {
        this.contentInliners = new ArrayList<>();
        addInliner(new BitmapContentInliner());
        addInliner(new HtmlContentInliner());
        addInliner(new ImageContentInliner());
    }

    public void addInliner(ContentInliner inliner) {
        this.contentInliners.add(inliner);
    }

    public void removeInliner(ContentInliner inliner) {
        this.contentInliners.remove(inliner);
    }

    public List<ContentInliner> getContentInliners() {
        return contentInliners;
    }
}
