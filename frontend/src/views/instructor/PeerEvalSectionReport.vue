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
          @update:model-value="loadReport"
        />
      </v-col>
    </v-row>

    <v-card v-if="report.length > 0">
      <v-data-table
        :headers="headers"
        :items="report"
        item-value="studentId"
        :sort-by="[{ key: 'studentName', order: 'asc' }]"
      >
        <template #item.grade="{ item }">
          <span class="font-weight-bold">{{ item.grade?.toFixed(1) }}</span>
        </template>
        <template #item.expand="{ item }">
          <v-btn icon="mdi-chevron-down" variant="text" size="small" @click="toggleExpand(item)" />
        </template>
      </v-data-table>
    </v-card>

    <v-alert v-else-if="selectedWeekId" type="info">No data for selected week.</v-alert>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSections, getActiveWeeks, getSectionPeerReport } from '@/api'

const sections = ref([])
const weeks = ref([])
const report = ref([])
const selectedSectionId = ref(null)
const selectedWeekId = ref(null)

const headers = [
  { title: 'Student', key: 'studentName' },
  { title: 'Grade', key: 'grade' },
  { title: 'Evaluations', key: 'evaluationCount' },
  { title: 'Details', key: 'expand', sortable: false },
]

onMounted(async () => {
  const res = await getSections()
  sections.value = res.data
})

async function loadWeeks() {
  if (!selectedSectionId.value) return
  const res = await getActiveWeeks(selectedSectionId.value)
  weeks.value = res.data.map(w => ({
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

function toggleExpand(item) {
  // TODO: show detailed breakdown dialog
}
</script>
