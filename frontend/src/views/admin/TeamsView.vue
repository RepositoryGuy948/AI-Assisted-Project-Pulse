<template>
  <div>
    <div class="d-flex align-center mb-6">
      <h1 class="text-h5 font-weight-bold">Teams</h1>
      <v-spacer />
      <v-btn color="primary" prepend-icon="mdi-plus" @click="dialog = true">New Team</v-btn>
    </div>

    <v-row class="mb-4">
      <v-col cols="4">
        <v-text-field v-model="filters.sectionName" label="Section" variant="outlined" density="compact"
          clearable @update:model-value="fetchTeams" />
      </v-col>
      <v-col cols="4">
        <v-text-field v-model="filters.teamName" label="Team name" variant="outlined" density="compact"
          clearable @update:model-value="fetchTeams" />
      </v-col>
    </v-row>

    <v-card>
      <v-data-table :headers="headers" :items="teams" :loading="loading" item-value="id">
        <template #item.instructors="{ item }">
          {{ item.instructors.map(i => i.lastName).join(', ') }}
        </template>
        <template #item.students="{ item }">
          {{ item.students.length }}
        </template>
        <template #item.actions="{ item }">
          <v-btn icon="mdi-eye" variant="text" size="small" :to="`/admin/teams/${item.id}`" />
        </template>
      </v-data-table>
    </v-card>

    <!-- Create Team Dialog -->
    <v-dialog v-model="dialog" max-width="500">
      <v-card>
        <v-card-title>New Team</v-card-title>
        <v-card-text>
          <v-select
            v-model="form.sectionId"
            :items="sections"
            item-title="name"
            item-value="id"
            label="Section"
            variant="outlined"
            class="mb-3"
          />
          <v-text-field v-model="form.name" label="Team Name" variant="outlined" class="mb-3" />
          <v-textarea v-model="form.description" label="Description" variant="outlined" class="mb-3" />
          <v-text-field v-model="form.websiteUrl" label="Website URL" variant="outlined" />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="dialog = false">Cancel</v-btn>
          <v-btn color="primary" :loading="saving" @click="save">Create</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTeams, createTeam, getSections } from '@/api'

const teams = ref([])
const sections = ref([])
const loading = ref(false)
const dialog = ref(false)
const saving = ref(false)
const filters = ref({ sectionName: '', teamName: '' })
const form = ref({ name: '', description: '', websiteUrl: '', sectionId: null })

const headers = [
  { title: 'Section', key: 'sectionName' },
  { title: 'Team Name', key: 'name' },
  { title: 'Members', key: 'students' },
  { title: 'Instructors', key: 'instructors' },
  { title: 'Actions', key: 'actions', sortable: false },
]

onMounted(() => {
  fetchTeams()
  loadSections()
})

async function fetchTeams() {
  loading.value = true
  try {
    const res = await getTeams({
      sectionName: filters.value.sectionName || undefined,
      teamName: filters.value.teamName || undefined,
    })
    teams.value = res.data
  } finally {
    loading.value = false
  }
}

async function loadSections() {
  const res = await getSections()
  sections.value = res.data
}

async function save() {
  saving.value = true
  try {
    await createTeam(form.value)
    dialog.value = false
    fetchTeams()
  } finally {
    saving.value = false
  }
}
</script>
