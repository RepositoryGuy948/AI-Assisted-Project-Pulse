<template>
  <div v-if="team">
    <!-- Header -->
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" to="/instructor/teams" />
      <h1 class="text-h5 font-weight-bold ml-2">{{ team.name }}</h1>
      <v-chip class="ml-3" size="small">{{ team.sectionName }}</v-chip>
    </div>

    <!-- Team info card (UC-8) -->
    <v-card class="mb-6">
      <v-card-text>
        <v-row>
          <v-col cols="12" sm="6">
            <div class="text-caption text-medium-emphasis">Description</div>
            <div>{{ team.description || '—' }}</div>
          </v-col>
          <v-col cols="12" sm="6">
            <div class="text-caption text-medium-emphasis">Website</div>
            <a v-if="team.websiteUrl" :href="team.websiteUrl" target="_blank" rel="noopener">
              {{ team.websiteUrl }}
            </a>
            <span v-else>—</span>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <v-row>
      <!-- Students -->
      <v-col cols="12" md="6">
        <v-card>
          <v-card-title>Students ({{ team.students?.length || 0 }})</v-card-title>
          <v-list>
            <v-list-item
              v-for="s in team.students"
              :key="s.id"
              :title="`${s.firstName} ${s.lastName}`"
              :subtitle="s.email"
              :to="`/instructor/students/${s.id}`"
            />
            <v-list-item v-if="!team.students?.length">
              <v-list-item-title class="text-medium-emphasis">No students assigned.</v-list-item-title>
            </v-list-item>
          </v-list>
        </v-card>
      </v-col>

      <!-- Instructors -->
      <v-col cols="12" md="6">
        <v-card>
          <v-card-title>Instructors ({{ team.instructors?.length || 0 }})</v-card-title>
          <v-list>
            <v-list-item
              v-for="i in team.instructors"
              :key="i.id"
              :title="`${i.firstName} ${i.lastName}`"
              :subtitle="i.email"
            />
            <v-list-item v-if="!team.instructors?.length">
              <v-list-item-title class="text-medium-emphasis">No instructors assigned.</v-list-item-title>
            </v-list-item>
          </v-list>
        </v-card>
      </v-col>

      <!-- Reports -->
      <v-col cols="12">
        <v-card>
          <v-card-title>Reports</v-card-title>
          <v-card-actions class="pa-4 gap-2">
            <v-btn color="primary" prepend-icon="mdi-clipboard-text"
              :to="`/instructor/reports/war/team/${team.id}`">
              WAR Report
            </v-btn>
            <v-btn color="secondary" prepend-icon="mdi-star-circle"
              to="/instructor/reports/peer-evaluation/section">
              Peer Evaluation Report
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </div>

  <div v-else-if="notFound" class="text-center mt-12 text-medium-emphasis">
    Team not found.
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getTeam } from '@/api'

const route = useRoute()
const team = ref(null)
const notFound = ref(false)

onMounted(async () => {
  try {
    const res = await getTeam(route.params.id)
    team.value = res.data
  } catch {
    notFound.value = true
  }
})
</script>
