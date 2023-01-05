package application;

import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        ChatHost chatHost = new ChatHost();
        chatHost.sendMessage("Hello");
        chatHost.sendMessage("My");
        chatHost.sendMessage("name");
        chatHost.sendMessage("is");
        chatHost.sendMessage("Jonas");
        for(String string : chatHost.recieveMessages()){
            System.out.println(string);
        }
    }
}
