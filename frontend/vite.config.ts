import tailwindcss from '@tailwindcss/vite';
import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';
import path from 'path';

export default defineConfig({
	plugins: [tailwindcss(), sveltekit()],
	server: {
		fs: {
			allow: [
				'.', // permite o diretório atual
				path.resolve('.yarn') // permite a pasta virtual do Yarn Berry
			]
		}
	}
});
