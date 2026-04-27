<template>
  <div>
    <h1 class="text-h5 font-weight-bold mb-6">Instructor Dashboard</h1>

    <v-row>
      <v-col cols="12" md="4">
        <v-card @click="$router.push('/instructor/teams')" style="cursor:pointer">
          <v-card-text class="text-center pa-10 d-flex flex-column align-center justify-center" style="min-height: 180px">
            <v-icon size="48" color="primary">mdi-account-group</v-icon>
            <div class="text-h6 mt-2">My Teams</div>
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <v-card @click="$router.push('/instructor/reports/peer-evaluation/section')" style="cursor:pointer">
          <v-card-text class="text-center pa-10 d-flex flex-column align-center justify-center" style="min-height: 180px">
            <v-icon size="48" color="secondary">mdi-star-circle</v-icon>
            <div class="text-h6 mt-2">Peer Eval Report</div>
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <v-card @click="$router.push('/instructor/students')" style="cursor:pointer">
          <v-card-text class="text-center pa-10 d-flex flex-column align-center justify-center" style="min-height: 180px">
            <v-icon size="48" color="success">mdi-school</v-icon>
            <div class="text-h6 mt-2">Students</div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>
