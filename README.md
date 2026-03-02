# Online Multiplayer Whist

Spring Boot + WebSocket backend with an HTML/CSS/JavaScript frontend for a playable multiplayer Whist MVP.

## Stack
- Java 21
- Spring Boot (Web + WebSocket)
- Maven Wrapper (`mvnw.cmd`)
- Vanilla HTML/CSS/JavaScript frontend in `src/main/resources/static`

## Run
1. Build:
   - `./mvnw.cmd -DskipTests compile`
2. Run tests:
   - `./mvnw.cmd test`
2. Start server:
   - `./mvnw.cmd spring-boot:run`
3. Open:
   - `http://localhost:8080`

## Debug
- Start with JDWP enabled:
  - `java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar ./target/whist-0.0.1-SNAPSHOT.jar --server.port=8081`
- Attach in VS Code using configuration:
  - `.vscode/launch.json` -> `Attach Whist Debug (5005)`

## How to Play (Current MVP)
- Open the app in 4 browser tabs/windows.
- In tab 1, enter your name and click `Create Room`.
- Copy the generated room ID and in tabs 2-4 enter a different name, paste the room ID, then click `Join Room`.
- Wait until all 4 players are shown in the `Players` panel; the game starts automatically.
- Auction phase: only the active player can click `Bid` or `Pass`.
- Trick phase: only the active player can click a playable card in hand.
- Follow-suit is enforced automatically; invalid plays are rejected with an error status.
- Continue until the game reaches `GAME_OVER` and winner is shown in status.

## Quick Start (in order)
1. Build and test once:
   - `./mvnw.cmd test`
2. Start server:
   - `./mvnw.cmd spring-boot:run`
3. Open browser:
   - `http://localhost:8080`
4. Run 4 players as described in **How to Play (Current MVP)**.

## Azure Deployment (App Service)
This project is configured with GitHub Actions workflow:
- `.github/workflows/deploy-azure-webapp.yml`

What you need once:
1. Create an Azure Web App (Linux, Java 21 runtime).
2. In Azure Portal, download the **Publish Profile** for that Web App.
3. In your GitHub repo settings, add secrets:
   - `AZURE_WEBAPP_NAME` = your web app name
   - `AZURE_WEBAPP_PUBLISH_PROFILE` = full publish profile XML content
4. Push to `main` (or run workflow manually from Actions tab).

Notes:
- App Service sets the `PORT` env var automatically; Spring Boot will bind correctly in Azure.
- The workflow builds and deploys `target/whist-0.0.1-SNAPSHOT.jar`.
- If you change `artifactId` or version in `pom.xml`, update the workflow package path.

## Current Implementation Scope
- Core game flow: auction -> trick play -> trick winner -> round progression -> game completion
- Rule checks: bid ordering, turn enforcement, follow-suit enforcement, illegal action rejection
- Multiplayer networking: room create/join, reconnect by player ID, per-player private snapshots
- Frontend gameplay: bid/pass actions, playable hand cards, table rendering, scoreboard and phase info
- Persistence/tournament stubs: `DataStore`, `FileStorage`, `TournamentManager`
- Tests: `BidTest`, `TrickTest`, `GameRoomTest`

## Next Milestones
- Expand scoring details to exact tournament rule variants if needed
- Add AI/bot fill for missing seats (optional)
- Add richer UI polish and card animations
