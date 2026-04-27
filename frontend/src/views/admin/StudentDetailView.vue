<template>
  <div v-if="student">
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" to="/admin/students" />
      <h1 class="text-h5 font-weight-bold ml-2">{{ student.firstName }} {{ student.lastName }}</h1>
      <v-spacer />
      <v-btn
        v-if="student.enabled"
        variant="outlined"
        color="warning"
        prepend-icon="mdi-account-off"
        class="mr-2"
        @click="deactivateDialog = true"
      >Deactivate</v-btn>
      <v-btn
        v-else
        variant="outlined"
        color="success"
        prepend-icon="mdi-account-check"
        class="mr-2"
        @click="toggleEnabled"
      >Reactivate</v-btn>
      <v-btn variant="outlined" color="error" prepend-icon="mdi-delete" @click="deleteDialog = true">
        Delete Student
      </v-btn>
    </div>

    <v-row>
      <v-col cols="12" md="4">
        <v-card>
          <v-card-title>Profile</v-card-title>
          <v-card-text>
            <v-list density="compact">
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
            <v-list v-else density="compact">
              <v-list-item
                v-for="war in wars"
                :key="war.id"
                :title="`Week of ${formatDate(war.weekStartDate)}`"
                :subtitle="`${war.activities.length} activit${war.activities.length === 1 ? 'y' : 'ies'}`"
                prepend-icon="mdi-clipboard-text"
              />
            </v-list>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Deactivate Confirmation Dialog -->
    <v-dialog v-model="deactivateDialog" max-width="420">
      <v-card>
        <v-card-title class="text-warning">Deactivate Student</v-card-title>
        <v-card-text>
          Are you sure you want to deactivate <strong>{{ student.firstName }} {{ student.lastName }}</strong>?
          They will not be able to log in until reactivated.
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="deactivateDialog = false">Cancel</v-btn>
          <v-btn color="warning" @click="confirmDeactivate">Deactivate</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="deleteDialog" max-width="450">
      <v-card>
        <v-card-title class="text-error">Delete Student</v-card-title>
        <v-card-text>
          <v-alert type="warning" density="compact" class="mb-3">
            This will permanently delete the student and all associated WARs and peer evaluations.
            This action cannot be undone.
          </v-alert>
          <p>Are you sure you want to delete <strong>{{ student.firstName }} {{ student.lastName }}</strong>?</p>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="deleteDialog = false">Cancel</v-btn>
          <v-btn color="error" :loading="deleting" @click="doDelete">Delete Permanently</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getStudent, getStudentWARs, deleteStudent, deactivateStudent, reactivateStudent } from '@/api'

const route = useRoute()
const router = useRouter()
const student = ref(null)
const wars = ref([])
const deleteDialog = ref(false)
const deleting = ref(false)
const deactivateDialog = ref(false)

onMounted(async () => {
  const [sRes, wRes] = await Promise.all([
    getStudent(route.params.id),
    getStudentWARs(route.params.id),
  ])
  student.value = sRes.data
  wars.value = wRes.data
})

async function confirmDeactivate() {
  deactivateDialog.value = false
  await deactivateStudent(route.params.id)
  const res = await getStudent(route.params.id)
  student.value = res.data
}

async function toggleEnabled() {
  await reactivateStudent(route.params.id)
  const res = await getStudent(route.params.id)
  student.value = res.data
}

async function doDelete() {
  deleting.value = true
  try {
    await deleteStudent(route.params.id)
    router.push('/admin/students')
  } finally {
    deleting.value = false
  }
}

function formatDate(d) {
  return new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}
</script>
