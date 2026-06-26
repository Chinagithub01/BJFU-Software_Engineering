<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '../components/layout/Header.vue'
import Sidebar from '../components/layout/Sidebar.vue'
import { toastError, toastSuccess, toastWarning } from '../utils/toast'
const route = useRoute()
const router = useRouter()
const courseId = route.params.id

const userRole = ref('student')
const userId = ref(1)
const activeTab = ref('作业管理')

const assignments = ref([])
const members = ref([])

// 控制发布作业弹窗
const showAddAssignmentModal = ref(false)
const newAssignment = ref({
  title: '',
  description: '',
  dueDate: '',
  reviewDueDate: ''
})
const rubricItems = ref([
  { itemName: '', maxScore: 100, weight: 1.0, description: '' }
])

const RUBRIC_WEIGHTED_TARGET = 100
const RUBRIC_TOLERANCE = 0.1

const rubricWeightedTotal = computed(() => {
  return rubricItems.value.reduce((sum, r) => {
    const maxScore = parseFloat(r.maxScore) || 0
    const weight = parseFloat(r.weight) || 0
    return sum + maxScore * weight
  }, 0)
})

const validateRubricItems = (items) => {
  const valid = items.filter(r => r.itemName.trim())
  if (valid.length === 0) return null
  for (const r of valid) {
    if (!r.maxScore || r.maxScore <= 0) return '每项满分须大于 0'
    if (!r.weight || r.weight <= 0) return '每项权重须大于 0'
  }
  const total = valid.reduce((sum, r) => sum + (parseFloat(r.maxScore) || 0) * (parseFloat(r.weight) || 0), 0)
  if (Math.abs(total - RUBRIC_WEIGHTED_TARGET) > RUBRIC_TOLERANCE) {
    return `评分项加权满分之和须为 100，当前为 ${total.toFixed(1)}`
  }
  return null
}

// 控制上传作业弹窗
const showUploadModal = ref(false)
const currentUploadAssignmentId = ref(null)
const selectedFile = ref(null)
const uploadText = ref('')
const publishing = ref(false)
const uploading = ref(false)
  const showImportModal = ref(false)
  const importing = ref(false)
  const importUsernames = ref('')

const handleLogout = () => {
  sessionStorage.removeItem('user')
  router.push('/login')
}

// 获取作业列表
const fetchAssignments = async () => {
  try {
    const response = await fetch(`/api/assignments?courseId=${courseId}`)
    const result = await response.json()
    if (result.success) {
      assignments.value = result.data
      
      // 如果是学生，额外去查一下每个作业自己是否提交过
      if (userRole.value === 'student') {
        for (let task of assignments.value) {
          checkSubmissionStatus(task)
        }
      }
    } else {
    }
  } catch (error) {
  }
}

// 检查某个作业当前学生是否已提交
const checkSubmissionStatus = async (task) => {
  try {
    const response = await fetch(`/api/submissions?assignmentId=${task.assignmentId}&studentId=${userId.value}`)
    const result = await response.json()
    if (result.success && result.data) {
      task.mySubmission = result.data
    }
  } catch (error) {
  }
}

// 获取班级成员名单
const fetchMembers = async () => {
  try {
    const response = await fetch(`/api/enrollments?courseId=${courseId}`)
    const result = await response.json()
    if (result.success) {
      members.value = result.data
    } else {
    }
  } catch (error) {
  }
}

// 提交发布作业
const handleAddAssignment = async () => {
  if (!newAssignment.value.title) return toastWarning('请填写作业标题')
  const items = rubricItems.value.filter(r => r.itemName.trim())
  if (items.length > 0) {
    const rubricError = validateRubricItems(items)
    if (rubricError) return toastWarning(rubricError)
  }
  if (publishing.value) return
  publishing.value = true
  try {
    const response = await fetch('/api/assignments', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        courseId: parseInt(courseId),
        title: newAssignment.value.title,
        description: newAssignment.value.description,
        dueDate: newAssignment.value.dueDate || null,
        reviewDueDate: newAssignment.value.reviewDueDate || null,
        createdBy: userId.value
      })
    })
    const result = await response.json()
    if (result.success) {
      const newId = result.data ? result.data.assignmentId : null
      if (newId) {
        if (items.length > 0) {
          const rbResp = await fetch('/api/rubrics', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ assignmentId: newId, items }) })
          const rbResult = await rbResp.json()
          if (!rbResult.success) {
            toastError(rbResult.message || '量规保存失败')
            publishing.value = false
            return
          }
        }
      }
      toastSuccess('发布作业成功！')
      showAddAssignmentModal.value = false
      resetPublishForm()
      fetchAssignments()
    } else {
      toastError(result.message || '发布失败')
    }
  } catch (error) {
    toastError('请求失败，请检查服务是否运行')
  } finally {
    publishing.value = false
  }
}

const addRubricItem = () => {
  rubricItems.value.push({ itemName: '', maxScore: 100, weight: 1.0, description: '' })
}
const removeRubricItem = (idx) => {
  if (rubricItems.value.length > 1) rubricItems.value.splice(idx, 1)
}
const resetPublishForm = () => {
  newAssignment.value = { title: '', description: '', dueDate: '', reviewDueDate: '' }
  rubricItems.value = [{ itemName: '', maxScore: 100, weight: 1.0, description: '' }]
}


const handleImportStudents = async () => {
  const names = importUsernames.value.split(/[\n,]+/).map(s => s.trim()).filter(Boolean)
  if (names.length === 0) return toastWarning('请输入至少一个用户名')
  importing.value = true
  try {
    const resp = await fetch('/api/enrollments/import', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ courseId: parseInt(courseId), usernames: names })
    })
    const result = await resp.json()
    if (result.success) {
      toastSuccess(result.message || '导入完成')
      showImportModal.value = false
      importUsernames.value = ''
      fetchMembers()
    } else {
      toastError(result.message || '导入失败')
    }
  } catch (e) { toastError('请求失败') }
  finally { importing.value = false }
}
const closeAssignment = (assignmentId) => {
  pendingCloseId.value = assignmentId
  showCloseConfirmModal.value = true
}

const confirmCloseAssignment = async () => {
  if (!pendingCloseId.value || closing.value) return
  closing.value = true
  try {
    const resp = await fetch('/api/assignments', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ assignmentId: pendingCloseId.value, status: 'closed' })
    })
    const result = await resp.json()
    if (result.success) {
      toastSuccess(result.message || '作业已关闭')
      showCloseConfirmModal.value = false
      pendingCloseId.value = null
      fetchAssignments()
    } else {
      toastError(result.message || '操作失败')
    }
  } catch (e) {
    toastError('请求失败')
  } finally {
    closing.value = false
  }
}

// 教师分配互评
const showCloseConfirmModal = ref(false)
const pendingCloseId = ref(null)
const closing = ref(false)
const showAssignConfirmModal = ref(false)
const pendingAssignId = ref(null)
const assigning = ref(false)

const assignPeerReview = (assignmentId) => {
  pendingAssignId.value = assignmentId
  showAssignConfirmModal.value = true
}

const confirmAssignPeerReview = async () => {
  if (!pendingAssignId.value || assigning.value) return
  assigning.value = true
  try {
    const response = await fetch('/api/reviews/assign', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ assignmentId: pendingAssignId.value, peerReviewCount: 3 })
    })
    const result = await response.json()
    if (result.success) {
      toastSuccess(result.message || '派评完成')
      showAssignConfirmModal.value = false
      pendingAssignId.value = null
    } else {
      toastError(result.message || '派评失败')
    }
  } catch (e) {
    toastError('请求失败，请检查后端服务')
  } finally {
    assigning.value = false
  }
}

// 处理文件选择
const handleFileChange = (e) => {
  if (e.target.files.length > 0) {
    selectedFile.value = e.target.files[0]
  }
}

// 提交作业文件
const handleUploadAssignment = async () => {
  if (!selectedFile.value) return toastWarning('请选择要上传的文件')
  if (uploading.value) return
  uploading.value = true

  const formData = new FormData()
  formData.append('assignmentId', currentUploadAssignmentId.value)
  formData.append('studentId', userId.value)
  formData.append('file', selectedFile.value)
  if (uploadText.value) formData.append('contentText', uploadText.value)

  try {
    const response = await fetch('/api/submissions', {
      method: 'POST',
      body: formData
    })
    const result = await response.json()
    if (result.success) {
      const msg = result.message || '提交成功'
      if (result.data?.similarityPct >= 90) {
        toastWarning(msg)
      } else {
        toastSuccess(msg)
      }
      showUploadModal.value = false
      selectedFile.value = null
      uploadText.value = ''
      fetchAssignments()
    } else {
      toastError(result.message || '提交失败')
    }
  } catch (error) {
    toastError('上传失败，请检查网络或后端服务')
  } finally {
    uploading.value = false
  }
}

const openUploadModal = (assignmentId) => {
  currentUploadAssignmentId.value = assignmentId
  selectedFile.value = null
  showUploadModal.value = true
}

onMounted(() => {
  const userStr = sessionStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    userRole.value = user.role
    userId.value = user.userId || 1
  }
  fetchAssignments()
  fetchMembers()
})
</script>

<template>
  <div class="app-container">
    <Header @logout="handleLogout" />
    <div class="main-body">
      <Sidebar />
      <main class="content-area fade-in">
        <div class="header-action">
          <button class="btn btn-secondary btn-sm" @click="router.push('/')">← 返回</button>
          <h2 class="page-title">课程详情 (ID: {{ courseId }})</h2>
        </div>

        <!-- 顶部标签栏 -->
        <div class="tabs-container">
          <div 
            :class="['tab', { active: activeTab === '作业管理' }]" 
            @click="activeTab = '作业管理'"
          >
            作业管理
          </div>
          <div v-if="userRole === 'teacher' || userRole === 'ta'"
            :class="['tab', { active: activeTab === '班级成员' }]"
            @click="activeTab = '班级成员'"
          >
            班级成员
          </div>
        </div>

        <!-- 作业管理视图 -->
        <div v-if="activeTab === '作业管理'">
          <div class="action-bar" v-if="userRole === 'teacher' || userRole === 'ta'">
            <button class="btn btn-primary" @click="showAddAssignmentModal = true">+ 发布作业</button>
          </div>
          
          <div class="list-container">
            <div v-if="assignments.length === 0" class="empty-state">
              <div class="empty-icon">📝</div>
              <p>暂无作业发布</p>
            </div>
            <div v-for="item in assignments" :key="item.assignmentId" class="card list-card">
              <div class="card-header">
                <h3 class="assignment-title">{{ item.title }}</h3>
                <div class="header-badges">
                  <span class="badge badge-neutral">{{ item.status === 'published' ? '进行中' : '已结束' }}</span>
                  <span v-if="item.mySubmission" class="badge badge-success">已提交</span>
                </div>
              </div>
              <p class="description">{{ item.description }}</p>
              <div class="meta-info">
                <span><i class="icon">📁</i> 格式: {{ item.fileTypes }}</span>
                <span><i class="icon">⏰</i> 提交截止: {{ new Date(item.dueDate).toLocaleString() }}</span>
                <span><i class="icon">⚖️</i> 互评截止: {{ new Date(item.reviewDueDate).toLocaleString() }}</span>
              </div>
              
              <!-- 教师的操作按钮区 -->
              <div class="card-actions" v-if="userRole === 'teacher' || userRole === 'ta'">
                <button class="btn btn-secondary" @click="router.push('/submissions/' + item.assignmentId + '?courseId=' + courseId)">查看提交</button>
                <button v-if="item.status === 'published'" class="btn btn-primary" @click="assignPeerReview(item.assignmentId)">分配互评</button>
                <button v-if="item.status === 'published'" class="btn btn-danger" @click="closeAssignment(item.assignmentId)">提前结束</button>
                <button v-if="item.status === 'closed'" class="btn btn-secondary" @click="router.push('/statistics/' + item.assignmentId + '?courseId=' + courseId)">数据展示</button>
              </div>
              <!-- 学生的上传按钮区 -->
              <div class="card-actions" v-if="userRole === 'student'">
                <div v-if="item.mySubmission" class="submission-info">
                  <span class="sim-text badge badge-warning">查重率: {{ item.mySubmission.similarityPct }}%</span>
                  <span class="file-name">📄 {{ item.mySubmission.fileName }}</span>
                </div>
                <button v-else class="btn btn-primary" @click="openUploadModal(item.assignmentId)">上传作业</button>
                <button v-if="item.mySubmission" class="btn btn-secondary" style="margin-left:8px" @click="router.push('/submission-detail/' + item.mySubmission.submissionId + '?courseId=' + courseId)">查看详情</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 班级成员视图 -->
        <div v-if="activeTab === '班级成员' && (userRole === 'teacher' || userRole === 'ta')">
          <div class="action-bar" v-if="userRole === 'teacher' || userRole === 'ta'">
            <button class="btn btn-primary" @click="showImportModal = true">+ 导入学生</button>
          </div>
          <div class="list-container">
            <div v-if="members.length === 0" class="empty-state">
              <div class="empty-icon">👥</div>
              <p>当前课程暂无学生加入</p>
            </div>
            <div class="modern-table-container" v-else>
              <table class="modern-table">
                <thead>
                  <tr>
                    <th>用户ID</th>
                    <th>用户名</th>
                    <th>真实姓名</th>
                    <th>角色</th>
                    <th>加入时间</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="member in members" :key="member.userId">
                    <td>{{ member.userId }}</td>
                    <td class="font-medium">{{ member.username }}</td>
                    <td>{{ member.realName }}</td>
                    <td>
                      <span :class="['badge', member.roleInCourse === 'teacher' ? 'badge-primary' : member.roleInCourse === 'ta' ? 'badge-warning' : 'badge-success']">
                        {{ member.roleInCourse === 'teacher' ? '教师' : member.roleInCourse === 'ta' ? '助教' : '学生' }}
                      </span>
                    </td>
                    <td class="text-secondary">{{ new Date(member.enrolledAt).toLocaleDateString() }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

      </main>
    </div>
    <!-- 发布作业的弹窗 -->
    <Teleport to="body">
      <div v-if="showAddAssignmentModal" class="modal-overlay" @click.self="showAddAssignmentModal = false">
        <div class="modal-content" style="width: 750px; max-height: 90vh; overflow-y: auto;">
          <h3 class="modal-title">发布新作业</h3>
          <div class="form-group">
            <label class="form-label">作业标题</label>
            <input v-model="newAssignment.title" class="form-input" type="text" placeholder="例如：M1 实验报告" />
          </div>
          <div class="form-group">
            <label class="form-label">详细描述</label>
            <textarea v-model="newAssignment.description" class="form-input" rows="3" placeholder="请输入作业要求..."></textarea>
          </div>
          <div style="display: flex; gap: 1rem;">
            <div class="form-group" style="flex: 1;">
              <label class="form-label">提交截止</label>
              <input v-model="newAssignment.dueDate" class="form-input" type="datetime-local" />
            </div>
            <div class="form-group" style="flex: 1;">
              <label class="form-label">互评截止</label>
              <input v-model="newAssignment.reviewDueDate" class="form-input" type="datetime-local" />
            </div>
          </div>
          <div class="form-group mt-4">
            <label class="form-label">评分量规 (Rubric)</label>
            <p class="rubric-hint">设定互评时学生需要逐项打分的评分维度。各字段含义：</p>
            <div class="rubric-labels">
              <span class="rl-name">评分项名称</span>
              <span class="rl-max">满分</span>
              <span class="rl-weight">权重</span>
              <span class="rl-desc">评分要求说明</span>
            </div>
            <div v-for="(r, idx) in rubricItems" :key="idx" class="rubric-row">
              <input v-model="r.itemName" class="form-input sm-input" type="text" placeholder="如：需求完整性" style="flex:1.5" />
              <input v-model.number="r.maxScore" class="form-input sm-input score-input" type="number" min="1" max="100" placeholder="如：25" />
              <input v-model.number="r.weight" class="form-input sm-input score-input" type="number" min="0.1" max="5" step="0.1" placeholder="如：1.0" />
              <input v-model="r.description" class="form-input sm-input" type="text" placeholder="如：功能需求是否完整覆盖业务场景" style="flex:2" />
              <button class="btn btn-secondary btn-sm" @click="removeRubricItem(idx)" :disabled="rubricItems.length <= 1">×</button>
            </div>
            <button class="btn btn-text btn-sm" @click="addRubricItem" style="margin-top:0.5rem">+ 添加评分项</button>
            <p class="rubric-sum" :class="{ 'rubric-sum--invalid': Math.abs(rubricWeightedTotal - 100) > 0.1 }">
              加权满分合计：<strong>{{ rubricWeightedTotal.toFixed(1) }}</strong> / 100
              <span v-if="Math.abs(rubricWeightedTotal - 100) > 0.1">（须等于 100 方可保存）</span>
            </p>
          </div>
          <div class="modal-actions">
            <button class="btn btn-secondary" @click="showAddAssignmentModal = false">取消</button>
            <button class="btn btn-primary" @click="handleAddAssignment" :disabled="publishing">
              {{ publishing ? '发布中...' : '确认发布' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 学生上传作业的弹窗 -->
    <Teleport to="body">
      <div v-if="showUploadModal" class="modal-overlay" @click.self="showUploadModal = false">
        <div class="modal-content">
          <h3 class="modal-title">上传作业文件</h3>
          <div class="form-group">
            <label class="form-label">选择文件 (支持多格式)</label>
            <input type="file" class="form-input" style="padding: 0.4rem;" @change="handleFileChange" />
          </div>
          <div class="form-group">
            <label class="form-label">文字/代码补充</label>
            <textarea v-model="uploadText" class="form-input" rows="4" placeholder="作业的补充文字说明或代码片段..."></textarea>
          </div>
          <div class="modal-actions">
            <button class="btn btn-secondary" @click="showUploadModal = false">取消</button>
            <button class="btn btn-primary" @click="handleUploadAssignment" :disabled="uploading">
              {{ uploading ? '提交中...' : '提交并查重' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 导入学生弹窗 -->
    <Teleport to="body">
      <div v-if="showImportModal" class="modal-overlay" @click.self="showImportModal = false">
        <div class="modal-content">
          <h3 class="modal-title">批量导入学生</h3>
          <div class="form-group">
            <label class="form-label">输入用户名（每行一个或用逗号分隔）</label>
            <textarea v-model="importUsernames" class="form-input" rows="6" placeholder="stu2026001&#10;stu2026002"></textarea>
          </div>
          <div class="modal-actions">
            <button class="btn btn-secondary" @click="showImportModal = false">取消</button>
            <button class="btn btn-primary" @click="handleImportStudents" :disabled="importing">{{ importing ? '导入中...' : '确认导入' }}</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 提前结束作业确认弹窗 -->
    <Teleport to="body">
      <div v-if="showCloseConfirmModal" class="modal-overlay" @click.self="showCloseConfirmModal = false">
        <div class="modal-content confirm-modal">
          <p class="confirm-message">确定要提前结束该作业？学生将无法继续提交。</p>
          <div class="modal-actions">
            <button class="btn btn-secondary" @click="showCloseConfirmModal = false" :disabled="closing">取消</button>
            <button class="btn btn-danger" @click="confirmCloseAssignment" :disabled="closing">
              {{ closing ? '处理中...' : '确定' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 分配互评确认弹窗 -->
    <Teleport to="body">
      <div v-if="showAssignConfirmModal" class="modal-overlay" @click.self="showAssignConfirmModal = false">
        <div class="modal-content confirm-modal">
          <p class="confirm-message">确定要为此作业分配互评任务吗？系统将为每位学生随机分配同伴进行匿名互评。</p>
          <div class="modal-actions">
            <button class="btn btn-secondary" @click="showAssignConfirmModal = false" :disabled="assigning">取消</button>
            <button class="btn btn-primary" @click="confirmAssignPeerReview" :disabled="assigning">
              {{ assigning ? '分配中...' : '确定' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.app-container { display: flex; flex-direction: column; height: 100vh; }
.main-body { display: flex; flex: 1; overflow: hidden; }
.content-area { flex: 1; background-color: var(--bg-app); overflow-y: auto; padding: 2rem 3rem; }

.header-action { display: flex; align-items: center; gap: 1rem; margin-bottom: 2rem; }
.page-title { margin: 0; font-size: 1.5rem; font-weight: 700; color: var(--text-primary); }

/* Tabs 样式 */
.tabs-container {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  background-color: var(--border-light);
  padding: 0.25rem;
  border-radius: var(--radius-lg);
  width: fit-content;
}
.tab {
  padding: 0.5rem 1.25rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}
.tab:hover { color: var(--text-primary); }
.tab.active {
  background-color: var(--bg-surface);
  color: var(--primary-color);
  box-shadow: var(--shadow-sm);
  font-weight: 600;
}

.action-bar { margin-bottom: 1.5rem; display: flex; justify-content: flex-end; }

.list-container { display: flex; flex-direction: column; gap: 1rem; padding-bottom: 2rem; }

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: var(--bg-surface);
  border-radius: var(--radius-lg);
  border: 1px dashed var(--border-base);
  color: var(--text-secondary);
}
.empty-icon { font-size: 2.5rem; margin-bottom: 0.5rem; opacity: 0.6; }

/* 作业卡片 */
.list-card { padding: 1.5rem; }
.card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.75rem; }
.assignment-title { margin: 0; font-size: 1.25rem; font-weight: 600; color: var(--text-primary); }
.header-badges { display: flex; gap: 0.5rem; }
.description { color: var(--text-secondary); margin-bottom: 1.25rem; font-size: 0.875rem; line-height: 1.5; }

.meta-info { 
  display: flex; 
  flex-wrap: wrap;
  gap: 1.5rem; 
  font-size: 0.8125rem; 
  color: var(--text-secondary); 
  margin-bottom: 1.5rem;
  padding: 0.75rem;
  background-color: var(--bg-app);
  border-radius: var(--radius-md);
}

.card-actions { 
  border-top: 1px solid var(--border-light); 
  padding-top: 1.25rem; 
  display: flex; 
  justify-content: flex-end; 
  align-items: center;
  gap: 0.75rem;
}

.submission-info { 
  display: flex; 
  align-items: center;
  gap: 1rem; 
  margin-right: auto;
}
.file-name { color: var(--primary-color); font-size: 0.875rem; font-weight: 500; }

.rubric-row { display: flex; gap: 0.5rem; margin-bottom: 0.5rem; align-items: center; }
.rubric-hint { font-size: 0.8rem; color: var(--text-secondary); margin-bottom: 0.5rem; }
.rubric-sum { font-size: 0.85rem; color: var(--text-secondary); margin-top: 0.5rem; }
.rubric-sum--invalid { color: #c05621; }
.rubric-sum--invalid strong { color: #c05621; }
.rubric-labels { display: flex; gap: 0.5rem; font-size: 0.75rem; color: #999; margin-bottom: 0.25rem; padding: 0 0.25rem; }
.rl-name { flex: 1.5; } .rl-max { width: 64px; text-align: center; } .rl-weight { width: 64px; text-align: center; } .rl-desc { flex: 2; }
.sm-input { padding: 0.4rem 0.5rem; font-size: 0.8125rem; }
.score-input { width: 80px; }
.btn-sm { padding: 0.4rem 0.75rem; font-size: 0.8125rem; }
.mt-4 { margin-top: 1rem; }
.font-medium { font-weight: 500; }
.text-secondary { color: var(--text-secondary); }

.confirm-modal { width: 420px; }
.confirm-message {
  margin: 0 0 1.5rem 0;
  font-size: 0.95rem;
  line-height: 1.6;
  color: var(--text-primary);
}
</style>
