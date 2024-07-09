import {Tag} from "antd";

interface TaskListPriorityProps {
    value: number | string
}

export const TaskPriority = ({value}: TaskListPriorityProps) => {
    const priorityKey = typeof value === "number" ? getPriorityKey(value) : value;
    return (
        <>
            {priorityKey === "low" && <Tag>Low</Tag>}
            {priorityKey === "normal" && <Tag color="green">Normal</Tag>}
            {priorityKey === "high" && <Tag color="warning">High</Tag>}
        </>
    );
}

const getPriorityKey = (priority: number) => {
    if (priority < 40) {
        return "low";
    }
    if (priority >= 40 && priority < 60) {
        return "normal";
    }

    if (priority >= 60) {
        return "high"
    }
}