<template>

  <el-card class="page-container">
    <template #header>
      <div class="header">
        <h2>订单列表</h2>
        <div class="extra">
          <el-button type="primary" @click="handleAddOrder">
            <el-icon><Plus /></el-icon> 新增订单
          </el-button>
          <el-input v-model="searchName" placeholder="请输入项目名" style="width: 200px;" :prefix-icon="Search"
                    class="search-input"></el-input>
          <el-button type="primary" @click="searchOrder">搜索</el-button>
        </div>
      </div>
    </template>


    <router-view :key="route.fullPath"></router-view>
    <!-- 表单 -->
    <el-table v-loading="loading.value" :data="orders" style="width: 100%">
      <el-table-column prop="bookingId" label="预定ID" width="150"></el-table-column>
      <el-table-column prop="courseName" label="项目名" width="180"></el-table-column>
      <el-table-column prop="userRealName" label="用户姓名" width="180"></el-table-column>
      <el-table-column prop="coachRealName" label="员工姓名" width="180"></el-table-column>
      <el-table-column prop="paymentId" label="支付ID" width="150"></el-table-column>
      <el-table-column prop="paymentType" label="支付方式" width="150"></el-table-column>
      <el-table-column prop="paymentStatus" label="支付状态" width="150">
        <template #default="{ row }">
          {{ statusText(row.paymentStatus) }}
        </template>
      </el-table-column>

      <el-table-column prop="amount" label="金额" width="150"></el-table-column>

      <el-table-column prop="bookingDate" label="预定时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.bookingDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="paymentDate" label="支付时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.paymentDate) }}
        </template>
      </el-table-column>

      <template #empty>
        <el-empty description="没有数据"/>
      </template>
    </el-table>


    <!-- 分页 -->
    <div class="el-page">
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.currentPage"
          :page-sizes="[10, 20, 30, 40, 50]"
          :page-size="pagination.pageSize"
          :total="pagination.total"
          :background="true"
          layout="jumper, total, sizes, prev, pager, next"
          style="margin-top: 20px;">
      </el-pagination>
    </div>

    <!-- 新增订单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="新增订单"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="orderForm" :rules="orderRules" ref="orderFormRef" label-width="100px">
        <el-form-item label="选择会员" prop="userId">
          <el-select
            v-model="orderForm.userId"
            placeholder="请选择会员"
            filterable
            style="width: 100%"
            @change="handleMemberChange"
          >
            <el-option
              v-for="member in memberList"
              :key="member.userId"
              :label="getMemberDisplayLabel(member)"
              :value="member.userId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="选择项目" prop="courseId">
          <el-select
            v-model="orderForm.courseId"
            placeholder="请选择项目"
            filterable
            style="width: 100%"
            @change="handleCourseChange"
          >
            <el-option
              v-for="course in filteredCourseList"
              :key="course.courseId"
              :label="`${course.courseName} (¥${course.courseFee})`"
              :value="course.courseId"
            >
              <div>
                <span>{{ course.courseName }}</span>
              </div>
              <div style="font-size: 12px; color: #909399">
                费用: ¥{{ course.courseFee }} | 时间: {{ formatDateShort(course.scheduleStart) }}
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="选择员工" prop="coachId">
          <el-select
            v-model="orderForm.coachId"
            placeholder="请选择员工"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="coach in coachList"
              :key="coach.coachId"
              :label="coach.coachRealName"
              :value="coach.coachId"
            >
              <div>
                <span>{{ coach.coachRealName }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">{{ coach.coachPhone || '无电话' }}</span>
              </div>
              <div style="font-size: 12px; color: #909399" v-if="coach.coachRemark">
                专长: {{ coach.coachRemark }}
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="支付方式">
          <el-input
            v-model="paymentTypeDisplay"
            readonly
            placeholder="请先选择会员"
            style="width: 100%"
          >
            <template #suffix>
              <el-icon v-if="currentMember">
                <CreditCard />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-divider />

        <el-form-item label="项目费用">
          <span style="font-size: 18px; font-weight: bold; color: #f56c6c">
            ¥ {{ selectedCourseFee || '0.00' }}
          </span>
        </el-form-item>

        <el-form-item label="会员信息">
          <span :style="{ fontSize: '18px', fontWeight: 'bold', color: getMemberInfoColor() }">
            {{ getMemberInfoDisplay() }}
          </span>
        </el-form-item>

        <el-alert
          v-if="!canAffordCourse()"
          :title="getAlertTitle()"
          type="error"
          :closable="false"
          style="margin-bottom: 20px"
        >
          {{ getAlertMessage() }}
        </el-alert>

        <el-alert
          v-else-if="selectedCourseFee > 0 && orderForm.userId"
          :title="getSuccessAlertTitle()"
          type="success"
          :closable="false"
          style="margin-bottom: 20px"
        >
          {{ getSuccessAlertMessage() }}
        </el-alert>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitOrder" :loading="submitLoading">
            确定创建
          </el-button>
        </span>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import {ElMessage, ElMessageBox} from "element-plus";
import {ref, onMounted} from "vue";
import {Plus, Bell, Delete, Edit, Search, CreditCard} from "@element-plus/icons-vue";
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import {useRoute} from 'vue-router'
import {getOrderListService, searchOrderService, createOrderService} from "@/apis/order";
import {getMemberListService} from "@/apis/member";
import {getCourseListService} from "@/apis/course";
import {getCoachListService} from "@/apis/coach";

const route = useRoute()

const orders = ref([]);
const loading = ref(false); // 控制加载状态的显示

// 对话框相关
const dialogVisible = ref(false);
const submitLoading = ref(false);
const orderFormRef = ref();

const orderForm = ref({
  userId: null,
  courseId: null,
  coachId: null,
  paymentType: null
});

const orderRules = {
  userId: [{ required: true, message: '请选择会员', trigger: 'change' }],
  courseId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  coachId: [{ required: true, message: '请选择员工', trigger: 'change' }]
};

// 会员列表
const memberList = ref([]);

// 项目列表
const courseList = ref([]);

// 过滤后的项目列表
const filteredCourseList = ref([]);

// 员工列表
const coachList = ref([]);

// 选中的项目费用
const selectedCourseFee = ref(0);

// 会员余额
const memberBalance = ref(0);

// 当前选中的会员信息
const currentMember = ref(null);

// 支付方式显示
const paymentTypeDisplay = ref('');

const statusText = (statusCode) => {
  const status = {
    '0': '未支付',
    '1': '已支付',
    '2': '已退款'
  };
  return status[statusCode] || '未知';
};

// 分页数据
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
});

// 处理每页显示条数变化
const handleSizeChange = (newSize) => {
  pagination.value.pageSize = newSize;
  fetchOrders();
};

// 处理当前页变化
const handleCurrentChange = (newPage) => {
  pagination.value.currentPage = newPage;
  fetchOrders();
};

const formatDate = (timestamp) => {
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: 'numeric',
    minute: 'numeric'
  }).format(new Date(timestamp));
};

const formatDateShort = (timestamp) => {
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(timestamp));
};

// 获取订单列表，并支持分页
const fetchOrders = async () => {
  try {
    loading.value = true;
    const pageNum = pagination.value.currentPage;
    const pageSize = pagination.value.pageSize;

    //console.log(params)
    const response = await getOrderListService({pageNum, pageSize});
    orders.value = response.data.data.items;
    console.log(orders.value)
    pagination.value.total = response.data.data.total;
    //console.log(pagination.value.total)
    loading.value = false;
  } catch (error) {
    console.error('获取订单列表失败:', error);
  }
};

onMounted(fetchOrders);


// 搜索订单
const searchName = ref('');
const searchOrder = async () => {
  try {
    const courseName = searchName.value;
    const pageNum = pagination.value.currentPage;
    const pageSize = pagination.value.pageSize;
    const response = await searchOrderService({pageNum, pageSize, courseName});
    orders.value = response.data.data.items; // 使用搜索结果更新列表
  } catch (error) {
    console.error('搜索订单失败:', error);
  }
};

// 获取会员列表
const fetchMemberList = async () => {
  try {
    const response = await getMemberListService({ pageNum: 1, pageSize: 100 });
    memberList.value = response.data.data.items || [];
  } catch (error) {
    console.error('获取会员列表失败:', error);
  }
};

// 获取项目列表
const fetchCourseList = async () => {
  try {
    const response = await getCourseListService({ pageNum: 1, pageSize: 100 });
    courseList.value = response.data.data.items || [];
  } catch (error) {
    console.error('获取项目列表失败:', error);
  }
};

// 获取教练列表
const fetchCoachList = async () => {
  try {
    const response = await getCoachListService({ pageNum: 1, pageSize: 100 });
    coachList.value = response.data.data.items || [];
  } catch (error) {
    console.error('获取员工列表失败:', error);
  }
};

// 打开新增订单对话框
const handleAddOrder = () => {
  orderForm.value = {
    userId: null,
    courseId: null,
    coachId: null,
    paymentType: null
  };
  selectedCourseFee.value = 0;
  memberBalance.value = 0;
  currentMember.value = null;
  paymentTypeDisplay.value = '';
  filteredCourseList.value = [];
  dialogVisible.value = true;

  // 加载会员、项目和员工列表
  fetchMemberList();
  fetchCourseList();
  fetchCoachList();
};

// 会员变化
const handleMemberChange = (userId) => {
  const member = memberList.value.find(m => m.userId === userId);
  if (member) {
    currentMember.value = member;
    memberBalance.value = member.cardAmount || 0;
    
    // 根据会员卡类型设置支付方式显示
    if (member.memberCardType === '0') {
      paymentTypeDisplay.value = '拓客卡';
      orderForm.value.paymentType = '拓客卡';
    } else if (member.memberCardType === '1') {
      paymentTypeDisplay.value = '活动促销卡';
      orderForm.value.paymentType = '活动促销卡';
    } else if (member.memberCardType === '2') {
      paymentTypeDisplay.value = '正常成交卡';
      orderForm.value.paymentType = '正常成交卡';
    } else {
      paymentTypeDisplay.value = '未知卡类型';
      orderForm.value.paymentType = '未知卡类型';
    }
    
    // 过滤项目列表
    filterCourseList();
    
    // 清空已选择的项目
    orderForm.value.courseId = null;
    selectedCourseFee.value = 0;
  } else {
    // 如果没有选择会员，清空支付方式
    orderForm.value.paymentType = null;
    currentMember.value = null;
    paymentTypeDisplay.value = '';
  }
};

// 项目变化
const handleCourseChange = (courseId) => {
  const course = courseList.value.find(c => c.courseId === courseId);
  if (course) {
    selectedCourseFee.value = course.courseFee || 0;
  }
};

// 提交订单
const submitOrder = async () => {
  // 表单验证
  await orderFormRef.value.validate();

  // 检查是否可以消费
  if (!canAffordCourse()) {
    ElMessage.error(getAlertMessage());
    return;
  }

  try {
    submitLoading.value = true;

    await createOrderService({
      userId: orderForm.value.userId,
      courseId: orderForm.value.courseId,
      coachId: orderForm.value.coachId,
      paymentType: orderForm.value.paymentType
    });

    ElMessage.success('订单创建成功');
    dialogVisible.value = false;

    // 刷新订单列表
    fetchOrders();
  } catch (error) {
    console.error('创建订单失败:', error);
    ElMessage.error(error.response?.data?.msg || '创建订单失败');
  } finally {
    submitLoading.value = false;
  }
};

// 会员卡类型文本转换
const getCardTypeText = (cardTypeCode) => {
  const cardTypes = {
    '0': '拓客卡',
    '1': '活动促销卡',
    '2': '正常成交卡'
  };
  return cardTypes[cardTypeCode] || '未知';
};

// 获取会员显示标签
const getMemberDisplayLabel = (member) => {
  const cardTypeText = getCardTypeText(member.memberCardType);
  
  if (member.memberCardType === '0') {
    // 拓客卡显示剩余次数
    return `${member.userRealName} (${cardTypeText} - 剩余${member.remainingCount || 0}次)`;
  } else {
    // 其他卡显示余额
    return `${member.userRealName} (${cardTypeText} - ¥${member.cardAmount || 0})`;
  }
};

// 获取会员信息显示
const getMemberInfoDisplay = () => {
  if (!currentMember.value) return '请选择会员';
  
  const member = currentMember.value;
  const cardTypeText = getCardTypeText(member.memberCardType);
  
  if (member.memberCardType === '0') {
    return `${cardTypeText} - 剩余${member.remainingCount || 0}次`;
  } else {
    return `${cardTypeText} - ¥${member.cardAmount || 0}`;
  }
};

// 获取会员信息颜色
const getMemberInfoColor = () => {
  if (!currentMember.value) return '#909399';
  
  if (currentMember.value.memberCardType === '0') {
    // 拓客卡根据剩余次数显示颜色
    return (currentMember.value.remainingCount || 0) > 0 ? '#67c23a' : '#f56c6c';
  } else {
    // 其他卡根据余额是否充足显示颜色
    const discountedFee = calculateDiscountedFee();
    return (currentMember.value.cardAmount || 0) >= discountedFee ? '#67c23a' : '#f56c6c';
  }
};

// 计算折扣后的费用
const calculateDiscountedFee = () => {
  if (!currentMember.value || !selectedCourseFee.value) return selectedCourseFee.value;
  
  const cardType = currentMember.value.memberCardType;
  const originalFee = selectedCourseFee.value;
  
  if (cardType === '1') {
    // 活动促销卡八折
    return originalFee * 0.8;
  } else if (cardType === '2') {
    // 正常成交卡六折
    return originalFee * 0.6;
  }
  
  return originalFee; // 拓客卡不扣费
};

// 检查是否可以消费该项目
const canAffordCourse = () => {
  if (!currentMember.value || !selectedCourseFee.value) return false;
  
  const member = currentMember.value;
  const cardType = member.memberCardType;
  
  if (cardType === '0') {
    // 拓客卡检查剩余次数
    return (member.remainingCount || 0) > 0 && isCourseAllowedForCustomerCard();
  } else {
    // 其他卡检查余额
    const discountedFee = calculateDiscountedFee();
    return (member.cardAmount || 0) >= discountedFee;
  }
};

// 检查项目是否允许拓客卡使用
const isCourseAllowedForCustomerCard = () => {
  if (!currentMember.value || !orderForm.value.courseId) return false;
  
  const course = courseList.value.find(c => c.courseId === orderForm.value.courseId);
  if (!course) return false;
  
  // 拓客卡只能使用面部项目（courseType=1）和身体项目中的肩颈项目
  if (course.courseType === 1) {
    return true; // 面部项目
  }
  
  if (course.courseType === 0 && course.courseName && course.courseName.includes('肩颈')) {
    return true; // 身体项目中的肩颈项目
  }
  
  return false;
};

// 获取警告标题
const getAlertTitle = () => {
  if (!currentMember.value) return '请选择会员';
  
  const cardType = currentMember.value.memberCardType;
  
  if (cardType === '0') {
    return '次数不足或项目不可用';
  } else {
    return '余额不足';
  }
};

// 获取警告消息
const getAlertMessage = () => {
  if (!currentMember.value) return '请先选择会员';
  
  const member = currentMember.value;
  const cardType = member.memberCardType;
  
  if (cardType === '0') {
    const remainingCount = member.remainingCount || 0;
    if (remainingCount <= 0) {
      return `拓客卡次数已用完，当前剩余${remainingCount}次`;
    } else {
      return '该项目不支持拓客卡使用，拓客卡只能使用面部项目和肩颈项目';
    }
  } else {
    const discountedFee = calculateDiscountedFee();
    const balance = member.cardAmount || 0;
    return `会员余额不足，当前余额¥${balance}，需要¥${discountedFee.toFixed(2)}`;
  }
};

// 获取成功警告标题
const getSuccessAlertTitle = () => {
  if (!currentMember.value) return '信息完整';
  
  const cardType = currentMember.value.memberCardType;
  
  if (cardType === '0') {
    return '次数充足';
  } else {
    return '余额充足';
  }
};

// 获取成功警告消息
const getSuccessAlertMessage = () => {
  if (!currentMember.value) return '请选择会员和项目';
  
  const member = currentMember.value;
  const cardType = member.memberCardType;
  
  if (cardType === '0') {
    const remainingCount = member.remainingCount || 0;
    return `拓客卡次数充足，当前剩余${remainingCount}次，可以正常预订`;
  } else {
    const discountedFee = calculateDiscountedFee();
    const balance = member.cardAmount || 0;
    const discount = cardType === '1' ? '八折' : '六折';
    return `会员余额充足，享受${discount}优惠，实际费用¥${discountedFee.toFixed(2)}`;
  }
};

// 过滤项目列表
const filterCourseList = () => {
  if (!currentMember.value) {
    filteredCourseList.value = [...courseList.value];
    return;
  }
  
  const cardType = currentMember.value.memberCardType;
  
  if (cardType === '0') {
    // 拓客卡只显示面部项目和肩颈项目
    filteredCourseList.value = courseList.value.filter(course => {
      if (course.courseType === 1) {
        return true; // 面部项目
      }
      if (course.courseType === 0 && course.courseName && course.courseName.includes('肩颈')) {
        return true; // 身体项目中的肩颈项目
      }
      return false;
    });
  } else {
    // 其他卡显示所有项目
    filteredCourseList.value = [...courseList.value];
  }
};

</script>

<style scoped>
.page-container {
  min-height: 100%;
  box-sizing: border-box;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.el-page {
  display: flex;
  justify-content: end;
  align-items: center;
}

.search-input {
  margin-right: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
