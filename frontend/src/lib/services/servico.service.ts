export interface Servico {
  id: number
  nome: string
  grupo: string
  duracaoMinutos: number
  margemLucroCustomPct?: number
  atividade?: { id: number; nome?: string }
  precoVigente?: number | null
  ativo: boolean
}

export interface ServicoMaterialLink {
  id: number
  materialId: number
  produto: string
  custoUnitario: number
  quantidadeUsada: number
}

export interface ServicoDetail {
  id: number
  nome: string
  grupo: string
  duracaoMinutos: number
  atividadeId?: number
  atividadeNome?: string
  atividadeAliquotaTotalPct?: number
  atividadeIssPct?: number
  margemLucroCustomPct?: number
  ativo: boolean
  precoVigente?: number | null
  materiais: ServicoMaterialLink[]
}

export interface ServicoInput {
  nome: string
  grupo: string
  duracaoMinutos: number
  atividadeId: number
  margemLucroCustomPct?: number
  ativo?: boolean
  materiais?: Array<{ materialId: number; quantidadeUsada: number }>
  precoPraticado?: number
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

class ServicoService {
  private readonly baseUrl = '/api/servico'

  async list(params?: URLSearchParams, signal?: AbortSignal): Promise<Page<Servico>> {
    const qs = params?.toString()
    const url = qs ? `${this.baseUrl}?${qs}` : this.baseUrl
    const r = await fetch(url, { credentials: 'include', signal })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao carregar serviços'))
    return r.json()
  }

  async create(data: ServicoInput): Promise<Servico> {
    const r = await fetch(`${this.baseUrl}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({
        ativo: true,
        nome: data.nome,
        grupo: data.grupo,
        duracaoMinutos: data.duracaoMinutos,
        margemLucroCustomPct: data.margemLucroCustomPct,
        atividade: { id: data.atividadeId },
        materiais: (data.materiais ?? []).map((m) => ({
          material: { id: m.materialId },
          quantidadeUsada: m.quantidadeUsada
        })),
        precoPraticadoInput: data.precoPraticado
      })
    })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao cadastrar serviço'))
    return r.json()
  }

  async update(id: number, data: ServicoInput): Promise<Servico> {
    const r = await fetch(`${this.baseUrl}/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({
        nome: data.nome,
        grupo: data.grupo,
        duracaoMinutos: data.duracaoMinutos,
        margemLucroCustomPct: data.margemLucroCustomPct,
        atividade: { id: data.atividadeId },
        materiais: (data.materiais ?? []).map((m) => ({
          material: { id: m.materialId },
          quantidadeUsada: m.quantidadeUsada
        })),
        precoPraticadoInput: data.precoPraticado
      })
    })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao editar serviço'))
    return r.json()
  }

  async get(id: number): Promise<ServicoDetail> {
    const r = await fetch(`${this.baseUrl}/${id}`, { credentials: 'include' })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao carregar serviço'))
    const json = await r.json()
    // Normalização de tipos numéricos
    json.materiais = (json.materiais || []).map((m: ServicoMaterialLink) => ({
      id: m.id,
      materialId: m.materialId,
      produto: m.produto,
      custoUnitario: Number(m.custoUnitario ?? 0),
      quantidadeUsada: Number(m.quantidadeUsada ?? 0)
    }))
    json.precoVigente = json.precoVigente == null ? null : Number(json.precoVigente)
    return json as ServicoDetail
  }
  async toggleStatus(id: number, ativo: boolean): Promise<Servico> {
    const r = await fetch(`${this.baseUrl}/${id}`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ ativo })
    })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao alterar status do serviço'))
    return r.json()
  }

  async remove(id: number): Promise<void> {
    const r = await fetch(`${this.baseUrl}/${id}`, { method: 'DELETE', credentials: 'include' })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao excluir serviço'))
  }
}

export const servicoService = new ServicoService()
