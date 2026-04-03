<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { apiService } from '../services/api'
import type { Drawer } from '../types/backend'

import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import InputNumber from 'primevue/inputnumber'
import Dialog from 'primevue/dialog'
import Message from 'primevue/message'

const drawers = ref<Drawer[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const newDrawerNumber = ref<number | null>(null)
const editingDrawer = ref<Drawer | null>(null)
const editDrawerNumber = ref<number | null>(null)

const creating = ref(false)
const saving = ref(false)
const createError = ref<string | null>(null)
const saveError = ref<string | null>(null)

const fetchDrawers = async () => {
  loading.value = true
  try {
    const data = await apiService.getDrawers()
    drawers.value = data.sort((a, b) => a.number - b.number)
  } catch (err: any) {
    console.error('Failed to load drawers:', err)
    error.value = "Failed to load drawers."
  } finally {
    loading.value = false
  }
}

const handleCreateDrawer = async () => {
  if (newDrawerNumber.value === null) return
  
  creating.value = true
  createError.value = null
  try {
    const newDrw = await apiService.createDrawer(newDrawerNumber.value)
    drawers.value.push(newDrw)
    drawers.value.sort((a, b) => a.number - b.number)
    showCreateDialog.value = false
    newDrawerNumber.value = null
  } catch (err: any) {
    createError.value = err.message || "Failed to create drawer"
  } finally {
    creating.value = false
  }
}

const openEditDialog = (drawer: Drawer) => {
  editingDrawer.value = drawer
  editDrawerNumber.value = drawer.number
  showEditDialog.value = true
  saveError.value = null
}

const handleUpdateDrawer = async () => {
  if (!editingDrawer.value || editDrawerNumber.value === null) return
  
  saving.value = true
  saveError.value = null
  try {
    const updated = await apiService.updateDrawer(editingDrawer.value.id, editDrawerNumber.value)
    const index = drawers.value.findIndex(d => d.id === updated.id)
    if (index !== -1) {
      drawers.value[index] = updated
    }
    drawers.value.sort((a, b) => a.number - b.number)
    showEditDialog.value = false
    editingDrawer.value = null
    editDrawerNumber.value = null
  } catch (err: any) {
    saveError.value = err.message || "Failed to update drawer"
  } finally {
    saving.value = false
  }
}

onMounted(fetchDrawers)
</script>

<template>
  <div class="drawers-view">
    <div class="header-section">
      <div>
        <h2 class="text-2xl font-bold">Drawers</h2>
        <p class="text-surface-500">Manage location drawers</p>
      </div>
      <Button label="New Drawer" icon="pi pi-plus" @click="showCreateDialog = true" />
    </div>

    <Message v-if="error" severity="error" class="mb-4">{{ error }}</Message>

    <div class="card p-4 bg-surface-0 dark:bg-surface-900 border border-surface-200 dark:border-surface-800 rounded-xl shadow-sm">
      <DataTable 
        :value="drawers" 
        :loading="loading" 
        stripedRows 
        responsiveLayout="stack" 
        breakpoint="960px"
        class="p-datatable-sm"
      >
        <template #empty>
          <div class="p-4 text-center text-surface-500">No drawers found.</div>
        </template>
        
        <Column field="id" header="ID" sortable style="width: 100px"></Column>
        <Column field="number" header="Drawer Number" sortable></Column>
        <Column header="Actions" style="width: 100px">
          <template #body="slotProps">
            <Button icon="pi pi-pencil" text rounded @click="openEditDialog(slotProps.data)" />
          </template>
        </Column>
      </DataTable>
    </div>

    <Dialog v-model:visible="showCreateDialog" header="Create New Drawer" :modal="true" :style="{ width: '400px' }">
      <div class="flex flex-col gap-4 mt-2">
        <div class="flex flex-col gap-2">
          <label for="number" class="font-semibold">Drawer Number</label>
          <InputNumber id="number" v-model="newDrawerNumber" @keyup.enter="handleCreateDrawer" autofocus :useGrouping="false" class="w-full" />
        </div>
        <Message v-if="createError" severity="error">{{ createError }}</Message>
      </div>
      <template #footer>
        <Button label="Cancel" text @click="showCreateDialog = false" />
        <Button label="Create" :loading="creating" @click="handleCreateDrawer" />
      </template>
    </Dialog>

    <Dialog v-model:visible="showEditDialog" header="Edit Drawer" :modal="true" :style="{ width: '400px' }">
      <div class="flex flex-col gap-4 mt-2">
        <div class="flex flex-col gap-2">
          <label for="editNumber" class="font-semibold">Drawer Number</label>
          <InputNumber id="editNumber" v-model="editDrawerNumber" @keyup.enter="handleUpdateDrawer" autofocus :useGrouping="false" class="w-full" />
        </div>
        <Message v-if="saveError" severity="error">{{ saveError }}</Message>
      </div>
      <template #footer>
        <Button label="Cancel" text @click="showEditDialog = false" />
        <Button label="Save Changes" :loading="saving" @click="handleUpdateDrawer" />
      </template>
    </Dialog>
  </div>
</template>

<style scoped>
.drawers-view {
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
