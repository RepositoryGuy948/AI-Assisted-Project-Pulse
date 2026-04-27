<template>
  <div>
    <h1 class="text-h5 font-weight-bold mb-6">My Account</h1>

    <v-card max-width="520">
      <v-card-text class="pa-6">
        <v-form ref="formRef" validate-on="submit lazy">
          <v-text-field
            v-model="form.firstName"
            label="First Name *"
            variant="outlined"
            class="mb-3"
            :rules="[v => !!v?.trim() || 'First name is required']"
          />
          <v-text-field
            v-model="form.lastName"
            label="Last Name *"
            variant="outlined"
            class="mb-3"
            :rules="[v => !!v?.trim() || 'Last name is required']"
          />
          <v-text-field
            v-model="form.email"
            label="Email *"
            type="email"
            variant="outlined"
            :rules="[
              v => !!v?.trim() || 'Email is required',
              v => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v) || 'Enter a valid email address',
            ]"
          />
        </v-form>
      </v-card-text>
      <v-card-actions class="pa-4 pt-0">
        <v-spacer />
        <v-btn variant="text" @click="resetForm">Reset</v-btn>
        <v-btn color="primary" :loading="saving" @click="save">Save Changes</v-btn>
      </v-card-actions>
    </v-card>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="4000" location="bottom right">
      {{ snackbar.message }}
    </v-snackbar>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getMe, updateStudent } from '@/api'

const auth = useAuthStore()
const saving = ref(false)
const formRef = ref(null)
const original = ref({})
const form = ref({ firstName: '', lastName: '', email: '' })
const snackbar = ref({ show: false, message: '', color: 'success' })

onMounted(async () => {
  const res = await getMe()
  original.value = { firstName: res.data.firstName, lastName: res.data.lastName, email: res.data.email }
  form.value = { ...original.value }
})

function resetForm() {
  form.value = { ...original.value }
  formRef.value?.resetValidation()
}

async function save() {
  const { valid } = await formRef.value.validate()
  if (!valid) return

  saving.value = true
  try {
    const res = await updateStudent(auth.user.id, form.value)
    // Keep auth store in sync so the nav bar shows updated name
    auth.setUser({ ...auth.user, firstName: res.data.firstName, lastName: res.data.lastName, email: res.data.email }, auth.token)
    original.value = { firstName: res.data.firstName, lastName: res.data.lastName, email: res.data.email }
    notify('Profile updated successfully.', 'success')
  } catch (e) {
    notify(e.response?.data?.message || 'Update failed.', 'error')
  } finally {
    saving.value = false
  }
}

function notify(message, color = 'success') {
  snackbar.value = { show: true, message, color }
}
</script>
