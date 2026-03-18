import type { Part } from '../types/backend'

const API_BASE_URL = 'http://localhost:8080/api'

export const apiService = {
  async getParts(): Promise<Part[]> {
    const response = await fetch(`${API_BASE_URL}/parts`)
    if (!response.ok) {
      throw new Error(`Failed to fetch parts: ${response.statusText}`)
    }
    return response.json()
  }
}
