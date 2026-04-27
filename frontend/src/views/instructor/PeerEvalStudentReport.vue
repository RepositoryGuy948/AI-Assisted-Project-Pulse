<template>
  <div>
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" @click="$router.back()" />
      <div class="ml-2">
        <h1 class="text-h5 font-weight-bold">Peer Evaluation Report</h1>
        <p v-if="studentName" class="text-body-2 text-medium-emphasis mb-0">{{ studentName }}</p>
      </div>
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
        <v-btn color="primary" :loading="loading" :disabled="!startWeekId || !endWeekId" @click="loadReport">
          Generate Report
        </v-btn>
      </v-col>
    </v-row>

    <v-card v-if="report.length > 0">
      <v-data-table
        :headers="headers"
        :items="report"
        item-value="weekId"
        expand-on-click
        show-expand
      >
        <template #item.weekStartDate="{ item }">
          {{ formatDate(item.weekStartDate) }}
        </template>
        <template #item.grade="{ item }">
          <v-chip v-if="item.noSubmission" color="warning" size="small">No submission</v-chip>
          <span v-else class="font-weight-bold">{{ item.grade?.toFixed(1) }} / 60</span>
        </template>
        <template #item.evaluations="{ item }">
          <span v-if="item.noSubmission" class="text-medium-emphasis">—</span>
          <span v-else>{{ item.evaluations.length }} evaluator(s)</span>
        </template>
        <template #expanded-row="{ item }">
          <td :colspan="headers.length + 1" class="pa-4 bg-grey-lighten-5">
            <div v-if="item.noSubmission" class="text-medium-emphasis">
              This student did not receive any peer evaluations for this week.
            </div>
            <v-card
              v-else
              v-for="ev in item.evaluations"
              :key="ev.id"
              class="mb-3"
              variant="outlined"
            >
              <v-card-title class="text-body-1">From: {{ ev.evaluatorName }}</v-card-title>
              <v-card-text>
                <!-- Per-criterion scores -->
                <v-table density="compact" class="mb-3">
                  <thead>
                    <tr>
                      <th>Criterion</th>
                      <th>Score</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="s in ev.scores" :key="s.criterionId">
                      <td>{{ s.criterionName }}</td>
                      <td>
                        <v-chip size="small" color="primary">{{ s.score }}</v-chip>
                      </td>
                    </tr>
                  </tbody>
                </v-table>
                <v-chip size="small" class="mb-2">Total: {{ ev.totalScore }}</v-chip>
                <div v-if="ev.publicComment" class="mt-1 text-body-2">
                  <strong>Public:</strong> {{ ev.publicComment }}
                </div>
                <div v-if="ev.privateComment" class="mt-1 text-body-2">
                  <strong>Private:</strong> {{ ev.privateComment }}
                </div>
              </v-card-text>
            </v-card>
          </td>
        </template>
      </v-data-table>
    </v-card>

    <v-alert v-else-if="generated" type="info" class="mt-4">
      No peer evaluation data found for this range.
    </v-alert>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getStudent, getTeam, getActiveWeeks, getStudentPeerPeriodReport } from '@/api'

const route = useRoute()
const weeks = ref([])
const report = ref([])
const startWeekId = ref(null)
const endWeekId = ref(null)
const loading = ref(false)
const generated = ref(false)
const studentName = ref('')

const headers = [
  { title: 'Week', key: 'weekStartDate' },
  { title: 'Grade', key: 'grade' },
  { title: 'Evaluations', key: 'evaluations', sortable: false },
]

onMounted(async () => {
  const studentRes = await getStudent(route.params.id)
  const student = studentRes.data
  studentName.value = `${student.firstName} ${student.lastName}`

  if (!student.teamId) return

  const teamRes = await getTeam(student.teamId)
  const weekRes = await getActiveWeeks(teamRes.data.sectionId)
  const now = new Date()

  weeks.value = weekRes.data
    .filter(w => w.active && new Date(w.endDate) < now)
    .map(w => ({
      id: w.id,
      label: `Week of ${formatDate(w.startDate)}`,
    }))
})

async function loadReport() {
  if (!startWeekId.value || !endWeekId.value) return
  loading.value = true
  generated.value = false
  try {
    const startIdx = weeks.value.findIndex(w => w.id === startWeekId.value)
    const endIdx = weeks.value.findIndex(w => w.id === endWeekId.value)
    const weekIds = weeks.value
      .slice(Math.min(startIdx, endIdx), Math.max(startIdx, endIdx) + 1)
      .map(w => w.id)

    const res = await getStudentPeerPeriodReport(route.params.id, weekIds)
    report.value = res.data
    generated.value = true
  } finally {
    loading.value = false
  }
}

function formatDate(d) {
  return new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}
</script>