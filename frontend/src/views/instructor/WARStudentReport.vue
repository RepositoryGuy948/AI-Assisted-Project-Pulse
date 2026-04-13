<template>
  <div>
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" @click="$router.back()" />
      <h1 class="text-h5 font-weight-bold ml-2">WAR Report — Individual Student</h1>
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
        />
      </v-col>
      <v-col cols="4" class="d-flex align-center">
        <v-btn color="primary" :loading="loading" @click="loadReport">Generate Report</v-btn>
      </v-col>
    </v-row>

    <div v-for="warGroup in report" :key="warGroup.weekId" class="mb-6">
      <h3 class="text-subtitle-1 font-weight-bold mb-2">Week of {{ warGroup.weekStartDate }}</h3>
      <v-data-table
        :headers="headers"
        :items="warGroup.activities"
        density="compact"
        hide-default-footer
      >
        <template #item.status="{ item }">
          <v-chip size="x-small" :color="statusColor(item.status)">{{ item.status }}</v-chip>
        </template>
      </v-data-table>
    </div>

    <v-alert v-if="report.length === 0 && loaded" type="info">No WAR data for selected period.</v-alert>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getStudent, getStudentWARReport } from '@/api'

const route = useRoute()
const weeks = ref([])
const report = ref([])
const startWeekId = ref(null)
const endWeekId = ref(null)
const loading = ref(false)
const loaded = ref(false)

const headers = [
  { title: 'Category', key: 'category' },
  { title: 'Description', key: 'description' },
  { title: 'Planned hrs', key: 'plannedHours' },
  { title: 'Actual hrs', key: 'actualHours' },
  { title: 'Status', key: 'status' },
]

onMounted(async () => {
  // In a full implementation, load weeks based on student's section
})

async function loadReport() {
  if (!startWeekId.value || !endWeekId.value) return
  loading.value = true
  try {
    const startIdx = weeks.value.findIndex(w => w.id === startWeekId.value)
    const endIdx = weeks.value.findIndex(w => w.id === endWeekId.value)
    const weekIds = weeks.value.slice(
      Math.min(startIdx, endIdx),
      Math.max(startIdx, endIdx) + 1
    ).map(w => w.id)

    const res = await getStudentWARReport(route.params.id, weekIds)
    // Group by week
    const grouped = {}
    res.data.forEach(war => {
      if (!grouped[war.weekId]) {
        grouped[war.weekId] = { weekId: war.weekId, weekStartDate: war.weekStartDate, activities: [] }
      }
      grouped[war.weekId].activities.push(...(war.activities || []))
    })
    report.value = Object.values(grouped)
    loaded.value = true
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
