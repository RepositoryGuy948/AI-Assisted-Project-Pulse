<template>
  <div>
    <div class="d-flex align-center mb-6">
      <h1 class="text-h5 font-weight-bold">Rubrics</h1>
      <v-spacer />
      <v-btn color="primary" prepend-icon="mdi-plus" to="/admin/rubrics/new">New Rubric</v-btn>
    </div>

    <v-row>
      <v-col v-for="rubric in rubrics" :key="rubric.id" cols="12" md="6" lg="4">
        <v-card>
          <v-card-title class="d-flex align-center">
            {{ rubric.name }}
            <v-spacer />
            <v-btn
              icon="mdi-pencil"
              variant="text"
              size="small"
              :to="`/admin/rubrics/${rubric.id}/edit`"
            />
          </v-card-title>
          <v-card-text>
            <v-list density="compact">
              <v-list-item
                v-for="c in rubric.criteria"
                :key="c.id"
                :title="c.name"
                :subtitle="`Max: ${c.maxScore} | ${c.description || ''}`"
                prepend-icon="mdi-clipboard-check"
              />
            </v-list>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getRubrics } from '@/api'

const rubrics = ref([])

onMounted(async () => {
  const res = await getRubrics()
  rubrics.value = res.data
})
</script>
