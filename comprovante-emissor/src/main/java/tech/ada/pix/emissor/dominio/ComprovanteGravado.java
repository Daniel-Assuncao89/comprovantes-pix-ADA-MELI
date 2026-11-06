package tech.ada.pix.emissor.dominio;

import java.util.UUID;

/**
 * EVENTO de domínio (Aula 3): "o comprovante foi gravado". É um FATO, no passado.
 *
 * Quem publica não sabe quem escuta: podem ser zero, um ou dez interessados.
 * Se der para criar um interessado novo sem mexer em quem publica, é evento.
 *
 * Leva o valor junto ("transferência de estado por evento"). Assim o antifraude
 * não precisa perguntar de volta ao emissor quanto foi o PIX.
 *
 * Não confundir com o ComprovanteGravadoEvent do shared-contracts: aquele é o contrato
 * do tópico, na aula de Kafka. Este aqui não sai do processo do emissor.
 */
public record ComprovanteGravado(UUID idComprovante, Dinheiro valor) {
}
