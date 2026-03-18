import type { Part, Drawer, Category, PartRequest } from '../types/backend'

const API_BASE_URL = 'http://localhost:8080/api'

export const apiService = {
  async getParts(): Promise<Part[]> {
    const response = await fetch(`${API_BASE_URL}/parts`)
    if (!response.ok) {
      throw new Error(`Failed to fetch parts: ${response.statusText}`)
    }
    return response.json()
  },

  async getPartById(id: string | number): Promise<Part> {
    const response = await fetch(`${API_BASE_URL}/parts/${id}`)
    if (!response.ok) {
      throw new Error(`Failed to fetch part: ${response.statusText}`)
    }
    return response.json()
  },

  async getDrawers(): Promise<Drawer[]> {
    const response = await fetch(`${API_BASE_URL}/drawers`)
    if (!response.ok) {
      throw new Error(`Failed to fetch drawers: ${response.statusText}`)
    }
    return response.json()
  },

  async getCategories(): Promise<Category[]> {
    const response = await fetch(`${API_BASE_URL}/categories`)
    if (!response.ok) {
      throw new Error(`Failed to fetch categories: ${response.statusText}`)
    }
    return response.json()
  },

  async createCategory(name: string): Promise<Category> {
    const response = await fetch(`${API_BASE_URL}/categories`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name })
    })
    if (!response.ok) {
      throw new Error('Failed to create category')
    }
    return response.json()
  },

  async createPart(data: PartRequest): Promise<Part> {
    const response = await fetch(`${API_BASE_URL}/parts`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    })
    if (!response.ok) {
      throw new Error(`Failed to create part: ${response.statusText}`)
    }
    return response.json()
  },

  async createDrawer(number: number): Promise<Drawer> {
    const response = await fetch(`${API_BASE_URL}/drawers`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ number }),
    })
    if (!response.ok) {
      throw new Error(`Failed to create drawer: ${response.statusText}`)
    }
    return response.json()
  },

  async uploadFile(file: File, type: 'itemImage' | 'doc'): Promise<string> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('type', type)

    const response = await fetch(`${API_BASE_URL}/files/upload`, {
      method: 'POST',
      body: formData,
    })

    if (!response.ok) {
      throw new Error(`Failed to upload file: ${response.statusText}`)
    }

    const result = await response.json()
    return result.url
  }
}
