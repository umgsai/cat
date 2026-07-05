import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

import App from './App.vue'
import { initTheme } from './theme'
import './styles.css'

initTheme()
createApp(App).use(ElementPlus).mount('#app')
