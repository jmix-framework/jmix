import dayjs from "dayjs";
import {DATE_TIME_DISPLAY_FORMAT} from "./formats.ts";

export const renderDateTime = (value?: string | null, nullValueString? :string) => {
    return value ? dayjs(value).format(DATE_TIME_DISPLAY_FORMAT) : nullValueString;
}