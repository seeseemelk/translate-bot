package be.seeseemelk.translatebot;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLEncoder;
import java.util.function.Consumer;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class TranslationEngine
{
	private final Logger logger = LogManager.getLogger("Translate");
	private final HttpClient client = HttpClients.createMinimal();
	
	public TranslationEngine()
	{
		
	}
	
	private String toLanguage(String code)
	{
		switch (code.toLowerCase())
		{
			case "en":
				return "0";
			case "se":
				return "48";
			default:
				throw new IllegalArgumentException("Unsupported language");
		}
	}
	
	private Reader request(String sourceLanguage, String targetLanguage, String message) throws IOException
	{
		String languagePair = URLEncoder.encode(sourceLanguage + "|" + targetLanguage, "UTF-8");
		String encodedMessage = URLEncoder.encode(message, "UTF-8");
		String uri = "http://translation.babylon-software.com/translate/babylon.php?v=1.0&q="+encodedMessage+"&langpair="+languagePair+"&callback=c&context=r";
		HttpGet request = new HttpGet(uri);
		HttpResponse response = client.execute(request);
		if (response.getStatusLine().getStatusCode() != 200)
		{
			logger.info("Request failed. Status is " + response.getStatusLine().toString());
			throw new IOException("Invalid status code");
		}
		return new InputStreamReader(response.getEntity().getContent());
	}
	
	public void translate(String languageCode, String message, Consumer<String> callback)
	{
		try
		{
			String sourceLanguage = toLanguage(languageCode);
			String targetLanguage;
			
			if (sourceLanguage.equals(toLanguage("en")))
				targetLanguage = toLanguage("se");
			else
				targetLanguage = toLanguage("en");
			
			Reader documentReader = request(sourceLanguage, targetLanguage, message);
			
			String rawTranslated = IOUtils.toString(documentReader);
			String extractedTranslated = rawTranslated.substring(26, rawTranslated.length() - 21).replace("\\\"", "\"");
			String paddedTranslated = "<html><body><div id=\"restext\">" + extractedTranslated + "</div></body></html>";
			
			Document document = Jsoup.parse(StringEscapeUtils.unescapeEcmaScript(paddedTranslated));
			String extractedText = document.getElementById("restext").text();
			callback.accept(extractedText);
			
		}
		catch (Exception e)
		{
			logger.throwing(e);
			logger.info("Failed to translate");
			callback.accept("Failed to translate. Reason: " + e.getMessage());
		}
	}
	
}
