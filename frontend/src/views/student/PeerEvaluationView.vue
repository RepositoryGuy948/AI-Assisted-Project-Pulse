<template>
  <div>
    <div :style="{ opacity: loading ? 0 : 1, transition: 'opacity 0.3s ease' }">
    <h1 class="text-h5 font-weight-bold mb-2">Peer Evaluation</h1>
    <p class="text-body-2 text-medium-emphasis mb-6">
      Evaluate all your teammates for the previous week. All fields are required.
    </p>
    <v-alert v-if="loadError" type="error" class="mb-4">{{ loadError }}</v-alert>

    <v-alert v-if="!loadError && !activeWeek" type="info" class="mb-4">
      No active week available for peer evaluation.
    </v-alert>

    <div v-if="activeWeek">
      <v-chip color="primary" class="mb-4">
        Week of {{ formatDate(activeWeek.startDate) }}
      </v-chip>

      <v-alert v-if="error" type="error" class="mb-4" density="compact">{{ error }}</v-alert>
      <v-alert v-if="success" type="success" class="mb-4" density="compact">Evaluations submitted!</v-alert>

      <v-alert v-if="teammates.length === 0" type="info" density="compact" class="mb-4">
        No teammates found. Make sure you are assigned to a team with other members.
      </v-alert>

      <template v-for="teammate in teammates" :key="teammate.id">
      <v-card v-if="evaluations[teammate.id]" class="mb-4">
        <v-card-title class="d-flex align-center">
          <v-icon class="mr-2">mdi-account</v-icon>
          {{ teammate.firstName }} {{ teammate.lastName }}
          <v-chip v-if="submittedFor.includes(teammate.id)" size="small" class="ml-2" color="success">Submitted</v-chip>
        </v-card-title>
        <v-card-text>
          <div v-if="criteria.length">
            <v-row v-for="c in criteria" :key="c.id" align="center" class="mb-2">
              <v-col cols="3">
                <span class="text-body-2">{{ c.name }}</span>
                <div class="text-caption text-medium-emphasis">Max: {{ c.maxScore }}</div>
              </v-col>
              <v-col cols="9">
                <v-slider
                  v-model="evaluations[teammate.id].scores[c.id]"
                  :min="0"
                  :max="c.maxScore"
                  step="1"
                  thumb-label
                  color="primary"
                  track-color="grey-lighten-2"
                />
              </v-col>
            </v-row>
          </div>
          <v-textarea
            v-model="evaluations[teammate.id].publicComment"
            label="Public Comment (visible to teammate)"
            variant="outlined"
            rows="2"
            class="mt-3"
          />
          <v-textarea
            v-model="evaluations[teammate.id].privateComment"
            label="Private Comment (instructor only)"
            variant="outlined"
            rows="2"
            class="mt-2"
          />
        </v-card-text>
      </v-card>
      </template>

      <v-btn
        v-if="teammates.length > 0"
        color="primary"
        size="large"
        :loading="submitting"
        @click="submitAll"
      >
        Submit All Evaluations
      </v-btn>
    </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { getMe, getTeam, getActiveWeeks, getSection, submitPeerEvaluation, getSubmittedEvaluations } from '@/api'

const teammates = ref([])
const criteria = ref([])
const activeWeek = ref(null)
const currentUser = ref(null)
const evaluations = reactive({})
const submittedFor = ref([])
const submitting = ref(false)
const loading = ref(true)
const loadError = ref('')
const error = ref('')
const success = ref(false)

onMounted(async () => {
  try {
    const me = await getMe()
    currentUser.value = me.data

    if (!me.data.teamId || !me.data.sectionId) return

    const teamRes = await getTeam(me.data.teamId)
    teammates.value = teamRes.data.students.filter(s => s.id !== me.data.id)

    const weekRes = await getActiveWeeks(teamRes.data.sectionId)
    const weeks = weekRes.data.sort((a, b) => new Date(b.startDate) - new Date(a.startDate))
    const today = new Date()
    const current = weeks.find(w => new Date(w.startDate) <= today && new Date(w.endDate) >= today)
    const mostRecentPast = weeks.find(w => new Date(w.endDate) < today)
    activeWeek.value = current || mostRecentPast || weeks[0] || null

    if (!activeWeek.value) return

    try {
      const sectionRes = await getSection(me.data.sectionId)
      criteria.value = sectionRes.data.rubricCriteria || []
    } catch {
      criteria.value = []
    }

    try {
      const subRes = await getSubmittedEvaluations(me.data.id, activeWeek.value.id)
      submittedFor.value = subRes.data.map(e => e.evaluateeId)
    } catch { /* non-fatal */ }

    teammates.value.forEach(tm => {
      const scores = {}
      criteria.value.forEach(c => { scores[c.id] = 0 })
      evaluations[tm.id] = { scores, publicComment: '', privateComment: '' }
    })

  } catch (e) {
    console.error('[PeerEval]', e)
    loadError.value = 'Failed to load peer evaluation data.'
  } finally {
    loading.value = false
  }
})

async function submitAll() {
  error.value = ''
  success.value = false
  submitting.value = true
  try {
    for (const teammate of teammates.value) {
      const evalData = evaluations[teammate.id]
      await submitPeerEvaluation(currentUser.value.id, {
        evaluateeId: teammate.id,
        weekId: activeWeek.value.id,
        teamId: currentUser.value.teamId,
        publicComment: evalData.publicComment,
        privateComment: evalData.privateComment,
        scores: criteria.value.map(c => ({
          criterionId: c.id,
          score: evalData.scores[c.id] || 0,
        })),
      })
    }
    success.value = true
    submittedFor.value = teammates.value.map(t => t.id)
  } catch (e) {
    error.value = e.response?.data?.message || 'Submission failed.'
  } finally {
    submitting.value = false
  }
}

function formatDate(d) {
  return new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}
</script>
