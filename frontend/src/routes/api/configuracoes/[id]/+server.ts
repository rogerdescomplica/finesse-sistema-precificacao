// src/routes/api/configuracoes/[id]/+server.ts
import { BACKEND_URL } from '$env/static/private';
import { json, type RequestHandler } from '@sveltejs/kit';
import { proxyWithRefresh } from '$lib/server/proxyBackend';

async function proxy(
  event: Parameters<RequestHandler>[0],
  method: 'PUT' | 'PATCH' | 'DELETE'
) {
  const { id } = event.params;
  if (!id) return json({ error: 'ID ausente' }, { status: 400 });

  if (method === 'DELETE') {
    return proxyWithRefresh({
      request: event.request,
      url: event.url,
      fetch: event.fetch,
      backendBase: BACKEND_URL,
      path: `/api/configuracoes/${id}`,
      method: 'DELETE',
      mode: 'json'
    });
  }

  const data: unknown = await event.request.json();

  return proxyWithRefresh({
    request: event.request,
    url: event.url,
    fetch: event.fetch,
    backendBase: BACKEND_URL,
    path: `/api/configuracoes/${id}`,
    method,
    body: data,
    mode: 'json'
  });
}

export const PUT: RequestHandler = (event) => proxy(event, 'PUT');
export const PATCH: RequestHandler = (event) => proxy(event, 'PATCH');
export const DELETE: RequestHandler = (event) => proxy(event, 'DELETE');
