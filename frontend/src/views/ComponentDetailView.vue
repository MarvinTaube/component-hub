<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiService } from '../services/api'
import type { Part, Category, Drawer, PartRequest } from '../types/backend'

import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Textarea from 'primevue/textarea'
import Select from 'primevue/select'
import AutoComplete from 'primevue/autocomplete'
import Message from 'primevue/message'

const route = useRoute()
const router = useRouter()

const part = ref<Part | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)

// Editing state
const isEditing = ref(false)
const saving = ref(false)
const saveError = ref<string | null>(null)

// Quick actions
const updatingStock = ref(false)

// Reference arrays
const categories = ref<Category[]>([])
const drawers = ref<Drawer[]>([])

// Form mirroring
const formData = ref<PartRequest>({
  name: '',
  description: '',
  stock: 0,
  imageLink: null,
  categoryId: null,
  drawerId: null,
  sectionNumber: null
})
const enableNotes = ref(false)
const formLinks = ref<string[]>([])
const formNotes = ref<string>('')

// Upload capabilities
const imageFileInput = ref<HTMLInputElement | null>(null)
const linkFileInputs = ref<any[]>([])
const uploadingImage = ref(false)
const uploadingLinkIndex = ref<number | null>(null)
const imageDragOver = ref(false)
const linkDragOver = ref<boolean[]>([])

// AutoComplete state
const selectedCategory = ref<Category | null>(null)
const filteredCategories = ref<Category[]>([])
const categoryQuery = ref('')

const selectedDrawer = ref<Drawer | null>(null)
const filteredDrawers = ref<Drawer[]>([])
const drawerQuery = ref('')

const creatingInline = ref(false)

const id = route.params.id as string

onMounted(async () => {
  try {
    const [fetchedPart, cats, drws] = await Promise.all([
      apiService.getPartById(id),
      apiService.getCategories(),
      apiService.getDrawers()
    ])
    part.value = fetchedPart
    categories.value = cats.sort((a, b) => a.name.localeCompare(b.name))
    drawers.value = drws.map(d => ({ ...d, label: String(d.number) })).sort((a, b) => a.number - b.number)
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

// --- AutoComplete search handlers ---
const searchCategories = (event: { query: string }) => {
  categoryQuery.value = event.query
  const q = event.query.toLowerCase()
  filteredCategories.value = categories.value.filter(c => c.name.toLowerCase().includes(q))
}

const searchDrawers = (event: { query: string }) => {
  drawerQuery.value = event.query
  const q = event.query.toLowerCase()
  filteredDrawers.value = drawers.value.filter(d => String(d.number).includes(q))
}

const onCategorySelect = (event: { value: Category }) => {
  selectedCategory.value = event.value
  formData.value.categoryId = event.value.id
}

const onDrawerSelect = (event: { value: Drawer }) => {
  selectedDrawer.value = event.value
  formData.value.drawerId = event.value.id
}

const onCategoryEnter = async () => {
  const q = categoryQuery.value.trim()
  if (!q || filteredCategories.value.length > 0) return
  
  creatingInline.value = true
  try {
    const newCat = await apiService.createCategory(q)
    categories.value.push(newCat)
    categories.value.sort((a, b) => a.name.localeCompare(b.name))
    selectedCategory.value = newCat
    formData.value.categoryId = newCat.id
    categoryQuery.value = ''
  } catch (e: any) {
    saveError.value = e.message || 'Failed to create category'
  } finally {
    creatingInline.value = false
  }
}

const onDrawerEnter = async () => {
  const q = drawerQuery.value.trim()
  const num = parseInt(q)
  if (!q || isNaN(num) || filteredDrawers.value.length > 0) return
  
  creatingInline.value = true
  try {
    const res = await apiService.createDrawer(num)
    const newDrw = { ...res, label: String(res.number) }
    drawers.value.push(newDrw)
    drawers.value.sort((a, b) => a.number - b.number)
    selectedDrawer.value = newDrw
    formData.value.drawerId = newDrw.id
    drawerQuery.value = ''
  } catch (e: any) {
    saveError.value = e.message || 'Failed to create drawer'
  } finally {
    creatingInline.value = false
  }
}

const onCategoryTab = () => {
  const first = filteredCategories.value[0]
  if (first) {
    selectedCategory.value = first
    formData.value.categoryId = first.id
  }
}

const onDrawerTab = () => {
  const first = filteredDrawers.value[0]
  if (first) {
    selectedDrawer.value = first
    formData.value.drawerId = first.id
  }
}

const getLocationString = () => {
  if (!part.value) return 'Unknown'
  if (!part.value.drawer) return 'Unassigned'
  return `Drawer ${part.value.drawer.number} - Section ${part.value.sectionNumber || 'None'}`
}

// --- Edit Controls ---
const enterEditMode = () => {
  if (!part.value) return
  
  formData.value = {
    name: part.value.name,
    description: part.value.description,
    stock: part.value.stock,
    imageLink: part.value.imageLink,
    categoryId: part.value.category?.id || null,
    drawerId: part.value.drawer?.id || null,
    sectionNumber: part.value.sectionNumber || null,
  }
  
  formLinks.value = part.value.links ? [...part.value.links] : []

  // Set autocomplete selected values
  selectedCategory.value = part.value.category || null
  selectedDrawer.value = part.value.drawer ? { ...part.value.drawer, label: String(part.value.drawer.number) } : null
  
  if (part.value.notes) {
    enableNotes.value = true
    formNotes.value = part.value.notes
  } else {
    enableNotes.value = false
    formNotes.value = ''
  }
  
  saveError.value = null
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
}

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
    saveError.value = null
  } catch (e: any) {
    console.error("Upload failed", e)
    saveError.value = e.message || "Failed to upload image"
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
    saveError.value = null
  } catch (e: any) {
    console.error("Upload failed", e)
    saveError.value = e.message || "Failed to upload documentation document"
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

const addLink = () => formLinks.value.push('')
const removeLink = (index: number) => formLinks.value.splice(index, 1)

// --- Quick Actions ---
const quickUpdateStock = async (newStock: number) => {
  if (!part.value || newStock < 0) return
  
  updatingStock.value = true
  
  const payload: PartRequest = {
    name: part.value.name,
    description: part.value.description,
    stock: newStock,
    imageLink: part.value.imageLink,
    categoryId: part.value.category?.id || null,
    drawerId: part.value.drawer?.id || null,
    sectionNumber: part.value.sectionNumber || null,
    links: part.value.links,
    notes: part.value.notes
  }
  
  try {
    const updated = await apiService.updatePart(id, payload)
    part.value = updated
  } catch (err) {
    console.error("Rapid stock update failed:", err)
  } finally {
    updatingStock.value = false
  }
}

const saveEdits = async () => {
  if (!formData.value.name || !formData.value.description) {
    saveError.value = "Name and description are required"
    return
  }
  
  saving.value = true
  saveError.value = null
  
  const payload = { ...formData.value }
  
  if (enableNotes.value && formNotes.value.trim() !== '') {
    payload.notes = formNotes.value
  } else {
    payload.notes = null
  }
  
  payload.links = formLinks.value.filter(link => link.trim() !== '')
  
  try {
    const updated = await apiService.updatePart(id, payload)
    part.value = updated
    isEditing.value = false
  } catch (e: any) {
    saveError.value = e.message || "Failed to save part"
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="component-detail-view">
    <div class="header-section">
      <div class="flex items-center gap-3">
        <Button icon="pi pi-arrow-left" variant="text" rounded aria-label="Go Back" @click="goBack" :disabled="isEditing" />
        <h2 class="page-title">{{ part ? part.name : 'Loading Details...' }}</h2>
      </div>
      <div v-if="part" class="flex gap-2">
        <Button v-if="!isEditing" icon="pi pi-pencil" label="Edit" outlined @click="enterEditMode" />
        <template v-else>
          <Button icon="pi pi-times" label="Cancel" severity="secondary" outlined @click="cancelEdit" :disabled="saving" />
          <Button icon="pi pi-check" label="Save Changes" @click="saveEdits" :loading="saving" />
        </template>
      </div>
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
      <Message v-if="saveError" severity="error" class="mb-4">{{ saveError }}</Message>

      <!-- Top Section -->
      <div class="top-split">
        
        <!-- Left: Image Box -->
        <div class="image-preview-box"
             :class="{ 'interactive-drop': isEditing, 'drop-active': imageDragOver }"
             @click="isEditing ? triggerImageUpload() : null"
             @dragover.prevent="isEditing ? imageDragOver = true : null"
             @dragleave.prevent="isEditing ? imageDragOver = false : null"
             @drop.prevent="isEditing ? onImageDrop($event) : null"
             :title="isEditing ? 'Click or Drag to Upload' : ''">
             
          <img v-if="!isEditing && part.imageLink" :src="part.imageLink" :alt="part.name" class="preview-img" />
          <img v-else-if="isEditing && formData.imageLink" :src="formData.imageLink" alt="Preview" class="preview-img" />
          
          <div v-else class="upload-placeholder">
            <i :class="isEditing ? 'pi pi-cloud-upload' : 'pi pi-image'" class="upload-icon"></i>
            <span class="upload-text">{{ isEditing ? 'Click or Drag Image Here' : 'No image available' }}</span>
          </div>

          <div v-if="uploadingImage" class="uploading-overlay">
            <i class="pi pi-spin pi-spinner text-3xl"></i>
          </div>
        </div>

        <input v-if="isEditing" type="file" ref="imageFileInput" accept="image/*" style="display: none;" @change="onImageFileChange" />

        <!-- Right: Basic Details Grid -->
        <div class="basic-details">
          
          <template v-if="!isEditing">
            <div class="detail-group">
              <span class="label">Component Name</span>
              <div class="value text-xl font-bold">{{ part.name }}</div>
            </div>
            
            <div class="detail-group">
              <span class="label">Stock Level</span>
              <div class="flex items-center gap-3 mt-1">
                <Button 
                  icon="pi pi-minus" 
                  severity="secondary" 
                  rounded 
                  outlined 
                  size="small" 
                  style="width: 2rem; height: 2rem; padding: 0"
                  :disabled="part.stock <= 0 || updatingStock" 
                  @click="quickUpdateStock(part.stock - 1)" 
                />
                <div class="value stock-value" :class="{'text-red-500': part.stock < 10, 'text-green-500': part.stock >= 10}">
                  {{ part.stock }} <span class="text-sm ml-1 text-surface-500 font-normal">units</span>
                </div>
                <Button 
                  icon="pi pi-plus" 
                  severity="secondary" 
                  rounded 
                  outlined 
                  size="small" 
                  style="width: 2rem; height: 2rem; padding: 0"
                  :disabled="updatingStock" 
                  @click="quickUpdateStock(part.stock + 1)" 
                />
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
          </template>

          <template v-else>
            <!-- Edit Form Replacements -->
             <div class="flex flex-col gap-2">
                <label for="name" class="font-semibold">Component Name <span class="text-red-500">*</span></label>
                <InputText id="name" v-model="formData.name" required class="w-full" />
              </div>

              <div class="flex flex-col gap-2">
                <label for="stock" class="font-semibold">Current Stock Level <span class="text-red-500">*</span></label>
                <InputNumber id="stock" v-model="formData.stock" showButtons :min="0" required class="w-full" />
              </div>

              <div class="flex flex-col gap-2">
                <div class="flex items-center gap-2">
                  <label for="category" class="font-semibold">Category</label>
                  <small v-if="categoryQuery.trim() && filteredCategories.length === 0 && !creatingInline" class="hint-text">— Press Enter to create "{{ categoryQuery }}"</small>
                </div>
                <AutoComplete 
                  id="category" 
                  v-model="selectedCategory" 
                  :suggestions="filteredCategories" 
                  optionLabel="name" 
                  placeholder="Type to search or create..." 
                  :loading="creatingInline" 
                  class="w-full"
                  @complete="searchCategories"
                  @item-select="onCategorySelect"
                  @keydown.enter="onCategoryEnter"
                  @keydown.tab="onCategoryTab"
                  forceSelection
                />
              </div>

              <div class="flex flex-col gap-2">
                <div class="flex items-center gap-2">
                  <label for="drawer" class="font-semibold">Location Drawer</label>
                  <small v-if="drawerQuery.trim() && filteredDrawers.length === 0 && !creatingInline && !isNaN(parseInt(drawerQuery))" class="hint-text">— Press Enter to create Drawer {{ drawerQuery }}</small>
                </div>
                <AutoComplete 
                  id="drawer" 
                  v-model="selectedDrawer" 
                  :suggestions="filteredDrawers" 
                  optionLabel="label" 
                  placeholder="Type drawer number..." 
                  :loading="creatingInline" 
                  class="w-full"
                  @complete="searchDrawers"
                  @item-select="onDrawerSelect"
                  @keydown.enter="onDrawerEnter"
                  @keydown.tab="onDrawerTab"
                  forceSelection
                >
                  <template #option="slotProps">
                    <div>Drawer {{ slotProps.option.number }}</div>
                  </template>
                </AutoComplete>
              </div>

              <div class="flex flex-col gap-2">
                <label for="sectionNumber" class="font-semibold">Section Number</label>
                <InputNumber id="sectionNumber" v-model="formData.sectionNumber" showButtons :min="1" class="w-full" />
              </div>

              <div class="flex flex-col gap-2 mt-2">
                <label for="imageLinkRaw" class="text-sm font-semibold">Explicit Image URL Binding</label>
                <InputText id="imageLinkRaw" v-model="formData.imageLink" placeholder="https://..." class="w-full" />
              </div>
          </template>

        </div>
      </div>
      
      <div class="divider full-width"></div>
      
      <!-- Bottom Section -->
      <div class="details-section">
        
        <template v-if="!isEditing">
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
        </template>

        <template v-else>
           <div class="flex flex-col gap-2">
            <label for="description" class="font-semibold">Description <span class="text-red-500">*</span></label>
            <Textarea id="description" v-model="formData.description" rows="5" required class="w-full" />
          </div>

          <div class="flex flex-col gap-2 mt-4">
            <div class="flex items-center justify-between">
              <label class="font-semibold mb-0">Documentation Links</label>
              <Button icon="pi pi-plus" label="Add Link" size="small" variant="text" @click="addLink" />
            </div>
            
            <Message v-if="formLinks.length === 0" severity="info" variant="simple" size="small">
              No links attached.
            </Message>

            <div class="flex flex-col gap-3">
              <div 
                v-for="(link, index) in formLinks" 
                :key="index" 
                class="flex gap-2 items-center drop-zone transition-colors"
                :class="{ 'drop-active': linkDragOver[index] }"
                @dragover.prevent="linkDragOver[index] = true"
                @dragleave.prevent="linkDragOver[index] = false"
                @drop.prevent="(e) => onLinkDrop(e, index)"
              >
                <InputText v-model="formLinks[index]" placeholder="Drag document here or input Link URL" class="flex-1" />
                
                <input 
                  type="file" 
                  :ref="(el) => { if (el) linkFileInputs[index] = el }" 
                  style="display: none;" 
                  @change="(e) => onLinkFileChange(e, index)" 
                />
                
                <Button 
                  icon="pi pi-upload" 
                  severity="secondary" 
                  aria-label="Upload Document" 
                  @click="triggerLinkUpload(index)" 
                  :loading="uploadingLinkIndex === index" 
                />
                <Button icon="pi pi-trash" severity="danger" variant="text" @click="removeLink(index)" />
              </div>
            </div>
          </div>

          <div class="flex flex-col gap-2 mt-4">
              <div class="flex items-center justify-between">
              <label class="font-semibold mb-0">Private Notes</label>
              <Button 
                :icon="enableNotes ? 'pi pi-minus' : 'pi pi-plus'" 
                :label="enableNotes ? 'Disable Notes' : 'Enable Notes'" 
                size="small" 
                :severity="enableNotes ? 'danger' : 'secondary'"
                variant="text" 
                @click="enableNotes = !enableNotes" 
              />
            </div>
            <Textarea 
              v-if="enableNotes"
              id="notes" 
              v-model="formNotes" 
              rows="4" 
              placeholder="Add personal hints, thoughts, or custom data here..." 
              class="w-full"
            />
          </div>
        </template>

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
  position: relative;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
}

.interactive-drop {
  border: 2px dashed var(--p-surface-300);
  cursor: pointer;
  transition: all 0.2s ease;
}

.interactive-drop:hover {
  border-color: var(--p-primary-400);
  background-color: var(--p-surface-100);
}

.drop-zone {
  border-radius: 6px;
  border: 2px dashed transparent;
  padding: 0.25rem;
  margin: -0.25rem;
}

.drop-active {
  border-color: var(--p-primary-500);
  background-color: var(--p-primary-50);
}

.uploading-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: rgba(255, 255, 255, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--p-primary-color);
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
.flex-col { flex-direction: column; }
.items-center { align-items: center; }
.justify-between { justify-content: space-between; }
.gap-2 { gap: 0.5rem; }
.gap-3 { gap: 0.75rem; }
.text-xl { font-size: 1.25rem; }
.font-bold { font-weight: 700; }
.font-medium { font-weight: 500; }
.font-semibold { font-weight: 600; }
.text-red-500 { color: var(--p-red-500, #ef4444) !important; }
.text-green-500 { color: var(--p-green-600, #16a34a) !important; }
.mr-2 { margin-right: 0.5rem; }
.mr-3 { margin-right: 0.75rem; }
.mb-0 { margin-bottom: 0px; }
.mb-4 { margin-bottom: 1rem; }
.ml-auto { margin-left: auto; }
.mt-1 { margin-top: 0.25rem; }
.mt-2 { margin-top: 0.5rem; }
.mt-4 { margin-top: 1rem; }
.w-full { width: 100%; }
.flex-1 { flex: 1; }
.transition-colors { transition: all 0.2s ease; }

.text-sm { font-size: 0.875rem; }
.text-lg { font-size: 1.125rem; }
.text-3xl { font-size: 2rem; }

.hint-text {
  color: var(--p-primary-500);
  font-style: italic;
  font-size: 0.8rem;
  white-space: nowrap;
}

/* Make AutoComplete input stretch full width */
:deep(.p-autocomplete) {
  width: 100% !important;
}
:deep(.p-autocomplete-input) {
  width: 100% !important;
}

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
  
  .interactive-drop:hover {
    border-color: var(--p-primary-500);
    background-color: var(--p-surface-700);
  }
  .drop-active {
    background-color: rgba(16, 185, 129, 0.1);
  }
  .uploading-overlay {
    background-color: rgba(15, 23, 42, 0.7);
  }
}
</style>
