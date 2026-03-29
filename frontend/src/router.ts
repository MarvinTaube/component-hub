import { createRouter, createWebHistory } from 'vue-router'

import HomeView from './views/HomeView.vue'
import AboutView from './views/AboutView.vue'
import AddComponentView from './views/AddComponentView.vue'
import ComponentDetailView from './views/ComponentDetailView.vue'

const routes = [
    { path: '/', component: HomeView },
    { path: '/add-component', component: AddComponentView },
    { path: '/component/:id', component: ComponentDetailView },
    { path: '/about', component: AboutView },
]

export const router = createRouter({
    history: createWebHistory(),
    routes,
})