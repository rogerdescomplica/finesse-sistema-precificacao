import type { Configuracao, ConfigInput } from '$lib/services/config.service'
import { configService } from '$lib/services/config.service'
import { SvelteURLSearchParams } from 'svelte/reactivity'

type SortBy = keyof Configuracao
type StatusFilter = '' | 'ativo' | 'inativo'

export class ConfigState {
  items = $state<Configuracao[]>([])
  loading = $state(false)
  error = $state<string>('')

  search = $state('')
  searchStatus = $state<StatusFilter>('')

  page = $state(1)
  perPage = $state(10)
  totalPages = $state(1)
  totalItems = $state(0)

  sortBy = $state<SortBy>('id')
  sortAsc = $state(true)

  private currentLoadController: AbortController | null = null

  private buildQueryParams() {
    const params = new SvelteURLSearchParams()
    params.set('page', String(Math.max(0, this.page - 1)))
    params.set('size', String(this.perPage))
    params.set('sort', String(this.sortBy))
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
      const page = await configService.list(params, controller.signal)
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

  resetPage() { this.page = 1 }
  setSortBy(column: SortBy) {
    if (this.sortBy === column) this.sortAsc = !this.sortAsc
    else { this.sortBy = column; this.sortAsc = true }
  }

  async create(data: ConfigInput) {
    this.loading = true
    this.error = ''
    try {
      await configService.create(data)
      await this.load()
    } catch (err) {
      this.error = err instanceof Error ? err.message : 'Erro inesperado'
      throw err
    } finally { this.loading = false }
  }

  async update(id: number, data: ConfigInput) {
    this.loading = true
    this.error = ''
    try {
      await configService.update(id, data)
      await this.load()
    } catch (err) {
      this.error = err instanceof Error ? err.message : 'Erro inesperado'
      throw err
    } finally { this.loading = false }
  }

  async toggleStatus(id: number, ativo: boolean) {
    this.loading = true
    this.error = ''
    try {
      await configService.toggleStatus(id, ativo)
      await this.load()
    } catch (err) {
      this.error = err instanceof Error ? err.message : 'Erro inesperado'
      throw err
    } finally { this.loading = false }
  }

  async delete(id: number) {
    this.loading = true
    this.error = ''
    try {
      await configService.remove(id)
      if (this.items.length === 1 && this.page > 1) this.page -= 1
      await this.load()
    } catch (err) {
      this.error = err instanceof Error ? err.message : 'Erro inesperado'
      throw err
    } finally { this.loading = false }
  }
}

export const configState = new ConfigState()
