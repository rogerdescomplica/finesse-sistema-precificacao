import { BACKEND_URL } from '$env/static/private';
import { json, type RequestHandler } from '@sveltejs/kit';
import { proxyWithRefresh } from '$lib/server/proxyBackend';

export const POST: RequestHandler = async (event) => {
  try {
    const { id } = event.params;
    if (!id) return json({ error: 'ID ausente' }, { status: 400 });

    const body: unknown = await event.request.json();

    return await proxyWithRefresh({
      request: event.request,
      url: event.url,
      fetch: event.fetch,
      backendBase: BACKEND_URL,
      path: `/api/servico/${id}/precos`,
      method: 'POST',
      body,
      mode: 'json'
    });
  } catch (error) {
    console.error('Erro no proxy POST /api/servico/:id/precos:', error);
    return json({ error: 'Erro interno do servidor' }, { status: 500 });
  }
};
