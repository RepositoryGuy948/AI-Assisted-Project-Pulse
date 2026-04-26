<template>
  <div v-if="team">
    <!-- Header -->
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" to="/admin/teams" />
      <h1 class="text-h5 font-weight-bold ml-2">{{ team.name }}</h1>
      <v-chip class="ml-3" size="small">{{ team.sectionName }}</v-chip>
      <v-spacer />
      <v-btn variant="outlined" prepend-icon="mdi-pencil" @click="openEditDialog">Edit</v-btn>
    </div>

    <!-- Team info card -->
    <v-card class="mb-6">
      <v-card-text>
        <v-row>
          <v-col cols="12" sm="6">
            <div class="text-caption text-medium-emphasis">Description</div>
            <div>{{ team.description || '—' }}</div>
          </v-col>
          <v-col cols="12" sm="6">
            <div class="text-caption text-medium-emphasis">Website</div>
            <a v-if="team.websiteUrl" :href="team.websiteUrl" target="_blank" rel="noopener">
              {{ team.websiteUrl }}
            </a>
            <span v-else>—</span>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <v-row>
      <!-- Students -->
      <v-col cols="12" md="6">
        <v-card>
          <v-card-title>Students ({{ team.students?.length || 0 }})</v-card-title>
          <v-list>
            <v-list-item
              v-for="s in team.students"
              :key="s.id"
              :title="`${s.firstName} ${s.lastName}`"
              :subtitle="s.email"
              :to="`/admin/students/${s.id}`"
            >
              <template #append>
                <v-btn icon="mdi-account-minus" variant="text" size="small" color="error"
                  @click.prevent="removeStudent(s.id)" />
              </template>
            </v-list-item>
            <v-list-item v-if="!team.students?.length">
              <v-list-item-title class="text-medium-emphasis">No students assigned.</v-list-item-title>
            </v-list-item>
          </v-list>
          <v-card-actions>
            <v-btn prepend-icon="mdi-account-plus" color="primary" @click="openAssignStudents">Assign Students</v-btn>
          </v-card-actions>
        </v-card>
      </v-col>

      <!-- Instructors -->
      <v-col cols="12" md="6">
        <v-card>
          <v-card-title>Instructors ({{ team.instructors?.length || 0 }})</v-card-title>
          <v-list>
            <v-list-item
              v-for="i in team.instructors"
              :key="i.id"
              :title="`${i.firstName} ${i.lastName}`"
              :subtitle="i.email"
            >
              <template #append>
                <v-btn icon="mdi-account-minus" variant="text" size="small" color="error"
                  @click="removeInstructor(i.id)" />
              </template>
            </v-list-item>
            <v-list-item v-if="!team.instructors?.length">
              <v-list-item-title class="text-medium-emphasis">No instructors assigned.</v-list-item-title>
            </v-list-item>
          </v-list>
          <v-card-actions>
            <v-btn prepend-icon="mdi-account-plus" color="secondary" @click="openAssignInstructors">Assign Instructors</v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

    <!-- Edit Team Dialog (UC-10) -->
    <v-dialog v-model="editDialog" max-width="520" @after-leave="editFormRef?.resetValidation()">
      <v-card>
        <v-card-title class="pa-4 pb-0">Edit Team</v-card-title>
        <v-card-text class="pt-4">
          <v-form ref="editFormRef" validate-on="submit lazy">
            <v-text-field
              v-model="editForm.name"
              label="Team Name *"
              variant="outlined"
              class="mb-3"
              :rules="[
                v => !!v?.trim() || 'Team name is required',
                v => v?.trim().length <= 100 || 'Max 100 characters',
              ]"
            />
            <v-textarea
              v-model="editForm.description"
              label="Description"
              variant="outlined"
              rows="3"
              class="mb-3"
            />
            <v-text-field
              v-model="editForm.websiteUrl"
              label="Website URL"
              variant="outlined"
              :rules="[
                v => !v || /^https?:\/\/.+/.test(v) || 'Must be a valid URL (https://...)',
              ]"
            />
          </v-form>
        </v-card-text>
        <v-card-actions class="pa-4 pt-0">
          <v-spacer />
          <v-btn variant="text" @click="editDialog = false">Cancel</v-btn>
          <v-btn color="primary" :loading="saving" @click="saveEdit">Save</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Assign Students Dialog -->
    <v-dialog v-model="assignStudentsDialog" max-width="500">
      <v-card>
        <v-card-title>Assign Students</v-card-title>
        <v-card-text>
          <v-select
            v-model="selectedStudents"
            :items="availableStudents"
            :item-title="s => `${s.firstName} ${s.lastName} (${s.email})`"
            item-value="id"
            label="Select students"
            variant="outlined"
            multiple
            chips
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="assignStudentsDialog = false">Cancel</v-btn>
          <v-btn color="primary" :loading="assigning" @click="doAssignStudents">Assign</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Assign Instructors Dialog -->
    <v-dialog v-model="assignInstructorsDialog" max-width="500">
      <v-card>
        <v-card-title>Assign Instructors</v-card-title>
        <v-card-text>
          <v-select
            v-model="selectedInstructors"
            :items="availableInstructors"
            :item-title="i => `${i.firstName} ${i.lastName} (${i.email})`"
            item-value="id"
            label="Select instructors"
            variant="outlined"
            multiple
            chips
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="assignInstructorsDialog = false">Cancel</v-btn>
          <v-btn color="secondary" :loading="assigning" @click="doAssignInstructors">Assign</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Snackbar -->
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="4000" location="bottom right">
      {{ snackbar.message }}
    </v-snackbar>
  </div>

  <div v-else-if="notFound" class="text-center mt-12 text-medium-emphasis">
    Team not found.
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import {
  getTeam, updateTeam,
  assignStudents, assignInstructors,
  removeStudentFromTeam, removeInstructorFromTeam,
  getStudents, getInstructors,
} from '@/api'

const route = useRoute()
const team = ref(null)
const notFound = ref(false)

const editDialog = ref(false)
const editFormRef = ref(null)
const editForm = ref({ name: '', description: '', websiteUrl: '' })
const saving = ref(false)

const assignStudentsDialog = ref(false)
const assignInstructorsDialog = ref(false)
const selectedStudents = ref([])
const selectedInstructors = ref([])
const availableStudents = ref([])
const availableInstructors = ref([])
const assigning = ref(false)

const snackbar = ref({ show: false, message: '', color: 'success' })

onMounted(loadTeam)

async function loadTeam() {
  try {
    const res = await getTeam(route.params.id)
    team.value = res.data
  } catch {
    notFound.value = true
  }
}

// UC-10: open edit dialog pre-filled with current values
function openEditDialog() {
  editForm.value = {
    name: team.value.name,
    description: team.value.description || '',
    websiteUrl: team.value.websiteUrl || '',
  }
  editDialog.value = true
}

async function saveEdit() {
  const { valid } = await editFormRef.value.validate()
  if (!valid) return

  saving.value = true
  try {
    await updateTeam(team.value.id, { ...editForm.value, sectionId: team.value.sectionId })
    editDialog.value = false
    notify(`Team "${editForm.value.name}" updated successfully.`, 'success')
    await loadTeam()
  } catch (err) {
    const msg = err.response?.data?.message || 'Failed to update team.'
    notify(msg, 'error')
  } finally {
    saving.value = false
  }
}

async function openAssignStudents() {
  const res = await getStudents({ sectionId: team.value.sectionId })
  const currentIds = new Set(team.value.students.map(s => s.id))
  availableStudents.value = res.data.filter(s => !currentIds.has(s.id))
  selectedStudents.value = []
  assignStudentsDialog.value = true
}

async function openAssignInstructors() {
  const res = await getInstructors()
  const currentIds = new Set(team.value.instructors.map(i => i.id))
  availableInstructors.value = res.data.filter(i => !currentIds.has(i.id))
  selectedInstructors.value = []
  assignInstructorsDialog.value = true
}

async function doAssignStudents() {
  assigning.value = true
  try {
    await assignStudents(team.value.id, selectedStudents.value)
    assignStudentsDialog.value = false
    await loadTeam()
  } catch {
    notify('Failed to assign students.', 'error')
  } finally {
    assigning.value = false
  }
}

async function doAssignInstructors() {
  assigning.value = true
  try {
    await assignInstructors(team.value.id, selectedInstructors.value)
    assignInstructorsDialog.value = false
    await loadTeam()
  } catch {
    notify('Failed to assign instructors.', 'error')
  } finally {
    assigning.value = false
  }
}

async function removeStudent(studentId) {
  try {
    await removeStudentFromTeam(team.value.id, studentId)
    await loadTeam()
  } catch {
    notify('Failed to remove student.', 'error')
  }
}

async function removeInstructor(instructorId) {
  try {
    await removeInstructorFromTeam(team.value.id, instructorId)
    await loadTeam()
  } catch {
    notify('Failed to remove instructor.', 'error')
  }
}

function notify(message, color = 'success') {
  snackbar.value = { show: true, message, color }
}
</script>
