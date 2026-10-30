package tech.ada.pix.emissor.dominio;

import tech.ada.pix.contracts.TipoChavePix;
import tech.ada.pix.emissor.ComprovanteInvalidoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Os conceitos da Aula 1 em forma de teste: VO é igual por valor, entidade é igual por id. */
class DominioTest {

    private final ContaBancaria conta = new ContaBancaria("2022", "00276", "0");
    private final Dinheiro valor = new Dinheiro(new BigDecimal("23.99"));
    private final ChavePix chave = new ChavePix(TipoChavePix.CELULAR, "11948755536");

    @Test
    void valueObjectsComOsMesmosValoresSaoIguais() {
        assertThat(new ContaBancaria("2022", "00276", "0")).isEqualTo(conta);
    }

    @Test
    void contaComTamanhoErradoEhRejeitada() {
        assertThatThrownBy(() -> new ContaBancaria("2022", "002761", "0"))
                .isInstanceOf(ComprovanteInvalidoException.class)
                .hasMessageContaining("numero_conta");
    }

    @Test
    void dinheiroIgnoraDiferencaDeCasasDecimais() {
        // sem o setScale(2) no Dinheiro, este teste falharia: 10.0 ≠ 10.00 no BigDecimal
        assertThat(new Dinheiro(new BigDecimal("10.0"))).isEqualTo(new Dinheiro(new BigDecimal("10.00")));
    }

    @Test
    void dinheiroZeroOuNegativoEhRejeitado() {
        assertThatThrownBy(() -> new Dinheiro(BigDecimal.ZERO)).isInstanceOf(ComprovanteInvalidoException.class);
        assertThatThrownBy(() -> new Dinheiro(new BigDecimal("-1"))).isInstanceOf(ComprovanteInvalidoException.class);
    }

    @Test
    void chavePixValidaConformeOTipo() {
        assertThatThrownBy(() -> new ChavePix(TipoChavePix.CPF, "123")).isInstanceOf(ComprovanteInvalidoException.class);
        assertThatThrownBy(() -> new ChavePix(TipoChavePix.EMAIL, "sem-arroba")).isInstanceOf(ComprovanteInvalidoException.class);
        assertThat(new ChavePix(TipoChavePix.CPF, "50329291076").valor()).isEqualTo("50329291076");
    }

    @Test
    void comprovantesComOsMesmosDadosSaoDiferentesPorqueOIdEhDiferente() {
        Comprovante primeiro = Comprovante.emitir("Giovanni", conta, valor, chave);
        Comprovante segundo = Comprovante.emitir("Giovanni", conta, valor, chave);

        assertThat(primeiro).isNotEqualTo(segundo);
        assertThat(primeiro).isEqualTo(primeiro);
    }
}
