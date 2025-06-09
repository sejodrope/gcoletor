package univille.br.gcoletor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Teste otimizado para Parallel GC
 * Melhor para aplicações com alto throughput e múltiplas threads
 */
public class ParallelGCTest {
    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    
    public static void main(String[] args) {
        System.out.println("=== TESTE PARALLEL GC ===");
        System.out.println("Recomendado para: aplicações batch, alto throughput, múltiplas threads");
        System.out.println("Comando: java -XX:+UseParallelGC -Xms512m -Xmx1g -XX:+PrintGC ParallelGCTest");
        System.out.println("Threads disponíveis: " + NUM_THREADS);
        System.out.println();
        
        long startTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        
        try {
            // Teste com alta carga de trabalho paralelo
            for (int batch = 0; batch < 20; batch++) {
                List<Future<?>> futures = new ArrayList<>();
                
                // Cria tarefas paralelas
                for (int thread = 0; thread < NUM_THREADS; thread++) {
                    final int threadId = thread;
                    final int batchId = batch;
                    
                    Future<?> future = executor.submit(() -> {
                        processBatch(threadId, batchId);
                    });
                    futures.add(future);
                }
                
                // Aguarda todas as tarefas terminarem
                for (Future<?> future : futures) {
                    try {
                        future.get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                if (batch % 5 == 0) {
                    System.out.printf("Batch %d completo - Memória livre: %.2f MB%n", 
                        batch, Runtime.getRuntime().freeMemory() / 1024.0 / 1024.0);
                    
                    // Força GC para medir performance
                    long gcStart = System.currentTimeMillis();
                    System.gc();
                    long gcEnd = System.currentTimeMillis();
                    System.out.println("GC paralelo executado em: " + (gcEnd - gcStart) + "ms");
                }
            }
        } finally {
            executor.shutdown();
        }
        
        long endTime = System.currentTimeMillis();
        System.out.printf("%nTempo total: %d ms%n", endTime - startTime);
        System.out.printf("Throughput: %.2f batches/segundo%n", 20.0 / ((endTime - startTime) / 1000.0));
    }
    
    private static void processBatch(int threadId, int batchId) {
        List<WorkObject> objects = new ArrayList<>();
        
        // Cria objetos de trabalho
        for (int i = 0; i < 2000; i++) {
            objects.add(new WorkObject(threadId, batchId, i));
        }
        
        // Processa intensivamente
        for (WorkObject obj : objects) {
            obj.heavyWork();
        }
        
        // Simula processamento adicional
        objects.parallelStream().forEach(WorkObject::parallelWork);
    }
    
    static class WorkObject {
        private int threadId;
        private int batchId;
        private int objectId;
        private double[] data;
        private String metadata;
        
        public WorkObject(int threadId, int batchId, int objectId) {
            this.threadId = threadId;
            this.batchId = batchId;
            this.objectId = objectId;
            this.data = new double[50];
            this.metadata = String.format("T%d-B%d-O%d", threadId, batchId, objectId);
            
            // Preenche array com dados
            for (int i = 0; i < data.length; i++) {
                this.data[i] = Math.random() * 1000;
            }
        }
        
        public void heavyWork() {
            // Simula processamento pesado
            for (int i = 0; i < data.length - 1; i++) {
                data[i] = Math.sqrt(data[i]) + Math.sin(data[i+1]);
            }
        }
        
        public void parallelWork() {
            // Trabalho adicional para streams paralelas
            double sum = 0;
            for (double value : data) {
                sum += Math.pow(value, 2);
            }
            this.metadata += "_" + (int)sum;
        }
    }
}