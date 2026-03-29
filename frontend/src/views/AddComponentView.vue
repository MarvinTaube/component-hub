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
import Card from 'primevue/card'
import Message from 'primevue/message'
import Fieldset from 'primevue/fieldset'
import FloatLabel from 'primevue/floatlabel'

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

const showCategoryDialog = ref(false)
const newCategoryName = ref<string>('')
const creatingCategory = ref(false)
const categoryError = ref<string | null>(null)

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

// --- Inline Dialog Creations ---
const createNewCategory = async () => {
  if (!newCategoryName.value.trim()) {
    categoryError.value = "Category name is required"
    return
  }
  
  categoryError.value = null
  creatingCategory.value = true
  
  try {
    const newCategory = await apiService.createCategory(newCategoryName.value.trim())
    categories.value.push(newCategory)
    categories.value.sort((a, b) => a.name.localeCompare(b.name))
    formData.value.categoryId = newCategory.id
    showCategoryDialog.value = false
    newCategoryName.value = ''
  } catch (e: any) {
    categoryError.value = e.message || "Failed to create category"
  } finally {
    creatingCategory.value = false
  }
}

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
  <div class="page-container">
    <Card class="form-card">
      <template #title>
        <div class="text-2xl font-bold mb-4">Add New Component</div>
      </template>

      <template #content>
        <form @submit.prevent="submitForm" id="componentForm">
          <div class="form-layout">
            
            <!-- Hero Image Column -->
            <div class="image-column">
              <div 
                class="image-preview transition-colors"
                :class="{ 'drop-active': imageDragOver }"
                @click="triggerImageUpload"
                @dragover.prevent="imageDragOver = true"
                @dragleave.prevent="imageDragOver = false"
                @drop.prevent="onImageDrop"
                title="Click or Drag to Upload"
              >
                <img v-if="formData.imageLink" :src="formData.imageLink" alt="Preview" class="preview-img" />
                <div v-else class="upload-placeholder">
                  <i class="pi pi-cloud-upload text-4xl mb-2"></i>
                  <span>Click or drag image here</span>
                </div>
                
                <div v-if="uploadingImage" class="uploading-overlay">
                  <i class="pi pi-spin pi-spinner text-3xl"></i>
                </div>
              </div>

              <input type="file" ref="imageFileInput" accept="image/*" style="display: none;" @change="onImageFileChange" />

              <div class="flex flex-col gap-2 w-full mt-2">
                <label for="imageLink" class="text-sm font-semibold">Or provide image URL</label>
                <InputText id="imageLink" v-model="formData.imageLink" placeholder="https://..." class="w-full" />
              </div>
            </div>

            <!-- Basic Details Column -->
            <div class="details-column">
              <div class="flex flex-col gap-2">
                <label for="name" class="font-semibold">Component Name <span class="text-red-500">*</span></label>
                <InputText id="name" v-model="formData.name" placeholder="e.g. 10k Resistor" required class="w-full" />
              </div>

              <div class="flex flex-col gap-2">
                <label for="stock" class="font-semibold">Initial Stock <span class="text-red-500">*</span></label>
                <InputNumber id="stock" v-model="formData.stock" showButtons :min="0" required class="w-full" />
              </div>

              <div class="flex flex-col gap-2">
                <label for="category" class="font-semibold">Category</label>
                <div class="flex gap-2 w-full">
                  <Select 
                    id="category" 
                    v-model="formData.categoryId" 
                    :options="categories" 
                    optionLabel="name" 
                    optionValue="id" 
                    placeholder="Select a Category" 
                    :loading="loadingOptions" 
                    class="flex-1"
                  />
                  <Button icon="pi pi-plus" @click="showCategoryDialog = true" aria-label="Create New Category" />
                </div>
              </div>

              <div class="flex flex-col gap-2">
                <label for="drawer" class="font-semibold">Location Drawer</label>
                <div class="flex gap-2 w-full">
                  <Select 
                    id="drawer" 
                    v-model="formData.drawerId" 
                    :options="drawers" 
                    optionLabel="number" 
                    optionValue="id" 
                    placeholder="Select a Drawer" 
                    :loading="loadingOptions" 
                    class="flex-1"
                  >
                    <template #option="slotProps">
                      <div>Drawer {{ slotProps.option.number }}</div>
                    </template>
                    <template #value="slotProps">
                      <div v-if="slotProps.value">
                        Drawer {{ drawers.find(d => d.id === slotProps.value)?.number }}
                      </div>
                      <span v-else>Select a Drawer</span>
                    </template>
                  </Select>
                  <Button icon="pi pi-plus" @click="showDrawerDialog = true" aria-label="Create New Drawer" />
                </div>
              </div>

              <div class="flex flex-col gap-2">
                <label for="sectionNumber" class="font-semibold">Section Number</label>
                <InputNumber id="sectionNumber" v-model="formData.sectionNumber" showButtons :min="1" class="w-full" />
              </div>
            </div>

          </div><!-- end form-layout -->

          <div class="mt-6">
            <Fieldset legend="Additional Details">
              <div class="flex flex-col gap-4">
                
                <div class="flex flex-col gap-2">
                  <label for="description" class="font-semibold">Description <span class="text-red-500">*</span></label>
                  <Textarea id="description" v-model="formData.description" rows="4" required class="w-full" />
                </div>

                <div class="flex flex-col gap-2 mt-4">
                  <div class="flex items-center justify-between">
                    <label class="font-semibold mb-0">Documentation Links</label>
                    <Button icon="pi pi-plus" label="Add Link" size="small" variant="text" @click="addLink" />
                  </div>
                  
                  <Message v-if="formLinks.length === 0" severity="info" variant="simple" size="small">
                    No links added yet.
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
                    rows="3" 
                    placeholder="Add personal hints, thoughts, or custom data here..." 
                    class="w-full"
                  />
                </div>

              </div>
            </Fieldset>
          </div>

          <Message v-if="submitError" severity="error" class="mt-4">{{ submitError }}</Message>

        </form>
      </template>

      <template #footer>
        <div class="flex justify-end gap-3 pt-2">
          <Button label="Cancel" severity="secondary" @click="cancel" outlined />
          <Button label="Save Component" icon="pi pi-check" :loading="submitting" @click="submitForm" />
        </div>
      </template>
    </Card>
    
    <!-- Drawer Creation Dialog -->
    <Dialog v-model:visible="showDrawerDialog" modal header="Create New Drawer" :style="{ width: '25rem' }">
      <div class="flex flex-col gap-2 mt-2">
        <label for="newDrawerNumber" class="font-semibold">Drawer Number</label>
        <InputNumber id="newDrawerNumber" v-model="newDrawerNumber" showButtons :min="1" class="w-full" autofocus />
      </div>
      <Message v-if="drawerError" severity="error" class="mt-4" size="small">{{ drawerError }}</Message>
      <div class="flex justify-end gap-2 mt-4">
        <Button label="Cancel" severity="secondary" @click="showDrawerDialog = false" variant="text" />
        <Button label="Create" @click="createNewDrawer" :loading="creatingDrawer" />
      </div>
    </Dialog>

    <!-- Category Creation Dialog -->
    <Dialog v-model:visible="showCategoryDialog" modal header="Create New Category" :style="{ width: '25rem' }">
      <div class="flex flex-col gap-2 mt-2">
        <label for="newCategoryName" class="font-semibold">Category Name</label>
        <InputText id="newCategoryName" v-model="newCategoryName" placeholder="e.g. Resistors" class="w-full" autofocus @keyup.enter="createNewCategory" />
      </div>
      <Message v-if="categoryError" severity="error" class="mt-4" size="small">{{ categoryError }}</Message>
      <div class="flex justify-end gap-2 mt-4">
        <Button label="Cancel" severity="secondary" @click="showCategoryDialog = false" variant="text" />
        <Button label="Create" @click="createNewCategory" :loading="creatingCategory" />
      </div>
    </Dialog>
  </div>
</template>

<style scoped>
.page-container {
  max-width: 900px;
  margin: 0 auto;
  padding-bottom: 2rem;
}

.form-layout {
  display: flex;
  flex-wrap: wrap;
  gap: 2.5rem;
  align-items: flex-start;
}

.image-column {
  flex: 1 1 250px;
  max-width: 380px;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.details-column {
  flex: 2 1 300px;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.image-preview {
  width: 100%;
  aspect-ratio: 1 / 1;
  background-color: var(--p-surface-50);
  border: 2px dashed var(--p-surface-300);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  cursor: pointer;
}

.image-preview:hover {
  border-color: var(--p-primary-400);
  background-color: var(--p-surface-100);
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background-color: var(--p-surface-50);
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: var(--p-surface-500);
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

.transition-colors {
  transition: all 0.2s ease;
}

/* Utility Flex Classes used to augment layout organically without arbitrary margin blocks */
.flex { display: flex; }
.flex-col { flex-direction: column; }
.items-center { align-items: center; }
.justify-between { justify-content: space-between; }
.justify-end { justify-content: flex-end; }
.gap-2 { gap: 0.5rem; }
.gap-3 { gap: 0.75rem; }
.gap-4 { gap: 1rem; }
.flex-1 { flex: 1; }
.w-full { width: 100%; }
.mt-2 { margin-top: 0.5rem; }
.mt-4 { margin-top: 1rem; }
.mt-6 { margin-top: 1.5rem; }
.mb-0 { margin-bottom: 0; }
.mb-2 { margin-bottom: 0.5rem; }
.mb-4 { margin-bottom: 1rem; }
.pt-2 { padding-top: 0.5rem; }
.text-2xl { font-size: 1.5rem; }
.text-3xl { font-size: 2rem; }
.text-4xl { font-size: 2.25rem; }
.text-sm { font-size: 0.875rem; }
.font-semibold { font-weight: 600; }
.font-bold { font-weight: 700; }
.text-red-500 { color: var(--p-red-500); }

/* Ensure Fieldset margins natively snap */
:deep(.p-fieldset-content) {
  padding: 1.25rem;
}

@media (prefers-color-scheme: dark) {
  .image-preview {
    background-color: var(--p-surface-800);
    border-color: var(--p-surface-700);
  }
  .image-preview:hover {
    border-color: var(--p-primary-500);
    background-color: var(--p-surface-700);
  }
  .preview-img {
    background-color: var(--p-surface-800);
  }
  .uploading-overlay {
    background-color: rgba(15, 23, 42, 0.7);
  }
  .drop-active {
    background-color: rgba(16, 185, 129, 0.1);
  }
}
</style>
