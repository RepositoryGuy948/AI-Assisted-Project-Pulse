<template>
  <v-layout>
    <v-navigation-drawer v-model="drawer" :rail="rail" permanent width="264">
      <template #prepend>
        <div class="nav-header instructor-gradient">
          <div class="nav-logo-box">
            <v-icon color="white" size="22">mdi-pulse</v-icon>
          </div>
          <template v-if="!rail">
            <div class="nav-brand-text">
              <img src="/tcu-logo.png" alt="TCU" class="nav-tcu-logo mb-1" />
              <div class="nav-brand-name">Project Pulse</div>
              <div class="nav-brand-role">Instructor Portal</div>
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

      <v-list density="comfortable" nav class="mt-2 px-2">
        <v-list-item prepend-icon="mdi-view-dashboard" title="Dashboard" to="/instructor/dashboard" rounded="lg" active-color="primary" class="mb-1" />
        <v-list-item prepend-icon="mdi-account-group"  title="Teams"     to="/instructor/teams"     rounded="lg" active-color="primary" class="mb-1" />
        <v-list-item prepend-icon="mdi-school"         title="Students"  to="/instructor/students"  rounded="lg" active-color="primary" class="mb-1" />
      </v-list>
      <v-divider class="mx-4 my-1" />
      <div class="nav-section-label px-5 pt-2 pb-1">Reports</div>
      <v-list density="comfortable" nav class="px-2">
        <v-list-item prepend-icon="mdi-star-circle"    title="Peer Eval — Section" to="/instructor/reports/peer-evaluation/section" rounded="lg" active-color="primary" class="mb-1" />
        <v-list-item prepend-icon="mdi-clipboard-text" title="WAR — Team"          to="/instructor/teams"                           rounded="lg" active-color="primary" class="mb-1" />
      </v-list>

      <template #append>
        <v-divider class="mb-1" />
        <v-list density="compact" nav class="pb-3 px-2">
          <v-list-item rounded="lg">
            <template #prepend>
              <v-avatar color="secondary" size="32" class="mr-1">
                <span class="text-caption font-weight-bold text-white">{{ initials }}</span>
              </v-avatar>
            </template>
            <v-list-item-title class="text-body-2 font-weight-medium">
              {{ auth.user?.firstName }} {{ auth.user?.lastName }}
            </v-list-item-title>
            <v-list-item-subtitle class="text-caption">Instructor</v-list-item-subtitle>
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
        <div class="page-content">
          <router-view />
        </div>
      </v-container>
    </v-main>
  </v-layout>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const drawer = ref(true)
const rail = ref(false)
const auth = useAuthStore()

const initials = computed(() => {
  const f = auth.user?.firstName?.[0] ?? ''
  const l = auth.user?.lastName?.[0] ?? ''
  return (f + l).toUpperCase()
})

function logout() {
  auth.logout()
  window.location.href = '/'
}
</script>

<style scoped>
.nav-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 18px 16px;
  min-height: 76px;
}

.instructor-gradient {
  background: linear-gradient(135deg, #6D28D9 0%, #9333EA 100%);
}

.nav-logo-box {
  width: 40px;
  height: 40px;
  border-radius: 12px;
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
  font-size: 1rem;
  line-height: 1.3;
}

.nav-brand-role {
  color: rgba(255, 255, 255, 0.72);
  font-size: 0.75rem;
  line-height: 1.3;
}

.nav-section-label {
  font-size: 0.68rem;
  font-weight: 700;
  letter-spacing: 0.09em;
  text-transform: uppercase;
  color: #9CA3AF;
}

:deep(.v-list-item__content .v-list-item-title) {
  color: #1e1b4b;
  font-size: 0.9rem;
  font-weight: 500;
  letter-spacing: 0.01em;
}

:deep(.v-list-item__prepend .v-icon) {
  opacity: 0.7;
}

:deep(.v-list-item--active .v-list-item__prepend .v-icon) {
  opacity: 1;
}

:deep(.v-list-item-subtitle) {
  font-size: 0.72rem;
  opacity: 0.75;
}

.nav-tcu-logo {
  height: 14px;
  width: auto;
  display: block;
  filter: brightness(0) invert(1);
  opacity: 0.85;
}
</style>
