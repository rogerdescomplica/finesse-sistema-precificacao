import { env } from '$env/dynamic/private';
import { json, type RequestHandler } from '@sveltejs/kit';

const BACKEND_URL = env.BACKEND_URL ?? 'http://localhost:8080';

const unitMap = {
	UN: 'UN',
	KG: 'KG',
	G: 'G',
	L: 'L',
	ML: 'ML',
	M: 'M'
} as const;

type UnitKey = keyof typeof unitMap;

type HasUnidadeMedida = {
	unidadeMedida: UnitKey;
};

function hasUnidadeMedida(value: unknown): value is HasUnidadeMedida {
	if (typeof value !== 'object' || value === null) return false;
	if (!('unidadeMedida' in value)) return false;

	const u = (value as { unidadeMedida?: unknown }).unidadeMedida;
	return typeof u === 'string' && u in unitMap;
}

function normalizeUnit(data: unknown): void {
	if (!hasUnidadeMedida(data)) return;
	data.unidadeMedida = unitMap[data.unidadeMedida];
}

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

async function proxy(
	event: Parameters<RequestHandler>[0],
	method: 'PUT' | 'PATCH' | 'DELETE',
	opts?: { normalize?: boolean }
) {
	const cookie = event.request.headers.get('cookie');
	if (!cookie) return json({ error: 'Não autenticado' }, { status: 401 });

	const { id } = event.params;
	if (!id) return json({ error: 'ID ausente' }, { status: 400 });

	const headers: HeadersInit = { cookie };
	let body: string | undefined;

	if (method !== 'DELETE') {
		const data: unknown = await event.request.json();
		if (opts?.normalize) normalizeUnit(data);
		body = JSON.stringify(data);
		(headers as Record<string, string>)['content-type'] = 'application/json';
	}

	const res = await event.fetch(`${BACKEND_URL}/api/material/${id}`, { method, headers, body });

	if (!res.ok) return json({ error: await readBackendError(res) }, { status: res.status });

	if (method === 'DELETE') return new Response(null, { status: 204 });

	return json(await res.json(), { status: 200 });
}

export const PUT: RequestHandler = (event) => proxy(event, 'PUT', { normalize: true });
export const PATCH: RequestHandler = (event) => proxy(event, 'PATCH'); // normalize: true se precisar
export const DELETE: RequestHandler = (event) => proxy(event, 'DELETE');
