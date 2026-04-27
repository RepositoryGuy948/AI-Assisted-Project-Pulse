<template>
  <div v-if="team">
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" to="/admin/teams" />
      <h1 class="text-h5 font-weight-bold ml-2">{{ team.name }}</h1>
      <v-chip class="ml-3" size="small">{{ team.sectionName }}</v-chip>
      <v-spacer />
      <v-btn variant="outlined" prepend-icon="mdi-pencil" class="mr-2" @click="editDialog = true">Edit</v-btn>
      <v-btn variant="outlined" color="error" prepend-icon="mdi-delete" @click="deleteDialog = true">Delete</v-btn>
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
                  @click.prevent="removeStudent(s.id)" />
              </template>
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
          </v-list>
          <v-card-actions>
            <v-btn prepend-icon="mdi-account-plus" color="secondary" @click="openAssignInstructors">Assign Instructors</v-btn>
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

    <!-- Delete Team Dialog -->
    <v-dialog v-model="deleteDialog" max-width="450">
      <v-card>
        <v-card-title class="text-error">Delete Team</v-card-title>
        <v-card-text>
          <v-alert type="warning" density="compact" class="mb-3">
            This will permanently delete the team and all associated WARs and peer evaluations.
            This action cannot be undone.
          </v-alert>
          <p>Are you sure you want to delete <strong>{{ team.name }}</strong>?</p>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="deleteDialog = false">Cancel</v-btn>
          <v-btn color="error" :loading="deleting" @click="doDeleteTeam">Delete Permanently</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTeam, updateTeam, deleteTeam, assignStudents, assignInstructors,
  removeStudentFromTeam, removeInstructorFromTeam, getStudents, getInstructors } from '@/api'

const route = useRoute()
const router = useRouter()
const team = ref(null)
const assignStudentsDialog = ref(false)
const assignInstructorsDialog = ref(false)
const editDialog = ref(false)
const deleteDialog = ref(false)
const selectedStudents = ref([])
const selectedInstructors = ref([])
const availableStudents = ref([])
const availableInstructors = ref([])
const assigning = ref(false)
const saving = ref(false)
const deleting = ref(false)
const editForm = ref({})

onMounted(async () => {
  await loadTeam()
})

async function loadTeam() {
  const res = await getTeam(route.params.id)
  team.value = res.data
  editForm.value = { name: team.value.name, description: team.value.description, websiteUrl: team.value.websiteUrl }
}

async function openAssignStudents() {
  const res = await getStudents({ sectionId: team.value.sectionId })
  const currentIds = team.value.students.map(s => s.id)
  availableStudents.value = res.data.filter(s => !currentIds.includes(s.id))
  selectedStudents.value = []
  assignStudentsDialog.value = true
}

async function openAssignInstructors() {
  const res = await getInstructors()
  const currentIds = team.value.instructors.map(i => i.id)
  availableInstructors.value = res.data.filter(i => !currentIds.includes(i.id))
  selectedInstructors.value = []
  assignInstructorsDialog.value = true
}

async function doAssignStudents() {
  assigning.value = true
  try {
    await assignStudents(team.value.id, selectedStudents.value)
    assignStudentsDialog.value = false
    await loadTeam()
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
  } finally {
    assigning.value = false
  }
}

async function removeStudent(studentId) {
  await removeStudentFromTeam(team.value.id, studentId)
  await loadTeam()
}

async function removeInstructor(instructorId) {
  await removeInstructorFromTeam(team.value.id, instructorId)
  await loadTeam()
}

async function saveEdit() {
  saving.value = true
  try {
    await updateTeam(team.value.id, { ...editForm.value, sectionId: team.value.sectionId })
    editDialog.value = false
    await loadTeam()
  } finally {
    saving.value = false
  }
}

async function doDeleteTeam() {
  deleting.value = true
  try {
    await deleteTeam(team.value.id)
    router.push('/admin/teams')
  } finally {
    deleting.value = false
  }
}
</script>
