package icsdiscover.openai;

import java.text.BreakIterator;
import java.util.ArrayList;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ChatGPTService {
	
	//provide your API url here;
	private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent";

	// <Set OPENAI_DEMO_API_KEY in the system environment variable>
	//set here api key here
	private static final String KEY = System.getenv("AIzaSyCai8TZRvAxw52hAA92jQp0xo2V-BPZjU0");
//	OPENAI_DEMO_API_KEY
	private static final ObjectMapper OM = new ObjectMapper();

	@RegisterReflectionForBinding({ ChatGPTMessage.class })
	private ArrayList<ChatGPTMessage> parseMessages(String data) throws Exception {
		ArrayList<ChatGPTMessage> messageList = new ArrayList<>();
		BreakIterator bi = BreakIterator.getSentenceInstance();
		bi.setText(data);
		int index = 0;
		while (bi.next() != BreakIterator.DONE) {
			String sentence = data.substring(index, bi.current());
			messageList.add(new ChatGPTMessage(sentence.endsWith("?") ? "user" : "assistant", sentence));
			index = bi.current();
		}
		return messageList;
	}

	@RegisterReflectionForBinding({ ChatGPTRequest.class, ChatGPTResponse.class })
	public String parseUnstructuredData(String data) throws Exception {
		HttpMethod httpMethod = HttpMethod.POST;
		String requestPath = "chat/completions";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Bearer " + KEY);

		HttpEntity<String> entity = new HttpEntity<String>(
				OM.writeValueAsString(new ChatGPTRequest(parseMessages(data))), headers);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<ChatGPTResponse> responseEntity = restTemplate.exchange(API_URL + requestPath, httpMethod,
				entity, ChatGPTResponse.class);

		ChatGPTResponse response = responseEntity.getBody();

		if (responseEntity.getStatusCode() == HttpStatus.OK) {
			return response.choices().get(0).message().content();
		}

		return "";
	}

}
