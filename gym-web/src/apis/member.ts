import request from '@/utils/request';
import {AxiosPromise} from "axios";

interface MemberList {
    pageNum: number,
    pageSize: number,
    userRealName?: string
}

interface MemberAddParams {
    userRealName: string,
    memberCardType: string,
    sex: number,
    userPhone: string,
    remainingCount?: number,
    cardAmount?: number
}

interface MemberEditParams {
    userId: string,
    memberCardId: string,
    userRealName: string,
    memberCardType: string,
    sex: number,
    userPhone: string,
    remainingCount?: number,
    cardAmount?: number
}

interface MemberDeleteParams {
    memberCardId: string
}

interface MemberSearchParams {
    userRealName: string
}

export const getMemberListService = ({ pageNum, pageSize }: MemberList) => {
    console.log(pageNum, pageSize)
    return request.get('/member/list', {
        params: {
            pageNum,
            pageSize
        }
    });
};


export const addMemberService = ({userRealName, memberCardType, sex, userPhone, remainingCount, cardAmount}: MemberAddParams): AxiosPromise => {
    return request.post('/member/add', {userRealName, memberCardType, sex, userPhone, remainingCount, cardAmount});
}

export const editMemberService = ({userId, memberCardId, userRealName, memberCardType, sex, userPhone, remainingCount, cardAmount}: MemberEditParams): AxiosPromise => {
    return request.put('/member/edit', {userId, memberCardId, userRealName, memberCardType, sex, userPhone, remainingCount, cardAmount});
}

export const deleteMemberService = ({memberCardId}: MemberDeleteParams): AxiosPromise => {
    console.log(memberCardId)
    return request.delete('/member/delete', {
        params: {
            memberCardId
        }
    });
}

export const searchMemberService = ({pageNum, pageSize, userRealName}: MemberList) => {
    console.log(pageNum, pageSize, userRealName)
    return request.get('/member/search', {
        params: {
            pageNum,
            pageSize,
            userRealName
        }
    });
};

