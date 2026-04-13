<template>
  <div v-if="instructor">
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" to="/admin/instructors" />
      <h1 class="text-h5 font-weight-bold ml-2">
        {{ instructor.firstName }} {{ instructor.lastName }}
      </h1>
      <v-chip class="ml-3" :color="instructor.enabled ? 'success' : 'default'" size="small">
        {{ instructor.enabled ? 'Active' : 'Inactive' }}
      </v-chip>
    </div>

    <v-card>
      <v-card-title>Profile</v-card-title>
      <v-card-text>
        <v-list>
          <v-list-item title="Email" :subtitle="instructor.email" />
        </v-list>
      </v-card-text>
      <v-card-actions>
        <v-btn v-if="instructor.enabled" color="warning" @click="deactivate">Deactivate</v-btn>
        <v-btn v-else color="success" @click="reactivate">Reactivate</v-btn>
      </v-card-actions>
    </v-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getInstructor, deactivateInstructor, reactivateInstructor } from '@/api'

const route = useRoute()
const instructor = ref(null)

onMounted(async () => {
  const res = await getInstructor(route.params.id)
  instructor.value = res.data
})

async function deactivate() {
  if (confirm('Deactivate this instructor?')) {
    await deactivateInstructor(route.params.id)
    instructor.value.enabled = false
  }
}

async function reactivate() {
  await reactivateInstructor(route.params.id)
  instructor.value.enabled = true
}
</script>
