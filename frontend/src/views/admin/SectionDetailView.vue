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
      <v-col cols="12" md="6">
        <v-card class="mb-4">
          <v-card-title>Details</v-card-title>
          <v-card-text>
            <v-list>
              <v-list-item title="Name" :subtitle="section.name" />
              <v-list-item title="Start Date" :subtitle="section.startDate" />
              <v-list-item title="End Date" :subtitle="section.endDate" />
              <v-list-item title="Rubric" :subtitle="section.rubricName || 'None'" />
            </v-list>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col cols="12" md="6">
        <v-card>
          <v-card-title>Teams ({{ section.teams?.length || 0 }})</v-card-title>
          <v-card-text>
            <v-list v-if="section.teams?.length">
              <v-list-item
                v-for="team in section.teams"
                :key="team.id"
                :title="team.name"
                :to="`/admin/teams/${team.id}`"
                prepend-icon="mdi-account-group"
              />
            </v-list>
            <p v-else class="text-medium-emphasis">No teams yet.</p>
          </v-card-text>
          <v-card-actions>
            <v-btn color="primary" prepend-icon="mdi-plus" @click="createTeamDialog = true">Add Team</v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

    <!-- Invite buttons -->
    <v-row class="mt-2">
      <v-col>
        <v-card>
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
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="inviteDialog = false">Cancel</v-btn>
          <v-btn color="primary" :loading="inviting" @click="sendInvites">Send Invitations</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

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
const createTeamDialog = ref(false)
const creatingTeam = ref(false)
const teamForm = ref({ name: '', description: '', websiteUrl: '' })

onMounted(async () => {
  const res = await getSection(route.params.id)
  section.value = res.data
})

function openInvite(role) {
  inviteRole.value = role
  inviteEmails.value = ''
  inviteMessage.value = ''
  inviteDialog.value = true
}

async function sendInvites() {
  inviting.value = true
  try {
    const data = { emails: inviteEmails.value, customMessage: inviteMessage.value }
    if (inviteRole.value === 'student') {
      await inviteStudents(section.value.id, data)
    } else {
      await inviteInstructors(section.value.id, data)
    }
    inviteDialog.value = false
  } finally {
    inviting.value = false
  }
}

async function saveTeam() {
  creatingTeam.value = true
  try {
    await createTeam({ ...teamForm.value, sectionId: section.value.id })
    createTeamDialog.value = false
    const res = await getSection(route.params.id)
    section.value = res.data
  } finally {
    creatingTeam.value = false
  }
}
</script>
