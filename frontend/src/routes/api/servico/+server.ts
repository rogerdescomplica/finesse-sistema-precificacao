import type { RequestHandler } from '@sveltejs/kit';
import { json } from '@sveltejs/kit';
import { BACKEND_URL } from '$env/static/private';
import { proxyWithRefresh } from '$lib/server/proxyBackend';

// GET - Listar serviços (paginação/filtros)
export const GET: RequestHandler = async ({ url, fetch, request }) => {
  try {
    return await proxyWithRefresh({
      request,
      url,
      fetch,
      backendBase: BACKEND_URL,
      path: '/api/servico',
      method: 'GET',
      mode: 'json',
      forwardQuery: true
    });
  } catch (error) {
    console.error('Erro no proxy GET serviços:', error);
    return json({ error: 'Erro interno do servidor' }, { status: 500 });
  }
};

// POST - Criar serviço
export const POST: RequestHandler = async ({ url, fetch, request }) => {
  try {
    const data: unknown = await request.json();
    return await proxyWithRefresh({
      request,
      url,
      fetch,
      backendBase: BACKEND_URL,
      path: '/api/servico',
      method: 'POST',
      body: data,
      mode: 'json'
    });
  } catch (error) {
    console.error('Erro no proxy POST serviços:', error);
    return json({ error: 'Erro interno do servidor' }, { status: 500 });
  }
};
