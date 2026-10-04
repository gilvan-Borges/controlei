package br.com.controlei.application.services.receipt;

/**
 * Descobre o tipo real de um arquivo pelos primeiros bytes. O Content-Type do upload vem do cliente e nao prova nada:
 * sem esta checagem qualquer arquivo seria enviado ao provedor de IA rotulado como imagem.
 */
public final class ReceiptFileType {

    public static final String JPEG = "image/jpeg";
    public static final String PNG = "image/png";
    public static final String PDF = "application/pdf";

    private ReceiptFileType() {
    }

    /** @return o tipo detectado, ou null se nao for JPEG, PNG nem PDF */
    public static String detect(byte[] b) {
        if (b == null || b.length < 5) {
            return null;
        }
        if ((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return JPEG;
        }
        if ((b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return PNG;
        }
        if (b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F' && b[4] == '-') {
            return PDF;
        }
        return null;
    }

    public static boolean isImage(String type) {
        return JPEG.equals(type) || PNG.equals(type);
    }

    /** Nome seguro para guardar: so o ultimo segmento, sem caracteres estranhos, com tamanho limitado. */
    public static String safeName(String original) {
        String name = original == null ? "" : original.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[^A-Za-z0-9._ -]", "_").trim();
        if (name.isEmpty() || name.chars().allMatch(c -> c == '.')) {
            return "comprovante";
        }
        return name.length() > 120 ? name.substring(name.length() - 120) : name;
    }
}
