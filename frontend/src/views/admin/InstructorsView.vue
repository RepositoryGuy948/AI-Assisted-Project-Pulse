<template>
  <div>
    <div class="d-flex align-center mb-6">
      <h1 class="text-h5 font-weight-bold">Instructors</h1>
    </div>

    <v-row class="mb-4">
      <v-col cols="3"><v-text-field v-model="filters.firstName" label="First Name" variant="outlined" density="compact" clearable @update:model-value="fetchInstructors" /></v-col>
      <v-col cols="3"><v-text-field v-model="filters.lastName" label="Last Name" variant="outlined" density="compact" clearable @update:model-value="fetchInstructors" /></v-col>
      <v-col cols="3"><v-text-field v-model="filters.email" label="Email" variant="outlined" density="compact" clearable @update:model-value="fetchInstructors" /></v-col>
      <v-col cols="3">
        <v-select
          v-model="filters.enabled"
          :items="statusOptions"
          item-title="label"
          item-value="value"
          label="Status"
          variant="outlined"
          density="compact"
          clearable
          @update:model-value="fetchInstructors"
        />
      </v-col>
    </v-row>

    <v-card>
      <v-data-table :headers="headers" :items="instructors" :loading="loading">
        <template #item.enabled="{ item }">
          <v-chip :color="item.enabled ? 'success' : 'default'" size="small">
            {{ item.enabled ? 'Active' : 'Inactive' }}
          </v-chip>
        </template>
        <template #item.actions="{ item }">
          <v-btn icon="mdi-eye" variant="text" size="small" :to="`/admin/instructors/${item.id}`" />
          <v-btn v-if="item.enabled" icon="mdi-account-off" variant="text" size="small" color="warning"
            @click="deactivate(item.id)" />
          <v-btn v-else icon="mdi-account-check" variant="text" size="small" color="success"
            @click="reactivate(item.id)" />
        </template>
      </v-data-table>
    </v-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getInstructors, deactivateInstructor, reactivateInstructor } from '@/api'

const instructors = ref([])
const loading = ref(false)
const filters = ref({ firstName: '', lastName: '', email: '', enabled: null })
const statusOptions = [
  { label: 'Active', value: true },
  { label: 'Inactive', value: false },
]

const headers = [
  { title: 'First Name', key: 'firstName' },
  { title: 'Last Name', key: 'lastName' },
  { title: 'Email', key: 'email' },
  { title: 'Status', key: 'enabled' },
  { title: 'Actions', key: 'actions', sortable: false },
]

onMounted(fetchInstructors)

async function fetchInstructors() {
  loading.value = true
  try {
    const res = await getInstructors({
      firstName: filters.value.firstName || undefined,
      lastName: filters.value.lastName || undefined,
      email: filters.value.email || undefined,
      enabled: filters.value.enabled != null ? filters.value.enabled : undefined,
    })
    instructors.value = res.data.sort((a, b) => a.lastName.localeCompare(b.lastName))
  } finally {
    loading.value = false
  }
}

async function deactivate(id) {
  if (confirm('Deactivate this instructor?')) {
    await deactivateInstructor(id)
    fetchInstructors()
  }
}

async function reactivate(id) {
  if (confirm('Reactivate this instructor? They will regain access to the system.')) {
    await reactivateInstructor(id)
    fetchInstructors()
  }
}
</script>
