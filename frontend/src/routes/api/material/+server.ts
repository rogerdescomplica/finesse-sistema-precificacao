// src/routes/api/material/+server.ts

import { env } from '$env/dynamic/private'
import type { RequestHandler } from '@sveltejs/kit'
import { json } from '@sveltejs/kit'

// PROXY para listar e criar materiais
// Backend aceita tanto /api/material quanto /api/materiais
const BACKEND_URL = env.BACKEND_URL || 'http://localhost:8080'

// GET - Listar todos os materiais (com suporte a paginação e filtros)
export const GET: RequestHandler = async ({ url, fetch, cookies }) => {
	try {
        // Lê cookies de autenticação definidos pelo backend
        const access = cookies.get('access_token')
        const refresh = cookies.get('refresh_token')
        if (!access && !refresh) {
            return json({ error: 'Não autenticado' }, { status: 401 })
        }

		// Extrai os query params da URL (produto, ativo, page, size, sort)
		const searchParams = url.searchParams
		const queryString = searchParams.toString()

		// Monta a URL completa com query params
		const backendUrl = queryString 
			? `${BACKEND_URL}/api/materiais?${queryString}`
			: `${BACKEND_URL}/api/materiais`

		// Chama o backend Spring Boot
        const response = await fetch(backendUrl, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                Cookie: [`access_token=${access ?? ''}`, refresh ? `refresh_token=${refresh}` : ''].filter(Boolean).join('; ')
            }
        })

		// Se o backend retornar erro
		if (!response.ok) {
			try {
				const errorData = await response.clone().json()
				return json(
					{ error: errorData.error || errorData.message || 'Erro ao carregar materiais' },
					{ status: response.status }
				)
			} catch {
				return json({ error: 'Erro ao carregar materiais' }, { status: response.status })
			}
		}

		// Backend retornou sucesso (Page<Material>)
		const body = await response.json()

        return json(body, { status: 200 })
	} catch (error) {
		console.error('Erro no proxy GET materiais:', error)
		return json({ error: 'Erro interno do servidor' }, { status: 500 })
	}
}

// POST - Criar novo material
// Requer role ADMIN ou MANAGER
export const POST: RequestHandler = async ({ request, fetch, cookies }) => {
	try {
        const access = cookies.get('access_token')
        const refresh = cookies.get('refresh_token')
        if (!access && !refresh) {
            return json({ error: 'Não autenticado' }, { status: 401 })
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
        const response = await fetch(`${BACKEND_URL}/api/materiais`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                Cookie: [`access_token=${access ?? ''}`, refresh ? `refresh_token=${refresh}` : ''].filter(Boolean).join('; ')
            },
            body: JSON.stringify(data)
        })

		// Se o backend retornar erro
		if (!response.ok) {
			try {
				const errorData = await response.clone().json()
				return json(
					{ error: errorData.error || errorData.message || 'Erro ao criar material' },
					{ status: response.status }
				)
			} catch {
				return json({ error: 'Erro ao criar material' }, { status: response.status })
			}
		}

        const body = await response.json()
        return json(body, { status: 201 })
	} catch (error) {
		console.error('Erro no proxy POST materiais:', error)
		return json({ error: 'Erro interno do servidor' }, { status: 500 })
	}
}
