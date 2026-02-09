import { BACKEND_URL } from '$env/static/private';
import { json, type RequestHandler } from '@sveltejs/kit';
import { proxyWithRefresh } from '$lib/server/proxyBackend';

export const GET: RequestHandler = async ({ url, fetch, request }) => {
  try {
    return await proxyWithRefresh({
      request,
      url,
      fetch,
      backendBase: BACKEND_URL,
      path: '/api/configuracoes',
      method: 'GET',
      mode: 'json',
      forwardQuery: true
    });
  } catch (error) {
    console.error('Erro no proxy GET configurações:', error);
    return json({ error: 'Erro interno do servidor' }, { status: 500 });
  }
};

export const POST: RequestHandler = async ({ url, fetch, request }) => {
  try {
    const data: unknown = await request.json();

    return await proxyWithRefresh({
      request,
      url,
      fetch,
      backendBase: BACKEND_URL,
      path: '/api/configuracoes',
      method: 'POST',
      body: data,
      mode: 'json'
    });
  } catch (error) {
    console.error('Erro no proxy POST configurações:', error);
    return json({ error: 'Erro interno do servidor' }, { status: 500 });
  }
};
