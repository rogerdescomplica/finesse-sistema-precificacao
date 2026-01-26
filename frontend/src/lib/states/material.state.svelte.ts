import type { Material, MaterialInput } from '$lib/services/material.service'
import { materialService } from '$lib/services/material.service'
import { SvelteURLSearchParams } from 'svelte/reactivity'

type SortBy = keyof Material
type StatusFilter = '' | 'ativo' | 'inativo'

export class MaterialState {
  items = $state<Material[]>([])
  loading = $state(false)
  error = $state<string>('')

  search = $state('')
  searchStatus = $state<StatusFilter>('')

  page = $state(1) // 1-based
  perPage = $state(10)
  totalPages = $state(1)
  totalItems = $state(0)

  sortBy = $state<SortBy>('id')
  sortAsc = $state(true)

  private currentLoadController: AbortController | null = null

  resetPage() { this.page = 1; }

  setSortBy(column: SortBy) { 
	if (this.sortBy === column) { 
		this.sortAsc = !this.sortAsc; 
	} else {
		 this.sortBy = column; this.sortAsc = true; }
   }

  private buildQueryParams() {
    const params = new SvelteURLSearchParams()
    params.set('page', String(Math.max(0, this.page - 1)))
    params.set('size', String(this.perPage))

    // Se seu backend suporta direção, melhor mandar "sort=campo,asc|desc"
    // Hoje seu proxy corta tudo depois da vírgula, então deixe só "campo" OU ajuste o proxy.
    params.set('sort', String(this.sortBy))

    const q = this.search.trim()
    if (q) params.set('produto', q)

    if (this.searchStatus === 'ativo') params.set('ativo', 'true')
    if (this.searchStatus === 'inativo') params.set('ativo', 'false')

    return params
  }

  async load() {
    this.currentLoadController?.abort()
    const controller = new AbortController()
    this.currentLoadController = controller

    this.loading = true
    this.error = ''

    try {
      const params = this.buildQueryParams()
      const page = await materialService.list(params, controller.signal)

      this.items = Array.isArray(page.content) ? page.content : []
      this.totalPages = Number.isFinite(page.totalPages) && page.totalPages > 0 ? page.totalPages : 1
      this.totalItems = Number.isFinite(page.totalElements) ? page.totalElements : 0
    } catch (err) {
      if (err instanceof DOMException && err.name === 'AbortError') return
      this.error = err instanceof Error ? err.message : 'Erro inesperado'
    } finally {
      if (this.currentLoadController === controller) this.loading = false
    }
  }

  async create(data: MaterialInput) {
    this.loading = true
    this.error = ''
    try {
      await materialService.create(data)
      await this.load()
    } catch (err) {
      this.error = err instanceof Error ? err.message : 'Erro inesperado'
      throw err
    } finally {
      this.loading = false
    }
  }

  async update(id: number, data: MaterialInput) {
    this.loading = true
    this.error = ''
    try {
      await materialService.update(id, data)
      await this.load()
    } catch (err) {
      this.error = err instanceof Error ? err.message : 'Erro inesperado'
      throw err
    } finally {
      this.loading = false
    }
  }

  async toggleStatus(id: number, ativo: boolean) {
    this.loading = true
    this.error = ''
    try {
      await materialService.toggleStatus(id, ativo)
      await this.load()
    } catch (err) {
      this.error = err instanceof Error ? err.message : 'Erro inesperado'
      throw err
    } finally {
      this.loading = false
    }
  }

  async delete(id: number) {
    this.loading = true
    this.error = ''
    try {
      await materialService.remove(id)

      if (this.items.length === 1 && this.page > 1) this.page -= 1
      await this.load()
    } catch (err) {
      this.error = err instanceof Error ? err.message : 'Erro inesperado'
      throw err
    } finally {
      this.loading = false
    }
  }
}

export const materialState = new MaterialState()
