<template>
  <div>
    <div :style="{ opacity: loading ? 0 : 1, transition: 'opacity 0.3s ease' }">
    <h1 class="text-h5 font-weight-bold mb-6">My Peer Evaluation Report</h1>

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

    <div v-if="report">
      <v-card class="mb-4">
        <v-card-title>Overall Grade</v-card-title>
        <v-card-text>
          <div class="text-h2 font-weight-bold text-primary">{{ report.grade?.toFixed(1) }}</div>
          <div class="text-body-2 text-medium-emphasis">Based on {{ report.evaluationCount }} evaluations</div>
        </v-card-text>
      </v-card>

      <v-card class="mb-4" v-if="report.criterionAverages?.length">
        <v-card-title>Scores by Criterion</v-card-title>
        <v-card-text>
          <v-list>
            <v-list-item
              v-for="c in report.criterionAverages"
              :key="c.criterionId"
              :title="c.criterionName"
              :subtitle="`Average: ${c.averageScore?.toFixed(2)}`"
            >
              <template #append>
                <v-chip size="small" color="primary">{{ c.averageScore?.toFixed(1) }}</v-chip>
              </template>
            </v-list-item>
          </v-list>
        </v-card-text>
      </v-card>

      <v-card v-if="report.publicComments?.length">
        <v-card-title>Comments from Teammates</v-card-title>
        <v-card-text>
          <v-list>
            <v-list-item
              v-for="(comment, i) in report.publicComments"
              :key="i"
              :title="comment"
              prepend-icon="mdi-comment"
            />
          </v-list>
        </v-card-text>
      </v-card>
    </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getMe, getTeam, getActiveWeeks, getStudentPeerReport } from '@/api'

const auth = useAuthStore()
const loading = ref(true)
const weeks = ref([])
const selectedWeekId = ref(null)
const report = ref(null)

onMounted(async () => {
  try {
    const me = await getMe()
    if (!me.data.teamId) return
    const teamRes = await getTeam(me.data.teamId)
    const weekRes = await getActiveWeeks(teamRes.data.sectionId)
    weeks.value = weekRes.data
      .sort((a, b) => new Date(b.startDate) - new Date(a.startDate))
      .map(w => ({
        id: w.id,
        label: `Week of ${new Date(w.startDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}`,
      }))
    if (weeks.value.length > 0) {
      selectedWeekId.value = weeks.value[0].id
      await loadReport()
    }
  } finally {
    loading.value = false
  }
})

async function loadReport() {
  if (!selectedWeekId.value) return
  const res = await getStudentPeerReport(auth.user.id, selectedWeekId.value)
  report.value = res.data
}
</script>
