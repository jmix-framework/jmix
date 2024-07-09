import dayjs from "dayjs";
import {DATE_DISPLAY_FORMAT} from "./formats.ts";

export const renderDate = (value?: string | null, nullValueString? :string) => {
    return value ? dayjs(value).format(DATE_DISPLAY_FORMAT) : nullValueString;
}