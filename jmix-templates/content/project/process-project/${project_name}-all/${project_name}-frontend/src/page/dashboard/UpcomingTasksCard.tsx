import {Flex, theme, Tooltip} from "antd";
import Title from "antd/es/typography/Title";
import {Link} from "react-router-dom";
import {ClockCircleOutlined, FireOutlined} from "@ant-design/icons";
import {renderDateTime} from "../../core/format/renderDateTime.ts";
import {TaskPriority} from "../task/TaskPriority.tsx";
import Text from "antd/es/typography/Text";
import {renderRelativeDateTime} from "../../core/format/renderRelativeDateTime.ts";
import {UserTask} from "../../core/types.ts";
import dayjs from "dayjs";
import {DashboardTaskListCard} from "./DashboardTaskListCard.tsx";
import {useEffect, useState} from "react";

export interface UpcomingTasksCardProps {
    items?: UserTask[]
    loading: boolean
}

export const UpcomingTasksCard = ({items, loading}: UpcomingTasksCardProps) => {
    const {
        token: {colorTextSecondary, colorWarningText},
    } = theme.useToken();


    return (

        <>
            <DashboardTaskListCard loading={loading}
                                   tasks={items}
                                   headerIcon={<FireOutlined style={{color: colorWarningText, fontSize: "medium"}}/>}
                                   header={<Title level={4}
                                                  style={{
                                                      marginTop: "1em",
                                                      textAlign: "center",
                                                      color: colorTextSecondary
                                                  }}>
                                       Upcoming Tasks
                                   </Title>}
                                   listItemRenderer={task => <UpcomingTaskCardContent item={task}/>}
                                   emptyText="No tasks"/>
        </>
    );
};

interface UpcomingTaskCardContentProps {
    item: UserTask
}

export const UpcomingTaskCardContent = ({item}: UpcomingTaskCardContentProps) => {
    const {
        token: {colorError, colorTextTertiary},
    } = theme.useToken();


    const isOverdue = item.dueDate ? dayjs(item.dueDate).isBefore(dayjs()) : false;
    return (
        <>
            <Flex vertical={true} gap={5}>
                <Text strong><Link to={`/tasks?task=\${item.id}`}>{item.name}</Link></Text>
                <Flex align={"center"}
                      style={{width: "100%", color: isOverdue ? colorError : colorTextTertiary}} gap={5}>
                    <ClockCircleOutlined/>
                    {(isOverdue || !item.dueDate) ? <Tooltip title={`Due date: \${renderDateTime(item.dueDate, "-")}`}>
                        {isOverdue && <span>Due date passed</span>}
                        {!item.dueDate && <span>No due date</span>}
                    </Tooltip> : <TaskRelativeDueDate date={item.dueDate}/>
                    }
                    <div style={{marginLeft: "auto"}}>
                        <TaskPriority value={item.priority}/>
                    </div>
                </Flex>
            </Flex>
        </>
    );
};

interface TaskRelativeDueDateProps {
    date: string
}

const TaskRelativeDueDate = ({date}: TaskRelativeDueDateProps) => {
    const [time, setTime] = useState(Date.now());

    useEffect(() => {
        const interval = setInterval(() => setTime(Date.now()), 10 * 1000);
        return () => {
            clearInterval(interval);
        };
    }, []);

    return <Tooltip title={`Due date: \${renderDateTime(date, "-")}`}>
        <span>Due in {renderRelativeDateTime(date, time)}</span>
    </Tooltip>
}
