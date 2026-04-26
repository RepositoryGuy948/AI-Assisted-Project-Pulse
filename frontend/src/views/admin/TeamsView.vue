<template>
  <div>
    <div class="d-flex align-center mb-6">
      <h1 class="text-h5 font-weight-bold">Teams</h1>
      <v-spacer />
      <v-btn color="primary" prepend-icon="mdi-plus" @click="openDialog">New Team</v-btn>
    </div>

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
          <v-btn icon="mdi-eye" variant="text" size="small" :to="`/admin/teams/${item.id}`" />
        </template>
      </v-data-table>
    </v-card>

    <!-- Create Team Dialog (UC-9) -->
    <v-dialog v-model="dialog" max-width="520" @after-leave="resetForm">
      <v-card>
        <v-card-title class="pa-4 pb-0">New Team</v-card-title>
        <v-card-text class="pt-4">
          <v-form ref="formRef" validate-on="submit lazy">
            <v-select
              v-model="form.sectionId"
              :items="sections"
              item-title="name"
              item-value="id"
              label="Section *"
              variant="outlined"
              class="mb-3"
              :rules="[v => !!v || 'Section is required']"
            />
            <v-text-field
              v-model="form.name"
              label="Team Name *"
              variant="outlined"
              class="mb-3"
              :rules="[
                v => !!v?.trim() || 'Team name is required',
                v => v?.trim().length <= 100 || 'Max 100 characters',
              ]"
            />
            <v-textarea
              v-model="form.description"
              label="Description"
              variant="outlined"
              rows="3"
              class="mb-3"
            />
            <v-text-field
              v-model="form.websiteUrl"
              label="Website URL"
              variant="outlined"
              :rules="[
                v => !v || /^https?:\/\/.+/.test(v) || 'Must be a valid URL (https://...)',
              ]"
            />
          </v-form>
        </v-card-text>
        <v-card-actions class="pa-4 pt-0">
          <v-spacer />
          <v-btn variant="text" @click="dialog = false">Cancel</v-btn>
          <v-btn color="primary" :loading="saving" @click="save">Create</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Snackbar feedback -->
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="4000" location="bottom right">
      {{ snackbar.message }}
    </v-snackbar>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTeams, createTeamInSection, getSections } from '@/api'

const teams = ref([])
const sections = ref([])
const loading = ref(false)
const dialog = ref(false)
const saving = ref(false)
const formRef = ref(null)
const filters = ref({ sectionName: '', teamName: '', instructor: '' })
const form = ref({ name: '', description: '', websiteUrl: '', sectionId: null })
const snackbar = ref({ show: false, message: '', color: 'success' })

const headers = [
  { title: 'Section', key: 'sectionName', sortable: true },
  { title: 'Team Name', key: 'name', sortable: true },
  { title: 'Members', key: 'students', sortable: false },
  { title: 'Instructors', key: 'instructors', sortable: false },
  { title: '', key: 'actions', sortable: false, align: 'end' },
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
      instructor: filters.value.instructor || undefined,
    })
    teams.value = res.data
  } catch {
    notify('Failed to load teams.', 'error')
  } finally {
    loading.value = false
  }
}

async function loadSections() {
  try {
    const res = await getSections()
    sections.value = res.data
  } catch {
    notify('Failed to load sections.', 'error')
  }
}

function openDialog() {
  resetForm()
  dialog.value = true
}

function resetForm() {
  form.value = { name: '', description: '', websiteUrl: '', sectionId: null }
  formRef.value?.reset()
}

async function save() {
  const { valid } = await formRef.value.validate()
  if (!valid) return

  saving.value = true
  try {
    const { sectionId, ...body } = form.value
    await createTeamInSection(sectionId, body)
    dialog.value = false
    notify(`Team "${form.value.name}" created successfully.`, 'success')
    await fetchTeams()
  } catch (err) {
    const msg = err.response?.data?.message || 'Failed to create team.'
    notify(msg, 'error')
  } finally {
    saving.value = false
  }
}

function notify(message, color = 'success') {
  snackbar.value = { show: true, message, color }
}
</script>
