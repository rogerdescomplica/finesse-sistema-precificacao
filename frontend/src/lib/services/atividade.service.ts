export interface Atividade {
  id: number
  nome: string
  cnae: string
  aliquotaTotalPct: number
  issPct: number
  observacao?: string
  ativo: boolean
}

export interface AtividadeInput {
  nome: string
  cnae: string
  aliquotaTotalPct: number
  issPct: number
  observacao?: string
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

class AtividadeService {
  private readonly baseUrl = '/api/atividades'

  async list(params?: URLSearchParams, signal?: AbortSignal): Promise<Page<Atividade>> {
    const qs = params?.toString()
    const url = qs ? `${this.baseUrl}?${qs}` : this.baseUrl
    const r = await fetch(url, { credentials: 'include', signal })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao carregar atividades'))
    return r.json()
  }

  async create(data: AtividadeInput): Promise<Atividade> {
    const r = await fetch(`${this.baseUrl}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ ativo: true, ...data })
    })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao cadastrar atividade'))
    return r.json()
  }

  async update(id: number, data: AtividadeInput): Promise<Atividade> {
    const r = await fetch(`${this.baseUrl}/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(data)
    })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao editar atividade'))
    return r.json()
  }

  async toggleStatus(id: number, ativo: boolean): Promise<Atividade> {
    const r = await fetch(`${this.baseUrl}/${id}`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ ativo })
    })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao alterar status da atividade'))
    return r.json()
  }

  async remove(id: number): Promise<void> {
    const r = await fetch(`${this.baseUrl}/${id}`, { method: 'DELETE', credentials: 'include' })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao excluir atividade'))
  }
}

export const atividadeService = new AtividadeService()
