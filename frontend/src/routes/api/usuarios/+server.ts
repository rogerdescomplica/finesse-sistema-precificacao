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
      path: '/api/usuarios',
      method: 'GET',
      mode: 'json'
    });
  } catch (error) {
    return json({ error: 'Erro interno' }, { status: 500 });
  }
};

export const POST: RequestHandler = async ({ url, fetch, request }) => {
  try {
    const body = await request.json();
    return await proxyWithRefresh({
      request,
      url,
      fetch,
      backendBase: BACKEND_URL,
      path: '/api/usuarios',
      method: 'POST',
      body,
      mode: 'json'
    });
  } catch (error) {
    return json({ error: 'Erro interno' }, { status: 500 });
  }
};
