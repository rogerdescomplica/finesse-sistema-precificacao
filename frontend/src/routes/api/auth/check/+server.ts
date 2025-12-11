// src/routes/api/auth/check/+server.ts

import type { RequestHandler } from '@sveltejs/kit';

/**
 * Verifica se existem cookies de autenticação válidos
 * Não precisa chamar o backend, apenas verifica se os cookies existem
 */
export const GET: RequestHandler = async ({ cookies }) => {
	const accessToken = cookies.get('access_token');
	const refreshToken = cookies.get('refresh_token');

	if (accessToken || refreshToken) {
		return new Response(JSON.stringify({ authenticated: true }), {
			status: 200,
			headers: { 'Content-Type': 'application/json' }
		});
	}

	return new Response(JSON.stringify({ authenticated: false }), {
		status: 401,
		headers: { 'Content-Type': 'application/json' }
	});
};
