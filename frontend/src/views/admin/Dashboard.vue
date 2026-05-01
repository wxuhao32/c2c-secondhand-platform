<template>
  <div class="admin-page">
    <div class="admin-sidebar">
      <div class="sidebar-header">
        <h3>管理后台</h3>
      </div>
      <el-menu :default-active="activeTab" @select="handleTabChange">
        <el-menu-item index="dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="goods">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="orders">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="comments">
          <el-icon><ChatDotRound /></el-icon>
          <span>评论管理</span>
        </el-menu-item>
        <el-menu-item index="notify">
          <el-icon><Bell /></el-icon>
          <span>消息推送</span>
        </el-menu-item>
        <el-menu-item index="sms">
          <el-icon><Message /></el-icon>
          <span>短信接码</span>
        </el-menu-item>
      </el-menu>
    </div>

    <div class="admin-content">
      <div v-if="activeTab === 'dashboard'" class="tab-panel">
        <h2 class="panel-title">数据概览</h2>
        <el-row :gutter="20" class="stat-cards">
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-value">{{ dashboard.userCount || 0 }}</div>
                <div class="stat-label">用户总数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-value">{{ dashboard.goodsCount || 0 }}</div>
                <div class="stat-label">商品总数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-value">{{ dashboard.orderCount || 0 }}</div>
                <div class="stat-label">订单总数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-value">{{ dashboard.commentCount || 0 }}</div>
                <div class="stat-label">评论总数</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <div v-if="activeTab === 'users'" class="tab-panel">
        <h2 class="panel-title">用户管理</h2>
        <el-table :data="userList" stripe style="width: 100%">
          <el-table-column prop="userId" label="ID" width="80" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="nickname" label="昵称" width="120" />
          <el-table-column prop="mobile" label="手机号" width="130" />
          <el-table-column prop="role" label="角色" width="100">
            <template #default="{ row }">
              <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">
                {{ row.role === 'ADMIN' ? '管理员' : '用户' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="注册时间" width="170" />
          <el-table-column label="操作" min-width="200">
            <template #default="{ row }">
              <el-button
                size="small"
                :type="row.status === 1 ? 'danger' : 'success'"
                @click="toggleUserStatus(row)"
              >
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button
                size="small"
                :type="row.role === 'ADMIN' ? 'warning' : 'primary'"
                @click="toggleUserRole(row)"
              >
                {{ row.role === 'ADMIN' ? '降为用户' : '升为管理员' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="activeTab === 'goods'" class="tab-panel">
        <h2 class="panel-title">商品管理</h2>
        <el-table :data="goodsList" stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="商品名称" min-width="200" show-overflow-tooltip />
          <el-table-column prop="price" label="价格" width="100" />
          <el-table-column prop="sellerId" label="卖家ID" width="80" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="goodsStatusType(row.status)" size="small">
                {{ goodsStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="viewCount" label="浏览" width="80" />
          <el-table-column prop="likeCount" label="点赞" width="80" />
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button size="small" type="warning" @click="toggleGoodsStatus(row)">
                {{ row.status === 1 ? '下架' : '上架' }}
              </el-button>
              <el-button size="small" type="danger" @click="handleDeleteGoods(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="activeTab === 'orders'" class="tab-panel">
        <h2 class="panel-title">订单管理</h2>
        <el-table :data="orderList" stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="orderNo" label="订单号" width="180" />
          <el-table-column prop="buyerId" label="买家" width="80" />
          <el-table-column prop="sellerId" label="卖家" width="80" />
          <el-table-column prop="price" label="金额" width="100" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="orderStatusType(row.status)" size="small">
                {{ orderStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="170" />
        </el-table>
      </div>

      <div v-if="activeTab === 'comments'" class="tab-panel">
        <h2 class="panel-title">评论管理</h2>
        <el-table :data="commentList" stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="goodsId" label="商品ID" width="80" />
          <el-table-column prop="userId" label="用户ID" width="80" />
          <el-table-column prop="content" label="评论内容" min-width="250" show-overflow-tooltip />
          <el-table-column prop="likeCount" label="点赞" width="80" />
          <el-table-column prop="createdAt" label="时间" width="170" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" type="danger" @click="handleDeleteComment(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="activeTab === 'notify'" class="tab-panel">
        <h2 class="panel-title">消息推送</h2>
        <el-card>
          <el-form :model="notifyForm" label-width="100px">
            <el-form-item label="接收用户">
              <el-radio-group v-model="notifyForm.sendType">
                <el-radio value="all">全部用户</el-radio>
                <el-radio value="single">指定用户</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="notifyForm.sendType === 'single'" label="用户ID">
              <el-input v-model="notifyForm.receiverId" placeholder="输入用户ID" />
            </el-form-item>
            <el-form-item label="消息类型">
              <el-select v-model="notifyForm.type">
                <el-option label="系统通知" :value="0" />
                <el-option label="活动消息" :value="2" />
                <el-option label="订单提醒" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="消息标题">
              <el-input v-model="notifyForm.title" placeholder="输入消息标题" />
            </el-form-item>
            <el-form-item label="消息内容">
              <el-input v-model="notifyForm.content" type="textarea" :rows="4" placeholder="输入消息内容" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSendNotify" :loading="sending">发送</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <div v-if="activeTab === 'sms'" class="tab-panel">
        <h2 class="panel-title">短信接码记录 <el-tag type="info" size="small">毕设演示用</el-tag></h2>
        <p class="sms-hint">此处展示系统发送的短信验证码，用于演示和测试</p>
        <el-table :data="smsList" stripe style="width: 100%">
          <el-table-column prop="mobile" label="手机号" width="150" />
          <el-table-column prop="code" label="验证码" width="120">
            <template #default="{ row }">
              <span class="sms-code">{{ row.code }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="sendTime" label="发送时间" width="180" />
          <el-table-column prop="expireMinutes" label="有效期(分钟)" width="120" />
        </el-table>
        <el-button type="primary" style="margin-top: 16px" @click="loadSmsCodes">刷新记录</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  DataAnalysis, User, Goods, List, ChatDotRound, Bell, Message
} from '@element-plus/icons-vue'
import {
  getDashboard, getUserList, updateUserStatus, updateUserRole,
  getGoodsList, updateGoodsStatus, deleteGoods,
  getCommentList, deleteComment, getOrderList, sendNotification,
  getRecentSmsCodes
} from '@/api/admin'

const activeTab = ref('dashboard')
const dashboard = ref({})
const userList = ref([])
const goodsList = ref([])
const orderList = ref([])
const commentList = ref([])
const smsList = ref([])
const sending = ref(false)

const notifyForm = ref({
  sendType: 'all',
  receiverId: '',
  type: 0,
  title: '系统通知',
  content: ''
})

const handleTabChange = (tab) => {
  activeTab.value = tab
  loadTabData(tab)
}

const loadTabData = (tab) => {
  switch (tab) {
    case 'dashboard': loadDashboard(); break
    case 'users': loadUsers(); break
    case 'goods': loadGoods(); break
    case 'orders': loadOrders(); break
    case 'comments': loadComments(); break
    case 'sms': loadSmsCodes(); break
  }
}

const loadDashboard = async () => {
  try {
    const res = await getDashboard()
    dashboard.value = res.data || {}
  } catch (e) { console.error(e) }
}

const loadUsers = async () => {
  try {
    const res = await getUserList()
    userList.value = res.data || []
  } catch (e) { console.error(e) }
}

const loadGoods = async () => {
  try {
    const res = await getGoodsList()
    goodsList.value = res.data || []
  } catch (e) { console.error(e) }
}

const loadOrders = async () => {
  try {
    const res = await getOrderList()
    orderList.value = res.data || []
  } catch (e) { console.error(e) }
}

const loadComments = async () => {
  try {
    const res = await getCommentList()
    commentList.value = res.data || []
  } catch (e) { console.error(e) }
}

const loadSmsCodes = async () => {
  try {
    const res = await getRecentSmsCodes()
    smsList.value = res.data || []
  } catch (e) { console.error(e) }
}

const toggleUserStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定${action}用户 ${row.username}？`, '提示', { type: 'warning' })
    await updateUserStatus(row.userId, newStatus)
    ElMessage.success(`${action}成功`)
    loadUsers()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const toggleUserRole = async (row) => {
  const newRole = row.role === 'ADMIN' ? 'USER' : 'ADMIN'
  const action = newRole === 'ADMIN' ? '升级为管理员' : '降级为普通用户'
  try {
    await ElMessageBox.confirm(`确定将用户 ${row.username} ${action}？`, '提示', { type: 'warning' })
    await updateUserRole(row.userId, newRole)
    ElMessage.success('操作成功')
    loadUsers()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const toggleGoodsStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updateGoodsStatus(row.id, newStatus)
    ElMessage.success('操作成功')
    loadGoods()
  } catch (e) { console.error(e) }
}

const handleDeleteGoods = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该商品？', '提示', { type: 'warning' })
    await deleteGoods(row.id)
    ElMessage.success('删除成功')
    loadGoods()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleDeleteComment = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该评论？', '提示', { type: 'warning' })
    await deleteComment(row.id)
    ElMessage.success('删除成功')
    loadComments()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleSendNotify = async () => {
  if (!notifyForm.value.content) {
    ElMessage.warning('请输入消息内容')
    return
  }
  sending.value = true
  try {
    const data = {
      title: notifyForm.value.title,
      content: notifyForm.value.content,
      type: notifyForm.value.type
    }
    if (notifyForm.value.sendType === 'single' && notifyForm.value.receiverId) {
      data.receiverId = notifyForm.value.receiverId
    }
    await sendNotification(data)
    ElMessage.success('发送成功')
    notifyForm.value.content = ''
  } catch (e) {
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

const goodsStatusText = (s) => ['待审核', '在售', '已售', '已下架'][s] || '未知'
const goodsStatusType = (s) => ['info', 'success', 'warning', 'danger'][s] || 'info'
const orderStatusText = (s) => ['待付款', '待发货', '待收货', '已完成', '已取消'][s] || '未知'
const orderStatusType = (s) => ['warning', 'primary', 'info', 'success', 'danger'][s] || 'info'

onMounted(() => {
  loadDashboard()
})
</script>

<style lang="scss" scoped>
.admin-page {
  display: flex;
  min-height: calc(100vh - 60px);
  background: #f5f7fa;
}

.admin-sidebar {
  width: 220px;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  flex-shrink: 0;

  .sidebar-header {
    padding: 20px;
    border-bottom: 1px solid #e8e8e8;

    h3 {
      margin: 0;
      font-size: 18px;
      color: #303133;
    }
  }
}

.admin-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.panel-title {
  font-size: 20px;
  margin: 0 0 20px;
  color: #303133;
}

.stat-cards {
  .stat-item {
    text-align: center;
    padding: 20px 0;

    .stat-value {
      font-size: 36px;
      font-weight: 700;
      color: #409eff;
    }

    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-top: 8px;
    }
  }
}

.sms-hint {
  color: #909399;
  font-size: 13px;
  margin: 0 0 16px;
}

.sms-code {
  font-family: 'Courier New', monospace;
  font-size: 18px;
  font-weight: 700;
  color: #e6a23c;
  letter-spacing: 4px;
}
</style>
