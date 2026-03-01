import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
});

export const partService = {
  getAll() {
    return api.get('/parts');
  },
  getById(id: number) {
    return api.get(`/parts/${id}`);
  },
  create(data: any) {
    return api.post('/parts', data);
  },
  update(id: number, data: any) {
    return api.put(`/parts/${id}`, data);
  },
  delete(id: number) {
    return api.delete(`/parts/${id}`);
  }
};

export const categoryService = {
  getAll() {
    return api.get('/categories');
  },
  create(data: any) {
    return api.post('/categories', data);
  },
  update(id: number, data: any) {
    return api.put(`/categories/${id}`, data);
  },
  delete(id: number) {
    return api.delete(`/categories/${id}`);
  }
};

export const tagService = {
  getAll() {
    return api.get('/tags');
  },
  create(data: any) {
    return api.post('/tags', data);
  },
  update(id: number, data: any) {
    return api.put(`/tags/${id}`, data);
  },
  delete(id: number) {
    return api.delete(`/tags/${id}`);
  }
};

export const projectService = {
  getAll() {
    return api.get('/projects');
  },
  getById(id: number) {
    return api.get(`/projects/${id}`);
  },
  create(data: any) {
    return api.post('/projects', data);
  },
  update(id: number, data: any) {
    return api.put(`/projects/${id}`, data);
  },
  delete(id: number) {
    return api.delete(`/projects/${id}`);
  }
};

export const drawerService = {
  getAll() {
    return api.get('/drawers');
  },
  getById(id: number) {
    return api.get(`/drawers/${id}`);
  },
  create(data: any) {
    return api.post('/drawers', data);
  },
  update(id: number, data: any) {
    return api.put(`/drawers/${id}`, data);
  },
  delete(id: number) {
    return api.delete(`/drawers/${id}`);
  }
};

export const transactionService = {
  getAll() {
    return api.get('/transactions');
  },
  getById(id: number) {
    return api.get(`/transactions/${id}`);
  },
  create(data: any) {
    return api.post('/transactions', data);
  },
  delete(id: number) {
    return api.delete(`/transactions/${id}`);
  },
  addLine(data: any) {
    return api.post('/transaction-lines', data);
  },
  deleteLine(id: number) {
    return api.delete(`/transaction-lines/${id}`);
  }
};

export const fileService = {
  getFileUrl(file: string, type: 'itemImage' | 'doc') {
    return `http://localhost:8080/api/files/getFile?file=${encodeURIComponent(file)}&type=${type}`;
  }
};

export default api;
