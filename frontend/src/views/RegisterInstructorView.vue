<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="6" lg="5">
        <v-card elevation="8" rounded="lg">
          <v-card-text class="pa-8">
            <div class="text-center mb-6">
              <v-icon size="48" color="secondary">mdi-account-tie</v-icon>
              <h1 class="text-h5 font-weight-bold mt-2">Instructor Registration</h1>
              <p v-if="sectionName" class="text-body-2 text-medium-emphasis">Section: {{ sectionName }}</p>
            </div>

            <v-alert v-if="error" type="error" class="mb-4" density="compact">{{ error }}</v-alert>
            <v-alert v-if="!tokenValid && !loading" type="warning" class="mb-4">
              Invalid or expired registration link.
            </v-alert>

            <v-form v-if="tokenValid" @submit.prevent="handleRegister">
              <v-row>
                <v-col cols="5">
                  <v-text-field v-model="form.firstName" label="First Name" variant="outlined" required />
                </v-col>
                <v-col cols="2">
                  <v-text-field v-model="form.middleInitial" label="MI" variant="outlined" maxlength="1" />
                </v-col>
                <v-col cols="5">
                  <v-text-field v-model="form.lastName" label="Last Name" variant="outlined" required />
                </v-col>
              </v-row>
              <v-text-field
                v-model="form.email"
                label="Email"
                type="email"
                variant="outlined"
                class="mb-3"
                required
              />
              <v-text-field
                v-model="form.password"
                label="Password"
                type="password"
                variant="outlined"
                class="mb-3"
                required
              />
              <v-text-field
                v-model="form.confirmPassword"
                label="Confirm Password"
                type="password"
                variant="outlined"
                class="mb-4"
                required
              />
              <v-btn type="submit" color="secondary" block size="large" :loading="submitting">
                Create Account
              </v-btn>
            </v-form>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { validateToken, registerInstructor } from '@/api'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const loading = ref(true)
const tokenValid = ref(false)
const sectionName = ref('')
const error = ref('')
const submitting = ref(false)

const form = ref({
  token: route.query.token || '',
  email: '',
  firstName: '',
  middleInitial: '',
  lastName: '',
  password: '',
  confirmPassword: '',
})

onMounted(async () => {
  try {
    const res = await validateToken(form.value.token)
    tokenValid.value = true
    form.value.email = res.data.email
    sectionName.value = res.data.sectionName
  } catch {
    tokenValid.value = false
  } finally {
    loading.value = false
  }
})

async function handleRegister() {
  if (form.value.password !== form.value.confirmPassword) {
    error.value = 'Passwords do not match.'
    return
  }
  error.value = ''
  submitting.value = true
  try {
    const res = await registerInstructor(form.value)
    auth.setUser({
      id: res.data.userId,
      email: res.data.email,
      firstName: res.data.firstName,
      lastName: res.data.lastName,
      role: res.data.role,
    }, res.data.token)
    router.push('/instructor/dashboard')
  } catch (e) {
    error.value = e.response?.data?.message || 'Registration failed.'
  } finally {
    submitting.value = false
  }
}
</script>
