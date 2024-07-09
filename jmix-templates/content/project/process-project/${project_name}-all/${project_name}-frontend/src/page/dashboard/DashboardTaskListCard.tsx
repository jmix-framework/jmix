import {UserTask} from "../../core/types.ts";
import {ReactNode} from "react";
import {Card, Empty, Flex, List, Skeleton} from "antd";
import {useNavigate} from "react-router-dom";
import "./DahboardTaskListCard.css";
import Text from "antd/es/typography/Text";

export interface DashboardTaskListCardProps {
    tasks?: UserTask[]
    loading: boolean
    header: ReactNode
    headerIcon?: ReactNode
    listItemRenderer: (task: UserTask) => ReactNode
    emptyText?: string
}

export const DashboardTaskListCard = ({
                                          headerIcon,
                                          header,
                                          listItemRenderer,
                                          loading,
                                          tasks,
                                          emptyText
                                      }: DashboardTaskListCardProps) => {
    const navigate = useNavigate();
    return (
        <>
            <Card bordered={true}
                  title={<Flex className="dashboard-task-list-card__header">
                      {headerIcon}
                      {header}
                  </Flex>}
                  className="dashboard-task-list-card__root-card">

                {loading && <List
                    itemLayout="horizontal"
                    dataSource={[0, 1, 2, 3, 4]}
                    renderItem={(_item) => (
                        <List.Item className="dashboard-task-list-card__list-item">
                            <List.Item.Meta className="dashboard-task-list-card__list-item-meta"
                                            description={<Card
                                                className="dashboard-task-list-card__item-card">
                                                <Skeleton active={true} title={false} paragraph={{rows: 2}}/>
                                            </Card>}
                            />
                        </List.Item>
                    )}
                />

                }
                {!loading && (!tasks || tasks.length === 0) &&
                    <Flex style={{height: "50%"}} justify={"center"} align={"center"}>
                        <Empty description={<Text type="secondary" strong={true}>{emptyText || "No data"}</Text> }/>
                    </Flex>
                }

                {!loading && tasks && tasks.length > 0 && <List
                    loading={loading}
                    itemLayout="horizontal"
                    dataSource={tasks}
                    renderItem={(item) => (
                        <List.Item
                            className="dashboard-task-list-card__list-item">
                            <List.Item.Meta className="dashboard-task-list-card__list-item-meta"
                                            description={<Card className="dashboard-task-list-card__item-card"
                                                               onClick={() => {
                                                                   navigate(`/tasks?task=\${item.id}`);
                                                               }}>{listItemRenderer(item)}</Card>}
                            />
                        </List.Item>
                    )}
                />}
            </Card>
        </>
    );
};