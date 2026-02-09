import type { RequestHandler } from '@sveltejs/kit';
import { json } from '@sveltejs/kit';
import { BACKEND_URL } from '$env/static/private';
import { proxyWithRefresh } from '$lib/server/proxyBackend';

export const GET: RequestHandler = async ({ url, fetch, request }) => {
  try {
    return await proxyWithRefresh({
      request,
      url,
      fetch,
      backendBase: BACKEND_URL,
      path: '/api/servico/precos',
      method: 'GET',
      mode: 'json'
    });
  } catch (error) {
    console.error('Erro no proxy GET preços:', error);
    return json({ error: 'Erro interno do servidor' }, { status: 500 });
  }
};
