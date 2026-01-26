import type { RequestHandler } from '@sveltejs/kit';
import { BACKEND_URL } from '$env/static/private';

export const GET: RequestHandler = async () => {
	try {
		
		console.log('Testando conexão com:', BACKEND_URL);

		const response = await fetch(`${BACKEND_URL}/api/auth/login`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ email: 'admin@sistema.com', senha: '123456' })
		});

		const text = await response.text();
		console.log('Status:', response.status);
		console.log('Response:', text);

		return new Response(
			JSON.stringify({
				status: response.status,
				body: text
			})
		);
	} catch (error) {
		console.error('Erro:', error);
		return new Response(
			JSON.stringify({
				error: String(error)
			}),
			{ status: 500 }
		);
	}
};
