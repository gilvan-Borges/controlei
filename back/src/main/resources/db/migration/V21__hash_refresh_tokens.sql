-- Refresh tokens passam a ser guardados como SHA-256 (hex) do valor entregue ao cliente, nunca em texto puro.
-- A coluna "token" continua com o mesmo nome e tamanho, mas agora contem o hash.
--
-- Os tokens que ja existem estavam em texto puro e nao podem ser convertidos (o hash seria de um valor que o
-- cliente ainda tem, mas o banco nao deve manter nenhum token utilizavel). Por isso sao removidos: quem estiver
-- logado faz login de novo uma vez. E o preco de nao deixar tokens legiveis no banco.
DELETE FROM refresh_tokens;
