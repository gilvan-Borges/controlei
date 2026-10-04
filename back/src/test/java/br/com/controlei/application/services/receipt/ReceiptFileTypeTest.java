package br.com.controlei.application.services.receipt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReceiptFileTypeTest {

    @Test
    void recognisesJpegPngAndPdfByTheirFirstBytes() {
        assertThat(ReceiptFileType.detect(new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0})).isEqualTo("image/jpeg");
        assertThat(ReceiptFileType.detect(new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x0D})).isEqualTo("image/png");
        assertThat(ReceiptFileType.detect("%PDF-1.7".getBytes())).isEqualTo("application/pdf");
    }

    @Test
    void refusesEverythingElse() {
        assertThat(ReceiptFileType.detect("#!/bin/sh\necho".getBytes())).isNull();
        assertThat(ReceiptFileType.detect("<html><script>".getBytes())).isNull();
        assertThat(ReceiptFileType.detect("GIF89a....".getBytes())).isNull();
        assertThat(ReceiptFileType.detect(new byte[] {1, 2})).isNull();
        assertThat(ReceiptFileType.detect(null)).isNull();
    }

    @Test
    void safeNameKeepsOnlyTheLastSegmentAndHarmlessCharacters() {
        assertThat(ReceiptFileType.safeName("../../etc/passwd")).isEqualTo("passwd");
        assertThat(ReceiptFileType.safeName("C:\\Users\\x\\cupom fiscal.jpg")).isEqualTo("cupom fiscal.jpg");
        assertThat(ReceiptFileType.safeName("a<script>.jpg")).isEqualTo("a_script_.jpg");
        assertThat(ReceiptFileType.safeName("..")).isEqualTo("comprovante");
        assertThat(ReceiptFileType.safeName(null)).isEqualTo("comprovante");
        assertThat(ReceiptFileType.safeName("x".repeat(500))).hasSize(120);
    }
}
