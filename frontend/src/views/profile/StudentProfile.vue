<script setup>
import { computed, onMounted } from 'vue'
import ProfileStats from '../../components/profile/ProfileStats.vue'
import { useProfile } from '../../composables/useProfile'

const { form, summary, loading, saving, fetchProfile, saveProfile } = useProfile()

const statItems = computed(() => [
  { label: '已选课程', value: summary.value.courseCount },
  { label: '待完成互评', value: summary.value.pendingReviews, warn: (summary.value.pendingReviews || 0) > 0 },
  { label: '已提交作业', value: summary.value.submissionCount }
])

const onSave = () => saveProfile({
  userId: form.value.userId,
  nickname: form.value.nickname,
  phone: form.value.phone,
  email: form.value.email,
  school: form.value.school,
  className: form.value.className,
  studentId: form.value.studentId
})

onMounted(fetchProfile)
</script>

<template>
  <div>
    <h2 class="page-title">个人资料</h2>
    <p v-if="loading" class="hint">加载中...</p>
    <template v-else>
      <h3 class="section-title">学习概览</h3>
      <ProfileStats :items="statItems" />

      <h3 class="section-title">基本信息</h3>
      <div class="card profile-card">
        <div class="form-group">
          <label class="form-label">昵称</label>
          <input v-model="form.nickname" class="form-input" type="text" placeholder="设置昵称" />
        </div>
        <div class="form-group">
          <label class="form-label">电话</label>
          <input v-model="form.phone" class="form-input" type="text" placeholder="输入电话号码" />
        </div>
        <div class="form-group">
          <label class="form-label">邮箱</label>
          <input v-model="form.email" class="form-input" type="email" placeholder="输入邮箱" />
        </div>
        <div class="form-group">
          <label class="form-label">学校</label>
          <input v-model="form.school" class="form-input" type="text" placeholder="输入学校名称" />
        </div>
        <div class="form-group">
          <label class="form-label">学号</label>
          <input v-model="form.studentId" class="form-input" type="text" placeholder="输入学号" />
        </div>
        <div class="form-group">
          <label class="form-label">班级</label>
          <input v-model="form.className" class="form-input" type="text" placeholder="输入班级" />
        </div>
        <div class="form-actions mt-4">
          <button class="btn btn-primary w-full" @click="onSave" :disabled="saving">
            {{ saving ? '保存中...' : '保存修改' }}
          </button>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.page-title { margin: 0 0 1.5rem 0; font-size: 1.5rem; font-weight: 700; color: var(--text-primary); }
.section-title { margin: 0 0 1rem 0; font-size: 1rem; font-weight: 600; color: var(--text-secondary); }
.profile-card { max-width: 500px; padding: 2rem; }
.hint { color: var(--text-secondary); }
.mt-4 { margin-top: 1.5rem; }
.w-full { width: 100%; }
</style>
