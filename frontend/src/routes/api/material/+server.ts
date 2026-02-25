// src/routes/api/material/+server.ts
import type { RequestHandler } from '@sveltejs/kit';
import { json } from '@sveltejs/kit';
import { BACKEND_URL } from '$env/static/private';
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

function normalizeUnitToBackend(data: unknown): void {
  if (typeof data !== 'object' || data === null) return;
  if (!('unidadeMedida' in data)) return;

  const u = (data as { unidadeMedida?: unknown }).unidadeMedida;
  if (typeof u === 'string' && (u as string) in unitMap) {
    (data as { unidadeMedida?: UnitKey }).unidadeMedida = unitMap[u as UnitKey];
  }
}

// GET - Listar materiais (paginação/filtros)
export const GET: RequestHandler = async ({ url, fetch, request }) => {
  try {
    return await proxyWithRefresh({
      request,
      url,
      fetch,
      backendBase: BACKEND_URL,
      path: '/api/material',
      method: 'GET',
      mode: 'json',        // aqui é CRUD
      forwardQuery: true   // repassa ?page=...&size=... etc
    });
  } catch (error) {
    console.error('Erro no proxy GET materiais:', error);
    return json({ error: 'Erro interno do servidor' }, { status: 500 });
  }
};

// POST - Criar material
export const POST: RequestHandler = async ({ url, fetch, request }) => {
  try {
    const data: unknown = await request.json();
    normalizeUnitToBackend(data);

    return await proxyWithRefresh({
      request,
      url,
      fetch,
      backendBase: BACKEND_URL,
      path: '/api/material',
      method: 'POST',
      body: data,
      mode: 'json'
    });
  } catch (error) {
    console.error('Erro no proxy POST materiais:', error);
    return json({ error: 'Erro interno do servidor' }, { status: 500 });
  }
};
