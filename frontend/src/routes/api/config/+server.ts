import { env } from '$env/dynamic/private';
import { json, type RequestHandler } from '@sveltejs/kit';

const BACKEND_URL = env.BACKEND_URL ?? 'http://localhost:8080';

async function readBackendError(res: Response): Promise<string> {
	try {
		const ct = res.headers.get('content-type') ?? '';
		if (ct.includes('application/json')) {
			const err = await res.clone().json();
			return err?.error || err?.message || `Erro no backend (${res.status})`;
		}
	} catch {}
	const text = await res.text().catch(() => '');
	return text || `Erro no backend (${res.status})`;
}

export const GET: RequestHandler = async ({ url, fetch, request }) => {
	try {
		const cookie = request.headers.get('cookie');
		if (!cookie) return json({ error: 'Não autenticado' }, { status: 401 });
		const params = new URLSearchParams(url.searchParams);
		const sortParam = params.get('sort');
		if (sortParam) params.set('sort', sortParam.split(',')[0]);
		const qs = params.toString();
		const backendUrl = qs ? `${BACKEND_URL}/api/config?${qs}` : `${BACKEND_URL}/api/config`;
		const res = await fetch(backendUrl, { method: 'GET', headers: { cookie, 'content-type': 'application/json' } });
		if (!res.ok) return json({ error: await readBackendError(res) }, { status: res.status });
		return json(await res.json(), { status: 200 });
	} catch (error) {
		console.error('Erro no proxy GET configurações:', error);
		return json({ error: 'Erro interno do servidor' }, { status: 500 });
	}
};

export const POST: RequestHandler = async ({ request, fetch }) => {
	try {
		const cookie = request.headers.get('cookie');
		if (!cookie) return json({ error: 'Não autenticado' }, { status: 401 });
		const data: unknown = await request.json();
		const res = await fetch(`${BACKEND_URL}/api/config`, {
			method: 'POST',
			headers: { cookie, 'content-type': 'application/json' },
			body: JSON.stringify(data)
		});
		if (!res.ok) return json({ error: await readBackendError(res) }, { status: res.status });
		return json(await res.json(), { status: 201 });
	} catch (error) {
		console.error('Erro no proxy POST configurações:', error);
		return json({ error: 'Erro interno do servidor' }, { status: 500 });
	}
};
