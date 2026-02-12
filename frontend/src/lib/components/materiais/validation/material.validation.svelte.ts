import type { UnidadeMedida } from '$lib/services/material.service'
import { FormValidationBase } from '$lib/utils/form-validation.base.svelte'

export interface MaterialFormData {
	produto: string
	unidade: UnidadeMedida | ''
	volume: number | ''
	preco: number | '' // pode vir como '' no populate, mas internamente você usa number
	observacoes: string
	ativo: boolean
}

type ValidatedField = Exclude<keyof MaterialFormData, 'observacoes' | 'ativo'>

const validatedFields = ['produto', 'unidade', 'volume', 'preco'] as const satisfies readonly ValidatedField[]

export class FormValidation extends FormValidationBase<MaterialFormData, ValidatedField> {
	produto = $state('')
	unidade = $state<UnidadeMedida | ''>('')
	volume = $state<number | ''>('')
	preco = $state<number>(0)
	observacoes = $state('')
	ativo = $state(true)

	constructor() {
		super(
			validatedFields,
			() => ({
				produto: !this.produto.trim() ? 'Produto é obrigatório' : '',
				unidade: !this.unidade ? 'Unidade de Medida é obrigatória' : '',
				volume: this.volume === '' || Number(this.volume) <= 0 ? 'Volume deve ser positivo' : '',
				preco: Number(this.preco) <= 0 ? 'Preço deve ser positivo' : ''
			}),
			500
		)
	}

	get custoUnitario() {
		if (this.preco && this.volume) {
			return Number((Number(this.preco) / Number(this.volume)).toFixed(2))
		}
		return 0
	}

	get custoUnitarioFormatado() {
		return this.custoUnitario.toFixed(2).replace('.', ',')
	}

	populate(data: MaterialFormData) {
		this.produto = data.produto
		this.unidade = data.unidade
		this.volume = data.volume
		this.preco = Number(data.preco) || 0
		this.observacoes = data.observacoes
		this.ativo = data.ativo

		// opcional: se quiser limpar erros ao carregar dados
		// this.resetValidationState()
	}

	reset() {
		this.produto = ''
		this.unidade = ''
		this.volume = ''
		this.preco = 0
		this.observacoes = ''
		this.ativo = true

		this.resetValidationState()
	}
}