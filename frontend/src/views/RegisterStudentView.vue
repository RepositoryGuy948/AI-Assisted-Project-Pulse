<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="6" lg="5">

        <!-- Loading state -->
        <div v-if="state === 'loading'" class="text-center">
          <v-progress-circular indeterminate color="primary" size="48" />
          <p class="mt-4 text-medium-emphasis">Validating your registration link…</p>
        </div>

        <!-- Token already used -->
        <v-card v-else-if="state === 'used'" elevation="8" rounded="lg">
          <v-card-text class="pa-8 text-center">
            <v-icon size="56" color="info" class="mb-4">mdi-account-check</v-icon>
            <h1 class="text-h5 font-weight-bold mb-2">Already Registered</h1>
            <p class="text-body-2 text-medium-emphasis mb-6">
              This registration link has already been used. If you have an account, please log in.
            </p>
            <v-btn color="primary" block size="large" to="/">Go to Login</v-btn>
          </v-card-text>
        </v-card>

        <!-- Invalid / expired token -->
        <v-card v-else-if="state === 'invalid'" elevation="8" rounded="lg">
          <v-card-text class="pa-8 text-center">
            <v-icon size="56" color="warning" class="mb-4">mdi-link-off</v-icon>
            <h1 class="text-h5 font-weight-bold mb-2">Invalid Registration Link</h1>
            <p class="text-body-2 text-medium-emphasis mb-6">
              This link is invalid or has expired. Please contact your administrator to request a new invitation.
            </p>
            <v-btn variant="outlined" block to="/">Go to Login</v-btn>
          </v-card-text>
        </v-card>

        <!-- Registration form -->
        <v-card v-else-if="state === 'ready'" elevation="8" rounded="lg">
          <v-card-text class="pa-8">
            <div class="text-center mb-6">
              <v-icon size="48" color="primary">mdi-account-plus</v-icon>
              <h1 class="text-h5 font-weight-bold mt-2">Create Your Account</h1>
              <p v-if="sectionName" class="text-body-2 text-medium-emphasis mt-1">
                Section: <strong>{{ sectionName }}</strong>
              </p>
            </div>

            <v-alert v-if="submitError" type="error" class="mb-4" density="compact" closable
              @click:close="submitError = ''">
              {{ submitError }}
            </v-alert>

            <v-form ref="formRef" validate-on="submit lazy" @submit.prevent="handleRegister">
              <v-row class="mb-0">
                <v-col cols="6" class="pb-0">
                  <v-text-field
                    v-model="form.firstName"
                    label="First Name *"
                    variant="outlined"
                    :rules="[v => !!v?.trim() || 'First name is required']"
                  />
                </v-col>
                <v-col cols="6" class="pb-0">
                  <v-text-field
                    v-model="form.lastName"
                    label="Last Name *"
                    variant="outlined"
                    :rules="[v => !!v?.trim() || 'Last name is required']"
                  />
                </v-col>
              </v-row>

              <v-text-field
                v-model="form.email"
                label="Email *"
                type="email"
                variant="outlined"
                class="mb-1"
                :rules="[
                  v => !!v?.trim() || 'Email is required',
                  v => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v) || 'Enter a valid email address',
                ]"
              />

              <v-text-field
                v-model="form.password"
                label="Password *"
                :type="showPassword ? 'text' : 'password'"
                variant="outlined"
                class="mb-1"
                :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
                @click:append-inner="showPassword = !showPassword"
                :rules="[
                  v => !!v || 'Password is required',
                  v => v.length >= 8 || 'Password must be at least 8 characters',
                ]"
              />

              <v-text-field
                v-model="form.confirmPassword"
                label="Confirm Password *"
                :type="showConfirm ? 'text' : 'password'"
                variant="outlined"
                class="mb-5"
                :append-inner-icon="showConfirm ? 'mdi-eye-off' : 'mdi-eye'"
                @click:append-inner="showConfirm = !showConfirm"
                :rules="[
                  v => !!v || 'Please confirm your password',
                  v => v === form.password || 'Passwords do not match',
                ]"
              />

              <v-btn
                type="submit"
                color="primary"
                block
                size="large"
                :loading="submitting"
              >
                Create Account
              </v-btn>
            </v-form>
          </v-card-text>
        </v-card>

        <!-- Success -->
        <v-card v-else-if="state === 'success'" elevation="8" rounded="lg">
          <v-card-text class="pa-8 text-center">
            <v-icon size="56" color="success" class="mb-4">mdi-check-circle</v-icon>
            <h1 class="text-h5 font-weight-bold mb-2">Account Created!</h1>
            <p class="text-body-2 text-medium-emphasis mb-6">
              Welcome, {{ registeredName }}. You are being redirected to your dashboard…
            </p>
            <v-progress-linear indeterminate color="success" rounded />
          </v-card-text>
        </v-card>

      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { validateToken, registerStudent } from '@/api'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

// 'loading' | 'ready' | 'used' | 'invalid' | 'success'
const state = ref('loading')
const sectionName = ref('')
const submitError = ref('')
const submitting = ref(false)
const showPassword = ref(false)
const showConfirm = ref(false)
const formRef = ref(null)
const registeredName = ref('')

const form = ref({
  token: route.query.token || '',
  email: '',
  firstName: '',
  lastName: '',
  password: '',
  confirmPassword: '',
})

onMounted(async () => {
  if (!form.value.token) {
    state.value = 'invalid'
    return
  }
  try {
    const res = await validateToken(form.value.token)
    form.value.email = res.data.email
    sectionName.value = res.data.sectionName
    state.value = 'ready'
  } catch (err) {
    const msg = err.response?.data?.message || ''
    state.value = msg.includes('already been used') ? 'used' : 'invalid'
  }
})

async function handleRegister() {
  const { valid } = await formRef.value.validate()
  if (!valid) return

  submitError.value = ''
  submitting.value = true
  try {
    const res = await registerStudent({
      token: form.value.token,
      email: form.value.email,
      firstName: form.value.firstName,
      lastName: form.value.lastName,
      password: form.value.password,
    })
    // Auto-login: store JWT and user, then redirect
    auth.setUser({
      id: res.data.userId,
      email: res.data.email,
      firstName: res.data.firstName,
      lastName: res.data.lastName,
      role: res.data.role,
    }, res.data.token)
    registeredName.value = res.data.firstName
    state.value = 'success'
    setTimeout(() => router.push('/student/dashboard'), 1500)
  } catch (e) {
    submitError.value = e.response?.data?.message || 'Registration failed. Please try again.'
  } finally {
    submitting.value = false
  }
}
</script>
