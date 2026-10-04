package br.com.controlei.application.contracts;

import br.com.controlei.shared.events.DomainEvent;

/**
 * Porta para publicar eventos de dominio. A camada de aplicacao so conhece este contrato: nao sabe se o evento vai
 * para Kafka, para uma fila ou para um log. A implementacao (infrastructure.outbox) grava o evento na MESMA transacao
 * do dado de negocio, e um processo a parte o entrega ao broker.
 *
 * <p>Isso resolve o "dual write": publicar direto no Kafka dentro de uma transacao pode emitir um evento de algo que
 * depois sofreu rollback, ou gravar o dado e perder o evento se o broker estiver fora. Com o outbox, dado e evento
 * sao confirmados juntos ou nenhum dos dois.
 */
public interface DomainEventPublisher {

    /**
     * Deve ser chamado dentro de uma transacao ativa; fora dela falha, de proposito.
     *
     * @param topic nome do topico de destino
     * @param event o evento; a familia vira a chave de particao, para ordenar os eventos da mesma familia
     */
    void publish(String topic, DomainEvent event);
}
