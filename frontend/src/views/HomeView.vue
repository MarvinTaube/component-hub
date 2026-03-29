<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import ComponentCard from '../components/ComponentCard.vue'
import type { ComponentItem } from '../components/ComponentCard.vue'
import { apiService } from '../services/api'

import InputText from 'primevue/inputtext'
import IconField from 'primevue/iconfield'
import InputIcon from 'primevue/inputicon'

const components = ref<ComponentItem[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const searchQuery = ref('')

const filteredComponents = computed(() => {
  if (!searchQuery.value) return components.value
  const query = searchQuery.value.toLowerCase()
  return components.value.filter(item => 
    item.name.toLowerCase().includes(query) || 
    (item.description && item.description.toLowerCase().includes(query))
  )
})

onMounted(async () => {
  try {
    const parts = await apiService.getParts()
    components.value = parts.map(part => {
      let drawerStr = 'Unassigned'
      if (part.drawer) {
        drawerStr = `D${part.drawer.number}-S${part.sectionNumber}`
      }
      
      return {
        id: part.id,
        name: part.name,
        description: part.description,
        category: part.category?.name || 'Uncategorized',
        drawerNumber: drawerStr,
        stock: part.stock,
        image: part.imageLink || undefined
      }
    })
  } catch (err) {
    console.error('Failed to fetch components:', err)
    error.value = 'Failed to load inventory. Is the backend running?'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="home">
    <div class="header-section">
      <h2 class="page-title">Inventory</h2>
      <div class="actions">
        <IconField class="search-field">
          <InputIcon class="pi pi-search" />
          <InputText v-model="searchQuery" placeholder="Search components..." class="w-full" />
        </IconField>
        <span class="component-count">{{ filteredComponents.length }} items</span>
      </div>
    </div>
    
    <div v-if="loading" class="state-message loading">
      <i class="pi pi-spin pi-spinner" style="font-size: 2rem"></i>
      <p>Loading components from backend...</p>
    </div>

    <div v-else-if="error" class="state-message error">
      <i class="pi pi-exclamation-triangle" style="font-size: 2rem"></i>
      <p>{{ error }}</p>
    </div>

    <div v-else-if="components.length === 0" class="state-message empty">
      <i class="pi pi-box" style="font-size: 2rem"></i>
      <p>No components found in the database.</p>
    </div>
    
    <div v-else-if="filteredComponents.length === 0" class="state-message empty">
      <i class="pi pi-search" style="font-size: 2rem"></i>
      <p>No components match your search.</p>
    </div>
    
    <div v-else class="component-grid">
      <ComponentCard 
        v-for="item in filteredComponents" 
        :key="item.id" 
        :item="item" 
      />
    </div>
  </div>
</template>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  height: 100%;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--p-surface-900, #0f172a);
}

.component-count {
  font-size: 0.875rem;
  color: var(--p-surface-500, #64748b);
  background-color: var(--p-surface-100, #f1f5f9);
  padding: 0.35rem 0.85rem;
  border-radius: 9999px;
  font-weight: 600;
  white-space: nowrap;
}

.actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.search-field {
  width: 280px;
}

.w-full {
  width: 100%;
}

.component-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 1.5rem;
  padding-bottom: 2rem;
}

.state-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  gap: 1rem;
  background-color: var(--p-surface-0, #ffffff);
  border: 1px dashed var(--p-surface-300, #cbd5e1);
  border-radius: 12px;
  color: var(--p-surface-500, #64748b);
  text-align: center;
}

.state-message.error {
  color: var(--p-red-600, #dc2626);
  border-color: var(--p-red-200, #fecaca);
  background-color: var(--p-red-50, #fef2f2);
}

@media (prefers-color-scheme: dark) {
  .page-title {
    color: var(--p-surface-50, #f8fafc);
  }
  .component-count {
    background-color: var(--p-surface-800, #1e293b);
    color: var(--p-surface-400, #94a3b8);
  }
  .state-message {
    background-color: var(--p-surface-900, #0f172a);
    border-color: var(--p-surface-700, #334155);
    color: var(--p-surface-400, #94a3b8);
  }
  .state-message.error {
    color: var(--p-red-400, #f87171);
    border-color: var(--p-red-900, #7f1d1d);
    background-color: rgba(127, 29, 29, 0.1);
  }
}
</style>