export interface ConfigFormData {
  pretensaoSalarialMensal: number | ''
  horasSemanais: number | ''
  semanasMediaMes: number | ''
  custoFixoPct: number | ''
  margemLucroPadraoPct: number | ''
  ativo: boolean
}

export class FormValidation {
  pretensaoSalarialMensal = $state<number | ''>('')
  horasSemanais = $state<number | ''>('')
  semanasMediaMes = $state<number | ''>('4.33' as unknown as number) // default
  custoFixoPct = $state<number | ''>('')
  margemLucroPadraoPct = $state<number | ''>('')
  ativo = $state(true)

  submitted = $state(false)
  showErrors = $state<Record<string, boolean>>({
    pretensaoSalarialMensal: false,
    horasSemanais: false,
    semanasMediaMes: false,
    custoFixoPct: false,
    margemLucroPadraoPct: false
  })

  private timers: Record<string, any> = {}
  private debounceMs = 500

  get errors() {
    return {
      pretensaoSalarialMensal: this.pretensaoSalarialMensal === '' || Number(this.pretensaoSalarialMensal) <= 0 ? 'Informe valor mensal' : '',
      horasSemanais: this.horasSemanais === '' || Number(this.horasSemanais) <= 0 ? 'Informe horas semanais' : '',
      semanasMediaMes: this.semanasMediaMes === '' || Number(this.semanasMediaMes) <= 0 ? 'Informe semanas/mês' : '',
      custoFixoPct: this.custoFixoPct === '' || Number(this.custoFixoPct) < 0 ? 'Informe custo fixo (%)' : '',
      margemLucroPadraoPct: this.margemLucroPadraoPct === '' || Number(this.margemLucroPadraoPct) < 0 ? 'Informe margem (%)' : ''
    }
  }

  get isValid() { return Object.values(this.errors).every(e => !e) }

  populate(data: ConfigFormData) {
    this.pretensaoSalarialMensal = data.pretensaoSalarialMensal
    this.horasSemanais = data.horasSemanais
    this.semanasMediaMes = data.semanasMediaMes
    this.custoFixoPct = data.custoFixoPct
    this.margemLucroPadraoPct = data.margemLucroPadraoPct
    this.ativo = data.ativo
  }

  reset() {
    this.pretensaoSalarialMensal = ''
    this.horasSemanais = ''
    this.semanasMediaMes = '4.33' as unknown as number
    this.custoFixoPct = ''
    this.margemLucroPadraoPct = ''
    this.ativo = true
    this.submitted = false
    this.showErrors = {
      pretensaoSalarialMensal: false,
      horasSemanais: false,
      semanasMediaMes: false,
      custoFixoPct: false,
      margemLucroPadraoPct: false
    }
  }

  scheduleErrorDisplay(fieldName: keyof typeof this.showErrors) {
    clearTimeout(this.timers[fieldName])
    this.timers[fieldName] = setTimeout(() => {
      this.showErrors[fieldName] = !!this.errors[fieldName]
    }, this.debounceMs)
  }

  displayError(fieldName: keyof typeof this.showErrors) {
    this.showErrors[fieldName] = !!this.errors[fieldName]
  }

  validateAll() {
    this.submitted = true
    Object.keys(this.showErrors).forEach(key => {
      this.showErrors[key as keyof typeof this.showErrors] = !!this.errors[key as keyof typeof this.errors]
    })
    return this.isValid
  }

  shouldShowError(fieldName: keyof typeof this.errors) {
    return (this.submitted || this.showErrors[fieldName]) && this.errors[fieldName]
  }
}
