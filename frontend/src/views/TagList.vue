<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { tagService } from '../services/api';
import Card from 'primevue/card';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import ColorPicker from 'primevue/colorpicker';
import Dialog from 'primevue/dialog';
import ProgressSpinner from 'primevue/progressspinner';
import Message from 'primevue/message';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Tag from 'primevue/tag';

const tags = ref<any[]>([]);
const loading = ref(true);
const error = ref('');
const showDialog = ref(false);
const isEditing = ref(false);

const tagForm = ref({
  id: null as number | null,
  name: '',
  color: '3b82f6'
});

const loadTags = async () => {
  loading.value = true;
  try {
    const res = await tagService.getAll();
    tags.value = res.data;
  } catch (err) {
    console.error('Failed to load tags', err);
    error.value = 'Failed to load tags.';
  } finally {
    loading.value = false;
  }
};

onMounted(loadTags);

const openNew = () => {
  tagForm.value = { id: null, name: '', color: '3b82f6' };
  isEditing.value = false;
  showDialog.value = true;
};

const editTag = (tag: any) => {
  tagForm.value = { ...tag };
  // Handle if color has # prefix
  if (tagForm.value.color.startsWith('#')) {
    tagForm.value.color = tagForm.value.color.substring(1);
  }
  isEditing.value = true;
  showDialog.value = true;
};

const saveTag = async () => {
  if (!tagForm.value.name) return;

  const payload = {
    ...tagForm.value,
    color: tagForm.value.color.startsWith('#') ? tagForm.value.color : '#' + tagForm.value.color
  };

  try {
    loading.value = true;
    if (isEditing.value && tagForm.value.id) {
      await tagService.update(tagForm.value.id, payload);
    } else {
      await tagService.create(payload);
    }
    showDialog.value = false;
    await loadTags();
  } catch (err) {
    console.error('Failed to save tag', err);
  } finally {
    loading.value = false;
  }
};

const confirmDelete = async (id: number) => {
  if (confirm('Are you sure you want to delete this tag?')) {
    try {
      loading.value = true;
      await tagService.delete(id);
      await loadTags();
    } catch (err) {
      console.error('Failed to delete tag', err);
    } finally {
      loading.value = false;
    }
  }
};
</script>

<template>
  <div class="tags-container">
    <div class="header flex justify-content-between align-items-center mb-6">
      <div>
        <h1 class="text-4xl font-light m-0">Tags</h1>
        <p class="text-500 mt-2">Create custom labels for your components.</p>
      </div>
      <Button label="New Tag" icon="pi pi-plus" @click="openNew" />
    </div>

    <div v-if="loading && tags.length === 0" class="flex justify-content-center p-8">
      <ProgressSpinner />
    </div>

    <div v-else-if="error" class="mb-4">
      <Message severity="error">{{ error }}</Message>
    </div>

    <div v-else>
      <Card class="border-none shadow-1">
        <template #content>
          <DataTable :value="tags" responsiveLayout="stack" breakpoint="960px" class="p-datatable-sm">
            <Column field="name" header="Name" sortable>
              <template #body="slotProps">
                <Tag :value="slotProps.data.name" :style="{ backgroundColor: slotProps.data.color, color: '#fff' }" rounded />
              </template>
            </Column>
            <Column field="color" header="Color Hex">
              <template #body="slotProps">
                <code class="text-xs">{{ slotProps.data.color }}</code>
              </template>
            </Column>
            <Column header="Actions" class="text-right">
              <template #body="slotProps">
                <div class="flex justify-content-end gap-2">
                  <Button icon="pi pi-pencil" variant="text" rounded @click="editTag(slotProps.data)" />
                  <Button icon="pi pi-trash" variant="text" severity="danger" rounded @click="confirmDelete(slotProps.data.id)" />
                </div>
              </template>
            </Column>
          </DataTable>
        </template>
      </Card>

      <div v-if="tags.length === 0" class="text-center py-8 text-500">
        <i class="pi pi-hashtag text-6xl mb-4"></i>
        <p class="text-xl">No tags found. Create some labels!</p>
      </div>
    </div>

    <Dialog v-model:visible="showDialog" :header="isEditing ? 'Edit Tag' : 'New Tag'" modal class="p-fluid max-w-lg w-full">
      <div class="field mb-4">
        <label for="name" class="block font-medium mb-2">Name</label>
        <InputText id="name" v-model="tagForm.name" required autofocus />
      </div>
      <div class="field mb-4">
        <label for="color" class="block font-medium mb-2">Color</label>
        <div class="flex align-items-center gap-3">
          <ColorPicker v-model="tagForm.color" />
          <InputText v-model="tagForm.color" class="w-full" placeholder="e.g. 3b82f6" />
        </div>
      </div>
      <template #footer>
        <div class="flex justify-content-end gap-2 mt-4">
          <Button label="Cancel" variant="text" @click="showDialog = false" />
          <Button label="Save" icon="pi pi-check" @click="saveTag" severity="success" />
        </div>
      </template>
    </Dialog>
  </div>
</template>

<style scoped>
.tags-container {
  max-width: 1000px;
  margin: 0 auto;
}

.flex { display: flex; }
.justify-content-between { justify-content: space-between; }
.justify-content-center { justify-content: center; }
.justify-content-end { justify-content: flex-end; }
.align-items-center { align-items: center; }
.gap-2 { gap: 0.5rem; }
.gap-3 { gap: 1rem; }
.m-0 { margin: 0; }
.mb-2 { margin-bottom: 0.5rem; }
.mb-4 { margin-bottom: 1rem; }
.mb-6 { margin-bottom: 2rem; }
.mt-2 { margin-top: 0.5rem; }
.mt-4 { margin-top: 1rem; }
.p-8 { padding: 4rem; }
.text-4xl { font-size: 2.25rem; }
.text-500 { color: #6b7280; }
.text-xs { font-size: 0.75rem; }
.font-light { font-weight: 300; }
.font-medium { font-weight: 500; }
.border-none { border: none !important; }
.shadow-1 { box-shadow: 0 1px 2px 0 rgba(0,0,0,0.05); }
.max-w-lg { max-width: 32rem; }
.w-full { width: 100%; }
.block { display: block; }

code {
  background: #f1f5f9;
  padding: 0.2rem 0.4rem;
  border-radius: 4px;
}
</style>
