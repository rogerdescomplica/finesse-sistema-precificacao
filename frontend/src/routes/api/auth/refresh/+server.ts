import type { RequestHandler } from './$types';
import { BACKEND_URL } from '$env/static/private';
import { json } from '@sveltejs/kit';

export const POST: RequestHandler = async ({ request, fetch }) => {
  try {
    // repassa TODOS os cookies (inclui refresh_token com Path restrito)
    const cookie = request.headers.get('cookie') ?? '';

    const response = await fetch(`${BACKEND_URL}/api/auth/refresh`, {
      method: 'POST',
      headers: { cookie }
    });

    // se falhou, repassa status/erro
    if (!response.ok) {
      const err = await response.json().catch(() => ({}));
      return json({ error: err.error ?? 'Sessão expirada' }, { status: response.status });
    }

    // sucesso: repassa body + Set-Cookie(s) do backend
    const body = await response.json();

    const setCookies = response.headers.getSetCookie?.() ?? [];
    const headers = new Headers({ 'Content-Type': 'application/json' });

    for (const c of setCookies) headers.append('Set-Cookie', c);

    return new Response(JSON.stringify(body), {
      status: response.status,
      headers
    });
  } catch (error) {
    console.error('Erro no proxy de refresh:', error);
    return json({ error: 'Erro ao renovar sessão' }, { status: 500 });
  }
};
