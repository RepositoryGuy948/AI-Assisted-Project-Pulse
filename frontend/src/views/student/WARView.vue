<template>
  <div>
    <h1 class="text-h5 font-weight-bold mb-2">Weekly Activity Report</h1>
    <p class="text-body-2 text-medium-emphasis mb-6">
      Record what you worked on each week. Select a week below to view or update your activities.
    </p>

    <!-- Student has no team -->
    <v-alert v-if="noTeam" type="info" variant="tonal" class="mb-6">
      You are not assigned to a team yet. Contact your instructor or admin.
    </v-alert>

    <template v-else>
      <!-- Week selector -->
      <v-row align="center" class="mb-4">
        <v-col cols="12" sm="6" md="4">
          <v-select
            v-model="selectedWeekId"
            :items="weeks"
            item-title="label"
            item-value="id"
            label="Select Week"
            variant="outlined"
            density="comfortable"
            :loading="loadingWeeks"
            no-data-text="No weeks available yet."
            @update:model-value="loadWAR"
          />
        </v-col>
        <v-col v-if="selectedWeekId" cols="12" sm="6" md="8">
          <div class="d-flex gap-4 flex-wrap">
            <v-chip
              prepend-icon="mdi-clock-outline"
              size="small"
              color="primary"
              variant="tonal"
            >
              Planned: {{ totalPlanned }}h
            </v-chip>
            <v-chip
              prepend-icon="mdi-check-circle-outline"
              size="small"
              :color="totalActual >= totalPlanned ? 'success' : 'warning'"
              variant="tonal"
            >
              Actual: {{ totalActual }}h
            </v-chip>
            <v-chip
              size="small"
              :color="doneCount === activities.length && activities.length > 0 ? 'success' : 'default'"
              variant="tonal"
            >
              {{ doneCount }} / {{ activities.length }} done
            </v-chip>
          </div>
        </v-col>
      </v-row>

      <!-- Activity table -->
      <div v-if="selectedWeekId">
        <div class="d-flex align-center mb-3">
          <span class="text-subtitle-1 font-weight-bold">Activities</span>
          <v-spacer />
          <v-btn color="primary" prepend-icon="mdi-plus" @click="openAdd">
            Add Activity
          </v-btn>
        </div>

        <v-card>
          <v-data-table
            :headers="tableHeaders"
            :items="activities"
            :loading="loadingWAR"
            item-value="id"
            no-data-text="No activities yet. Add your first activity above."
          >
            <!-- Category chip -->
            <template #item.category="{ item }">
              <v-chip size="small" :color="categoryColor(item.category)" variant="tonal">
                {{ categoryLabel(item.category) }}
              </v-chip>
            </template>

            <!-- Description — truncate long text -->
            <template #item.description="{ item }">
              <span :title="item.description">
                {{ item.description.length > 80 ? item.description.slice(0, 80) + '…' : item.description }}
              </span>
            </template>

            <!-- Hours -->
            <template #item.plannedHours="{ item }">
              {{ item.plannedHours ?? '—' }}
            </template>
            <template #item.actualHours="{ item }">
              {{ item.actualHours ?? '—' }}
            </template>

            <!-- Status chip -->
            <template #item.status="{ item }">
              <v-chip size="small" :color="statusColor(item.status)" variant="tonal">
                {{ statusLabel(item.status) }}
              </v-chip>
            </template>

            <!-- Actions -->
            <template #item.actions="{ item }">
              <v-btn icon="mdi-pencil" variant="text" size="small" @click="openEdit(item)" />
              <v-btn icon="mdi-delete" variant="text" size="small" color="error"
                @click="confirmDelete(item)" />
            </template>
          </v-data-table>
        </v-card>
      </div>

      <div v-else-if="!loadingWeeks && weeks.length === 0" class="text-center text-medium-emphasis mt-8">
        No active weeks have been set up for your section yet.
      </div>
    </template>

    <!-- Add / Edit Activity Dialog -->
    <v-dialog v-model="activityDialog" max-width="580" @after-leave="actFormRef?.resetValidation()">
      <v-card>
        <v-card-title class="pa-4 pb-0">
          {{ editingId ? 'Edit Activity' : 'Add Activity' }}
        </v-card-title>
        <v-card-text class="pt-4">
          <v-form ref="actFormRef" validate-on="submit lazy">
            <!-- Category -->
            <v-select
              v-model="actForm.category"
              :items="categoryItems"
              item-title="label"
              item-value="value"
              label="Category *"
              variant="outlined"
              class="mb-3"
              :rules="[v => !!v || 'Category is required']"
            />

            <!-- Description -->
            <v-textarea
              v-model="actForm.description"
              label="Description *"
              variant="outlined"
              rows="3"
              class="mb-3"
              :rules="[v => !!v?.trim() || 'Description is required']"
            />

            <!-- Hours -->
            <v-row>
              <v-col cols="6">
                <v-text-field
                  v-model.number="actForm.plannedHours"
                  label="Planned Hours"
                  type="number"
                  variant="outlined"
                  min="0"
                  step="0.5"
                  :rules="[v => v == null || v >= 0 || 'Must be ≥ 0']"
                />
              </v-col>
              <v-col cols="6">
                <v-text-field
                  v-model.number="actForm.actualHours"
                  label="Actual Hours"
                  type="number"
                  variant="outlined"
                  min="0"
                  step="0.5"
                  :rules="[v => v == null || v >= 0 || 'Must be ≥ 0']"
                />
              </v-col>
            </v-row>

            <!-- Status -->
            <v-select
              v-model="actForm.status"
              :items="statusItems"
              item-title="label"
              item-value="value"
              label="Status *"
              variant="outlined"
              :rules="[v => !!v || 'Status is required']"
            />
          </v-form>
        </v-card-text>
        <v-card-actions class="pa-4 pt-0">
          <v-spacer />
          <v-btn variant="text" @click="activityDialog = false">Cancel</v-btn>
          <v-btn color="primary" :loading="saving" @click="saveActivity">
            {{ editingId ? 'Save Changes' : 'Add Activity' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="deleteDialog" max-width="400">
      <v-card>
        <v-card-title>Delete Activity</v-card-title>
        <v-card-text>
          Delete this <strong>{{ categoryLabel(deletingActivity?.category) }}</strong> activity?
          This cannot be undone.
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="deleteDialog = false">Cancel</v-btn>
          <v-btn color="error" :loading="deleting" @click="doDelete">Delete</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Snackbar -->
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="3500" location="bottom right">
      {{ snackbar.message }}
    </v-snackbar>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getMe, getTeam, getActiveWeeks, getWAR, addActivity, updateActivity, deleteActivity } from '@/api'

const auth = useAuthStore()

// ── State ────────────────────────────────────────────────────────────────────
const noTeam        = ref(false)
const loadingWeeks  = ref(false)
const loadingWAR    = ref(false)
const saving        = ref(false)
const deleting      = ref(false)

const weeks          = ref([])
const selectedWeekId = ref(null)
const activities     = ref([])

const activityDialog  = ref(false)
const deleteDialog    = ref(false)
const actFormRef      = ref(null)
const editingId       = ref(null)
const deletingActivity = ref(null)

const actForm = ref(blankForm())
const snackbar = ref({ show: false, message: '', color: 'success' })

// ── Enums ────────────────────────────────────────────────────────────────────
const categoryItems = [
  { value: 'DEVELOPMENT',    label: 'Development'    },
  { value: 'TESTING',        label: 'Testing'        },
  { value: 'BUGFIX',         label: 'Bug Fix'        },
  { value: 'COMMUNICATION',  label: 'Communication'  },
  { value: 'DOCUMENTATION',  label: 'Documentation'  },
  { value: 'DESIGN',         label: 'Design'         },
  { value: 'PLANNING',       label: 'Planning'       },
  { value: 'LEARNING',       label: 'Learning'       },
  { value: 'DEPLOYMENT',     label: 'Deployment'     },
  { value: 'SUPPORT',        label: 'Support'        },
  { value: 'MISCELLANEOUS',  label: 'Miscellaneous'  },
]

const statusItems = [
  { value: 'IN_PROGRESS',    label: 'In Progress'    },
  { value: 'UNDER_TESTING',  label: 'Under Testing'  },
  { value: 'DONE',           label: 'Done'           },
]

// ── Table headers ─────────────────────────────────────────────────────────────
const tableHeaders = [
  { title: 'Category',      key: 'category',     sortable: true,  width: '160px' },
  { title: 'Description',   key: 'description',  sortable: false               },
  { title: 'Planned (h)',   key: 'plannedHours', sortable: true,  width: '110px' },
  { title: 'Actual (h)',    key: 'actualHours',  sortable: true,  width: '100px' },
  { title: 'Status',        key: 'status',       sortable: true,  width: '150px' },
  { title: '',              key: 'actions',      sortable: false, width: '90px',  align: 'end' },
]

// ── Computed totals ───────────────────────────────────────────────────────────
const totalPlanned = computed(() =>
  +activities.value.reduce((s, a) => s + (a.plannedHours ?? 0), 0).toFixed(1))

const totalActual = computed(() =>
  +activities.value.reduce((s, a) => s + (a.actualHours ?? 0), 0).toFixed(1))

const doneCount = computed(() =>
  activities.value.filter(a => a.status === 'DONE').length)

// ── Init ──────────────────────────────────────────────────────────────────────
onMounted(async () => {
  const res = await getMe()
  if (!res.data.teamId) { noTeam.value = true; return }
  await loadWeeks(res.data.teamId)
})

async function loadWeeks(teamId) {
  loadingWeeks.value = true
  try {
    const teamRes = await getTeam(teamId)
    const weekRes = await getActiveWeeks(teamRes.data.sectionId)
    weeks.value = weekRes.data
      .map(w => ({
        id: w.id,
        startDate: w.startDate,
        active: w.active,
        label: formatWeekLabel(w),
      }))
      .sort((a, b) => new Date(b.startDate) - new Date(a.startDate))
    if (weeks.value.length > 0) {
      selectedWeekId.value = weeks.value[0].id
      await loadWAR()
    }
  } catch {
    notify('Failed to load weeks.', 'error')
  } finally {
    loadingWeeks.value = false
  }
}

async function loadWAR() {
  if (!selectedWeekId.value) return
  loadingWAR.value = true
  try {
    const res = await getWAR(auth.user.id, selectedWeekId.value)
    activities.value = res.data?.activities ?? []
  } catch {
    notify('Failed to load activities.', 'error')
    activities.value = []
  } finally {
    loadingWAR.value = false
  }
}

// ── Add / Edit ────────────────────────────────────────────────────────────────
function openAdd() {
  editingId.value = null
  actForm.value = blankForm()
  activityDialog.value = true
}

function openEdit(activity) {
  editingId.value = activity.id
  actForm.value = {
    category:     activity.category,
    description:  activity.description,
    plannedHours: activity.plannedHours,
    actualHours:  activity.actualHours,
    status:       activity.status,
  }
  activityDialog.value = true
}

async function saveActivity() {
  const { valid } = await actFormRef.value.validate()
  if (!valid) return

  saving.value = true
  try {
    if (editingId.value) {
      await updateActivity(editingId.value, actForm.value)
      notify('Activity updated.', 'success')
    } else {
      await addActivity(auth.user.id, selectedWeekId.value, actForm.value)
      notify('Activity added.', 'success')
    }
    activityDialog.value = false
    await loadWAR()
  } catch (e) {
    notify(e.response?.data?.message || 'Failed to save activity.', 'error')
  } finally {
    saving.value = false
  }
}

// ── Delete ────────────────────────────────────────────────────────────────────
function confirmDelete(activity) {
  deletingActivity.value = activity
  deleteDialog.value = true
}

async function doDelete() {
  deleting.value = true
  try {
    await deleteActivity(deletingActivity.value.id)
    deleteDialog.value = false
    notify('Activity deleted.', 'success')
    await loadWAR()
  } catch (e) {
    notify(e.response?.data?.message || 'Failed to delete activity.', 'error')
  } finally {
    deleting.value = false
  }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
function blankForm() {
  return { category: null, description: '', plannedHours: null, actualHours: null, status: null }
}

function formatWeekLabel(w) {
  const start = new Date(w.startDate)
  const end   = new Date(start)
  end.setDate(end.getDate() + 6)
  const fmt = d => d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
  const inactive = !w.active ? ' (inactive)' : ''
  return `${fmt(start)} – ${fmt(end)}${inactive}`
}

function categoryLabel(val) {
  return categoryItems.find(c => c.value === val)?.label ?? val ?? ''
}

function statusLabel(val) {
  return statusItems.find(s => s.value === val)?.label ?? val ?? ''
}

function categoryColor(val) {
  const map = {
    DEVELOPMENT: 'blue', TESTING: 'purple', BUGFIX: 'red',
    COMMUNICATION: 'teal', DOCUMENTATION: 'brown', DESIGN: 'pink',
    PLANNING: 'indigo', LEARNING: 'cyan', DEPLOYMENT: 'green',
    SUPPORT: 'orange', MISCELLANEOUS: 'grey',
  }
  return map[val] ?? 'default'
}

function statusColor(val) {
  if (val === 'DONE')          return 'success'
  if (val === 'UNDER_TESTING') return 'warning'
  return 'info'
}

function notify(message, color = 'success') {
  snackbar.value = { show: true, message, color }
}
</script>
