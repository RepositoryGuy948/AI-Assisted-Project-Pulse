<template>
  <div>
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" @click="$router.back()" />
      <h1 class="text-h5 font-weight-bold ml-2">WAR Report — {{ studentName }}</h1>
    </div>

    <v-row class="mb-4">
      <v-col cols="4">
        <v-select
          v-model="startWeekId"
          :items="weeks"
          item-title="label"
          item-value="id"
          label="Start Week"
          variant="outlined"
          :disabled="weeks.length === 0"
        />
      </v-col>
      <v-col cols="4">
        <v-select
          v-model="endWeekId"
          :items="weeks"
          item-title="label"
          item-value="id"
          label="End Week"
          variant="outlined"
          :disabled="weeks.length === 0"
        />
      </v-col>
      <v-col cols="4" class="d-flex align-center">
        <v-btn color="primary" :loading="loading" :disabled="!startWeekId || !endWeekId" @click="loadReport">
          Generate Report
        </v-btn>
      </v-col>
    </v-row>

    <v-alert v-if="weeks.length === 0 && !loadingWeeks" type="warning" class="mb-4">
      No active weeks found for this student's section.
    </v-alert>

    <div v-for="warGroup in report" :key="warGroup.weekId" class="mb-6">
      <h3 class="text-subtitle-1 font-weight-bold mb-2">
        Week of {{ formatDate(warGroup.weekStartDate) }}
      </h3>
      <v-data-table
        :headers="headers"
        :items="warGroup.activities"
        density="compact"
        hide-default-footer
        class="mb-2"
      >
        <template #item.status="{ item }">
          <v-chip size="x-small" :color="statusColor(item.status)">{{ item.status }}</v-chip>
        </template>
        <template #no-data>
          <span class="text-medium-emphasis">No activities submitted.</span>
        </template>
      </v-data-table>
    </div>

    <v-alert v-if="report.length === 0 && loaded" type="info">
      No WAR data for selected period.
    </v-alert>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getStudent, getStudentWARReport, getTeam, getActiveWeeks } from '@/api'

const route = useRoute()
const weeks = ref([])
const report = ref([])
const startWeekId = ref(null)
const endWeekId = ref(null)
const loading = ref(false)
const loadingWeeks = ref(true)
const loaded = ref(false)
const studentName = ref('')

const headers = [
  { title: 'Category', key: 'category' },
  { title: 'Description', key: 'description' },
  { title: 'Planned hrs', key: 'plannedHours' },
  { title: 'Actual hrs', key: 'actualHours' },
  { title: 'Status', key: 'status' },
]

onMounted(async () => {
  try {
    const studentRes = await getStudent(route.params.id)
    const student = studentRes.data
    studentName.value = `${student.firstName} ${student.lastName}`

    if (student.teamId) {
      const teamRes = await getTeam(student.teamId)
      const weekRes = await getActiveWeeks(teamRes.data.sectionId)
      weeks.value = weekRes.data
        .sort((a, b) => new Date(a.startDate) - new Date(b.startDate))
        .map(w => ({
          id: w.id,
          label: `Week of ${formatDate(w.startDate)}`,
        }))
      if (weeks.value.length > 0) {
        startWeekId.value = weeks.value[0].id
        endWeekId.value = weeks.value[weeks.value.length - 1].id
      }
    }
  } finally {
    loadingWeeks.value = false
  }
})

async function loadReport() {
  if (!startWeekId.value || !endWeekId.value) return
  loading.value = true
  loaded.value = false
  try {
    const startIdx = weeks.value.findIndex(w => w.id === startWeekId.value)
    const endIdx = weeks.value.findIndex(w => w.id === endWeekId.value)
    const lo = Math.min(startIdx, endIdx)
    const hi = Math.max(startIdx, endIdx)
    const weekIds = weeks.value.slice(lo, hi + 1).map(w => w.id)

    const res = await getStudentWARReport(route.params.id, weekIds)

    // Group by week
    const grouped = {}
    res.data.forEach(war => {
      const key = war.weekId
      if (!grouped[key]) {
        grouped[key] = { weekId: war.weekId, weekStartDate: war.weekStartDate, activities: [] }
      }
      grouped[key].activities.push(...(war.activities || []))
    })
    // Fill in weeks with no submission
    weekIds.forEach(id => {
      const matchingWeek = weeks.value.find(w => w.id === id)
      if (!grouped[id] && matchingWeek) {
        grouped[id] = { weekId: id, weekStartDate: matchingWeek.label.replace('Week of ', ''), activities: [] }
      }
    })
    report.value = Object.values(grouped).sort((a, b) => a.weekId - b.weekId)
    loaded.value = true
  } finally {
    loading.value = false
  }
}

function formatDate(d) {
  return new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

function statusColor(s) {
  if (s === 'DONE') return 'success'
  if (s === 'UNDER_TESTING') return 'warning'
  return 'info'
}
</script>
