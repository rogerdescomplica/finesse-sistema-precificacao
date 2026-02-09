// src/routes/api/atividades/+server.ts
import { BACKEND_URL } from '$env/static/private';
import { json, type RequestHandler } from '@sveltejs/kit';
import { proxyWithRefresh } from '$lib/server/proxyBackend';

// GET - Listar atividades (paginação/filtros/sort)
export const GET: RequestHandler = async ({ url, fetch, request }) => {
  try {
    return await proxyWithRefresh({
      request,
      url,
      fetch,
      backendBase: BACKEND_URL,
      path: '/api/atividades',
      method: 'GET',
      mode: 'json',
      forwardQuery: true // mantém sort no formato "campo,asc|desc" vindo do frontend
    });
  } catch (error) {
    console.error('Erro no proxy GET atividades:', error);
    return json({ error: 'Erro interno do servidor' }, { status: 500 });
  }
};

// POST - Criar atividade
export const POST: RequestHandler = async ({ url, fetch, request }) => {
  try {
    const data: unknown = await request.json();

    return await proxyWithRefresh({
      request,
      url,
      fetch,
      backendBase: BACKEND_URL,
      path: '/api/atividades',
      method: 'POST',
      body: data,
      mode: 'json'
    });
  } catch (error) {
    console.error('Erro no proxy POST atividades:', error);
    return json({ error: 'Erro interno do servidor' }, { status: 500 });
  }
};
