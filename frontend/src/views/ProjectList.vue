<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { projectService } from '../services/api';
import Card from 'primevue/card';
import Button from 'primevue/button';
import Tag from 'primevue/tag';
import ProgressSpinner from 'primevue/progressspinner';

const projects = ref<any[]>([]);
const loading = ref(true);

onMounted(async () => {
  try {
    const res = await projectService.getAll();
    projects.value = res.data;
  } catch (err) {
    console.error('Failed to load projects', err);
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="projects-container">
    <div class="header flex justify-content-between align-items-center mb-6">
      <div>
        <h1 class="text-4xl font-light m-0">Projects</h1>
        <p class="text-500 mt-2">Track components used in your builds.</p>
      </div>
      <Button label="New Project" icon="pi pi-plus" />
    </div>

    <div v-if="loading" class="flex justify-content-center p-8">
      <ProgressSpinner />
    </div>

    <div v-else class="grid">
      <div v-for="project in projects" :key="project.id" class="col-12 md:col-6 lg:col-4">
        <Card class="project-card h-full border-none shadow-1">
          <template #title>
            <div class="flex justify-content-between align-items-start">
              <span class="text-xl font-semibold">{{ project.name }}</span>
              <Tag :severity="project.finished ? 'success' : 'info'" :value="project.finished ? 'Finished' : 'In Progress'" rounded />
            </div>
          </template>
          <template #content>
            <p class="text-500 line-height-3 mb-4">{{ project.description }}</p>
            <div v-if="project.blogLink" class="mb-3">
               <a :href="project.blogLink" target="_blank" class="text-blue-500 no-underline flex align-items-center">
                 <i class="pi pi-external-link mr-2 text-xs"></i> Project Documentation
               </a>
            </div>
          </template>
          <template #footer>
            <div class="flex gap-2">
              <Button label="View Details" variant="outlined" size="small" class="flex-grow-1" />
              <Button icon="pi pi-pencil" variant="text" size="small" />
            </div>
          </template>
        </Card>
      </div>
      <div v-if="projects.length === 0" class="col-12 text-center py-8 text-500">
        <i class="pi pi-folder-open text-6xl mb-4"></i>
        <p class="text-xl">No projects found. Start by creating one!</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.projects-container {
  max-width: 1200px;
  margin: 0 auto;
}

.project-card {
  transition: transform 0.2s;
}

.project-card:hover {
  transform: translateY(-4px);
}

.grid {
  display: flex;
  flex-wrap: wrap;
  margin-right: -1rem;
  margin-left: -1rem;
}

.col-12, .md\:col-6, .lg\:col-4 {
  padding: 1rem;
}

@media screen and (min-width: 768px) {
  .md\:col-6 { flex: 0 0 auto; width: 50%; }
}

@media screen and (min-width: 992px) {
  .lg\:col-4 { flex: 0 0 auto; width: 33.3333%; }
}

.flex { display: flex; }
.align-items-center { align-items: center; }
.align-items-start { align-items: flex-start; }
.justify-content-between { justify-content: space-between; }
.justify-content-center { justify-content: center; }
.mb-3 { margin-bottom: 0.75rem; }
.mb-4 { margin-bottom: 1rem; }
.mb-6 { margin-bottom: 2rem; }
.mt-2 { margin-top: 0.5rem; }
.mr-2 { margin-right: 0.5rem; }
.p-8 { padding: 4rem; }
.m-0 { margin: 0; }
.flex-grow-1 { flex-grow: 1; }
.gap-2 { gap: 0.5rem; }
.border-none { border: none !important; }
.shadow-1 { box-shadow: 0 1px 2px 0 rgba(0,0,0,0.05); }
.text-4xl { font-size: 2.25rem; }
.text-xl { font-size: 1.25rem; }
.text-500 { color: #6b7280; }
.text-blue-500 { color: #3b82f6; }
.text-xs { font-size: 0.75rem; }
.font-light { font-weight: 300; }
.font-semibold { font-weight: 600; }
.line-height-3 { line-height: 1.5; }
.no-underline { text-decoration: none; }
</style>
