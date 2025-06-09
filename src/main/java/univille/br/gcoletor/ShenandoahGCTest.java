package univille.br.gcoletor;
import java.util.*;
import java.util.concurrent.*;

/**
 * Teste otimizado para Shenandoah GC
 * Melhor para latência consistente e previsível
 */
public class ShenandoahGCTest {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private static final Map<String, GameState> activeGames = new ConcurrentHashMap<>();
    
    public static void main(String[] args) {
        System.out.println("=== TESTE SHENANDOAH GC ===");
        System.out.println("Recomendado para: latência ultra-consistente, jogos online, sistemas tempo real");
        System.out.println("Comando: java -XX:+UseShenandoahGC -Xms1g -Xmx2g -XX:+PrintGC ShenandoahGCTest");
        System.out.println("ATENÇÃO: Requer Java 12+ ou OpenJDK com Shenandoah");
        System.out.println();
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Simula sistema de jogos online com requisitos de latência
            simulateOnlineGameServer();
        } finally {
            scheduler.shutdown();
        }
        
        long endTime = System.currentTimeMillis();
        System.out.printf("%nTempo total: %d ms%n", endTime - startTime);
        System.out.printf("Jogos ativos finais: %d%n", activeGames.size());
    }
    
    private static void simulateOnlineGameServer() {
        System.out.println("Simulando servidor de jogos online...");
        
        // Inicia múltiplos jogos
        for (int gameId = 0; gameId < 20; gameId++) {
            startNewGame(gameId);
        }
        
        // Executa por 60 segundos simulando operação contínua
        long duration = 60000; // 60 segundos
        long startTime = System.currentTimeMillis();
        
        while (System.currentTimeMillis() - startTime < duration) {
            // Processa um frame do jogo
            processGameFrame();
            
            // Adiciona novos jogadores ocasionalmente
            if (Math.random() < 0.1) {
                addRandomPlayers();
            }
            
            // Remove jogadores inativos
            if (Math.random() < 0.05) {
                removeInactivePlayers();
            }
            
            // Mede latência crítica a cada segundo
            if ((System.currentTimeMillis() - startTime) % 1000 < 50) {
                measureCriticalLatency();
            }
            
            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException e) {
                break;
            }
        }
    }
    
    private static void startNewGame(int gameId) {
        GameState game = new GameState(gameId);
        activeGames.put("game_" + gameId, game);
        
        // Agenda atualizações regulares do jogo
        scheduler.scheduleAtFixedRate(() -> {
            updateGame(game);
        }, 0, 100, TimeUnit.MILLISECONDS); // Atualiza a cada 100ms
        
        System.out.println("Jogo " + gameId + " iniciado");
    }
    
    private static void processGameFrame() {
        // Processa um frame para todos os jogos ativos
        List<GameUpdate> updates = new ArrayList<>();
        
        for (GameState game : activeGames.values()) {
            GameUpdate update = new GameUpdate(game);
            updates.add(update);
        }
        
        // Processa atualizações em paralelo (crítico para latência)
        updates.parallelStream().forEach(GameUpdate::process);
    }
    
    private static void updateGame(GameState game) {
        // Atualização regular do estado do jogo
        game.updateState();
        
        // Cria objetos temporários (teste para Young Generation)
        List<GameEvent> events = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            events.add(new GameEvent(game.getId(), i));
        }
        
        // Processa eventos
        events.forEach(GameEvent::process);
    }
    
    private static void addRandomPlayers() {
        if (activeGames.size() > 0) {
            String[] gameIds = activeGames.keySet().toArray(new String[0]);
            String randomGameId = gameIds[(int)(Math.random() * gameIds.length)];
            GameState game = activeGames.get(randomGameId);
            
            if (game != null) {
                game.addPlayer();
            }
        }
    }
    
    private static void removeInactivePlayers() {
        activeGames.values().forEach(GameState::removeInactivePlayers);
    }
    
    private static void measureCriticalLatency() {
        // Simula operação crítica de latência (como processamento de input)
        long start = System.nanoTime();
        
        // Operação que deve ter latência mínima
        List<CriticalOperation> operations = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            operations.add(new CriticalOperation(i));
        }
        
        operations.forEach(CriticalOperation::execute);
        
        long end = System.nanoTime();
        double latency = (end - start) / 1_000_000.0;
        
        System.out.printf("Latência crítica: %.3f ms - Jogos ativos: %d%n", 
            latency, activeGames.size());
        
        // Força GC para medir pausa
        if (Math.random() < 0.2) { // 20% das vezes
            long gcStart = System.nanoTime();
            System.gc();
            long gcEnd = System.nanoTime();
            System.out.printf("Pausa Shenandoah: %.3f ms%n", (gcEnd - gcStart) / 1_000_000.0);
        }
    }
    
    static class GameState {
        private int gameId;
        private List<Player> players;
        private GameWorld world;
        private long lastUpdate;
        private int frameCount;
        
        public GameState(int gameId) {
            this.gameId = gameId;
            this.players = new ArrayList<>();
            this.world = new GameWorld(gameId);
            this.lastUpdate = System.currentTimeMillis();
            this.frameCount = 0;
            
            // Adiciona jogadores iniciais
            for (int i = 0; i < 5; i++) {
                players.add(new Player(gameId, i));
            }
        }
        
        public void updateState() {
            this.lastUpdate = System.currentTimeMillis();
            this.frameCount++;
            
            // Atualiza jogadores
            players.forEach(Player::update);
            
            // Atualiza mundo
            world.update();
        }
        
        public void addPlayer() {
            int playerId = players.size();
            players.add(new Player(gameId, playerId));
            System.out.printf("Jogador %d adicionado ao jogo %d%n", playerId, gameId);
        }
        
        public void removeInactivePlayers() {
            players.removeIf(Player::isInactive);
        }
        
        public int getId() {
            return gameId;
        }
    }
    
    static class Player {
        private int gameId;
        private int playerId;
        private Position position;
        private PlayerStats stats;
        private List<Action> recentActions;
        private long lastActivity;
        
        public Player(int gameId, int playerId) {
            this.gameId = gameId;
            this.playerId = playerId;
            this.position = new Position();
            this.stats = new PlayerStats();
            this.recentActions = new ArrayList<>();
            this.lastActivity = System.currentTimeMillis();
        }
        
        public void update() {
            // Simula atualização do jogador
            position.update();
            stats.update();
            
            // Adiciona ação aleatória
            if (Math.random() < 0.3) {
                recentActions.add(new Action(playerId));
                this.lastActivity = System.currentTimeMillis();
            }
            
            // Limita histórico de ações
            if (recentActions.size() > 10) {
                recentActions.remove(0);
            }
        }
        
        public boolean isInactive() {
            return System.currentTimeMillis() - lastActivity > 10000; // 10 segundos
        }
    }
    
    static class GameWorld {
        private int worldId;
        private List<WorldObject> objects;
        private Environment environment;
        
        public GameWorld(int worldId) {
            this.worldId = worldId;
            this.objects = new ArrayList<>();
            this.environment = new Environment();
            
            // Cria objetos do mundo
            for (int i = 0; i < 100; i++) {
                objects.add(new WorldObject(i));
            }
        }
        
        public void update() {
            // Atualiza objetos do mundo
            objects.forEach(WorldObject::update);
            environment.update();
            
            // Ocasionalmente adiciona/remove objetos
            if (Math.random() < 0.1) {
                objects.add(new WorldObject(objects.size()));
            }
            if (objects.size() > 150 && Math.random() < 0.1) {
                objects.remove(objects.size() - 1);
            }
        }
    }
    
    static class GameUpdate {
        private GameState game;
        private long timestamp;
        private List<UpdateTask> tasks;
        
        public GameUpdate(GameState game) {
            this.game = game;
            this.timestamp = System.currentTimeMillis();
            this.tasks = new ArrayList<>();
            
            // Cria tarefas de atualização
            for (int i = 0; i < 10; i++) {
                tasks.add(new UpdateTask(game.getId(), i));
            }
        }
        
        public void process() {
            // Processa todas as tarefas
            tasks.forEach(UpdateTask::execute);
        }
    }
    
    static class GameEvent {
        private int gameId;
        private int eventId;
        private String eventType;
        private Map<String, Object> data;
        private long timestamp;
        
        public GameEvent(int gameId, int eventId) {
            this.gameId = gameId;
            this.eventId = eventId;
            this.eventType = "Event_" + (eventId % 5);
            this.data = new HashMap<>();
            this.timestamp = System.currentTimeMillis();
            
            // Adiciona dados do evento
            data.put("gameId", gameId);
            data.put("eventId", eventId);
            data.put("value", Math.random() * 100);
        }
        
        public void process() {
            // Simula processamento do evento
            String result = eventType + "_processed_" + timestamp;
            data.put("result", result);
        }
    }
    
    static class CriticalOperation {
        private int operationId;
        private byte[] criticalData;
        private double result;
        
        public CriticalOperation(int operationId) {
            this.operationId = operationId;
            this.criticalData = new byte[256];
            Arrays.fill(criticalData, (byte) operationId);
        }
        
        public void execute() {
            // Operação que deve ter latência mínima
            double sum = 0;
            for (byte b : criticalData) {
                sum += Math.sqrt(Math.abs(b));
            }
            this.result = sum / criticalData.length;
        }
    }
    
    static class Position {
        private double x, y, z;
        
        public Position() {
            this.x = Math.random() * 100;
            this.y = Math.random() * 100;
            this.z = Math.random() * 100;
        }
        
        public void update() {
            x += (Math.random() - 0.5) * 2;
            y += (Math.random() - 0.5) * 2;
            z += (Math.random() - 0.5) * 2;
        }
    }
    
    static class PlayerStats {
        private int health;
        private int mana;
        private int experience;
        
        public PlayerStats() {
            this.health = 100;
            this.mana = 50;
            this.experience = 0;
        }
        
        public void update() {
            if (Math.random() < 0.1) experience++;
            if (health < 100 && Math.random() < 0.2) health++;
            if (mana < 50 && Math.random() < 0.3) mana++;
        }
    }
    
    static class Action {
        private int playerId;
        private String actionType;
        private long timestamp;
        
        public Action(int playerId) {
            this.playerId = playerId;
            this.actionType = "Action_" + (int)(Math.random() * 5);
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    static class WorldObject {
        private int objectId;
        private Position position;
        private String objectType;
        
        public WorldObject(int objectId) {
            this.objectId = objectId;
            this.position = new Position();
            this.objectType = "Object_" + (objectId % 10);
        }
        
        public void update() {
            if (Math.random() < 0.1) {
                position.update();
            }
        }
    }
    
    static class Environment {
        private double temperature;
        private double humidity;
        private String weather;
        
        public Environment() {
            this.temperature = 20 + Math.random() * 20;
            this.humidity = 0.3 + Math.random() * 0.4;
            this.weather = "sunny";
        }
        
        public void update() {
            temperature += (Math.random() - 0.5) * 2;
            humidity += (Math.random() - 0.5) * 0.1;
            if (Math.random() < 0.05) {
                weather = Math.random() < 0.5 ? "sunny" : "rainy";
            }
        }
    }
    
    static class UpdateTask {
        private int gameId;
        private int taskId;
        private String taskType;
        
        public UpdateTask(int gameId, int taskId) {
            this.gameId = gameId;
            this.taskId = taskId;
            this.taskType = "Task_" + (taskId % 3);
        }
        
        public void execute() {
            // Simula execução da tarefa
            String result = taskType + "_executed_" + System.currentTimeMillis();
        }
    }
}