import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// Vuetify
import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import '@mdi/font/css/materialdesignicons.css'

const vuetify = createVuetify({
  components,
  directives,
  theme: {
    defaultTheme: 'light',
    themes: {
      light: {
        colors: {
          primary:    '#7C3AED',
          secondary:  '#A855F7',
          accent:     '#9333EA',
          error:      '#EF4444',
          warning:    '#F59E0B',
          info:       '#8B5CF6',
          success:    '#10B981',
          background: '#F5F3FF',
          surface:    '#FFFFFF',
        },
      },
    },
  },
  defaults: {
    VBtn: { rounded: 'lg' },
    VTextField: { rounded: 'lg' },
    VCard: { rounded: 'lg' },
    VAlert: { rounded: 'lg' },
  },
  icons: {
    defaultSet: 'mdi',
  },
})

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(vuetify)
app.mount('#app')
