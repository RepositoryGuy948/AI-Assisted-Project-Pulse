<template>
  <v-layout>
    <v-navigation-drawer v-model="drawer" :rail="rail" permanent>
      <template #prepend>
        <div class="nav-header instructor-gradient">
          <div class="nav-logo-box">
            <v-icon color="white" size="20">mdi-pulse</v-icon>
          </div>
          <template v-if="!rail">
            <div class="nav-brand-text">
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

      <v-list density="compact" nav class="mt-2 px-2">
        <v-list-item prepend-icon="mdi-view-dashboard" title="Dashboard" to="/instructor/dashboard"                             rounded="lg" active-color="secondary" />
        <v-list-item prepend-icon="mdi-account-group"  title="Teams"     to="/instructor/teams"                                 rounded="lg" active-color="secondary" />
        <v-list-item prepend-icon="mdi-school"         title="Students"  to="/instructor/students"                              rounded="lg" active-color="secondary" />
        <v-list-subheader class="text-caption font-weight-medium text-uppercase mt-2 px-2">Reports</v-list-subheader>
        <v-list-item prepend-icon="mdi-star-circle"    title="Peer Eval (Section)" to="/instructor/reports/peer-evaluation/section" rounded="lg" active-color="secondary" />
        <v-list-item prepend-icon="mdi-clipboard-text" title="WAR (Team)"          to="/instructor/teams"                          rounded="lg" active-color="secondary" />
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

.instructor-gradient {
  background: linear-gradient(135deg, #6D28D9 0%, #9333EA 100%);
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
