package br.com.controlei.application.services.receipt;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Leitura deterministica de texto de comprovante, por regras. E o caminho sem IA e o plano B quando a IA falha.
 * A confianca e honesta: so e alta quando valor e estabelecimento vieram de campos rotulados.
 */
@Component
public class RegexReceiptExtractor {

    private static final Pattern AMOUNT = Pattern.compile(
            "(?:R\\$\\s*|Valor:\\s*R?\\$?\\s*)(\\d+(?:\\.\\d{3})*[.,]\\d{2})", Pattern.CASE_INSENSITIVE);
    private static final Pattern MERCHANT = Pattern.compile(
            "(?:Estabelecimento|Favorecido|Destinatario|Loja|Beneficiario):\\s*([^\\n]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})");
    private static final DateTimeFormatter BR_DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    public ReceiptExtraction extract(String text, LocalDate today) {
        BigDecimal amount = null;
        Matcher am = AMOUNT.matcher(text);
        if (am.find()) {
            String v = am.group(1);
            if (v.contains(",")) {
                v = v.replace(".", "").replace(",", ".");
            }
            try {
                amount = new BigDecimal(v);
            } catch (NumberFormatException ignored) {
                // fica nulo
            }
        }

        String merchant = null;
        Matcher mm = MERCHANT.matcher(text);
        if (mm.find()) {
            merchant = mm.group(1).trim();
        } else {
            for (String line : text.split("\n")) {
                String l = line.toLowerCase();
                if (l.contains("supermercado") || l.contains("mercado") || l.contains("posto")
                        || l.contains("farmacia") || l.contains("restaurante")) {
                    merchant = line.trim();
                    break;
                }
            }
        }

        LocalDate date = today;
        Matcher dm = DATE.matcher(text);
        if (dm.find()) {
            try {
                date = LocalDate.parse(dm.group(1), BR_DATE);
            } catch (DateTimeParseException ignored) {
                // mantem hoje
            }
        }

        String confidence = amount != null && merchant != null ? "0.70" : amount != null ? "0.40" : "0.00";
        return new ReceiptExtraction(amount, date, merchant, null, new BigDecimal(confidence), text);
    }
}
