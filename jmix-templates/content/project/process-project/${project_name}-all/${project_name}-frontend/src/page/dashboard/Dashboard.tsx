import {Card, Col, Result, Row, Statistic} from "antd";
import "./Dashboard.css";
import {UpcomingTasksCard} from "./UpcomingTasksCard.tsx";
import {RecentTasksCard} from "./RecentTasksCard.tsx";
import Title from "antd/es/typography/Title";
import useBreakpoint from "antd/es/grid/hooks/useBreakpoint";
import {useEffect, useState} from "react";
import {loadDashboardData} from "../../core/data-provider/dashboardDataProvider.ts";
import {UserTaskStatistics} from "../../core/types.ts";
import {useAuth} from "react-oidc-context";
import {ClockCircleOutlined, HourglassOutlined} from "@ant-design/icons";
import {MonthlyStatistic} from "./MonthlyStatistic.tsx";
import {RecentActivityCard} from "./RecentActivityCard.tsx";


export const Dashboard = () => {
    const {user} = useAuth();
    const {xxl} = useBreakpoint();
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState();
    const [userTaskStatistics, setUserTaskStatistics] = useState<UserTaskStatistics>();

    useEffect(() => {
        loadDashboardData(user?.access_token)
            .then(value => {
                setLoading(false);
                setUserTaskStatistics(value?.data);
            }).catch(error => {
            console.log("Error on dashboard data loading:", error);
            setLoading(false);
            setError(error);
        });
    }, [user?.access_token]);

    if (error) {
        return <Result
            status="500"
            title="Internal error"
            subTitle="Sorry, something went wrong."
        />
    }

    return (
        <>
            <Row style={{
                paddingInline: xxl ? "10em" : "2em",
                paddingTop: "2em",
                paddingBottom: "1em",
                height: "100%",
                width: "100%"
            }}
                 gutter={[10, 10]}>
                <Col xs={24}>
                    <Title level={3} style={{marginTop: 0}}>My Tasks Overview</Title>
                </Col>
                <Col xs={24} sm={24} md={24} lg={14} xl={12}>
                    <Row gutter={[10, 10]}>
                        <Col xs={12} sm={8} md={8} xl={8}>
                            <Card bordered={false}>
                                <Statistic
                                    title="Active"
                                    loading={loading}
                                    className="dashboard__active-task-statistics"
                                    value={userTaskStatistics?.activeTasksCount}
                                    prefix={<HourglassOutlined/>}
                                />
                            </Card></Col>
                        <Col xs={12} sm={8} md={8} xl={8}>
                            <Card bordered={false}>
                                <Statistic
                                    title="Overdue"
                                    loading={loading}
                                    value={userTaskStatistics?.overdueTasksCount}
                                    className="dashboard__overdue-task-statistics"
                                    prefix={<ClockCircleOutlined/>}
                                />
                            </Card>
                        </Col>
                        <Col xs={12} sm={8} md={8} xl={8}>
                            <Card bordered={false}>
                                <MonthlyStatistic loading={loading} data={userTaskStatistics?.monthlyStatistics}/>
                            </Card>
                        </Col>
                        <Col xs={24} sm={24} md={18} lg={24} xl={24}>
                            <RecentActivityCard loading={loading} data={userTaskStatistics?.weeklyActivity}/>
                        </Col>
                    </Row>
                </Col>
                <Col xs={24} sm={15} md={12} lg={10} xl={6}>
                    <RecentTasksCard items={userTaskStatistics?.lastCreatedTasks} loading={loading}/>
                </Col>
                <Col xs={24} sm={15} md={12} lg={10} xl={6}>
                    <UpcomingTasksCard items={userTaskStatistics?.upcomingTasks} loading={loading}/>
                </Col>
            </Row>
        </>
    );
};