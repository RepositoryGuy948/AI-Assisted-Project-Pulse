<template>
  <div>
    <div class="d-flex align-center mb-6">
      <h1 class="text-h5 font-weight-bold">Senior Design Sections</h1>
      <v-spacer />
      <v-btn color="primary" prepend-icon="mdi-plus" @click="openCreateDialog">New Section</v-btn>
    </div>

    <v-text-field
      v-model="search"
      label="Search sections..."
      prepend-inner-icon="mdi-magnify"
      variant="outlined"
      density="compact"
      class="mb-4"
      clearable
      @update:model-value="fetchSections"
    />

    <v-card>
      <v-data-table
        :headers="headers"
        :items="sections"
        :loading="loading"
        item-value="id"
      >
        <template #item.actions="{ item }">
          <v-btn icon="mdi-eye" variant="text" size="small" :to="`/admin/sections/${item.id}`" />
          <v-btn icon="mdi-pencil" variant="text" size="small" @click="openEditDialog(item)" />
          <v-btn icon="mdi-calendar-check" variant="text" size="small" :to="`/admin/sections/${item.id}/active-weeks`" />
        </template>
      </v-data-table>
    </v-card>

    <!-- Create/Edit Dialog -->
    <v-dialog v-model="dialog" max-width="500">
      <v-card>
        <v-card-title>{{ editing ? 'Edit Section' : 'New Section' }}</v-card-title>
        <v-card-text>
          <v-text-field v-model="form.name" label="Section Name (e.g. 2024-2025)" variant="outlined" class="mb-3" />
          <v-text-field v-model="form.startDate" label="Start Date" type="date" variant="outlined" class="mb-3" />
          <v-text-field v-model="form.endDate" label="End Date" type="date" variant="outlined" class="mb-3" />
          <v-select
            v-model="form.rubricId"
            :items="rubrics"
            item-title="name"
            item-value="id"
            label="Rubric"
            variant="outlined"
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="dialog = false">Cancel</v-btn>
          <v-btn color="primary" :loading="saving" @click="save">Save</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSections, createSection, updateSection, getRubrics } from '@/api'

const sections = ref([])
const rubrics = ref([])
const loading = ref(false)
const search = ref('')
const dialog = ref(false)
const saving = ref(false)
const editing = ref(null)

const headers = [
  { title: 'Name', key: 'name' },
  { title: 'Start Date', key: 'startDate' },
  { title: 'End Date', key: 'endDate' },
  { title: 'Actions', key: 'actions', sortable: false },
]

const form = ref({ name: '', startDate: '', endDate: '', rubricId: null })

onMounted(() => {
  fetchSections()
  fetchRubrics()
})

async function fetchSections() {
  loading.value = true
  try {
    const res = await getSections({ name: search.value || undefined })
    sections.value = res.data.sort((a, b) => b.name.localeCompare(a.name))
  } finally {
    loading.value = false
  }
}

async function fetchRubrics() {
  const res = await getRubrics()
  rubrics.value = res.data
}

function openCreateDialog() {
  editing.value = null
  form.value = { name: '', startDate: '', endDate: '', rubricId: null }
  dialog.value = true
}

function openEditDialog(item) {
  editing.value = item
  form.value = { name: item.name, startDate: item.startDate, endDate: item.endDate, rubricId: item.rubricId }
  dialog.value = true
}

async function save() {
  saving.value = true
  try {
    if (editing.value) {
      await updateSection(editing.value.id, form.value)
    } else {
      await createSection(form.value)
    }
    dialog.value = false
    fetchSections()
  } finally {
    saving.value = false
  }
}
</script>
