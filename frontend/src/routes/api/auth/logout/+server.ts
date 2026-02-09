// src/routes/api/auth/logout/+server.ts

import type { RequestHandler } from '@sveltejs/kit';
import { BACKEND_URL } from '$env/static/private';
import { json } from '@sveltejs/kit';

export const POST: RequestHandler = async ({ cookies }) => {
	try {
		const accessToken = cookies.get('access_token');

		// Se tem token, tenta revogar no backend
		if (accessToken) {
			try {
				await fetch(`${BACKEND_URL}/api/auth/logout`, {
					method: 'POST',
					headers: {
						Authorization: `Bearer ${accessToken}`,
						'Content-Type': 'application/json'
					}
				});
			} catch (error) {
				// Se falhar no backend, continua e remove os cookies mesmo assim
				console.error('Erro ao fazer logout no backend:', error);
			}
		}

		// Remove os cookies independente do resultado do backend
		cookies.delete('access_token', { path: '/api' });
		cookies.delete('refresh_token', { path: '/api' });

		return json({ success: true }, { status: 200 });
	} catch (error) {
		console.error('Erro no proxy de logout:', error);

		// Mesmo com erro, remove os cookies
		cookies.delete('access_token', { path: '/api' });
		cookies.delete('refresh_token', { path: '/api' });

		return json({ error: 'Erro ao fazer logout' }, { status: 500 });
	}
};
