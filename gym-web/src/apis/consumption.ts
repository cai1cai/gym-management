import request from '@/utils/request';
import {AxiosPromise} from "axios";

interface ConsumptionListParams {
    userId: string,
    pageNum: number,
    pageSize: number
}

interface ConsumptionSearchParams {
    userId: string,
    courseName?: string,
    pageNum: number,
    pageSize: number
}

// 获取会员消费记录
export const getMemberConsumptionService = ({userId, pageNum, pageSize}: ConsumptionListParams): AxiosPromise => {
  return request.get('/member/consumption/list', {
        params: {
            userId,
            pageNum,
            pageSize
        }
    });
};

// 搜索会员消费记录
export const searchMemberConsumptionService = ({userId, courseName, pageNum, pageSize}: ConsumptionSearchParams): AxiosPromise => {
  return request.get('/member/consumption/search', {
        params: {
            userId,
            courseName,
            pageNum,
            pageSize
        }
    });
};
