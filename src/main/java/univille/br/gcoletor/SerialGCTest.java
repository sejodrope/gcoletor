package univille.br.gcoletor;
import java.util.ArrayList;
import java.util.List;

/**
 * Teste otimizado para Serial GC
 * Melhor para aplicações pequenas e single-threaded
 */
public class SerialGCTest {
    public static void main(String[] args) {
        System.out.println("=== TESTE SERIAL GC ===");
        System.out.println("Recomendado para: aplicações pequenas, máquinas com poucos recursos");
        System.out.println("Comando: java -XX:+UseSerialGC -Xms128m -Xmx256m -XX:+PrintGC SerialGCTest");
        System.out.println();
        
        long startTime = System.currentTimeMillis();
        
        // Teste com objetos pequenos (ideal para Serial GC)
        for (int cycle = 0; cycle < 50; cycle++) {
            List<SmallObject> objects = new ArrayList<>();
            
            // Cria muitos objetos pequenos
            for (int i = 0; i < 5000; i++) {
                objects.add(new SmallObject(i));
            }
            
            // Processa os objetos
            processSmallObjects(objects);
            
            if (cycle % 10 == 0) {
                System.out.printf("Ciclo %d - Objetos criados: %d - Memória livre: %.2f MB%n", 
                    cycle, objects.size(), 
                    Runtime.getRuntime().freeMemory() / 1024.0 / 1024.0);
            }
            
            // Força coleta
            if (cycle % 15 == 0) {
                long gcStart = System.currentTimeMillis();
                System.gc();
                long gcEnd = System.currentTimeMillis();
                System.out.println("GC executado em: " + (gcEnd - gcStart) + "ms");
            }
            
            objects.clear(); // Torna objetos elegíveis para GC
        }
        
        long endTime = System.currentTimeMillis();
        System.out.printf("%nTempo total: %d ms%n", endTime - startTime);
        System.out.printf("Memória final utilizada: %.2f MB%n", 
            (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024.0 / 1024.0);
    }
    
    private static void processSmallObjects(List<SmallObject> objects) {
        for (SmallObject obj : objects) {
            obj.doWork();
        }
    }
    
    static class SmallObject {
        private int id;
        private String name;
        private double value;
        
        public SmallObject(int id) {
            this.id = id;
            this.name = "Object_" + id;
            this.value = Math.random() * 100;
        }
        
        public void doWork() {
            // Trabalho simples
            this.value = Math.sqrt(this.value) + this.id;
        }
    }
}