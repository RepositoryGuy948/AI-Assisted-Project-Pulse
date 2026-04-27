<template>
  <div>
    <div class="d-flex align-center mb-6">
      <h1 class="text-h5 font-weight-bold">Students</h1>
    </div>

    <v-row class="mb-4">
      <v-col cols="3"><v-text-field v-model="filters.firstName" label="First Name" variant="outlined" density="compact" clearable @update:model-value="fetchStudents" /></v-col>
      <v-col cols="3"><v-text-field v-model="filters.lastName" label="Last Name" variant="outlined" density="compact" clearable @update:model-value="fetchStudents" /></v-col>
      <v-col cols="3"><v-text-field v-model="filters.email" label="Email" variant="outlined" density="compact" clearable @update:model-value="fetchStudents" /></v-col>
    </v-row>

    <v-card>
      <v-data-table :headers="headers" :items="students" :loading="loading" item-value="id">
        <template #item.actions="{ item }">
          <v-btn icon="mdi-eye" variant="text" size="small" :to="`/admin/students/${item.id}`" />
          <v-btn icon="mdi-delete" variant="text" size="small" color="error" @click="promptDelete(item)" />
        </template>
      </v-data-table>
    </v-card>

    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="deleteDialog" max-width="420">
      <v-card>
        <v-card-title class="text-error">Delete Student</v-card-title>
        <v-card-text v-if="studentToDelete">
          <p>Permanently delete <strong>{{ studentToDelete.firstName }} {{ studentToDelete.lastName }}</strong>?</p>
          <p class="mt-2 text-caption text-medium-emphasis">This will also delete their WARs and peer evaluations. This cannot be undone.</p>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="deleteDialog = false">Cancel</v-btn>
          <v-btn color="error" :loading="deleting" @click="confirmDelete">Delete</v-btn>
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
import { getStudents, deleteStudent } from '@/api'

const students = ref([])
const loading = ref(false)
const filters = ref({ firstName: '', lastName: '', email: '' })
const deleteDialog = ref(false)
const studentToDelete = ref(null)
const deleting = ref(false)
const snackbar = ref({ show: false, message: '', color: 'success' })

const headers = [
  { title: 'First Name', key: 'firstName' },
  { title: 'Last Name', key: 'lastName' },
  { title: 'Email', key: 'email' },
  { title: 'Team', key: 'teamName' },
  { title: 'Section', key: 'sectionName' },
  { title: 'Actions', key: 'actions', sortable: false },
]

onMounted(fetchStudents)

async function fetchStudents() {
  loading.value = true
  try {
    const res = await getStudents({
      firstName: filters.value.firstName || undefined,
      lastName: filters.value.lastName || undefined,
      email: filters.value.email || undefined,
    })
    students.value = res.data.sort((a, b) => a.lastName.localeCompare(b.lastName))
  } finally {
    loading.value = false
  }
}

function promptDelete(student) {
  studentToDelete.value = student
  deleteDialog.value = true
}

async function confirmDelete() {
  deleting.value = true
  try {
    await deleteStudent(studentToDelete.value.id)
    deleteDialog.value = false
    await fetchStudents()
  } catch {
    snackbar.value = { show: true, message: 'Failed to delete student.', color: 'error' }
  } finally {
    deleting.value = false
  }
}
</script>