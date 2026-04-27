<template>
  <div v-if="student">
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" to="/admin/students" />
      <h1 class="text-h5 font-weight-bold ml-2">{{ student.firstName }} {{ student.lastName }}</h1>
      <v-spacer />
      <v-btn variant="outlined" prepend-icon="mdi-chart-bar" class="mr-2"
        :to="`/instructor/reports/peer-evaluation/student/${route.params.id}`">
        Peer Eval Report
      </v-btn>
      <v-btn variant="outlined" color="error" prepend-icon="mdi-delete" @click="deleteDialog = true">Delete Student</v-btn>
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

    <!-- Delete Student Dialog -->
    <v-dialog v-model="deleteDialog" max-width="500">
      <v-card>
        <v-card-title class="text-error">Delete Student</v-card-title>
        <v-card-text>
          <v-alert type="warning" variant="tonal" class="mb-4">
            This action is <strong>permanent and cannot be undone</strong>.
          </v-alert>
          <p class="mb-3">
            Permanently delete <strong>{{ student.firstName }} {{ student.lastName }}</strong>?
            The following will also be deleted:
          </p>
          <v-list density="compact">
            <v-list-item prepend-icon="mdi-clipboard-text">
              <strong>{{ wars.length }} Weekly Activity Report(s)</strong> and all activities within them
            </v-list-item>
            <v-list-item prepend-icon="mdi-account-group">
              All <strong>peer evaluations</strong> submitted by or about this student
            </v-list-item>
          </v-list>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="deleteDialog = false">Cancel</v-btn>
          <v-btn color="error" :loading="deleting" @click="confirmDelete">Delete Student</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" timeout="3000">
      {{ snackbar.message }}
    </v-snackbar>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getStudent, getStudentWARs, deleteStudent } from '@/api'

const route = useRoute()
const router = useRouter()
const student = ref(null)
const wars = ref([])
const deleteDialog = ref(false)
const deleting = ref(false)
const snackbar = ref({ show: false, message: '', color: 'success' })

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

async function confirmDelete() {
  deleting.value = true
  try {
    await deleteStudent(student.value.id)
    router.push('/admin/students')
  } catch {
    snackbar.value = { show: true, message: 'Failed to delete student.', color: 'error' }
    deleting.value = false
  }
}
</script>