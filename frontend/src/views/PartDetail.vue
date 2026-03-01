<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { partService, categoryService, tagService, fileService } from '../services/api';
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
const loading = ref(true);
const error = ref('');
const isEditing = ref(false);

const editForm = ref({
  name: '',
  description: '',
  stock: 0,
  categoryId: null as number | null,
  tagIds: [] as number[],
  imageLink: ''
});

onMounted(async () => {
  try {
    const [partRes, catRes, tagRes] = await Promise.all([
      partService.getById(partId),
      categoryService.getAll(),
      tagService.getAll()
    ]);
    part.value = partRes.data;
    categories.value = catRes.data;
    tags.value = tagRes.data;
    
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
      imageLink: part.value.imageLink || ''
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
  <div class="part-detail-container">
    <div class="flex justify-content-between align-items-center mb-6">
      <Button icon="pi pi-arrow-left" label="Back to Inventory" variant="text" @click="goBack" />
      <div class="flex gap-2">
         <Button v-if="!isEditing" icon="pi pi-pencil" label="Edit Component" @click="isEditing = true" />
         <Button v-if="!isEditing" icon="pi pi-trash" severity="danger" variant="outlined" />
      </div>
    </div>

    <div v-if="loading" class="flex justify-content-center p-8">
      <ProgressSpinner />
    </div>
    <div v-else-if="error" class="mb-4">
      <Message severity="error">{{ error }}</Message>
    </div>
    <div v-else-if="part">
      <div class="grid">
        <div class="col-12 lg:col-5">
           <Card class="image-card border-none shadow-1 overflow-hidden">
             <template #content>
               <div class="image-container bg-faint border-round overflow-hidden mb-4">
                 <img :src="getImageUrl(isEditing ? editForm.imageLink : part.imageLink)" :alt="part.name" class="detail-image" />
               </div>
               <div v-if="isEditing" class="field">
                 <label class="block text-sm font-medium mb-2">Image URL or Filename</label>
                 <InputText v-model="editForm.imageLink" placeholder="e.g. resistor.jpg" class="w-full" />
               </div>
             </template>
           </Card>
        </div>

        <div class="col-12 lg:col-7">
           <div v-if="!isEditing" class="details-section">
             <div class="header-info mb-5">
               <h1 class="part-detail-title">{{ part.name }}</h1>
               <div class="flex flex-wrap align-items-center gap-2 mt-3">
                 <span class="detail-category-badge">{{ part.category?.name || 'Uncategorized' }}</span>
                 <div class="flex flex-wrap gap-2">
                   <Tag v-for="t in part.tag" :key="t.id" :value="t.name" rounded :style="{ backgroundColor: t.color + '22', color: t.color, border: '1px solid ' + t.color + '44' }" />
                 </div>
               </div>
             </div>

             <div class="stock-info p-5 bg-white border-round shadow-1 mb-6 flex justify-content-between align-items-center">
               <div>
                 <span class="block text-64748b text-xs font-bold uppercase mb-2 tracking-wider">Inventory Status</span>
                 <div class="flex align-items-baseline gap-2">
                   <span class="text-5xl font-black" :class="part.stock < 5 ? 'text-red-500' : 'text-slate-900'">{{ part.stock }}</span>
                   <span class="text-slate-500 font-bold uppercase text-sm">units available</span>
                 </div>
               </div>
               <div class="flex gap-2">
                 <Button icon="pi pi-minus" variant="outlined" rounded severity="secondary" />
                 <Button icon="pi pi-plus" variant="outlined" rounded severity="primary" />
               </div>
             </div>

             <Card class="border-none shadow-1 mb-5">
               <template #title><span class="text-lg font-semibold">About this component</span></template>
               <template #content>
                 <p class="text-500 line-height-3 m-0">{{ part.description || 'No description provided.' }}</p>
                 
                 <Divider class="my-4" />
                 
                 <div class="grid">
                   <div class="col-6">
                     <span class="block text-500 text-xs font-bold uppercase mb-2">Location</span>
                     <div class="flex align-items-center">
                       <i class="pi pi-map-marker text-blue-500 mr-2"></i>
                       <span v-if="part.drawer">Drawer #{{ part.drawer.number }}, Section {{ part.sectionNumber }}</span>
                       <span v-else class="text-400 italic">Not assigned</span>
                     </div>
                   </div>
                   <div class="col-6">
                     <span class="block text-500 text-xs font-bold uppercase mb-2">Technical Links</span>
                     <div class="flex flex-column gap-1">
                        <span v-if="!part.links || part.links.length === 0" class="text-400 italic text-sm">No links</span>
                        <a v-for="(link, index) in part.links" :key="index" :href="getLinkUrl(link)" target="_blank" class="text-blue-500 text-sm no-underline hover:underline">
                          Datasheet {{ index + 1 }}
                        </a>
                     </div>
                   </div>
                 </div>
               </template>
             </Card>

             <Card v-if="part.notes" class="border-none shadow-1 bg-yellow-50">
                <template #title><span class="text-lg font-semibold">Notes</span></template>
                <template #content>
                  <p class="m-0 text-700 italic">{{ part.notes }}</p>
                </template>
             </Card>
           </div>

           <div v-else class="edit-section">
             <Card class="border-none shadow-1">
               <template #title><span class="text-xl font-semibold">Edit Component Details</span></template>
               <template #content>
                 <div class="grid p-fluid">
                   <div class="col-12 field mb-4">
                     <label class="block font-medium mb-2">Name</label>
                     <InputText v-model="editForm.name" />
                   </div>
                   
                   <div class="col-12 field mb-4">
                     <label class="block font-medium mb-2">Description</label>
                     <Textarea v-model="editForm.description" rows="4" autoResize />
                   </div>

                   <div class="col-6 field mb-4">
                     <label class="block font-medium mb-2">Category</label>
                     <Select v-model="editForm.categoryId" :options="categories" optionLabel="name" optionValue="id" placeholder="Select Category" showClear />
                   </div>

                   <div class="col-6 field mb-4">
                     <label class="block font-medium mb-2">Stock Level</label>
                     <InputNumber v-model="editForm.stock" showButtons :min="0" />
                   </div>

                   <div class="col-12 field mb-6">
                     <label class="block font-medium mb-2">Tags</label>
                     <MultiSelect v-model="editForm.tagIds" :options="tags" optionLabel="name" optionValue="id" placeholder="Select Tags" display="chip" />
                   </div>
                 </div>

                 <div class="flex gap-2 justify-content-end mt-4">
                   <Button label="Cancel" variant="text" @click="cancelEdit" />
                   <Button label="Save Changes" icon="pi pi-check" @click="saveChanges" severity="success" />
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
  max-width: 1200px;
  margin: 0 auto;
}

.part-detail-title {
  font-size: 3rem;
  font-weight: 800;
  color: #0f172a;
  margin: 0;
  line-height: 1.1;
  letter-spacing: -0.02em;
}

.detail-category-badge {
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #475569;
  background: #f1f5f9;
  padding: 0.25rem 0.6rem;
  border-radius: 6px;
}

.text-64748b { color: #64748b; }
.text-slate-900 { color: #0f172a; }
.text-slate-500 { color: #64748b; }
.text-red-500 { color: #ef4444; }
.tracking-wider { letter-spacing: 0.05em; }
.text-5xl { font-size: 3rem; }

.bg-faint { background-color: #f8fafc; }
.bg-yellow-50 { background-color: #fffbef; }

.image-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
}

.detail-image {
  max-width: 100%;
  max-height: 500px;
  object-fit: contain;
}

.grid {
  display: flex;
  flex-wrap: wrap;
  margin-right: -1rem;
  margin-left: -1rem;
}

.col-12, .lg\:col-5, .lg\:col-7, .col-6 {
  padding: 1rem;
}

@media screen and (min-width: 992px) {
  .lg\:col-5 { flex: 0 0 auto; width: 41.6666%; }
  .lg\:col-7 { flex: 0 0 auto; width: 58.3333%; }
}

.flex { display: flex; }
.flex-column { flex-direction: column; }
.justify-content-between { justify-content: space-between; }
.justify-content-center { justify-content: center; }
.justify-content-end { justify-content: flex-end; }
.align-items-center { align-items: center; }
.gap-1 { gap: 0.25rem; }
.gap-2 { gap: 0.5rem; }
.m-0 { margin: 0; }
.mb-2 { margin-bottom: 0.5rem; }
.mb-4 { margin-bottom: 1rem; }
.mb-5 { margin-bottom: 1.5rem; }
.mb-6 { margin-bottom: 2rem; }
.mt-3 { margin-top: 0.75rem; }
.mt-4 { margin-top: 1rem; }
.ml-2 { margin-left: 0.5rem; }
.mr-2 { margin-right: 0.5rem; }
.my-4 { margin-top: 1.5rem; margin-bottom: 1.5rem; }
.p-4 { padding: 1.25rem; }
.p-8 { padding: 4rem; }
.p-fluid { width: 100%; }
.w-full { width: 100%; }

.border-none { border: none !important; }
.border-round { border-radius: 12px; }
.shadow-1 { box-shadow: 0 1px 2px 0 rgba(0,0,0,0.05); }
.overflow-hidden { overflow: hidden; }

.text-4xl { font-size: 2.25rem; }
.text-lg { font-size: 1.125rem; }
.text-sm { font-size: 0.875rem; }
.text-xs { font-size: 0.75rem; }
.text-900 { color: #111827; }
.text-700 { color: #374151; }
.text-500 { color: #6b7280; }
.text-400 { color: #9ca3af; }
.text-blue-500 { color: #3b82f6; }
.text-orange-500 { color: #f97316; }

.font-medium { font-weight: 500; }
.font-semibold { font-weight: 600; }
.font-bold { font-weight: 700; }
.uppercase { text-transform: uppercase; }
.italic { font-style: italic; }
.no-underline { text-decoration: none; }
.line-height-3 { line-height: 1.5; }
.block { display: block; }
</style>
