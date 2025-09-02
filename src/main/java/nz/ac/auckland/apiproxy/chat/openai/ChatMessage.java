package nz.ac.auckland.apiproxy.chat.openai;

public class ChatMessage {

  private String role;
  private String content;
  private boolean isSystemPrompt;

  public ChatMessage(String role, String content) {
    this.role = role;
    this.content = content;
  }

  public String getRole() {
    return role;
  }

  public String getContent() {
    return content;
  }

  public void setSystemPrompt(boolean b) {
    this.isSystemPrompt = b;
  }

  public boolean isSystemPrompt() {
    return isSystemPrompt;
  }
}
