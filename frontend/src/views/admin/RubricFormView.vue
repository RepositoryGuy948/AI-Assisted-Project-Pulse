<template>
  <div>
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" to="/admin/rubrics" />
      <h1 class="text-h5 font-weight-bold ml-2">New Rubric</h1>
    </div>

    <v-card max-width="700">
      <v-card-text>
        <v-alert v-if="error" type="error" class="mb-4" density="compact">{{ error }}</v-alert>

        <v-text-field v-model="form.name" label="Rubric Name" variant="outlined" class="mb-4" required />

        <h3 class="text-subtitle-1 font-weight-bold mb-3">Criteria</h3>
        <v-card v-for="(c, i) in form.criteria" :key="i" variant="outlined" class="mb-3 pa-3">
          <v-row align="center">
            <v-col cols="4">
              <v-text-field v-model="c.name" label="Criterion Name" variant="outlined" density="compact" />
            </v-col>
            <v-col cols="4">
              <v-text-field v-model="c.description" label="Description" variant="outlined" density="compact" />
            </v-col>
            <v-col cols="2">
              <v-text-field v-model.number="c.maxScore" label="Max Score" type="number" variant="outlined" density="compact" />
            </v-col>
            <v-col cols="2">
              <v-btn icon="mdi-delete" variant="text" color="error" @click="removeCriterion(i)" />
            </v-col>
          </v-row>
        </v-card>

        <v-btn prepend-icon="mdi-plus" variant="tonal" @click="addCriterion" class="mb-4">Add Criterion</v-btn>
      </v-card-text>
      <v-card-actions class="pa-4">
        <v-spacer />
        <v-btn to="/admin/rubrics">Cancel</v-btn>
        <v-btn color="primary" :loading="saving" @click="save">Create Rubric</v-btn>
      </v-card-actions>
    </v-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { createRubric } from '@/api'

const router = useRouter()
const saving = ref(false)
const error = ref('')

const form = ref({
  name: '',
  criteria: [{ name: '', description: '', maxScore: 10 }],
})

function addCriterion() {
  form.value.criteria.push({ name: '', description: '', maxScore: 10 })
}

function removeCriterion(i) {
  form.value.criteria.splice(i, 1)
}

async function save() {
  error.value = ''
  saving.value = true
  try {
    await createRubric(form.value)
    router.push('/admin/rubrics')
  } catch (e) {
    error.value = e.response?.data?.message || 'Failed to create rubric.'
  } finally {
    saving.value = false
  }
}
</script>
