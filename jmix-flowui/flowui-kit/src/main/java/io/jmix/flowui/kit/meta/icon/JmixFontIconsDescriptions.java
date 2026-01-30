/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.kit.meta.icon;

import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.theme.lumo.LumoIcon;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import io.jmix.flowui.kit.meta.StudioAPI;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

@StudioAPI(description = "Meta descriptions of jmix font icons")
interface JmixFontIconsDescriptions {

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.OK, vaadinIconRef = VaadinIcon.CHECK)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.CANCEL, vaadinIconRef = VaadinIcon.BAN)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.YES, vaadinIconRef = VaadinIcon.CHECK)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.NO, vaadinIconRef = VaadinIcon.BAN)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.DIALOG_OK, vaadinIconRef = VaadinIcon.CHECK)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.DIALOG_CANCEL, vaadinIconRef = VaadinIcon.BAN)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.DIALOG_YES, vaadinIconRef = VaadinIcon.CHECK)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.DIALOG_NO, vaadinIconRef = VaadinIcon.BAN)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.DIALOG_CLOSE, vaadinIconRef = VaadinIcon.CLOSE)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.CREATE_ACTION, vaadinIconRef = VaadinIcon.PLUS)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.EDIT_ACTION, vaadinIconRef = VaadinIcon.PENCIL)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.REMOVE_ACTION, vaadinIconRef = VaadinIcon.TRASH)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.ADD_ACTION, vaadinIconRef = VaadinIcon.PLUS)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.EXCLUDE_ACTION, vaadinIconRef = VaadinIcon.CLOSE)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.READ_ACTION, vaadinIconRef = VaadinIcon.EYE)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.REFRESH_ACTION, vaadinIconRef = VaadinIcon.REFRESH)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.VIEW_CLOSE_ACTION, vaadinIconRef = VaadinIcon.BAN)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.DETAIL_SAVE_CLOSE_ACTION, vaadinIconRef = VaadinIcon.CHECK)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.DETAIL_SAVE_ACTION, vaadinIconRef = VaadinIcon.ARCHIVE)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.DETAIL_CLOSE_ACTION, vaadinIconRef = VaadinIcon.BAN)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.DETAIL_DISCARD_ACTION, vaadinIconRef = VaadinIcon.BAN)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.DETAIL_ENABLE_EDITING_ACTION, vaadinIconRef = VaadinIcon.PENCIL)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.LOOKUP_SELECT_ACTION, vaadinIconRef = VaadinIcon.CHECK)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.LOOKUP_DISCARD_ACTION, vaadinIconRef = VaadinIcon.BAN)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.ENTITY_LOOKUP_ACTION, vaadinIconRef = VaadinIcon.ELLIPSIS_DOTS_H)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.ENTITY_CLEAR_ACTION, vaadinIconRef = VaadinIcon.CLOSE)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.ENTITY_OPEN_ACTION, vaadinIconRef = VaadinIcon.SEARCH_PLUS)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.VALUE_CLEAR_ACTION, vaadinIconRef = VaadinIcon.CLOSE)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.MULTI_VALUE_SELECT_ACTION, vaadinIconRef = VaadinIcon.ELLIPSIS_DOTS_H)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.DATE_INTERVAL_ACTION, vaadinIconRef = VaadinIcon.ELLIPSIS_DOTS_H)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.LOGOUT_ACTION, vaadinIconRef = VaadinIcon.SIGN_OUT)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.BULK_EDIT_ACTION, vaadinIconRef = VaadinIcon.TABLE)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.JSON_EXPORT_ACTION, vaadinIconRef = VaadinIcon.FILE_CODE)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.EXCEL_EXPORT_ACTION, vaadinIconRef = VaadinIcon.FILE_TABLE)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.SHOW_ROLE_ASSIGNMENTS_ACTION, vaadinIconRef = VaadinIcon.SHIELD)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.ASSIGN_TO_USERS_ACTION, vaadinIconRef = VaadinIcon.USERS)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.RUN_REPORT_ACTION, vaadinIconRef = VaadinIcon.PRINT)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.RUN_SINGLE_ENTITY_REPORT_ACTION, vaadinIconRef = VaadinIcon.PRINT)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.RUN_LIST_ENTITY_REPORT_ACTION, vaadinIconRef = VaadinIcon.PRINT)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.SHOW_EXECUTION_REPORT_HISTORY_ACTION, vaadinIconRef = VaadinIcon.CLOCK)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.DAYS_OF_WEEK_EDIT_ACTION, vaadinIconRef = VaadinIcon.ELLIPSIS_DOTS_H)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.USER_MENU_THEME_SWITCH_ACTION_SYSTEM_THEME, vaadinIconRef = VaadinIcon.ADJUST)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.USER_MENU_THEME_SWITCH_ACTION_LIGHT_THEME, vaadinIconRef = VaadinIcon.SUN_O)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.USER_MENU_THEME_SWITCH_ACTION_DARK_THEME, vaadinIconRef = VaadinIcon.MOON_O)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.USER_MENU_SUBSTITUTE_USER_ACTION, vaadinIconRef = VaadinIcon.EXCHANGE)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.GENERIC_FILTER_SAVE_ACTION, vaadinIconRef = VaadinIcon.ARCHIVE)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.GENERIC_FILTER_EDIT_ACTION, vaadinIconRef = VaadinIcon.PENCIL)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.GENERIC_FILTER_COPY_ACTION, vaadinIconRef = VaadinIcon.COPY)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.GENERIC_FILTER_REMOVE_ACTION, vaadinIconRef = VaadinIcon.TRASH)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.GENERIC_FILTER_CLEAR_VALUES_ACTION, vaadinIconRef = VaadinIcon.ERASER)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.GENERIC_FILTER_MAKE_DEFAULT_ACTION, vaadinIconRef = VaadinIcon.STAR)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.GENERIC_FILTER_ADD_CONDITION_ACTION, vaadinIconRef = VaadinIcon.PLUS)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.CREATE_NOTIFICATION_ACTION, vaadinIconRef = VaadinIcon.PLUS)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.INTERVAL_FIELD_HELP, vaadinIconRef = VaadinIcon.QUESTION_CIRCLE)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.COMBO_BUTTON_DROPDOWN, vaadinIconRef = VaadinIcon.CHEVRON_DOWN)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.PAGINATION_FIRST_PAGE, vaadinIconRef = VaadinIcon.ANGLE_DOUBLE_LEFT)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.PAGINATION_PREVIOUS_PAGE, vaadinIconRef = VaadinIcon.ANGLE_LEFT)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.PAGINATION_NEXT_PAGE, vaadinIconRef = VaadinIcon.ANGLE_RIGHT)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.PAGINATION_LAST_PAGE, vaadinIconRef = VaadinIcon.ANGLE_DOUBLE_RIGHT)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.TWIN_COLUMN_SELECT_ALL, vaadinIconRef = VaadinIcon.ANGLE_DOUBLE_RIGHT)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.TWIN_COLUMN_DESELECT_ALL, vaadinIconRef = VaadinIcon.ANGLE_DOUBLE_LEFT)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.TWIN_COLUMN_SELECT, vaadinIconRef = VaadinIcon.ANGLE_RIGHT)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.TWIN_COLUMN_DESELECT, vaadinIconRef = VaadinIcon.ANGLE_LEFT)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.GENERIC_FILTER_SETTINGS, vaadinIconRef = VaadinIcon.COG)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.GENERIC_FILTER_CONDITION_REMOVE, vaadinIconRef = VaadinIcon.TRASH)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.DATA_GRID_HEADER_FILTER, vaadinIconRef = VaadinIcon.FILTER)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.DATA_GRID_HEADER_FILTER_APPLY, vaadinIconRef = VaadinIcon.CHECK)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.DATA_GRID_HEADER_FILTER_CANCEL, vaadinIconRef = VaadinIcon.BAN)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.DATA_GRID_HEADER_FILTER_CLEAR, vaadinIconRef = VaadinIcon.ERASER)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.SEARCH_FIELD_SEARCH, vaadinIconRef = VaadinIcon.SEARCH)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.SEARCH_FIELD_SETTINGS, vaadinIconRef = VaadinIcon.ELLIPSIS_DOTS_V)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.NOTIFICATIONS_INDICATOR, vaadinIconRef = VaadinIcon.BELL)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.NOTIFICATIONS_INDICATOR_REFRESH, vaadinIconRef = VaadinIcon.REFRESH)

    @JmixFontIconDescription(jmixIcon = JmixFontIcon.COLUMNS_GROUPER_ITEM_ADD, lumoIconRef = LumoIcon.PLUS)
    @JmixFontIconDescription(jmixIcon = JmixFontIcon.COLUMNS_GROUPER_ITEM_REMOVE, lumoIconRef = LumoIcon.CROSS)
    void jmixFontIconsDescriptions();

    /**
     * Description of jmix font icon default value.
     * Will be used in Studio (e.g., in line markers)
     */
    @Target(ElementType.METHOD)
    @Repeatable(value = JmixFontIconDescriptions.class)
    @interface JmixFontIconDescription {
        JmixFontIcon jmixIcon();

        VaadinIcon vaadinIconRef() default VaadinIcon.PICTURE;

        LumoIcon lumoIconRef() default LumoIcon.PHOTO;
    }

    @Target(ElementType.METHOD)
    @interface JmixFontIconDescriptions {
        JmixFontIconDescription[] value();
    }
}
