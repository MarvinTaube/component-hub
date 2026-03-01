<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { partService, projectService } from '../services/api';
import Card from 'primevue/card';
import Button from 'primevue/button';
import { useRouter } from 'vue-router';

const router = useRouter();
const parts = ref<any[]>([]);
const projects = ref<any[]>([]);
const loading = ref(true);

onMounted(async () => {
  try {
    const [partsRes, projectsRes] = await Promise.all([
      partService.getAll(),
      projectService.getAll()
    ]);
    parts.value = partsRes.data;
    projects.value = projectsRes.data;
  } catch (err) {
    console.error('Failed to load dashboard data', err);
  } finally {
    loading.value = false;
  }
});

const lowStockParts = computed(() => {
  return parts.value.filter(p => p.stock < 5).slice(0, 5);
});

const recentParts = computed(() => {
  return [...parts.value].reverse().slice(0, 5);
});

const stats = computed(() => [
  { label: 'Total Components', value: parts.value.length, icon: 'pi-box', color: 'text-blue-500' },
  { label: 'Active Projects', value: projects.value.filter(p => !p.finished).length, icon: 'pi-folder', color: 'text-green-500' },
  { label: 'Low Stock Items', value: parts.value.filter(p => p.stock < 5).length, icon: 'pi-exclamation-triangle', color: 'text-orange-500' }
]);

const navigateTo = (routeName: string) => {
  router.push({ name: routeName });
};
</script>

<template>
  <div class="dashboard-container">
    <div class="welcome-section mb-6">
      <h1 class="text-4xl font-light m-0">Welcome back.</h1>
      <p class="text-500 mt-2">Here's what's happening with your components.</p>
    </div>

    <div class="grid mb-6">
      <div v-for="stat in stats" :key="stat.label" class="col-12 md:col-4">
        <Card class="stat-card border-none shadow-1">
          <template #content>
            <div class="flex align-items-center">
              <div :class="['stat-icon p-3 border-round mr-3 bg-faint', stat.color]">
                <i :class="['pi text-2xl', stat.icon]"></i>
              </div>
              <div>
                <span class="block text-500 font-medium mb-1">{{ stat.label }}</span>
                <div class="text-900 font-bold text-2xl">{{ stat.value }}</div>
              </div>
            </div>
          </template>
        </Card>
      </div>
    </div>

    <div class="grid">
      <div class="col-12 lg:col-6">
        <Card class="h-full border-none shadow-1">
          <template #title>
            <div class="flex justify-content-between align-items-center mb-3">
              <span class="text-xl font-semibold">Low Stock Alert</span>
              <Button label="View All" icon="pi pi-chevron-right" iconPos="right" variant="text" size="small" @click="navigateTo('home')" />
            </div>
          </template>
          <template #content>
            <div v-if="lowStockParts.length === 0" class="text-center py-5 text-500">
              <i class="pi pi-check-circle text-4xl mb-3 text-green-500"></i>
              <p>All items are well stocked.</p>
            </div>
            <ul v-else class="list-none p-0 m-0">
              <li v-for="part in lowStockParts" :key="part.id" class="flex align-items-center justify-content-between py-3 border-bottom-1 surface-border last:border-none">
                <div class="flex align-items-center">
                   <div class="w-2rem h-2rem border-round bg-faint flex align-items-center justify-content-center mr-3">
                     <i class="pi pi-image text-400"></i>
                   </div>
                   <span>{{ part.name }}</span>
                </div>
                <span class="font-bold text-orange-500">{{ part.stock }} left</span>
              </li>
            </ul>
          </template>
        </Card>
      </div>

      <div class="col-12 lg:col-6">
        <Card class="h-full border-none shadow-1">
          <template #title>
             <div class="flex justify-content-between align-items-center mb-3">
              <span class="text-xl font-semibold">Recent Components</span>
              <Button label="Inventory" icon="pi pi-chevron-right" iconPos="right" variant="text" size="small" @click="navigateTo('home')" />
            </div>
          </template>
          <template #content>
            <ul class="list-none p-0 m-0">
              <li v-for="part in recentParts" :key="part.id" class="flex align-items-center justify-content-between py-3 border-bottom-1 surface-border">
                <div class="flex align-items-center">
                   <div class="w-2rem h-2rem border-round bg-faint flex align-items-center justify-content-center mr-3">
                     <i class="pi pi-image text-400"></i>
                   </div>
                   <div>
                     <span class="block font-medium">{{ part.name }}</span>
                     <small class="text-500">{{ part.category?.name || 'Uncategorized' }}</small>
                   </div>
                </div>
                <Button icon="pi pi-arrow-right" variant="text" rounded size="small" @click="router.push({name: 'part-detail', params: {id: part.id}})" />
              </li>
            </ul>
          </template>
        </Card>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
}

.bg-faint {
  background-color: rgba(0, 0, 0, 0.03);
}

.stat-card {
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.border-bottom-1:last-child {
  border-bottom: none !important;
}

.grid {
  display: flex;
  flex-wrap: wrap;
  margin-right: -1rem;
  margin-left: -1rem;
}

.col-12, .md\:col-4, .lg\:col-6 {
  padding: 1rem;
}

@media screen and (min-width: 768px) {
  .md\:col-4 { flex: 0 0 auto; width: 33.3333%; }
}

@media screen and (min-width: 992px) {
  .lg\:col-6 { flex: 0 0 auto; width: 50%; }
}

.flex { display: flex; }
.align-items-center { align-items: center; }
.justify-content-between { justify-content: space-between; }
.justify-content-center { justify-content: center; }
.flex-column { flex-direction: column; }
.mr-3 { margin-right: 1rem; }
.mb-1 { margin-bottom: 0.25rem; }
.mb-3 { margin-bottom: 0.75rem; }
.mb-6 { margin-bottom: 2rem; }
.mt-2 { margin-top: 0.5rem; }
.py-3 { padding-top: 0.75rem; padding-bottom: 0.75rem; }
.py-5 { padding-top: 1.5rem; padding-bottom: 1.5rem; }
.p-3 { padding: 0.75rem; }
.p-0 { padding: 0; }
.m-0 { margin: 0; }
.w-2rem { width: 2rem; }
.h-2rem { height: 2rem; }
.border-round { border-radius: 8px; }
.border-none { border: none !important; }
.shadow-1 { box-shadow: 0 1px 2px 0 rgba(0,0,0,0.05); }
.text-4xl { font-size: 2.25rem; }
.text-2xl { font-size: 1.5rem; }
.text-xl { font-size: 1.25rem; }
.text-900 { color: #111827; }
.text-500 { color: #6b7280; }
.text-400 { color: #9ca3af; }
.font-light { font-weight: 300; }
.font-medium { font-weight: 500; }
.font-semibold { font-weight: 600; }
.font-bold { font-weight: 700; }
.list-none { list-style: none; }
.surface-border { border-color: #e5e7eb; }
.border-bottom-1 { border-bottom: 1px solid #e5e7eb; }
</style>
