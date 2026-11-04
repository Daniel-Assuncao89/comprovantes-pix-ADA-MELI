package tech.ada.pix.emissor.dominio;

/**
 * Em que pé está o comprovante na SAGA de emissão (Aula 2).
 *
 *   ACEITO ──gravou──► GRAVADO
 *     │
 *     └──gravação falhou──► INVALIDADO   (ação compensatória)
 */
public enum StatusComprovante {
    ACEITO,
    GRAVADO,
    INVALIDADO
}
