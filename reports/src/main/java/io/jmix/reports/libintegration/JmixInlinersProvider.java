/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.reports.libintegration;

import com.haulmont.yarg.formatters.factory.inline.DefaultInlinersProvider;

public class JmixInlinersProvider extends DefaultInlinersProvider {

    public JmixInlinersProvider(){
        super();
        addInliner(new FileStorageContentInliner());
    }
}
