<template>
  <div v-if="team">
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" to="/instructor/teams" />
      <h1 class="text-h5 font-weight-bold ml-2">{{ team.name }}</h1>
    </div>

    <v-row>
      <v-col cols="12" md="6">
        <v-card>
          <v-card-title>Students</v-card-title>
          <v-list>
            <v-list-item
              v-for="s in team.students"
              :key="s.id"
              :title="`${s.firstName} ${s.lastName}`"
              :subtitle="s.email"
              :to="`/instructor/students/${s.id}`"
            />
          </v-list>
        </v-card>
      </v-col>
      <v-col cols="12" md="6">
        <v-card>
          <v-card-title>Reports</v-card-title>
          <v-card-actions class="pa-4 flex-column align-start">
            <v-btn color="primary" prepend-icon="mdi-clipboard-text" :to="`/instructor/reports/war/team/${team.id}`">
              WAR Report
            </v-btn>
            <v-btn color="secondary" prepend-icon="mdi-star-circle" class="mt-2"
              to="/instructor/reports/peer-evaluation/section">
              Peer Evaluation Report
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getTeam } from '@/api'

const route = useRoute()
const team = ref(null)

onMounted(async () => {
  const res = await getTeam(route.params.id)
  team.value = res.data
})
</script>
