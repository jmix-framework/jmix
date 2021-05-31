/*
 * Copyright 2021 Haulmont.
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

package io.jmix.ui.exception;

import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.Notifications;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component("ui_DeletePolicyHandler")
public class DeletePolicyExceptionHandler extends AbstractUiExceptionHandler implements Ordered {

	@Autowired
	protected Messages messages;

	@Autowired
	protected Metadata metadata;

	@Autowired
	private MessageTools messageTools;

	@Autowired
	private ExtendedEntities extendedEntities;

	public DeletePolicyExceptionHandler() {
		super(DeletePolicyException.class.getName());
	}

	@Override
	protected void doHandle(String className, String message, @Nullable Throwable throwable, UiContext context) {
		if (throwable != null) {
			DeletePolicyException exception = (DeletePolicyException) throwable;

			String caption = null;
			String notificationMessage = null;

			MetaClass deletedEntityMetaClass = recognizeEntityMetaClass(exception.getEntity());
			if (deletedEntityMetaClass != null) {
				String customCaptionKey = String.format("deletePolicy.caption.%s", deletedEntityMetaClass.getName());
				String customCaption = messages.getMessage(customCaptionKey);
				if (!customCaptionKey.equals(customCaption)) {
					caption = customCaption;
				}

				String customMessageKey = String.format("deletePolicy.references.message.%s", deletedEntityMetaClass.getName());
				String customMessage = messages.getMessage(customMessageKey);
				if (!customMessageKey.equals(customMessage)) {
					notificationMessage = customMessage;
				}
			}

			if (StringUtils.isEmpty(caption)) {
				caption = messages.getMessage("deletePolicy.caption");
			}

			if (StringUtils.isEmpty(notificationMessage)) {
				MetaClass metaClass = recognizeEntityMetaClass(exception.getRefEntity());
				if (metaClass != null) {
					String localizedEntityName = messageTools.getEntityCaption(metaClass);
					String referencesMessage = messages.getMessage("deletePolicy.references.message");
					notificationMessage = String.format(referencesMessage, localizedEntityName);
				}
			}

			context.getNotifications()
					.create(Notifications.NotificationType.ERROR)
					.withCaption(caption)
					.withDescription(notificationMessage)
					.show();
		}
	}

	@Nullable
	protected MetaClass recognizeEntityMetaClass(String entityName) {
		if (!StringUtils.isEmpty(entityName)) {
			MetaClass metaClass = metadata.findClass(entityName);
			if (metaClass != null) {
				MetaClass originalMetaClass = extendedEntities.getOriginalMetaClass(metaClass);
				metaClass = originalMetaClass != null ? originalMetaClass : metaClass;
			}
			return metaClass;
		}
		return null;
	}

	@Override
	public int getOrder() {
		return JmixOrder.HIGHEST_PRECEDENCE + 30;
	}
}
