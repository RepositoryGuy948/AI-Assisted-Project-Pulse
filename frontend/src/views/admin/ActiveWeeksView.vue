<template>
  <div>
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" :to="`/admin/sections/${route.params.id}`" />
      <h1 class="text-h5 font-weight-bold ml-2">Active Weeks</h1>
      <v-spacer />
      <v-btn color="primary" :loading="saving" @click="saveWeeks">Save Changes</v-btn>
    </div>

    <v-card>
      <v-card-text>
        <p class="text-body-2 text-medium-emphasis mb-4">
          Toggle weeks to mark them as active or inactive. Students must submit WARs and peer evaluations during active weeks.
        </p>
        <v-list>
          <v-list-item v-for="week in weeks" :key="week.id">
            <template #prepend>
              <v-switch v-model="week.active" color="primary" hide-details />
            </template>
            <v-list-item-title>
              Week {{ formatDate(week.startDate) }} – {{ formatDate(week.endDate) }}
            </v-list-item-title>
            <template #append>
              <v-chip :color="week.active ? 'success' : 'default'" size="small">
                {{ week.active ? 'Active' : 'Inactive' }}
              </v-chip>
            </template>
          </v-list-item>
        </v-list>
      </v-card-text>
    </v-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getActiveWeeks, updateActiveWeeks } from '@/api'

const route = useRoute()
const weeks = ref([])
const saving = ref(false)

onMounted(async () => {
  const res = await getActiveWeeks(route.params.id)
  weeks.value = res.data
})

async function saveWeeks() {
  saving.value = true
  try {
    await updateActiveWeeks(route.params.id, {
      weeks: weeks.value.map(w => ({ id: w.id, active: w.active }))
    })
  } finally {
    saving.value = false
  }
}

function formatDate(d) {
  return new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}
</script>
