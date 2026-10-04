package br.com.controlei.application.services.assistant;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Base de conhecimento do assistente: o que o app faz e onde fica cada coisa. E a unica fonte de verdade das
 * respostas, tanto do modelo (vai no prompt) quanto do plano B por palavras-chave (quando a IA esta desligada,
 * sem cota ou indisponivel). Contem so informacao publica do produto, nunca dados de uma familia.
 */
public final class AssistantKnowledge {

    /** Um topico: palavras que o ativam e a resposta pronta. */
    public record Topic(String title, List<String> keywords, String answer) {}

    static final List<Topic> TOPICS = List.of(
            new Topic("Lancar despesa ou receita",
                    List.of("despesa", "receita", "lancar", "lancamento", "gasto", "adicionar", "transacao", "registrar"),
                    "Abra Transacoes e use Nova despesa ou Nova receita. Informe valor, categoria e conta; a data de hoje "
                            + "e o membro ja vem preenchidos. Salve e o saldo da conta e atualizado."),
            new Topic("Comprovantes com IA",
                    List.of("comprovante", "recibo", "nota", "foto", "escanear", "ler"),
                    "Em Comprovantes (IA), envie uma foto ou PDF (JPEG, PNG ou PDF, ate 10 MB). O app sugere valor, data, "
                            + "estabelecimento e categoria; voce revisa e confirma, nada e lancado sozinho."),
            new Topic("Orcamentos",
                    List.of("orcamento", "teto", "limite", "estourar", "alerta"),
                    "Em Orcamentos defina um teto mensal por categoria. O app avisa quando voce se aproxima ou passa do limite."),
            new Topic("Metas",
                    List.of("meta", "objetivo", "poupar", "economizar", "guardar", "sonho"),
                    "Em Metas crie um objetivo com valor e prazo e registre aportes. O progresso aparece como porcentagem."),
            new Topic("Cartoes e parcelas",
                    List.of("cartao", "credito", "fatura", "parcela", "parcelado", "parcelamento"),
                    "Em Cartoes cadastre limite, fechamento e vencimento. Compras parceladas geram as parcelas dos proximos meses."),
            new Topic("Divisao de despesas",
                    List.of("dividir", "divisao", "rateio", "rachar", "racha", "quem deve", "dividas", "divida"),
                    "Em Divisao de despesas registre quem pagou e como dividir. O app calcula o menor numero de "
                            + "transferencias para todos ficarem quites."),
            new Topic("Membros da familia",
                    List.of("membro", "convidar", "familia", "usuario", "responsavel"),
                    "Em Usuarios o responsavel cadastra os membros. Cada familia so enxerga os proprios dados, e so o "
                            + "responsavel gerencia membros."),
            new Topic("Relatorios e exportacao",
                    List.of("relatorio", "exportar", "csv", "planilha", "excel", "grafico", "periodo"),
                    "Em Relatorios escolha o periodo e veja gastos por categoria. Voce pode exportar em CSV."),
            new Topic("Recorrentes e assinaturas",
                    List.of("recorrente", "assinatura", "mensal", "repetir", "automatico", "streaming"),
                    "Em Recorrentes e Assinaturas cadastre o que se repete todo mes (aluguel, streaming) para nao esquecer."),
            new Topic("Instalar como aplicativo",
                    List.of("instalar", "celular", "pwa", "offline", "tela inicial"),
                    "No celular, abra o site no navegador e escolha Adicionar a tela inicial (ou Instalar app). "
                            + "Ele abre em tela cheia; os dados precisam de internet."),
            new Topic("Conta e seguranca",
                    List.of("senha", "login", "entrar", "sair", "seguranca", "privacidade", "dados", "conta", "cadastro"),
                    "Sua conta e protegida por senha com bloqueio contra tentativas repetidas, e os dados de cada familia "
                            + "sao isolados. Para sair, use o menu do seu perfil.")
    );

    private AssistantKnowledge() {}

    /** Texto da base inteira, usado no prompt do modelo. */
    public static String asPrompt() {
        StringBuilder sb = new StringBuilder();
        for (Topic t : TOPICS) {
            sb.append("- ").append(t.title()).append(": ").append(t.answer()).append('\n');
        }
        return sb.toString();
    }

    /** Plano B: o topico com mais palavras-chave presentes na pergunta, se houver ao menos uma. */
    public static Optional<Topic> bestMatch(String question) {
        String q = " " + normalize(question) + " ";
        Topic best = null;
        int bestScore = 0;
        for (Topic t : TOPICS) {
            int score = 0;
            for (String k : t.keywords()) {
                if (q.contains(" " + k)) {
                    score++;
                }
            }
            if (score > bestScore) {
                best = t;
                bestScore = score;
            }
        }
        return Optional.ofNullable(best);
    }

    static String normalize(String s) {
        String n = Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ");
    }
}
