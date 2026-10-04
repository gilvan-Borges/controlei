package br.com.controlei.application.services.receipt;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RegexReceiptExtractorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 4);
    private final RegexReceiptExtractor extractor = new RegexReceiptExtractor();

    @Test
    void readsLabeledFieldsWithHighConfidence() {
        var r = extractor.extract("Estabelecimento: Farmacia Drogasil\nValor: R$ 89,50\nData: 15/08/2026", TODAY);

        assertThat(r.amount()).isEqualByComparingTo("89.50");
        assertThat(r.merchant()).isEqualTo("Farmacia Drogasil");
        assertThat(r.date()).isEqualTo(LocalDate.of(2026, 8, 15));
        assertThat(r.confidence()).isEqualByComparingTo("0.70");
    }

    @Test
    void understandsThousandsSeparator() {
        assertThat(extractor.extract("Valor: R$ 1.234,56", TODAY).amount()).isEqualByComparingTo("1234.56");
    }

    @Test
    void anAmountWithoutMerchantIsLowConfidence() {
        var r = extractor.extract("Total R$ 20,00", TODAY);
        assertThat(r.amount()).isEqualByComparingTo("20.00");
        assertThat(r.confidence()).isEqualByComparingTo("0.40");
    }

    @Test
    void nothingRecognizedMeansZeroConfidenceAndTodayAsDate() {
        var r = extractor.extract("texto sem nada util", TODAY);
        assertThat(r.amount()).isNull();
        assertThat(r.merchant()).isNull();
        assertThat(r.date()).isEqualTo(TODAY);
        assertThat(r.confidence()).isEqualByComparingTo("0.00");
    }

    @Test
    void anImpossibleDateFallsBackToToday() {
        assertThat(extractor.extract("Valor: R$ 10,00\nData: 31/02/2026", TODAY).date()).isEqualTo(TODAY);
    }
}
