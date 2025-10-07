🐳 Configuração Docker - Backend NutriX

Este projeto utiliza Spring Boot + PostgreSQL rodando em containers Docker.
O objetivo é padronizar o ambiente de desenvolvimento entre todos os membros do grupo.

🚀 Primeira execução (setup inicial)

Clone o repositório e rode o projeto:

```bash
git clone <repo>
cd API-VI-Back-end
docker compose up --build -d
```

Isso vai:

Criar e subir o container do PostgreSQL

Buildar e subir o backend (Spring Boot)

Conectar automaticamente os dois

A API ficará disponível em:
👉 http://localhost:8080

🔁 Execuções futuras

Se não houver mudança no Dockerfile ou dependências, use:

```bash
docker compose up -d
```

Se alterar o código do backend (novas entidades, controllers, etc):

```bash
docker compose up --build -d
```

🧹 Reset geral (limpar containers e volumes)

Se quiser apagar tudo e começar do zero:

```bash
docker compose down -v
```

Depois suba novamente:

```bash
docker compose up --build -d
```

⚙️ Variáveis de ambiente (.env)

Crie um arquivo chamado .env na raiz do projeto com o seguinte conteúdo:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/nutrixdb
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

O docker-compose.yml já está configurado para usar essas variáveis automaticamente.

🔍 Logs e monitoramento

Ver logs do backend:

```bash
docker logs -f backend
```

Acessar o banco PostgreSQL dentro do container:

```bash
docker exec -it db psql -U postgres -d nutrixdb
```

🧑‍💻 Estrutura esperada do projeto

```bash
API-VI-Back-end/
│
├── src/
│   └── main/java/...      # Código do Spring Boot
│
├── Dockerfile             # Build do backend
├── docker-compose.yml     # Configuração do ambiente
├── .env                   # Variáveis de ambiente
└── README.md              # Este arquivo 😎
```

✅ Dicas finais

Todo mundo do grupo só precisa ter Docker e Docker Compose instalados.

O banco de dados e o backend são criados automaticamente.

Se quiser conectar via ferramenta tipo SQLTools, use:

```yaml
Host: localhost
Port: 5432
Database: nutrixdb
User: postgres
Password: postgres
```