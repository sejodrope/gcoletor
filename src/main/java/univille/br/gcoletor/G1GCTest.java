package univille.br.gcoletor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Teste otimizado para G1 GC
 * Melhor para aplicações com pausas baixas e heaps grandes
 */
public class G1GCTest {
    private static Map<String, Object> longLivedCache = new ConcurrentHashMap<>();
    
    public static void main(String[] args) {
        System.out.println("=== TESTE G1 GC ===");
        System.out.println("Recomendado para: baixa latência, heaps grandes, servidores web");
        System.out.println("Comando: java -XX:+UseG1GC -Xms1g -Xmx2g -XX:MaxGCPauseMillis=100 -XX:+PrintGC G1GCTest");
        System.out.println();
        
        long startTime = System.currentTimeMillis();
        
        // Simula aplicação web com cache e requisições
        simulateWebApplication();
        
        long endTime = System.currentTimeMillis();
        System.out.printf("%nTempo total: %d ms%n", endTime - startTime);
        System.out.printf("Objetos no cache: %d%n", longLivedCache.size());
        System.out.printf("Memória utilizada: %.2f MB%n", 
            (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024.0 / 1024.0);
    }
    
    private static void simulateWebApplication() {
        // Simula 100 ciclos de requisições web
        for (int cycle = 0; cycle < 100; cycle++) {
            
            // Simula burst de requisições
            List<WebRequest> requests = new ArrayList<>();
            for (int req = 0; req < 200; req++) {
                requests.add(new WebRequest(cycle, req));
            }
            
            // Processa requisições
            for (WebRequest request : requests) {
                processRequest(request);
            }
            
            // Adiciona dados ao cache de longa duração (Old Generation)
            if (cycle % 5 == 0) {
                addToCache(cycle);
            }
            
            // Limpa cache antigo ocasionalmente
            if (cycle % 20 == 0) {
                cleanOldCache();
            }
            
            if (cycle % 10 == 0) {
                System.out.printf("Ciclo %d - Requisições: %d - Cache: %d - Memória: %.2f MB%n", 
                    cycle, requests.size(), longLivedCache.size(),
                    Runtime.getRuntime().freeMemory() / 1024.0 / 1024.0);
                
                // Mede pausa do GC
                long gcStart = System.nanoTime();
                System.gc();
                long gcEnd = System.nanoTime();
                System.out.printf("Pausa GC: %.2f ms%n", (gcEnd - gcStart) / 1_000_000.0);
            }
        }
    }
    
    private static void processRequest(WebRequest request) {
        // Simula processamento de requisição web
        request.process();
        
        // Cria objetos temporários (Young Generation)
        List<String> tempData = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            tempData.add("TempData_" + request.getId() + "_" + i);
        }
        
        // Processa dados temporários
        tempData.stream()
               .filter(s -> s.hashCode() % 3 == 0)
               .map(String::toUpperCase)
               .forEach(s -> { /* simula processamento */ });
    }
    
    private static void addToCache(int cycle) {
        // Adiciona dados que vivem por muito tempo (Old Generation)
        for (int i = 0; i < 10; i++) {
            String key = "cache_" + cycle + "_" + i;
            CachedData data = new CachedData(key, cycle);
            longLivedCache.put(key, data);
        }
    }
    
    private static void cleanOldCache() {
        // Remove entradas antigas do cache
        List<String> keysToRemove = new ArrayList<>();
        for (String key : longLivedCache.keySet()) {
            if (Math.random() < 0.3) { // Remove 30% aleatoriamente
                keysToRemove.add(key);
            }
        }
        keysToRemove.forEach(longLivedCache::remove);
        System.out.println("Cache limpo, removidas " + keysToRemove.size() + " entradas");
    }
    
    static class WebRequest {
        private int cycle;
        private int requestId;
        private String sessionId;
        private Map<String, String> parameters;
        private long timestamp;
        
        public WebRequest(int cycle, int requestId) {
            this.cycle = cycle;
            this.requestId = requestId;
            this.sessionId = "session_" + (requestId % 20); // Simula 20 sessões
            this.parameters = new HashMap<>();
            this.timestamp = System.currentTimeMillis();
            
            // Adiciona parâmetros simulados
            for (int i = 0; i < 5; i++) {
                parameters.put("param" + i, "value" + (requestId + i));
            }
        }
        
        public void process() {
            // Simula processamento da requisição
            String result = sessionId + "_processed_" + timestamp;
            for (String value : parameters.values()) {
                result += "_" + value.hashCode();
            }
        }
        
        public String getId() {
            return cycle + "_" + requestId;
        }
    }
    
    static class CachedData {
        private String key;
        private int cycle;
        private byte[] data;
        private long created;
        
        public CachedData(String key, int cycle) {
            this.key = key;
            this.cycle = cycle;
            this.created = System.currentTimeMillis();
            
            // Dados que ocupam espaço significativo
            this.data = new byte[1024]; // 1KB por entrada
            Arrays.fill(data, (byte) (cycle % 256));
        }
    }
}