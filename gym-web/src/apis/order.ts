
import request from "@/utils/request";

interface OrderList {
    pageNum: number,
    pageSize: number,
    courseName?: string
}

interface CoachRevenueParams {
    coachId: number,
    startDate?: string,
    endDate?: string
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
    console.log(pageNum, pageSize, courseName)
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
