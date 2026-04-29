<template>
  <div>
    <h1 class="text-h5 font-weight-bold mb-2">Peer Evaluation</h1>
    <p class="text-body-2 text-medium-emphasis mb-6">
      Evaluate all your teammates for the previous week. All scores are required (1–10).
    </p>

    <!-- Outside-window states -->
    <v-alert v-if="!teamId" type="warning" class="mb-4">
      You must be assigned to a team before you can submit peer evaluations.
    </v-alert>

    <v-alert v-else-if="windowState === 'no-weeks'" type="info" class="mb-4">
      No completed weeks available yet. Peer evaluations open after a week ends.
    </v-alert>

    <v-alert v-else-if="windowState === 'closed'" type="warning" class="mb-4">
      The submission window is closed. You can only submit peer evaluations for the most recently
      completed week, and only before the next week ends.
    </v-alert>

    <div v-if="activeWeek && windowState === 'open'">
      <v-chip color="primary" class="mb-4">
        Evaluating week of {{ formatDate(activeWeek.startDate) }}
      </v-chip>

      <v-alert v-if="error" type="error" class="mb-4" density="compact">{{ error }}</v-alert>
      <v-alert v-if="success" type="success" class="mb-4" density="compact">
        Evaluations submitted successfully!
      </v-alert>

      <v-card v-for="teammate in teammates" :key="teammate.id" class="mb-4">
        <v-card-title class="d-flex align-center">
          <v-icon class="mr-2">mdi-account</v-icon>
          {{ teammate.firstName }} {{ teammate.lastName }}
          <v-chip v-if="teammate.id === auth.user?.id" size="small" class="ml-2" color="secondary">You</v-chip>
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
                  :min="1"
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
            label="Public Comment (visible to teammate — optional)"
            variant="outlined"
            rows="2"
            class="mt-3"
          />
          <v-textarea
            v-model="evaluations[teammate.id].privateComment"
            label="Private Comment (instructor only — optional)"
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
        @click="openReview"
      >
        Review & Submit
      </v-btn>
    </div>

    <!-- Confirmation Review Dialog -->
    <v-dialog v-model="reviewDialog" max-width="680" scrollable>
      <v-card>
        <v-card-title>Review Evaluations — Week of {{ activeWeek ? formatDate(activeWeek.startDate) : '' }}</v-card-title>
        <v-divider />
        <v-card-text style="max-height: 60vh; overflow-y: auto;">
          <v-card
            v-for="teammate in teammates"
            :key="teammate.id"
            class="mb-4"
            variant="outlined"
          >
            <v-card-title class="text-body-1">
              {{ teammate.firstName }} {{ teammate.lastName }}
              <v-chip v-if="teammate.id === auth.user?.id" size="x-small" class="ml-2">You</v-chip>
            </v-card-title>
            <v-card-text class="pb-2">
              <v-row dense v-for="c in criteria" :key="c.id">
                <v-col cols="6" class="text-body-2">{{ c.name }}</v-col>
                <v-col cols="6">
                  <v-chip size="small" color="primary">
                    {{ evaluations[teammate.id]?.scores[c.id] }} / {{ c.maxScore }}
                  </v-chip>
                </v-col>
              </v-row>
              <div v-if="evaluations[teammate.id]?.publicComment" class="mt-2 text-body-2">
                <strong>Public:</strong> {{ evaluations[teammate.id].publicComment }}
              </div>
              <div v-if="evaluations[teammate.id]?.privateComment" class="mt-1 text-body-2">
                <strong>Private (instructor only):</strong> {{ evaluations[teammate.id].privateComment }}
              </div>
            </v-card-text>
          </v-card>
        </v-card-text>
        <v-divider />
        <v-card-actions>
          <v-btn @click="reviewDialog = false">Cancel</v-btn>
          <v-spacer />
          <v-btn color="primary" :loading="submitting" @click="submitAll">Confirm & Submit</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
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
const reviewDialog = ref(false)
const error = ref('')
const success = ref(false)
const teamId = ref(null)
const windowState = ref('no-weeks') // 'no-weeks' | 'open' | 'closed'

onMounted(async () => {
  try {
    const me = await getMe()
    if (!me.data.teamId) return
    teamId.value = me.data.teamId

    const teamRes = await getTeam(me.data.teamId)
    const team = teamRes.data

    const weekRes = await getActiveWeeks(team.sectionId)
    const now = new Date()
    const allActive = weekRes.data.filter(w => w.active)
    const pastWeeks = allActive.filter(w => new Date(w.endDate) < now)
    const currentWeek = allActive.find(w => new Date(w.startDate) <= now && new Date(w.endDate) >= now)

    if (pastWeeks.length === 0) {
      windowState.value = 'no-weeks'
      return
    }

    // Submission window is only open while a current week is running
    if (!currentWeek) {
      windowState.value = 'closed'
      return
    }

    const previousWeek = pastWeeks[pastWeeks.length - 1]

    // Load section/rubric before showing the form
    const sectionRes = await getSection(team.sectionId)
    if (sectionRes.data.rubricId) {
      const rubricRes = await getRubric(sectionRes.data.rubricId)
      criteria.value = rubricRes.data.criteria
    }

    const subRes = await getSubmittedEvaluations(auth.user?.id, previousWeek.id)
    submittedFor.value = subRes.data.map(e => e.evaluateeId)

    const filteredTeammates = team.students.filter(s => s.id !== auth.user?.id)

    // Initialize evaluations for every teammate BEFORE making them visible
    filteredTeammates.forEach(tm => {
      const scores = {}
      criteria.value.forEach(c => { scores[c.id] = 1 })
      evaluations[tm.id] = { scores, publicComment: '', privateComment: '' }
    })

    // Only now expose data to the template — evaluations is fully ready
    activeWeek.value = previousWeek
    teammates.value = filteredTeammates
    windowState.value = 'open'
  } catch (e) {
    error.value = e.response?.data?.message || 'Failed to load evaluation data.'
  }
})

function openReview() {
  error.value = ''
  reviewDialog.value = true
}

async function submitAll() {
  error.value = ''
  success.value = false
  submitting.value = true
  try {
    const me = await getMe()
    for (const teammate of teammates.value) {
      const evalData = evaluations[teammate.id]
      await submitPeerEvaluation(auth.user?.id, {
        evaluateeId: teammate.id,
        weekId: activeWeek.value.id,
        teamId: me.data.teamId,
        publicComment: evalData.publicComment,
        privateComment: evalData.privateComment,
        scores: criteria.value.map(c => ({
          criterionId: c.id,
          score: evalData.scores[c.id] || 1,
        })),
      })
    }
    reviewDialog.value = false
    success.value = true
    submittedFor.value = teammates.value.map(t => t.id)
  } catch (e) {
    error.value = e.response?.data?.message || 'Submission failed.'
    reviewDialog.value = false
  } finally {
    submitting.value = false
  }
}

function formatDate(d) {
  return new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}
</script>