<template>
  <v-layout>
    <v-navigation-drawer v-model="drawer" :rail="rail" permanent width="264">
      <template #prepend>
        <div class="nav-header student-gradient">
          <div class="nav-logo-box">
            <v-icon color="white" size="22">mdi-pulse</v-icon>
          </div>
          <template v-if="!rail">
            <div class="nav-brand-text">
              <img src="/tcu-logo.png" alt="TCU" class="nav-tcu-logo mb-1" />
              <div class="nav-brand-name">Project Pulse</div>
              <div class="nav-brand-role">Student Portal</div>
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
        <v-list-item prepend-icon="mdi-view-dashboard" title="Dashboard"       to="/student/dashboard"       rounded="lg" active-color="primary" class="mb-1" />
        <v-list-item prepend-icon="mdi-clipboard-text" title="My WAR"          to="/student/war"             rounded="lg" active-color="primary" class="mb-1" />
        <v-list-item prepend-icon="mdi-account-group"  title="Team WAR Report" to="/student/team-war-report" rounded="lg" active-color="primary" class="mb-1" />
        <v-list-item prepend-icon="mdi-star-circle"    title="Peer Evaluation" to="/student/peer-evaluation" rounded="lg" active-color="primary" class="mb-1" />
        <v-list-item prepend-icon="mdi-chart-bar"      title="My Report"       to="/student/my-report"       rounded="lg" active-color="primary" class="mb-1" />
        <v-list-item prepend-icon="mdi-account"        title="My Account"      to="/student/account"         rounded="lg" active-color="primary" class="mb-1" />
      </v-list>

      <template #append>
        <v-divider class="mb-1" />
        <v-list density="compact" nav class="pb-3 px-2">
          <v-list-item rounded="lg">
            <template #prepend>
              <v-avatar color="info" size="32" class="mr-1">
                <span class="text-caption font-weight-bold text-white">{{ initials }}</span>
              </v-avatar>
            </template>
            <v-list-item-title class="text-body-2 font-weight-medium">{{ displayName }}</v-list-item-title>
            <v-list-item-subtitle class="text-caption">Student</v-list-item-subtitle>
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getMe } from '@/api'

const drawer = ref(true)
const rail = ref(false)
const router = useRouter()
const auth = useAuthStore()
const currentUser = ref(null)

onMounted(async () => {
  try {
    const res = await getMe()
    currentUser.value = res.data
    if (!auth.user && res.data) {
      auth.user = {
        id: res.data.id,
        email: res.data.email,
        firstName: res.data.firstName,
        lastName: res.data.lastName,
        role: res.data.role,
      }
    }
  } catch (e) {
    auth.logout()
    router.replace('/')
  }
})

const displayName = computed(() => {
  const u = currentUser.value || auth.user
  if (!u) return ''
  return `${u.firstName ?? ''} ${u.lastName ?? ''}`.trim()
})

const initials = computed(() => {
  const u = currentUser.value || auth.user
  const f = u?.firstName?.[0] ?? ''
  const l = u?.lastName?.[0] ?? ''
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

.student-gradient {
  background: linear-gradient(135deg, #7C3AED 0%, #A855F7 100%);
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
