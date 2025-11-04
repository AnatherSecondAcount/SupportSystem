package org.bstu.helpdesk.network;

public class NetworkManager {
    // Один-единственный статический экземпляр на все приложение
    private static final ClientNetwork network = new ClientNetwork();

    // Метод для получения этого экземпляра
    public static ClientNetwork getNetwork() {
        return network;
    }
}