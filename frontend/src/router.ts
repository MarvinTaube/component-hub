import {createRouter, createWebHistory} from 'vue-router'

import HomeView from './views/HomeView.vue'
import AboutView from './views/AboutView.vue'
import AddComponentView from './views/AddComponentView.vue'

const routes = [
    { path: '/', component: HomeView },
    { path: '/add-component', component: AddComponentView },
    { path: '/about', component: AboutView },
]

export const router = createRouter({
    history: createWebHistory(),
    routes,
})