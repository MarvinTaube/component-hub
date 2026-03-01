<script setup lang="ts">
import { RouterView } from 'vue-router'
import Menubar from 'primevue/menubar';
import { ref } from 'vue';

const items = ref([
    {
        label: 'Dashboard',
        icon: 'pi pi-chart-bar',
        to: '/'
    },
    {
        label: 'Inventory',
        icon: 'pi pi-box',
        to: '/inventory'
    },
    {
        label: 'Projects',
        icon: 'pi pi-folder',
        to: '/projects'
    },
    {
        label: 'Organization',
        icon: 'pi pi-sitemap',
        items: [
            { label: 'Storage (Drawers)', icon: 'pi pi-database', to: '/storage' },
            { label: 'Categories', icon: 'pi pi-tag', to: '/categories' },
            { label: 'Tags', icon: 'pi pi-hashtag', to: '/tags' }
        ]
    }
]);
</script>

<template>
  <div class="layout-wrapper">
    <Menubar :model="items" class="main-menubar">
      <template #start>
        <span class="text-xl font-bold px-4">Component Hub</span>
      </template>
      <template #item="{ item, props, hasSubmenu }">
        <router-link v-if="item.to" v-bind="props.action" :to="item.to" class="flex align-items-center">
            <span :class="item.icon" />
            <span class="ml-2">{{ item.label }}</span>
        </router-link>
        <a v-else v-bind="props.action" class="flex align-items-center" :target="item.target">
            <span :class="item.icon" />
            <span class="ml-2">{{ item.label }}</span>
            <span v-if="hasSubmenu" class="pi pi-fw pi-angle-down ml-2" />
        </a>
      </template>
    </Menubar>

    <main class="p-4">
      <RouterView />
    </main>
  </div>
</template>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  background-color: #f8f9fa;
  color: #333;
}

.layout-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-menubar {
  border-radius: 0 !important;
  border-top: none;
  border-left: none;
  border-right: none;
  padding: 0.5rem 1rem;
}

.text-xl { font-size: 1.25rem; }
.font-bold { font-weight: 700; }
.px-4 { padding-left: 1rem; padding-right: 1rem; }
.ml-2 { margin-left: 0.5rem; }
.p-4 { padding: 1rem; }
.flex { display: flex; }
.align-items-center { align-items: center; }
</style>
