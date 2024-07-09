import {TaskCommonProps} from "../../../core/types.ts";
import {DescriptionsItemType} from "antd/es/descriptions";
import {renderDateTime} from "../../../core/format/renderDateTime.ts";
import {Card, CardProps, Descriptions, Space, Tag} from "antd";
import {TaskPriority} from "../TaskPriority.tsx";
import {
    getProcessDefinitionRecordRepresentation
} from "../../../core/record-representation/getProcessDefinitionRecordRepresentation.ts";
import Text from "antd/es/typography/Text";
import {TaskDueDate} from "../TaskDueDate.tsx";


export const TaskSystemInfoCard = (props: TaskCommonProps & CardProps) => {
    const {task, ...rest} = props;

    const items: DescriptionsItemType[] = [
        {
            label: "Name",
            children: task?.name,
            span: 2,
        },
        {
            label: "Process",
            children: getProcessDefinitionRecordRepresentation(task?.processDefinition),
            span: 2,
        },
        {
            label: "Creation date",
            children: renderDateTime(task?.createDate, "-"),
            span: 2,
        },
        {
            label: "Due date",
            children: <TaskDueDate value={task?.dueDate} emptyString="-"/>,
            span: 2,
        },
        {
            label: "Assignee",
            children: <Tag color="processing">
                {task?.assignee}
            </Tag>,
            span: {xs: 2, sm: 2, md: 2, xl: 2, xxl: 1},
        },
        {
            label: "Priority",
            children: <TaskPriority value={task?.priority || 0}/>,
            span: {xs: 2, sm: 2, md: 2, xl: 2, xxl: 1},
        }
    ];


    return (
        <>
            <Space direction="vertical">
                <Card bordered={true} {...rest} title="Task information">
                    <Descriptions items={items} column={2}/>
                </Card>
                <Card bordered={true} title="Description" {...rest}>
                    {task?.description ? <Text>{task.description}</Text>
                        : <Text type="secondary">No task description</Text>}
                </Card>
            </Space>

        </>
    );
}