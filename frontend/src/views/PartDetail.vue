<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { partService, categoryService, tagService, fileService, drawerService } from '../services/api';
import Button from 'primevue/button';
import Card from 'primevue/card';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import Textarea from 'primevue/textarea';
import Select from 'primevue/select';
import MultiSelect from 'primevue/multiselect';
import Tag from 'primevue/tag';
import ProgressSpinner from 'primevue/progressspinner';
import Message from 'primevue/message';
import Divider from 'primevue/divider';

const route = useRoute();
const router = useRouter();
const partId = parseInt(route.params.id as string);

const part = ref<any>(null);
const categories = ref<any[]>([]);
const tags = ref<any[]>([]);
const drawers = ref<any[]>([]);
const loading = ref(true);
const error = ref('');
const isEditing = ref(false);
const fileInput = ref<HTMLInputElement | null>(null);
const uploadingImage = ref(false);

const editForm = ref({
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

onMounted(async () => {
  try {
    const [partRes, catRes, tagRes, drawerRes] = await Promise.all([
      partService.getById(partId),
      categoryService.getAll(),
      tagService.getAll(),
      drawerService.getAll()
    ]);
    part.value = partRes.data;
    categories.value = catRes.data;
    tags.value = tagRes.data;
    drawers.value = drawerRes.data;
    
    resetForm();
  } catch (err) {
    console.error(err);
    error.value = 'Failed to load component details.';
  } finally {
    loading.value = false;
  }
});

const resetForm = () => {
  if (part.value) {
    editForm.value = {
      name: part.value.name,
      description: part.value.description,
      stock: part.value.stock,
      categoryId: part.value.category?.id || null,
      tagIds: part.value.tag?.map((t: any) => t.id) || [],
      imageLink: part.value.imageLink || '',
      drawerId: part.value.drawer?.id || null,
      sectionNumber: part.value.sectionNumber || null,
      notes: part.value.notes || '',
      links: part.value.links ? [...part.value.links] : []
    };
  }
};

const saveChanges = async () => {
  try {
    loading.value = true;
    const res = await partService.update(partId, editForm.value);
    part.value = res.data;
    isEditing.value = false;
  } catch (err) {
    console.error(err);
    alert('Failed to save changes.');
  } finally {
    loading.value = false;
  }
};

const cancelEdit = () => {
  resetForm();
  isEditing.value = false;
};

const addLink = () => {
  editForm.value.links.push('');
};

const removeLink = (index: number) => {
  editForm.value.links.splice(index, 1);
};

const handleImageUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (!target.files || target.files.length === 0) return;
  const file = target.files[0];
  if (!file) return;
  
  try {
    uploadingImage.value = true;
    const res = await fileService.uploadFile(file, 'itemImage');
    editForm.value.imageLink = `/api/files/getFile?file=${res.data.filename}&type=itemImage`;
  } catch (err) {
    console.error('Failed to upload image:', err);
    alert('Failed to upload image.');
  } finally {
    uploadingImage.value = false;
    if (fileInput.value) fileInput.value.value = '';
  }
};

const triggerFileUpload = () => {
  fileInput.value?.click();
};

const getImageUrl = (imageLink: string | null) => {
  if (!imageLink) return '/placeholder.png';
  if (imageLink.startsWith('http')) return imageLink;
  if (imageLink.startsWith('/api/files/getFile')) {
    return `http://localhost:8080${imageLink}`;
  }
  return `/images/${imageLink}`;
};

const getLinkUrl = (link: string) => {
  if (link.startsWith('http')) return link;
  if (link.startsWith('/api/files/getFile')) {
    return `http://localhost:8080${link}`;
  }
  return link;
};

const goBack = () => {
  router.push({ name: 'home' });
};
</script>

<template>
  <div class="part-detail-container px-4 py-6 md:px-6 lg:px-8">
    <div class="flex justify-content-between align-items-center mb-6 border-bottom-1 surface-border pb-4">
      <Button icon="pi pi-arrow-left" label="Back to Inventory" variant="text" class="p-button-plain hover:text-primary transition-colors font-semibold" @click="goBack" />
      <div class="flex gap-3">
         <Button v-if="!isEditing" icon="pi pi-pencil" label="Edit Detail" class="p-button-outlined p-button-secondary border-round-xl px-4 font-semibold shadow-1" @click="isEditing = true" />
         <Button v-if="!isEditing" icon="pi pi-trash" class="p-button-outlined p-button-danger border-round-xl shadow-1" />
      </div>
    </div>

    <div v-if="loading" class="flex justify-content-center p-8">
      <ProgressSpinner strokeWidth="4" class="w-4rem h-4rem" />
    </div>
    <div v-else-if="error" class="mb-4">
      <Message severity="error" class="border-round-xl shadow-1">{{ error }}</Message>
    </div>
    <div v-else-if="part">
      <div class="grid grid-nogutter gap-5">
        <div class="col-12 lg:col-5">
           <Card class="image-card border-none shadow-2 overflow-hidden border-round-2xl">
             <template #content>
               <div class="image-container bg-gray-50 border-round-2xl overflow-hidden mb-4 relative flex align-items-center justify-content-center">
                 <img :src="getImageUrl(isEditing ? editForm.imageLink : part.imageLink)" :alt="part.name" class="detail-image w-full h-full object-cover absolute top-0 left-0" />
               </div>
               <div v-if="isEditing" class="field mt-4">
                 <label class="block text-sm font-semibold mb-2 text-700">Image URL or Upload</label>
                 <div class="p-inputgroup flex-1">
                   <InputText v-model="editForm.imageLink" placeholder="e.g. https://... or resistor.jpg" class="w-full border-round-left-lg bg-gray-50 border-none px-3" />
                   <Button icon="pi pi-upload" label="Upload" class="p-button-secondary border-round-right-lg shadow-none px-3" @click="triggerFileUpload" :loading="uploadingImage" />
                 </div>
                 <input type="file" accept="image/*" class="hidden" ref="fileInput" @change="handleImageUpload" />
               </div>
             </template>
           </Card>
        </div>

        <div class="col-12 lg:col flex-1 pl-0 lg:pl-4">
           <div v-if="!isEditing" class="details-section">
             <div class="header-info mb-5">
               <h1 class="part-detail-title text-900 m-0 tracking-tight">{{ part.name }}</h1>
               <div class="flex flex-wrap align-items-center gap-3 mt-4">
                 <span class="detail-category-badge shadow-1">{{ part.category?.name || 'Uncategorized' }}</span>
                 <div class="flex flex-wrap gap-2">
                   <Tag v-for="t in part.tag" :key="t.id" :value="t.name" rounded class="px-3" :style="{ backgroundColor: t.color + '15', color: t.color, border: '1px solid ' + t.color + '30' }" />
                 </div>
               </div>
             </div>

             <div class="stock-info p-5 bg-white border-round-2xl shadow-1 mb-5 flex flex-column sm:flex-row justify-content-between align-items-start sm:align-items-center gap-4">
               <div>
                 <span class="block text-500 text-xs font-bold uppercase mb-2 tracking-widest">Inventory Status</span>
                 <div class="flex align-items-baseline gap-2">
                   <span class="text-6xl font-black" :class="part.stock < 5 ? 'text-red-500' : 'text-slate-900'">{{ part.stock }}</span>
                   <span class="text-500 font-bold uppercase text-sm tracking-widest">units available</span>
                 </div>
               </div>
               <div class="flex gap-2">
                 <Button icon="pi pi-minus" class="p-button-rounded p-button-secondary p-button-outlined w-3rem h-3rem" />
                 <Button icon="pi pi-plus" class="p-button-rounded p-button-primary shadow-2 w-3rem h-3rem" />
               </div>
             </div>

             <Card class="border-none shadow-1 mb-5 border-round-2xl">
               <template #title><span class="text-2xl font-bold text-800 tracking-tight">About</span></template>
               <template #content>
                 <p class="text-600 line-height-3 m-0 text-lg">{{ part.description || 'No description provided.' }}</p>
                 
                 <Divider class="my-5" />
                 
                 <div class="grid">
                   <div class="col-12 sm:col-6 mb-4 sm:mb-0">
                     <span class="block text-500 text-xs font-bold uppercase mb-3 tracking-widest">Location</span>
                     <div class="flex align-items-center bg-gray-50 p-3 border-round-xl w-max">
                       <i class="pi pi-box text-blue-500 mr-3 text-xl"></i>
                       <span v-if="part.drawer" class="font-bold text-700 text-lg">Drawer #{{ part.drawer.number }}, Sec {{ part.sectionNumber }}</span>
                       <span v-else class="text-500 italic">Not assigned</span>
                     </div>
                   </div>
                   <div class="col-12 sm:col-6">
                     <span class="block text-500 text-xs font-bold uppercase mb-3 tracking-widest">Technical Resources</span>
                     <div class="flex flex-column gap-2">
                        <span v-if="!part.links || part.links.length === 0" class="text-500 italic bg-gray-50 p-3 border-round-xl text-sm">No links available</span>
                        <a v-for="(link, index) in part.links" :key="index" :href="getLinkUrl(link)" target="_blank" class="flex align-items-center text-blue-500 no-underline hover:underline bg-blue-50 p-3 border-round-xl transition-colors hover:bg-blue-100 font-semibold text-sm">
                          <i class="pi pi-external-link mr-2"></i> Resource Link {{ Number(index) + 1 }}
                        </a>
                     </div>
                   </div>
                 </div>
               </template>
             </Card>

             <Card v-if="part.notes" class="border-none shadow-1 bg-yellow-50 border-round-2xl">
                <template #title>
                  <span class="text-xl font-bold text-yellow-800 flex align-items-center gap-2">
                    <i class="pi pi-info-circle text-xl"></i> Internal Notes
                  </span>
                </template>
                <template #content>
                  <p class="m-0 text-yellow-900 line-height-3 text-lg">{{ part.notes }}</p>
                </template>
             </Card>
           </div>

           <!-- Edit Section -->
           <div v-else class="edit-section">
             <Card class="border-none shadow-2 border-round-2xl">
               <template #title><span class="text-2xl font-bold text-900 tracking-tight">Edit Component Details</span></template>
               <template #content>
                 <div class="grid p-fluid mt-2">
                   <div class="col-12 field mb-4">
                     <label class="block font-semibold mb-2 text-700">Name</label>
                     <InputText v-model="editForm.name" class="p-3 border-round-lg bg-gray-50 border-none hover:bg-gray-100 focus:bg-gray-100 transition-colors" />
                   </div>
                   
                   <div class="col-12 field mb-4">
                     <label class="block font-semibold mb-2 text-700">Description</label>
                     <Textarea v-model="editForm.description" rows="4" autoResize class="p-3 border-round-lg bg-gray-50 border-none hover:bg-gray-100 focus:bg-gray-100 transition-colors" />
                   </div>

                   <div class="col-12 sm:col-6 field mb-4 pr-0 sm:pr-3">
                     <label class="block font-semibold mb-2 text-700">Category</label>
                     <Select v-model="editForm.categoryId" :options="categories" optionLabel="name" optionValue="id" placeholder="Select Category" showClear class="border-round-lg bg-gray-50 border-none hover:bg-gray-100 transition-colors py-2" />
                   </div>

                   <div class="col-12 sm:col-6 field mb-4 pl-0 sm:pl-3">
                     <label class="block font-semibold mb-2 text-700">Stock Level</label>
                     <InputNumber v-model="editForm.stock" showButtons :min="0" class="w-full" inputClass="border-round-lg bg-gray-50 border-none hover:bg-gray-100 focus:bg-gray-100 pt-3 pb-3 transition-colors" />
                   </div>

                   <div class="col-12 field mb-4">
                     <label class="block font-semibold mb-2 text-700">Tags</label>
                     <MultiSelect v-model="editForm.tagIds" :options="tags" optionLabel="name" optionValue="id" placeholder="Select Tags" display="chip" class="border-round-lg bg-gray-50 border-none hover:bg-gray-100 transition-colors py-2" />
                   </div>
                   
                   <Divider class="col-12 my-3" />
                   
                   <div class="col-12 sm:col-6 field mb-4 pr-0 sm:pr-3">
                     <label class="block font-semibold mb-2 text-700">Drawer</label>
                     <Select v-model="editForm.drawerId" :options="drawers" optionLabel="number" optionValue="id" placeholder="Select Drawer" showClear class="border-round-lg bg-gray-50 border-none hover:bg-gray-100 transition-colors py-2" />
                   </div>

                   <div class="col-12 sm:col-6 field mb-4 pl-0 sm:pl-3">
                     <label class="block font-semibold mb-2 text-700">Section Number</label>
                     <InputNumber v-model="editForm.sectionNumber" :min="1" class="w-full" inputClass="border-round-lg bg-gray-50 border-none hover:bg-gray-100 focus:bg-gray-100 pt-3 pb-3 transition-colors" />
                   </div>

                   <div class="col-12 field mb-4">
                     <label class="block font-semibold mb-2 text-700 flex justify-content-between align-items-center">
                       Technical Links
                       <Button icon="pi pi-plus" variant="text" size="small" class="p-button-sm w-2rem h-2rem p-0 p-button-secondary" @click="addLink" />
                     </label>
                     <div class="flex flex-column gap-2 mb-2">
                       <div v-for="(link, index) in editForm.links" :key="index" class="flex gap-2 align-items-center">
                         <span class="font-bold text-400 text-sm w-1rem text-right">{{ Number(index) + 1 }}.</span>
                         <InputText v-model="editForm.links[index as number]" placeholder="https://..." class="flex-1 p-2 border-round-lg bg-gray-50 border-none hover:bg-gray-100 focus:bg-gray-100 transition-colors" />
                         <Button icon="pi pi-trash" class="p-button-danger p-button-text p-button-rounded" @click="removeLink(index as number)" />
                       </div>
                       <Message v-if="editForm.links.length === 0" severity="secondary" :closable="false" class="m-0 border-round-lg bg-gray-50 p-3 shadow-none text-600 font-medium">No external links added.</Message>
                     </div>
                   </div>

                   <div class="col-12 field mb-0">
                     <label class="block font-semibold mb-2 text-700">Notes (Internal)</label>
                     <Textarea v-model="editForm.notes" rows="4" autoResize class="p-3 border-round-lg bg-yellow-50 border-none focus:bg-yellow-100 transition-colors" placeholder="Any internal notes or warnings..." />
                   </div>

                 </div>

                 <div class="flex gap-3 justify-content-end mt-5 pt-4 border-top-1 surface-border">
                   <Button label="Cancel" class="p-button-text p-button-secondary font-semibold" @click="cancelEdit" />
                   <Button label="Save Changes" icon="pi pi-check" @click="saveChanges" class="p-button-primary shadow-2 font-semibold px-4 border-round-xl" />
                 </div>
               </template>
             </Card>
           </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.part-detail-container {
  max-width: 1400px;
  margin: 0 auto;
}

.part-detail-title {
  font-size: 3.5rem;
  font-weight: 800;
  line-height: 1.1;
}

.detail-category-badge {
  font-size: 0.75rem;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: #fff;
  background: #3b82f6;
  padding: 0.4rem 0.8rem;
  border-radius: 2rem;
}

.bg-white { background-color: var(--surface-0) !important; }
.bg-gray-50 { background-color: var(--surface-50) !important; }
.bg-gray-100 { background-color: var(--surface-100) !important; }
.bg-blue-50 { background-color: var(--highlight-bg, #eff6ff); }

.text-900 { color: var(--text-color) !important; }
.text-800 { color: var(--text-color) !important; }
.text-700 { color: var(--text-color) !important; }
.text-600 { color: var(--text-color-secondary) !important; }
.text-500 { color: var(--text-color-secondary) !important; }
.text-400 { color: var(--text-color-secondary) !important; }
.text-slate-900 { color: var(--text-color) !important; }

.bg-yellow-50 { background-color: #fefce8; }
.bg-yellow-100 { background-color: #fef9c3; }
.text-yellow-800 { color: #854d0e; }
.text-yellow-900 { color: #713f12; }

.image-container {
  aspect-ratio: 1/1;
  width: 100%;
  position: relative;
  overflow: hidden;
  border-radius: 1.25rem;
  background-color: var(--surface-0);
}

.detail-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
}

::v-deep(.p-select) {
  box-shadow: none !important;
}
::v-deep(.p-inputtext) {
  box-shadow: none !important;
}
::v-deep(.p-inputnumber-input) {
  box-shadow: none !important;
}
::v-deep(.p-multiselect) {
  box-shadow: none !important;
}
::v-deep(.p-message) {
  border: none;
}

.grid {
  display: flex;
  flex-wrap: wrap;
  margin-right: -1rem;
  margin-left: -1rem;
}

.col-12, .sm\:col-6, .lg\:col-5, .lg\:col {
  padding: 1rem;
  flex: 0 0 100%;
  max-width: 100%;
}

@media screen and (min-width: 576px) {
  .sm\:col-6 { flex: 0 0 50%; max-width: 50%; }
}

@media screen and (min-width: 992px) {
  .lg\:col-5 { flex: 0 0 41.6667%; max-width: 41.6667%; }
  .lg\:col { flex: 1 1 0; max-width: 100%; }
}
</style>
