import { DeleteOutlined, EditOutlined, ExclamationCircleFilled } from '@ant-design/icons';
import { Button, Card, message, Modal } from 'antd';
import services from 'apis';
import { PageConstant } from 'commons/page.constant';
import { UrlResolver } from 'commons/url-resolver';
import RenderStatus from 'components/commons/RenderStatus';
import DigitalTable from 'components/digital-table/digital-table';
import { CategoryModel, ICategoryRequest } from 'models/category.model';
import { useState } from 'react'
import { Link, useHistory } from 'react-router-dom'

export default function Category() {
    const history = useHistory();
    const [reloadPage, setReloadPage] = useState(null);
    const columns = [
        {
            title: "Mã",
            dataIndex: "code",
        },
        {
            title: "Tên",
            dataIndex: "name",
        },
        {
            title: "Trạng Thái",
            render: (data: CategoryModel) => (
                <>
                    <RenderStatus status={data.status} />
                </>
            ),
        },
        {
            title: "",
            width: 10,
            render: (data: CategoryModel) => (
                <>
                    <div className="ant-employed d-flex align-items-center justify-content-center">
                        <Button className="mx-2 table-action-button" onClick={() => { history.push(UrlResolver.buildUrl(`/${PageConstant.category}/${data.id}/edit`)); }} type="default">
                            <EditOutlined />
                        </Button>
                        <Button className='table-action-button' onClick={() => { showDeleteConfirm(data); }}>
                            <DeleteOutlined style={{ color: '#e90000' }} />
                        </Button>
                    </div>
                </>
            ),
        },
    ];

    const { confirm } = Modal;
    const showDeleteConfirm = (data: any) => {
        confirm({
            title: "Bạn muốn xóa category này?",
            icon: <ExclamationCircleFilled />,
            content: `Danh mục:  ${data.code} sẽ bị xóa !`,
            okText: "Đồng ý",
            okType: "primary",
            cancelText: "Đóng",
            onOk() {
                services.categoryApi.delCategory(data.id).then(() => {
                    message.success("Xóa thành công", 1.5)
                    setReloadPage([]);
                });
            },
        });
    };

    const getDataSource = async (filter: ICategoryRequest) => {
        return services.categoryApi.getCategory(filter);
    }

    return (
        <div>
            <Card bordered={false} className="criclebox tablespace mb-24" title="Danh sách danh mục"
                extra={
                    <>
                        <Link to={`${PageConstant.category}/add`}>  <Button type="primary">Tạo mới +</Button></Link>
                    </>
                }
            />

            <DigitalTable reloadPage={reloadPage} columns={columns} getDataSource={getDataSource} />
        </div>
    )
}
