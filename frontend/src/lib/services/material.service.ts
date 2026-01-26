export type UnidadeMedida = 'UN' | 'KG' | 'G' | 'L' | 'ML' | 'M'

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
  observacoes?: string
  ativo?: boolean
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  first: boolean
  last: boolean
  numberOfElements: number
  empty: boolean
}

type ApiErrorResponse = { error?: string; message?: string }

async function parseError(r: Response, fallback: string) {
  try {
    const body = (await r.clone().json()) as ApiErrorResponse
    return body?.error || body?.message || fallback
  } catch {
    return fallback
  }
}

class MaterialService {
  private readonly baseUrl = '/api/material'

  async list(params?: URLSearchParams, signal?: AbortSignal): Promise<Page<Material>> {
    const qs = params?.toString()
    const url = qs ? `${this.baseUrl}?${qs}` : this.baseUrl
    const r = await fetch(url, { credentials: 'include', signal })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao carregar materiais'))
    return r.json()
  }

  async create(data: MaterialInput): Promise<Material> {
    const r = await fetch(`${this.baseUrl}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ ativo: true, ...data })
    })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao cadastrar material'))
    return r.json()
  }

  async update(id: number, data: MaterialInput): Promise<Material> {
    const r = await fetch(`${this.baseUrl}/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(data)
    })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao editar material'))
    return r.json()
  }

  async toggleStatus(id: number, ativo: boolean): Promise<Material> {
    const r = await fetch(`${this.baseUrl}/${id}`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ ativo })
    })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao alterar status do material'))
    return r.json()
  }

  async remove(id: number): Promise<void> {
    const r = await fetch(`${this.baseUrl}/${id}`, { method: 'DELETE', credentials: 'include' })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao excluir material'))
  }
}

export const materialService = new MaterialService()
