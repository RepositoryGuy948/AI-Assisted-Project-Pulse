<template>
  <v-layout>
    <v-navigation-drawer v-model="drawer" :rail="rail" permanent>
      <v-list-item prepend-icon="mdi-pulse" title="Project Pulse" nav class="py-4">
        <template #append>
          <v-btn :icon="rail ? 'mdi-chevron-right' : 'mdi-chevron-left'" variant="text" @click="rail = !rail" />
        </template>
      </v-list-item>

      <v-divider />

      <v-list density="compact" nav>
        <v-list-item prepend-icon="mdi-view-dashboard" title="Dashboard" to="/student/dashboard" />
        <v-list-item prepend-icon="mdi-clipboard-text" title="My WAR" to="/student/war" />
        <v-list-item prepend-icon="mdi-star-circle" title="Peer Evaluation" to="/student/peer-evaluation" />
        <v-list-item prepend-icon="mdi-chart-bar" title="My Report" to="/student/my-report" />
        <v-list-item prepend-icon="mdi-account" title="My Account" to="/student/account" />
      </v-list>

      <template #append>
        <v-divider />
        <v-list density="compact" nav>
          <v-list-item
            prepend-icon="mdi-account-circle"
            :title="auth.user?.firstName + ' ' + auth.user?.lastName"
            subtitle="Student"
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
