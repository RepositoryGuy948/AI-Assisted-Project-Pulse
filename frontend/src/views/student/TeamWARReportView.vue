<template>
  <div>
    <div :style="{ opacity: pageLoading ? 0 : 1, transition: 'opacity 0.3s ease' }">
    <h1 class="text-h5 font-weight-bold mb-2">Team WAR Report</h1>
    <p class="text-body-2 text-medium-emphasis mb-6">View your team's weekly activity reports.</p>
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

    <v-alert v-if="!teamId" type="warning" class="mb-4">
      You are not assigned to a team yet.
    </v-alert>

    <v-card v-if="report.length > 0" style="overflow-x:auto">
      <v-card-subtitle class="pt-3 pb-1">
        Students who have NOT submitted:
        <v-chip
          v-for="name in missingStudents"
          :key="name"
          size="small"
          color="warning"
          class="ml-1"
        >{{ name }}</v-chip>
        <span v-if="missingStudents.length === 0" class="text-success ml-1">All submitted</span>
      </v-card-subtitle>
      <v-data-table
        :headers="headers"
        :items="flatActivities"
        :loading="loading"
        density="comfortable"
      >
        <template #item.status="{ item }">
          <v-chip size="small" :color="statusColor(item.status)">{{ item.status }}</v-chip>
        </template>
      </v-data-table>
    </v-card>

    <v-alert v-else-if="selectedWeekId && !loading" type="info">
      No WAR data submitted for this week.
    </v-alert>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getMe, getTeam, getActiveWeeks, getTeamWARReport } from '@/api'
import { weekLabel } from '@/utils/weekLabel'

const auth = useAuthStore()
const pageLoading = ref(true)
const weeks = ref([])
const report = ref([])
const loading = ref(false)
const selectedWeekId = ref(null)
const teamId = ref(null)
const teamStudents = ref([])

const headers = [
  { title: 'Student', key: 'studentName' },
  { title: 'Category', key: 'category' },
  { title: 'Description', key: 'description' },
  { title: 'Planned hrs', key: 'plannedHours' },
  { title: 'Actual hrs', key: 'actualHours' },
  { title: 'Status', key: 'status' },
]

const flatActivities = computed(() =>
  report.value.flatMap(war =>
    war.activities.map(a => ({
      ...a,
      studentName: `${war.studentFirstName} ${war.studentLastName}`,
    }))
  )
)

const missingStudents = computed(() => {
  const reportedIds = new Set(report.value.map(w => w.studentId))
  return teamStudents.value
    .filter(s => !reportedIds.has(s.id))
    .map(s => `${s.firstName} ${s.lastName}`)
})

onMounted(async () => {
  const me = await getMe()
  if (!me.data.teamId) return
  teamId.value = me.data.teamId

  const teamRes = await getTeam(teamId.value)
  teamStudents.value = teamRes.data.students

  const weekRes = await getActiveWeeks(teamRes.data.sectionId)
  weeks.value = weekRes.data
    .sort((a, b) => new Date(b.startDate) - new Date(a.startDate))
    .map(w => ({
      id: w.id,
      label: weekLabel(w),
    }))

  if (weeks.value.length > 0) {
    selectedWeekId.value = weeks.value[0].id
    await loadReport()
  }
  pageLoading.value = false
})

async function loadReport() {
  if (!teamId.value || !selectedWeekId.value) return
  loading.value = true
  try {
    const res = await getTeamWARReport(teamId.value, selectedWeekId.value)
    report.value = res.data
  } finally {
    loading.value = false
  }
}

function statusColor(s) {
  if (s === 'DONE') return 'success'
  if (s === 'UNDER_TESTING') return 'warning'
  return 'info'
}
</script>
