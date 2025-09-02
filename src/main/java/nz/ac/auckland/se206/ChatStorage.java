package nz.ac.auckland.se206;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;

public class ChatStorage {
  private static final Map<String, List<ChatMessage>> characterHistories =
      new ConcurrentHashMap<>();
  private static final Map<String, ChatMessage> systemPrompts = new ConcurrentHashMap<>();
  private static final List<ChatMessage> presentDayMessages = new CopyOnWriteArrayList<>();

  public static void addMessage(String profession, ChatMessage msg) {
    // Add all messages to map sorted by profession
    characterHistories.computeIfAbsent(profession, k -> new CopyOnWriteArrayList<>()).add(msg);

    // Adds all non-prompts to shared conversation map
    if (!msg.isSystemPrompt()) {
      presentDayMessages.add(msg);
    }
  }

  public static List<ChatMessage> getHistory(String profession) {
    return characterHistories.getOrDefault(profession, new ArrayList<>());
  }

  public static List<ChatMessage> getContext() {
    return new ArrayList<>(presentDayMessages);
  }

  public static void setSystemPrompt(String profession, ChatMessage systemPrompt) {
    systemPrompts.put(profession, systemPrompt);
  }

  public static ChatMessage getSystemPrompt(String profession) {
    return systemPrompts.get(profession);
  }
}
