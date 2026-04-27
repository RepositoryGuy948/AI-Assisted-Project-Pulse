<template>
  <div class="login-page">
    <v-container class="fill-height" fluid>
      <v-row align="center" justify="center" class="fill-height">
        <v-col cols="12" sm="10" md="7" lg="5" xl="4">
          <v-card class="login-card" elevation="0" rounded="xl">
            <v-card-text class="pa-10">
              <div class="text-center mb-8">
                <img
                  :src="tcuLogo"
                  alt="TCU"
                  class="tcu-logo mx-auto mb-4"
                />
                <div class="logo-icon mx-auto mb-4">
                  <v-icon size="36" color="white">mdi-pulse</v-icon>
                </div>
                <h1 class="text-h4 font-weight-bold">Project Pulse</h1>
                <p class="text-body-2 text-medium-emphasis mt-1">Senior Design Management Platform</p>
              </div>

              <v-alert
                v-if="error"
                type="error"
                class="mb-5"
                density="compact"
                variant="tonal"
              >{{ error }}</v-alert>

              <v-form @submit.prevent="handleLogin">
                <v-text-field
                  v-model="email"
                  label="Email address"
                  type="email"
                  prepend-inner-icon="mdi-email-outline"
                  variant="outlined"
                  class="mb-3"
                  :disabled="loading"
                  required
                />
                <v-text-field
                  v-model="password"
                  label="Password"
                  :type="showPassword ? 'text' : 'password'"
                  prepend-inner-icon="mdi-lock-outline"
                  :append-inner-icon="showPassword ? 'mdi-eye-off-outline' : 'mdi-eye-outline'"
                  @click:append-inner="showPassword = !showPassword"
                  variant="outlined"
                  class="mb-6"
                  :disabled="loading"
                  required
                />
                <v-btn
                  type="submit"
                  color="primary"
                  block
                  size="large"
                  :loading="loading"
                  class="sign-in-btn"
                >
                  Sign In
                </v-btn>
              </v-form>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const tcuLogo = '/tcu-logo.png'

const email = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')
const showPassword = ref(false)

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    const user = await auth.login(email.value, password.value)
    if (user.role === 'ADMIN') router.push('/admin')
    else if (user.role === 'INSTRUCTOR') router.push('/instructor')
    else router.push('/student')
  } catch (e) {
    if (!e.response) {
      error.value = 'Unable to reach the server. Please check your connection.'
    } else if (e.response.status === 403) {
      error.value = 'Your account has been deactivated. Contact your administrator.'
    } else {
      error.value = 'Incorrect email or password.'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, #7C3AED 0%, #C4B5FD 60%, #EDE9FE 100%);
  display: flex;
  align-items: center;
  overflow-y: auto;
}

.login-card {
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.22) !important;
}

.logo-icon {
  width: 72px;
  height: 72px;
  border-radius: 20px;
  background: linear-gradient(135deg, #6D28D9 0%, #9333EA 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12px 32px rgba(124, 58, 237, 0.5);
}

.tcu-logo {
  display: block;
  height: 80px;
  width: auto;
  object-fit: contain;
}

.sign-in-btn {
  height: 52px;
  font-size: 1rem;
  font-weight: 600;
  letter-spacing: 0.02em;
}
</style>
