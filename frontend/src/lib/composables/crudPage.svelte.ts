export type CrudStateAdapter<T extends { id: number; ativo: boolean }, F, S> = {
	items: T[]
	error: string
	loadingList?: boolean
	mutating?: boolean
	search: string
	searchStatus: '' | 'ativo' | 'inativo'
	page: number
	perPage: number
	totalPages: number
	totalItems: number
	sortBy: S
	sortAsc: boolean
	resetPage: () => void
	setPage: (page: number) => void
	setSortBy: (column: S) => void
	requestLoad: (delayMs?: number) => void
	load: (opts?: { silent?: boolean }) => Promise<void>
	create: (data: F) => Promise<void>
	update: (id: number, data: F) => Promise<void>
	toggleStatus: (id: number, ativo: boolean) => Promise<void>
	delete: (id: number) => Promise<void>
}

export function useCrudPage<T extends { id: number; ativo: boolean }, F, S>(
	state: CrudStateAdapter<T, F, S>,
	opts?: { isAuthenticated?: () => boolean }
) {
	let formOpen = $state(false)
	let editingItem = $state<T | null>(null)
	let confirmDeleteItem = $state<T | null>(null)

	$effect(() => {
		if (opts?.isAuthenticated && !opts.isAuthenticated()) return
		const search = state.search
		const status = state.searchStatus
		void search
		void status
		state.resetPage()
		state.requestLoad(250)
	})

	$effect(() => {
		if (opts?.isAuthenticated && !opts.isAuthenticated()) return
		const page = state.page
		const perPage = state.perPage
		const sortBy = state.sortBy
		const sortAsc = state.sortAsc
		void page
		void perPage
		void sortBy
		void sortAsc
		state.requestLoad(0)
	})

	function openCreateForm() {
		editingItem = null
		formOpen = true
	}

	function openEditForm(item: T) {
		editingItem = item
		formOpen = true
	}

	function closeForm() {
		formOpen = false
		editingItem = null
	}

	async function submit(data: F) {
		if (editingItem) await state.update(editingItem.id, data)
		else await state.create(data)
		closeForm()
	}

	async function toggleStatus(item: T) {
		await state.toggleStatus(item.id, !item.ativo)
	}

	function openDeleteDialog(item: T) {
		confirmDeleteItem = item
	}

	async function confirmDelete(item: T) {
		await state.delete(item.id)
		confirmDeleteItem = null
	}

	function cancelDelete() {
		confirmDeleteItem = null
	}

	function onSort(column: S) {
		state.setSortBy(column)
	}

	function onPageChange(page: number) {
		state.setPage(page)
	}

	return {
		get formOpen() { return formOpen },
		get editingItem() { return editingItem },
		get confirmDeleteItem() { return confirmDeleteItem },
		openCreateForm,
		openEditForm,
		closeForm,
		submit,
		toggleStatus,
		openDeleteDialog,
		confirmDelete,
		cancelDelete,
		onSort,
		onPageChange
	}
}
