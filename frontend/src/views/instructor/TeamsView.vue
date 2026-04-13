<template>
  <div>
    <h1 class="text-h5 font-weight-bold mb-6">Teams</h1>
    <v-card>
      <v-data-table :headers="headers" :items="teams" :loading="loading">
        <template #item.students="{ item }">{{ item.students.length }}</template>
        <template #item.actions="{ item }">
          <v-btn icon="mdi-eye" variant="text" size="small" :to="`/instructor/teams/${item.id}`" />
        </template>
      </v-data-table>
    </v-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTeams } from '@/api'

const teams = ref([])
const loading = ref(false)
const headers = [
  { title: 'Section', key: 'sectionName' },
  { title: 'Team', key: 'name' },
  { title: 'Members', key: 'students' },
  { title: 'Actions', key: 'actions', sortable: false },
]

onMounted(async () => {
  loading.value = true
  try {
    const res = await getTeams()
    teams.value = res.data
  } finally {
    loading.value = false
  }
})
</script>
