// src/routes/api/auth/register/+server.ts

import type { RequestHandler } from '@sveltejs/kit';
import { BACKEND_URL } from '$env/static/private';	
import { json } from '@sveltejs/kit';

export const POST: RequestHandler = async ({ request, cookies }) => {
	try {
		const data = await request.json();

		// Validação básica dos campos obrigatórios
		const requiredFields = ['nome', 'email', 'senha', 'confirmarSenha', 'nomeClinica', 'cnpj'];
		const missingFields = requiredFields.filter((field) => !data[field]);

		if (missingFields.length > 0) {
			return json(
				{ error: `Campos obrigatórios faltando: ${missingFields.join(', ')}` },
				{ status: 400 }
			);
		}

		// Validação de senhas
		if (data.senha !== data.confirmarSenha) {
			return json({ error: 'As senhas não coincidem' }, { status: 400 });
		}

		// Chama o backend Spring Boot
		const response = await fetch(`${BACKEND_URL}/api/auth/register`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(data)
		});

		if (!response.ok) {
			return json({ error: 'Erro ao registrar' }, { status: response.status });
		}

		const body = await response.json();

		// Define cookies httpOnly
		cookies.set('access_token', body.tokenAcesso, {
			path: '/',
			httpOnly: true,
			sameSite: 'lax',
			secure: !import.meta.env.DEV,
			maxAge: 60 * 60 * 24
		});

		cookies.set('refresh_token', body.tokenRefresh, {
			path: '/',
			httpOnly: true,
			sameSite: 'lax',
			secure: !import.meta.env.DEV,
			maxAge: 60 * 60 * 24 * 7
		});

		return json({ usuario: body.usuario }, { status: 201 });
	} catch (error) {
		console.error('Erro no proxy de register:', error);
		return json({ error: 'Erro interno do servidor' }, { status: 500 });
	}
};
