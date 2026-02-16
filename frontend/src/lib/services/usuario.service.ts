export type Perfil = 'ADMIN' | 'VISUALIZADOR';

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  ativo: boolean;
  perfil: Perfil;
  admin: boolean;
}

export interface UsuarioInput {
  nome: string
  email?: string
  senha?: string
  perfil?: Perfil
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

class UsuarioService {
  private readonly baseUrl = '/api/usuarios'

 async list(params?: URLSearchParams, signal?: AbortSignal): Promise<Page<Usuario>> {
     const qs = params?.toString()
     const url = qs ? `${this.baseUrl}?${qs}` : this.baseUrl
     const r = await fetch(url, { credentials: 'include', signal })
     if (!r.ok) throw new Error(await parseError(r, 'Falha ao carregar usuários'))
     return r.json()
   }

  async getByEmail(email: string): Promise<Usuario> {
    const r = await fetch(`${this.baseUrl}/email/${encodeURIComponent(email)}`, { credentials: 'include' })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao buscar usuário por email'))
    return r.json()
  }

  async create(data: UsuarioInput): Promise<Usuario> {
    const r = await fetch(this.baseUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({
        ativo: true,
        nome: data.nome,
        email: data.email,
        senha: data.senha,
        perfil: data.perfil
      })
    })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao cadastrar usuário'))
    return r.json()
  }

  async update(id: number, data: UsuarioInput): Promise<Usuario> {
    const r = await fetch(`${this.baseUrl}/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({
        nome: data.nome,
        email: data.email,
        perfil: data.perfil
      })
    })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao editar usuário'))
    const text = await r.text()
    const perfilOut = data.perfil ? data.perfil : 'VISUALIZADOR'
    return text ? JSON.parse(text) : ({ id: id, nome: data.nome, email: String(data.email ?? ''), ativo: Boolean(data.ativo ?? true), perfil: perfilOut, admin: perfilOut === 'ADMIN' })
  }

  async alterarSenha(id: number, novaSenha: string): Promise<void> {
    const r = await fetch(`${this.baseUrl}/${id}/senha`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ novaSenha })
    })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao alterar senha'))
  }

  async toggleStatus(id: number, ativo: boolean): Promise<Usuario> {
      const r = await fetch(`${this.baseUrl}/${id}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ ativo })
      })
      if (!r.ok) throw new Error(await parseError(r, 'Falha ao alterar status do usuário'))
      return r.json()
    }

  async remove(id: number): Promise<void> {
    const r = await fetch(`${this.baseUrl}/${id}`, {
      method: 'DELETE',
      credentials: 'include'
    })
    if (!r.ok) throw new Error(await parseError(r, 'Falha ao excluir usuário'))
  }
}

export const usuarioService = new UsuarioService()
