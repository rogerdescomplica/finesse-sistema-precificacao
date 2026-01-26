import type { RequestHandler } from './$types';
import { BACKEND_URL } from '$env/static/private';
import { json } from '@sveltejs/kit';

export const GET: RequestHandler = async ({ request, fetch }) => {
  const cookie = request.headers.get('cookie') ?? '';

  const res = await fetch(`${BACKEND_URL}/api/auth/check`, {
    method: 'GET',
    headers: { cookie }
  });

  const data = await res.json();

  return json(data, { status: res.status });
};
