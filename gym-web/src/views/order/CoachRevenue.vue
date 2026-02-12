<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <h2>员工收益统计</h2>
      </div>
    </template>

    <!-- 筛选区域 -->
    <div class="filter-section">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="选择员工">
          <el-select
            v-model="filterForm.coachId"
            placeholder="请选择员工"
            filterable
            clearable
            style="width: 200px"
            @change="handleCoachChange"
          >
            <el-option
              v-for="coach in coachList"
              :key="coach.coachId"
              :label="coach.coachRealName"
              :value="coach.coachId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="开始日期">
          <el-date-picker
            v-model="filterForm.startDate"
            type="date"
            placeholder="选择开始日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 180px"
          />
        </el-form-item>

        <el-form-item label="结束日期">
          <el-date-picker
            v-model="filterForm.endDate"
            type="date"
            placeholder="选择结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 180px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">
            查询
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计摘要 -->
    <div v-if="revenueList.length > 0" class="summary-section">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="summary-card">
            <div class="summary-item">
              <div class="summary-label">项目总数</div>
              <div class="summary-value">{{ revenueList.length }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="summary-card">
            <div class="summary-item">
              <div class="summary-label">总预订数</div>
              <div class="summary-value">{{ totalBookings }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="summary-card">
            <div class="summary-item">
              <div class="summary-label">总收益</div>
              <div class="summary-value highlight">¥ {{ totalRevenue.toFixed(2) }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 收益明细表格 -->
    <el-table
      v-loading="loading"
      :data="revenueList"
      style="width: 100%; margin-top: 20px"
      border
    >
      <el-table-column prop="courseId" label="项目ID" width="100" />
      <el-table-column prop="courseName" label="项目名称" width="200" />
      <el-table-column prop="courseFee" label="项目费用" width="120">
        <template #default="{ row }">
          ¥ {{ row.courseFee.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="scheduleStart" label="开始时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.scheduleStart) }}
        </template>
      </el-table-column>
      <el-table-column prop="scheduleEnd" label="结束时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.scheduleEnd) }}
        </template>
      </el-table-column>
      <el-table-column prop="bookingCount" label="预订数量" width="120" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.bookingCount > 0" type="success">
            {{ row.bookingCount }} 人
          </el-tag>
          <el-tag v-else type="info">0 人</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalRevenue" label="项目收益" width="150" align="right">
        <template #default="{ row }">
          <span class="revenue-amount">¥ {{ (row.totalRevenue || 0).toFixed(2) }}</span>
        </template>
      </el-table-column>

      <template #empty>
        <el-empty description="暂无数据" />
      </template>
    </el-table>

    <!-- 空状态提示 -->
    <el-empty v-if="!loading && revenueList.length === 0 && hasSearched" description="暂无相关数据" style="margin-top: 50px" />

  </el-card>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { getCoachRevenueService, getCoachTotalRevenueService } from '@/apis/order';
import { getCoachListService } from '@/apis/coach';

// 员工列表
const coachList = ref([]);

// 筛选表单
const filterForm = ref({
  coachId: null,
  startDate: '',
  endDate: ''
});

// 收益列表
const revenueList = ref([]);

// 加载状态
const loading = ref(false);

// 是否已搜索
const hasSearched = ref(false);

// 后端返回的总收益
const backendTotalRevenue = ref(0);

// 总预订数
const totalBookings = computed(() => {
  return revenueList.value.reduce((sum, item) => sum + (item.bookingCount || 0), 0);
});

// 总收益（使用后端返回的值）
const totalRevenue = computed(() => backendTotalRevenue.value);

// 格式化日期
const formatDate = (timestamp) => {
  if (!timestamp) return '-';
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(timestamp));
};

// 获取员工列表
const fetchCoachList = async () => {
  try {
    const response = await getCoachListService({ pageNum: 1, pageSize: 100 });
    coachList.value = response.data.data.items || [];
  } catch (error) {
    console.error('获取员工列表失败:', error);
    ElMessage.error('获取员工列表失败');
  }
};

// 查询收益统计
const handleSearch = async () => {
  if (!filterForm.value.coachId) {
    ElMessage.warning('请选择员工');
    return;
  }

  // 日期范围校验
  if (filterForm.value.startDate && filterForm.value.endDate) {
    const start = new Date(filterForm.value.startDate);
    const end = new Date(filterForm.value.endDate);
    if (start > end) {
      ElMessage.error('结束日期不能早于开始日期');
      return;
    }
  }

  try {
    loading.value = true;
    hasSearched.value = true;

    const params = {
      coachId: filterForm.value.coachId,
      startDate: filterForm.value.startDate,
      endDate: filterForm.value.endDate
    };

    // 并发请求收益明细和总收益
    const [revenueResponse, totalRevenueResponse] = await Promise.all([
      getCoachRevenueService(params),
      getCoachTotalRevenueService(params)
    ]);

    revenueList.value = revenueResponse.data.data || [];
    backendTotalRevenue.value = totalRevenueResponse.data.data || 0;

    if (revenueList.value.length > 0) {
      ElMessage.success(`查询成功，共找到 ${revenueList.value.length} 条记录`);
    } else {
      ElMessage.info('暂无相关数据');
    }
    } catch (error) {
    console.error('查询员工收益失败:', error);
    ElMessage.error('查询失败');
    } finally {
    loading.value = false;
  }
};

// 重置
const handleReset = () => {
  filterForm.value = {
    coachId: null,
    startDate: '',
    endDate: ''
  };
  revenueList.value = [];
  backendTotalRevenue.value = 0;
  hasSearched.value = false;
};

// 员工变化时清空结果
const handleCoachChange = () => {
  revenueList.value = [];
  hasSearched.value = false;
};

// 页面加载时获取员工列表
onMounted(() => {
  fetchCoachList();
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

.filter-section {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.summary-section {
  margin: 20px 0;
}

.summary-card {
  text-align: center;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.summary-item {
  padding: 20px;
}

.summary-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.summary-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.summary-value.highlight {
  color: #f56c6c;
  font-size: 28px;
}

.revenue-amount {
  color: #67c23a;
  font-weight: bold;
  font-size: 16px;
}
</style>
