package com.example.ocr.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import okhttp3.OkHttpClient;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Service
public class QuestionGenerationService {

	@Value("${openai.api.key}")
	private String openaiApiKey;

	public String generateQuestionsJson(File pdfFile, String subject, String difficulty, String questionTypes,
			int numQuestions, int totalMarks, String paperTitle) throws Exception {

		String text = extractTextFromPdf(pdfFile);
//		System.out.println("Error : "+ System.getProperty("java.io.tmpdir"));

		String prompt = buildPrompt(text, subject, difficulty, questionTypes, numQuestions, totalMarks, paperTitle);

		OpenAiService service = createOpenAiServiceWithCustomTimeout();
		ChatMessage userMessage = new ChatMessage("user", prompt);

		ChatCompletionRequest chatRequest = ChatCompletionRequest.builder().model("gpt-4-1106-preview")
				.messages(List.of(userMessage)).temperature(0.8).maxTokens(2000).build();

		String rawResponse = service.createChatCompletion(chatRequest)
	            .getChoices()
	            .get(0)
	            .getMessage()
	            .getContent();

	   
	    String cleanJson = rawResponse.replaceAll("(?i)```json|```", "").trim();

	    return cleanJson;
	}

//	private String extractTextFromPdf(File file) throws IOException {
//		try (PDDocument document = PDDocument.load(file)) {
//			return new PDFTextStripper().getText(document);
//		}
//	}
	
	private String extractTextFromPdf(File file) throws IOException {
	    String name = file.getName().toLowerCase();

	    if (name.endsWith(".pdf")) {
	        try (PDDocument document = PDDocument.load(file)) {
	            return new PDFTextStripper().getText(document);
	        }
	    } else if (name.endsWith(".docx")) {
	        try (FileInputStream fis = new FileInputStream(file);
	             XWPFDocument docx = new XWPFDocument(fis)) {
	            XWPFWordExtractor extractor = new XWPFWordExtractor(docx);
	            return extractor.getText();
	        }
	    } else if (name.endsWith(".txt")) {
	        return new String(java.nio.file.Files.readAllBytes(file.toPath()));
	    } else {
	        throw new IOException("Unsupported file type. Only PDF, DOCX, and TXT are supported.");
	    }
	}


	private String buildPrompt(String text, String subject, String difficulty, String questionTypes, int numQuestions,
			int totalMarks, String paperTitle) {
		return String.format(
				"""
						You are an AI designed to generate question papers from textbook content provided below.

						Respond ONLY with a valid JSON structure in the following format:
						{
						"title": "%s",
						"subject": "%s",
						"duration": "1 hour",
						"totalMarks": %d,
						"questions": [
						 {
						   "question": "What is the capital of France?",
						   "marks": 2,
						   "type": "MCQ",
						   "options": ["Berlin", "Paris", "London", "Rome"],
						   "correctAnswer": "Paris"
						 },
						 {
						   "question": "Explain the process of photosynthesis.",
						   "marks": 5,
						   "type": "Short",
						   "answer": "Photosynthesis is the process by which green plants convert sunlight into energy..."
						 }
						]
						} 

						Instructions:
						- Title: %s
						- Total questions: %d
						- Question types to include: %s
						- Difficulty level: %s
						- Include options and correct answer for MCQ-type questions.
						- Include answer text for Short and Long type questions.
						- Do NOT add explanations, pretext, or extra formatting—only output the JSON.
						- Do NOT use triple backticks or any markdown code blocks.
						- Respond with only the raw JSON object.
						
						Content:
						---
						%s
						""",
				paperTitle, subject, totalMarks, paperTitle, numQuestions, questionTypes, difficulty, text);
	}

	private OpenAiService createOpenAiServiceWithCustomTimeout() {
		return new OpenAiService(openaiApiKey, Duration.ofMinutes(10));
	}

}
