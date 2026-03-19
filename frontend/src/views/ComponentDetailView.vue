<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiService } from '../services/api'
import type { Part } from '../types/backend'

import Button from 'primevue/button'

const route = useRoute()
const router = useRouter()

const part = ref<Part | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)

onMounted(async () => {
  const id = route.params.id as string
  try {
    part.value = await apiService.getPartById(id)
  } catch (err: any) {
    console.error('Failed to load part details:', err)
    error.value = "Component not found or backend unavailable."
  } finally {
    loading.value = false
  }
})

const goBack = () => {
  router.push('/')
}

const getLocationString = () => {
  if (!part.value) return 'Unknown'
  if (!part.value.drawer) return 'Unassigned'
  return `Drawer ${part.value.drawer.number} - Section ${part.value.sectionNumber || 'None'}`
}
</script>

<template>
  <div class="component-detail-view">
    <div class="header-section">
      <div class="flex items-center gap-3">
        <Button icon="pi pi-arrow-left" variant="text" rounded aria-label="Go Back" @click="goBack" />
        <h2 class="page-title">{{ part ? part.name : 'Loading Details...' }}</h2>
      </div>
      <!-- Future: edit button -->
      <Button icon="pi pi-pencil" label="Edit" outlined severity="secondary" disabled v-tooltip="'Editing coming soon'" />
    </div>
    
    <div v-if="loading" class="state-message">
      <i class="pi pi-spin pi-spinner text-3xl"></i>
      <p>Loading component details...</p>
    </div>
    
    <div v-else-if="error" class="state-message error">
      <i class="pi pi-exclamation-triangle text-3xl"></i>
      <p>{{ error }}</p>
    </div>
    
    <div v-else-if="part" class="component-card">
      <!-- Top Section -->
      <div class="top-split">
        <!-- Left: Image Box -->
        <div class="image-preview-box">
          <img v-if="part.imageLink" :src="part.imageLink" :alt="part.name" class="preview-img" />
          <div v-else class="upload-placeholder">
            <i class="pi pi-image upload-icon"></i>
            <span class="upload-text">No image available</span>
          </div>
        </div>
        
        <!-- Right: Basic Details Grid -->
        <div class="basic-details">
          <div class="detail-group">
            <span class="label">Component Name</span>
            <div class="value text-xl font-bold">{{ part.name }}</div>
          </div>
          
          <div class="detail-group">
            <span class="label">Stock Level</span>
            <div class="value stock-value" :class="{'text-red-500': part.stock < 10, 'text-green-500': part.stock >= 10}">
              <i class="pi pi-box mr-2"></i>{{ part.stock }} units
            </div>
          </div>
          
          <div class="detail-group">
            <span class="label">Category</span>
            <div class="value mt-1">
              <span class="category-badge">{{ part.category?.name || 'Uncategorized' }}</span>
            </div>
          </div>
          
          <div class="detail-group">
            <span class="label">Physical Location</span>
            <div class="value flex items-center font-medium loc-color mt-1">
              <i class="pi pi-map-marker mr-2"></i>{{ getLocationString() }}
            </div>
          </div>
        </div>
      </div>
      
      <div class="divider full-width"></div>
      
      <!-- Bottom Section -->
      <div class="details-section">
        <div class="detail-group full-width">
          <span class="label">Description</span>
          <div class="value text-block">
            {{ part.description }}
          </div>
        </div>
        
        <div class="detail-group full-width" v-if="part.links && part.links.length > 0">
          <span class="label">Documentation Links</span>
          <div class="links-container mt-2">
            <a 
              v-for="(link, idx) in part.links" 
              :key="idx" 
              :href="link" 
              target="_blank" 
              rel="noopener noreferrer"
              class="doc-link-button"
            >
              <i class="pi pi-file mr-3 text-lg"></i>
              <span class="link-text">{{ link.split('/').pop() || link }}</span>
              <i class="pi pi-external-link ml-auto text-sm text-surface-400"></i>
            </a>
          </div>
        </div>
        
        <div class="detail-group full-width" v-if="part.notes">
          <span class="label">Private Notes</span>
          <div class="value text-block notes-block">
            {{ part.notes }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.component-detail-view {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  height: 100%;
  max-width: 850px;
  margin: 0 auto;
  padding-bottom: 2rem;
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

.component-card {
  background-color: var(--p-surface-0, #ffffff);
  border: 1px solid var(--p-surface-200, #e2e8f0);
  border-radius: 16px;
  padding: 2.5rem;
  display: flex;
  flex-direction: column;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05), 0 4px 6px -2px rgba(0, 0, 0, 0.025);
}

.divider {
  height: 1px;
  background-color: var(--p-surface-200, #e2e8f0);
  margin: 2.5rem 0;
}

/* Layout Core */
.top-split {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 3rem;
  align-items: start;
}

/* Image Box */
.image-preview-box {
  width: 100%;
  aspect-ratio: 1 / 1;
  background-color: var(--p-surface-50, #f8fafc);
  border: 1px solid var(--p-surface-200, #e2e8f0);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background-color: var(--p-surface-50, #f8fafc);
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  color: var(--p-surface-400, #94a3b8);
}

.upload-icon {
  font-size: 3rem;
  opacity: 0.5;
}

/* Basic Details Typography */
.basic-details {
  display: flex;
  flex-direction: column;
  gap: 1.75rem;
}

.details-section {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.detail-group {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.label {
  font-size: 0.8rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--p-surface-400, #94a3b8);
}

.value {
  color: var(--p-surface-900, #0f172a);
  font-size: 1rem;
  line-height: 1.5;
}

/* Special Values */
.stock-value {
  font-size: 1.25rem;
  font-weight: 700;
  display: flex;
  align-items: center;
}

.category-badge {
  display: inline-block;
  background-color: var(--p-surface-100, #f1f5f9);
  color: var(--p-surface-700, #334155);
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.875rem;
  font-weight: 600;
}

.loc-color {
  color: var(--p-surface-600, #475569);
}

.text-block {
  background-color: var(--p-surface-50, #f8fafc);
  border: 1px solid var(--p-surface-200, #e2e8f0);
  border-radius: 8px;
  padding: 1.25rem;
  white-space: pre-wrap;
  font-size: 0.95rem;
}

.notes-block {
  background-color: rgba(245, 158, 11, 0.05) !important;
  border-color: rgba(245, 158, 11, 0.2) !important;
  color: var(--p-surface-800, #1e293b);
}

/* Links List */
.links-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
}

.doc-link-button {
  display: flex;
  align-items: center;
  padding: 1rem 1.25rem;
  background-color: var(--p-surface-0, #ffffff);
  border: 1px solid var(--p-surface-200, #e2e8f0);
  border-radius: 8px;
  color: var(--p-primary-600, #059669);
  text-decoration: none;
  font-weight: 500;
  transition: all 0.2s ease;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.doc-link-button:hover {
  border-color: var(--p-primary-300, #6ee7b7);
  background-color: var(--p-primary-50, #ecfdf5);
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.link-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Utility Layouts */
.flex { display: flex; }
.items-center { align-items: center; }
.gap-3 { gap: 0.75rem; }
.text-xl { font-size: 1.25rem; }
.font-bold { font-weight: 700; }
.font-medium { font-weight: 500; }
.text-red-500 { color: var(--p-red-500, #ef4444) !important; }
.text-green-500 { color: var(--p-green-600, #16a34a) !important; }
.mr-2 { margin-right: 0.5rem; }
.mr-3 { margin-right: 0.75rem; }
.ml-auto { margin-left: auto; }
.mt-1 { margin-top: 0.25rem; }
.mt-2 { margin-top: 0.5rem; }
.text-sm { font-size: 0.875rem; }
.text-lg { font-size: 1.125rem; }
.text-3xl { font-size: 2rem; }

.state-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  gap: 1rem;
  color: var(--p-surface-500, #64748b);
  border: 1px dashed var(--p-surface-300, #cbd5e1);
  border-radius: 12px;
}

.state-message.error {
  color: var(--p-red-600, #dc2626);
  border-color: var(--p-red-200, #fecaca);
  background-color: var(--p-red-50, #fef2f2);
}

@media (max-width: 768px) {
  .top-split {
    grid-template-columns: 1fr;
    gap: 2rem;
  }
  .image-preview-box {
    max-width: 300px;
    margin: 0 auto;
  }
}

@media (prefers-color-scheme: dark) {
  .page-title { color: var(--p-surface-50, #f8fafc); }
  .component-card {
    background-color: var(--p-surface-900, #0f172a);
    border-color: var(--p-surface-800, #1e293b);
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.4);
  }
  .divider { background-color: var(--p-surface-800, #1e293b); }
  
  .image-preview-box {
    background-color: var(--p-surface-800, #1e293b);
    border-color: var(--p-surface-700, #334155);
  }
  .preview-img { background-color: var(--p-surface-900, #0f172a); }
  
  .value { color: var(--p-surface-50, #f8fafc); }
  .loc-color { color: var(--p-surface-300, #cbd5e1); }
  
  .category-badge {
    background-color: var(--p-surface-800, #1e293b);
    color: var(--p-surface-300, #cbd5e1);
  }
  
  .text-block {
    background-color: var(--p-surface-800, #1e293b);
    border-color: var(--p-surface-700, #334155);
  }
  .notes-block {
    background-color: rgba(245, 158, 11, 0.05) !important;
    border-color: rgba(245, 158, 11, 0.2) !important;
    color: var(--p-surface-200, #e2e8f0);
  }
  
  .doc-link-button {
    background-color: var(--p-surface-800, #1e293b);
    border-color: var(--p-surface-700, #334155);
    color: var(--p-primary-400, #34d399);
  }
  .doc-link-button:hover {
    background-color: var(--p-surface-700, #334155);
    border-color: var(--p-primary-500, #10b981);
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.3);
  }
  
  .state-message {
    border-color: var(--p-surface-700, #334155);
    background-color: var(--p-surface-900, #0f172a);
  }
}
</style>
