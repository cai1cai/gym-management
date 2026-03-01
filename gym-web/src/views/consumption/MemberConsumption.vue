<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <h2>会员消费记录 - {{ userName }}</h2>
        <div class="extra">
          <el-input v-model="searchName" placeholder="请输入项目名称" style="width: 200px;" :prefix-icon="Search"
                    class="search-input"></el-input>
          <el-button type="primary" @click="searchConsumption">搜索</el-button>
          <el-button type="primary" @click="resetSearch">重置</el-button>
          <el-button @click="goBack">返回会员列表</el-button>
        </div>
      </div>
    </template>

    <!-- 表单 -->
    <el-table v-loading="loading.value" :data="consumptions" style="width: 100%">
      <el-table-column prop="bookingId" label="订单号" width="120"></el-table-column>
      <el-table-column prop="courseName" label="项目名称" width="180"></el-table-column>
      <el-table-column prop="coachRealName" label="员工姓名" width="150"></el-table-column>
      <el-table-column prop="amount" label="消费金额" width="120">
        <template #default="{ row }">
          ¥{{ row.amount || '0.00' }}
        </template>
      </el-table-column>
      <el-table-column prop="paymentType" label="支付方式" width="120">
        <template #default="{ row }">
          <el-tag :type="getPaymentTypeColor(row.paymentType)">
            {{ row.paymentType }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="paymentStatus" label="支付状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusColor(row.paymentStatus)">
            {{ getStatusText(row.paymentStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="paymentDate" label="消费时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.paymentDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="scheduleStart" label="预约时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.scheduleStart) }}
        </template>
      </el-table-column>

      <template #empty>
        <el-empty description="没有消费记录"/>
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
  </el-card>
</template>

<script setup>
import {ElMessage} from "element-plus";
import {ref, onMounted} from "vue";
import {Search} from "@element-plus/icons-vue";
import {useRoute, useRouter} from 'vue-router'
import {getMemberConsumptionService, searchMemberConsumptionService} from '@/apis/consumption';

const route = useRoute()
const router = useRouter()

// 从路由参数获取用户信息
const userId = route.query.userId;
const userName = route.query.userName || '未知用户';

console.log('消费记录页面接收到的参数：', { userId, userName });

const consumptions = ref([]);
const loading = ref(false);
const searchName = ref('');

// 分页数据
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
});

// 获取消费记录
const fetchConsumptions = async () => {
  try {
    loading.value = true;
    const pageNum = pagination.value.currentPage;
    const pageSize = pagination.value.pageSize;
    const response = await getMemberConsumptionService({
      userId,
      pageNum,
      pageSize
    });
    consumptions.value = response.data.data.items || [];
    pagination.value.total = response.data.data.total || 0;
  } catch (error) {
    console.error('获取消费记录失败:', error);
    ElMessage.error('获取消费记录失败');
  } finally {
    loading.value = false;
  }
};

// 搜索消费记录
const searchConsumption = async () => {
  try {
    loading.value = true;
    const pageNum = pagination.value.currentPage;
    const pageSize = pagination.value.pageSize;
    const courseName = searchName.value;
    const response = await searchMemberConsumptionService({
      userId,
      courseName,
      pageNum,
      pageSize
    });
    consumptions.value = response.data.data.items || [];
    pagination.value.total = response.data.data.total || 0;
  } catch (error) {
    console.error('搜索消费记录失败:', error);
    ElMessage.error('搜索消费记录失败');
  } finally {
    loading.value = false;
  }
};

// 返回会员列表
const goBack = () => {
  router.push('/member/list');
};

// 重置搜索
const resetSearch = () => {
  searchName.value = '';
  fetchConsumptions();
};

// 分页大小变化
const handleSizeChange = (val) => {
  pagination.value.pageSize = val;
  pagination.value.currentPage = 1;
  fetchConsumptions();
};

// 当前页变化
const handleCurrentChange = (val) => {
  pagination.value.currentPage = val;
  fetchConsumptions();
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN');
};

// 获取支付方式颜色
const getPaymentTypeColor = (paymentType) => {
  const colorMap = {
    '拓客卡': 'success',
    '活动促销卡': 'warning',
    '正常成交卡': 'primary',
    '管理员代预订': 'info',
    '现金支付': 'success',
    '微信支付': 'success',
    '支付宝支付': 'success'
  };
  return colorMap[paymentType] || 'info';
};

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    '0': 'danger',    // 未支付
    '1': 'success',   // 已支付
    '2': 'warning'    // 已退款
  };
  return colorMap[status] || 'info';
};

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    '0': '未支付',
    '1': '已支付',
    '2': '已退款'
  };
  return statusMap[status] || '未知';
};

// 页面加载时获取数据
onMounted(() => {
  fetchConsumptions();
});
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

.header h2 {
  margin: 0;
  color: #409eff;
}

.extra {
  display: flex;
  gap: 10px;
  align-items: center;
}

.search-input {
  margin-right: 10px;
}

.el-page {
  display: flex;
  justify-content: center;
}
</style>
