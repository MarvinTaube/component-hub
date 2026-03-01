<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { categoryService } from '../services/api';
import Card from 'primevue/card';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Textarea from 'primevue/textarea';
import Dialog from 'primevue/dialog';
import ProgressSpinner from 'primevue/progressspinner';
import Message from 'primevue/message';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';

const categories = ref<any[]>([]);
const loading = ref(true);
const error = ref('');
const showDialog = ref(false);
const isEditing = ref(false);

const categoryForm = ref({
  id: null as number | null,
  name: '',
  notes: ''
});

const loadCategories = async () => {
  loading.value = true;
  try {
    const res = await categoryService.getAll();
    categories.value = res.data;
  } catch (err) {
    console.error('Failed to load categories', err);
    error.value = 'Failed to load categories.';
  } finally {
    loading.value = false;
  }
};

onMounted(loadCategories);

const openNew = () => {
  categoryForm.value = { id: null, name: '', notes: '' };
  isEditing.value = false;
  showDialog.value = true;
};

const editCategory = (category: any) => {
  categoryForm.value = { ...category };
  isEditing.value = true;
  showDialog.value = true;
};

const saveCategory = async () => {
  if (!categoryForm.value.name) return;

  try {
    loading.value = true;
    if (isEditing.value && categoryForm.value.id) {
      await categoryService.update(categoryForm.value.id, categoryForm.value);
    } else {
      await categoryService.create(categoryForm.value);
    }
    showDialog.value = false;
    await loadCategories();
  } catch (err) {
    console.error('Failed to save category', err);
  } finally {
    loading.value = false;
  }
};

const confirmDelete = async (id: number) => {
  if (confirm('Are you sure you want to delete this category?')) {
    try {
      loading.value = true;
      await categoryService.delete(id);
      await loadCategories();
    } catch (err) {
      console.error('Failed to delete category', err);
    } finally {
      loading.value = false;
    }
  }
};
</script>

<template>
  <div class="categories-container">
    <div class="header flex justify-content-between align-items-center mb-6">
      <div>
        <h1 class="text-4xl font-light m-0">Categories</h1>
        <p class="text-500 mt-2">Organize your components into logical groups.</p>
      </div>
      <Button label="New Category" icon="pi pi-plus" @click="openNew" />
    </div>

    <div v-if="loading && categories.length === 0" class="flex justify-content-center p-8">
      <ProgressSpinner />
    </div>

    <div v-else-if="error" class="mb-4">
      <Message severity="error">{{ error }}</Message>
    </div>

    <div v-else>
      <Card class="border-none shadow-1">
        <template #content>
          <DataTable :value="categories" responsiveLayout="stack" breakpoint="960px" class="p-datatable-sm">
            <Column field="name" header="Name" sortable class="font-semibold"></Column>
            <Column field="notes" header="Notes">
              <template #body="slotProps">
                <span class="text-500 italic">{{ slotProps.data.notes || 'No notes' }}</span>
              </template>
            </Column>
            <Column header="Actions" class="text-right">
              <template #body="slotProps">
                <div class="flex justify-content-end gap-2">
                  <Button icon="pi pi-pencil" variant="text" rounded @click="editCategory(slotProps.data)" />
                  <Button icon="pi pi-trash" variant="text" severity="danger" rounded @click="confirmDelete(slotProps.data.id)" />
                </div>
              </template>
            </Column>
          </DataTable>
        </template>
      </Card>

      <div v-if="categories.length === 0" class="text-center py-8 text-500">
        <i class="pi pi-tag text-6xl mb-4"></i>
        <p class="text-xl">No categories found. Create one to get started!</p>
      </div>
    </div>

    <Dialog v-model:visible="showDialog" :header="isEditing ? 'Edit Category' : 'New Category'" modal class="p-fluid max-w-lg w-full">
      <div class="field mb-4">
        <label for="name" class="block font-medium mb-2">Name</label>
        <InputText id="name" v-model="categoryForm.name" required autofocus />
      </div>
      <div class="field mb-4">
        <label for="notes" class="block font-medium mb-2">Notes</label>
        <Textarea id="notes" v-model="categoryForm.notes" rows="3" autoResize />
      </div>
      <template #footer>
        <div class="flex justify-content-end gap-2 mt-4">
          <Button label="Cancel" variant="text" @click="showDialog = false" />
          <Button label="Save" icon="pi pi-check" @click="saveCategory" severity="success" />
        </div>
      </template>
    </Dialog>
  </div>
</template>

<style scoped>
.categories-container {
  max-width: 1000px;
  margin: 0 auto;
}

.flex { display: flex; }
.justify-content-between { justify-content: space-between; }
.justify-content-center { justify-content: center; }
.justify-content-end { justify-content: flex-end; }
.align-items-center { align-items: center; }
.gap-2 { gap: 0.5rem; }
.m-0 { margin: 0; }
.mb-2 { margin-bottom: 0.5rem; }
.mb-4 { margin-bottom: 1rem; }
.mb-6 { margin-bottom: 2rem; }
.mt-2 { margin-top: 0.5rem; }
.mt-4 { margin-top: 1rem; }
.p-8 { padding: 4rem; }
.text-4xl { font-size: 2.25rem; }
.text-500 { color: #6b7280; }
.font-light { font-weight: 300; }
.font-medium { font-weight: 500; }
.font-semibold { font-weight: 600; }
.italic { font-style: italic; }
.border-none { border: none !important; }
.shadow-1 { box-shadow: 0 1px 2px 0 rgba(0,0,0,0.05); }
.max-w-lg { max-width: 32rem; }
.w-full { width: 100%; }
.block { display: block; }

:::v-deep(.p-datatable-header) {
  background: transparent;
  border: none;
}
</style>
