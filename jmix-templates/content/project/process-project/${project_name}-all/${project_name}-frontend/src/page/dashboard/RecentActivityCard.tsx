import {Card, Flex, Skeleton, theme} from "antd";
import Title from "antd/es/typography/Title";
import {RecentActivityChart} from "./RecentActivityChart.tsx";
import {TaskExecutionPeriodStatistics} from "../../core/types.ts";
import {BarChartOutlined} from "@ant-design/icons";

export interface RecentActivityCardProps {
    data?: TaskExecutionPeriodStatistics
    loading: boolean
}

export const RecentActivityCard = ({data, loading}: RecentActivityCardProps) => {
    const {
        token: {colorTextTertiary},
    } = theme.useToken();

    return (
        <>
            <Card styles={{
                header: {
                    borderBottom: "none"
                },
                body: {
                    paddingTop: 0
                }
            }}
                  title={<Title level={4}>Recent Activity</Title>}>
                {!loading && data && <RecentActivityChart data={data}/>}
                {loading &&
                    <Flex style={{height: "23em"}} align="center" justify="center">
                        <Skeleton.Node active={true}>
                            <BarChartOutlined style={{fontSize: 40, color: colorTextTertiary}}/>
                        </Skeleton.Node>
                    </Flex>
                }
            </Card>
        </>
    );
};