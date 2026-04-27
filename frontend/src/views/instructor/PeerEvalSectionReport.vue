<template>
  <div>
    <h1 class="text-h5 font-weight-bold mb-6">Peer Evaluation Report — Section</h1>

    <v-row class="mb-4">
      <v-col cols="4">
        <v-select
          v-model="selectedSectionId"
          :items="sections"
          item-title="name"
          item-value="id"
          label="Section"
          variant="outlined"
          @update:model-value="loadWeeks"
        />
      </v-col>
      <v-col cols="4">
        <v-select
          v-model="selectedWeekId"
          :items="weeks"
          item-title="label"
          item-value="id"
          label="Week"
          variant="outlined"
          :disabled="!selectedSectionId"
          @update:model-value="loadReport"
        />
      </v-col>
    </v-row>

    <v-card v-if="report.length > 0">
      <v-card-subtitle class="pt-3 pb-1">
        Students with no evaluations received:
        <v-chip
          v-for="name in missingStudents"
          :key="name"
          size="small"
          color="warning"
          class="ml-1"
        >{{ name }}</v-chip>
        <span v-if="missingStudents.length === 0" class="text-success ml-1">All students received evaluations</span>
      </v-card-subtitle>
      <v-data-table
        :headers="headers"
        :items="report"
        item-value="studentId"
        :sort-by="[{ key: 'studentName', order: 'asc' }]"
      >
        <template #item.grade="{ item }">
          <span class="font-weight-bold">{{ item.grade != null ? item.grade.toFixed(1) : 'N/A' }}</span>
        </template>
        <template #item.actions="{ item }">
          <v-btn size="small" variant="text" color="primary" @click="showDetails(item)">
            Details
          </v-btn>
        </template>
      </v-data-table>
    </v-card>

    <v-alert v-else-if="selectedWeekId" type="info" class="mt-2">
      No peer evaluation data for selected week.
    </v-alert>

    <!-- Student Evaluation Details Dialog -->
    <v-dialog v-model="detailDialog" max-width="800" scrollable>
      <v-card v-if="selectedStudent">
        <v-card-title>
          {{ selectedStudent.studentName }} — Evaluation Details
          <span class="ml-4 text-body-1 text-medium-emphasis">
            Grade: {{ selectedStudent.grade != null ? selectedStudent.grade.toFixed(1) : 'N/A' }}
          </span>
        </v-card-title>
        <v-divider />
        <v-card-text>
          <v-card
            v-for="ev in selectedStudent.evaluations"
            :key="ev.id"
            class="mb-3"
            variant="outlined"
          >
            <v-card-title class="text-body-1 pb-1">
              Evaluator: {{ ev.evaluatorName }}
              <v-chip size="x-small" class="ml-2" color="primary">Total: {{ ev.totalScore }}</v-chip>
            </v-card-title>
            <v-card-text class="pt-1">
              <v-row dense>
                <v-col
                  v-for="s in ev.scores"
                  :key="s.criterionId"
                  cols="6"
                >
                  <span class="text-body-2 font-weight-medium">{{ s.criterionName }}:</span>
                  <span class="text-body-2 ml-1">{{ s.score }}</span>
                </v-col>
              </v-row>
              <div v-if="ev.publicComment" class="mt-2">
                <strong>Public comment:</strong> {{ ev.publicComment }}
              </div>
              <div v-if="ev.privateComment" class="mt-1">
                <strong>Private comment:</strong> {{ ev.privateComment }}
              </div>
            </v-card-text>
          </v-card>
          <v-alert v-if="!selectedStudent.evaluations || selectedStudent.evaluations.length === 0" type="info">
            No evaluations received this week.
          </v-alert>
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
import { ref, computed, onMounted } from 'vue'
import { getSections, getActiveWeeks, getSectionPeerReport } from '@/api'

const sections = ref([])
const weeks = ref([])
const report = ref([])
const selectedSectionId = ref(null)
const selectedWeekId = ref(null)
const detailDialog = ref(false)
const selectedStudent = ref(null)

const headers = [
  { title: 'Student', key: 'studentName' },
  { title: 'Grade', key: 'grade' },
  { title: 'Evaluations received', key: 'evaluationCount' },
  { title: 'Details', key: 'actions', sortable: false },
]

const missingStudents = computed(() =>
  report.value.filter(r => r.evaluationCount === 0).map(r => r.studentName)
)

onMounted(async () => {
  const res = await getSections()
  sections.value = res.data
})

async function loadWeeks() {
  if (!selectedSectionId.value) return
  const res = await getActiveWeeks(selectedSectionId.value)
  weeks.value = res.data
    .sort((a, b) => new Date(b.startDate) - new Date(a.startDate))
    .map(w => ({
      id: w.id,
      label: `Week of ${new Date(w.startDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}`,
    }))
  selectedWeekId.value = null
  report.value = []
}

async function loadReport() {
  if (!selectedSectionId.value || !selectedWeekId.value) return
  const res = await getSectionPeerReport(selectedSectionId.value, selectedWeekId.value)
  report.value = res.data
}

function showDetails(item) {
  selectedStudent.value = item
  detailDialog.value = true
}
</script>
