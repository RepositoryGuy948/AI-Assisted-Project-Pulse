<template>
  <div v-if="instructor">
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" to="/admin/instructors" />
      <h1 class="text-h5 font-weight-bold ml-2">
        {{ instructor.firstName }} {{ instructor.middleInitial ? instructor.middleInitial + '. ' : '' }}{{ instructor.lastName }}
      </h1>
      <v-chip class="ml-3" :color="instructor.enabled ? 'success' : 'default'" size="small">
        {{ instructor.enabled ? 'Active' : 'Inactive' }}
      </v-chip>
    </div>

    <v-row>
      <v-col cols="12" md="5">
        <v-card class="mb-4">
          <v-card-title>Profile</v-card-title>
          <v-card-text>
            <v-list density="compact">
              <v-list-item title="Email" :subtitle="instructor.email" />
            </v-list>
          </v-card-text>
          <v-card-actions class="pa-4 pt-0">
            <v-btn
              v-if="instructor.enabled"
              color="warning"
              variant="tonal"
              prepend-icon="mdi-account-off"
              @click="deactivateDialog = true"
            >
              Deactivate
            </v-btn>
            <v-btn
              v-else
              color="success"
              variant="tonal"
              prepend-icon="mdi-account-check"
              :loading="reactivating"
              @click="reactivate"
            >
              Reactivate
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>

      <v-col cols="12" md="7">
        <v-card>
          <v-card-title>Supervised Teams</v-card-title>
          <v-card-text>
            <div v-if="instructor.supervisedTeams && instructor.supervisedTeams.length > 0">
              <!-- Group by section -->
              <div v-for="section in groupedTeams" :key="section.name" class="mb-3">
                <div class="text-subtitle-2 font-weight-bold text-medium-emphasis mb-1">
                  {{ section.name }}
                </div>
                <v-list density="compact" border rounded>
                  <v-list-item
                    v-for="team in section.teams"
                    :key="team.id"
                    :title="team.name"
                    :to="`/admin/teams/${team.id}`"
                    prepend-icon="mdi-account-group"
                  />
                </v-list>
              </div>
            </div>
            <p v-else class="text-medium-emphasis">Not assigned to any teams.</p>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Deactivate Dialog -->
    <v-dialog v-model="deactivateDialog" max-width="450">
      <v-card>
        <v-card-title>Deactivate Instructor</v-card-title>
        <v-card-text>
          <p class="text-body-2">
            This instructor will lose access to the system. Their information and team assignments are preserved.
          </p>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="deactivateDialog = false">Cancel</v-btn>
          <v-btn color="warning" :loading="deactivating" @click="deactivate">Deactivate</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getInstructor, deactivateInstructor, reactivateInstructor } from '@/api'

const route = useRoute()
const instructor = ref(null)
const deactivateDialog = ref(false)
const deactivating = ref(false)
const reactivating = ref(false)

const groupedTeams = computed(() => {
  if (!instructor.value?.supervisedTeams) return []
  const map = {}
  instructor.value.supervisedTeams.forEach(t => {
    const sec = t.sectionName || 'Unassigned Section'
    if (!map[sec]) map[sec] = { name: sec, teams: [] }
    map[sec].teams.push(t)
  })
  return Object.values(map).sort((a, b) => b.name.localeCompare(a.name))
})

onMounted(async () => {
  const res = await getInstructor(route.params.id)
  instructor.value = res.data
})

async function deactivate() {
  deactivating.value = true
  try {
    await deactivateInstructor(route.params.id)
    instructor.value.enabled = false
    deactivateDialog.value = false
  } finally {
    deactivating.value = false
  }
}

async function reactivate() {
  reactivating.value = true
  try {
    await reactivateInstructor(route.params.id)
    instructor.value.enabled = true
  } finally {
    reactivating.value = false
  }
}
</script>
