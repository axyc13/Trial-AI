# Trial AI

This is a design project, completed under SOFTENG206 in collaboration with [Sophie](https://github.com/sophiec8081) and [Vincent](https://github.com/Comrademonke). 

Frontend with JavaFX and CSS, Backend with Java and utilising OpenAI API.
<hr />

Trial AI is an interactive 'game' that aims to teach users about the ethics of using AI. Set in the near future, an AI defendant appears before the court, having created a song that sounds eerily like other copyrighted works. 

You have 6 minutes to answer the question: <b>"Was the Defendant acting ethically?"</b>

Chat with three characters (an AI Witness, a Human Witness, and the defendant itself) and play with the interactable elements to uncover the truth. 

There may be conflicts in their stories – can you figure out the true narrative?




## How to try for yourself:

In your terminal:
```
git clone https://github.com/axyc13/Trial-AI.git trialAi
```
```
cd trialAi
```
Set up API keys in order to chat with characters:

- add in the root of the project (i.e., the same level where `pom.xml` is located) a file named `apiproxy.config`
- copy inside the following details:

  ```
  email: "YOUR_EMAIL"
  apiKey: "YOUR_KEY"
  ```
```
./mvnw clean javafx:run
```
