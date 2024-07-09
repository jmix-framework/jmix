import {Flex, theme, Tooltip} from "antd";
import Title from "antd/es/typography/Title";
import {Link} from "react-router-dom";
import {CalendarOutlined, HistoryOutlined} from "@ant-design/icons";
import {renderDateTime} from "../../core/format/renderDateTime.ts";
import {TaskPriority} from "../task/TaskPriority.tsx";
import {UserTask} from "../../core/types.ts";
import Text from "antd/es/typography/Text";
import {renderRelativeDateTime} from "../../core/format/renderRelativeDateTime.ts";
import {DashboardTaskListCard} from "./DashboardTaskListCard.tsx";
import {useEffect, useState} from "react";

export interface RecentTasksCardProps {
    items?: UserTask[]
    loading: boolean
}

export const RecentTasksCard = ({items, loading}: RecentTasksCardProps) => {
    const {
        token: {colorTextSecondary},
    } = theme.useToken();

    return (
        <>
            <DashboardTaskListCard loading={loading}
                                   tasks={items}
                                   header={<Title level={4} style={{
                                       marginTop: "1em",
                                       color: colorTextSecondary
                                   }}>Recent Tasks</Title>}
                                   headerIcon={<HistoryOutlined
                                       style={{color: "rgba(0,0,0,.45)", fontSize: "medium"}}/>}
                                   listItemRenderer={task => <RecentTaskCardContent item={task}/>}
                                   emptyText="No tasks"
            />
        </>
    );
};

interface RecentTaskCardProps {
    item: UserTask
}

export const RecentTaskCardContent = ({item}: RecentTaskCardProps) => {
    const {
        token: {colorTextTertiary},
    } = theme.useToken();

    return (
        <>
            <Flex vertical={true} gap={5}>
                <Text strong><Link to={`/tasks?task=\${item.id}`}>{item.name}</Link></Text>
                <Flex align={"center"}
                      style={{width: "100%", color: colorTextTertiary}} gap={5}>
                    <CalendarOutlined/>

                    <TaskRelativeCreateDate date={item.createDate}/>

                    <div style={{marginLeft: "auto"}}>
                        <TaskPriority value={item.priority}/>
                    </div>
                </Flex>
            </Flex>
        </>
    );
};

interface TaskRelativeCreateDateProps {
    date?: string
}

const TaskRelativeCreateDate = ({date}: TaskRelativeCreateDateProps) => {
    const [time, setTime] = useState(Date.now());

    useEffect(() => {
        const interval = setInterval(() => setTime(Date.now()), 10 * 1000);
        return () => {
            clearInterval(interval);
        };
    }, []);

    return <Tooltip title={`Creation date: \${renderDateTime(date)}`}><span>Created {renderRelativeDateTime(time, date)} ago</span>
    </Tooltip>
}