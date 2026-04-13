<template>
  <div v-if="student">
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" to="/admin/students" />
      <h1 class="text-h5 font-weight-bold ml-2">{{ student.firstName }} {{ student.lastName }}</h1>
    </div>

    <v-row>
      <v-col cols="12" md="4">
        <v-card>
          <v-card-title>Profile</v-card-title>
          <v-card-text>
            <v-list>
              <v-list-item title="Email" :subtitle="student.email" />
              <v-list-item title="Team" :subtitle="student.teamName || 'Unassigned'" />
              <v-list-item title="Section" :subtitle="student.sectionName || 'N/A'" />
            </v-list>
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="12" md="8">
        <v-card>
          <v-card-title>Recent WARs</v-card-title>
          <v-card-text>
            <p class="text-medium-emphasis" v-if="!wars.length">No activity reports found.</p>
            <v-list v-else>
              <v-list-item
                v-for="war in wars"
                :key="war.id"
                :title="`Week of ${formatDate(war.weekStartDate)}`"
                :subtitle="`${war.activities.length} activities`"
                prepend-icon="mdi-clipboard-text"
              />
            </v-list>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getStudent, getStudentWARs } from '@/api'

const route = useRoute()
const student = ref(null)
const wars = ref([])

onMounted(async () => {
  const [sRes, wRes] = await Promise.all([
    getStudent(route.params.id),
    getStudentWARs(route.params.id),
  ])
  student.value = sRes.data
  wars.value = wRes.data
})

function formatDate(d) {
  return new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}
</script>
