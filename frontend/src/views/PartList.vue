<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { partService, categoryService, tagService, fileService, drawerService } from '../services/api';
import { useRouter } from 'vue-router';
import InputText from 'primevue/inputtext';
import Select from 'primevue/select';
import Card from 'primevue/card';
import Tag from 'primevue/tag';
import ProgressSpinner from 'primevue/progressspinner';
import Message from 'primevue/message';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import InputNumber from 'primevue/inputnumber';
import Textarea from 'primevue/textarea';
import MultiSelect from 'primevue/multiselect';
import Divider from 'primevue/divider';

const parts = ref<any[]>([]);
const categories = ref<any[]>([]);
const tags = ref<any[]>([]);
const drawers = ref<any[]>([]);
const loading = ref(true);
const error = ref('');
const showCreateModal = ref(false);
const creating = ref(false);
const fileInput = ref<HTMLInputElement | null>(null);
const uploadingImage = ref(false);

const newPartForm = ref({
  name: '',
  description: '',
  stock: 0,
  categoryId: null as number | null,
  tagIds: [] as number[],
  imageLink: '',
  drawerId: null as number | null,
  sectionNumber: null as number | null,
  notes: '',
  links: [] as string[]
});

const searchQuery = ref('');
const selectedCategory = ref(null);
const selectedTag = ref(null);

const router = useRouter();

onMounted(async () => {
  try {
    const [partsRes, catRes, tagRes, drawerRes] = await Promise.all([
      partService.getAll(),
      categoryService.getAll(),
      tagService.getAll(),
      drawerService.getAll()
    ]);
    parts.value = partsRes.data;
    categories.value = catRes.data;
    tags.value = tagRes.data;
    drawers.value = drawerRes.data;
  } catch (err) {
    console.error(err);
    error.value = 'Failed to load data from backend.';
  } finally {
    loading.value = false;
  }
});

const filteredParts = computed(() => {
  return parts.value.filter(part => {
    const matchesText = part.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
                        part.description.toLowerCase().includes(searchQuery.value.toLowerCase());
    const matchesCategory = !selectedCategory.value || part.category?.id == selectedCategory.value;
    const matchesTag = !selectedTag.value || part.tag?.some((t: any) => t.id == selectedTag.value);
    
    return matchesText && matchesCategory && matchesTag;
  });
});

const goToDetail = (id: number) => {
  router.push({ name: 'part-detail', params: { id } });
};

const getImageUrl = (imageLink: string | null) => {
  if (!imageLink) return '/placeholder.png';
  if (imageLink.startsWith('http')) return imageLink;
  if (imageLink.startsWith('/api/files/getFile')) {
    // Already a backend link, just ensure it has the right base URL if needed
    // Assuming the DB stores it relative to /api, or as a full path.
    // The issue description says: /api/files/getFile?file=...&type=itemImage
    return `http://localhost:8080${imageLink}`;
  }
  return `/images/${imageLink}`;
};

const openCreateModal = () => {
  newPartForm.value = {
    name: '',
    description: '',
    stock: 0,
    categoryId: null,
    tagIds: [],
    imageLink: '',
    drawerId: null,
    sectionNumber: null,
    notes: '',
    links: []
  };
  showCreateModal.value = true;
};

const handleImageUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (!target.files || target.files.length === 0) return;
  const file = target.files[0];
  if (!file) return;
  
  try {
    uploadingImage.value = true;
    const res = await fileService.uploadFile(file, 'itemImage');
    newPartForm.value.imageLink = `/api/files/getFile?file=${res.data.filename}&type=itemImage`;
  } catch (err) {
    console.error('Failed to upload image:', err);
    alert('Failed to upload image.');
  } finally {
    uploadingImage.value = false;
    if (fileInput.value) fileInput.value.value = ''; // reset
  }
};

const triggerFileUpload = () => {
  fileInput.value?.click();
};

const closeCreateModal = () => {
  showCreateModal.value = false;
};

const saveNewPart = async () => {
  if (!newPartForm.value.name) {
    alert('Name is required.');
    return;
  }
  
  try {
    creating.value = true;
    const res = await partService.create(newPartForm.value);
    parts.value.push(res.data);
    showCreateModal.value = false;
  } catch (err) {
    console.error(err);
    alert('Failed to create part.');
  } finally {
    creating.value = false;
  }
};

const addCreateLink = () => {
  newPartForm.value.links.push('');
};

const removeCreateLink = (index: number) => {
  newPartForm.value.links.splice(index, 1);
};
</script>

<template>
  <div class="part-list-container py-6 px-4 md:px-6 lg:px-8">
    <div class="header flex flex-column md:flex-row justify-content-between align-items-start md:align-items-center mb-6 gap-4">
      <div>
        <h1 class="text-5xl font-bold text-900 m-0 tracking-tight">Component Hub</h1>
        <p class="text-500 text-lg mt-2 m-0 line-height-3">Manage and browse your microelectronic inventory.</p>
      </div>
      <Button label="New Component" icon="pi pi-plus" class="p-button-rounded shadow-2 px-4 py-2 font-semibold" @click="openCreateModal" />
    </div>
    
    <div class="search-section mb-6 bg-white p-3 sm:p-4 border-round-2xl shadow-1">
      <div class="flex flex-column md:flex-row gap-4">
        <div class="relative flex-grow-1 search-input-wrapper flex align-items-center">
          <i class="pi pi-search text-500 ml-4 text-xl"></i>
          <InputText v-model="searchQuery" placeholder="Search components by name or description..." class="w-full border-none py-3 px-3 text-lg font-medium" />
        </div>
        <div class="flex flex-column sm:flex-row gap-3">
          <Select v-model="selectedCategory" :options="categories" optionLabel="name" optionValue="id" placeholder="All Categories" showClear class="w-full sm:w-16rem border-none bg-gray-50 hover:bg-gray-100 border-round-xl py-2 shadow-none font-medium" />
          <Select v-model="selectedTag" :options="tags" optionLabel="name" optionValue="id" placeholder="All Tags" showClear class="w-full sm:w-16rem border-none bg-gray-50 hover:bg-gray-100 border-round-xl py-2 shadow-none font-medium" />
        </div>
      </div>
    </div>

    <div v-if="loading" class="flex justify-content-center p-8">
      <ProgressSpinner strokeWidth="4" animationDuration=".5s" class="w-4rem h-4rem" />
    </div>
    <div v-else-if="error" class="mb-4">
      <Message severity="error" class="border-round-xl shadow-1">{{ error }}</Message>
    </div>
    
    <div v-else>
      <div class="grid">
        <div v-for="part in filteredParts" :key="part.id" class="col-12 sm:col-6 md:col-4 xl:col-3 p-3 flex">
          <Card class="part-card w-full cursor-pointer border-none shadow-2 hover:shadow-4 overflow-hidden border-round-2xl transition-all transition-duration-300 flex flex-column" @click="goToDetail(part.id)">
            <template #header>
              <div class="image-wrapper relative bg-gray-50 border-bottom-1 surface-border">
                <img :src="getImageUrl(part.imageLink)" :alt="part.name" class="part-image absolute top-0 left-0 w-full h-full object-cover" />
                <div class="overlay-gradient absolute bottom-0 left-0 w-full h-6rem z-1 pointer-events-none"></div>
                
                <div class="absolute top-0 right-0 p-3 z-2">
                   <Tag v-if="part.stock === 0" severity="danger" value="Out of Stock" rounded class="shadow-2 font-bold px-3 py-1 text-xs uppercase tracking-wider" />
                   <Tag v-else-if="part.stock < 5" severity="warn" value="Low Stock" rounded class="shadow-2 font-bold px-3 py-1 text-xs uppercase tracking-wider bg-orange-500 text-white border-none" />
                </div>
                
                <div class="absolute bottom-0 left-0 p-3 z-2 w-full">
                   <span class="category-badge shadow-2">{{ part.category?.name || 'Uncategorized' }}</span>
                </div>
              </div>
            </template>
            <template #title>
                <!-- Omitted title to place everything in content cleanly -->
            </template>
            <template #content>
              <div class="flex flex-column h-full">
                <h2 class="part-name m-0 mb-2 text-xl font-bold text-900 line-height-2">{{ part.name }}</h2>
                <div class="flex flex-wrap gap-2 mb-3">
                  <Tag v-for="t in part.tag" :key="t.id" :value="t.name" rounded class="text-xs font-medium px-2 py-1" :style="{ backgroundColor: t.color + '15', color: t.color, border: '1px solid ' + t.color + '30' }" />
                </div>
                <p class="m-0 text-500 text-sm line-height-3 line-clamp-2 flex-grow-1">
                  {{ part.description || 'No description provided.' }}
                </p>
              </div>
            </template>
            <template #footer>
              <div class="flex justify-content-between align-items-center pt-3 border-top-1 surface-border mt-auto">
                <div class="flex align-items-baseline gap-1">
                  <span class="text-2xl font-black" :class="part.stock < 5 ? 'text-red-500' : 'text-900'">{{ part.stock }}</span>
                  <span class="text-500 text-xs font-bold uppercase tracking-widest">units</span>
                </div>
                <div v-if="part.drawer" class="flex align-items-center gap-2 text-700 bg-gray-100 px-3 py-1 border-round-xl shadow-1">
                  <i class="pi pi-box text-blue-500 text-sm"></i>
                  <span class="text-xs font-bold">{{ part.drawer.number }}-{{ part.sectionNumber }}</span>
                </div>
              </div>
            </template>
          </Card>
        </div>
      </div>
      <div v-if="filteredParts.length === 0" class="flex flex-column align-items-center justify-content-center py-8 bg-white border-round-2xl shadow-1 mt-4">
        <div class="surface-100 w-6rem h-6rem border-circle flex align-items-center justify-content-center mb-4">
          <i class="pi pi-search text-400 text-4xl"></i>
        </div>
        <h3 class="text-xl text-900 font-semibold m-0 mb-2">No components found</h3>
        <p class="text-lg text-500 m-0 mb-4 text-center max-w-15rem">We couldn't find any items matching your current filters.</p>
        <Button label="Clear Filters" icon="pi pi-filter-slash" class="p-button-outlined p-button-secondary border-round-xl" @click="searchQuery=''; selectedCategory=null; selectedTag=null;" />
      </div>
    </div>
    
    <!-- Create Component Modal -->
    <Dialog v-model:visible="showCreateModal" modal header="Add New Component" :style="{ width: '50rem' }" :breakpoints="{ '1199px': '75vw', '575px': '90vw' }" class="p-fluid">
      <div class="grid mt-2">
        <div class="col-12 field mb-4">
          <label class="block font-semibold mb-2 text-700 text-color">Name *</label>
          <InputText v-model="newPartForm.name" placeholder="Component Name" class="p-3 border-round-lg bg-gray-50 border-none transition-colors" />
        </div>
        
        <div class="col-12 field mb-4">
          <label class="block font-semibold mb-2 text-700 text-color">Description</label>
          <Textarea v-model="newPartForm.description" rows="3" autoResize class="p-3 border-round-lg bg-gray-50 border-none transition-colors" />
        </div>

        <div class="col-12 sm:col-6 field mb-4 pr-0 sm:pr-3">
          <label class="block font-semibold mb-2 text-700 text-color">Category</label>
          <Select v-model="newPartForm.categoryId" :options="categories" optionLabel="name" optionValue="id" placeholder="Select Category" showClear class="border-round-lg bg-gray-50 border-none transition-colors py-2" />
        </div>

        <div class="col-12 sm:col-6 field mb-4 pl-0 sm:pl-3">
          <label class="block font-semibold mb-2 text-700 text-color">Stock Level</label>
          <InputNumber v-model="newPartForm.stock" showButtons :min="0" class="w-full" inputClass="border-round-lg bg-gray-50 border-none transition-colors" />
        </div>

        <div class="col-12 field mb-4">
          <label class="block font-semibold mb-2 text-700 text-color">Tags</label>
          <MultiSelect v-model="newPartForm.tagIds" :options="tags" optionLabel="name" optionValue="id" placeholder="Select Tags" display="chip" class="border-round-lg bg-gray-50 border-none transition-colors py-2" />
        </div>
        
        <Divider class="col-12 my-3" />
        
        <div class="col-12 sm:col-6 field mb-4 pr-0 sm:pr-3">
          <label class="block font-semibold mb-2 text-700 text-color">Drawer</label>
          <Select v-model="newPartForm.drawerId" :options="drawers" optionLabel="number" optionValue="id" placeholder="Select Drawer" showClear class="border-round-lg bg-gray-50 border-none transition-colors py-2" />
        </div>

        <div class="col-12 sm:col-6 field mb-4 pl-0 sm:pl-3">
          <label class="block font-semibold mb-2 text-700 text-color">Section Number</label>
          <InputNumber v-model="newPartForm.sectionNumber" :min="1" class="w-full" inputClass="border-round-lg bg-gray-50 border-none transition-colors" />
        </div>

        <div class="col-12 field mb-4">
          <label class="block font-semibold mb-2 text-700 text-color">Image URL or Upload</label>
          <div class="p-inputgroup flex-1">
            <InputText v-model="newPartForm.imageLink" placeholder="e.g. https://... or resistor.jpg" class="p-3 border-round-left-lg bg-gray-50 border-none transition-colors" />
            <Button icon="pi pi-upload" label="Upload" class="p-button-secondary border-round-right-lg shadow-none px-4" @click="triggerFileUpload" :loading="uploadingImage" />
          </div>
          <input type="file" accept="image/*" class="hidden" ref="fileInput" @change="handleImageUpload" />
        </div>

        <div class="col-12 field mb-4">
          <label class="block font-semibold mb-2 text-700 text-color flex justify-content-between align-items-center">
            Technical Links
            <Button icon="pi pi-plus" variant="text" size="small" class="p-button-sm w-2rem h-2rem p-0 p-button-secondary" @click="addCreateLink" />
          </label>
          <div class="flex flex-column gap-2 mb-2">
            <div v-for="(link, index) in newPartForm.links" :key="index" class="flex gap-2 align-items-center">
              <span class="font-bold text-400 text-sm w-1rem text-right">{{ Number(index) + 1 }}.</span>
              <InputText v-model="newPartForm.links[index as number]" placeholder="https://..." class="flex-1 p-2 border-round-lg bg-gray-50 border-none transition-colors" />
              <Button icon="pi pi-trash" class="p-button-danger p-button-text p-button-rounded" @click="removeCreateLink(index as number)" />
            </div>
            <Message v-if="newPartForm.links.length === 0" severity="secondary" :closable="false" class="m-0 border-round-lg bg-gray-50 p-3 shadow-none text-600 font-medium">No external links added.</Message>
          </div>
        </div>

        <div class="col-12 field mb-0">
          <label class="block font-semibold mb-2 text-700 text-color">Notes (Internal)</label>
          <Textarea v-model="newPartForm.notes" rows="3" autoResize class="p-3 border-round-lg bg-yellow-50 border-none focus:bg-yellow-100 transition-colors" placeholder="Any internal notes or warnings..." style="color: var(--text-color) !important" />
        </div>
      </div>

      <template #footer>
        <div class="flex justify-content-end gap-3 pt-4">
          <Button label="Cancel" icon="pi pi-times" @click="closeCreateModal" class="p-button-text p-button-secondary font-semibold border-none bg-transparent" :disabled="creating" />
          <Button label="Save Component" icon="pi pi-check" @click="saveNewPart" class="p-button-primary shadow-2 font-semibold px-4 border-round-xl" :loading="creating" />
        </div>
      </template>
    </Dialog>
  </div>
</template>

<style scoped>
.part-list-container {
  max-width: 1400px;
  margin: 0 auto;
}

.bg-white { background-color: var(--surface-0) !important; }
.bg-gray-50 { background-color: var(--surface-50) !important; }
.bg-gray-100 { background-color: var(--surface-100) !important; }

.text-900 { color: var(--text-color) !important; }
.text-700 { color: var(--text-color) !important; }
.text-500 { color: var(--text-color-secondary) !important; }
.text-400 { color: var(--text-color-secondary) !important; }
.text-primary { color: var(--primary-color) !important; }

.image-wrapper {
  height: 220px;
  position: relative;
  overflow: hidden;
}

.overlay-gradient {
  background: linear-gradient(to top, rgba(0,0,0,0.6) 0%, rgba(0,0,0,0) 100%);
}

.part-image {
  transition: transform 0.6s cubic-bezier(0.16, 1, 0.3, 1);
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: absolute;
  top: 0;
  left: 0;
}

.part-card:hover .part-image {
  transform: scale(1.05);
}

.category-badge {
  font-size: 0.7rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  padding: 0.35rem 0.75rem;
  border-radius: 2rem;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

::v-deep(.p-card-body) {
  padding: 1.5rem;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

::v-deep(.p-card-content) {
  padding: 0;
  margin-top: 0.75rem;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

::v-deep(.p-card-footer) {
  padding: 0;
  margin-top: auto;
}

::v-deep(.p-select) {
  box-shadow: none !important;
}

::v-deep(.p-inputtext) {
  box-shadow: none !important;
  color: var(--text-color) !important;
  background-color: transparent !important;
}

/* Specific Search Bar Overrides */
.search-input-wrapper {
  background-color: var(--surface-50);
  border: 1px solid var(--surface-border);
  border-radius: 1rem;
  transition: box-shadow 0.2s, border-color 0.2s;
}

.search-input-wrapper:focus-within {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 0.2rem var(--primary-color-transparent, rgba(59, 130, 246, 0.2));
}

.grid {
  display: flex;
  flex-wrap: wrap;
  margin-right: -1rem;
  margin-left: -1rem;
}

.col-12, .sm\:col-6, .md\:col-4, .xl\:col-3 {
  padding: 1rem;
  flex: 0 0 100%;
  max-width: 100%;
}

@media screen and (min-width: 576px) {
  .sm\:col-6 { flex: 0 0 50%; max-width: 50%; }
}

@media screen and (min-width: 768px) {
  .md\:col-4 { flex: 0 0 33.3333%; max-width: 33.3333%; }
}

@media screen and (min-width: 1200px) {
  .xl\:col-3 { flex: 0 0 25%; max-width: 25%; }
}
</style>
