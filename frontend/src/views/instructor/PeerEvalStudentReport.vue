<template>
  <div>
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" @click="$router.back()" />
      <h1 class="text-h5 font-weight-bold ml-2">Peer Evaluation Report — Individual</h1>
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
        <v-btn color="primary" @click="loadReport" :loading="loading">Generate Report</v-btn>
      </v-col>
    </v-row>

    <v-card v-if="report.length > 0">
      <v-data-table
        :headers="headers"
        :items="report"
        item-value="weekId"
      >
        <template #item.grade="{ item }">
          <span class="font-weight-bold">{{ item.grade?.toFixed(1) }}</span>
        </template>
        <template #item.evaluations="{ item }">
          <v-btn size="small" variant="text" @click="showDetails(item)">
            {{ item.evaluations.length }} evaluations
          </v-btn>
        </template>
      </v-data-table>
    </v-card>

    <!-- Details Dialog -->
    <v-dialog v-model="detailDialog" max-width="700">
      <v-card v-if="selectedItem">
        <v-card-title>Week {{ selectedItem.weekStartDate }}</v-card-title>
        <v-card-text>
          <v-card v-for="ev in selectedItem.evaluations" :key="ev.id" class="mb-3" variant="outlined">
            <v-card-title class="text-body-1">From: {{ ev.evaluatorName }}</v-card-title>
            <v-card-text>
              <div v-if="ev.publicComment"><strong>Public:</strong> {{ ev.publicComment }}</div>
              <div v-if="ev.privateComment" class="mt-1"><strong>Private:</strong> {{ ev.privateComment }}</div>
              <v-chip size="small" class="mt-2">Total: {{ ev.totalScore }}</v-chip>
            </v-card-text>
          </v-card>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="detailDialog = false">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getStudent, getStudents, getActiveWeeks, getStudentPeerPeriodReport } from '@/api'

const route = useRoute()
const weeks = ref([])
const report = ref([])
const startWeekId = ref(null)
const endWeekId = ref(null)
const loading = ref(false)
const detailDialog = ref(false)
const selectedItem = ref(null)

const headers = [
  { title: 'Week', key: 'weekStartDate' },
  { title: 'Grade', key: 'grade' },
  { title: 'Evaluations', key: 'evaluations', sortable: false },
]

onMounted(async () => {
  // Get student's section weeks - we need to know which section this student is in
  const studentRes = await getStudent(route.params.id)
  const student = studentRes.data
  // For weeks, we'd need to know the section - simplified approach
  // In real implementation, load section weeks based on student's team
})

async function loadReport() {
  if (!startWeekId.value || !endWeekId.value) return
  loading.value = true
  try {
    // Get all week IDs in range
    const startIdx = weeks.value.findIndex(w => w.id === startWeekId.value)
    const endIdx = weeks.value.findIndex(w => w.id === endWeekId.value)
    const weekIds = weeks.value.slice(
      Math.min(startIdx, endIdx),
      Math.max(startIdx, endIdx) + 1
    ).map(w => w.id)

    const res = await getStudentPeerPeriodReport(route.params.id, weekIds)
    report.value = res.data
  } finally {
    loading.value = false
  }
}

function showDetails(item) {
  selectedItem.value = item
  detailDialog.value = true
}
</script>
