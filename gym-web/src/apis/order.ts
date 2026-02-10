
import request from "@/utils/request";

interface OrderList {
    pageNum: number,
    pageSize: number,
    courseName?: string
}

/**
 * 教练收益查询参数（日期格式：YYYY-MM-DD）
 */
interface CoachRevenueParams {
    coachId: number,
    startDate?: string,
    endDate?: string
}

interface OrderCreate {
    userId: number,
    courseId: number
}

export const getOrderListService = ({pageNum, pageSize}: OrderList) => {
    return request.get('/order/list', {
        params: {
            pageNum,
            pageSize
        }
    });
};

export const searchOrderService = ({pageNum, pageSize, courseName}: OrderList) => {
    return request.get('/order/search', {
        params: {
            pageNum,
            pageSize,
            courseName
        }
    });
};

/**
 * 查询教练收益统计
 */
export const getCoachRevenueService = ({coachId, startDate, endDate}: CoachRevenueParams) => {
    return request.get('/order/coach/revenue', {
        params: {
            coachId,
            startDate,
            endDate
        }
    });
};

/**
 * 查询教练总收益
 */
export const getCoachTotalRevenueService = ({coachId, startDate, endDate}: CoachRevenueParams) => {
    return request.get('/order/coach/totalRevenue', {
        params: {
            coachId,
            startDate,
            endDate
        }
    });
};

/**
 * 管理员替会员创建订单
 */
export const createOrderService = (data: OrderCreate) => {
    return request.post('/order/create', data);
};
