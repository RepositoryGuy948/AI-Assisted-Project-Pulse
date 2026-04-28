<template>
  <div>
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" @click="$router.back()" />
      <h1 class="text-h5 font-weight-bold ml-2">Peer Evaluation Report — {{ studentName }}</h1>
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
        <v-btn color="primary" @click="loadReport" :loading="loading" :disabled="!startWeekId || !endWeekId">
          Generate Report
        </v-btn>
      </v-col>
    </v-row>

    <v-alert v-if="weeks.length === 0 && !loadingWeeks" type="warning" class="mb-4">
      No active weeks found for this student's section.
    </v-alert>

    <v-card v-if="report.length > 0">
      <v-data-table
        :headers="headers"
        :items="report"
        item-value="weekId"
        show-expand
        expand-on-click
      >
        <template #item.grade="{ item }">
          <v-chip v-if="item.noSubmission" size="small" color="warning" variant="tonal">No Submissions</v-chip>
          <span v-else class="font-weight-bold">{{ formatGrade(item) }}</span>
        </template>
        <template #item.evaluations="{ item }">
          <span v-if="item.noSubmission" class="text-medium-emphasis">—</span>
          <span v-else>{{ item.evaluations.length }} evaluator(s)</span>
        </template>
        <template #expanded-row="{ item, columns }">
          <tr>
            <td :colspan="columns.length + 1" class="pa-4 bg-grey-lighten-5">
              <div v-if="item.noSubmission" class="text-medium-emphasis">
                No peer evaluations were received for this week.
              </div>
              <div v-else>
                <v-card
                  v-for="ev in item.evaluations"
                  :key="ev.id"
                  class="mb-3"
                  variant="outlined"
                >
                  <v-card-title class="text-body-1 pb-1">
                    From: {{ ev.evaluatorName }}
                    <v-chip size="x-small" class="ml-2" color="primary">Total: {{ ev.totalScore }}</v-chip>
                  </v-card-title>
                  <v-card-text class="pt-1">
                    <v-row dense>
                      <v-col v-for="s in ev.scores" :key="s.criterionId" cols="6">
                        <span class="text-body-2 font-weight-medium">{{ s.criterionName }}:</span>
                        <span class="text-body-2 ml-1">{{ s.score }}</span>
                      </v-col>
                    </v-row>
                    <div v-if="ev.publicComment" class="mt-2">
                      <strong>Public:</strong> {{ ev.publicComment }}
                    </div>
                    <div v-if="ev.privateComment" class="mt-1">
                      <strong>Private:</strong> {{ ev.privateComment }}
                    </div>
                  </v-card-text>
                </v-card>
              </div>
            </td>
          </tr>
        </template>
      </v-data-table>
    </v-card>

    <v-alert v-if="report.length === 0 && loaded" type="info">
      No peer evaluation data for selected period.
    </v-alert>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getStudent, getActiveWeeks, getTeam, getStudentPeerPeriodReport } from '@/api'

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
  { title: 'Week', key: 'weekStartDate' },
  { title: 'Grade', key: 'grade' },
  { title: 'Evaluations', key: 'evaluations', sortable: false },
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
          startDate: w.startDate,
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

    const res = await getStudentPeerPeriodReport(route.params.id, weekIds)
    report.value = res.data.map(item => ({
      ...item,
      weekStartDate: item.weekStartDate
        ? formatDate(item.weekStartDate)
        : `Week ${item.weekId}`,
    }))
    loaded.value = true
  } finally {
    loading.value = false
  }
}

function formatGrade(item) {
  if (item.grade == null) return 'N/A'
  return item.grade.toFixed(1)
}

function formatDate(d) {
  return new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}
</script>
