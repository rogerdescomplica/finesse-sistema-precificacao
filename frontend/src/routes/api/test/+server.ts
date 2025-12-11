import type { RequestHandler } from '@sveltejs/kit';
import { env } from '$env/dynamic/private';

export const GET: RequestHandler = async () => {
	try {
		const backendUrl = env.BACKEND_URL;
		console.log('Testando conexão com:', backendUrl);

		const response = await fetch(`${backendUrl}/api/auth/login`, {
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
