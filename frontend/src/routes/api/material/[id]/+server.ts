// src/routes/api/material/[id]/+server.ts

import { env } from '$env/dynamic/private'
import type { RequestHandler } from '@sveltejs/kit'
import { json } from '@sveltejs/kit'

// PROXY para operações individuais de materiais
const BACKEND_URL = env.BACKEND_URL || 'http://localhost:8080'

// GET - Obter material por ID
export const GET: RequestHandler = async ({ params, fetch, cookies }) => {
	try {
		// Pega o token JWT dos cookies
		const token = cookies.get('token')
		
		if (!token) {
			return json({ error: 'Não autenticado' }, { status: 401 })
		}

		// Validação do ID
		const id = params.id
		if (!id || isNaN(Number(id))) {
			return json({ error: 'ID inválido' }, { status: 400 })
		}

		// Chama o backend Spring Boot
		const response = await fetch(`${BACKEND_URL}/api/materiais/${id}`, {
			method: 'GET',
			headers: {
				'Content-Type': 'application/json',
				Cookie: `token=${token}`
			}
		})

		// Se o backend retornar erro (404 = not found)
		if (!response.ok) {
			try {
				const errorData = await response.clone().json()
				return json(
					{ error: errorData.error || errorData.message || 'Material não encontrado' },
					{ status: response.status }
				)
			} catch {
				// Backend retornou 404 sem body
				if (response.status === 404) {
					return json({ error: 'Material não encontrado' }, { status: 404 })
				}
				return json({ error: 'Erro ao buscar material' }, { status: response.status })
			}
		}

		// Backend retornou sucesso
		const body = await response.json()

		// Pega os cookies que o backend definiu
		const setCookies = response.headers.getSetCookie()

		if (setCookies.length > 0) {
			const headers = new Headers({
				'Content-Type': 'application/json'
			})

			setCookies.forEach((cookie) => {
				headers.append('Set-Cookie', cookie)
			})

			return new Response(JSON.stringify(body), {
				status: 200,
				headers
			})
		}

		return json(body, { status: 200 })
	} catch (error) {
		console.error('Erro no proxy GET material:', error)
		return json({ error: 'Erro interno do servidor' }, { status: 500 })
	}
}

// PUT - Atualizar material completo
// Requer role ADMIN ou MANAGER
export const PUT: RequestHandler = async ({ params, request, fetch, cookies }) => {
	try {
		// Pega o token JWT dos cookies
		const token = cookies.get('token')
		
		if (!token) {
			return json({ error: 'Não autenticado' }, { status: 401 })
		}

		// Validação do ID
		const id = params.id
		if (!id || isNaN(Number(id))) {
			return json({ error: 'ID inválido' }, { status: 400 })
		}

		// Pega os dados do formulário
		const data = await request.json()

		// Validação básica (mesma do backend com @Valid)
		if (!data.produto || String(data.produto).trim().length === 0) {
			return json({ error: 'Produto é obrigatório' }, { status: 400 })
		}

		if (!data.unidadeMedida) {
			return json({ error: 'Unidade de medida é obrigatória' }, { status: 400 })
		}

		// Validação de unidade de medida (enum no backend)
		const validUnits = ['UN', 'KG', 'G', 'L', 'ML']
		if (!validUnits.includes(data.unidadeMedida)) {
			return json({ error: 'Unidade de medida inválida. Use: UN, KG, G, L ou ML' }, { status: 400 })
		}

		if (!data.volumeEmbalagem || Number(data.volumeEmbalagem) <= 0) {
			return json({ error: 'Volume da embalagem deve ser maior que zero' }, { status: 400 })
		}

		if (!data.precoEmbalagem || Number(data.precoEmbalagem) <= 0) {
			return json({ error: 'Preço da embalagem deve ser maior que zero' }, { status: 400 })
		}

		if (data.custoUnitario === undefined || data.custoUnitario === null) {
			return json({ error: 'Custo unitário é obrigatório' }, { status: 400 })
		}

		// Chama o backend Spring Boot
		const response = await fetch(`${BACKEND_URL}/api/materiais/${id}`, {
			method: 'PUT',
			headers: {
				'Content-Type': 'application/json',
				Cookie: `token=${token}`
			},
			body: JSON.stringify(data)
		})

		// Se o backend retornar erro
		if (!response.ok) {
			try {
				const errorData = await response.clone().json()
				return json(
					{ error: errorData.error || errorData.message || 'Erro ao atualizar material' },
					{ status: response.status }
				)
			} catch {
				if (response.status === 404) {
					return json({ error: 'Material não encontrado' }, { status: 404 })
				}
				return json({ error: 'Erro ao atualizar material' }, { status: response.status })
			}
		}

		// Backend retornou sucesso
		const body = await response.json()

		// Pega os cookies que o backend definiu
		const setCookies = response.headers.getSetCookie()

		if (setCookies.length > 0) {
			const headers = new Headers({
				'Content-Type': 'application/json'
			})

			setCookies.forEach((cookie) => {
				headers.append('Set-Cookie', cookie)
			})

			return new Response(JSON.stringify(body), {
				status: 200,
				headers
			})
		}

		return json(body, { status: 200 })
	} catch (error) {
		console.error('Erro no proxy PUT material:', error)
		return json({ error: 'Erro interno do servidor' }, { status: 500 })
	}
}

// PATCH - Alterar status (ativar/inativar)
// Backend espera: { "ativo": true/false }
// Requer role ADMIN ou MANAGER
export const PATCH: RequestHandler = async ({ params, request, fetch, cookies }) => {
	try {
		// Pega o token JWT dos cookies
		const token = cookies.get('token')
		
		if (!token) {
			return json({ error: 'Não autenticado' }, { status: 401 })
		}

		// Validação do ID
		const id = params.id
		if (!id || isNaN(Number(id))) {
			return json({ error: 'ID inválido' }, { status: 400 })
		}

		// Pega os dados do formulário
		const data = await request.json()

		// Validação: backend espera { "ativo": boolean }
		if (typeof data.ativo !== 'boolean') {
			return json({ error: 'Campo "ativo" deve ser true ou false' }, { status: 400 })
		}

		// Chama o backend Spring Boot
		// Backend espera Map<String, Object> com campo "ativo"
		const response = await fetch(`${BACKEND_URL}/api/materiais/${id}`, {
			method: 'PATCH',
			headers: {
				'Content-Type': 'application/json',
				Cookie: `token=${token}`
			},
			body: JSON.stringify({ ativo: data.ativo })
		})

		// Se o backend retornar erro
		if (!response.ok) {
			try {
				const errorData = await response.clone().json()
				return json(
					{ error: errorData.error || errorData.message || 'Erro ao atualizar status' },
					{ status: response.status }
				)
			} catch {
				if (response.status === 404) {
					return json({ error: 'Material não encontrado' }, { status: 404 })
				}
				return json({ error: 'Erro ao atualizar status' }, { status: response.status })
			}
		}

		// Backend retornou sucesso
		const body = await response.json()

		// Pega os cookies que o backend definiu
		const setCookies = response.headers.getSetCookie()

		if (setCookies.length > 0) {
			const headers = new Headers({
				'Content-Type': 'application/json'
			})

			setCookies.forEach((cookie) => {
				headers.append('Set-Cookie', cookie)
			})

			return new Response(JSON.stringify(body), {
				status: 200,
				headers
			})
		}

		return json(body, { status: 200 })
	} catch (error) {
		console.error('Erro no proxy PATCH material:', error)
		return json({ error: 'Erro interno do servidor' }, { status: 500 })
	}
}

// DELETE - Remover material
// Backend retorna 200 se sucesso, 404 se não encontrado
// Requer role ADMIN
export const DELETE: RequestHandler = async ({ params, fetch, cookies }) => {
	try {
		// Pega o token JWT dos cookies
		const token = cookies.get('token')
		
		if (!token) {
			return json({ error: 'Não autenticado' }, { status: 401 })
		}

		// Validação do ID
		const id = params.id
		if (!id || isNaN(Number(id))) {
			return json({ error: 'ID inválido' }, { status: 400 })
		}

		// Chama o backend Spring Boot
		const response = await fetch(`${BACKEND_URL}/api/materiais/${id}`, {
			method: 'DELETE',
			headers: {
				Cookie: `token=${token}`
			}
		})

		// Se o backend retornar erro
		if (!response.ok) {
			try {
				const errorData = await response.clone().json()
				return json(
					{ error: errorData.error || errorData.message || 'Erro ao excluir material' },
					{ status: response.status }
				)
			} catch {
				if (response.status === 404) {
					return json({ error: 'Material não encontrado' }, { status: 404 })
				}
				return json({ error: 'Erro ao excluir material' }, { status: response.status })
			}
		}

		// Backend retorna 200 (não 204) quando sucesso
		// Pode retornar Void ou um body vazio
		const setCookies = response.headers.getSetCookie()

		if (setCookies.length > 0) {
			const headers = new Headers()

			setCookies.forEach((cookie) => {
				headers.append('Set-Cookie', cookie)
			})

			return new Response(null, {
				status: 200,
				headers
			})
		}

		// Sem cookies, retorna 200 (conforme controller)
		return new Response(null, { status: 200 })
	} catch (error) {
		console.error('Erro no proxy DELETE material:', error)
		return json({ error: 'Erro interno do servidor' }, { status: 500 })
	}
}