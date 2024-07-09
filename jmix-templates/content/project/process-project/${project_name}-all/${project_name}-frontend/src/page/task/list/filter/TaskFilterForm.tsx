import {useForm} from "antd/es/form/Form";
import {DatePicker, Flex, Form, Input, Row, Select} from "antd";
import {useCallback, useState} from "react";
import dayjs from "dayjs";
import Col, {type ColProps} from "antd/es/grid/col";
import Button from "antd/es/button";
import {CloseOutlined, SearchOutlined} from "@ant-design/icons";
import {TaskPriority} from "../../TaskPriority.tsx";
import {FormProps} from "antd/lib";
import {DATE_DISPLAY_FORMAT} from "../../../../core/format/formats.ts";
import {TaskFilterFormData} from "../../../../core/types.ts";
import {useSearchParams} from "react-router-dom";
import {getTaskParam} from "../../../../core/searchParamsUtils.ts";


interface TaskFilterFormProps {
    handleSubmit: (taskFilters: TaskFilterFormData) => void,
    handleReset: () => void
}

export const TaskFilterForm = ({handleSubmit, handleReset}: TaskFilterFormProps & FormProps) => {
    const [form] = useForm();
    const [showCustomDueDate, setShowCustomDueDate] = useState<boolean>();
    const [searchParams] = useSearchParams();

    const onClearButtonClick = useCallback(() => {
        form.resetFields();
        setShowCustomDueDate(false);
        handleReset();
    }, [form, handleReset]);

    const onDueDateOptionChange = useCallback((value: string) => setShowCustomDueDate(value === "period"), []);


    const onFormFinish = (values: TaskFilterFormData) => {
        handleSubmit(values);
    };

    const labelCol: ColProps = {span: 24, style: {paddingBottom: 0}};
    const wrapperCol: ColProps = {span: 24};
    const formItemStyles = {marginBottom: "0.5em"};

    const taskId = getTaskParam(searchParams);
    return (
        <>
            <Form form={form} onFinish={onFormFinish}>
                <Row style={{width: "100%"}} gutter={[10, 5]}>
                    <Col xs={24} sm={taskId ? 24 : 12} xl={taskId ? 12 : 7}>
                        <Form.Item label="Name" name="name" labelCol={labelCol} wrapperCol={wrapperCol}
                                   style={formItemStyles}>
                            <Input allowClear={true} placeholder="Enter a task name"/>
                        </Form.Item>
                    </Col>
                    <Col xs={24} sm={taskId ? 24 : 12} xl={taskId ? 12 : 7}>
                        <Form.Item name="process" label="Process" labelCol={labelCol} wrapperCol={wrapperCol}
                                   style={formItemStyles}>
                            <Input allowClear={true} placeholder="Enter a process name"/>
                        </Form.Item>
                    </Col>
                    <Col xs={24} sm={taskId ? 24 : 12} xl={taskId ? 12 : 4}>
                        <Form.Item name="priority" label="Priority" labelCol={labelCol} wrapperCol={wrapperCol}
                                   style={formItemStyles}>
                            <Select allowClear={true} placeholder="Select a priority">
                                {["low", "normal", "high"].map(value => <Select.Option value={value} key={value}>
                                    <Flex align="center">
                                        <TaskPriority value={value}/>
                                    </Flex>
                                </Select.Option>)
                                }
                            </Select>

                        </Form.Item>
                    </Col>
                    <Col xs={24} sm={taskId ? 24 : 12} xl={taskId ? 12 : 6}>
                        <Form.Item label="Creation date" name="createDatePeriod" labelCol={labelCol}
                                   wrapperCol={wrapperCol} style={formItemStyles}>
                            <DatePicker.RangePicker
                                format={DATE_DISPLAY_FORMAT}
                                disabledDate={current => current && current > dayjs().endOf('day')}
                                presets={[
                                    {label: 'Today', value: [dayjs(), dayjs()]},
                                    {label: 'Last 3 Days', value: [dayjs().add(-3, 'd'), dayjs()]},
                                    {label: 'Last 7 Days', value: [dayjs().add(-7, 'd'), dayjs()]},
                                    {label: 'Last 14 Days', value: [dayjs().add(-14, 'd'), dayjs()]},
                                ]}
                            />
                        </Form.Item>
                    </Col>
                </Row>
                <Row style={{width: "100%"}} gutter={[10, 5]}>
                    <Col xs={24} sm={taskId ? 24 : 12} xl={taskId ? 7 : 4}>
                        <Form.Item label="Due date" name="dueDate" labelCol={labelCol} wrapperCol={wrapperCol} style={formItemStyles}>
                            <Select placeholder="Select an option" onChange={onDueDateOptionChange} allowClear={true}>
                                <Select.Option value="overdue">Overdue</Select.Option>
                                <Select.Option value="today">Today</Select.Option>
                                <Select.Option value="period">Period</Select.Option>
                                <Select.Option value="noDueDate">No due date</Select.Option>
                            </Select>
                        </Form.Item>
                    </Col>
                    <Col xs={24} sm={taskId ? 24 : 12} xl={taskId ? 10 : 6}>
                        {showCustomDueDate && <Flex>
                            <Form.Item name="dueDatePeriod" label="Period" labelCol={labelCol} wrapperCol={wrapperCol} style={formItemStyles}
                                       rules={[{required: showCustomDueDate, message: "Please select a date range"}]}>
                                <DatePicker.RangePicker
                                    format={DATE_DISPLAY_FORMAT}
                                    disabled={!showCustomDueDate}
                                    presets={[
                                        {label: "Next 3 Days", value: [dayjs(), dayjs().add(3, "d")]},
                                        {label: "Next 7 Days", value: [dayjs(), dayjs().add(7, "d")]},
                                        {label: "Next 14 Days", value: [dayjs(), dayjs().add(14, "d"),]},
                                        {label: "Next 30 Days", value: [dayjs(), dayjs().add(30, "d"),]},
                                    ]}
                                />
                            </Form.Item>
                        </Flex>}
                    </Col>
                    <Col xs={24} sm={24} md={24} xl={taskId ? 24 : 14}>
                        <Flex align="end" justify="end" gap={10} style={{width: "100%", height: "100%"}}>
                            <Form.Item style={formItemStyles}>
                                <Button htmlType="button" onClick={onClearButtonClick} icon={<CloseOutlined/>}>Clear</Button>
                            </Form.Item>
                            <Form.Item style={formItemStyles}>
                                <Button type="primary" htmlType="submit" icon={<SearchOutlined/>}>Apply</Button>
                            </Form.Item>
                        </Flex>
                    </Col>
                </Row>
            </Form>
        </>
    );
};