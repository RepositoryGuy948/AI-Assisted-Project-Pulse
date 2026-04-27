<template>
  <div v-if="section">
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" to="/admin/sections" />
      <h1 class="text-h5 font-weight-bold ml-2">Section: {{ section.name }}</h1>
      <v-spacer />
      <v-btn variant="outlined" prepend-icon="mdi-calendar" :to="`/admin/sections/${section.id}/active-weeks`">
        Active Weeks
      </v-btn>
    </div>

    <v-row>
      <!-- Section details -->
      <v-col cols="12" md="4">
        <v-card class="mb-4">
          <v-card-title>Details</v-card-title>
          <v-card-text>
            <v-list density="compact">
              <v-list-item title="Name" :subtitle="section.name" />
              <v-list-item title="Start Date" :subtitle="section.startDate" />
              <v-list-item title="End Date" :subtitle="section.endDate" />
              <v-list-item title="Rubric" :subtitle="section.rubricName || 'None'" />
            </v-list>
          </v-card-text>
        </v-card>

        <!-- Rubric criteria -->
        <v-card v-if="section.rubricCriteria && section.rubricCriteria.length" class="mb-4">
          <v-card-title>Rubric Criteria</v-card-title>
          <v-card-text>
            <v-list density="compact">
              <v-list-item
                v-for="c in section.rubricCriteria"
                :key="c.id"
                :title="c.name"
                :subtitle="`${c.description} | Max: ${c.maxScore}`"
                prepend-icon="mdi-clipboard-check"
              />
            </v-list>
          </v-card-text>
        </v-card>

        <!-- Unassigned students -->
        <v-card v-if="section.unassignedStudents && section.unassignedStudents.length" class="mb-4">
          <v-card-title>
            Students Not on a Team
            <v-chip size="small" color="warning" class="ml-2">{{ section.unassignedStudents.length }}</v-chip>
          </v-card-title>
          <v-card-text>
            <v-list density="compact">
              <v-list-item
                v-for="s in section.unassignedStudents"
                :key="s.id"
                :title="`${s.firstName} ${s.lastName}`"
                :subtitle="s.email"
                prepend-icon="mdi-account-alert"
              />
            </v-list>
          </v-card-text>
        </v-card>
      </v-col>

      <!-- Teams with members -->
      <v-col cols="12" md="8">
        <div class="d-flex align-center mb-3">
          <span class="text-subtitle-1 font-weight-bold">Teams ({{ section.teams?.length || 0 }})</span>
          <v-spacer />
          <v-btn color="primary" size="small" prepend-icon="mdi-plus" @click="createTeamDialog = true">
            Add Team
          </v-btn>
        </div>

        <v-card v-if="!section.teams?.length" class="mb-4">
          <v-card-text class="text-medium-emphasis text-center py-6">No teams yet.</v-card-text>
        </v-card>

        <v-card v-for="team in section.teams" :key="team.id" class="mb-3">
          <v-card-title class="d-flex align-center">
            <v-icon class="mr-2">mdi-account-group</v-icon>
            <router-link :to="`/admin/teams/${team.id}`" class="text-decoration-none">
              {{ team.name }}
            </router-link>
          </v-card-title>
          <v-card-text>
            <v-row>
              <v-col cols="6">
                <div class="text-caption font-weight-bold mb-1 text-medium-emphasis">STUDENTS</div>
                <v-chip
                  v-for="s in team.students"
                  :key="s.id"
                  size="small"
                  class="mr-1 mb-1"
                  prepend-icon="mdi-account"
                >
                  {{ s.firstName }} {{ s.lastName }}
                </v-chip>
                <span v-if="!team.students?.length" class="text-caption text-medium-emphasis">None</span>
              </v-col>
              <v-col cols="6">
                <div class="text-caption font-weight-bold mb-1 text-medium-emphasis">INSTRUCTORS</div>
                <v-chip
                  v-for="i in team.instructors"
                  :key="i.id"
                  size="small"
                  color="secondary"
                  class="mr-1 mb-1"
                  prepend-icon="mdi-account-tie"
                >
                  {{ i.firstName }} {{ i.lastName }}
                </v-chip>
                <span v-if="!team.instructors?.length" class="text-caption text-medium-emphasis">None</span>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>

        <!-- Invitations -->
        <v-card class="mt-2">
          <v-card-title>Invitations</v-card-title>
          <v-card-actions class="pa-4">
            <v-btn color="primary" prepend-icon="mdi-email" @click="openInvite('student')">Invite Students</v-btn>
            <v-btn color="secondary" prepend-icon="mdi-email" class="ml-2" @click="openInvite('instructor')">Invite Instructors</v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

    <!-- Invite Dialog -->
    <v-dialog v-model="inviteDialog" max-width="500">
      <v-card>
        <v-card-title>Invite {{ inviteRole === 'student' ? 'Students' : 'Instructors' }}</v-card-title>
        <v-card-text>
          <v-alert v-if="inviteError" type="error" density="compact" class="mb-3" closable @click:close="inviteError = ''">
            {{ inviteError }}
          </v-alert>
          <v-textarea
            v-model="inviteEmails"
            label="Emails (semicolon separated)"
            placeholder="john.doe@tcu.edu; f.smith@tcu.edu"
            variant="outlined"
            rows="3"
          />
          <v-textarea
            v-model="inviteMessage"
            label="Custom message (optional)"
            variant="outlined"
            rows="3"
            class="mt-3"
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="inviteDialog = false">Cancel</v-btn>
          <v-btn color="primary" :loading="inviting" @click="sendInvites">Send Invitations</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar v-model="inviteSuccess" color="success" timeout="3000" location="bottom end">
      Invitations sent successfully.
      <template #actions>
        <v-btn variant="text" @click="inviteSuccess = false">Dismiss</v-btn>
      </template>
    </v-snackbar>

    <!-- Create Team Dialog -->
    <v-dialog v-model="createTeamDialog" max-width="500">
      <v-card>
        <v-card-title>New Team</v-card-title>
        <v-card-text>
          <v-text-field v-model="teamForm.name" label="Team Name" variant="outlined" class="mb-3" />
          <v-textarea v-model="teamForm.description" label="Description" variant="outlined" class="mb-3" />
          <v-text-field v-model="teamForm.websiteUrl" label="Website URL" variant="outlined" />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="createTeamDialog = false">Cancel</v-btn>
          <v-btn color="primary" :loading="creatingTeam" @click="saveTeam">Create</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getSection, inviteStudents, inviteInstructors, createTeam } from '@/api'

const route = useRoute()
const section = ref(null)
const inviteDialog = ref(false)
const inviteRole = ref('student')
const inviteEmails = ref('')
const inviteMessage = ref('')
const inviting = ref(false)
const inviteError = ref('')
const inviteSuccess = ref(false)
const createTeamDialog = ref(false)
const creatingTeam = ref(false)
const teamForm = ref({ name: '', description: '', websiteUrl: '' })

onMounted(async () => {
  await loadSection()
})

async function loadSection() {
  const res = await getSection(route.params.id)
  section.value = res.data
}

function openInvite(role) {
  inviteRole.value = role
  inviteEmails.value = ''
  inviteMessage.value = ''
  inviteError.value = ''
  inviteDialog.value = true
}

async function sendInvites() {
  inviting.value = true
  inviteError.value = ''
  try {
    const data = { emails: inviteEmails.value, customMessage: inviteMessage.value }
    if (inviteRole.value === 'student') {
      await inviteStudents(section.value.id, data)
    } else {
      await inviteInstructors(section.value.id, data)
    }
    inviteDialog.value = false
    inviteSuccess.value = true
  } catch (e) {
    inviteError.value = e.response?.data?.message || 'Failed to send invitations. Please try again.'
  } finally {
    inviting.value = false
  }
}

async function saveTeam() {
  creatingTeam.value = true
  try {
    await createTeam({ ...teamForm.value, sectionId: section.value.id })
    createTeamDialog.value = false
    await loadSection()
  } finally {
    creatingTeam.value = false
  }
}
</script>
