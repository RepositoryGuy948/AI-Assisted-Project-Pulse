<template>
  <div>
    <h1 class="text-h5 font-weight-bold mb-6">Teams</h1>

    <!-- Search filters (UC-7) -->
    <v-row class="mb-4">
      <v-col cols="12" sm="4">
        <v-text-field
          v-model="filters.sectionName"
          label="Section"
          variant="outlined"
          density="compact"
          clearable
          @update:model-value="fetchTeams"
        />
      </v-col>
      <v-col cols="12" sm="4">
        <v-text-field
          v-model="filters.teamName"
          label="Team name"
          variant="outlined"
          density="compact"
          clearable
          @update:model-value="fetchTeams"
        />
      </v-col>
      <v-col cols="12" sm="4">
        <v-text-field
          v-model="filters.instructor"
          label="Instructor"
          variant="outlined"
          density="compact"
          clearable
          @update:model-value="fetchTeams"
        />
      </v-col>
    </v-row>

    <v-card>
      <v-data-table
        :headers="headers"
        :items="teams"
        :loading="loading"
        item-value="id"
        no-data-text="No teams found."
      >
        <template #item.instructors="{ item }">
          {{ item.instructors.map(i => `${i.firstName} ${i.lastName}`).join(', ') || '—' }}
        </template>
        <template #item.students="{ item }">
          {{ item.students.length }}
        </template>
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
const filters = ref({ sectionName: '', teamName: '', instructor: '' })

const headers = [
  { title: 'Section', key: 'sectionName', sortable: true },
  { title: 'Team', key: 'name', sortable: true },
  { title: 'Members', key: 'students', sortable: false },
  { title: 'Instructors', key: 'instructors', sortable: false },
  { title: '', key: 'actions', sortable: false, align: 'end' },
]

onMounted(fetchTeams)

async function fetchTeams() {
  loading.value = true
  try {
    const res = await getTeams({
      sectionName: filters.value.sectionName || undefined,
      teamName: filters.value.teamName || undefined,
      instructor: filters.value.instructor || undefined,
    })
    teams.value = res.data
  } finally {
    loading.value = false
  }
}
</script>
