<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { drawerService, partService } from '../services/api';
import Card from 'primevue/card';
import ProgressSpinner from 'primevue/progressspinner';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';

const drawers = ref<any[]>([]);
const parts = ref<any[]>([]);
const loading = ref(true);

onMounted(async () => {
  try {
    const [drawersRes, partsRes] = await Promise.all([
      drawerService.getAll(),
      partService.getAll()
    ]);
    drawers.value = drawersRes.data;
    parts.value = partsRes.data;
  } catch (err) {
    console.error('Failed to load storage data', err);
  } finally {
    loading.value = false;
  }
});

const getPartsInDrawer = (drawerId: number) => {
  return parts.value.filter(p => p.drawer?.id === drawerId);
};
</script>

<template>
  <div class="storage-container">
    <div class="header mb-6">
      <h1 class="text-4xl font-light m-0">Storage Overview</h1>
      <p class="text-500 mt-2">Manage your drawers and component locations.</p>
    </div>

    <div v-if="loading" class="flex justify-content-center p-8">
      <ProgressSpinner />
    </div>

    <div v-else class="grid">
      <div v-for="drawer in drawers" :key="drawer.id" class="col-12">
        <Card class="drawer-card border-none shadow-1 mb-4">
          <template #title>
            <div class="flex align-items-center">
              <i class="pi pi-database mr-3 text-2xl text-blue-500"></i>
              <span>Drawer #{{ drawer.number }}</span>
            </div>
          </template>
          <template #content>
            <DataTable :value="getPartsInDrawer(drawer.id)" size="small" stripedRows responsiveLayout="scroll">
              <Column field="sectionNumber" header="Section" sortable style="width: 10%"></Column>
              <Column field="name" header="Component" sortable style="width: 40%"></Column>
              <Column field="category.name" header="Category" sortable style="width: 30%"></Column>
              <Column field="stock" header="Stock" sortable style="width: 20%">
                <template #body="slotProps">
                  <span :class="{'text-orange-500 font-bold': slotProps.data.stock < 5}">
                    {{ slotProps.data.stock }}
                  </span>
                </template>
              </Column>
            </DataTable>
            <div v-if="getPartsInDrawer(drawer.id).length === 0" class="text-center py-4 text-500 italic">
              Empty drawer
            </div>
          </template>
        </Card>
      </div>
      <div v-if="drawers.length === 0" class="col-12 text-center py-8 text-500">
        <i class="pi pi-box text-6xl mb-4"></i>
        <p class="text-xl">No storage units defined.</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.storage-container {
  max-width: 1000px;
  margin: 0 auto;
}

.grid {
  display: flex;
  flex-wrap: wrap;
  margin-right: -1rem;
  margin-left: -1rem;
}

.col-12 {
  padding: 1rem;
  width: 100%;
}

.flex { display: flex; }
.align-items-center { align-items: center; }
.justify-content-center { justify-content: center; }
.mb-4 { margin-bottom: 1rem; }
.mb-6 { margin-bottom: 2rem; }
.mt-2 { margin-top: 0.5rem; }
.mr-3 { margin-right: 1rem; }
.p-8 { padding: 4rem; }
.m-0 { margin: 0; }
.border-none { border: none !important; }
.shadow-1 { box-shadow: 0 1px 2px 0 rgba(0,0,0,0.05); }
.text-4xl { font-size: 2.25rem; }
.text-2xl { font-size: 1.5rem; }
.text-500 { color: #6b7280; }
.text-blue-500 { color: #3b82f6; }
.text-orange-500 { color: #f97316; }
.font-light { font-weight: 300; }
.font-bold { font-weight: 700; }
.italic { font-style: italic; }
</style>
