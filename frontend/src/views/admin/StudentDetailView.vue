<template>
  <div v-if="student">
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" to="/admin/students" />
      <h1 class="text-h5 font-weight-bold ml-2">
        {{ student.firstName }} {{ student.lastName }}
      </h1>
      <v-chip v-if="student.sectionName" class="ml-3" size="small">{{ student.sectionName }}</v-chip>
    </div>

    <v-row>
      <!-- Profile card -->
      <v-col cols="12" md="4">
        <v-card height="100%">
          <v-card-title>Profile</v-card-title>
          <v-card-text>
            <v-list density="compact">
              <v-list-item prepend-icon="mdi-email" title="Email" :subtitle="student.email" />
              <v-list-item prepend-icon="mdi-account-group"
                title="Team" :subtitle="student.teamName || 'Unassigned'" />
              <v-list-item prepend-icon="mdi-school"
                title="Section" :subtitle="student.sectionName || '—'" />
            </v-list>
          </v-card-text>
        </v-card>
      </v-col>

      <!-- Reports (UC-16: launching point for UC-33 and UC-34) -->
      <v-col cols="12" md="4">
        <v-card height="100%">
          <v-card-title>Reports</v-card-title>
          <v-card-actions class="pa-4 pt-2 flex-column align-start gap-2">
            <v-btn
              color="primary"
              variant="outlined"
              prepend-icon="mdi-clipboard-text"
              :to="`/instructor/reports/war/student/${student.id}`"
              block
            >
              WAR Report (UC-34)
            </v-btn>
            <v-btn
              color="secondary"
              variant="outlined"
              prepend-icon="mdi-star-circle"
              :to="`/instructor/reports/peer-evaluation/student/${student.id}`"
              block
              class="mt-2"
            >
              Peer Evaluation Report (UC-33)
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>

      <!-- Recent WARs -->
      <v-col cols="12" md="4">
        <v-card height="100%">
          <v-card-title>Recent Activity Reports</v-card-title>
          <v-card-text v-if="!wars.length" class="text-medium-emphasis">
            No activity reports submitted.
          </v-card-text>
          <v-list v-else density="compact">
            <v-list-item
              v-for="war in wars.slice(0, 5)"
              :key="war.id"
              :title="`Week of ${formatDate(war.weekStartDate)}`"
              :subtitle="`${war.activities?.length ?? 0} activit${war.activities?.length === 1 ? 'y' : 'ies'}`"
              prepend-icon="mdi-clipboard-text-outline"
            />
          </v-list>
          <v-card-actions v-if="wars.length > 5" class="pt-0">
            <v-btn
              size="small"
              variant="text"
              :to="`/instructor/reports/war/student/${student.id}`"
            >
              View full report
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </div>

  <div v-else-if="notFound" class="text-center mt-12 text-medium-emphasis">
    Student not found.
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getStudent, getStudentWARs } from '@/api'

const route = useRoute()
const student = ref(null)
const wars = ref([])
const notFound = ref(false)

onMounted(async () => {
  try {
    const [sRes, wRes] = await Promise.all([
      getStudent(route.params.id),
      getStudentWARs(route.params.id),
    ])
    student.value = sRes.data
    wars.value = wRes.data
  } catch {
    notFound.value = true
  }
})

function formatDate(d) {
  return new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}
</script>
