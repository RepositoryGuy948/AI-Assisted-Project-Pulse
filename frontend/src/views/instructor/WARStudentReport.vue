<template>
  <div>
    <!-- Header -->
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" @click="$router.back()" />
      <div class="ml-2">
        <h1 class="text-h5 font-weight-bold">
          WAR Report — {{ studentName || 'Student' }}
        </h1>
        <p class="text-body-2 text-medium-emphasis mb-0">
          Weekly Activity Reports for a selected date range
        </p>
      </div>
    </div>

    <!-- No team warning -->
    <v-alert v-if="noTeam" type="info" variant="tonal" class="mb-6">
      This student is not assigned to a team and has no activity report data.
    </v-alert>

    <template v-else>
      <!-- Week range selectors + Generate -->
      <v-card class="mb-6 pa-4">
        <v-row align="center">
          <v-col cols="12" sm="4">
            <v-select
              v-model="startWeekId"
              :items="weeks"
              item-title="label"
              item-value="id"
              label="Start Week"
              variant="outlined"
              density="comfortable"
              :loading="loadingWeeks"
              no-data-text="No weeks available."
            />
          </v-col>
          <v-col cols="12" sm="4">
            <v-select
              v-model="endWeekId"
              :items="weeks"
              item-title="label"
              item-value="id"
              label="End Week"
              variant="outlined"
              density="comfortable"
              :loading="loadingWeeks"
              no-data-text="No weeks available."
            />
          </v-col>
          <v-col cols="12" sm="4" class="d-flex align-center">
            <v-btn
              color="primary"
              prepend-icon="mdi-magnify"
              :loading="loading"
              :disabled="!startWeekId || !endWeekId || loadingWeeks"
              @click="generateReport"
            >
              Generate Report
            </v-btn>
          </v-col>
        </v-row>

        <!-- Validation hint -->
        <v-alert
          v-if="rangeError"
          type="warning"
          variant="tonal"
          density="compact"
          class="mt-2"
        >
          {{ rangeError }}
        </v-alert>
      </v-card>

      <!-- Summary chips (after generation) -->
      <div v-if="generated && !loading" class="d-flex gap-4 flex-wrap mb-4">
        <v-chip prepend-icon="mdi-calendar-range" size="small" color="primary" variant="tonal">
          {{ weekGroups.length }} week{{ weekGroups.length !== 1 ? 's' : '' }} in range
        </v-chip>
        <v-chip prepend-icon="mdi-clipboard-check" size="small" color="success" variant="tonal">
          {{ submittedWeeks }} submitted
        </v-chip>
        <v-chip prepend-icon="mdi-clock-outline" size="small" color="secondary" variant="tonal">
          Total planned: {{ totalPlanned }}h
        </v-chip>
        <v-chip prepend-icon="mdi-check-circle-outline" size="small" variant="tonal"
          :color="totalActual >= totalPlanned ? 'success' : 'warning'">
          Total actual: {{ totalActual }}h
        </v-chip>
      </div>

      <!-- Per-week groups -->
      <template v-if="generated && !loading">
        <div
          v-for="group in weekGroups"
          :key="group.weekId"
          class="mb-6"
        >
          <div class="d-flex align-center mb-2">
            <span class="text-subtitle-1 font-weight-bold">
              {{ group.label }}
            </span>
            <v-chip
              v-if="!group.submitted"
              size="x-small"
              color="warning"
              variant="tonal"
              class="ml-3"
            >
              No submission
            </v-chip>
            <template v-else>
              <v-chip size="x-small" color="primary" variant="tonal" class="ml-3">
                {{ group.activities.length }} activit{{ group.activities.length !== 1 ? 'ies' : 'y' }}
              </v-chip>
              <v-chip size="x-small" color="default" variant="tonal" class="ml-2">
                {{ groupPlanned(group) }}h planned / {{ groupActual(group) }}h actual
              </v-chip>
            </template>
          </div>

          <v-card v-if="group.submitted">
            <v-data-table
              :headers="headers"
              :items="group.activities"
              density="compact"
              hide-default-footer
              :items-per-page="-1"
            >
              <!-- Category chip -->
              <template #item.category="{ item }">
                <v-chip size="small" :color="categoryColor(item.category)" variant="tonal">
                  {{ categoryLabel(item.category) }}
                </v-chip>
              </template>

              <!-- Description -->
              <template #item.description="{ item }">
                <span :title="item.description">
                  {{ item.description?.length > 70 ? item.description.slice(0, 70) + '…' : item.description }}
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
            </v-data-table>
          </v-card>

          <v-card v-else>
            <v-card-text class="text-medium-emphasis py-3">
              No activities were submitted for this week.
            </v-card-text>
          </v-card>
        </div>

        <!-- All-empty state -->
        <v-alert
          v-if="weekGroups.length === 0"
          type="info"
          variant="tonal"
        >
          No weeks found in the selected range.
        </v-alert>
      </template>
    </template>

    <!-- Snackbar -->
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="3500" location="bottom right">
      {{ snackbar.message }}
    </v-snackbar>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getStudent, getTeam, getActiveWeeks, getStudentWARReport } from '@/api'

const route = useRoute()

const studentName  = ref('')
const noTeam       = ref(false)
const loadingWeeks = ref(false)
const loading      = ref(false)
const generated    = ref(false)

// weeks sorted most-recent-first (for selectors)
const weeks      = ref([])
// raw week objects (to compute range)
const weeksRaw   = ref([])
const startWeekId = ref(null)
const endWeekId   = ref(null)
const rangeError  = ref('')

// report state: array of { weekId, label, submitted, activities[] }
const weekGroups = ref([])
const snackbar   = ref({ show: false, message: '', color: 'success' })

// ── Table headers ──────────────────────────────────────────────────────────────
const headers = [
  { title: 'Category',    key: 'category',    sortable: true,  width: '160px' },
  { title: 'Description', key: 'description', sortable: false               },
  { title: 'Planned (h)', key: 'plannedHours', sortable: true, width: '110px' },
  { title: 'Actual (h)',  key: 'actualHours',  sortable: true, width: '100px' },
  { title: 'Status',      key: 'status',       sortable: true, width: '150px' },
]

// ── Enums ──────────────────────────────────────────────────────────────────────
const categoryItems = [
  { value: 'DEVELOPMENT',   label: 'Development'   },
  { value: 'TESTING',       label: 'Testing'       },
  { value: 'BUGFIX',        label: 'Bug Fix'       },
  { value: 'COMMUNICATION', label: 'Communication' },
  { value: 'DOCUMENTATION', label: 'Documentation' },
  { value: 'DESIGN',        label: 'Design'        },
  { value: 'PLANNING',      label: 'Planning'      },
  { value: 'LEARNING',      label: 'Learning'      },
  { value: 'DEPLOYMENT',    label: 'Deployment'    },
  { value: 'SUPPORT',       label: 'Support'       },
  { value: 'MISCELLANEOUS', label: 'Miscellaneous' },
]

const statusItems = [
  { value: 'IN_PROGRESS',   label: 'In Progress'   },
  { value: 'UNDER_TESTING', label: 'Under Testing' },
  { value: 'DONE',          label: 'Done'          },
]

// ── Computed summaries ─────────────────────────────────────────────────────────
const submittedWeeks = computed(() => weekGroups.value.filter(g => g.submitted).length)

const totalPlanned = computed(() =>
  +weekGroups.value.flatMap(g => g.activities)
    .reduce((s, a) => s + (a.plannedHours ?? 0), 0).toFixed(1))

const totalActual = computed(() =>
  +weekGroups.value.flatMap(g => g.activities)
    .reduce((s, a) => s + (a.actualHours ?? 0), 0).toFixed(1))

function groupPlanned(group) {
  return +group.activities.reduce((s, a) => s + (a.plannedHours ?? 0), 0).toFixed(1)
}
function groupActual(group) {
  return +group.activities.reduce((s, a) => s + (a.actualHours ?? 0), 0).toFixed(1)
}

// ── Init ───────────────────────────────────────────────────────────────────────
onMounted(async () => {
  loadingWeeks.value = true
  try {
    const sRes = await getStudent(route.params.id)
    const student = sRes.data
    studentName.value = `${student.firstName} ${student.lastName}`

    if (!student.teamId) {
      noTeam.value = true
      return
    }

    const teamRes = await getTeam(student.teamId)
    const weekRes = await getActiveWeeks(teamRes.data.sectionId)

    // Sort most-recent-first for selectors; keep raw for range computation
    weeksRaw.value = weekRes.data
      .map(w => ({ ...w, start: new Date(w.startDate) }))
      .sort((a, b) => b.start - a.start)

    weeks.value = weeksRaw.value.map(w => ({ id: w.id, label: formatWeekLabel(w) }))

    // Default: start = oldest, end = most recent (full history)
    if (weeksRaw.value.length > 0) {
      endWeekId.value   = weeksRaw.value[0].id                              // most recent
      startWeekId.value = weeksRaw.value[weeksRaw.value.length - 1].id      // oldest
    }
  } catch {
    notify('Failed to load student data.', 'error')
  } finally {
    loadingWeeks.value = false
  }
})

// ── Generate ───────────────────────────────────────────────────────────────────
async function generateReport() {
  rangeError.value = ''

  const startIdx = weeksRaw.value.findIndex(w => w.id === startWeekId.value)
  const endIdx   = weeksRaw.value.findIndex(w => w.id === endWeekId.value)

  if (startIdx === -1 || endIdx === -1) return

  // weeksRaw is most-recent-first; "start" should be the earlier date (larger index)
  const lo = Math.min(startIdx, endIdx)
  const hi = Math.max(startIdx, endIdx)
  const rangeWeeks = weeksRaw.value.slice(lo, hi + 1)   // still most-recent-first
  const weekIds = rangeWeeks.map(w => w.id)

  if (weekIds.length > 20) {
    rangeError.value = 'Please select a range of 20 weeks or fewer.'
    return
  }

  loading.value = true
  generated.value = false
  try {
    const res = await getStudentWARReport(route.params.id, weekIds)
    const submittedMap = {}
    res.data.forEach(war => { submittedMap[war.weekId] = war })

    // Build groups in chronological order (oldest first for readability)
    weekGroups.value = [...rangeWeeks].reverse().map(w => {
      const war = submittedMap[w.id]
      return {
        weekId:     w.id,
        label:      formatWeekLabel(w),
        submitted:  !!war,
        activities: war?.activities ?? [],
      }
    })
    generated.value = true
  } catch {
    notify('Failed to load report.', 'error')
  } finally {
    loading.value = false
  }
}

// ── Helpers ────────────────────────────────────────────────────────────────────
function formatWeekLabel(w) {
  const start = new Date(w.startDate)
  const end   = new Date(start)
  end.setDate(end.getDate() + 6)
  const fmt = d => d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
  const tag = !w.active ? ' (inactive)' : ''
  return `${fmt(start)} – ${fmt(end)}${tag}`
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
