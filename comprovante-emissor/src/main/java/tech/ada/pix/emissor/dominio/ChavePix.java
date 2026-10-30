package tech.ada.pix.emissor.dominio;

import tech.ada.pix.contracts.TipoChavePix;
import tech.ada.pix.emissor.ComprovanteInvalidoException;

/**
 * Value Object: a chave PIX de destino (tipo + valor).
 *
 * "Dependendo do tipo, o conteúdo da chave muda" (Sessão 2 dos requisitos).
 * As regras por tipo aqui são simples, só para mostrar ONDE essa regra deve morar.
 *
 * (Na aula, este VO é o desafio dos alunos — está aqui como resposta.)
 */
public record ChavePix(TipoChavePix tipo, String valor) {

    public ChavePix {
        if (tipo == null)
            throw new ComprovanteInvalidoException("tipo_chave_pix_destino é obrigatório");
        if (valor == null || valor.isBlank())
            throw new ComprovanteInvalidoException("chave_pix_destino é obrigatória");

        boolean valida = switch (tipo) {
            case CPF -> valor.matches("\\d{11}");
            case CNPJ -> valor.matches("\\d{14}");
            case EMAIL -> valor.contains("@");
            case CELULAR, ALEATORIA -> true;
        };
        if (!valida)
            throw new ComprovanteInvalidoException("chave_pix_destino inválida para o tipo " + tipo);
    }
}
