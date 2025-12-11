import type { Handle } from '@sveltejs/kit';
import { randomUUID } from 'crypto';
import { dev } from '$app/environment';

export const handle: Handle = async ({ event, resolve }) => {
	const access = event.cookies.get('access_token');
	event.locals.user = access ? { token: access } : null;

	let csrf = event.cookies.get('csrf_token');
	if (!csrf) {
		csrf = randomUUID();
		event.cookies.set('csrf_token', csrf, {
			path: '/',
			httpOnly: true,
			sameSite: 'lax',
			secure: !dev
		});
	}

	return resolve(event);
};
