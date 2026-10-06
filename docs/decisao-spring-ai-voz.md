# Decisão: Spring AI 2.0.1 para a voz do assistente

**Data:** 2026-10-06 · **Estado:** adotada

## Contexto

A voz do assistente precisa de duas coisas de um provedor externo: transcrição (speech-to-text) e síntese de fala (text-to-speech). O pedido era usar o **Spring AI** (`OpenAiAudioTranscriptionModel` e `OpenAiAudioSpeechModel`), com a condição de conferir antes a compatibilidade com o Spring Boot do projeto (4.1.x, Spring Framework 7). Se nenhuma versão estável fosse compatível, o plano B era implementar os adapters com `RestClient` direto nas rotas `/v1/audio/transcriptions` e `/v1/audio/speech`, mantendo as mesmas portas.

## O que foi verificado

| Item | Resultado |
|---|---|
| Versões publicadas do `spring-ai-bom` (Maven Central, 2026-10-06) | … 2.0.0-RC2, **2.0.0, 2.0.1** (estáveis), 2.1.0-M1 (milestone) |
| Base do Spring AI 2.0.1 | Spring Framework **7.0.9** (`spring-context-support` 7.0.9 no POM do `spring-ai-openai`): a mesma linha do Spring Boot 4.1.1 usado pelo projeto |
| Classes de áudio no `spring-ai-openai` 2.0.1 | `OpenAiAudioTranscriptionModel` e `OpenAiAudioSpeechModel`, montadas por *builder* com `OpenAiAudioTranscriptionOptions` / `OpenAiAudioSpeechOptions` (`apiKey`, `baseUrl`, `model`, `language`, `voice`, `responseFormat`, `timeout`, `maxRetries`) |
| Dependências que entram | `spring-ai-model`, `spring-ai-commons`, `openai-java-core` 4.49.0, `okhttp` 4.12.0 (com `kotlin-stdlib`). O `azure-identity` é opcional e **não** entra. O Jackson continua nas versões corrigidas que o projeto força |
| Subida e testes | O contexto sobe com a dependência no classpath (sem *starter*, nada se configura sozinho); a suíte inteira passou; o `VoiceAiAdaptersTest` exercita os dois modelos contra um servidor local que imita a API |

## Decisão

Usar o **Spring AI 2.0.1** pelo BOM oficial (`spring-ai-bom`), importando **só o módulo** `spring-ai-openai`, sem o `spring-ai-starter-model-openai`.

- **Por que a 2.0.1:** é a versão estável mais nova, feita sobre o Spring Framework 7 (o mesmo do Boot 4.1). A 2.1.0 ainda é milestone.
- **Por que sem o starter:** o starter configura modelos de chat, embeddings e áudio a partir de `spring.ai.openai.*` só por estar no classpath. Aqui os dois modelos são montados à mão em `VoiceAiConfig`, que só existe com a voz ligada e falha na subida se a chave faltar.
- **Onde fica:** só em `infrastructure/ai`. A aplicação conhece apenas as portas `SpeechToTextClient` e `TextToSpeechClient` (`application/contracts`); trocar de provedor ou voltar ao plano B (`RestClient`) não muda nada fora da infraestrutura.

## Configuração: sem variáveis próprias

A voz não ganhou variáveis de ambiente. Em `application.properties`, `controlei.voice.enabled`, `controlei.voice.api-key` e `controlei.voice.base-url` apontam para `controlei.ai.enabled`, `controlei.ai.api-key` e `controlei.ai.base-url`: a voz liga junto com a IA do servidor e usa a mesma chave e o mesmo provedor. Modelos (`whisper-1`, `tts-1`), voz (`alloy`) e a cota (20/dia por família) ficam fixos no arquivo.

### OpenRouter: fala pelo Spring AI, transcrição por adapter próprio

O provedor da IA é o OpenRouter, que tem as duas rotas de áudio, mas só uma no formato da OpenAI:

| Rota | Formato no OpenRouter | Adapter |
|---|---|---|
| `/audio/speech` | igual ao da OpenAI (JSON `model`, `input`, `voice`, `response_format`; resposta em bytes de áudio) | `OpenAiAudioSpeechModel` do **Spring AI** |
| `/audio/transcriptions` | **diferente:** JSON com `input_audio: { data (base64), format }`, `model`, `language`; resposta `{ text, usage }` | `OpenRouterSpeechToTextClient` com **RestClient** (o Spring AI manda multipart, que o OpenRouter não aceita) |

Modelos, escolhidos no catálogo do OpenRouter: **`openai/whisper-large-v3-turbo`** para transcrever (aceita webm, ogg, mp3, mp4 e wav; 99+ idiomas; US$ 0,000003/s, cerca de 33 vezes mais barato que o `openai/whisper-1`) e **`openai/gpt-4o-mini-tts-2025-12-15`** para falar (MP3, voz `alloy`). `controlei.voice.provider=openai` volta a transcrição para o Spring AI, para um provedor OpenAI de verdade.

Se o provedor recusar alguma das rotas, a transcrição falha com 422, o disjuntor da voz abre depois de 3 falhas e o chat de texto segue normal.

## Detalhe descoberto no teste

O `OpenAiAudioTranscriptionModel` pede `response_format=text`. Nesse formato a API devolve o texto puro (não JSON), e o adapter lê a transcrição direto do resultado do Spring AI. O teste do adapter confere `language=pt`, `response_format=text`, o nome do arquivo com a extensão do tipo e, na fala, modelo, voz e MP3.

## Consequências e o que observar

- O `okhttp` 4 traz o `kotlin-stdlib` para a imagem do back. O Trivy do CI passa a verificá-los; se aparecer vulnerabilidade com correção, sobe-se a versão pelo BOM ou por propriedade, como já é feito com Tomcat e Jackson.
- Ao subir o Spring Boot para uma linha nova (Framework 7.1+, por exemplo), conferir de novo a linha do Spring AI correspondente.
- O agente de texto **não** foi migrado para o `ChatClient` do Spring AI: a voz reutiliza o agente existente (com tool calling próprio) para não criar um segundo caminho de execução. Migrar o agente é uma decisão separada.
