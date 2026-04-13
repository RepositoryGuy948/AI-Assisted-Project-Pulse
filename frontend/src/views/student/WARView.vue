<template>
  <div>
    <h1 class="text-h5 font-weight-bold mb-6">Weekly Activity Report</h1>

    <v-select
      v-model="selectedWeekId"
      :items="weeks"
      item-title="label"
      item-value="id"
      label="Select Week"
      variant="outlined"
      class="mb-6"
      style="max-width: 400px"
      @update:model-value="loadWAR"
    />

    <div v-if="selectedWeekId">
      <div class="d-flex align-center mb-4">
        <h2 class="text-subtitle-1 font-weight-bold">Activities</h2>
        <v-spacer />
        <v-btn color="primary" prepend-icon="mdi-plus" @click="openAddActivity">Add Activity</v-btn>
      </div>

      <v-card v-if="activities.length === 0" class="mb-4">
        <v-card-text class="text-center text-medium-emphasis py-8">
          No activities yet. Add your first activity.
        </v-card-text>
      </v-card>

      <v-card v-for="activity in activities" :key="activity.id" class="mb-3">
        <v-card-text>
          <div class="d-flex align-center">
            <v-chip size="small" color="primary" class="mr-3">{{ activity.category }}</v-chip>
            <span class="font-weight-medium">{{ activity.description }}</span>
            <v-spacer />
            <v-chip size="small" :color="statusColor(activity.status)" class="mr-2">{{ activity.status }}</v-chip>
            <v-btn icon="mdi-pencil" variant="text" size="small" @click="openEditActivity(activity)" />
            <v-btn icon="mdi-delete" variant="text" size="small" color="error" @click="removeActivity(activity.id)" />
          </div>
          <div class="d-flex mt-2 text-body-2 text-medium-emphasis">
            <span class="mr-4">Planned: {{ activity.plannedHours }}h</span>
            <span>Actual: {{ activity.actualHours }}h</span>
          </div>
        </v-card-text>
      </v-card>
    </div>

    <!-- Activity Dialog -->
    <v-dialog v-model="activityDialog" max-width="550">
      <v-card>
        <v-card-title>{{ editingActivity ? 'Edit Activity' : 'Add Activity' }}</v-card-title>
        <v-card-text>
          <v-select
            v-model="actForm.category"
            :items="categories"
            label="Category"
            variant="outlined"
            class="mb-3"
          />
          <v-textarea
            v-model="actForm.description"
            label="Description"
            variant="outlined"
            rows="3"
            class="mb-3"
          />
          <v-row>
            <v-col cols="6">
              <v-text-field v-model.number="actForm.plannedHours" label="Planned Hours" type="number" variant="outlined" />
            </v-col>
            <v-col cols="6">
              <v-text-field v-model.number="actForm.actualHours" label="Actual Hours" type="number" variant="outlined" />
            </v-col>
          </v-row>
          <v-select
            v-model="actForm.status"
            :items="statuses"
            label="Status"
            variant="outlined"
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="activityDialog = false">Cancel</v-btn>
          <v-btn color="primary" :loading="savingActivity" @click="saveActivity">Save</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getMe, getTeam, getActiveWeeks, getWAR, addActivity, updateActivity, deleteActivity } from '@/api'

const auth = useAuthStore()
const weeks = ref([])
const selectedWeekId = ref(null)
const activities = ref([])
const activityDialog = ref(false)
const savingActivity = ref(false)
const editingActivity = ref(null)

const categories = ['DEVELOPMENT', 'TESTING', 'BUGFIX', 'COMMUNICATION', 'DOCUMENTATION',
  'DESIGN', 'PLANNING', 'LEARNING', 'DEPLOYMENT', 'SUPPORT', 'MISCELLANEOUS']
const statuses = ['IN_PROGRESS', 'UNDER_TESTING', 'DONE']

const actForm = ref({ category: '', description: '', plannedHours: 0, actualHours: 0, status: 'IN_PROGRESS' })

onMounted(async () => {
  const me = await getMe()
  await loadWeeks(me.data)
})

async function loadWeeks(me) {
  if (!me.teamId) return
  try {
    const teamRes = await getTeam(me.teamId)
    const weekRes = await getActiveWeeks(teamRes.data.sectionId)
    const today = new Date()
    weeks.value = weekRes.data
      .filter(w => new Date(w.startDate) <= today)
      .map(w => ({
        id: w.id,
        label: `Week of ${new Date(w.startDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}${w.active ? '' : ' (inactive)'}`,
      }))
      .reverse()
    if (weeks.value.length > 0) {
      selectedWeekId.value = weeks.value[0].id
      await loadWAR()
    }
  } catch (e) {
    console.error(e)
  }
}

async function loadWAR() {
  if (!selectedWeekId.value) return
  const res = await getWAR(auth.user.id, selectedWeekId.value)
  activities.value = res.data.activities || []
}

function openAddActivity() {
  editingActivity.value = null
  actForm.value = { category: 'DEVELOPMENT', description: '', plannedHours: 0, actualHours: 0, status: 'IN_PROGRESS' }
  activityDialog.value = true
}

function openEditActivity(activity) {
  editingActivity.value = activity
  actForm.value = { ...activity }
  activityDialog.value = true
}

async function saveActivity() {
  savingActivity.value = true
  try {
    if (editingActivity.value) {
      await updateActivity(editingActivity.value.id, actForm.value)
    } else {
      await addActivity(auth.user.id, selectedWeekId.value, actForm.value)
    }
    activityDialog.value = false
    await loadWAR()
  } finally {
    savingActivity.value = false
  }
}

async function removeActivity(id) {
  if (confirm('Delete this activity?')) {
    await deleteActivity(id)
    await loadWAR()
  }
}

function statusColor(s) {
  if (s === 'DONE') return 'success'
  if (s === 'UNDER_TESTING') return 'warning'
  return 'info'
}
</script>
