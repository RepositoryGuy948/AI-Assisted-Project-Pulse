<template>
  <div>
    <h1 class="text-h5 font-weight-bold mb-6">Dashboard</h1>

    <v-row v-if="student">
      <v-col cols="12" md="4">
        <v-card color="primary" dark class="mb-4">
          <v-card-text>
            <div class="text-h6">Welcome, {{ student.firstName }}!</div>
            <div class="text-body-2 mt-1">{{ student.teamName || 'No team assigned' }}</div>
            <div class="text-body-2">{{ student.sectionName || '' }}</div>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col cols="12" md="4">
        <v-card class="mb-4" @click="$router.push('/student/war')" style="cursor:pointer">
          <v-card-text class="text-center pa-6">
            <v-icon size="48" color="primary">mdi-clipboard-text</v-icon>
            <div class="text-h6 mt-2">Submit WAR</div>
            <div class="text-body-2 text-medium-emphasis">Weekly Activity Report</div>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col cols="12" md="4">
        <v-card class="mb-4" @click="$router.push('/student/peer-evaluation')" style="cursor:pointer">
          <v-card-text class="text-center pa-6">
            <v-icon size="48" color="secondary">mdi-star-circle</v-icon>
            <div class="text-h6 mt-2">Peer Evaluation</div>
            <div class="text-body-2 text-medium-emphasis">Rate your teammates</div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getMe } from '@/api'

const auth = useAuthStore()
const student = ref(null)

onMounted(async () => {
  const res = await getMe()
  student.value = res.data
})
</script>
