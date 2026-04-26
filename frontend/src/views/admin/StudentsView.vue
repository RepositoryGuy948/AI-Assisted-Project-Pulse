<template>
  <div>
    <h1 class="text-h5 font-weight-bold mb-6">Students</h1>

    <!-- Search filters (UC-15) -->
    <v-row class="mb-4">
      <v-col cols="6" sm="4" md="2">
        <v-text-field v-model="filters.firstName" label="First Name" variant="outlined"
          density="compact" clearable @update:model-value="fetchStudents" />
      </v-col>
      <v-col cols="6" sm="4" md="2">
        <v-text-field v-model="filters.lastName" label="Last Name" variant="outlined"
          density="compact" clearable @update:model-value="fetchStudents" />
      </v-col>
      <v-col cols="12" sm="4" md="3">
        <v-text-field v-model="filters.email" label="Email" variant="outlined"
          density="compact" clearable @update:model-value="fetchStudents" />
      </v-col>
      <v-col cols="6" sm="4" md="2">
        <v-text-field v-model="filters.teamName" label="Team" variant="outlined"
          density="compact" clearable @update:model-value="fetchStudents" />
      </v-col>
      <v-col cols="6" sm="4" md="3">
        <v-text-field v-model="filters.sectionName" label="Section" variant="outlined"
          density="compact" clearable @update:model-value="fetchStudents" />
      </v-col>
    </v-row>

    <v-card>
      <v-data-table
        :headers="headers"
        :items="students"
        :loading="loading"
        item-value="id"
        no-data-text="No students found."
      >
        <template #item.actions="{ item }">
          <v-btn icon="mdi-eye" variant="text" size="small" :to="`/admin/students/${item.id}`" />
          <v-btn icon="mdi-delete" variant="text" size="small" color="error"
            @click="confirmDelete(item)" />
        </template>
      </v-data-table>
    </v-card>

    <!-- Delete confirmation dialog -->
    <v-dialog v-model="deleteDialog" max-width="420">
      <v-card>
        <v-card-title>Delete Student</v-card-title>
        <v-card-text>
          Delete <strong>{{ deletingStudent?.firstName }} {{ deletingStudent?.lastName }}</strong>?
          This cannot be undone.
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="deleteDialog = false">Cancel</v-btn>
          <v-btn color="error" :loading="deleting" @click="doDelete">Delete</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="4000" location="bottom right">
      {{ snackbar.message }}
    </v-snackbar>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStudents, deleteStudent } from '@/api'

const students = ref([])
const loading = ref(false)
const deleteDialog = ref(false)
const deleting = ref(false)
const deletingStudent = ref(null)
const snackbar = ref({ show: false, message: '', color: 'success' })

const filters = ref({ firstName: '', lastName: '', email: '', teamName: '', sectionName: '' })

const headers = [
  { title: 'First Name', key: 'firstName', sortable: true },
  { title: 'Last Name', key: 'lastName', sortable: true },
  { title: 'Email', key: 'email', sortable: true },
  { title: 'Team', key: 'teamName', sortable: true },
  { title: 'Section', key: 'sectionName', sortable: true },
  { title: '', key: 'actions', sortable: false, align: 'end' },
]

onMounted(fetchStudents)

async function fetchStudents() {
  loading.value = true
  try {
    const res = await getStudents({
      firstName: filters.value.firstName || undefined,
      lastName: filters.value.lastName || undefined,
      email: filters.value.email || undefined,
      teamName: filters.value.teamName || undefined,
      sectionName: filters.value.sectionName || undefined,
    })
    students.value = res.data   // already sorted by backend: section desc, last name asc
  } catch {
    notify('Failed to load students.', 'error')
  } finally {
    loading.value = false
  }
}

function confirmDelete(student) {
  deletingStudent.value = student
  deleteDialog.value = true
}

async function doDelete() {
  deleting.value = true
  try {
    await deleteStudent(deletingStudent.value.id)
    deleteDialog.value = false
    notify(`${deletingStudent.value.firstName} ${deletingStudent.value.lastName} deleted.`, 'success')
    await fetchStudents()
  } catch (e) {
    notify(e.response?.data?.message || 'Delete failed.', 'error')
  } finally {
    deleting.value = false
  }
}

function notify(message, color = 'success') {
  snackbar.value = { show: true, message, color }
}
</script>
