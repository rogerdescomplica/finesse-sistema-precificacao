// src/routes/api/auth/refresh/+server.ts

import type { RequestHandler } from '@sveltejs/kit';
import { env } from '$env/dynamic/private';
import { json } from '@sveltejs/kit';

export const POST: RequestHandler = async ({ cookies }) => {
	try {
		const refreshToken = cookies.get('refresh_token');

		if (!refreshToken) {
			return json({ error: 'Refresh token não encontrado' }, { status: 401 });
		}

		// Chama o backend para renovar o token
		const backendUrl = env.BACKEND_URL;
		const response = await fetch(`${backendUrl}/api/auth/refresh`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ tokenRefresh: refreshToken })
		});

		if (!response.ok) {
			// Refresh token inválido ou expirado
			cookies.delete('access_token', { path: '/' });
			cookies.delete('refresh_token', { path: '/' });
			return json({ error: 'Sessão expirada' }, { status: 401 });
		}

		const body = await response.json();

		// Atualiza os cookies com os novos tokens
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

		return json({ usuario: body.usuario }, { status: 200 });
	} catch (error) {
		console.error('Erro no proxy de refresh:', error);
		return json({ error: 'Erro ao renovar sessão' }, { status: 500 });
	}
};
