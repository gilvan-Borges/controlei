package br.com.controlei.application.services.assistant;

import br.com.controlei.application.exceptions.VoiceException;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** O Content-Type vem do navegador e nao prova nada: quem decide e o conteudo do arquivo. */
class AudioClipTest {

    private static final byte[] WEBM = VoiceAssistantServiceTest.WEBM;
    private static final byte[] OGG = {'O', 'g', 'g', 'S', 0, 2, 0, 0};
    private static final byte[] MP3 = {'I', 'D', '3', 4, 0, 0};
    private static final byte[] MP4 = {0, 0, 0, 0x20, 'f', 't', 'y', 'p', 'M', '4', 'A', ' '};

    /** WAV PCM 16 kHz mono 16 bits (32.000 bytes/s) com o tamanho de dados pedido. */
    static byte[] wav(int dataBytes) {
        ByteBuffer b = ByteBuffer.allocate(44 + dataBytes).order(ByteOrder.LITTLE_ENDIAN);
        b.put("RIFF".getBytes()).putInt(36 + dataBytes).put("WAVE".getBytes());
        b.put("fmt ".getBytes()).putInt(16).putShort((short) 1).putShort((short) 1).putInt(16_000).putInt(32_000)
                .putShort((short) 2).putShort((short) 16);
        b.put("data".getBytes()).putInt(dataBytes);
        return b.array();
    }

    private static int status(Runnable r) {
        return assertThrows(VoiceException.class, r::run).getStatus();
    }

    @Test
    void acceptsEachSupportedTypeByItsMagicBytes() {
        assertEquals("audio/webm", AudioClip.of(WEBM, "audio/webm;codecs=opus", null).mimeType());
        assertEquals("audio/ogg", AudioClip.of(OGG, "audio/ogg", null).mimeType());
        assertEquals("audio/mpeg", AudioClip.of(MP3, "audio/mpeg", null).mimeType());
        assertEquals("audio/mp4", AudioClip.of(MP4, "audio/mp4", null).mimeType());
        assertEquals("audio/wav", AudioClip.of(wav(32_000), "audio/wav", null).mimeType());
        assertEquals("pergunta.webm", AudioClip.of(WEBM, "audio/webm", null).filename());
    }

    @Test
    void rejectsATypeOutsideTheList() {
        assertEquals(400, status(() -> AudioClip.of(WEBM, "video/webm", null)));
        assertEquals(400, status(() -> AudioClip.of(WEBM, "application/octet-stream", null)));
        assertEquals(400, status(() -> AudioClip.of(WEBM, null, null)));
    }

    @Test
    void rejectsFakeMagicBytes() {
        byte[] html = "<html><script>alert(1)</script>".getBytes();
        assertEquals(400, status(() -> AudioClip.of(html, "audio/webm", null)));
        // Tipo declarado diferente do conteudo real: um OGG dizendo ser MP3
        assertEquals(400, status(() -> AudioClip.of(OGG, "audio/mpeg", null)));
        // EBML de um .mkv (sem o DocType webm) nao passa como WebM
        byte[] mkv = Arrays.copyOf(WEBM, WEBM.length);
        System.arraycopy("mkvx".getBytes(), 0, mkv, 8, 4);
        assertEquals(400, status(() -> AudioClip.of(mkv, "audio/webm", null)));
    }

    @Test
    void rejectsAnEmptyOrTooLargeFile() {
        assertEquals(400, status(() -> AudioClip.of(new byte[0], "audio/webm", null)));
        byte[] big = Arrays.copyOf(WEBM, AudioClip.MAX_BYTES + 1);
        assertEquals(413, status(() -> AudioClip.of(big, "audio/webm", null)));
    }

    @Test
    void enforcesSixtySecondsFromTheWavHeaderAndFromTheDeclaredDuration() {
        assertEquals(1_000L, AudioClip.of(wav(32_000), "audio/wav", null).durationMs());
        // 61 s de PCM a 32.000 bytes/s cabe em 2 MB: a duracao, nao o tamanho, e que barra
        assertEquals(413, status(() -> AudioClip.of(wav(61 * 32_000), "audio/wav", null)));
        assertEquals(413, status(() -> AudioClip.of(WEBM, "audio/webm", 61_000L)));
    }

    @Test
    void neverPrintsTheAudioContent() {
        assertEquals("AudioClip[16 bytes, audio/webm, 3000 ms]", AudioClip.of(WEBM, "audio/webm", 3_000L).toString());
    }
}
