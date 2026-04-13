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
          <v-btn icon="mdi-delete" variant="text" size="small" color="error" @click="deleteS(item.id)" />
        </template>
      </v-data-table>
    </v-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStudents, deleteStudent } from '@/api'

const students = ref([])
const loading = ref(false)
const filters = ref({ firstName: '', lastName: '', email: '' })

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

async function deleteS(id) {
  if (confirm('Delete this student? This cannot be undone.')) {
    await deleteStudent(id)
    fetchStudents()
  }
}
</script>
