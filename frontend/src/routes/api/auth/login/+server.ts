// src/routes/api/auth/login/+server.ts

import { env } from '$env/dynamic/private';
import type { RequestHandler } from '@sveltejs/kit';
import { json } from '@sveltejs/kit';

// ESTE É O PROXY!
// Este é o endpoint que o frontend chama para fazer login
// Ele chama o backend Spring Boot e retorna o token de acesso
export const POST: RequestHandler = async ({ request }) => {
	try {
		// Pega os dados do formulário
		const data = await request.json();
		const email = String(data?.email || '').trim();
		const senha = String(data?.senha || '').trim();

		// Validação básica
		if (!email || !senha) {
			return json({ error: 'Credenciais inválidas' }, { status: 400 });
		}
		// Validação simples de email
		if (!email.includes('@')) {
			return json({ error: 'Email inválido' }, { status: 400 });
		}

		// Chama o backend Spring Boot
		const backendUrl = env.BACKEND_URL || 'http://localhost:8080';
		const response = await fetch(`${backendUrl}/api/auth/login`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ email, senha })
		});

		// Se o backend retornar erro
		if (!response.ok) {
			try {
				// Backend retorna: { error: "Email ou senha incorretos" }
				const errorData = await response.clone().json();

				return json(
					{ error: errorData.error || 'Erro ao fazer login' },
					{ status: response.status }
				);
			} catch {
				// Se não conseguir parsear, retorna erro genérico
				return json({ error: 'Erro ao fazer login' }, { status: response.status });
			}
		}

		// Backend retornou sucesso
		const body = await response.json();

		//Pega os cookies que o backend definiu
		const cookies = response.headers.getSetCookie();

		// Cria headers com múltiplos Set-Cookie
		const headers = new Headers({
			'Content-Type': 'application/json'
		});

		// Adiciona cada cookie individualmente
		cookies.forEach((cookie) => {
			headers.append('Set-Cookie', cookie);
		});

		return new Response(JSON.stringify(body), {
			status: 200,
			headers
		});
	} catch (error) {
		console.error('Erro no proxy de login:', error);
		return json({ error: 'Erro interno do servidor' }, { status: 500 });
	}
};
