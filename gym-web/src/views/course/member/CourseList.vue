<template>

  <el-card class="page-container">
    <template #header>
      <div class="header">
        <h2>项目列表</h2>
        <div class="extra">
          <el-input v-model="searchName" placeholder="请输入项目名" style="width: 200px;" :prefix-icon="Search"
                    class="search-input"></el-input>
          <el-button type="primary" @click="searchCourse">搜索</el-button>
        </div>
      </div>
    </template>


    <router-view :key="route.fullPath"></router-view>
    <!-- 表单 -->
    <el-table v-loading="loading.value" :data="courses" style="width: 100%">
      <el-table-column prop="courseId" label="项目ID" width="150"></el-table-column>
      <el-table-column prop="courseName" label="项目名" width="180"></el-table-column>
      <el-table-column prop="coachId" label="员工ID" width="150"></el-table-column>
      <el-table-column prop="coachRealName" label="员工姓名" width="180"></el-table-column>
      <el-table-column prop="courseFee" label="金额/每次" width="180"></el-table-column>

      <el-table-column prop="scheduleStart" label="项目开始时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.scheduleStart) }}
        </template>
      </el-table-column>
      <el-table-column prop="scheduleEnd" label="项目结束时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.scheduleEnd) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.isEnrolledByOther === '1'" type="info" size="mini" disabled>已被预订</el-button>
          <el-button v-else-if="row.isEnrolledByCurrentUser === '1'" type="warning" size="mini" @click="handleRefund(row)">退订</el-button>
          <el-button v-else type="primary" size="mini" @click="handleEnroll(row)">预订</el-button>
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

  </el-card>

</template>

<script setup>
import {ElMessage, ElMessageBox} from "element-plus";
import {ref, onMounted} from "vue";
import {Bell, Delete, Edit, Search} from "@element-plus/icons-vue";
import {useRoute} from 'vue-router'
import {
  addCourseService,
  deleteCourseService,
  editCourseService, getCoachListService,
  getCourseListService, getMemberCourseListService, refundCourseService,
  searchCourseService
} from "@/apis/course";
import router from "@/router";

const route = useRoute()

const courses = ref([]); // 定义
const loading = ref(false); // 控制加载状态的显示


// 分页数据
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
});

// 处理每页显示条数变化
const handleSizeChange = (newSize) => {
  pagination.value.pageSize = newSize;
  fetchCourses();
};

// 处理当前页变化
const handleCurrentChange = (newPage) => {
  pagination.value.currentPage = newPage;
  fetchCourses();
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

// 获取项目列表，并支持分页
const fetchCourses = async () => {
  try {
    loading.value = true;
    const pageNum = pagination.value.currentPage;
    const pageSize = pagination.value.pageSize;

    //console.log(params)
    const response = await getMemberCourseListService({pageNum, pageSize});
    courses.value = response.data.data.items;
    console.log(courses.value)
    pagination.value.total = response.data.data.total;
    //console.log(pagination.value.total)
    loading.value = false;
  } catch (error) {
    console.error('获取项目列表失败:', error);
  }
};

onMounted(fetchCourses);


// 搜索项目
const searchName = ref('');
const searchCourse = async () => {
  try {
    const courseName = searchName.value;
    const pageNum = pagination.value.currentPage;
    const pageSize = pagination.value.pageSize;
    const response = await searchCourseService({pageNum, pageSize, courseName});
    courses.value = response.data.data.items; // 使用搜索结果更新列表
  } catch (error) {
    console.error('搜索项目失败:', error);
  }
};

// 获取用户余额
import {userGetBalanceService} from '@/apis/user';

// checkUserBalance 方法用于检查用户余额是否足够
const checkUserBalance = async (courseFee) => {
  try {
    const response = await userGetBalanceService(); // 调用API获取用户余额
    const userBalance = response.data.data; // 假设返回数据中包含余额信息

    console.log('userBalance', userBalance)

    return userBalance >= courseFee; // 比较余额和项目费用
  } catch (error) {
    console.error('获取用户余额失败:', error);
    return false; // 出现错误时默认返回不足够
  }
};

import { payCourseFeeService } from '@/apis/course';
// payForCourse 方法用于调用支付接口
const payForCourse = async (courseId) => {
  try {
    console.log(courseId)
    //处理支付逻辑
    const response = await payCourseFeeService(courseId);
    return response.data; // 返回支付结果
  } catch (error) {
    console.error('支付项目费用失败:', error);
    throw new Error('支付失败'); // 抛出错误，可在调用时进行处理
  }
};

// import { enrollInCourseService } from '@/apis/course';
// // enrollCourseService 方法用于调用预订接口
// const enrollCourseService = async (courseId) => {
//   try {
//     //处理项目预订
//     const response = await enrollInCourseService(courseId);
//     return response.data; // 返回预订结果
//   } catch (error) {
//     console.error('预订项目失败:', error);
//     throw new Error('预订失败'); // 抛出错误，可在调用时进行处理
//   }
// };


// 预订
const handleEnroll = async (courses) => {
  try {
    //console.log(courses)

    // 检查项目能否预订
    if (courses.isEnrolledByOther === '1') {
      ElMessage.error('该项目已被其他会员预订');
      return;
    }

    if (courses.scheduleStart < Date.now()) {
      ElMessage.error('该项目已开始，无法预订');
      return;
    }

    // 检查余额逻辑（假设有一个方法 checkUserBalance 返回余额是否足够）
    const balanceEnough = await checkUserBalance(courses.courseFee);

    //console.log(courses.courseFee)

    if (!balanceEnough) {
      ElMessage.warning('余额不足，请先充值');
      await router.push('/recharge'); // 跳转到充值页面
      return;
    }

    // 调用支付接口，然后预订
    await payForCourse(courses.courseId);

    console.log(courses.courseId)
    //await enrollCourseService(courses.courseId);
    ElMessage.success('预订成功！');
    //console.log(courses)
  } catch (error) {
    console.error('预订失败:', error);
    ElMessage.error('预订失败');
  } finally {
    // 无论预订成功还是失败，都要重新获取项目列表
    await fetchCourses();
  }
};

const canRefund = (scheduleStart) => {
  const now = Date.now();
  return now < scheduleStart;
};

// 退订
const handleRefund = async (courses) => {
  try {
    // 检查是否可以退订（比如，检查当前时间是否在项目开始时间之前）
    if (!canRefund(courses.scheduleStart)) {
      ElMessage.warning('项目已开始，无法退订');
      return;
    }

    await ElMessageBox.confirm('确定要退订吗？', '退订确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });

    //调用退款接口，然后退订
    //await refundForCourse(course.courseId);
    await refundCourseService(courses.courseId);
    ElMessage.success('退订成功！');
    courses.isEnrolledByCurrrentUser = '0';
    await fetchCourses();
  } catch (error) {
    // 用户点击了取消或退订过程中出现错误
    if (error !== 'cancel') {
      console.error('退订失败:', error);
      ElMessage.error('退订失败');
    }
  } finally {
    // 无论退订成功还是失败，都要重新获取项目列表
    await fetchCourses();
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

</style>
