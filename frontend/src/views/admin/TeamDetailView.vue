<template>
  <div v-if="team">
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" to="/admin/teams" />
      <h1 class="text-h5 font-weight-bold ml-2">{{ team.name }}</h1>
      <v-chip class="ml-3" size="small">{{ team.sectionName }}</v-chip>
      <v-spacer />
      <v-btn variant="outlined" prepend-icon="mdi-pencil" @click="editDialog = true" class="mr-2">Edit</v-btn>
      <v-btn variant="outlined" color="error" prepend-icon="mdi-delete" @click="deleteDialog = true">Delete Team</v-btn>
    </div>

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
                  @click.prevent="promptRemoveStudent(s)" />
              </template>
            </v-list-item>
          </v-list>
          <v-card-actions>
            <v-btn prepend-icon="mdi-account-plus" color="primary" :loading="loadingStudents" @click="openAssignStudents">Assign Students</v-btn>
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
                  @click="promptRemoveInstructor(i)" />
              </template>
            </v-list-item>
          </v-list>
          <v-card-actions>
            <v-btn prepend-icon="mdi-account-plus" color="secondary" :loading="loadingInstructors" @click="openAssignInstructors">Assign Instructors</v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

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
          <v-btn color="primary" :disabled="!selectedStudents.length" @click="openConfirmStudents">Review & Assign</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Assign Students Confirmation Dialog -->
    <v-dialog v-model="confirmStudentsDialog" max-width="500">
      <v-card>
        <v-card-title>Confirm Assignment</v-card-title>
        <v-card-text>
          <p class="mb-3">
            Assign the following {{ studentsToConfirm.length }} student(s) to <strong>{{ team.name }}</strong>?
          </p>
          <v-list density="compact">
            <v-list-item
              v-for="s in studentsToConfirm"
              :key="s.id"
              :title="`${s.firstName} ${s.lastName}`"
              :subtitle="s.email"
              prepend-icon="mdi-account"
            />
          </v-list>
          <p class="mt-3 text-caption text-medium-emphasis">Each student will receive a notification email.</p>
        </v-card-text>
        <v-card-actions>
          <v-btn @click="backToSelectStudents">Back</v-btn>
          <v-spacer />
          <v-btn @click="confirmStudentsDialog = false">Cancel</v-btn>
          <v-btn color="primary" :loading="assigning" @click="doAssignStudents">Confirm</v-btn>
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
          <v-btn color="secondary" :disabled="!selectedInstructors.length" @click="openConfirmInstructors">Review & Assign</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Assign Instructors Confirmation Dialog -->
    <v-dialog v-model="confirmInstructorsDialog" max-width="500">
      <v-card>
        <v-card-title>Confirm Assignment</v-card-title>
        <v-card-text>
          <p class="mb-3">
            Assign the following {{ instructorsToConfirm.length }} instructor(s) to <strong>{{ team.name }}</strong>?
          </p>
          <v-list density="compact">
            <v-list-item
              v-for="i in instructorsToConfirm"
              :key="i.id"
              :title="`${i.firstName} ${i.lastName}`"
              :subtitle="i.email"
              prepend-icon="mdi-account-tie"
            />
          </v-list>
          <p class="mt-3 text-caption text-medium-emphasis">Each instructor will receive a notification email.</p>
        </v-card-text>
        <v-card-actions>
          <v-btn @click="backToSelectInstructors">Back</v-btn>
          <v-spacer />
          <v-btn @click="confirmInstructorsDialog = false">Cancel</v-btn>
          <v-btn color="secondary" :loading="assigning" @click="doAssignInstructors">Confirm</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Remove Student Confirmation Dialog -->
    <v-dialog v-model="removeStudentDialog" max-width="450">
      <v-card>
        <v-card-title>Remove Student</v-card-title>
        <v-card-text v-if="studentToRemove">
          <p>
            Remove <strong>{{ studentToRemove.firstName }} {{ studentToRemove.lastName }}</strong>
            from <strong>{{ team.name }}</strong>?
          </p>
          <p class="mt-2 text-caption text-medium-emphasis">
            Their WARs and peer evaluations will be kept. A notification email will be sent.
          </p>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="removeStudentDialog = false">Cancel</v-btn>
          <v-btn color="error" :loading="removing" @click="confirmRemoveStudent">Remove</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Remove Instructor Confirmation Dialog -->
    <v-dialog v-model="removeInstructorDialog" max-width="450">
      <v-card>
        <v-card-title>Remove Instructor</v-card-title>
        <v-card-text v-if="instructorToRemove">
          <p>
            Remove <strong>{{ instructorToRemove.firstName }} {{ instructorToRemove.lastName }}</strong>
            from <strong>{{ team.name }}</strong>?
          </p>
          <p class="mt-2 text-caption text-medium-emphasis">A notification email will be sent.</p>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="removeInstructorDialog = false">Cancel</v-btn>
          <v-btn color="error" :loading="removing" @click="confirmRemoveInstructor">Remove</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Delete Team Dialog -->
    <v-dialog v-model="deleteDialog" max-width="540">
      <v-card>
        <v-card-title class="text-error">Delete Team</v-card-title>
        <v-card-text>
          <v-alert type="warning" variant="tonal" class="mb-4">
            This action is <strong>permanent and cannot be undone</strong>.
          </v-alert>
          <p class="mb-3">The following will be permanently deleted:</p>
          <v-list density="compact" class="mb-3">
            <v-list-item prepend-icon="mdi-account-group">
              <strong>{{ team.students?.length || 0 }} student(s)</strong>
              <span v-if="team.students?.length" class="text-medium-emphasis">
                — {{ team.students.map(s => `${s.firstName} ${s.lastName}`).join(', ') }}
              </span>
            </v-list-item>
            <v-list-item prepend-icon="mdi-account-tie">
              <strong>{{ team.instructors?.length || 0 }} instructor(s)</strong>
              <span v-if="team.instructors?.length" class="text-medium-emphasis">
                — {{ team.instructors.map(i => `${i.firstName} ${i.lastName}`).join(', ') }}
              </span>
            </v-list-item>
            <v-list-item prepend-icon="mdi-clipboard-text">
              All <strong>WARs</strong> and <strong>peer evaluations</strong> associated with this team
            </v-list-item>
          </v-list>
          <p>Students and instructors will be notified by email.</p>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="deleteDialog = false">Cancel</v-btn>
          <v-btn color="error" :loading="deleting" @click="confirmDeleteTeam">Delete Team</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Edit Team Dialog -->
    <v-dialog v-model="editDialog" max-width="500">
      <v-card>
        <v-card-title>Edit Team</v-card-title>
        <v-card-text>
          <v-text-field v-model="editForm.name" label="Name" variant="outlined" class="mb-3" />
          <v-textarea v-model="editForm.description" label="Description" variant="outlined" class="mb-3" />
          <v-text-field v-model="editForm.websiteUrl" label="Website URL" variant="outlined" />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="editDialog = false">Cancel</v-btn>
          <v-btn color="primary" :loading="saving" @click="saveEdit">Save</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" timeout="3000">
      {{ snackbar.message }}
    </v-snackbar>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTeam, updateTeam, deleteTeam, assignStudents, assignInstructors,
  removeStudentFromTeam, removeInstructorFromTeam, getStudents, getInstructors } from '@/api'

const route = useRoute()
const router = useRouter()
const team = ref(null)
const assignStudentsDialog = ref(false)
const confirmStudentsDialog = ref(false)
const assignInstructorsDialog = ref(false)
const confirmInstructorsDialog = ref(false)
const removeStudentDialog = ref(false)
const removeInstructorDialog = ref(false)
const editDialog = ref(false)
const selectedStudents = ref([])
const selectedInstructors = ref([])
const availableStudents = ref([])
const availableInstructors = ref([])
const studentToRemove = ref(null)
const instructorToRemove = ref(null)
const deleteDialog = ref(false)
const assigning = ref(false)
const removing = ref(false)
const saving = ref(false)
const deleting = ref(false)
const loadingStudents = ref(false)
const loadingInstructors = ref(false)
const editForm = ref({})
const snackbar = ref({ show: false, message: '', color: 'success' })

const studentsToConfirm = computed(() =>
  availableStudents.value.filter(s => selectedStudents.value.includes(s.id))
)

const instructorsToConfirm = computed(() =>
  availableInstructors.value.filter(i => selectedInstructors.value.includes(i.id))
)

onMounted(async () => {
  await loadTeam()
})

function notify(message, color = 'success') {
  snackbar.value = { show: true, message, color }
}

async function loadTeam() {
  const res = await getTeam(route.params.id)
  team.value = res.data
  editForm.value = { name: team.value.name, description: team.value.description, websiteUrl: team.value.websiteUrl }
}

async function openAssignStudents() {
  loadingStudents.value = true
  try {
    const res = await getStudents({ sectionId: team.value.sectionId })
    const currentIds = team.value.students.map(s => s.id)
    availableStudents.value = res.data.filter(s => !currentIds.includes(s.id))
    selectedStudents.value = []
    assignStudentsDialog.value = true
  } catch {
    notify('Failed to load students.', 'error')
  } finally {
    loadingStudents.value = false
  }
}

async function openAssignInstructors() {
  loadingInstructors.value = true
  try {
    const res = await getInstructors()
    const currentIds = team.value.instructors.map(i => i.id)
    availableInstructors.value = res.data.filter(i => !currentIds.includes(i.id))
    selectedInstructors.value = []
    assignInstructorsDialog.value = true
  } catch {
    notify('Failed to load instructors.', 'error')
  } finally {
    loadingInstructors.value = false
  }
}

function openConfirmStudents() {
  assignStudentsDialog.value = false
  confirmStudentsDialog.value = true
}

function backToSelectStudents() {
  confirmStudentsDialog.value = false
  assignStudentsDialog.value = true
}

function openConfirmInstructors() {
  assignInstructorsDialog.value = false
  confirmInstructorsDialog.value = true
}

function backToSelectInstructors() {
  confirmInstructorsDialog.value = false
  assignInstructorsDialog.value = true
}

async function doAssignStudents() {
  assigning.value = true
  try {
    await assignStudents(team.value.id, selectedStudents.value)
    confirmStudentsDialog.value = false
    await loadTeam()
    notify('Students assigned successfully.')
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
    confirmInstructorsDialog.value = false
    await loadTeam()
    notify('Instructors assigned successfully.')
  } catch {
    notify('Failed to assign instructors.', 'error')
  } finally {
    assigning.value = false
  }
}

function promptRemoveStudent(student) {
  studentToRemove.value = student
  removeStudentDialog.value = true
}

async function confirmRemoveStudent() {
  removing.value = true
  try {
    await removeStudentFromTeam(team.value.id, studentToRemove.value.id)
    removeStudentDialog.value = false
    await loadTeam()
    notify('Student removed.')
  } catch {
    notify('Failed to remove student.', 'error')
  } finally {
    removing.value = false
  }
}

function promptRemoveInstructor(instructor) {
  if (team.value.instructors.length <= 1) {
    notify('Cannot remove the last instructor from a team.', 'error')
    return
  }
  instructorToRemove.value = instructor
  removeInstructorDialog.value = true
}

async function confirmRemoveInstructor() {
  removing.value = true
  try {
    await removeInstructorFromTeam(team.value.id, instructorToRemove.value.id)
    removeInstructorDialog.value = false
    await loadTeam()
    notify('Instructor removed.')
  } catch {
    notify('Failed to remove instructor.', 'error')
  } finally {
    removing.value = false
  }
}

async function confirmDeleteTeam() {
  deleting.value = true
  try {
    await deleteTeam(team.value.id)
    router.push('/admin/teams')
  } catch {
    notify('Failed to delete team.', 'error')
    deleting.value = false
  }
}

async function saveEdit() {
  saving.value = true
  try {
    await updateTeam(team.value.id, { ...editForm.value, sectionId: team.value.sectionId })
    editDialog.value = false
    await loadTeam()
    notify('Team updated.')
  } catch {
    notify('Failed to update team.', 'error')
  } finally {
    saving.value = false
  }
}
</script>