package br.com.controlei.application.services.assistant;

import br.com.controlei.application.exceptions.VoiceException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

/**
 * Audio enviado ao assistente, ja validado: ate {@value #MAX_BYTES} bytes, um dos tipos aceitos, com os magic bytes
 * conferidos (o Content-Type vem do navegador e nao prova nada) e no maximo {@value #MAX_DURATION_MS} ms.
 *
 * <p>Duracao: o WAV traz a duracao no cabecalho e e conferido exatamente. WebM, OGG, MP3 e MP4 gravados pelo
 * navegador nao tem duracao confiavel sem decodificar o audio; para eles valem o teto de tamanho (2 MB e cerca de
 * 60 s de Opus a 256 kbps), o corte de 60 s do proprio gravador e a duracao declarada pelo cliente, se vier.
 *
 * @param bytes      conteudo; so em memoria, nunca gravado
 * @param mimeType   tipo normalizado, sem parametros (audio/webm;codecs=opus vira audio/webm)
 * @param durationMs duracao conhecida (WAV) ou declarada; null quando desconhecida
 */
public record AudioClip(byte[] bytes, String mimeType, Long durationMs) {

    public static final int MAX_BYTES = 2 * 1024 * 1024;
    public static final long MAX_DURATION_MS = 60_000;

    /** Tipo aceito e a extensao que vai no nome do arquivo para o provedor. */
    static final Map<String, String> EXTENSIONS = Map.of(
            "audio/webm", "webm",
            "audio/ogg", "ogg",
            "audio/mpeg", "mp3",
            "audio/wav", "wav",
            "audio/mp4", "m4a");

    public static AudioClip of(byte[] bytes, String declaredType, Long declaredDurationMs) {
        if (bytes == null || bytes.length == 0) {
            throw VoiceException.badRequest("Nenhum áudio foi enviado.");
        }
        if (bytes.length > MAX_BYTES) {
            throw VoiceException.tooLarge("O áudio passa de 2 MB. Grave uma pergunta mais curta (até 60 segundos).");
        }
        String type = normalize(declaredType);
        if (!EXTENSIONS.containsKey(type)) {
            throw VoiceException.badRequest("Formato de áudio não aceito. Use WebM, OGG, MP3, WAV ou MP4.");
        }
        if (!contentMatches(type, bytes)) {
            throw VoiceException.badRequest("O conteúdo do arquivo não é um áudio " + EXTENSIONS.get(type).toUpperCase(Locale.ROOT) + " válido.");
        }
        if (declaredDurationMs != null && (declaredDurationMs < 0 || declaredDurationMs > MAX_DURATION_MS)) {
            throw VoiceException.tooLarge("O áudio passa de 60 segundos.");
        }
        Long duration = declaredDurationMs;
        if ("audio/wav".equals(type)) {
            duration = wavDurationMs(bytes);
            if (duration == null) {
                throw VoiceException.badRequest("Cabeçalho WAV inválido.");
            }
            if (duration > MAX_DURATION_MS) {
                throw VoiceException.tooLarge("O áudio passa de 60 segundos.");
            }
        }
        return new AudioClip(bytes, type, duration);
    }

    /** Nome sintetico para o provedor (que deduz o formato pela extensao). Nunca o nome enviado pela pessoa. */
    public String filename() {
        return "pergunta." + EXTENSIONS.get(mimeType);
    }

    static String normalize(String contentType) {
        if (contentType == null) {
            return "";
        }
        String base = contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        return switch (base) {
            case "audio/x-wav", "audio/wave", "audio/vnd.wave" -> "audio/wav";
            case "audio/mp3" -> "audio/mpeg";
            case "audio/x-m4a", "audio/m4a" -> "audio/mp4";
            default -> base;
        };
    }

    static boolean contentMatches(String type, byte[] b) {
        return switch (type) {
            // EBML; o DocType "webm" vem logo no cabecalho (um .mkv tem "matroska")
            case "audio/webm" -> startsWith(b, 0, 0x1A, 0x45, 0xDF, 0xA3) && contains(head(b, 64), "webm");
            case "audio/ogg" -> startsWith(b, 0, 'O', 'g', 'g', 'S');
            case "audio/wav" -> startsWith(b, 0, 'R', 'I', 'F', 'F') && startsWith(b, 8, 'W', 'A', 'V', 'E');
            case "audio/mp4" -> startsWith(b, 4, 'f', 't', 'y', 'p');
            // MP3 com tag ID3, ou direto o sincronismo de quadro (11 bits em 1)
            case "audio/mpeg" -> startsWith(b, 0, 'I', 'D', '3')
                    || (b.length > 1 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xE0) == 0xE0);
            default -> false;
        };
    }

    /** Duracao pelo cabecalho: bytes do bloco "data" dividido pela taxa de bytes por segundo do bloco "fmt ". */
    static Long wavDurationMs(byte[] b) {
        ByteBuffer buf = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
        long byteRate = 0;
        int pos = 12;
        while (pos + 8 <= b.length) {
            String id = new String(b, pos, 4, StandardCharsets.US_ASCII);
            long size = Integer.toUnsignedLong(buf.getInt(pos + 4));
            if ("fmt ".equals(id) && pos + 16 <= b.length) {
                byteRate = Integer.toUnsignedLong(buf.getInt(pos + 16));
            } else if ("data".equals(id)) {
                return byteRate == 0 ? null : size * 1000 / byteRate;
            }
            // Em long: um tamanho forjado no arquivo nao pode estourar a posicao
            long next = pos + 8L + size + (size & 1);
            if (next > b.length) {
                return null;
            }
            pos = (int) next;
        }
        return null;
    }

    private static boolean startsWith(byte[] b, int offset, int... expected) {
        if (b.length < offset + expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if ((b[offset + i] & 0xFF) != expected[i]) {
                return false;
            }
        }
        return true;
    }

    private static byte[] head(byte[] b, int n) {
        return Arrays.copyOf(b, Math.min(n, b.length));
    }

    private static boolean contains(byte[] haystack, String needle) {
        return new String(haystack, StandardCharsets.ISO_8859_1).contains(needle);
    }

    @Override
    public String toString() {
        // Nunca o conteudo: so o que pode ir para log
        return "AudioClip[" + bytes.length + " bytes, " + mimeType + ", " + durationMs + " ms]";
    }
}
