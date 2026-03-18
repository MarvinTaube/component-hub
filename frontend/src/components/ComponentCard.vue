<script setup lang="ts">
import Card from 'primevue/card'

export interface ComponentItem {
  id: string | number
  name: string
  category: string
  image?: string
  drawerNumber: string
  stock: number
  description?: string
}

defineProps<{
  item: ComponentItem
}>()
</script>

<template>
  <Card class="component-card">
    <template #header>
      <div class="card-image-wrapper">
        <img v-if="item.image" :src="item.image" :alt="item.name" class="card-image" />
        <div v-else class="placeholder-image">
          <i class="pi pi-box"></i>
        </div>
      </div>
    </template>
    
    <template #title>
      <div class="component-name" :title="item.name">{{ item.name }}</div>
    </template>
    
    <template #subtitle>
      <span class="component-category">{{ item.category }}</span>
    </template>
    
    <template #content>
      <!-- We don't need additional content for the scaffolding, but slot exists -->
    </template>

    <template #footer>
      <div class="card-footer">
        <div class="location-badge" title="Drawer Number">
          <i class="pi pi-map-marker"></i>
          <span>{{ item.drawerNumber }}</span>
        </div>
        <div class="stock-badge" :class="{'low-stock': item.stock < 10}" title="Current Stock Level">
          <i class="pi pi-box"></i>
          <span>{{ item.stock }}</span>
        </div>
      </div>
    </template>
  </Card>
</template>

<style scoped>
.component-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: pointer;
}

.component-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  border-color: var(--p-primary-300, #6ee7b7);
}

/* Ensure the header takes its own space without padding in primevue */
:deep(.p-card-header) {
  padding: 0;
}

:deep(.p-card-body) {
  display: flex;
  flex-direction: column;
  flex: 1;
}

:deep(.p-card-title) {
  margin-bottom: 0;
}

:deep(.p-card-content) {
  padding: 0;
}

:deep(.p-card-footer) {
  margin-top: auto;
  padding-top: 1rem;
  border-top: 1px solid var(--p-surface-200, #e2e8f0);
}

.card-image-wrapper {
  height: 180px;
  width: 100%;
  background-color: var(--p-surface-50, #f8fafc);
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--p-surface-100, #f1f5f9);
  overflow: hidden;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.component-card:hover .card-image {
  transform: scale(1.05);
}

.placeholder-image {
  color: var(--p-surface-300, #cbd5e1);
  font-size: 3rem;
  opacity: 0.8;
}

.placeholder-image i {
  font-size: inherit;
}

.component-name {
  font-size: 1.125rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 0;
  color: var(--p-surface-900, #0f172a);
}

.component-category {
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--p-surface-500, #64748b);
  display: inline-block;
  background-color: var(--p-surface-100, #f1f5f9);
  padding: 0.25rem 0.6rem;
  border-radius: 4px;
  width: fit-content;
  margin-top: 0.25rem;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.location-badge, .stock-badge {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--p-surface-700, #334155);
}

.location-badge i {
  color: var(--p-surface-400, #94a3b8);
  font-size: 1rem;
}

.stock-badge i {
  color: var(--p-primary-500, #10b981);
  font-size: 1rem;
}

.stock-badge.low-stock {
  color: var(--p-red-600, #dc2626);
}

.stock-badge.low-stock i {
  color: var(--p-red-500, #ef4444);
}

@media (prefers-color-scheme: dark) {
  .component-card:hover { border-color: var(--p-primary-600, #059669); }
  .component-name { color: var(--p-surface-50, #f8fafc); }
  
  .card-image-wrapper {
    background-color: var(--p-surface-800, #1e293b);
    border-color: var(--p-surface-700, #334155);
  }
  
  .placeholder-image { color: var(--p-surface-600, #475569); }
  .component-category { background-color: var(--p-surface-800, #1e293b); }
  :deep(.p-card-footer) { border-color: var(--p-surface-800, #1e293b); }
  .location-badge, .stock-badge { color: var(--p-surface-300, #cbd5e1); }
}
</style>
