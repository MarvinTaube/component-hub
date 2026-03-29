<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink, RouterView } from 'vue-router'
import Button from 'primevue/button'
import { apiService } from './services/api'

const isCollapsed = ref(false)
const exporting = ref(false)

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const exportToAI = async () => {
  exporting.value = true
  try {
    const parts = await apiService.getParts()
    
    // Sort by category then name
    const sortedParts = [...parts].sort((a, b) => {
      const catA = a.category?.name || 'Uncategorized'
      const catB = b.category?.name || 'Uncategorized'
      if (catA !== catB) return catA.localeCompare(catB)
      return a.name.localeCompare(b.name)
    })

    let mdContent = '# Component Inventory Overview\n\n'
    mdContent += 'This file provides an overview of available electronic components and their current stock levels for project planning context.\n\n'
    mdContent += '| Part Name | Category | Stock |\n'
    mdContent += '|-----------|----------|-------|\n'

    sortedParts.forEach(part => {
      const category = part.category?.name || 'Uncategorized'
      mdContent += `| ${part.name} | ${category} | ${part.stock} |\n`
    })

    const blob = new Blob([mdContent], { type: 'text/markdown' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `inventory_context_${new Date().toISOString().split('T')[0]}.md`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error('Export failed:', e)
  } finally {
    exporting.value = false
  }
}
</script>

<template>
  <div class="app-layout">
    <!-- Sidebar -->
    <aside :class="['sidebar', { 'collapsed': isCollapsed }]">
      <div class="sidebar-header">
        <h2 class="logo-text" v-if="!isCollapsed">Hub</h2>
        <Button 
          :icon="isCollapsed ? 'pi pi-angle-right' : 'pi pi-angle-left'" 
          text 
          rounded 
          aria-label="Toggle Sidebar" 
          @click="toggleSidebar" 
          class="toggle-btn"
        />
      </div>
      
      <nav class="sidebar-nav">
        <RouterLink to="/add-component" class="nav-link add-link" active-class="active">
          <i class="pi pi-plus-circle"></i>
          <span v-if="!isCollapsed" class="nav-text">Add Component</span>
        </RouterLink>
        <div class="nav-divider"></div>
        <RouterLink to="/" class="nav-link" active-class="active">
          <i class="pi pi-home"></i>
          <span v-if="!isCollapsed" class="nav-text">Home</span>
        </RouterLink>
        <RouterLink to="/categories" class="nav-link" active-class="active">
          <i class="pi pi-tags"></i>
          <span v-if="!isCollapsed" class="nav-text">Categories</span>
        </RouterLink>
        <RouterLink to="/drawers" class="nav-link" active-class="active">
          <i class="pi pi-database"></i>
          <span v-if="!isCollapsed" class="nav-text">Drawers</span>
        </RouterLink>
        <RouterLink to="/about" class="nav-link" active-class="active">
          <i class="pi pi-info-circle"></i>
          <span v-if="!isCollapsed" class="nav-text">About</span>
        </RouterLink>

        <div class="nav-divider"></div>
        
        <button class="nav-link export-btn" @click="exportToAI" :disabled="exporting">
          <i :class="exporting ? 'pi pi-spin pi-spinner' : 'pi pi-download'"></i>
          <span v-if="!isCollapsed" class="nav-text">AI Export (.md)</span>
        </button>
      </nav>
    </aside>

    <!-- Main Content -->
    <main class="main-content">
      <header class="top-header">
        <h1>Component Hub</h1>
      </header>
      <div class="content-body">
        <RouterView />
      </div>
    </main>
  </div>
</template>

<style>
/* Remove default #app styles to allow full height */
#app {
  width: 100%;
  height: 100vh;
  margin: 0;
  padding: 0;
  max-width: none;
}
body {
  margin: 0;
}
</style>

<style scoped>
.app-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background-color: var(--p-surface-50, #f8fafc);
  color: var(--p-surface-900, #0f172a);
}

.sidebar {
  width: 260px;
  background-color: var(--p-surface-0, #ffffff);
  border-right: 1px solid var(--p-surface-200, #e2e8f0);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  z-index: 10;
}

.sidebar.collapsed {
  width: 80px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem 1rem;
  height: 70px;
  border-bottom: 1px solid var(--p-surface-100, #f1f5f9);
  overflow: hidden;
}

.sidebar.collapsed .sidebar-header {
  justify-content: center;
}

.logo-text {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
  color: var(--p-primary-color, #10b981);
  white-space: nowrap;
}

.toggle-btn {
  width: 2rem !important;
  height: 2rem !important;
}

.sidebar-nav {
  flex: 1;
  padding: 1rem 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  overflow-x: hidden;
}

.nav-divider {
  height: 1px;
  background-color: var(--p-surface-200, #e2e8f0);
  margin: 0.5rem 1.5rem;
}

.sidebar.collapsed .nav-divider {
  margin: 0.5rem;
}

.add-link {
  color: var(--p-primary-color, #10b981) !important;
  font-weight: 700;
  margin-bottom: 0.25rem;
}

.add-link:hover {
  background-color: var(--p-primary-50, #ecfdf5) !important;
  color: var(--p-primary-700, #047857) !important;
}

.add-link.active {
  background-color: var(--p-primary-100, #d1fae5) !important;
  border-left-color: var(--p-primary-600, #059669) !important;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem 1.5rem;
  color: var(--p-surface-600, #475569);
  text-decoration: none;
  font-weight: 500;
  transition: all 0.2s ease;
  border-left: 3px solid transparent;
}

.sidebar.collapsed .nav-link {
  justify-content: center;
  padding: 0.75rem;
  margin: 0 0.5rem;
  border-radius: 8px;
  border-left: none;
  width: calc(100% - 1rem); /* to match padding margins */
  box-sizing: border-box;
}

.nav-link:hover {
  background-color: var(--p-surface-100, #f1f5f9);
  color: var(--p-surface-900, #0f172a);
}

.nav-link.active {
  color: var(--p-primary-color, #10b981);
  background-color: var(--p-primary-50, #ecfdf5);
  border-left-color: var(--p-primary-color, #10b981);
}

.export-btn {
  background: none;
  border: none;
  width: 100%;
  cursor: pointer;
  text-align: left;
  font-family: inherit;
  font-size: inherit;
}

.export-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.sidebar.collapsed .nav-link.active {
  border-left: none;
}

.nav-link i {
  font-size: 1.25rem;
  min-width: 1.25rem;
}

.nav-text {
  white-space: nowrap;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.top-header {
  height: 70px;
  padding: 0 2rem;
  display: flex;
  align-items: center;
  background-color: var(--p-surface-0, #ffffff);
  border-bottom: 1px solid var(--p-surface-200, #e2e8f0);
}

.top-header h1 {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
}

.content-body {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
}

@media (prefers-color-scheme: dark) {
  .app-layout {
    background-color: var(--p-surface-950, #020617);
    color: var(--p-surface-50, #f8fafc);
  }
  .sidebar, .top-header {
    background-color: var(--p-surface-900, #0f172a);
    border-color: var(--p-surface-800, #1e293b);
  }
  .sidebar-header {
    border-color: var(--p-surface-800, #1e293b);
  }
  .nav-link {
    color: var(--p-surface-400, #94a3b8);
  }
  .nav-link:hover {
    background-color: var(--p-surface-800, #1e293b);
    color: var(--p-surface-50, #f8fafc);
  }
  .nav-link.active {
    background-color: rgba(16, 185, 129, 0.1);
  }
  .nav-divider {
    background-color: var(--p-surface-800, #1e293b);
  }
  .add-link:hover {
    background-color: rgba(16, 185, 129, 0.15) !important;
    color: var(--p-primary-300, #6ee7b7) !important;
  }
  .add-link.active {
    background-color: rgba(16, 185, 129, 0.2) !important;
  }
}
</style>
