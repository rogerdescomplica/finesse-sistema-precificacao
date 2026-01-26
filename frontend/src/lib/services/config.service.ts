export interface Configuracao {
  id: number
  pretensaoSalarialMensal: number
  horasSemanais: number
  semanasMediaMes: number
  custoFixoPct: number
  margemLucroPadraoPct: number
  atualizadoEm: string
  ativo: boolean
}

export interface ConfigInput {
  pretensaoSalarialMensal: number
  horasSemanais: number
  semanasMediaMes: number
  custoFixoPct: number
  margemLucroPadraoPct: number
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

class ConfigService {
  private readonly baseUrl = '/api/config'

  async list(params?: URLSearchParams, signal?: AbortSignal): Promise<Page<Configuracao>> {
    const qs = params?.toString()
    const url = qs ? `${this.baseUrl}?${qs}` : this.baseUrl
    const r = await fetch(url, { credentials: 'include', signal })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao carregar configurações'))
    return r.json()
  }

  async create(data: ConfigInput): Promise<Configuracao> {
    const r = await fetch(`${this.baseUrl}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ ativo: true, ...data })
    })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao cadastrar configuração'))
    return r.json()
  }

  async update(id: number, data: ConfigInput): Promise<Configuracao> {
    const r = await fetch(`${this.baseUrl}/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(data)
    })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao editar configuração'))
    return r.json()
  }

  async toggleStatus(id: number, ativo: boolean): Promise<Configuracao> {
    const r = await fetch(`${this.baseUrl}/${id}`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ ativo })
    })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao alterar status da configuração'))
    return r.json()
  }

  async remove(id: number): Promise<void> {
    const r = await fetch(`${this.baseUrl}/${id}`, { method: 'DELETE', credentials: 'include' })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao excluir configuração'))
  }
}

export const configService = new ConfigService()
