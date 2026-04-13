<template>
  <div v-if="student">
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" to="/instructor/students" />
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
          <v-card-title>Reports</v-card-title>
          <v-card-actions class="pa-4 flex-column align-start">
            <v-btn color="primary" prepend-icon="mdi-clipboard-text" :to="`/instructor/reports/war/student/${student.id}`">
              WAR Report
            </v-btn>
            <v-btn color="secondary" prepend-icon="mdi-star-circle" class="mt-2"
              :to="`/instructor/reports/peer-evaluation/student/${student.id}`">
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
import { getStudent } from '@/api'

const route = useRoute()
const student = ref(null)

onMounted(async () => {
  const res = await getStudent(route.params.id)
  student.value = res.data
})
</script>
