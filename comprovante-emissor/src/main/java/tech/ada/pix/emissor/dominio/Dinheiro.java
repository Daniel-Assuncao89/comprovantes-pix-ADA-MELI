package tech.ada.pix.emissor.dominio;

import tech.ada.pix.emissor.ComprovanteInvalidoException;

import java.math.BigDecimal;

/**
 * Value Object: o valor da transação.
 *
 * Pegadinha do BigDecimal: para o Java, 10.0 e 10.00 são DIFERENTES no equals.
 * Como um VO é igual por valor, padronizamos sempre em 2 casas decimais.
 */
public record Dinheiro(BigDecimal valor) {

    public Dinheiro {
        if (valor == null || valor.signum() <= 0)
            throw new ComprovanteInvalidoException("valor_transacao deve ser maior que zero");
        if (valor.scale() > 2)
            throw new ComprovanteInvalidoException("valor_transacao deve ter no máximo 2 casas decimais");
        valor = valor.setScale(2); // assim 10.0 e 10.00 viram o mesmo Dinheiro
    }
}
