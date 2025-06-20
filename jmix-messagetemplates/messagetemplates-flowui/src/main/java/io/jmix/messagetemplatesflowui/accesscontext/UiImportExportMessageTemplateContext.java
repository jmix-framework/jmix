/*
 * Copyright (c) Haulmont 2023. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.jmix.messagetemplatesflowui.accesscontext;

import io.jmix.core.accesscontext.SpecificOperationAccessContext;
import io.jmix.messagetemplates.entity.MessageTemplate;
import io.jmix.messagetemplatesflowui.view.messagetemplate.MessageTemplateListView;

/**
 * An access context to check permissions to import/export {@link MessageTemplate MessageTemplates} using
 * import/export actions in {@link MessageTemplateListView}.
 */
public class UiImportExportMessageTemplateContext extends SpecificOperationAccessContext {

    public static final String NAME = "messagetemplates.importExportMessageTemplate";

    public UiImportExportMessageTemplateContext() {
        super(NAME);
    }
}
