<template>
  <div>
    <h1 class="text-h5 font-weight-bold mb-2">Invite Students</h1>
    <p class="text-body-2 text-medium-emphasis mb-6">
      Students will receive an email with a unique registration link.
    </p>

    <v-row>
      <!-- Left: input form -->
      <v-col cols="12" md="6">
        <v-card>
          <v-card-text>
            <v-form ref="formRef" validate-on="submit lazy">
              <!-- Section selector -->
              <v-select
                v-model="form.sectionId"
                :items="sections"
                item-title="name"
                item-value="id"
                label="Section *"
                variant="outlined"
                class="mb-4"
                :rules="[v => !!v || 'Section is required']"
                @update:model-value="updatePreview"
              />

              <!-- Email textarea -->
              <v-textarea
                v-model="form.emails"
                label="Email addresses *"
                placeholder="john@tcu.edu; jane@tcu.edu; bob@tcu.edu"
                hint="Separate multiple emails with semicolons ( ; )"
                persistent-hint
                variant="outlined"
                rows="5"
                class="mb-4"
                :rules="[
                  v => !!v?.trim() || 'At least one email is required',
                  v => parsedEmails(v).length > 0 || 'No valid emails found',
                ]"
                @update:model-value="updatePreview"
              />

              <!-- Email count badge -->
              <v-chip
                v-if="emailCount > 0"
                color="primary"
                size="small"
                class="mb-4"
                prepend-icon="mdi-email-multiple"
              >
                {{ emailCount }} recipient{{ emailCount !== 1 ? 's' : '' }}
              </v-chip>

              <!-- Custom message -->
              <v-textarea
                v-model="form.customMessage"
                label="Custom message (optional)"
                placeholder="Add a personal note to include in the invitation email..."
                variant="outlined"
                rows="3"
                class="mb-2"
                @update:model-value="updatePreview"
              />
            </v-form>
          </v-card-text>
          <v-card-actions class="pa-4 pt-0">
            <v-spacer />
            <v-btn variant="text" @click="reset">Clear</v-btn>
            <v-btn
              color="primary"
              prepend-icon="mdi-send"
              :loading="sending"
              @click="sendInvitations"
            >
              Send Invitations
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>

      <!-- Right: email preview panel -->
      <v-col cols="12" md="6">
        <v-card>
          <v-card-title class="d-flex align-center">
            <v-icon class="mr-2">mdi-email-outline</v-icon>
            Email Preview
          </v-card-title>
          <v-divider />
          <v-card-text>
            <div class="text-caption text-medium-emphasis mb-1">Subject</div>
            <div class="mb-4 font-weight-medium">
              Welcome to The Peer Evaluation Tool - Complete Your Registration
            </div>

            <div class="text-caption text-medium-emphasis mb-1">Body</div>
            <v-sheet
              color="grey-lighten-4"
              rounded
              class="pa-4 text-body-2"
              style="white-space: pre-wrap; font-family: monospace;"
            >{{ previewBody }}</v-sheet>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Snackbar -->
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="5000" location="bottom right">
      {{ snackbar.message }}
    </v-snackbar>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getSections, inviteStudents } from '@/api'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const sections = ref([])
const sending = ref(false)
const formRef = ref(null)

const form = ref({
  sectionId: null,
  emails: '',
  customMessage: '',
})

const snackbar = ref({ show: false, message: '', color: 'success' })

// Admin display name for preview
const adminName = computed(() =>
  auth.user ? `${auth.user.firstName} ${auth.user.lastName}`.trim() || 'An administrator' : 'An administrator'
)

// Parse the email textarea into a clean list
function parsedEmails(raw) {
  if (!raw) return []
  return raw.split(';')
    .map(e => e.trim())
    .filter(e => e.length > 0)
}

const emailCount = computed(() => parsedEmails(form.value.emails).length)

// Build the live email body preview
const previewBody = computed(() => {
  const lines = [`Hello,`, ``, `${adminName.value} has invited you to join The Peer Evaluation Tool.`]
  if (form.value.customMessage?.trim()) {
    lines.push('', form.value.customMessage.trim())
  }
  lines.push(
    '',
    'To complete your registration, please use the link below:',
    'http://localhost:5173/register/student?token=<unique-token>',
    '',
    'This link is for one-time use only.',
  )
  return lines.join('\n')
})

// Force re-render of preview (the computed handles this, method kept for clarity)
function updatePreview() {}

onMounted(async () => {
  try {
    const res = await getSections()
    sections.value = res.data
  } catch {
    notify('Failed to load sections.', 'error')
  }
})

async function sendInvitations() {
  const { valid } = await formRef.value.validate()
  if (!valid) return

  sending.value = true
  try {
    const res = await inviteStudents(form.value.sectionId, {
      emails: form.value.emails,
      customMessage: form.value.customMessage || null,
    })
    const count = res.data.count ?? emailCount.value
    notify(`${count} invitation${count !== 1 ? 's' : ''} sent successfully.`, 'success')
    reset()
  } catch (err) {
    const msg = err.response?.data?.message || 'Failed to send invitations.'
    notify(msg, 'error')
  } finally {
    sending.value = false
  }
}

function reset() {
  form.value = { sectionId: null, emails: '', customMessage: '' }
  formRef.value?.reset()
}

function notify(message, color = 'success') {
  snackbar.value = { show: true, message, color }
}
</script>
