package tech.ada.pix.emissor.dominio;

import tech.ada.pix.emissor.ComprovanteInvalidoException;

/**
 * Value Object: a conta de origem do PIX.
 *
 * Não tem id, não muda depois de criado e se valida sozinho — a regra
 * "agência 4, conta 5, dígito 1" mora AQUI, não espalhada pelo service.
 * São Strings de propósito: zero à esquerda importa ("0341" ≠ "341").
 */
public record ContaBancaria(String agencia, String conta, String digito) {

    public ContaBancaria {
        exigirTamanho(agencia, 4, "numero_agencia");
        exigirTamanho(conta, 5, "numero_conta");
        exigirTamanho(digito, 1, "digito_verificador_conta");
    }

    private static void exigirTamanho(String valor, int tamanho, String campo) {
        if (valor == null || valor.length() != tamanho)
            throw new ComprovanteInvalidoException(campo + " deve ter " + tamanho + " caractere(s)");
    }
}
