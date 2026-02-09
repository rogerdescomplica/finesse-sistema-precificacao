import { BACKEND_URL } from '$env/static/private';
import { json, type RequestHandler } from '@sveltejs/kit';

async function readBackendError(res: Response): Promise<string> {
	try {
		const ct = res.headers.get('content-type') ?? '';
		if (ct.includes('application/json')) {
			const err = await res.clone().json();
			return err?.error || err?.message || `Erro no backend (${res.status})`;
		}
	} catch {
		// ignore
	}
	const text = await res.text().catch(() => '');
	return text || `Erro no backend (${res.status})`;
}

async function proxy(event: Parameters<RequestHandler>[0], method: 'PUT' | 'PATCH' | 'DELETE') {
	const cookie = event.request.headers.get('cookie');
	if (!cookie) return json({ error: 'Não autenticado' }, { status: 401 });

	const { id } = event.params;
	if (!id) return json({ error: 'ID ausente' }, { status: 400 });

	const headers: HeadersInit = { cookie };
	let body: string | undefined;

	if (method !== 'DELETE') {
		const data: unknown = await event.request.json();
		body = JSON.stringify(data);
		(headers as Record<string, string>)['content-type'] = 'application/json';
	}

	const res = await event.fetch(`${BACKEND_URL}/api/atividades/${id}`, { method, headers, body });

	if (!res.ok) return json({ error: await readBackendError(res) }, { status: res.status });

	if (method === 'DELETE') return new Response(null, { status: 204 });

	return json(await res.json(), { status: 200 });
}

export const PUT: RequestHandler = (event) => proxy(event, 'PUT');
export const PATCH: RequestHandler = (event) => proxy(event, 'PATCH');
export const DELETE: RequestHandler = (event) => proxy(event, 'DELETE');
