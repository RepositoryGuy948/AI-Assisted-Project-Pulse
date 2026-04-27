<template>
  <v-app>
    <router-view />
  </v-app>
</template>

<script setup>
</script>

<style>
html, body, #app {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  overflow-x: hidden;
  background-color: #F5F3FF;
}

.v-application {
  width: 100% !important;
  min-height: 100vh !important;
}

.v-application__wrap {
  min-height: 100vh !important;
  padding: 0 !important;
  margin: 0 !important;
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.15);
  border-radius: 6px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.28);
}

.v-card[style*="cursor:pointer"],
.v-card[style*="cursor: pointer"] {
  transition: box-shadow 0.2s ease, transform 0.2s ease !important;
}

.v-card[style*="cursor:pointer"]:hover,
.v-card[style*="cursor: pointer"]:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.15) !important;
}
</style>
