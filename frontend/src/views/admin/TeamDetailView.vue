<template>
  <div v-if="team">
    <!-- Header -->
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" to="/admin/teams" />
      <h1 class="text-h5 font-weight-bold ml-2">{{ team.name }}</h1>
      <v-chip class="ml-3" size="small">{{ team.sectionName }}</v-chip>
      <v-spacer />
      <v-btn variant="outlined" prepend-icon="mdi-pencil" class="mr-2" @click="openEditDialog">Edit</v-btn>
      <v-btn variant="outlined" color="error" prepend-icon="mdi-delete" @click="deleteDialog = true">Delete</v-btn>
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
                  @click.stop.prevent="promptRemoveStudent(s)" />
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
                  @click.stop="promptRemoveInstructor(i)" />
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

    <!-- Edit Team Dialog -->
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

    <!-- Assign Students — Step 1: Select -->
    <v-dialog v-model="assignStudentsDialog" max-width="500">
      <v-card>
        <v-card-title>Assign Students — Select</v-card-title>
        <v-card-text>
          <p v-if="availableStudents.length === 0" class="text-medium-emphasis">
            No unassigned students available in this section.
          </p>
          <v-select
            v-else
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
          <v-btn color="primary" :disabled="!selectedStudents.length" @click="reviewAssignStudents">
            Review →
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Assign Students — Step 2: Confirm -->
    <v-dialog v-model="confirmStudentsDialog" max-width="500">
      <v-card>
        <v-card-title>Assign Students — Confirm</v-card-title>
        <v-card-text>
          <p class="mb-3">Assign the following students to <strong>{{ team.name }}</strong>?</p>
          <v-list density="compact">
            <v-list-item
              v-for="s in studentsToAssign"
              :key="s.id"
              :title="`${s.firstName} ${s.lastName}`"
              :subtitle="s.email"
              prepend-icon="mdi-account"
            />
          </v-list>
        </v-card-text>
        <v-card-actions>
          <v-btn @click="backToSelectStudents">← Back</v-btn>
          <v-spacer />
          <v-btn @click="confirmStudentsDialog = false">Cancel</v-btn>
          <v-btn color="primary" :loading="assigning" @click="doAssignStudents">Confirm & Assign</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Assign Instructors — Step 1: Select -->
    <v-dialog v-model="assignInstructorsDialog" max-width="500">
      <v-card>
        <v-card-title>Assign Instructors — Select</v-card-title>
        <v-card-text>
          <p v-if="availableInstructors.length === 0" class="text-medium-emphasis">
            No available instructors to assign.
          </p>
          <v-select
            v-else
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
          <v-btn color="secondary" :disabled="!selectedInstructors.length" @click="reviewAssignInstructors">
            Review →
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Assign Instructors — Step 2: Confirm -->
    <v-dialog v-model="confirmInstructorsDialog" max-width="500">
      <v-card>
        <v-card-title>Assign Instructors — Confirm</v-card-title>
        <v-card-text>
          <p class="mb-3">Assign the following instructors to <strong>{{ team.name }}</strong>?</p>
          <v-list density="compact">
            <v-list-item
              v-for="i in instructorsToAssign"
              :key="i.id"
              :title="`${i.firstName} ${i.lastName}`"
              :subtitle="i.email"
              prepend-icon="mdi-account-tie"
            />
          </v-list>
        </v-card-text>
        <v-card-actions>
          <v-btn @click="backToSelectInstructors">← Back</v-btn>
          <v-spacer />
          <v-btn @click="confirmInstructorsDialog = false">Cancel</v-btn>
          <v-btn color="secondary" :loading="assigning" @click="doAssignInstructors">Confirm & Assign</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Remove Student Confirmation -->
    <v-dialog v-model="removeStudentDialog" max-width="420">
      <v-card>
        <v-card-title>Remove Student from Team</v-card-title>
        <v-card-text>
          Remove <strong>{{ studentToRemove?.firstName }} {{ studentToRemove?.lastName }}</strong> from
          <strong>{{ team.name }}</strong>?
          Their WARs and peer evaluations will be kept.
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="removeStudentDialog = false">Cancel</v-btn>
          <v-btn color="error" :loading="removing" @click="doRemoveStudent">Remove</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Remove Instructor Confirmation -->
    <v-dialog v-model="removeInstructorDialog" max-width="420">
      <v-card>
        <v-card-title>Remove Instructor from Team</v-card-title>
        <v-card-text>
          Remove <strong>{{ instructorToRemove?.firstName }} {{ instructorToRemove?.lastName }}</strong> from
          <strong>{{ team.name }}</strong>?
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="removeInstructorDialog = false">Cancel</v-btn>
          <v-btn color="error" :loading="removing" @click="doRemoveInstructor">Remove</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Delete Team Dialog -->
    <v-dialog v-model="deleteDialog" max-width="480">
      <v-card>
        <v-card-title class="text-error">Delete Team</v-card-title>
        <v-card-text>
          <v-alert type="warning" density="compact" class="mb-3">
            This action is <strong>permanent and cannot be undone</strong>.
          </v-alert>
          <p class="mb-2">Deleting <strong>{{ team.name }}</strong> will permanently remove:</p>
          <v-list density="compact" class="mb-2">
            <v-list-item prepend-icon="mdi-account-group">
              All <strong>{{ team.students?.length || 0 }} student(s)</strong> will be unassigned from the team
            </v-list-item>
            <v-list-item prepend-icon="mdi-clipboard-text">
              All <strong>Weekly Activity Reports</strong> submitted by team students
            </v-list-item>
            <v-list-item prepend-icon="mdi-star-circle">
              All <strong>peer evaluations</strong> associated with the team
            </v-list-item>
          </v-list>
          <p>All students and instructors will be notified by email.</p>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="deleteDialog = false">Cancel</v-btn>
          <v-btn color="error" :loading="deleting" @click="doDeleteTeam">Delete Permanently</v-btn>
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
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  getTeam, updateTeam, deleteTeam,
  assignStudents, assignInstructors,
  removeStudentFromTeam, removeInstructorFromTeam,
  getStudents, getInstructors,
} from '@/api'

const route = useRoute()
const router = useRouter()
const team = ref(null)
const notFound = ref(false)

const editDialog = ref(false)
const editFormRef = ref(null)
const editForm = ref({ name: '', description: '', websiteUrl: '' })
const saving = ref(false)

const assignStudentsDialog = ref(false)
const confirmStudentsDialog = ref(false)
const assignInstructorsDialog = ref(false)
const confirmInstructorsDialog = ref(false)
const deleteDialog = ref(false)
const removeStudentDialog = ref(false)
const removeInstructorDialog = ref(false)

const selectedStudents = ref([])
const selectedInstructors = ref([])
const availableStudents = ref([])
const availableInstructors = ref([])
const studentToRemove = ref(null)
const instructorToRemove = ref(null)

const assigning = ref(false)
const removing = ref(false)
const deleting = ref(false)

const snackbar = ref({ show: false, message: '', color: 'success' })

const studentsToAssign = computed(() =>
  availableStudents.value.filter(s => selectedStudents.value.includes(s.id))
)
const instructorsToAssign = computed(() =>
  availableInstructors.value.filter(i => selectedInstructors.value.includes(i.id))
)

onMounted(loadTeam)

async function loadTeam() {
  try {
    const res = await getTeam(route.params.id)
    team.value = res.data
  } catch {
    notFound.value = true
  }
}

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
    notify(`Team "${editForm.value.name}" updated successfully.`)
    await loadTeam()
  } catch (err) {
    notify(err.response?.data?.message || 'Failed to update team.', 'error')
  } finally {
    saving.value = false
  }
}

async function openAssignStudents() {
  const res = await getStudents({ sectionId: team.value.sectionId })
  const currentIds = new Set(team.value.students.map(s => s.id))
  // UC-12: only show students not already on any team
  availableStudents.value = res.data.filter(s => !currentIds.has(s.id) && !s.teamId)
  selectedStudents.value = []
  assignStudentsDialog.value = true
}

function reviewAssignStudents() {
  assignStudentsDialog.value = false
  confirmStudentsDialog.value = true
}

function backToSelectStudents() {
  confirmStudentsDialog.value = false
  assignStudentsDialog.value = true
}

async function doAssignStudents() {
  assigning.value = true
  try {
    await assignStudents(team.value.id, selectedStudents.value)
    confirmStudentsDialog.value = false
    notify('Students assigned successfully.')
    await loadTeam()
  } catch (err) {
    notify(err.response?.data?.message || 'Failed to assign students.', 'error')
  } finally {
    assigning.value = false
  }
}

async function openAssignInstructors() {
  const res = await getInstructors()
  const currentIds = new Set(team.value.instructors.map(i => i.id))
  availableInstructors.value = res.data.filter(i => !currentIds.has(i.id))
  selectedInstructors.value = []
  assignInstructorsDialog.value = true
}

function reviewAssignInstructors() {
  assignInstructorsDialog.value = false
  confirmInstructorsDialog.value = true
}

function backToSelectInstructors() {
  confirmInstructorsDialog.value = false
  assignInstructorsDialog.value = true
}

async function doAssignInstructors() {
  assigning.value = true
  try {
    await assignInstructors(team.value.id, selectedInstructors.value)
    confirmInstructorsDialog.value = false
    notify('Instructors assigned successfully.')
    await loadTeam()
  } catch (err) {
    notify(err.response?.data?.message || 'Failed to assign instructors.', 'error')
  } finally {
    assigning.value = false
  }
}

function promptRemoveStudent(student) {
  studentToRemove.value = student
  removeStudentDialog.value = true
}

async function doRemoveStudent() {
  removing.value = true
  try {
    await removeStudentFromTeam(team.value.id, studentToRemove.value.id)
    removeStudentDialog.value = false
    notify(`${studentToRemove.value.firstName} ${studentToRemove.value.lastName} removed from team.`)
    await loadTeam()
  } catch (err) {
    notify(err.response?.data?.message || 'Failed to remove student.', 'error')
  } finally {
    removing.value = false
  }
}

function promptRemoveInstructor(instructor) {
  instructorToRemove.value = instructor
  removeInstructorDialog.value = true
}

async function doRemoveInstructor() {
  removing.value = true
  try {
    await removeInstructorFromTeam(team.value.id, instructorToRemove.value.id)
    removeInstructorDialog.value = false
    notify(`${instructorToRemove.value.firstName} ${instructorToRemove.value.lastName} removed from team.`)
    await loadTeam()
  } catch (err) {
    notify(err.response?.data?.message || 'Failed to remove instructor.', 'error')
  } finally {
    removing.value = false
  }
}

async function doDeleteTeam() {
  deleting.value = true
  try {
    await deleteTeam(team.value.id)
    router.push('/admin/teams')
  } catch (err) {
    notify(err.response?.data?.message || 'Failed to delete team.', 'error')
    deleting.value = false
  }
}

function notify(message, color = 'success') {
  snackbar.value = { show: true, message, color }
}
</script>