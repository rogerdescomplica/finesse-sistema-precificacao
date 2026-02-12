type TimerId = ReturnType<typeof setTimeout>

/**
 * TForm: interface do form (ex: ConfigFormData)
 * TValidated: union dos campos validados (ex: Exclude<keyof TForm, 'ativo'>)
 */
export class FormValidationBase<TForm extends Record<string, any>, TValidated extends keyof TForm> {
  submitted = $state(false)

  showErrors = $state<Record<TValidated, boolean>>({} as Record<TValidated, boolean>)

  private timers: Partial<Record<TValidated, TimerId>> = {}
  private debounceMs: number

  constructor(
    private validatedFields: readonly TValidated[],
    private getErrorsFn: () => Record<TValidated, string>,
    debounceMs = 500
  ) {
    this.debounceMs = debounceMs

    // inicializa showErrors = false pra todos os campos validados
    const init = {} as Record<TValidated, boolean>
    for (const f of validatedFields) init[f] = false
    this.showErrors = init
  }

  get errors(): Record<TValidated, string> {
    return this.getErrorsFn()
  }

  get isValid() {
    return Object.values(this.errors).every((e) => !e)
  }

  scheduleErrorDisplay(fieldName: TValidated) {
    const t = this.timers[fieldName]
    if (t) clearTimeout(t)

    this.timers[fieldName] = setTimeout(() => {
      this.showErrors[fieldName] = !!this.errors[fieldName]
    }, this.debounceMs)
  }

  displayError(fieldName: TValidated) {
    this.showErrors[fieldName] = !!this.errors[fieldName]
  }

  validateAll() {
    this.submitted = true
    for (const key of this.validatedFields) {
      this.showErrors[key] = !!this.errors[key]
    }
    return this.isValid
  }

  shouldShowError(fieldName: TValidated) {
    return (this.submitted || this.showErrors[fieldName]) && this.errors[fieldName]
  }

  /** Útil se você quiser “limpar” a UI de erros quando repopular */
  resetValidationState() {
    this.submitted = false
    for (const f of this.validatedFields) this.showErrors[f] = false
  }
}