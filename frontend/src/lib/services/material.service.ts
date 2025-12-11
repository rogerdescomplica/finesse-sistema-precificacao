export type UnidadeMedida = 'UN' | 'KG' | 'G' | 'L' | 'ML'

export interface Material {
  id: number
  produto: string
  unidadeMedida: UnidadeMedida
  volumeEmbalagem: number
  precoEmbalagem: number
  custoUnitario: number
  observacoes?: string
  ativo: boolean
}

export interface MaterialInput {
  produto: string
  unidadeMedida: UnidadeMedida
  volumeEmbalagem: number
  precoEmbalagem: number
  custoUnitario: number
  observacoes?: string
  ativo?: boolean
}

// Crie o tipo Page
interface Page<T> {
    content: T[]
    pageable: {
        pageNumber: number
        pageSize: number
        sort: {
            empty: boolean
            sorted: boolean
            unsorted: boolean
        }
        offset: number
        paged: boolean
        unpaged: boolean
    }
    totalElements: number
    totalPages: number
    last: boolean
    size: number
    number: number
    sort: {
        empty: boolean
        sorted: boolean
        unsorted: boolean
    }
    first: boolean
    numberOfElements: number
    empty: boolean
}

class MaterialService {
  private readonly base = '/api/material'

async list(): Promise<Page<Material>> {
    const r = await fetch(this.base, { credentials: 'include' })
    if (!r.ok) throw new Error('Falha ao carregar materiais')
    return r.json();
}

  async create(data: MaterialInput): Promise<Material> {
    const r = await fetch(this.base, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ ativo: true, ...data })
    })
    if (!r.ok) throw new Error('Falha ao cadastrar material')
    return r.json()
  }

  async update(id: number, data: MaterialInput): Promise<Material> {
    const r = await fetch(`${this.base}/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(data)
    })
    if (!r.ok) throw new Error('Falha ao editar material')
    return r.json()
  }

  async toggleStatus(id: number, ativo: boolean): Promise<Material> {
    const r = await fetch(`${this.base}/${id}`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ ativo })
    })
    if (!r.ok) throw new Error('Falha ao alterar status do material')
    return r.json()
  }

  async remove(id: number): Promise<void> {
    const r = await fetch(`${this.base}/${id}`, { method: 'DELETE', credentials: 'include' })
    if (!r.ok) throw new Error('Falha ao excluir material')
  }
}

export const materialService = new MaterialService()
