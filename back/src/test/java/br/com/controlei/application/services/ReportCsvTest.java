package br.com.controlei.application.services;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Um membro malicioso controla descricao e observacao; o responsavel abre o CSV no Excel. */
class ReportCsvTest {

    @Test
    void formulasAreNeutralisedWithALeadingApostrophe() {
        assertThat(ReportService.csv("=HYPERLINK(\"http://x\")")).startsWith("'=");
        assertThat(ReportService.csv("+cmd|' /C calc'!A0")).startsWith("'+");
        assertThat(ReportService.csv("-2+3")).startsWith("'-");
        assertThat(ReportService.csv("@SUM(A1:A9)")).startsWith("'@");
        assertThat(ReportService.csv("\t=1+1")).startsWith("'\t");
    }

    @Test
    void lineBreaksCannotForgeExtraRows() {
        assertThat(ReportService.csv("Mercado\n2026-10-04;Pagamento;EXPENSE;;;;-1.00;PAID;"))
                .doesNotContain("\n").doesNotContain("\r").doesNotContain(";");
    }

    @Test
    void quotesAreDoubledAndOrdinaryTextIsUntouched() {
        assertThat(ReportService.csv("Pao \"fresco\"")).isEqualTo("Pao \"\"fresco\"\"");
        assertThat(ReportService.csv("Supermercado Central")).isEqualTo("Supermercado Central");
        assertThat(ReportService.csv(null)).isEmpty();
    }
}
