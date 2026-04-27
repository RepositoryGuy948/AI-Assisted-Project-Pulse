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
          <v-btn icon="mdi-eye" variant="text" size="small" :to="`/instructor/students/${item.id}`" />
        </template>
      </v-data-table>
    </v-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStudents } from '@/api'

const students = ref([])
const loading = ref(false)
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
    students.value = res.data
  } finally {
    loading.value = false
  }
}
</script>
