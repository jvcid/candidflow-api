# Deploy do CandidFlow no Render

## 1. Preparar o repositório

Confirme que nenhuma credencial foi adicionada ao Git:

```powershell
git status --short
git grep -n "DB_PASSWORD"
```

O segundo comando pode encontrar apenas os nomes das variáveis, nunca a senha real.

Depois envie o projeto ao GitHub:

```powershell
git add .
git commit -m "feat: implement candidflow REST API"
git push origin main
```

## 2. Criar o Web Service

No Render:

1. Clique em **New** e **Web Service**.
2. Conecte o repositório do GitHub.
3. Escolha o runtime **Docker**.
4. Use o nome `candidflow-api`.
5. Selecione o plano gratuito.
6. Configure `/actuator/health` como Health Check Path.

O repositório também contém `render.yaml`, que permite utilizar **New → Blueprint** como alternativa.

## 3. Cadastrar variáveis

Na seção **Environment**, adicione:

```text
DB_URL       = jdbc:oracle:thin:@(description=...string completa candidflow_tp...)
DB_USERNAME  = CANDIDFLOW_APP
DB_PASSWORD  = senha do CANDIDFLOW_APP
HMAC_API_KEY = candidflow-client
HMAC_SECRET  = segredo aleatorio de pelo menos 32 bytes
JAVA_OPTS    = -XX:MaxRAMPercentage=75.0
```

No painel do Render, cole os valores sem aspas. Nunca adicione essas credenciais ao GitHub.

## 4. Autorizar o Render no Oracle

O primeiro deploy pode falhar porque o Oracle permite apenas o IP local cadastrado anteriormente. Depois que o Web Service existir:

1. Abra o serviço no Render.
2. Clique em **Connect**.
3. Abra a aba **Outbound**.
4. Copie todos os intervalos de IP apresentados em notação CIDR.
5. Abra `CandidFlowDB` na Oracle Cloud.
6. Em **Network**, edite a Access Control List.
7. Adicione todos os intervalos do Render.
8. Preserve também a regra do seu IP local.
9. Confirme que **Require mutual TLS (mTLS)** continua desativado.

Os intervalos são compartilhados e podem conter mais de um valor; todos precisam ser autorizados.

## 5. Refazer o deploy

No Render, selecione:

```text
Manual Deploy → Deploy latest commit
```

Nos logs, procure por:

```text
Successfully applied migration
Tomcat started on port
Started CandidflowApiApplication
```

## 6. Validar

Abra:

```text
https://SEU-SERVICO.onrender.com/actuator/health
```

Resposta esperada:

```json
{
  "status": "UP"
}
```

Atualize a variável `baseUrl` da coleção do Postman:

```text
http://localhost:8080
```

para:

```text
https://SEU-SERVICO.onrender.com
```

Execute novamente os 25 itens em ordem.

## Diagnóstico rápido

| Erro | Causa provável |
|---|---|
| Timeout ao conectar ao Oracle | IPs de saída do Render não estão na ACL |
| `ORA-01017` | Usuário ou senha incorretos |
| `ORA-01045` | Falta `CREATE SESSION` |
| Flyway acusa schema `ADMIN` | `DB_USERNAME` ainda está como `ADMIN` |
| Porta não detectada | `server.port` não está usando `${PORT:8080}` |
| Health check falha | Aplicação não iniciou ou banco não está acessível |
