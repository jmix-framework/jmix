import {renderDateTime} from "../../core/format/renderDateTime.ts";
import Text from "antd/es/typography/Text";
import dayjs from "dayjs";

export interface TaskDueDateProps {
    value?: string | null
    emptyString?: string
}

export const TaskDueDate = ({value, emptyString}: TaskDueDateProps) => {
    const isOverdue = value ? dayjs(value).isBefore(dayjs()) : false;
    return (
        <>
            {!value && emptyString && <Text>{emptyString}</Text>}
            {value && <Text type={isOverdue ? "danger" : undefined}>{renderDateTime(value, emptyString)}</Text>}
        </>
    );
};