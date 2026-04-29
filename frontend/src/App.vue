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

/* ── Card defaults ─────────────────────────────────── */
.v-card {
  border-radius: 12px;
}

/* ── Page content: full width, no centering cap ─────── */
.page-content {
  width: 100%;
}

/* ── Data table headers ────────────────────────────── */
.v-data-table .v-data-table__th {
  background-color: #F5F3FF !important;
  color: #5B21B6 !important;
  font-size: 0.7rem !important;
  font-weight: 700 !important;
  text-transform: uppercase !important;
  letter-spacing: 0.07em !important;
  white-space: nowrap;
}

/* ── Table cards: allow horizontal scroll ──────────── */
/* v-card clips overflow by default; override for data tables */
.v-card .v-table__wrapper {
  overflow-x: auto;
}

/* ── Active nav item: bold title ───────────────────── */
.v-navigation-drawer .v-list-item--active .v-list-item-title {
  font-weight: 700 !important;
}

/* ── Dialog titles ─────────────────────────────────── */
.v-overlay__content > .v-card .v-card-title {
  font-size: 1.05rem !important;
  font-weight: 700 !important;
  color: #1e1b4b !important;
  padding: 20px 24px 8px !important;
}

/* ── Dashboard welcome banner ──────────────────────── */
/* !important needed: Vuetify's .v-card surface background wins otherwise */
.dash-welcome {
  background: linear-gradient(135deg, #4C1D95 0%, #7C3AED 100%) !important;
  border-radius: 16px !important;
}

/* ── Page heading ──────────────────────────────────── */
.page-heading {
  font-size: 1.4rem;
  font-weight: 700;
  color: #1e1b4b;
  letter-spacing: -0.01em;
}
</style>
