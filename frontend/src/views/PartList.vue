<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { partService, categoryService, tagService, fileService } from '../services/api';
import { useRouter } from 'vue-router';
import InputText from 'primevue/inputtext';
import Select from 'primevue/select';
import Card from 'primevue/card';
import Tag from 'primevue/tag';
import ProgressSpinner from 'primevue/progressspinner';
import Message from 'primevue/message';
import Button from 'primevue/button';

const parts = ref<any[]>([]);
const categories = ref<any[]>([]);
const tags = ref<any[]>([]);
const loading = ref(true);
const error = ref('');

const searchQuery = ref('');
const selectedCategory = ref(null);
const selectedTag = ref(null);

const router = useRouter();

onMounted(async () => {
  try {
    const [partsRes, catRes, tagRes] = await Promise.all([
      partService.getAll(),
      categoryService.getAll(),
      tagService.getAll()
    ]);
    parts.value = partsRes.data;
    categories.value = catRes.data;
    tags.value = tagRes.data;
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
</script>

<template>
  <div class="part-list-container">
    <div class="header flex justify-content-between align-items-center mb-6">
      <div>
        <h1 class="text-4xl font-light m-0">Inventory</h1>
        <p class="text-500 mt-2">Manage and browse your microelectronic components.</p>
      </div>
      <Button label="Add Component" icon="pi pi-plus" />
    </div>
    
    <div class="search-section mb-6">
      <div class="flex flex-column md:flex-row gap-3">
        <div class="relative flex-grow-1">
          <i class="pi pi-search text-400 absolute left-3 top-50 translate-middle-y"></i>
          <InputText v-model="searchQuery" placeholder="Search by name, description..." class="w-full border-1 surface-border py-3 pl-6 text-lg" />
        </div>
        <div class="flex gap-3">
          <Select v-model="selectedCategory" :options="categories" optionLabel="name" optionValue="id" placeholder="Category" showClear class="w-12rem border-1 surface-border" />
          <Select v-model="selectedTag" :options="tags" optionLabel="name" optionValue="id" placeholder="Tag" showClear class="w-12rem border-1 surface-border" />
        </div>
      </div>
    </div>

    <div v-if="loading" class="flex justify-content-center p-8">
      <ProgressSpinner />
    </div>
    <div v-else-if="error" class="mb-4">
      <Message severity="error">{{ error }}</Message>
    </div>
    
    <div v-else>
      <div class="grid">
        <div v-for="part in filteredParts" :key="part.id" class="col-12 sm:col-6 md:col-4 lg:col-3 flex">
          <Card class="part-card w-full cursor-pointer border-none shadow-1 hover:shadow-3 transition-all transition-duration-300" @click="goToDetail(part.id)">
            <template #header>
              <div class="image-wrapper">
                <img :src="getImageUrl(part.imageLink)" :alt="part.name" class="part-image" />
                <div v-if="part.stock < 5" class="low-stock-badge">
                   <Tag severity="warn" value="Low Stock" size="small" rounded />
                </div>
              </div>
            </template>
            <template #title>
              <div class="card-title-container">
                <div class="part-name">{{ part.name }}</div>
              </div>
            </template>
            <template #subtitle>
              <div class="flex align-items-center gap-2">
                <span class="category-badge">{{ part.category?.name || 'Uncategorized' }}</span>
              </div>
            </template>
            <template #content>
              <div class="flex flex-wrap gap-1 mb-3">
                <Tag v-for="t in part.tag" :key="t.id" :value="t.name" severity="secondary" rounded class="text-xs font-normal" :style="{ backgroundColor: t.color + '22', color: t.color, border: '1px solid ' + t.color + '44' }" />
              </div>
              <p class="m-0 text-600 text-sm line-height-3 line-clamp-2">
                {{ part.description }}
              </p>
            </template>
            <template #footer>
              <div class="card-footer-container">
                <div class="stock-badge" :class="{ 'low-stock-text': part.stock < 5 }">
                  <span class="stock-value">{{ part.stock }}</span>
                  <span class="stock-label"> units</span>
                </div>
                <div v-if="part.drawer" class="location-info">
                  <i class="pi pi-map-marker"></i>
                  <span>{{ part.drawer.number }}-{{ part.sectionNumber }}</span>
                </div>
              </div>
            </template>
          </Card>
        </div>
      </div>
      <div v-if="filteredParts.length === 0" class="text-center py-8">
        <i class="pi pi-search text-500 text-6xl mb-4"></i>
        <p class="text-xl text-500">No components found matching your criteria.</p>
        <Button label="Clear Filters" variant="text" @click="searchQuery=''; selectedCategory=null; selectedTag=null;" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.part-list-container {
  max-width: 1300px;
  margin: 0 auto;
}

.bg-white { background-color: white; }
.bg-gray-50 { background-color: #f9fafb; }
.text-gray-900 { color: #111827; }
.text-gray-500 { color: #6b7280; }
.text-600 { color: #4b5563; }
.tracking-wider { letter-spacing: 0.05em; }
.w-12rem { width: 12rem; }
.pl-6 { padding-left: 3rem !important; }
.left-3 { left: 1rem; }
.top-50 { top: 50%; }
.translate-middle-y { transform: translateY(-50%); }
.relative { position: relative; }
.absolute { position: absolute; }
.text-primary { color: #3b82f6; }

.image-wrapper {
  position: relative;
  overflow: hidden;
  height: 200px;
  background-color: #f3f4f6;
  border-bottom: 1px solid #f1f5f9;
}

.card-title-container {
  padding: 0.25rem 0;
}

.part-name {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.3;
  word-break: break-word;
}

.category-badge {
  font-size: 0.65rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #64748b;
  background: #f1f5f9;
  padding: 0.15rem 0.4rem;
  border-radius: 4px;
}

.card-footer-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 1rem;
}

.stock-badge {
  display: flex;
  align-items: baseline;
  gap: 0.25rem;
  color: #1a1a1a;
}

.stock-value {
  font-size: 1.5rem;
  font-weight: 800;
}

.stock-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
}

.low-stock-text {
  color: #e11d48;
}

.low-stock-text .stock-label {
  color: #fb7185;
}

.location-info {
  font-size: 0.75rem;
  font-weight: 600;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.location-info i {
  color: #3b82f6;
  font-size: 0.8rem;
}

.part-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.part-card:hover .part-image {
  transform: scale(1.08);
}

.low-stock-badge {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  z-index: 10;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.part-card {
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  height: 100%;
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
}

::v-deep(.p-card-footer) {
  padding: 0;
  margin-top: auto;
}

::v-deep(.p-card-subtitle) {
  margin-bottom: 0;
}

.grid {
  display: flex;
  flex-wrap: wrap;
  margin-right: -1rem;
  margin-left: -1rem;
}

.col-12, .sm\:col-6, .md\:col-4, .lg\:col-3 {
  padding: 1rem;
  flex: 0 0 100%;
  max-width: 100%;
}

@media screen and (min-width: 576px) {
  .sm\:col-6 {
    flex: 0 0 50%;
    max-width: 50%;
  }
}

@media screen and (min-width: 768px) {
  .md\:col-4 {
    flex: 0 0 33.3333%;
    max-width: 33.3333%;
  }
}

@media screen and (min-width: 992px) {
  .lg\:col-3 {
    flex: 0 0 25%;
    max-width: 25%;
  }
}

.border-round { border-radius: 12px; }
.shadow-1 { box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px -1px rgba(0, 0, 0, 0.1); }
.hover\:shadow-3:hover { box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1); }
.transition-all { transition-property: all; }
.transition-duration-300 { transition-duration: 300ms; }
.cursor-pointer { cursor: pointer; }
.flex { display: flex; }
.flex-wrap { flex-wrap: wrap; }
.justify-content-between { justify-content: space-between; }
.justify-content-center { justify-content: center; }
.align-items-center { align-items: center; }
.align-items-baseline { align-items: baseline; }
.gap-1 { gap: 0.25rem; }
.mb-4 { margin-bottom: 1rem; }
.mb-6 { margin-bottom: 2rem; }
.mt-2 { margin-top: 0.5rem; }
.ml-1 { margin-left: 0.25rem; }
.mr-1 { margin-right: 0.25rem; }
.pt-2 { padding-top: 0.5rem; }
.py-3 { padding-top: 0.75rem; padding-bottom: 0.75rem; }
.py-8 { padding-top: 4rem; padding-bottom: 4rem; }
.p-8 { padding: 4rem; }
.p-inputgroup { display: flex; width: 100%; }
.p-inputgroup-addon { padding: 0.75rem; display: flex; align-items: center; }
.border-none { border: none !important; }
.border-left-1 { border-left: 1px solid #e5e7eb !important; }
.surface-border { border-color: #e5e7eb; }
.overflow-hidden { overflow: hidden; }
.font-light { font-weight: 300; }
.font-semibold { font-weight: 600; }
.font-bold { font-weight: 700; }
.font-normal { font-weight: 400; }
.text-4xl { font-size: 2.25rem; }
.text-xl { font-size: 1.25rem; }
.text-2xl { font-size: 1.5rem; }
.text-sm { font-size: 0.875rem; }
.text-xs { font-size: 0.75rem; }
.text-lg { font-size: 1.125rem; }
.text-900 { color: #111827; }
.text-500 { color: #6b7280; }
.text-400 { color: #9ca3af; }
.line-height-3 { line-height: 1.5; }
</style>
