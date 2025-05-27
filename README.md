# Braspag Automator V2

## Visão Geral
Braspag Automator V2 é uma aplicação Spring Boot projetada para automatizar a obtenção, processamento e geração de relatórios de dados de estabelecimentos (merchants) a partir da API 3.0 da Braspag. Integra scripts Python para extração de dados, armazena resultados em banco de dados e fornece uma API RESTful para gerenciamento e relatórios das informações dos estabelecimentos.

## Funcionalidades

- **Automação de Dados de Estabelecimentos**: Dispara scripts Python para obter dados de estabelecimentos na API 3.0 da Braspag, suportando consultas para um ou múltiplos ECs.
- **Armazenamento em Banco de Dados**: Persiste os dados dos estabelecimentos em banco relacional utilizando entidades JPA.
- **API REST**: Disponibiliza endpoints para:
  - Submeter estabelecimentos para automação (`POST /api/v1/retrieve-merchant`)
  - Listar todos os estabelecimentos e status da automação (`GET /api/v1/retrieve-merchant`)
  - Enviar relatórios Excel por e-mail (`GET /api/v1/retrieve-merchant/email`)
  - Excluir todos os registros de estabelecimentos (`DELETE /api/v1/retrieve-merchant`)
- **Geração de Relatórios Excel**: Gera arquivos Excel detalhados com informações e status dos estabelecimentos usando Apache POI.
- **Notificação por E-mail**: Envia relatórios Excel ou notificações de erro para e-mails especificados.
- **Validação de Header**: Protege os endpoints com validação de header de autorização.
- **Tratamento de Erros**: Loga e reporta erros, incluindo falhas na execução de scripts e entradas inválidas.
- **Processamento Assíncrono**: Executa tarefas longas (automação, envio de e-mails) de forma assíncrona para melhor desempenho.
- **Gerenciamento de Arquivos**: Remove arquivos temporários (Excel, JSON) após o processamento.

## Tecnologias Utilizadas
- Java 17+
- Spring Boot
- JPA/Hibernate
- Apache POI (geração de Excel)
- Python (extração de dados dos estabelecimentos)
- SLF4J (logging)

## Como Começar
1. **Configurar Ambiente**: Ajuste o `application.properties` e variáveis de ambiente com as credenciais da Braspag.
2. **Instalar Dependências Python**: Veja `python/requirements.txt`.
3. **Build e Execução**: Use Maven ou Docker para compilar e iniciar a aplicação.
4. **Uso da API**: Interaja com a API REST usando ferramentas como Postman ou curl. Todos os endpoints exigem header `Authorization`.

## Endpoints da API
- `POST /api/v1/retrieve-merchant` — Inicia automação para um ou mais estabelecimentos.
- `GET /api/v1/retrieve-merchant` — Lista estabelecimentos e status da automação.
- `GET /api/v1/retrieve-merchant/email` — Envia relatório Excel por e-mail.
- `DELETE /api/v1/retrieve-merchant` — Exclui todos os registros de estabelecimentos.

## Estrutura do Projeto
- `src/main/java/.../controller` — Controllers REST
- `src/main/java/.../service` — Lógica de negócio, manipulação de arquivos, e-mail, validação
- `src/main/java/.../model` — Entidades JPA
- `src/main/java/.../dto` — Objetos de transferência de dados
- `python/` — Scripts Python para extração de dados
