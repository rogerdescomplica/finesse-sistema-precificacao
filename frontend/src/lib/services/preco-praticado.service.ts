export interface PrecoSetRequest {
  preco: number
  vigenciaInicio?: string
}

export interface PrecoSetResponse {
  changed: boolean
  vigente: {
    id: number
    servico: { id: number } | number
    preco: number
    vigenciaInicio: string
    vigenciaFim?: string | null
    vigente: boolean
  }
}

export interface PrecoAtual {
  servicoId: number
  precoAtual: number | null
}

class PrecoPraticadoService {
  private readonly baseUrl = '/api/servico'
  async definir(servicoId: number, req: PrecoSetRequest): Promise<PrecoSetResponse> {
    const r = await fetch(`${this.baseUrl}/${servicoId}/precos`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(req)
    })
    if (!r.ok) {
      const msg = await r.text().catch(() => 'Falha ao definir preço')
      throw new Error(msg || 'Falha ao definir preço')
    }
    return r.json()
  }

  async listarPrecosAtuais(): Promise<PrecoAtual[]> {
      const r = await fetch(`${this.baseUrl}/precos`, { credentials: 'include' })
      if (!r.ok) {
        const msg = await r.text().catch(() => 'Falha ao listar preços')
        throw new Error(msg || 'Falha ao listar preços')
      }
      const json = await r.json()
      return Array.isArray(json) ? json : []
    }
}



export const precoPraticadoService = new PrecoPraticadoService()
