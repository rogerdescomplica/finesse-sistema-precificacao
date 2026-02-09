import { json } from '@sveltejs/kit';

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE' | 'HEAD';
type Mode = 'json' | 'passthrough';

type ProxyArgs = {
  request: Request;
  url: URL;
  fetch: typeof globalThis.fetch;

  backendBase: string;  // BACKEND_URL
  path: string;         // ex: '/api/material'
  method?: HttpMethod;
  body?: unknown;

  forwardQuery?: boolean;
  extraHeaders?: Record<string, string>;

  // 'json' para CRUD; 'passthrough' para arquivos/stream
  mode?: Mode;
};

function ct(res: Response) {
  return (res.headers.get('content-type') ?? '').toLowerCase();
}

function isJson(res: Response) {
  return ct(res).includes('application/json');
}

async function readBackendError(res: Response): Promise<string> {
  const contentType = ct(res);
  if (contentType.includes('application/json')) {
    try {
      const data = await res.json();
      if (typeof data?.error === 'string') return data.error;
      if (typeof data?.message === 'string') return data.message;
      return JSON.stringify(data);
    } catch {}
  }
  return await res.text().catch(() => 'Erro no backend');
}

function getSetCookies(res: Response): string[] {
  const anyHeaders = res.headers as any;

  if (typeof anyHeaders.getSetCookie === 'function') {
    const arr = anyHeaders.getSetCookie();
    return Array.isArray(arr) ? arr.filter(Boolean) : [];
  }

  const sc = res.headers.get('set-cookie');
  return sc ? [sc] : [];
}



function parseCookieHeader(cookieHeader: string | null | undefined): Map<string, string> {
  const map = new Map<string, string>();
  if (!cookieHeader) return map;

  cookieHeader.split(';').forEach((chunk) => {
    const part = chunk.trim();
    if (!part) return;
    const eq = part.indexOf('=');
    if (eq <= 0) return;
    const name = part.slice(0, eq).trim();
    const value = part.slice(eq + 1).trim();
    if (name) map.set(name, value);
  });

  return map;
}

function mergeSetCookieIntoCookieHeader(currentCookie: string, setCookies: string[]): string {
  const cookieMap = parseCookieHeader(currentCookie);

  for (const sc of setCookies) {
    const first = sc.split(';', 1)[0]?.trim(); // name=value
    if (!first) continue;
    const eq = first.indexOf('=');
    if (eq <= 0) continue;

    const name = first.slice(0, eq).trim();
    const value = first.slice(eq + 1).trim();
    if (name) cookieMap.set(name, value);
  }

  return Array.from(cookieMap.entries())
    .map(([k, v]) => `${k}=${v}`)
    .join('; ');
}

export async function proxyWithRefresh({
  request,
  url,
  fetch,
  backendBase,
  path,
  method = 'GET',
  body,
  forwardQuery = true,
  extraHeaders = {},
  mode = 'json'
}: ProxyArgs) {
  let cookie = request.headers.get('cookie'); // pode ser null

  const qs = forwardQuery ? url.searchParams.toString() : '';
  const backendUrl = qs ? `${backendBase}${path}?${qs}` : `${backendBase}${path}`;

  const hasBody =
    body !== undefined &&
    body !== null &&
    method !== 'GET' &&
    method !== 'HEAD' &&
    method !== 'DELETE';

  // Em modo passthrough, não força accept
  const buildHeaders = (): Record<string, string> => ({
    ...(cookie ? { cookie } : {}),
    ...(mode === 'json' ? { accept: 'application/json' } : {}),
    ...extraHeaders,
    ...(hasBody ? { 'content-type': 'application/json' } : {})
  });

  const buildOptions = (): RequestInit => ({
    method,
    headers: buildHeaders(),
    ...(hasBody ? { body: typeof body === 'string' ? body : JSON.stringify(body) } : {})
  });

  let refreshSetCookies: string[] = [];

  for (let attempt = 0; attempt < 2; attempt++) {
    const res = await fetch(backendUrl, buildOptions());

    if (res.status !== 401) {
      if (!res.ok) {
        // erro: no modo passthrough, pode repassar cru também, mas normalmente é melhor JSON padronizado
        return json({ error: await readBackendError(res) }, { status: res.status });
      }

      // sucesso: passthrough (arquivos)
      if (mode === 'passthrough') {
        const out = new Response(res.body, {
          status: res.status,
          headers: new Headers(res.headers)
        });
        for (const sc of refreshSetCookies) out.headers.append('set-cookie', sc);
        return out;
      }

      // sucesso: json (CRUD)
      if (res.status === 204) {
        const out = new Response(null, { status: 204 });
        for (const sc of refreshSetCookies) out.headers.append('set-cookie', sc);
        return out;
      }

      if (isJson(res)) {
        const data = await res.json();
        const out = json(data, { status: res.status }); // repassa status real do backend
        for (const sc of refreshSetCookies) out.headers.append('set-cookie', sc);
        return out;
      }

      const raw = await res.text().catch(() => '');
      return json({ error: raw || 'Resposta inválida do backend' }, { status: 502 });
    }

    // 401 → tenta refresh (precisa cookie)
    if (!cookie) {
      return json({ error: 'Não autenticado' }, { status: 401 });
    }

    const refresh = await fetch(`${backendBase}/api/auth/refresh`, {
      method: 'POST',
      headers: { cookie }
    });

    if (!refresh.ok) {
      return json({ error: await readBackendError(refresh) }, { status: refresh.status || 401 });
    }

    //const setCookies = getSetCookies(refresh);
    //refreshSetCookies = setCookies;

    //if (typeof (refresh.headers as any).getSetCookie === 'function' && setCookies.length > 0) {
    //     cookie = mergeSetCookieIntoCookieHeader(cookie, setCookies);
    //}
    refreshSetCookies = getSetCookies(refresh);

    if (refreshSetCookies.length > 0) {
      cookie = mergeSetCookieIntoCookieHeader(cookie ?? "", refreshSetCookies);
    }

    
  }

  return json({ error: 'Falha inesperada no proxy' }, { status: 500 });
}
