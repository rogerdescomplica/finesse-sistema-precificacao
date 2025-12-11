<script lang="ts">
	import { goto } from '$app/navigation';
	import { onMount } from 'svelte';
	function hasToken() {
		return document.cookie.split('; ').find((row) => row.startsWith('access_token='));
	}
	onMount(() => {
		const authed = !!hasToken();
		if (authed) goto('/dashboard', { replaceState: true });
		else goto('/login', { replaceState: true });
	});
</script>
