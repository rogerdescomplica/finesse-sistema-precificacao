<script lang="ts">
	interface Props {
		size?: 'sm' | 'md' | 'lg';
		lightSrc?: string;
		darkSrc?: string;
		alt?: string;
	}

	let {
		size = 'md',
		lightSrc = 'https://tailwindcss.com/plus-assets/img/logos/mark.svg?color=indigo&shade=600',
		darkSrc = 'https://tailwindcss.com/plus-assets/img/logos/mark.svg?color=indigo&shade=500',
		alt = 'Your Company'
	}: Props = $props();

	const sizeClasses = {
		sm: 'h-6 w-auto',
		md: 'h-8 w-auto',
		lg: 'h-10 w-auto'
	};
</script>

<img
	class={[sizeClasses[size], 'dark:hidden']}
	src={lightSrc}
	{alt}
/>
<img
	class={[sizeClasses[size], 'hidden dark:block']}
	src={darkSrc}
	{alt}
/>