<template>
  <div v-if="student">
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" to="/instructor/students" />
      <h1 class="text-h5 font-weight-bold ml-2">
        {{ student.firstName }} {{ student.lastName }}
      </h1>
      <v-chip v-if="student.sectionName" class="ml-3" size="small">{{ student.sectionName }}</v-chip>
    </div>

    <v-row>
      <!-- Profile -->
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

      <!-- Reports (UC-16 launching point for UC-33 and UC-34) -->
      <v-col cols="12" md="8">
        <v-card>
          <v-card-title>Reports</v-card-title>
          <v-card-actions class="pa-4 pt-2">
            <v-btn
              color="primary"
              prepend-icon="mdi-clipboard-text"
              :to="`/instructor/reports/war/student/${student.id}`"
            >
              WAR Report
            </v-btn>
            <v-btn
              color="secondary"
              prepend-icon="mdi-star-circle"
              class="ml-2"
              :to="`/instructor/reports/peer-evaluation/student/${student.id}`"
            >
              Peer Evaluation Report
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
import { getStudent } from '@/api'

const route = useRoute()
const student = ref(null)
const notFound = ref(false)

onMounted(async () => {
  try {
    const res = await getStudent(route.params.id)
    student.value = res.data
  } catch {
    notFound.value = true
  }
})
</script>
