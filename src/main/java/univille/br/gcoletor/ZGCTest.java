package univille.br.gcoletor;
import java.util.*;
import java.util.concurrent.*;

/**
 * Teste otimizado para Z GC
 * Melhor para heaps muito grandes e latência extremamente baixa
 */
public class ZGCTest {
    private static final int HUGE_HEAP_SIMULATION = 1000000; // 1M objetos
    private static Map<String, LargeObject> massiveDataSet = new ConcurrentHashMap<>();
    
    public static void main(String[] args) {
        System.out.println("=== TESTE Z GC ===");
        System.out.println("Recomendado para: heaps gigantes (GB-TB), latência ultra-baixa");
        System.out.println("Comando: java -XX:+UseZGC -Xms2g -Xmx8g -XX:+PrintGC ZGCTest");
        System.out.println("ATENÇÃO: Requer Java 11+ e heap grande para melhor demonstração");
        System.out.println();
        
        long startTime = System.currentTimeMillis();
        
        // Teste 1: Carregamento de dataset massivo
        loadMassiveDataset();
        
        // Teste 2: Processamento concorrente com heap grande
        performConcurrentProcessing();
        
        // Teste 3: Simulação de aplicação financeira (HFT)
        simulateHighFrequencyTrading();
        
        long endTime = System.currentTimeMillis();
        System.out.printf("%nTempo total: %d ms%n", endTime - startTime);
        System.out.printf("Dataset size: %d objetos%n", massiveDataSet.size());
        System.out.printf("Memória utilizada: %.2f MB%n", 
            (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024.0 / 1024.0);
    }
    
    private static void loadMassiveDataset() {
        System.out.println("Carregando dataset massivo...");
        long start = System.currentTimeMillis();
        
        // Carrega grande quantidade de dados em memória
        for (int i = 0; i < HUGE_HEAP_SIMULATION / 10; i++) {
            String key = "data_" + i;
            LargeObject obj = new LargeObject(i);
            massiveDataSet.put(key, obj);
            
            if (i % 10000 == 0) {
                System.out.printf("Carregados %d objetos - Heap: %.2f MB%n", 
                    i, Runtime.getRuntime().totalMemory() / 1024.0 / 1024.0);
            }
        }
        
        long end = System.currentTimeMillis();
        System.out.printf("Dataset carregado em %d ms%n", end - start);
    }
    
    private static void performConcurrentProcessing() {
        System.out.println("Iniciando processamento concorrente...");
        ExecutorService executor = Executors.newFixedThreadPool(8);
        
        List<Future<?>> futures = new ArrayList<>();
        
        // Cria tarefas concorrentes que manipulam o heap grande
        for (int task = 0; task < 8; task++) {
            final int taskId = task;
            Future<?> future = executor.submit(() -> {
                processLargeDataChunk(taskId);
            });
            futures.add(future);
        }
        
        // Aguarda conclusão
        futures.forEach(f -> {
            try {
                f.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        executor.shutdown();
    }
    
    private static void processLargeDataChunk(int taskId) {
        List<LargeObject> workingSet = new ArrayList<>();
        
        // Cria objetos grandes para processamento
        for (int i = 0; i < 50000; i++) {
            workingSet.add(new LargeObject(taskId * 50000 + i));
        }
        
        // Processamento intensivo
        for (LargeObject obj : workingSet) {
            obj.intensiveComputation();
        }
        
        System.out.printf("Task %d processou %d objetos%n", taskId, workingSet.size());
    }
    
    private static void simulateHighFrequencyTrading() {
        System.out.println("Simulando High-Frequency Trading...");
        
        for (int round = 0; round < 50; round++) {
            long roundStart = System.nanoTime();
            
            // Simula processamento de alta frequência
            List<TradeOrder> orders = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                orders.add(new TradeOrder(round, i));
            }
            
            // Processamento crítico de latência
            orders.parallelStream().forEach(order -> {
                order.executeHighSpeedProcessing();
            });
            
            long roundEnd = System.nanoTime();
            double latency = (roundEnd - roundStart) / 1_000_000.0; // ms
            
            if (round % 10 == 0) {
                System.out.printf("Round %d - Latência: %.3f ms - Orders: %d%n", 
                    round, latency, orders.size());
                
                // Mede pausa ZGC (deve ser mínima)
                long gcStart = System.nanoTime();
                System.gc();
                long gcEnd = System.nanoTime();
                System.out.printf("Pausa ZGC: %.3f ms%n", (gcEnd - gcStart) / 1_000_000.0);
            }
        }
    }
    
    static class LargeObject {
        private int id;
        private byte[] largeData;
        private double[] calculations;
        private String metadata;
        private Map<String, Object> properties;
        
        public LargeObject(int id) {
            this.id = id;
            this.largeData = new byte[4096]; // 4KB por objeto
            this.calculations = new double[100];
            this.metadata = "LargeObject_" + id + "_" + System.nanoTime();
            this.properties = new HashMap<>();
            
            // Popula dados
            Arrays.fill(largeData, (byte) (id % 256));
            for (int i = 0; i < calculations.length; i++) {
                calculations[i] = Math.random() * 1000;
            }
            
            properties.put("created", System.currentTimeMillis());
            properties.put("size", largeData.length);
        }
        
        public void intensiveComputation() {
            // Simulação de processamento pesado
            for (int i = 0; i < calculations.length - 1; i++) {
                calculations[i] = Math.pow(calculations[i], 1.5) + Math.sqrt(calculations[i+1]);
            }
            
            // Modifica metadata
            this.metadata += "_processed_" + System.nanoTime();
        }
    }
    
    static class TradeOrder {
        private int round;
        private int orderId;
        private double price;
        private int quantity;
        private long timestamp;
        private String symbol;
        
        public TradeOrder(int round, int orderId) {
            this.round = round;
            this.orderId = orderId;
            this.price = 100 + Math.random() * 900; // $100-1000
            this.quantity = (int)(Math.random() * 1000) + 1;
            this.timestamp = System.nanoTime();
            this.symbol = "STOCK" + (orderId % 100);
        }
        
        public void executeHighSpeedProcessing() {
            // Simulação de processamento crítico de latência
            double risk = calculateRisk();
            boolean shouldExecute = risk < 0.8;
            
            if (shouldExecute) {
                executeOrder();
            }
        }
        
        private double calculateRisk() {
            return (price * quantity) / 1000000.0; // Risk simplificado
        }
        
        private void executeOrder() {
            // Simula execução da ordem
            this.timestamp = System.nanoTime();
        }
    }
}
