import { BACKEND_URL } from '$env/static/private';
import { json, type RequestHandler } from '@sveltejs/kit';
import { proxyWithRefresh } from '$lib/server/proxyBackend';

const unitMap = {
	UN: 'UN',
	KG: 'KG',
	G: 'G',
	L: 'L',
	ML: 'ML',
	M: 'M',
	UI: 'UI'
} as const;

type UnitKey = keyof typeof unitMap;

type HasUnidadeMedida = {
	unidadeMedida: UnitKey;
};

export const GET: RequestHandler = async (event) => {
	const { id } = event.params;
	if (!id) return json({ error: 'ID ausente' }, { status: 400 });
	return proxyWithRefresh({
		request: event.request,
		url: event.url,
		fetch: event.fetch,
		backendBase: BACKEND_URL,
		path: `/api/material/${id}`,
		method: 'GET',
		mode: 'json'
	});
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

async function proxy(
	event: Parameters<RequestHandler>[0],
	method: 'PUT' | 'PATCH' | 'DELETE',
	opts?: { normalize?: boolean }
) {
	const { id } = event.params;
	if (!id) return json({ error: 'ID ausente' }, { status: 400 });

	if (method === 'DELETE') {
		return proxyWithRefresh({
			request: event.request,
			url: event.url,
			fetch: event.fetch,
			backendBase: BACKEND_URL,
			path: `/api/material/${id}`,
			method: 'DELETE',
			mode: 'json'
		});
	}

	const data: unknown = await event.request.json();
	if (opts?.normalize) normalizeUnit(data);

	return proxyWithRefresh({
		request: event.request,
		url: event.url,
		fetch: event.fetch,
		backendBase: BACKEND_URL,
		path: `/api/material/${id}`,
		method,
		body: data,
		mode: 'json'
	});
}

export const PUT: RequestHandler = (event) => proxy(event, 'PUT', { normalize: true });
export const PATCH: RequestHandler = (event) => proxy(event, 'PATCH'); // normalize: true se precisar
export const DELETE: RequestHandler = (event) => proxy(event, 'DELETE');
