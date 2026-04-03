<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { apiService } from '../services/api'
import type { Category } from '../types/backend'

import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Dialog from 'primevue/dialog'
import Message from 'primevue/message'

const categories = ref<Category[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const newCategoryName = ref('')
const editingCategory = ref<Category | null>(null)
const editCategoryName = ref('')

const creating = ref(false)
const saving = ref(false)
const createError = ref<string | null>(null)
const saveError = ref<string | null>(null)

const fetchCategories = async () => {
  loading.value = true
  try {
    const data = await apiService.getCategories()
    categories.value = data.sort((a, b) => a.name.localeCompare(b.name))
  } catch (err: any) {
    console.error('Failed to load categories:', err)
    error.value = "Failed to load categories."
  } finally {
    loading.value = false
  }
}

const handleCreateCategory = async () => {
  if (!newCategoryName.value.trim()) return
  
  creating.value = true
  createError.value = null
  try {
    const newCat = await apiService.createCategory(newCategoryName.value.trim())
    categories.value.push(newCat)
    categories.value.sort((a, b) => a.name.localeCompare(b.name))
    showCreateDialog.value = false
    newCategoryName.value = ''
  } catch (err: any) {
    createError.value = err.message || "Failed to create category"
  } finally {
    creating.value = false
  }
}

const openEditDialog = (category: Category) => {
  editingCategory.value = category
  editCategoryName.value = category.name
  showEditDialog.value = true
  saveError.value = null
}

const handleUpdateCategory = async () => {
  if (!editingCategory.value || !editCategoryName.value.trim()) return
  
  saving.value = true
  saveError.value = null
  try {
    const updated = await apiService.updateCategory(editingCategory.value.id, editCategoryName.value.trim())
    const index = categories.value.findIndex(c => c.id === updated.id)
    if (index !== -1) {
      categories.value[index] = updated
    }
    categories.value.sort((a, b) => a.name.localeCompare(b.name))
    showEditDialog.value = false
    editingCategory.value = null
    editCategoryName.value = ''
  } catch (err: any) {
    saveError.value = err.message || "Failed to update category"
  } finally {
    saving.value = false
  }
}

onMounted(fetchCategories)
</script>

<template>
  <div class="categories-view">
    <div class="header-section">
      <div>
        <h2 class="text-2xl font-bold">Categories</h2>
        <p class="text-surface-500">Manage component categories</p>
      </div>
      <Button label="New Category" icon="pi pi-plus" @click="showCreateDialog = true" />
    </div>

    <Message v-if="error" severity="error" class="mb-4">{{ error }}</Message>

    <div class="card p-4 bg-surface-0 dark:bg-surface-900 border border-surface-200 dark:border-surface-800 rounded-xl shadow-sm">
      <DataTable 
        :value="categories" 
        :loading="loading" 
        stripedRows 
        responsiveLayout="stack" 
        breakpoint="960px"
        class="p-datatable-sm"
      >
        <template #empty>
          <div class="p-4 text-center text-surface-500">No categories found.</div>
        </template>
        
        <Column field="id" header="ID" sortable style="width: 100px"></Column>
        <Column field="name" header="Name" sortable></Column>
        <Column field="notes" header="Notes">
            <template #body="slotProps">
                {{ slotProps.data.notes || '-' }}
            </template>
        </Column>
        <Column header="Actions" style="width: 100px">
          <template #body="slotProps">
            <Button icon="pi pi-pencil" text rounded @click="openEditDialog(slotProps.data)" />
          </template>
        </Column>
      </DataTable>
    </div>

    <Dialog v-model:visible="showCreateDialog" header="Create New Category" :modal="true" :style="{ width: '400px' }">
      <div class="flex flex-col gap-4 mt-2">
        <div class="flex flex-col gap-2">
          <label for="name" class="font-semibold">Category Name</label>
          <InputText id="name" v-model="newCategoryName" @keyup.enter="handleCreateCategory" autofocus class="w-full" />
        </div>
        <Message v-if="createError" severity="error">{{ createError }}</Message>
      </div>
      <template #footer>
        <Button label="Cancel" text @click="showCreateDialog = false" />
        <Button label="Create" :loading="creating" @click="handleCreateCategory" />
      </template>
    </Dialog>

    <Dialog v-model:visible="showEditDialog" header="Edit Category" :modal="true" :style="{ width: '400px' }">
      <div class="flex flex-col gap-4 mt-2">
        <div class="flex flex-col gap-2">
          <label for="editName" class="font-semibold">Category Name</label>
          <InputText id="editName" v-model="editCategoryName" @keyup.enter="handleUpdateCategory" autofocus class="w-full" />
        </div>
        <Message v-if="saveError" severity="error">{{ saveError }}</Message>
      </div>
      <template #footer>
        <Button label="Cancel" text @click="showEditDialog = false" />
        <Button label="Save Changes" :loading="saving" @click="handleUpdateCategory" />
      </template>
    </Dialog>
  </div>
</template>

<style scoped>
.categories-view {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card {
  transition: transform 0.2s ease, shadow 0.2s ease;
}
</style>
