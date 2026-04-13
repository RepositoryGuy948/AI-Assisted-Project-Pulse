<template>
  <div>
    <h1 class="text-h5 font-weight-bold mb-6">Students</h1>
    <v-card>
      <v-data-table :headers="headers" :items="students" :loading="loading">
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
const headers = [
  { title: 'First Name', key: 'firstName' },
  { title: 'Last Name', key: 'lastName' },
  { title: 'Email', key: 'email' },
  { title: 'Team', key: 'teamName' },
  { title: 'Actions', key: 'actions', sortable: false },
]

onMounted(async () => {
  loading.value = true
  try {
    const res = await getStudents()
    students.value = res.data.sort((a, b) => a.lastName.localeCompare(b.lastName))
  } finally {
    loading.value = false
  }
})
</script>
