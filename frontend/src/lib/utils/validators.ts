
/**
 * Valida email com regex básico (frontend)
 * IMPORTANTE: Backend DEVE ter validação própria!
 */
export function isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

/**
 * Valida senha (frontend)
 * IMPORTANTE: Backend DEVE ter validação própria!
 */
export function isValidPassword(senha: string): boolean {
    return senha.length >= 6;
}