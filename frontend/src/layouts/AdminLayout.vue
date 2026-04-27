<template>
  <v-layout>
    <v-navigation-drawer v-model="drawer" :rail="rail" permanent>
      <template #prepend>
        <div class="nav-header admin-gradient">
          <div class="nav-logo-box">
            <v-icon color="white" size="20">mdi-pulse</v-icon>
          </div>
          <template v-if="!rail">
            <div class="nav-brand-text">
              <div class="nav-brand-name">Project Pulse</div>
              <div class="nav-brand-role">Admin Portal</div>
            </div>
            <v-btn
              icon="mdi-chevron-left"
              variant="text"
              color="white"
              size="x-small"
              @click="rail = !rail"
            />
          </template>
          <template v-else>
            <v-btn
              icon="mdi-chevron-right"
              variant="text"
              color="rgba(255,255,255,0.8)"
              size="x-small"
              @click="rail = !rail"
            />
          </template>
        </div>
      </template>

      <v-divider />

      <v-list density="compact" nav class="mt-2 px-2">
        <v-list-item prepend-icon="mdi-view-dashboard" title="Sections"        to="/admin/sections"           rounded="lg" active-color="primary" />
        <v-list-item prepend-icon="mdi-account-group"  title="Teams"           to="/admin/teams"              rounded="lg" active-color="primary" />
        <v-list-item prepend-icon="mdi-school"         title="Students"        to="/admin/students"           rounded="lg" active-color="primary" />
        <v-list-item prepend-icon="mdi-account-tie"    title="Instructors"     to="/admin/instructors"        rounded="lg" active-color="primary" />
        <v-list-item prepend-icon="mdi-clipboard-list" title="Rubrics"         to="/admin/rubrics"            rounded="lg" active-color="primary" />
        <v-list-item prepend-icon="mdi-email-fast"     title="Invite Students"    to="/admin/invite/students"    rounded="lg" active-color="primary" />
        <v-list-item prepend-icon="mdi-email-arrow-right" title="Invite Instructors" to="/admin/invite/instructors" rounded="lg" active-color="primary" />
      </v-list>

      <template #append>
        <v-divider class="mb-1" />
        <v-list density="compact" nav class="pb-3 px-2">
          <v-list-item rounded="lg">
            <template #prepend>
              <v-avatar color="primary" size="32" class="mr-1">
                <span class="text-caption font-weight-bold text-white">{{ initials }}</span>
              </v-avatar>
            </template>
            <v-list-item-title class="text-body-2 font-weight-medium">
              {{ auth.user?.firstName }} {{ auth.user?.lastName }}
            </v-list-item-title>
            <v-list-item-subtitle class="text-caption">Admin</v-list-item-subtitle>
          </v-list-item>
          <v-list-item
            prepend-icon="mdi-logout"
            title="Logout"
            @click="logout"
            rounded="lg"
            base-color="error"
          />
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
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const drawer = ref(true)
const rail = ref(false)
const router = useRouter()
const auth = useAuthStore()

const initials = computed(() => {
  const f = auth.user?.firstName?.[0] ?? ''
  const l = auth.user?.lastName?.[0] ?? ''
  return (f + l).toUpperCase()
})

function logout() {
  auth.logout()
  router.push('/')
}
</script>

<style scoped>
.nav-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 12px;
  min-height: 68px;
}

.admin-gradient {
  background: linear-gradient(135deg, #4C1D95 0%, #7C3AED 100%);
}

.nav-logo-box {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.nav-brand-text {
  flex: 1;
  min-width: 0;
}

.nav-brand-name {
  color: white;
  font-weight: 700;
  font-size: 0.95rem;
  line-height: 1.3;
}

.nav-brand-role {
  color: rgba(255, 255, 255, 0.75);
  font-size: 0.72rem;
  line-height: 1.2;
}

:deep(.v-list-item__content .v-list-item-title) {
  color: #000000;
  font-size: 16px;
  font-weight: 500;
}
</style>
