import { BACKEND_URL } from '$env/static/private';
import { json, type RequestHandler } from '@sveltejs/kit';
import { proxyWithRefresh } from '$lib/server/proxyBackend';

export const GET: RequestHandler = async (event) => {
  try {
    const { email } = event.params;
    if (!email) return json({ error: 'Email ausente' }, { status: 400 });
    return await proxyWithRefresh({
      request: event.request,
      url: event.url,
      fetch: event.fetch,
      backendBase: BACKEND_URL,
      path: `/api/usuarios/email/${encodeURIComponent(email)}`,
      method: 'GET',
      mode: 'json'
    });
  } catch (error) {
    return json({ error: 'Erro interno' }, { status: 500 });
  }
};
