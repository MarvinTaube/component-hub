<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { apiService } from '../services/api'
import type { Category, Drawer, PartRequest } from '../types/backend'

import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import InputNumber from 'primevue/inputnumber'
import Select from 'primevue/select'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'

const router = useRouter()

const categories = ref<Category[]>([])
const drawers = ref<Drawer[]>([])

const loadingOptions = ref(true)
const submitting = ref(false)
const submitError = ref<string | null>(null)

const showDrawerDialog = ref(false)
const newDrawerNumber = ref<number | null>(null)
const creatingDrawer = ref(false)
const drawerError = ref<string | null>(null)

const enableNotes = ref(false)
const formLinks = ref<string[]>([])
const formNotes = ref<string>('')

// Form state
const formData = ref<PartRequest>({
  name: '',
  description: '',
  stock: 0,
  imageLink: null,
  categoryId: null,
  drawerId: null,
  sectionNumber: null
})

// Upload references and state
const imageFileInput = ref<HTMLInputElement | null>(null)
const linkFileInputs = ref<any[]>([])

const uploadingImage = ref(false)
const uploadingLinkIndex = ref<number | null>(null)

// Drag and drop state
const imageDragOver = ref(false)
const linkDragOver = ref<boolean[]>([])

onMounted(async () => {
  try {
    const [cats, drws] = await Promise.all([
      apiService.getCategories(),
      apiService.getDrawers()
    ])
    categories.value = cats
    drawers.value = drws
  } catch (e) {
    console.error('Failed to load form options:', e)
  } finally {
    loadingOptions.value = false
  }
})

// --- Upload Handlers ---
const triggerImageUpload = () => imageFileInput.value?.click()

const triggerLinkUpload = (index: number) => {
  const input = linkFileInputs.value[index]
  if (input) input.click()
}

const handleImageUpload = async (file: File) => {
  uploadingImage.value = true
  try {
    const url = await apiService.uploadFile(file, 'itemImage')
    formData.value.imageLink = url
    submitError.value = null
  } catch (e: any) {
    console.error("Upload failed", e)
    submitError.value = e.message || "Failed to upload image"
  } finally {
    uploadingImage.value = false
  }
}

const onImageFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    await handleImageUpload(target.files[0]!)
    target.value = '' 
  }
}

const onImageDrop = async (event: DragEvent) => {
  imageDragOver.value = false
  if (event.dataTransfer?.files && event.dataTransfer.files.length > 0) {
    await handleImageUpload(event.dataTransfer.files[0]!)
  }
}

const handleLinkUpload = async (file: File, index: number) => {
  uploadingLinkIndex.value = index
  try {
    const url = await apiService.uploadFile(file, 'doc')
    formLinks.value[index] = url
    submitError.value = null
  } catch (e: any) {
    console.error("Upload failed", e)
    submitError.value = e.message || "Failed to upload documentation document"
  } finally {
    uploadingLinkIndex.value = null
  }
}

const onLinkFileChange = async (event: Event, index: number) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    await handleLinkUpload(target.files[0]!, index)
    target.value = ''
  }
}

const onLinkDrop = async (event: DragEvent, index: number) => {
  linkDragOver.value[index] = false
  if (event.dataTransfer?.files && event.dataTransfer.files.length > 0) {
    await handleLinkUpload(event.dataTransfer.files[0]!, index)
  }
}

// --- Dynamic Links ---
const addLink = () => {
  formLinks.value.push('')
}

const removeLink = (index: number) => {
  formLinks.value.splice(index, 1)
}

// --- Form Submission ---
const submitForm = async () => {
  if (!formData.value.name || !formData.value.description) {
    submitError.value = "Name and description are required"
    return
  }
  
  submitError.value = null
  submitting.value = true
  
  const payload = { ...formData.value }
  
  if (enableNotes.value && formNotes.value.trim() !== '') {
    payload.notes = formNotes.value
  } else {
    payload.notes = null
  }
  
  payload.links = formLinks.value.filter(link => link.trim() !== '')
  
  try {
    await apiService.createPart(payload)
    router.push('/')
  } catch (e: any) {
    submitError.value = e.message || "Failed to create part"
  } finally {
    submitting.value = false
  }
}

// --- Inline Drawer Creation ---
const createNewDrawer = async () => {
  if (newDrawerNumber.value === null) {
    drawerError.value = "Drawer number is required"
    return
  }
  
  drawerError.value = null
  creatingDrawer.value = true
  
  try {
    const newDrawer = await apiService.createDrawer(newDrawerNumber.value)
    drawers.value.push(newDrawer)
    drawers.value.sort((a, b) => a.number - b.number)
    formData.value.drawerId = newDrawer.id
    showDrawerDialog.value = false
    newDrawerNumber.value = null
  } catch (e: any) {
    drawerError.value = e.message || "Failed to create drawer"
  } finally {
    creatingDrawer.value = false
  }
}

const cancel = () => {
  router.push('/')
}
</script>

<template>
  <div class="add-component-view">
    <div class="header-section">
      <h2 class="page-title">Add New Component</h2>
    </div>
    
    <form @submit.prevent="submitForm" class="component-form">
      <div class="form-grid">
        <div class="field required">
          <label for="name">Component Name</label>
          <InputText id="name" v-model="formData.name" placeholder="e.g. 10k Resistor" required />
        </div>
        
        <div class="field required">
          <label for="stock">Initial Stock</label>
          <InputNumber id="stock" v-model="formData.stock" showButtons :min="0" required />
        </div>
        
        <div class="field full-width required">
          <label for="description">Description <span class="field-hint">(Manufacturer or general info)</span></label>
          <Textarea id="description" v-model="formData.description" rows="3" required />
        </div>
        
        <div class="field">
          <label for="category">Category</label>
          <Select 
            id="category" 
            v-model="formData.categoryId" 
            :options="categories" 
            optionLabel="name" 
            optionValue="id" 
            placeholder="Select a Category" 
            :loading="loadingOptions" 
            class="w-full"
          />
        </div>
        
        <div class="field">
          <label for="drawer">Location Drawer</label>
          <div class="flex-row gap-2">
            <Select 
              id="drawer" 
              v-model="formData.drawerId" 
              :options="drawers" 
              optionLabel="number" 
              optionValue="id" 
              placeholder="Select a Drawer" 
              :loading="loadingOptions" 
              class="w-full flex-1"
            >
              <template #option="slotProps">
                <div>Drawer {{ slotProps.option.number }}</div>
              </template>
              <template #value="slotProps">
                <div v-if="slotProps.value">
                  Drawer {{ drawers.find(d => d.id === slotProps.value)?.number }}
                </div>
                <span v-else>
                  Select a Drawer
                </span>
              </template>
            </Select>
            <Button icon="pi pi-plus" @click="showDrawerDialog = true" aria-label="Create New Drawer" v-tooltip="'Create New Drawer'" />
          </div>
        </div>
        
        <div class="field">
          <label for="sectionNumber">Section Number</label>
          <InputNumber id="sectionNumber" v-model="formData.sectionNumber" showButtons :min="1" />
        </div>
        
        <div class="field">
          <label for="imageLink">Image URL & Upload</label>
          <div 
            class="flex-row gap-2 transition-colors drop-zone" 
            :class="{ 'drop-active': imageDragOver }"
            @dragover.prevent="imageDragOver = true"
            @dragleave.prevent="imageDragOver = false"
            @drop.prevent="onImageDrop"
          >
            <InputText id="imageLink" v-model="formData.imageLink" placeholder="Drag image here or link https://..." class="w-full flex-1" />
            
            <input 
              type="file" 
              ref="imageFileInput" 
              accept="image/*" 
              class="hidden" 
              @change="onImageFileChange" 
            />
            
            <Button 
              icon="pi pi-upload" 
              aria-label="Upload Image" 
              v-tooltip="'Upload Image'" 
              @click="triggerImageUpload" 
              :loading="uploadingImage" 
              type="button"
            />
          </div>
        </div>
        
        <div class="divider full-width"></div>

        <div class="field full-width">
          <div class="flex-row justify-between align-center mb-2">
            <label class="mb-0">Documentation Links</label>
            <Button icon="pi pi-plus" label="Add Link" size="small" variant="text" @click="addLink" type="button" class="p-0" />
          </div>
          <div v-if="formLinks.length === 0" class="empty-state-text">
            No links added. You can paste URLs or drag and drop local files.
          </div>
          <div class="link-list">
            <div 
              v-for="(link, index) in formLinks" 
              :key="index" 
              class="flex-row gap-2 mb-2 transition-colors drop-zone"
              :class="{ 'drop-active': linkDragOver[index] }"
              @dragover.prevent="linkDragOver[index] = true"
              @dragleave.prevent="linkDragOver[index] = false"
              @drop.prevent="(e) => onLinkDrop(e, index)"
            >
              <InputText v-model="formLinks[index]" placeholder="Drag document here or input Link URL" class="flex-1" />
              
              <input 
                type="file" 
                :ref="(el) => { if (el) linkFileInputs[index] = el }" 
                class="hidden" 
                @change="(e) => onLinkFileChange(e, index)" 
              />
              
              <Button 
                icon="pi pi-upload" 
                severity="secondary" 
                aria-label="Upload Document" 
                v-tooltip="'Upload Document'" 
                @click="triggerLinkUpload(index)" 
                :loading="uploadingLinkIndex === index" 
                type="button" 
              />
              <Button icon="pi pi-trash" severity="danger" variant="text" @click="removeLink(index)" type="button" aria-label="Remove Link" />
            </div>
          </div>
        </div>

        <div class="field full-width">
           <div class="flex-row justify-between align-center mb-2">
            <label class="mb-0">Private Notes</label>
            <Button 
              :icon="enableNotes ? 'pi pi-minus' : 'pi pi-plus'" 
              :label="enableNotes ? 'Disable Notes' : 'Enable Notes'" 
              size="small" 
              :severity="enableNotes ? 'danger' : 'secondary'"
              variant="text" 
              @click="enableNotes = !enableNotes" 
              type="button" 
              class="p-0"
            />
          </div>
          <Textarea 
            v-if="enableNotes"
            id="notes" 
            v-model="formNotes" 
            rows="3" 
            placeholder="Add personal hints, thoughts, or custom data here..." 
            class="w-full"
          />
        </div>
        
      </div>
      
      <div v-if="submitError" class="error-message">
        {{ submitError }}
      </div>
      
      <div class="form-actions">
        <Button label="Cancel" severity="secondary" @click="cancel" outlined />
        <Button type="submit" label="Save Component" icon="pi pi-check" :loading="submitting" />
      </div>
    </form>
    
    <!-- Drawer Creation Dialog -->
    <Dialog v-model:visible="showDrawerDialog" modal header="Create New Drawer" :style="{ width: '25rem' }">
      <div class="field mt-3">
        <label for="newDrawerNumber" class="mb-2 block font-semibold">Drawer Number</label>
        <InputNumber id="newDrawerNumber" v-model="newDrawerNumber" showButtons :min="1" class="w-full" autofocus />
      </div>
      <div v-if="drawerError" class="error-message mt-2 mb-0">
        {{ drawerError }}
      </div>
      <div class="flex justify-end gap-2 mt-4">
        <Button type="button" label="Cancel" severity="secondary" @click="showDrawerDialog = false"></Button>
        <Button type="button" label="Create" @click="createNewDrawer" :loading="creatingDrawer"></Button>
      </div>
    </Dialog>
  </div>
</template>

<style scoped>
.add-component-view {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  height: 100%;
  max-width: 800px;
  margin: 0 auto;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.divider {
  height: 1px;
  background-color: var(--p-surface-200, #e2e8f0);
  margin: 1rem 0;
}

.page-title {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--p-surface-900, #0f172a);
}

.component-form {
  background-color: var(--p-surface-0, #ffffff);
  border: 1px solid var(--p-surface-200, #e2e8f0);
  border-radius: 12px;
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 2rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.field.full-width {
  grid-column: 1 / -1;
}

.field label {
  font-weight: 600;
  font-size: 0.875rem;
  color: var(--p-surface-700, #334155);
}

.field-hint {
  font-weight: 400;
  color: var(--p-surface-400, #94a3b8);
  margin-left: 0.5rem;
}

.field.required label::after {
  content: " *";
  color: var(--p-red-500, #ef4444);
}

.flex-row {
  display: flex;
  align-items: center;
}

.justify-between {
  justify-content: space-between;
}

.gap-2 {
  gap: 0.5rem;
}

.flex-1 {
  flex: 1;
}

.w-full {
  width: 100%;
}

.hidden {
  display: none !important;
}

/* Drag and Drop styles */
.drop-zone {
  border-radius: 6px;
  border: 2px dashed transparent;
  padding: 0.25rem;
  margin: -0.25rem;
}

.transition-colors {
  transition: all 0.2s ease;
}

.drop-active {
  border-color: var(--p-primary-500, #10b981);
  background-color: var(--p-primary-50, #ecfdf5);
}

.mt-3 { margin-top: 1rem; }
.mt-4 { margin-top: 1.5rem; }
.mt-2 { margin-top: 0.5rem; }
.mb-2 { margin-bottom: 0.5rem; }
.mb-0 { margin-bottom: 0 !important; }
.p-0 { padding: 0 !important; }
.block { display: block; }
.font-semibold { font-weight: 600; }
.flex { display: flex; }
.justify-end { justify-content: flex-end; }
.align-center { align-items: center; }

.empty-state-text {
  font-size: 0.875rem;
  color: var(--p-surface-400, #94a3b8);
  font-style: italic;
}

.error-message {
  color: var(--p-red-600, #dc2626);
  background-color: var(--p-red-50, #fef2f2);
  padding: 1rem;
  border-radius: 6px;
  border: 1px solid var(--p-red-200, #fecaca);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--p-surface-100, #f1f5f9);
}

@media (prefers-color-scheme: dark) {
  .page-title {
    color: var(--p-surface-50, #f8fafc);
  }
  .component-form {
    background-color: var(--p-surface-900, #0f172a);
    border-color: var(--p-surface-800, #1e293b);
  }
  .divider {
    background-color: var(--p-surface-800, #1e293b);
  }
  .field label {
    color: var(--p-surface-300, #cbd5e1);
  }
  .form-actions {
    border-color: var(--p-surface-800, #1e293b);
  }
  .error-message {
    color: var(--p-red-400, #f87171);
    border-color: var(--p-red-900, #7f1d1d);
    background-color: rgba(127, 29, 29, 0.1);
  }
  .drop-active {
    border-color: var(--p-primary-500, #10b981);
    background-color: rgba(16, 185, 129, 0.1);
  }
}
</style>
