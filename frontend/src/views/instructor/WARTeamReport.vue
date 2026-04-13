<template>
  <div>
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" @click="$router.back()" />
      <h1 class="text-h5 font-weight-bold ml-2">WAR Report — Team</h1>
    </div>

    <v-select
      v-model="selectedWeekId"
      :items="weeks"
      item-title="label"
      item-value="id"
      label="Select Week"
      variant="outlined"
      class="mb-6"
      style="max-width: 400px"
      @update:model-value="loadReport"
    />

    <v-card v-if="report.length > 0">
      <v-card-title>Activities</v-card-title>
      <v-data-table :headers="headers" :items="flatActivities" :loading="loading">
        <template #item.status="{ item }">
          <v-chip size="small" :color="statusColor(item.status)">{{ item.status }}</v-chip>
        </template>
      </v-data-table>
    </v-card>

    <v-card v-else-if="selectedWeekId">
      <v-card-text>
        <v-alert type="info">No WAR data for this week.</v-alert>
        <div v-if="missingStudents.length > 0" class="mt-3">
          <strong>Students who have NOT submitted:</strong>
          <v-chip v-for="s in missingStudents" :key="s" class="ml-2 mt-1" color="warning" size="small">{{ s }}</v-chip>
        </div>
      </v-card-text>
    </v-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getTeam, getActiveWeeks, getTeamWARReport } from '@/api'

const route = useRoute()
const weeks = ref([])
const report = ref([])
const loading = ref(false)
const selectedWeekId = ref(null)
const teamStudents = ref([])

const headers = [
  { title: 'Student', key: 'studentName' },
  { title: 'Category', key: 'category' },
  { title: 'Description', key: 'description' },
  { title: 'Planned hrs', key: 'plannedHours' },
  { title: 'Actual hrs', key: 'actualHours' },
  { title: 'Status', key: 'status' },
]

onMounted(async () => {
  const teamRes = await getTeam(route.params.id)
  teamStudents.value = teamRes.data.students
  const weekRes = await getActiveWeeks(teamRes.data.sectionId)
  weeks.value = weekRes.data.map(w => ({
    id: w.id,
    label: `Week of ${new Date(w.startDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}`,
  }))
})

async function loadReport() {
  if (!selectedWeekId.value) return
  loading.value = true
  try {
    const res = await getTeamWARReport(route.params.id, selectedWeekId.value)
    report.value = res.data
  } finally {
    loading.value = false
  }
}

const flatActivities = computed(() => {
  return report.value.flatMap(war =>
    war.activities.map(a => ({
      ...a,
      studentName: `${war.studentFirstName} ${war.studentLastName}`,
    }))
  )
})

const missingStudents = computed(() => {
  const reported = new Set(report.value.map(w => w.studentId))
  return teamStudents.value
    .filter(s => !reported.has(s.id))
    .map(s => `${s.firstName} ${s.lastName}`)
})

function statusColor(s) {
  if (s === 'DONE') return 'success'
  if (s === 'UNDER_TESTING') return 'warning'
  return 'info'
}
</script>
