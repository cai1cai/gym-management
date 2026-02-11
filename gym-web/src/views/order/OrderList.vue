<template>

  <el-card class="page-container">
    <template #header>
      <div class="header">
        <h2>订单列表</h2>
        <div class="extra">
          <el-button type="primary" @click="handleAddOrder">
            <el-icon><Plus /></el-icon> 新增订单
          </el-button>
          <el-input v-model="searchName" placeholder="请输入课程名" style="width: 200px;" :prefix-icon="Search"
                    class="search-input"></el-input>
          <el-button type="primary" @click="searchOrder">搜索</el-button>
        </div>
      </div>
    </template>


    <router-view :key="route.fullPath"></router-view>
    <!-- 表单 -->
    <el-table v-loading="loading.value" :data="orders" style="width: 100%">
      <el-table-column prop="bookingId" label="预定ID" width="150"></el-table-column>
      <el-table-column prop="courseName" label="课程名" width="180"></el-table-column>
      <el-table-column prop="userRealName" label="用户姓名" width="180"></el-table-column>
      <el-table-column prop="coachRealName" label="教练姓名" width="180"></el-table-column>
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
              :label="`${member.userRealName} (余额: ¥${member.memberFee})`"
              :value="member.userId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="选择课程" prop="courseId">
          <el-select
            v-model="orderForm.courseId"
            placeholder="请选择课程"
            filterable
            style="width: 100%"
            @change="handleCourseChange"
          >
            <el-option
              v-for="course in courseList"
              :key="course.courseId"
              :label="`${course.courseName} - ${course.coachRealName} (¥${course.courseFee})`"
              :value="course.courseId"
            >
              <div>
                <span>{{ course.courseName }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">{{ course.coachRealName }}</span>
              </div>
              <div style="font-size: 12px; color: #909399">
                费用: ¥{{ course.courseFee }} | 时间: {{ formatDateShort(course.scheduleStart) }}
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-divider />

        <el-form-item label="课程费用">
          <span style="font-size: 18px; font-weight: bold; color: #f56c6c">
            ¥ {{ selectedCourseFee || '0.00' }}
          </span>
        </el-form-item>

        <el-form-item label="会员余额">
          <span :style="{ fontSize: '18px', fontWeight: 'bold', color: memberBalance >= selectedCourseFee ? '#67c23a' : '#f56c6c' }">
            ¥ {{ memberBalance || '0.00' }}
          </span>
        </el-form-item>

        <el-alert
          v-if="memberBalance < selectedCourseFee"
          title="余额不足"
          type="error"
          :closable="false"
          style="margin-bottom: 20px"
        >
          会员余额不足，当前余额 ¥{{ memberBalance }}，需要 ¥{{ selectedCourseFee }}
        </el-alert>

        <el-alert
          v-else-if="selectedCourseFee > 0"
          title="余额充足"
          type="success"
          :closable="false"
          style="margin-bottom: 20px"
        >
          会员余额充足，可以正常预约
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
import {Plus, Bell, Delete, Edit, Search} from "@element-plus/icons-vue";
import {useRoute} from 'vue-router'
import {getOrderListService, searchOrderService, createOrderService} from "@/apis/order";
import {getMemberListService} from "@/apis/member";
import {getCourseListService} from "@/apis/course";

const route = useRoute()

const orders = ref([]);
const loading = ref(false); // 控制加载状态的显示

// 对话框相关
const dialogVisible = ref(false);
const submitLoading = ref(false);
const orderFormRef = ref();

const orderForm = ref({
  userId: null,
  courseId: null
});

const orderRules = {
  userId: [{ required: true, message: '请选择会员', trigger: 'change' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }]
};

// 会员列表
const memberList = ref([]);

// 课程列表
const courseList = ref([]);

// 选中的课程费用
const selectedCourseFee = ref(0);

// 会员余额
const memberBalance = ref(0);

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

// 获取课程列表
const fetchCourseList = async () => {
  try {
    const response = await getCourseListService({ pageNum: 1, pageSize: 100 });
    courseList.value = response.data.data.items || [];
  } catch (error) {
    console.error('获取课程列表失败:', error);
  }
};

// 打开新增订单对话框
const handleAddOrder = () => {
  orderForm.value = {
    userId: null,
    courseId: null
  };
  selectedCourseFee.value = 0;
  memberBalance.value = 0;
  dialogVisible.value = true;

  // 加载会员和课程列表
  fetchMemberList();
  fetchCourseList();
};

// 会员变化
const handleMemberChange = (userId) => {
  const member = memberList.value.find(m => m.userId === userId);
  if (member) {
    memberBalance.value = member.memberFee || 0;
  }
};

// 课程变化
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

  // 检查余额是否充足
  if (memberBalance.value < selectedCourseFee.value) {
    ElMessage.error('会员余额不足，请先充值');
    return;
  }

  try {
    submitLoading.value = true;

    await createOrderService({
      userId: orderForm.value.userId,
      courseId: orderForm.value.courseId
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
