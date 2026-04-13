<template>
  <div>
    <h1 class="text-h5 font-weight-bold mb-6">My Account</h1>

    <v-card max-width="500">
      <v-card-text class="pa-6">
        <v-alert v-if="success" type="success" class="mb-4" density="compact">Profile updated.</v-alert>
        <v-alert v-if="error" type="error" class="mb-4" density="compact">{{ error }}</v-alert>

        <v-text-field v-model="form.firstName" label="First Name" variant="outlined" class="mb-3" />
        <v-text-field v-model="form.lastName" label="Last Name" variant="outlined" class="mb-3" />
        <v-text-field v-model="form.email" label="Email" variant="outlined" class="mb-4" />
      </v-card-text>
      <v-card-actions class="pa-4">
        <v-spacer />
        <v-btn color="primary" :loading="saving" @click="save">Save Changes</v-btn>
      </v-card-actions>
    </v-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getMe, updateStudent } from '@/api'

const auth = useAuthStore()
const saving = ref(false)
const success = ref(false)
const error = ref('')
const form = ref({ firstName: '', lastName: '', email: '' })

onMounted(async () => {
  const res = await getMe()
  form.value = { firstName: res.data.firstName, lastName: res.data.lastName, email: res.data.email }
})

async function save() {
  saving.value = true
  success.value = false
  error.value = ''
  try {
    await updateStudent(auth.user.id, form.value)
    success.value = true
  } catch (e) {
    error.value = e.response?.data?.message || 'Update failed.'
  } finally {
    saving.value = false
  }
}
</script>
