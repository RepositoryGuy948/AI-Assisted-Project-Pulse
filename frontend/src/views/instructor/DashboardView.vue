<template>
  <div>
    <h1 class="text-h5 font-weight-bold mb-4" style="color:#1e1b4b">Instructor Dashboard</h1>

    <!-- Welcome banner -->
    <v-card class="dash-welcome mb-6" elevation="0">
      <v-card-text class="pa-6">
        <div class="d-flex align-center" style="gap:16px">
          <v-avatar color="rgba(255,255,255,0.2)" size="52">
            <v-icon color="white" size="26">mdi-account-tie</v-icon>
          </v-avatar>
          <div>
            <div class="text-h6 font-weight-bold text-white">
              Welcome back, {{ auth.user?.firstName }}!
            </div>
            <div class="text-body-2 mt-1" style="color:rgba(255,255,255,0.8)">
              Instructor Portal — Texas Christian University
            </div>
          </div>
        </div>
      </v-card-text>
    </v-card>

    <v-row>
      <v-col cols="12" sm="6" md="4">
        <v-card class="action-card" elevation="1" @click="$router.push('/instructor/teams')" style="cursor:pointer">
          <v-card-text class="text-center pa-8">
            <div class="action-icon-wrap bg-primary mx-auto mb-4">
              <v-icon size="28" color="white">mdi-account-group</v-icon>
            </div>
            <div class="text-h6 font-weight-bold">My Teams</div>
            <div class="text-body-2 text-medium-emphasis mt-1">View and manage your teams</div>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col cols="12" sm="6" md="4">
        <v-card class="action-card" elevation="1" @click="$router.push('/instructor/reports/peer-evaluation/section')" style="cursor:pointer">
          <v-card-text class="text-center pa-8">
            <div class="action-icon-wrap bg-secondary mx-auto mb-4">
              <v-icon size="28" color="white">mdi-star-circle</v-icon>
            </div>
            <div class="text-h6 font-weight-bold">Peer Eval Report</div>
            <div class="text-body-2 text-medium-emphasis mt-1">Section peer evaluation results</div>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col cols="12" sm="6" md="4">
        <v-card class="action-card" elevation="1" @click="$router.push('/instructor/students')" style="cursor:pointer">
          <v-card-text class="text-center pa-8">
            <div class="action-icon-wrap bg-success mx-auto mb-4">
              <v-icon size="28" color="white">mdi-school</v-icon>
            </div>
            <div class="text-h6 font-weight-bold">Students</div>
            <div class="text-body-2 text-medium-emphasis mt-1">Browse and search students</div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
const auth = useAuthStore()
</script>

<style scoped>
.action-card {
  border: 1px solid rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.action-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 28px rgba(124, 58, 237, 0.14) !important;
}

.action-icon-wrap {
  width: 60px;
  height: 60px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
