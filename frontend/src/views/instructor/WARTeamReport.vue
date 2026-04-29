<template>
  <div>
    <!-- Header -->
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" @click="$router.back()" />
      <div class="ml-2">
        <h1 class="text-h5 font-weight-bold">
          WAR Report — {{ teamName || 'Team' }}
        </h1>
        <p class="text-body-2 text-medium-emphasis mb-0">
          Weekly Activity Reports for all team members
        </p>
      </div>
    </div>

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
          no-data-text="No weeks available."
          @update:model-value="loadReport"
        />
      </v-col>

      <!-- Summary chips -->
      <v-col v-if="selectedWeekId && !loading" cols="12" sm="6" md="8">
        <div class="d-flex gap-4 flex-wrap">
          <v-chip
            prepend-icon="mdi-account-check"
            size="small"
            :color="summaryColor"
            variant="tonal"
          >
            {{ submittedCount }} / {{ totalStudents }} submitted
          </v-chip>
          <v-chip
            prepend-icon="mdi-clock-outline"
            size="small"
            color="primary"
            variant="tonal"
          >
            Total planned: {{ totalPlanned }}h
          </v-chip>
          <v-chip
            prepend-icon="mdi-check-circle-outline"
            size="small"
            color="secondary"
            variant="tonal"
          >
            Total actual: {{ totalActual }}h
          </v-chip>
        </div>
      </v-col>
    </v-row>

    <!-- Non-submitters alert -->
    <v-alert
      v-if="selectedWeekId && !loading && nonSubmitters.length > 0"
      type="warning"
      variant="tonal"
      class="mb-4"
      title="Missing Submissions"
    >
      The following students have not submitted a WAR this week:
      <div class="mt-2 d-flex flex-wrap gap-2">
        <v-chip
          v-for="s in nonSubmitters"
          :key="s.studentId"
          size="small"
          color="warning"
          variant="flat"
        >
          {{ s.studentFirstName }} {{ s.studentLastName }}
        </v-chip>
      </div>
    </v-alert>

    <!-- Activities table -->
    <v-card v-if="selectedWeekId" style="overflow-x:auto">
      <v-data-table
        :headers="headers"
        :items="flatActivities"
        :loading="loading"
        item-value="activityId"
        no-data-text="No activities submitted this week."
        :row-props="rowProps"
      >
        <!-- Student name with missing badge -->
        <template #item.studentName="{ item }">
          <span>{{ item.studentName }}</span>
          <v-chip
            v-if="!item.submitted"
            size="x-small"
            color="warning"
            variant="tonal"
            class="ml-2"
          >
            Not submitted
          </v-chip>
        </template>

        <!-- Category chip -->
        <template #item.category="{ item }">
          <v-chip
            v-if="item.category"
            size="small"
            :color="categoryColor(item.category)"
            variant="tonal"
          >
            {{ categoryLabel(item.category) }}
          </v-chip>
          <span v-else class="text-medium-emphasis">—</span>
        </template>

        <!-- Description -->
        <template #item.description="{ item }">
          <span v-if="item.description" :title="item.description">
            {{ item.description.length > 70 ? item.description.slice(0, 70) + '…' : item.description }}
          </span>
          <span v-else class="text-medium-emphasis">—</span>
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
          <v-chip
            v-if="item.status"
            size="small"
            :color="statusColor(item.status)"
            variant="tonal"
          >
            {{ statusLabel(item.status) }}
          </v-chip>
          <span v-else class="text-medium-emphasis">—</span>
        </template>
      </v-data-table>
    </v-card>

    <div v-else-if="!loadingWeeks && weeks.length === 0" class="text-center text-medium-emphasis mt-8">
      No active weeks have been set up for this team's section yet.
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getTeam, getActiveWeeks, getTeamWARReport } from '@/api'

const route = useRoute()

const teamName      = ref('')
const weeks         = ref([])
const loadingWeeks  = ref(false)
const loading       = ref(false)
const selectedWeekId = ref(null)
const report        = ref([])   // array of { submitted, studentId, studentFirstName, studentLastName, activities[] }

// ── Table headers ──────────────────────────────────────────────────────────────
const headers = [
  { title: 'Student',      key: 'studentName',  sortable: true,  width: '180px' },
  { title: 'Category',     key: 'category',     sortable: true,  width: '160px' },
  { title: 'Description',  key: 'description',  sortable: false               },
  { title: 'Planned (h)',  key: 'plannedHours', sortable: true,  width: '110px' },
  { title: 'Actual (h)',   key: 'actualHours',  sortable: true,  width: '100px' },
  { title: 'Status',       key: 'status',       sortable: true,  width: '150px' },
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
  { value: 'IN_PROGRESS',   label: 'In Progress'  },
  { value: 'UNDER_TESTING', label: 'Under Testing' },
  { value: 'DONE',          label: 'Done'          },
]

// ── Computed ───────────────────────────────────────────────────────────────────
const submittedCount = computed(() => report.value.filter(r => r.submitted).length)
const totalStudents  = computed(() => report.value.length)
const nonSubmitters  = computed(() => report.value.filter(r => !r.submitted))
const summaryColor   = computed(() =>
  submittedCount.value === totalStudents.value && totalStudents.value > 0 ? 'success' : 'warning')

const flatActivities = computed(() => {
  const rows = []
  for (const entry of report.value) {
    const studentName = `${entry.studentFirstName} ${entry.studentLastName}`
    if (!entry.submitted || entry.activities.length === 0) {
      rows.push({
        activityId: `no-sub-${entry.studentId}`,
        studentName,
        submitted: entry.submitted,
        category: null,
        description: null,
        plannedHours: null,
        actualHours: null,
        status: null,
      })
    } else {
      for (const a of entry.activities) {
        rows.push({
          activityId: a.id,
          studentName,
          submitted: true,
          category: a.category,
          description: a.description,
          plannedHours: a.plannedHours,
          actualHours: a.actualHours,
          status: a.status,
        })
      }
    }
  }
  return rows
})

const totalPlanned = computed(() =>
  +flatActivities.value.reduce((s, a) => s + (a.plannedHours ?? 0), 0).toFixed(1))

const totalActual = computed(() =>
  +flatActivities.value.reduce((s, a) => s + (a.actualHours ?? 0), 0).toFixed(1))

// ── Row styling for non-submitters ─────────────────────────────────────────────
function rowProps({ item }) {
  if (!item.submitted) return { class: 'bg-warning-lighten-5' }
  return {}
}

// ── Init ───────────────────────────────────────────────────────────────────────
onMounted(async () => {
  loadingWeeks.value = true
  try {
    const teamRes = await getTeam(route.params.id)
    teamName.value = teamRes.data.name
    const weekRes = await getActiveWeeks(teamRes.data.sectionId)

    // Default to previous completed week (most recent week before today)
    const today = new Date()
    const sorted = weekRes.data
      .map(w => ({ ...w, start: new Date(w.startDate) }))
      .sort((a, b) => b.start - a.start)

    weeks.value = sorted.map(w => ({
      id: w.id,
      label: formatWeekLabel(w),
    }))

    // Pre-select: most recent week whose start is before today
    const prev = sorted.find(w => w.start <= today)
    if (prev) {
      selectedWeekId.value = prev.id
      await loadReport()
    }
  } finally {
    loadingWeeks.value = false
  }
})

async function loadReport() {
  if (!selectedWeekId.value) return
  loading.value = true
  try {
    const res = await getTeamWARReport(route.params.id, selectedWeekId.value)
    report.value = res.data
  } catch {
    report.value = []
  } finally {
    loading.value = false
  }
}

// ── Helpers ────────────────────────────────────────────────────────────────────
function formatWeekLabel(w) {
  const start = new Date(w.startDate)
  const end = new Date(start)
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
</script>
