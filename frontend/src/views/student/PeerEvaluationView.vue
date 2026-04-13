<template>
  <div>
    <h1 class="text-h5 font-weight-bold mb-2">Peer Evaluation</h1>
    <p class="text-body-2 text-medium-emphasis mb-6">
      Evaluate all your teammates for the previous week. All fields are required.
    </p>

    <v-alert v-if="!activeWeek" type="info" class="mb-4">
      No active week available for peer evaluation.
    </v-alert>

    <div v-if="activeWeek">
      <v-chip color="primary" class="mb-4">
        Week of {{ formatDate(activeWeek.startDate) }}
      </v-chip>

      <v-alert v-if="error" type="error" class="mb-4" density="compact">{{ error }}</v-alert>
      <v-alert v-if="success" type="success" class="mb-4" density="compact">Evaluations submitted!</v-alert>

      <v-card v-for="teammate in teammates" :key="teammate.id" class="mb-4">
        <v-card-title class="d-flex align-center">
          <v-icon class="mr-2">mdi-account</v-icon>
          {{ teammate.firstName }} {{ teammate.lastName }}
          <v-chip v-if="teammate.id === auth.user.id" size="small" class="ml-2" color="secondary">You</v-chip>
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
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getMe, getTeam, getActiveWeeks, getRubric, getSection, submitPeerEvaluation, getSubmittedEvaluations } from '@/api'

const auth = useAuthStore()
const teammates = ref([])
const criteria = ref([])
const activeWeek = ref(null)
const evaluations = reactive({})
const submittedFor = ref([])
const submitting = ref(false)
const error = ref('')
const success = ref(false)

onMounted(async () => {
  const me = await getMe()
  if (!me.data.teamId) return

  const [teamRes] = await Promise.all([getTeam(me.data.teamId)])
  const team = teamRes.data

  // Get previous active week
  const weekRes = await getActiveWeeks(team.sectionId)
  const now = new Date()
  const activeWeeks = weekRes.data.filter(w => w.active && new Date(w.endDate) < now)
  if (activeWeeks.length === 0) return
  activeWeek.value = activeWeeks[activeWeeks.length - 1]

  // Get team members
  teammates.value = team.students

  // Get rubric criteria
  const sectionRes = await getSection(team.sectionId)
  if (sectionRes.data.rubricId) {
    const rubricRes = await getRubric(sectionRes.data.rubricId)
    criteria.value = rubricRes.data.criteria
  }

  // Check already submitted
  const subRes = await getSubmittedEvaluations(auth.user.id, activeWeek.value.id)
  submittedFor.value = subRes.data.map(e => e.evaluateeId)

  // Initialize evaluation forms
  teammates.value.forEach(tm => {
    const scores = {}
    criteria.value.forEach(c => { scores[c.id] = 0 })
    evaluations[tm.id] = {
      scores,
      publicComment: '',
      privateComment: '',
    }
  })
})

async function submitAll() {
  error.value = ''
  success.value = false
  submitting.value = true
  try {
    const me = await getMe()
    for (const teammate of teammates.value) {
      const evalData = evaluations[teammate.id]
      await submitPeerEvaluation(auth.user.id, {
        evaluateeId: teammate.id,
        weekId: activeWeek.value.id,
        teamId: me.data.teamId,
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
