<template>
  <v-layout>
    <v-navigation-drawer v-model="drawer" :rail="rail" permanent>
      <v-list-item
        prepend-icon="mdi-pulse"
        title="Project Pulse"
        nav
        class="py-4"
      >
        <template #append>
          <v-btn :icon="rail ? 'mdi-chevron-right' : 'mdi-chevron-left'" variant="text" @click="rail = !rail" />
        </template>
      </v-list-item>

      <v-divider />

      <v-list density="compact" nav>
        <v-list-item prepend-icon="mdi-view-dashboard" title="Sections" to="/admin/sections" />
        <v-list-item prepend-icon="mdi-account-group" title="Teams" to="/admin/teams" />
        <v-list-item prepend-icon="mdi-school" title="Students" to="/admin/students" />
        <v-list-item prepend-icon="mdi-teach" title="Instructors" to="/admin/instructors" />
        <v-list-item prepend-icon="mdi-clipboard-list" title="Rubrics" to="/admin/rubrics" />
        <v-list-item prepend-icon="mdi-email-fast" title="Invite Students" to="/admin/invite/students" />
      </v-list>

      <template #append>
        <v-divider />
        <v-list density="compact" nav>
          <v-list-item
            prepend-icon="mdi-account-circle"
            :title="auth.user?.firstName + ' ' + auth.user?.lastName"
            subtitle="Admin"
          />
          <v-list-item prepend-icon="mdi-logout" title="Logout" @click="logout" />
        </v-list>
      </template>
    </v-navigation-drawer>

    <v-main>
      <v-container fluid class="pa-6">
        <router-view />
      </v-container>
    </v-main>
  </v-layout>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const drawer = ref(true)
const rail = ref(false)
const router = useRouter()
const auth = useAuthStore()

function logout() {
  auth.logout()
  router.push('/')
}
</script>
