<template>
  <div>
    <h1 class="text-h5 font-weight-bold mb-6">Dashboard</h1>

    <v-row v-if="student">
      <!-- Welcome card -->
      <v-col cols="12">
        <v-card class="welcome-card mb-2" elevation="0" rounded="xl">
          <v-card-text class="pa-6">
            <div class="d-flex align-center gap-4">
              <v-avatar color="white" size="56" class="welcome-avatar">
                <span class="text-h6 font-weight-bold" style="color: #6366F1">
                  {{ initials }}
                </span>
              </v-avatar>
              <div>
                <div class="text-h6 font-weight-bold text-white">
                  Welcome back, {{ student.firstName }}!
                </div>
                <div class="text-body-2 mt-1" style="color: rgba(255,255,255,0.8)">
                  {{ student.teamName || 'No team assigned' }}
                  <span v-if="student.sectionName"> &mdash; {{ student.sectionName }}</span>
                </div>
              </div>
            </div>
          </v-card-text>
        </v-card>
      </v-col>

      <!-- Action cards -->
      <v-col cols="12" sm="6" md="4">
        <v-card class="action-card" elevation="1" @click="$router.push('/student/war')" style="cursor:pointer">
          <v-card-text class="text-center pa-7">
            <div class="action-icon-wrap bg-primary mx-auto mb-3">
              <v-icon size="28" color="white">mdi-clipboard-text</v-icon>
            </div>
            <div class="text-h6 font-weight-bold">Submit WAR</div>
            <div class="text-body-2 text-medium-emphasis mt-1">Weekly Activity Report</div>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col cols="12" sm="6" md="4">
        <v-card class="action-card" elevation="1" @click="$router.push('/student/peer-evaluation')" style="cursor:pointer">
          <v-card-text class="text-center pa-7">
            <div class="action-icon-wrap bg-accent mx-auto mb-3">
              <v-icon size="28" color="white">mdi-star-circle</v-icon>
            </div>
            <div class="text-h6 font-weight-bold">Peer Evaluation</div>
            <div class="text-body-2 text-medium-emphasis mt-1">Rate your teammates</div>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col cols="12" sm="6" md="4">
        <v-card class="action-card" elevation="1" @click="$router.push('/student/team-war-report')" style="cursor:pointer">
          <v-card-text class="text-center pa-7">
            <div class="action-icon-wrap bg-success mx-auto mb-3">
              <v-icon size="28" color="white">mdi-account-group</v-icon>
            </div>
            <div class="text-h6 font-weight-bold">Team WAR Report</div>
            <div class="text-body-2 text-medium-emphasis mt-1">View team activity by week</div>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col cols="12" sm="6" md="4">
        <v-card class="action-card" elevation="1" @click="$router.push('/student/my-report')" style="cursor:pointer">
          <v-card-text class="text-center pa-7">
            <div class="action-icon-wrap bg-warning mx-auto mb-3">
              <v-icon size="28" color="white">mdi-chart-bar</v-icon>
            </div>
            <div class="text-h6 font-weight-bold">My Peer Eval Report</div>
            <div class="text-body-2 text-medium-emphasis mt-1">See how teammates rated you</div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getMe } from '@/api'

const student = ref(null)

onMounted(async () => {
  const res = await getMe()
  student.value = res.data
})

const initials = computed(() => {
  const f = student.value?.firstName?.[0] ?? ''
  const l = student.value?.lastName?.[0] ?? ''
  return (f + l).toUpperCase()
})
</script>

<style scoped>
.welcome-card {
  background: linear-gradient(135deg, #4C1D95 0%, #7C3AED 100%);
}

.welcome-avatar {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  flex-shrink: 0;
}

.action-card {
  border: 1px solid rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.action-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 28px rgba(99, 102, 241, 0.14) !important;
}

.action-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
