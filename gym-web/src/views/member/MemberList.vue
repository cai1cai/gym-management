<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <h2>会员列表</h2>
        <div class="extra">
          <el-input v-model="searchName" placeholder="请输入会员姓名" style="width: 200px;" :prefix-icon="Search"
                    class="search-input"></el-input>
          <el-input v-model="searchPhone" placeholder="请输入手机号" style="width: 200px;" :prefix-icon="Search"
                    class="search-input"></el-input>
          <el-button type="primary" @click="searchMember">搜索</el-button>
          <el-button type="primary" @click="resetSearch">重置</el-button>
          <el-button type="primary" @click="addMember">添加会员</el-button>
        </div>
      </div>
    </template>

    <router-view :key="route.fullPath"></router-view>
    <!-- 表单 -->
    <el-table v-loading="loading.value" :data="members" style="width: 100%">
      <el-table-column prop="memberCardId" label="会员卡号" width="150"></el-table-column>
      <el-table-column prop="userId" label="用户ID" width="150"></el-table-column>
      <el-table-column prop="userRealName" label="会员姓名" width="180"></el-table-column>
      <el-table-column prop="sex" label="性别" width="150">
        <template #default="{ row }">
          {{ genderText(row.sex) }}
        </template>
      </el-table-column>
      <el-table-column prop="userPhone" label="手机号" width="180"></el-table-column>

      <el-table-column prop="memberCardType" label="会员卡类型" width="180">
        <template #default="{ row }">
          {{ cardTypeText(row.memberCardType) }}
        </template>
      </el-table-column>
      <el-table-column prop="remainingCount" label="剩余次数" width="120">
        <template #default="{ row }">
          {{ row.memberCardType === '0' ? (row.remainingCount || 0) + '次' : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="cardAmount" label="卡内金额" width="150">
        <template #default="{ row }">
          {{ (row.memberCardType === '1' || row.memberCardType === '2') ? '¥' + (row.cardAmount || 0) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.updateTime) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="info" size="mini" @click="handleDetail(row)" :icon="Document" circle plain title="消费明细"></el-button>
          <el-button type="primary" size="mini" @click="handleEdit(row)" :icon="Edit" circle plain title="编辑"></el-button>
          <el-button type="danger" size="mini" @click="handleDelete(row)" :icon="Delete" circle plain title="删除"></el-button>
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


  <!-- 编辑会员信息对话框 -->
  <el-dialog v-model="editDialogVisible" title="编辑会员信息">
    <el-form :model="editFormData">
      <el-form-item label="会员卡号" :label-width="formLabelWidth">
        <el-input v-model="editFormData.memberCardId" disabled></el-input>
      </el-form-item>
      <el-form-item label="会员姓名" :label-width="formLabelWidth">
        <el-input v-model="editFormData.userRealName" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item label="性别" :label-width="formLabelWidth">
        <el-radio-group v-model="editFormData.sex">
          <el-radio :label="0">男</el-radio>
          <el-radio :label="1">女</el-radio>
          <el-radio :label="2">保密</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="手机号" :label-width="formLabelWidth">
        <el-input v-model="editFormData.userPhone" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item label="会员卡类型" :label-width="formLabelWidth">
        <el-select v-model="editFormData.memberCardType" placeholder="请选择会员卡类型" @change="onCardTypeChange">
          <el-option label="拓客卡" value="0"></el-option>
          <el-option label="活动促销卡" value="1"></el-option>
          <el-option label="正常成交卡" value="2"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="剩余次数" :label-width="formLabelWidth" v-if="editFormData.memberCardType === '0'">
        <el-input-number v-model="editFormData.remainingCount" :min="0" :max="6" placeholder="请输入剩余次数"></el-input-number>
      </el-form-item>
      <el-form-item label="卡内金额" :label-width="formLabelWidth" v-if="editFormData.memberCardType === '1' || editFormData.memberCardType === '2'">
        <el-input v-model="editFormData.cardAmount" autocomplete="off" placeholder="请输入卡内金额"></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updateMemberInfo">确认</el-button>
      </span>
    </template>
  </el-dialog>


  <!-- 添加会员信息对话框 -->
  <el-dialog v-model="addDialogVisible" title="添加会员信息">
    <el-form :model="addFormData" :rules="rules">
      <el-form-item label="会员卡号" :label-width="formLabelWidth">
        <el-input disabled placeholder="卡号由系统自动生成"></el-input>
      </el-form-item>
      <el-form-item label="会员姓名" :label-width="formLabelWidth">
        <el-input v-model="addFormData.userRealName" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item label="性别" :label-width="formLabelWidth">
        <el-radio-group v-model="addFormData.sex">
          <el-radio :label="0">男</el-radio>
          <el-radio :label="1">女</el-radio>
          <el-radio :label="2">保密</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="手机号" :label-width="formLabelWidth">
        <el-input v-model="addFormData.userPhone" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item label="会员卡类型" :label-width="formLabelWidth">
        <el-select v-model="addFormData.memberCardType" placeholder="请选择会员卡类型" @change="onAddCardTypeChange">
          <el-option label="拓客卡" value="0"></el-option>
          <el-option label="活动促销卡" value="1"></el-option>
          <el-option label="正常成交卡" value="2"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="剩余次数" :label-width="formLabelWidth" v-if="addFormData.memberCardType === '0'">
        <el-input-number v-model="addFormData.remainingCount" :min="0" :max="6" placeholder="请输入剩余次数"></el-input-number>
      </el-form-item>
      <el-form-item label="卡内金额" :label-width="formLabelWidth" v-if="addFormData.memberCardType === '1' || addFormData.memberCardType === '2'">
        <el-input v-model="addFormData.cardAmount" autocomplete="off" placeholder="请输入卡内金额"></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addNewMember">确认</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import {
  addMemberService,
  deleteMemberService,
  editMemberService,
  getMemberListService,
  searchMemberService
} from '@/apis/member';
import {ElMessage, ElMessageBox} from "element-plus";
import {ref, onMounted} from "vue";
import {Delete, Edit, Search, Document} from "@element-plus/icons-vue";
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()


const members = ref([]); // 定义 members
const loading = ref(false); // 控制加载状态的显示

// const fetchMembers = async () => {
//   try {
//     loading.value = true;
//     const response = await getMemberListService();
//     members.value = response.data.data; // 使用响应数据更新 members
//     //members.value = []
//     loading.value = false;
//   } catch (error) {
//     console.error('获取会员列表失败:', error);
//   }
// };

// 分页数据
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
});

// 处理每页显示条数变化
const handleSizeChange = (newSize) => {
  pagination.value.pageSize = newSize;
  fetchMembers();
};

// 处理当前页变化
const handleCurrentChange = (newPage) => {
  pagination.value.currentPage = newPage;
  fetchMembers();
};

// 获取会员列表，并支持分页
const fetchMembers = async () => {
  try {
    loading.value = true;
    const pageNum = pagination.value.currentPage;
    const pageSize = pagination.value.pageSize;

    //console.log(params)
    const response = await getMemberListService({pageNum, pageSize});
    members.value = response.data.data.items;
    console.log(members.value)
    pagination.value.total = response.data.data.total;
    loading.value = false;
  } catch (error) {
    console.error('获取会员列表失败:', error);
  }
};

onMounted(fetchMembers);


const genderText = (genderCode) => {
  const genders = {
    '0': '男',
    '1': '女',
    '2': '保密'
  };
  return genders[genderCode] || '未知'; // 如果代码不是0、1或2，则返回'未知'
};

const cardTypeText = (cardTypeCode) => {
  const cardTypes = {
    '0': '拓客卡',
    '1': '活动促销卡',
    '2': '正常成交卡'
  };
  return cardTypes[cardTypeCode] || '未知';
};

const statusText = (statusCode) => {
  const status = {
    '0': '未激活',
    '1': '已激活',
    '2': '已过期'
  };
  return status[statusCode];
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

const formLabelWidth = '100px';
const formLabelHeight = '10px';
const editDialogVisible = ref(false); // 控制编辑对话框的显示
const editFormData = ref({}); // 存储编辑表单数据
const addDialogVisible = ref(false); // 控制添加对话框的显示
const addFormData = ref({}); // 存储添加表单数据

// 打开编辑对话框
const handleEdit = (members) => {
  //console.log('编辑会员信息', members);
  editFormData.value = {...members}; // 深拷贝会员信息到表单数据
  console.log(editFormData.value)
  editDialogVisible.value = true; // 显示对话框
  console.log(editDialogVisible.value)
};

// 打开添加对话框
const addMember = () => {
  console.log('添加会员');
  addDialogVisible.value = true; // 显示对话框
};


// 更新会员信息的逻辑
const updateMemberInfo = async () => {
  console.log('更新会员信息', editFormData.value);
  try {
    if (editFormData.value.userRealName === undefined || editFormData.value.userRealName === '') {
      ElMessage.warning('请输入会员姓名');
      return;
    } else if (!/^[\u4E00-\u9FA5]{2,10}/.test(editFormData.value.userRealName)) {
      ElMessage.warning('会员姓名格式不正确');
      return;
    }
    if (editFormData.value.userPhone === undefined || editFormData.value.userPhone === '') {
      ElMessage.warning('请输入手机号');
      return;
    } else if (!/^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/.test(editFormData.value.userPhone)) {
      ElMessage.warning('手机号格式不正确');
      return;
    }
    if (editFormData.value.sex === undefined) {
      ElMessage.warning('请选择性别');
      return;
    }
    if (!editFormData.value.memberCardType) {
      ElMessage.warning('请选择会员卡类型');
      return;
    }
    // 根据卡类型验证相应字段
    if (editFormData.value.memberCardType === '0') {
      if (!editFormData.value.remainingCount || editFormData.value.remainingCount <= 0) {
        ElMessage.warning('请输入拓客卡剩余次数');
        return;
      }
    } else {
      if (!editFormData.value.cardAmount || editFormData.value.cardAmount <= 0) {
        ElMessage.warning('请输入卡内金额');
        return;
      }
    }
    await editMemberService(editFormData.value);

    ElMessage.success('会员信息更新成功');
  } catch (error) {
    console.error('更新会员信息失败:', error);
    ElMessage.error('更新失败');
  }

  editDialogVisible.value = false;
  await fetchMembers(); // 重新加载会员列表
};

// 删除会员
const handleDelete = (members) => {
  console.log('删除会员操作', members.userRealName)
  ElMessageBox.confirm(`确定删除会员 ${members.userRealName} 吗?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
      .then(async () => {
        // 用户确认删除
        //console.log('删除会员', members);
        // 调用 API 删除会员...
        try {
          //console.log('删除会员', members.memberCardId)
          await deleteMemberService({memberCardId: members.memberCardId});
        } catch (error) {
          console.error('删除会员失败:', error);
          ElMessage.error('删除失败');
        }
        ElMessage.success('会员删除成功');
      })
      .catch(() => {
        // 用户取消删除
        ElMessage.info('取消删除操作');
      })
      .finally(async () => {
        // 不管用户是否取消，都会执行这里的代码
        await fetchMembers(); // 重新加载会员列表
      });
};


// 添加会员
const addNewMember = async () => {
  try {
    const memberData = {
      userRealName: addFormData.value.userRealName,
      memberCardType: addFormData.value.memberCardType,
      sex: addFormData.value.sex,
      userPhone: addFormData.value.userPhone,
      remainingCount: addFormData.value.remainingCount,
      cardAmount: addFormData.value.cardAmount
    };
    if (memberData.userRealName === undefined || memberData.userRealName === '') {
      ElMessage.warning('请输入会员姓名');
      return;
    } else if (!/^[\u4E00-\u9FA5]{2,10}/.test(memberData.userRealName)) {
      ElMessage.warning('会员姓名格式不正确');
      return;
    }
    if (memberData.userPhone === undefined || memberData.userPhone === '') {
      ElMessage.warning('请输入手机号');
      return;
    } else if (!/^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/.test(memberData.userPhone)) {
      ElMessage.warning('手机号格式不正确');
      return;
    }
    if (memberData.sex === undefined) {
      ElMessage.warning('请选择性别');
      return;
    }
    if (!memberData.memberCardType) {
      ElMessage.warning('请选择会员卡类型');
      return;
    }
    // 根据卡类型验证相应字段
    if (memberData.memberCardType === '0') {
      if (!memberData.remainingCount || memberData.remainingCount <= 0) {
        ElMessage.warning('请输入拓客卡剩余次数');
        return;
      }
    } else {
      if (!memberData.cardAmount || memberData.cardAmount <= 0) {
        ElMessage.warning('请输入卡内金额');
        return;
      }
    }
    console.log('添加会员信息', memberData);
    await addMemberService(memberData);
    ElMessage.success('会员添加成功');

    addFormData.value = {}; // 清空表单数据
    memberData.value = {};

    addDialogVisible.value = false;
    await fetchMembers(); // 重新加载会员列表
  } catch (error) {
    console.error('添加会员失败:', error);
    ElMessage.error('添加失败');
  }
};

// 会员卡类型选择变化事件（编辑表单）
const onCardTypeChange = (value) => {
  // 清空相关字段
  editFormData.value.remainingCount = null;
  editFormData.value.cardAmount = null;
  
  // 根据类型设置默认值
  if (value === '0') {
    editFormData.value.remainingCount = 6; // 拓客卡默认6次
  } else if (value === '1' || value === '2') {
    editFormData.value.cardAmount = 0; // 其他卡默认金额0
  }
};

// 会员卡类型选择变化事件（添加表单）
const onAddCardTypeChange = (value) => {
  // 清空相关字段
  addFormData.value.remainingCount = null;
  addFormData.value.cardAmount = null;
  
  // 根据类型设置默认值
  if (value === '0') {
    addFormData.value.remainingCount = 6; // 拓客卡默认6次
  } else if (value === '1' || value === '2') {
    addFormData.value.cardAmount = 0; // 其他卡默认金额0
  }
};

// 测试明细按钮
const testDetail = (row) => {
  console.log('测试明细按钮点击，会员数据：', row);
  alert(`点击了会员 ${row.userRealName} 的明细按钮`);
};

// 查看会员消费明细
const handleDetail = (row) => {
  console.log('点击明细按钮，会员信息：', row);
  // 跳转到消费记录页面，传递用户ID和姓名
  router.push({
    path: '/member-consumption',
    query: {
      userId: row.userId,
      userName: row.userRealName
    }
  });
};

// 搜索会员
const searchName = ref('');
const searchPhone = ref('');
const searchMember = async () => {
  try {
    const userRealName = searchName.value;
    const userPhone = searchPhone.value;
    const pageNum = pagination.value.currentPage;
    const pageSize = pagination.value.pageSize;
    const response = await searchMemberService({pageNum, pageSize, userRealName, userPhone});
    members.value = response.data.data.items; // 使用搜索结果更新会员列表
  } catch (error) {
    console.error('搜索会员失败:', error);
  }
};

// 重置搜索
const resetSearch = () => {
  searchName.value = '';
  searchPhone.value = '';
  fetchMembers();
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
