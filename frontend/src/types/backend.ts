export interface Drawer {
  id: number;
  number: number;
}

export interface Category {
  id: number;
  name: string;
  notes: string | null;
}

export interface Tag {
  id: number;
  name: string;
  color: string;
}

export interface Part {
  id: number;
  name: string;
  description: string;
  stock: number;
  links: string[];
  notes: string | null;
  imageLink: string | null;
  drawer: Drawer | null;
  sectionNumber: number;
  category: Category | null;
  tag: Tag[];
}
