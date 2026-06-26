<script setup>
import { ref, onMounted } from 'vue'
import Header from '../components/layout/Header.vue'
import Sidebar from '../components/layout/Sidebar.vue'
import StudentProfile from './profile/StudentProfile.vue'
import TeacherProfile from './profile/TeacherProfile.vue'
import AdminProfile from './profile/AdminProfile.vue'

const role = ref('student')

onMounted(() => {
  const user = JSON.parse(sessionStorage.getItem('user') || '{}')
  role.value = user.role || 'student'
})
</script>

<template>
  <div class="app-container">
    <Header />
    <div class="main-body">
      <Sidebar />
      <main class="content-area fade-in">
        <StudentProfile v-if="role === 'student' || role === 'ta'" />
        <TeacherProfile v-else-if="role === 'teacher'" />
        <AdminProfile v-else-if="role === 'admin'" />
        <StudentProfile v-else />
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-container { display: flex; flex-direction: column; height: 100vh; }
.main-body { display: flex; flex: 1; overflow: hidden; }
.content-area { flex: 1; background-color: var(--bg-app); padding: 2rem 3rem; overflow-y: auto; }
</style>
